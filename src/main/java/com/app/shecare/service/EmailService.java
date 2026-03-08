package com.app.shecare.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    private static final String FROM_EMAIL = "support@shecare.fit";
    private static final String FROM_NAME  = "SheCare";
    private static final String REPLY_TO   = "shecare382@gmail.com";

    private SendGrid sendGrid;

    @PostConstruct
    public void init() {
        this.sendGrid = new SendGrid(sendGridApiKey); // ✅ created once
    }

    // ─── WELCOME EMAIL ────────────────────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        if (toEmail == null || toEmail.isBlank()) return;
        sendEmail(
            toEmail,
            "Welcome to SheCare 🌸 — Your Health Journey Begins",
            buildWelcomeEmailHtml(userName)
        );
    }

    // ─── PASSWORD RESET EMAIL ─────────────────────────────────────────────────
    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        if (toEmail == null || toEmail.isBlank()) return;
        sendEmail(
            toEmail,
            "SheCare — Password Reset Request 🔐",
            buildPasswordResetEmailHtml(userName, resetLink)
        );
    }

    // ─── CORE SEND METHOD ─────────────────────────────────────────────────────
    private void sendEmail(String toEmail, String subject, String htmlBody) {
        Email from      = new Email(FROM_EMAIL, FROM_NAME);
        Email to        = new Email(toEmail);
        Content content = new Content("text/html", htmlBody);
        Mail mail       = new Mail(from, subject, to, content);
        mail.setReplyTo(new Email(REPLY_TO, FROM_NAME + " Support"));

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("SendGrid error {} sending to {}: {}",
                    response.getStatusCode(), toEmail, response.getBody());
            } else {
                log.info("Email sent successfully to {}", toEmail); // ✅ only logs on success
            }

        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ─── HTML TEMPLATES ───────────────────────────────────────────────────────

    private String buildWelcomeEmailHtml(String userName) {
        String displayName = (userName != null && !userName.isBlank()) ? userName : "there";
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Welcome to SheCare</title>
            </head>
            <body style="margin:0;padding:0;background:#F9F0F5;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#F9F0F5;padding:40px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">

                    <tr>
                      <td style="background:linear-gradient(135deg,#D85E82 0%%,#80468E 100%%);border-radius:20px 20px 0 0;padding:48px 40px 36px;text-align:center;">
                        <div style="font-size:42px;margin-bottom:12px;">🌸</div>
                        <h1 style="margin:0;font-size:32px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;">SheCare</h1>
                        <p style="margin:6px 0 0;font-size:13px;color:rgba(255,255,255,0.75);letter-spacing:1.5px;text-transform:uppercase;">Your Body. Finally Understood.</p>
                      </td>
                    </tr>

                    <tr>
                      <td style="background:#ffffff;padding:40px 48px 32px;">
                        <h2 style="margin:0 0 14px;font-size:24px;font-weight:700;color:#2D1040;">Welcome, %s! 💖</h2>
                        <p style="margin:0 0 20px;font-size:16px;line-height:1.75;color:#603377;">
                          We're so glad you're here. SheCare was built with one mission —
                          to give every woman the tools and knowledge to truly understand her body.
                        </p>
                        <p style="margin:0 0 32px;font-size:15px;line-height:1.75;color:#7A5090;">
                          From AI-powered cycle predictions to early PCOS risk screening,
                          everything you need is right at your fingertips.
                        </p>
                        <div style="text-align:center;margin-bottom:36px;">
                          <a href="https://shecare.fit/dashboard"
                             style="display:inline-block;background:linear-gradient(135deg,#D85E82,#80468E);color:#ffffff;
                                    text-decoration:none;font-size:16px;font-weight:700;padding:16px 40px;
                                    border-radius:50px;box-shadow:0 8px 24px rgba(216,94,130,0.40);letter-spacing:0.3px;">
                            🌸 Go to My Dashboard →
                          </a>
                        </div>
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td style="padding:0 8px 16px 0;" width="50%%">
                              <div style="background:#FDF0F6;border-radius:14px;padding:20px 18px;border:1px solid #F0D8E8;">
                                <div style="font-size:24px;margin-bottom:8px;">🧠</div>
                                <p style="margin:0 0 4px;font-size:14px;font-weight:700;color:#2D1040;">AI Predictions</p>
                                <p style="margin:0;font-size:13px;color:#9A70A8;line-height:1.5;">94%% accurate cycle forecasting.</p>
                              </div>
                            </td>
                            <td style="padding:0 0 16px 8px;" width="50%%">
                              <div style="background:#FDF0F6;border-radius:14px;padding:20px 18px;border:1px solid #F0D8E8;">
                                <div style="font-size:24px;margin-bottom:8px;">🧬</div>
                                <p style="margin:0 0 4px;font-size:14px;font-weight:700;color:#2D1040;">PCOS Screening</p>
                                <p style="margin:0;font-size:13px;color:#9A70A8;line-height:1.5;">Early detection in under 3 minutes.</p>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:0 8px 0 0;" width="50%%">
                              <div style="background:#FDF0F6;border-radius:14px;padding:20px 18px;border:1px solid #F0D8E8;">
                                <div style="font-size:24px;margin-bottom:8px;">📊</div>
                                <p style="margin:0 0 4px;font-size:14px;font-weight:700;color:#2D1040;">Symptom Tracking</p>
                                <p style="margin:0;font-size:13px;color:#9A70A8;line-height:1.5;">Patterns surface automatically.</p>
                              </div>
                            </td>
                            <td style="padding:0 0 0 8px;" width="50%%">
                              <div style="background:#FDF0F6;border-radius:14px;padding:20px 18px;border:1px solid #F0D8E8;">
                                <div style="font-size:24px;margin-bottom:8px;">🔐</div>
                                <p style="margin:0 0 4px;font-size:14px;font-weight:700;color:#2D1040;">Privacy First</p>
                                <p style="margin:0;font-size:13px;color:#9A70A8;line-height:1.5;">Zero data sold. Ever.</p>
                              </div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <tr>
                      <td style="background:linear-gradient(135deg,#F3E6EE,#E8D0F0);padding:28px 48px;text-align:center;">
                        <p style="margin:0;font-size:16px;font-style:italic;color:#80468E;line-height:1.7;">
                          "Understanding your cycle is the first step to understanding yourself."
                        </p>
                        <p style="margin:8px 0 0;font-size:12px;color:#B090C0;font-weight:600;">— The SheCare Team</p>
                      </td>
                    </tr>

                    <tr>
                      <td style="background:#2D1040;border-radius:0 0 20px 20px;padding:28px 40px;text-align:center;">
                        <p style="margin:0 0 8px;font-size:13px;color:#9A70A8;">
                          Questions? <a href="mailto:support@shecare.fit" style="color:#D85E82;text-decoration:none;font-weight:600;">support@shecare.fit</a>
                        </p>
                        <p style="margin:0 0 14px;font-size:12px;color:#6B4080;">© 2026 SheCare · Built with 💗 for women everywhere</p>
                        <p style="margin:0;font-size:11px;color:#4A2860;">
                          <a href="https://shecare.fit/privacy" style="color:#6B4080;text-decoration:underline;">Privacy Policy</a>
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(displayName);
    }

    private String buildPasswordResetEmailHtml(String userName, String resetLink) {
        String displayName = (userName != null && !userName.isBlank()) ? userName : "there";
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Reset Your Password</title>
            </head>
            <body style="margin:0;padding:0;background:#F9F0F5;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#F9F0F5;padding:40px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">

                    <tr>
                      <td style="background:linear-gradient(135deg,#D85E82 0%%,#80468E 100%%);border-radius:20px 20px 0 0;padding:40px;text-align:center;">
                        <div style="font-size:36px;margin-bottom:10px;">🔐</div>
                        <h1 style="margin:0;font-size:26px;font-weight:700;color:#ffffff;">Password Reset Request</h1>
                        <p style="margin:6px 0 0;font-size:13px;color:rgba(255,255,255,0.70);">SheCare Account Security</p>
                      </td>
                    </tr>

                    <tr>
                      <td style="background:#ffffff;padding:40px 48px;">
                        <p style="margin:0 0 16px;font-size:16px;color:#2D1040;font-weight:700;">Hi %s,</p>
                        <p style="margin:0 0 20px;font-size:15px;line-height:1.75;color:#603377;">
                          We received a request to reset your SheCare password.
                          Click the button below — this link expires in <strong>15 minutes</strong>.
                        </p>
                        <div style="text-align:center;margin:32px 0;">
                          <a href="%s"
                             style="display:inline-block;background:linear-gradient(135deg,#D85E82,#80468E);color:#ffffff;
                                    text-decoration:none;font-size:16px;font-weight:700;padding:16px 40px;
                                    border-radius:50px;box-shadow:0 8px 24px rgba(216,94,130,0.40);">
                            Reset My Password →
                          </a>
                        </div>
                        <div style="background:#FFF5F8;border-left:4px solid #D85E82;border-radius:8px;padding:16px 20px;margin-bottom:24px;">
                          <p style="margin:0;font-size:13px;color:#9A70A8;line-height:1.6;">
                            🛡️ <strong>Didn't request this?</strong> You can safely ignore this email.
                            Your account remains secure and no changes have been made.
                          </p>
                        </div>
                        <p style="margin:0;font-size:13px;color:#B090C0;line-height:1.6;">
                          If the button doesn't work, paste this link into your browser:<br/>
                          <a href="%s" style="color:#D85E82;word-break:break-all;">%s</a>
                        </p>
                      </td>
                    </tr>

                    <tr>
                      <td style="background:#2D1040;border-radius:0 0 20px 20px;padding:28px 40px;text-align:center;">
                        <p style="margin:0 0 8px;font-size:13px;color:#9A70A8;">
                          Need help? <a href="mailto:support@shecare.fit" style="color:#D85E82;text-decoration:none;font-weight:600;">support@shecare.fit</a>
                        </p>
                        <p style="margin:0;font-size:12px;color:#6B4080;">© 2026 SheCare · Built with 💗 for women everywhere</p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(displayName, resetLink, resetLink, resetLink);
    }
}