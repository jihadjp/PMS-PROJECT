package com.jptechgenius.payroll.service;

import com.jptechgenius.payroll.model.User; // User মডেল ইমপোর্ট করা হলো
import com.jptechgenius.payroll.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    public void sendResetTokenEmail(String toEmail, String token, String resetUrl) {
        String userName = "User";
        Optional<User> userOpt = userRepository.findByEmail(toEmail);
        if (userOpt.isPresent()) {
            userName = userOpt.get().getFullName();
        }

        String subject = "Reset Your Password - Sal-Pay";
        String link = resetUrl + "?token=" + token;

        String content = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }"
                + ".container { max-width: 600px; margin: 30px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); border: 1px solid #e1e4e8; }"
                + ".header { background: linear-gradient(135deg, #4f46e5, #7c3aed); padding: 35px; text-align: center; color: white; }"
                + ".header h1 { margin: 0; font-size: 26px; font-weight: 700; letter-spacing: 1px; }"
                + ".content { padding: 40px 30px; text-align: center; color: #333; }"
                + ".content h2 { color: #1f2937; margin-top: 0; }"
                + ".content p { font-size: 16px; line-height: 1.6; color: #4b5563; margin-bottom: 25px; }"
                + ".btn { display: inline-block; background: #4f46e5; color: #ffffff !important; text-decoration: none; padding: 14px 35px; border-radius: 50px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 6px rgba(79, 70, 229, 0.2); transition: all 0.3s; }"
                + ".btn:hover { background: #4338ca; transform: translateY(-2px); box-shadow: 0 6px 12px rgba(79, 70, 229, 0.3); }"
                + ".footer { background: #f8fafc; padding: 25px; text-align: center; font-size: 13px; color: #6b7280; border-top: 1px solid #eee; }"
                + ".footer a { color: #4f46e5; text-decoration: none; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"

                // --- Header with Logo ---
                + "  <div class='header'>"
                + "    <h1>Payroll System</h1>"
                + "    <p style='margin: 5px 0 0; opacity: 0.9; font-size: 14px;'>Secure Account Recovery</p>"
                + "  </div>"

                // --- Main Content ---
                + "  <div class='content'>"
                + "    <h2>Password Reset Request</h2>"
                + "    <p>Hello <strong>" + userName + "</strong>,</p>"
                + "    <p>We received a request to reset the password for your account associated with <strong>" + toEmail + "</strong>.</p>"
                + "    <p>Click the button below to set a new password. This link is valid for <strong>30 minutes</strong>.</p>"
                + "    <a href=\"" + link + "\" class='btn'>Reset My Password</a>"
                + "    <p style='margin-top: 30px; font-size: 13px; color: #9ca3af;'>If you didn't request a password reset, you can safely ignore this email.</p>"
                + "  </div>"

                // --- Footer ---
                + "  <div class='footer'>"
                + "    &copy; 2025 <strong>Axiom Devs</strong>. All rights reserved.<br>"
                + "    Dhaka, Bangladesh | <a href='#'>Privacy Policy</a>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@axiomdevs.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}