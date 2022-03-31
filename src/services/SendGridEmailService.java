package services;

import com.sendgrid.*;

import java.io.IOException;

/**
 * SendGridEmailService Class provide service to send Email's
 * @author anilk
 *
 */
public class SendGridEmailService {
	private static String APIKEY="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";
	private static String senderEmail = "anil.khadka3777@gmail.com";
	
	/**
	 * 
	 * @param subject Subject of the Email
	 * @param toEmail Receiver of the Email
	 * @param contents Contents of Email in plain text
	 * @return status code email service
	 * @throws IOException
	 */
	public static int sendEmail(String subject, String toEmail, String contents) throws IOException {
		Email from = new Email(senderEmail);
	    Email to = new Email(toEmail);
	    Content content = new Content("text/plain", contents);
	    Mail mail = new Mail(from, subject, to, content);

	    SendGrid sg = new SendGrid(APIKEY);
	    Request request = new Request();
	    try {
	      request.setMethod(Method.POST);
	      request.setEndpoint("mail/send");
	      request.setBody(mail.build());
	      Response response = sg.api(request);
	      System.out.println(response.getStatusCode());
	      System.out.println(response.getBody());
	      System.out.println(response.getHeaders());
	      
	      return response.getStatusCode();
	    } catch (IOException ex) {
	      throw ex;
	    }
	}
}
