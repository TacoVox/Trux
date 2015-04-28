/*
 * Copyright 2015 jonas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.gu.tux.truxserver.services;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author jonas
 */
public class EMailSender {
    /**
     * Static part.
     */
    private static EMailSender es;
    
    public static EMailSender getInstance()
    {
        if(es == null)
            es = new EMailSender();
        
        return es;
    }
    
    public static EMailSender gI()
    {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private EMailSender() {}
    
    public void sendConfirmationMail(String receiver, String accesscode)
    {
        Properties mailServerProperties;
	Session getMailSession;
	MimeMessage generateMailMessage;
        
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
 
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        
        try {
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));

        generateMailMessage.setSubject("Confirm that you registered."
                + "");
        String emailBody = "This feature is soon available.<br> Try this link: "
                + "<a href=\"www.derkahler.de/trux/index.php?id=" + accesscode +
                "\">Confirm eMail</a><br><br> Regards, <br>Jonas";
        
        generateMailMessage.setContent(emailBody, "text/html");

        Transport transport = getMailSession.getTransport("smtp");
        
        transport.connect("smtp.gmail.com", "tacovox@gmail.com", "picknicker");
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String args[])
    {
        EMailSender.gI().sendConfirmationMail("tacovox@icloud.com", "asd");
        System.out.println("eMail sent.");
    } 
}