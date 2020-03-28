package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import controllers.biz.ConfigBiz;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;

public class EmailController extends Controller implements IConst {
    public static String emailUser;

    public static Result sendMail(String receiver, String title, String content, String fileName, String filePath) {
//        if (!Application.taskFlag) return ok(EMAIL_ISSUE + ": no run flag");

        if (StrUtil.isNull(receiver) || StrUtil.isNull(title)) {
            return ok(EMAIL_ISSUE + ": " + PARAM_ISSUE);
        }

        if (StrUtil.isNull(emailUser)) {
            return ok(EMAIL_ISSUE + ": sender not set");
        }

        try {
            String result = doSendMail(emailUser, receiver, title, content, fileName, filePath, true);
            play.Logger.info(EMAIL_SUCCESS + ": " + result);
            return ok(EMAIL_SUCCESS + ": " + result);
        } catch (Exception ex) {
            play.Logger.error(EMAIL_ISSUE + ": " + receiver + " - " + ex.getMessage());
            return ok(EMAIL_ISSUE + ": " + receiver + " - " + ex.getMessage());
        }
    }

    public static String doSendMail(String sender, String receiver, String title, String content, String fileName, String filePath, boolean isHtmlBody) {
        Email email = new Email();
        email.setSubject(title);
        email.setFrom(sender);
        email.addTo(receiver);
        if (StrUtil.isNotNull(fileName) && StrUtil.isNotNull(filePath)) {
            email.addAttachment(fileName, new File(filePath));
        }
        // adds inline attachment from byte array
//                .addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE)
        // sends text, HTML or both...
        if (isHtmlBody)
            email.setBodyHtml(content);
        else
            email.setBodyText(content);
//                .setBodyHtml("<html><body><p>An <b>html</b> message</p></body></html>");
        return MailerPlugin.send(email);
    }
}
