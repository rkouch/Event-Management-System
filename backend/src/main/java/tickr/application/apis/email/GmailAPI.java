package tickr.application.apis.email;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import tickr.util.FileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class GmailAPI implements IEmailAPI {
    private static final String DEFAULT_EMAIL = "tickr@example.com";

    private Gmail gmailService;
    private GoogleCredentials googleCredential;

    public GmailAPI () {
        try {
            googleCredential = GoogleCredentials.fromStream(FileHelper.openInputStream("/credentials.json"))
                    .createScoped(GmailScopes.GMAIL_SEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get google credentials!", e);
        }

        var requestInitialiser = new HttpCredentialsAdapter(googleCredential);
        gmailService = new Gmail.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), requestInitialiser)
                .setApplicationName("tickr")
                .build();
    }

    @Override
    public void sendEmail (String toEmail, String subject, String body) {
        var message = makeMessage(toEmail, subject, body);

        try {
            message = gmailService.users().messages().send("me", message).execute();
        } catch (GoogleJsonResponseException e) {
            var err = e.getDetails();
            if (err.getCode() == 403) {
                throw new RuntimeException("Message send was forbidden!", e);
            } else {
                throw new RuntimeException("Message send failed unexpectedly!", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to send message!", e);
        }
    }

    private Message makeMessage (String toEmail, String subject, String body) {
        var properties = new Properties();
        var session = Session.getDefaultInstance(properties, null);

        var mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(DEFAULT_EMAIL));
            mimeMessage.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(toEmail));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(body);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Email creation failed!", e);
        }

        try {
            var buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            var bytes = buffer.toByteArray();
            var encodedEmail = Base64.encodeBase64URLSafeString(bytes);

            var message = new Message();
            message.setRaw(encodedEmail);

            return message;
        } catch (IOException | MessagingException e) {
            throw new RuntimeException("Email creation failed!", e);
        }
    }
}
