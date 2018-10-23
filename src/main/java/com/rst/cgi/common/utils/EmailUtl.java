package com.rst.cgi.common.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.controller.interceptor.CustomException;
import net.sf.json.JSONObject;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/19 下午3:17
*/
@Service
public class EmailUtl {
    private static final String API_KEY = "";
    public static void send (String fromName,
                             String toEmail,
                             String subject,
                             String content,
                             Boolean useSmtp,
                             JavaMailSender mailSender){
        if (useSmtp == null || useSmtp) {
            smtpSend(fromName, toEmail, subject, content, mailSender);
        } else {
            httpMailSend(fromName, toEmail, subject, content);
        }
    }


    private static void httpMailSend(String fromName,
                                     String toEmail,
                                     String subject,
                                     String content){

        try {

            JSONObject param = new JSONObject();
            param.put("from", fromName);
            param.put("to", toEmail);
            param.put("subject", subject);
            param.put("html", content);
            HttpResponse<JsonNode> response = Unirest.post(Urls.MAILGUN_SEND)
                    .header("accept","application/json")
                    .basicAuth("api", API_KEY)
                    .fields(param)
                    .asJson();
            if (response.getStatus() != 200) {
                CustomException.response(-1, response.getBody().getObject().getString("message"));
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }


    }

    private static void smtpSend(String fromName,
                                 String toEmail,
                                 String subject,
                                 String content,
                                 JavaMailSender mailSender){
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            CustomException.response(Error.MAIL_SEND_ERROR);
        }
    }
}
