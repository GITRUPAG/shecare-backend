package com.app.shecare.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct; // ✅ jakarta not javax
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {

        System.out.println("🔥 Firebase init starting...");

        try {
            if (FirebaseApp.getApps().isEmpty()) {

                FirebaseOptions options;

                String serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
                System.out.println("FIREBASE_SERVICE_ACCOUNT_JSON present: "
                    + (serviceAccountJson != null && !serviceAccountJson.isBlank()));

                if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
                    System.out.println("Using env variable credentials...");
                    InputStream stream = new ByteArrayInputStream(
                        serviceAccountJson.getBytes(StandardCharsets.UTF_8)
                    );
                    options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();

                } else {
                    try {
                        System.out.println("Trying service account file...");
                        InputStream serviceAccount =
                            new ClassPathResource("firebase-service-account.json").getInputStream();
                        options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                        System.out.println("✅ Using service account file");

                    } catch (IOException e) {
                        System.out.println("⚠️ No service account file, trying Application Default...");
                        options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();
                    }
                }

                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized. Apps: "
                    + FirebaseApp.getApps().size());

            } else {
                System.out.println("✅ Firebase already initialized.");
            }

        } catch (Exception e) {
            System.out.println("❌ Firebase init FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}