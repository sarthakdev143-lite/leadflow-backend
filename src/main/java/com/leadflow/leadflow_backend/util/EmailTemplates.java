package com.leadflow.leadflow_backend.util;

public class EmailTemplates {

    // ─── Welcome Email (AUTO_NEW_LEAD) ────────────────────────────────────────────
    public static String welcomeEmail(String name) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width:600px;margin:auto;padding:24px;border:1px solid #eee;border-radius:8px;">
                  <h2 style="color:#1a73e8;">Welcome to LeadFlow! 🎉</h2>
                  <p>Hi <strong>%s</strong>,</p>
                  <p>Thank you for your interest! We've received your details and our team will reach out to you shortly.</p>
                  <p>If you have any urgent questions, feel free to reply to this email.</p>
                  <br/>
                  <p>Best regards,</p>
                  <p><strong>LeadFlow Team</strong></p>
                </div>
              </body>
            </html>
            """.formatted(name);
    }

    // ─── Reminder Email (REMINDER) ───────────────────────────────────────────────
    public static String reminderEmail(String name) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width:600px;margin:auto;padding:24px;border:1px solid #eee;border-radius:8px;">
                  <h2 style="color:#f9ab00;">Just checking in! 👋</h2>
                  <p>Hi <strong>%s</strong>,</p>
                  <p>We noticed you showed interest in our services. Is there anything we can help you with?</p>
                  <p>Feel free to reply to this email with any questions.</p>
                  <br/>
                  <p>Best regards,</p>
                  <p><strong>LeadFlow Team</strong></p>
                </div>
              </body>
            </html>
            """.formatted(name);
    }

    // ─── Follow-up Email (FOLLOWUP) ──────────────────────────────────────────────
    public static String followupEmail(String name) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width:600px;margin:auto;padding:24px;border:1px solid #eee;border-radius:8px;">
                  <h2 style="color:#34a853;">Following up 📞</h2>
                  <p>Hi <strong>%s</strong>,</p>
                  <p>Following up on our previous conversation. Do you have any questions or would you like to discuss further?</p>
                  <p>We're here to help!</p>
                  <br/>
                  <p>Best regards,</p>
                  <p><strong>LeadFlow Team</strong></p>
                </div>
              </body>
            </html>
            """.formatted(name);
    }

    // ─── Manual Email (MANUAL) ───────────────────────────────────────────────────
    public static String manualEmail(String name) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width:600px;margin:auto;padding:24px;border:1px solid #eee;border-radius:8px;">
                  <h2 style="color:#333;">LeadFlow Update 📋</h2>
                  <p>Hi <strong>%s</strong>,</p>
                  <p>We'd love to hear from you! Please reach out if you have any questions.</p>
                  <br/>
                  <p>Best regards,</p>
                  <p><strong>LeadFlow Team</strong></p>
                </div>
              </body>
            </html>
            """.formatted(name);
    }
}
