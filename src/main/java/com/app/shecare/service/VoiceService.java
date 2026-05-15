package com.app.shecare.service;

import com.app.shecare.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoiceService {

    private final ConversationService conversationService;

    private static final String FASTAPI_URL = "http://192.168.0.109:8001/stream";

    public void proxyStream(
            MultipartFile audio,
            String conversationIdStr,
            String token,
            User user,
            OutputStream clientOutput
    ) throws IOException {

        // 🔥 1. Conversation handling
        Conversation conversation = null;

        if (conversationIdStr != null && !conversationIdStr.isBlank()) {
            try {
                UUID conversationId = UUID.fromString(conversationIdStr);
                conversation = conversationService.getConversation(conversationId);

                if (!conversation.getUser().getId().equals(user.getId())) {
                    conversation = null;
                }

            } catch (Exception ignored) {}
        }

        if (conversation == null) {
            conversation = conversationService.createConversation(user);
        }

        System.out.println("Using conversation: " + conversation.getId());

        // 🔥 2. Setup multipart request
        String boundary = "----SheCareBoundary" + System.currentTimeMillis();

        URL url = new URL(FASTAPI_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream out = conn.getOutputStream();

        // 🔥 audio
        writeFilePart(out, boundary, "audio", audio);

        // 🔥 conversationId
        writeFormField(out, boundary, "conversationId", conversation.getId().toString());

        out.write(("--" + boundary + "--\r\n").getBytes());
        out.flush();

        // 🔥 3. STREAM + SAVE
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
        );

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(clientOutput, StandardCharsets.UTF_8)
        );

        String line;
        String transcript = null;
        StringBuilder reply = new StringBuilder();
        String emotion = "neutral";

        while ((line = reader.readLine()) != null) {

            writer.write(line + "\n");
            writer.flush();

            if (line.startsWith("data: ")) {
                String json = line.substring(6);

                if (json.contains("\"type\":\"transcript\"")) {
                    transcript = extract(json, "text");

                    conversationService.saveMessage(
                            conversation,
                            "user",
                            transcript,
                            null
                    );
                }

                if (json.contains("\"type\":\"emotion\"")) {
                    emotion = extract(json, "emotion");
                }

                if (json.contains("\"type\":\"chunk\"")) {
                    reply.append(extract(json, "text"));
                }

                if (json.contains("\"type\":\"done\"")) {

                    conversationService.saveMessage(
                            conversation,
                            "assistant",
                            reply.toString(),
                            emotion
                    );

                    conversationService.updateLastMessage(
                            conversation,
                            reply.toString()
                    );

                    // 🔥 send conversationId back
                    writer.write("data: {\"conversationId\":\"" + conversation.getId() + "\"}\n\n");
                    writer.flush();
                }
            }
        }
    }

    // 🔧 helpers
    private void writeFilePart(OutputStream out, String boundary, String name, MultipartFile file) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getOriginalFilename() + "\"\r\n").getBytes());
        out.write(("Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes());
        out.write(file.getBytes());
        out.write("\r\n".getBytes());
    }

    private void writeFormField(OutputStream out, String boundary, String name, String value) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        out.write((value + "\r\n").getBytes());
    }

    private String extract(String json, String key) {
        try {
            int start = json.indexOf("\"" + key + "\":\"") + key.length() + 4;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
}