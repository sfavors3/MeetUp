package com.pete.meetup.mail;

import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MailHandler extends javax.mail.Authenticator { 
	private String username; 
	private String password; 

	private String port = "25"; 
	private String host = "smtp.live.com"; 
 
	private boolean auth = true; 

	static {
		// There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added. 
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822"); 
		CommandMap.setDefaultCommandMap(mc); 
	} 

	public MailHandler(String username, String password) { 
		this.username = username; 
		this.password = password;
	} 

	/**
	 * Send the mail
	 * @return
	 * @throws Exception
	 */
	public boolean send(String from, String[] to,
			String subject, String body) throws Exception { 
		Properties props = setProperties(); 

		if(username != null &&
		   password != null &&
		   to != null &&
		   from != null) { 
			Session session = Session.getInstance(props, this); 

			MimeMessage msg = new MimeMessage(session); 

			InternetAddress[] addressTo = new InternetAddress[to.length]; 
			
			for (int i = 0; i < to.length; i++) { 
				addressTo[i] = new InternetAddress(to[i]); 
			}
		
			msg.setFrom(new InternetAddress(from)); 
			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo); 
			msg.setSubject(subject); 
			msg.setContent(body, "text/plain");
			
			// send email 
			Transport.send(msg); 

			return true; 
		} else { 
			return false; 
		} 
	} 

	@Override 
	public PasswordAuthentication getPasswordAuthentication() { 
		return new PasswordAuthentication(username, password); 
	} 

	private Properties setProperties() { 
		Properties props = new Properties(); 

		props.put("mail.smtp.host", host); 

		if(auth) { 
			props.put("mail.smtp.auth", "true"); 
		} 

		props.put("mail.smtp.port", port); 
		props.put("mail.smtp.starttls.enable","true");

		return props; 
	} 
}
