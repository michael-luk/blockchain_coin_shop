package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.riversoft.weixin.common.oauth2.AccessToken;
import com.riversoft.weixin.common.oauth2.OpenUser;
import com.riversoft.weixin.open.base.AppSetting;
import com.riversoft.weixin.open.oauth2.OpenOAuth2s;
import controllers.biz.ConfigBiz;
import controllers.biz.UserBiz;
import models.Purchase;
import models.User;
import org.w3c.dom.Document;
import play.api.libs.json.Json;
import play.libs.F;
import play.libs.XPath;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import java.util.List;

import static controllers.WeiXinController.generateResellerCodeBarcode;

public class ApiController extends Controller implements IConst {

    public static F.Promise<Result> getDoudizhuBalanceApi(String unionId) {
        //http://rdpintai.cqyunshi.com/wechat/getgoldnum?unionid=ondqu1TGWHAWtDHRJMOJ2v8oFjnE
        F.Promise<WSResponse> response = WS.url(ConfigBiz.getStrConfig("game.coin.api.host")
                + "/wechat/getgoldnum?unionid=" + unionId).get();
        return response.map(resp -> {
            play.Logger.info("response getgoldnum: " + resp.getBody());
            JsonNode json = resp.asJson();
            if (json.get("status").asInt() == 1)
                return ok(json.get("data").get("goldnum"));
            else
                return ok(json.get("msg"));
        });
    }

    public static F.Promise<Result> getDoudizhuBalanceApiJson(String unionId) {
        //http://rdpintai.cqyunshi.com/wechat/getgoldnum?unionid=ondqu1TGWHAWtDHRJMOJ2v8oFjnE
        F.Promise<WSResponse> response = WS.url(ConfigBiz.getStrConfig("game.coin.api.host")
                + "/wechat/getgoldnum?unionid=" + unionId).get();
        return response.map(resp -> {
            play.Logger.info("response getgoldnum: " + resp.getBody());
            JsonNode json = resp.asJson();
            Msg<Double> msg = new Msg<>();
            if (json.get("status").asInt() == 1) {
                msg.flag = true;
                msg.data = json.get("data").get("goldnum").asDouble();
            } else {
                msg.message = json.get("msg").asText();
            }
            return ok(play.libs.Json.toJson(msg));
        });
    }

    public static F.Promise<Result> doPayBalance(String unionId, double amount, String typeStr, long uid) {
        //http://rdpintai.cqyunshi.com/wechat/sendgoldnum?unionid=ondqu1TGWHAWtDHRJMOJ2v8oFjnE&amount=10&type=&extend=123
        // type=refund 则退款
        // 正式服退币
        // http://api.poolgpu.com/wechat/sendgoldnum?unionid=odC1u0aiGJspuSv2ORZcgTHW32YQ&amount=10&type=refund&extend=123
        String url = ConfigBiz.getStrConfig("game.coin.api.host")
                + "/wechat/sendgoldnum?unionid=" + unionId + "&amount=" + amount + "&type=" + typeStr + "&extend=" + uid;
        play.Logger.info("response sendgoldnum url: " + url);
        F.Promise<WSResponse> response = WS.url(url).get();
        return response.map(resp -> {
            play.Logger.info("response sendgoldnum result: " + resp.getBody());
            JsonNode json = resp.asJson();
            Msg msg = new Msg<>();
            int status = json.get("status").asInt();
            msg.flag = status == 1;
            msg.code = status;
            msg.message = json.get("msg").asText();
            return ok(play.libs.Json.toJson(msg));
        });
    }

    public static Result getDoudizhuBalance(String unionId) {
        if (StrUtil.isNull(unionId)) return ok("UID为空");
        return getDoudizhuBalanceApi(unionId).get(5 * 1000);
    }

    public static Result getDoudizhuBalanceByUid(long uid) {
        if (uid <= 0) return ok("用户id不正确");
        User user = User.find.byId(uid);
        if (user == null) return ok("用户不存在");
        return getDoudizhuBalanceApiJson(user.unionId).get(5 * 1000);
    }

    public static Result payBalance(String unionId, double amount, String typeStr, long uid) {
        return doPayBalance(unionId, amount, typeStr, uid).get(5 * 1000);
    }
}
