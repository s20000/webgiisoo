package com.giisoo.utils.notify;

import java.io.*;
import java.util.Properties;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class Email.
 */
public class Email {

  /** The log. */
  static Log log = LogFactory.getLog(Email.class);
  
  /** The conf. */
  static Configuration conf;

  /**
   * Inits the.
   *
   * @param conf the conf
   */
  public static void init(Configuration conf) {
    if (Email.conf == null) {
      Email.conf = conf;
    }
  }

  /**
   * Send email.
   *
   * @param to the to
   * @param from the from
   * @param subject the subject
   * @param body the body
   * @return true, if successful
   * @deprecated 
   */
  public static boolean sendEmail(String to, String from, String subject, String body) {
    Properties props = new Properties();
    props.setProperty("mail.transport.protocol", conf.getString("mail.protocol"));
    props.setProperty("mail.host", conf.getString("mail.host"));
    props.setProperty("mail.user", conf.getString("mail.user"));
    props.setProperty("mail.password", conf.getString("mail.password"));

    try {
      Session mailSession = Session.getDefaultInstance(props, null);
      Transport transport = mailSession.getTransport();

      MimeMessage message = new MimeMessage(mailSession);
      message.setSubject(subject);
      message.setContent(body, "text/html; charset=UTF-8");
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      InternetAddress f = new InternetAddress(from);
      f.setPersonal(conf.getString("mail.user.display"));
      message.setFrom(f);

      transport.connect();
      transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
      transport.close();

      return true;

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return false;
  }

  /**
   * The Class GiisooAuthenticator.
   */
  static class GiisooAuthenticator extends Authenticator {
    
    /** The auth. */
    private static PasswordAuthentication auth = null;

    /* (non-Javadoc)
     * @see javax.mail.Authenticator#getPasswordAuthentication()
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      if (auth == null) {
        auth = new PasswordAuthentication(conf.getString("mail.user", "service@giisoo.com"), conf.getString("mail.password", "service123456"));
      }
      return auth;
    }
  }

  /** The Constant anthenticator. */
  private static final GiisooAuthenticator anthenticator = new GiisooAuthenticator();

  /**
   * Send.
   *
   * @param subject the subject
   * @param body the body
   * @param to the to
   * @param attachments the attachments
   * @param names the names
   * @param contents the contents
   * @return true, if successful
   */
  public static boolean send(String subject, String body, String to, InputStream[] attachments, String[] names, String[] contents) {
    Properties props = new Properties();

    props.setProperty("mail.transport.protocol", conf.getString("mail.protocol", "smtp"));
    props.setProperty("mail.host", conf.getString("mail.host", "smtp.exmail.qq.com"));
    props.setProperty("mail.smtp.auth", "true");
    // props.setProperty("mail.user", conf.getString("mail.user"));
    // props.setProperty("mail.password", conf.getString("mail.password"));

    try {
      Session mailSession = Session.getDefaultInstance(props, anthenticator);
      Transport transport = mailSession.getTransport();

      MimeMessage message = new MimeMessage(mailSession);
      message.setSubject(subject, "utf-8");

      BodyPart messageBodyPart = new MimeBodyPart();
      body = body.replaceAll("\r", "<br/>").replaceAll(" ", "&nbsp;");
      messageBodyPart.setContent(body, "text/html; charset=utf-8");
      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(messageBodyPart);

      if (attachments != null) {

        for (int i = 0; i < attachments.length; i++) {
          InputStream in = attachments[i];
          BodyPart attachmentPart = new MimeBodyPart();
          DataSource source = new ByteArrayDataSource(in, contents[i]);
          attachmentPart.setDataHandler(new DataHandler(source));
          attachmentPart.setFileName(names[i]);
          multipart.addBodyPart(attachmentPart);
        }
      }

      message.setContent(multipart);
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      InternetAddress f = new InternetAddress("service@giisoo.com");
      f.setPersonal(conf.getString("mail.user.display"));
      message.setFrom(f);

      transport.connect();
      transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
      transport.close();
      return true;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return false;
  }

  /**
   * Send.
   *
   * @param subject the subject
   * @param body the body
   * @param to the to
   * @return true, if successful
   */
  public static boolean send(String subject, String body, String to) {
    return send(subject, body, to, null, null, null);
  }

  /**
   * Send.
   *
   * @param subject the subject
   * @param body the body
   * @param to the to
   * @param from the from
   * @param displayname the displayname
   * @return true, if successful
   */
  public static boolean send(String subject, String body, String to, String from, String displayname) {
    Properties props = new Properties();

    props.setProperty("mail.transport.protocol", conf.getString("mail.protocol", "smtp"));
    props.setProperty("mail.host", conf.getString("mail.host", "smtp.exmail.qq.com"));
    props.setProperty("mail.smtp.auth", "true");

    try {
      Session mailSession = Session.getDefaultInstance(props, anthenticator);
      Transport transport = mailSession.getTransport();

      MimeMessage message = new MimeMessage(mailSession);
      message.setSubject(subject);
      message.setContent(body, "text/html; charset=UTF-8");
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      InternetAddress f = new InternetAddress(from);
      f.setPersonal(displayname);
      message.setFrom(f);

      transport.connect();
      transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
      transport.close();
      return true;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return false;
  }

}
