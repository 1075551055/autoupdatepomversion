package util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    public static void sendEmail(String emailContent) throws MessagingException {
        String to = "water.li@test.com";
        String from = "AutoUpdatePom@test.com";
        String host = "localhost";
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.auth", "false");
        props.setProperty("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", 25);
        Session session = Session.getInstance(props);
        Transport transport = session.getTransport();
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            Address[] targetAddresses = {new InternetAddress(to)};
            message.addRecipients(Message.RecipientType.TO, targetAddresses);
            message.setSubject("Auto update pom version");
            message.setContent(emailContent, "text/html");
            transport.send(message);
            System.out.println("Sent email successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
}
