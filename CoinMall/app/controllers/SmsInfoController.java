package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import controllers.biz.ConfigBiz;
import controllers.biz.SmsBiz;
import models.SmsInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.todaynic.client.mobile.SMS;

import static play.data.Form.form;

public class SmsInfoController extends Controller implements IConst {

    public static Result getVetfy() {
        Msg msg = new Msg<>();
        if (!Application.smsCaptchaFunction) {
            msg.message = "短信验证码获取失败，功能未集成";
            play.Logger.error(msg.message);
            return ok(Json.toJson(msg));
        }

        Form<SmsInfo> httpForm = form(SmsInfo.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            SmsInfo checkCodeF = httpForm.get();
            Hashtable configTable = new Hashtable();
            //测试环境
//			configTable.put("VCPSERVER","testxml.todaynic.com");
            //真实环境
            configTable.put("VCPSERVER", ConfigBiz.getStrConfig("sms.server"));
            configTable.put("VCPSVPORT", ConfigBiz.getStrConfig("sms.port"));
            configTable.put("VCPUSERID", ConfigBiz.getStrConfig("sms.uid"));
            configTable.put("VCPPASSWD", ConfigBiz.getStrConfig("sms.psw"));

            String PhoneNumber = checkCodeF.phone;
            String randVery = getRandVery();
            SmsInfo checkCode = new SmsInfo();
            checkCode.phone = checkCodeF.phone;
            checkCode.checkCode = randVery;

            //写入发送的验证码到数据库中
            String MsgAbout = "租金所短信验证码： " + randVery + "，请不要透漏给别人";
            String SendTime = "0";     //即时发送	//reqguest.getParameteer("dtTime");
            String MsgContent = MsgAbout;
            String type = "0";    //通道选择: 0：默认通道； 2：通道2； 3：即时通道
            SMS smssender = new SMS(configTable);
            try {
                smssender.sendSMS(PhoneNumber, MsgContent, SendTime, type);
            } catch (Exception e) {
                msg.message = "获取验证码失败";
                play.Logger.error(msg.message + e.getMessage());
                return ok(Json.toJson(msg));
            }

            String sendXml = smssender.getSendXml();
            Hashtable recTable = smssender.getRespData();
            String receiveXml = smssender.getRecieveXml();
            String code = smssender.getCode();
            String recmsg = smssender.getMsg();

            checkCode.sendXml = sendXml;
            checkCode.returnTable = recTable.toString();
            checkCode.receiveXml = receiveXml;
            checkCode.code = code;
            if ("2000".equals(code)) {
                msg.flag = true;
                msg.message = "发送成功";
                play.Logger.info(msg.message + ", code: " + code);
            } else {
                msg.message = "发送失败";
                play.Logger.error(msg.message + ", code: " + code);
            }
            checkCode.returnMsg = recmsg;
            checkCode.save();
        } else {
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
        }
        return ok(Json.toJson(msg));
    }

    public static Result verifyCode() {
        Msg msg = new Msg<>();
        Form<SmsInfo> httpForm = form(SmsInfo.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            SmsInfo checkCode = httpForm.get();

            SmsInfo dbCode = SmsBiz.getLatestCode(checkCode.phone, checkCode.checkCode);
            if (dbCode != null) {
                msg.flag = true;
            } else {
                msg.flag = false;
                msg.message = "验证码错误";
            }
        } else {
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
        }
        return ok(Json.toJson(msg));
    }

    public static String getRandVery() {
        String res = "";
        res = res + Integer.toString(new Random().nextInt(10));
        res = res + Integer.toString(new Random().nextInt(10));
        res = res + Integer.toString(new Random().nextInt(10));
        res = res + Integer.toString(new Random().nextInt(10));
        return res;
    }

    public static Result phoneBind() {
        Msg msg = new Msg<>();
        Form<PhoneBindParser> httpForm = form(PhoneBindParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            PhoneBindParser phoneBindParser = httpForm.get();

            SmsInfo dbCode = SmsBiz.getLatestCode(phoneBindParser.phone, phoneBindParser.sms);
            msg.message = "短信验证码错误";
            if (dbCode != null) {
                if (phoneBindParser.sms.equals(dbCode.checkCode)) {
                    // 验证通过
                    msg.flag = true;
                    msg.message = "短信验证码正确";
                    play.Logger.info(msg.message);
                }
            }
        } else {
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
        }
        return ok(Json.toJson(msg));
    }

    public static class PhoneBindParser {
        public String phone;
        public String sms;

        public String validate() {
            if (StrUtil.isNull(phone)) {
                return "手机号不能为空";
            }
            if (StrUtil.isNull(sms)) {
                return "短信验证码不能为空";
            }
            return null;
        }
    }
}
