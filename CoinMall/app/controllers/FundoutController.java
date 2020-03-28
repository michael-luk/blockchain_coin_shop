package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import models.Fundout;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Date;

import static play.data.Form.form;

public class FundoutController extends Controller implements IConst {

    @Security.Authenticated(Secured.class)
    public static Result addNew(String key, long id) {
        Msg msg = new Msg<>();

        User found = User.find.byId(id);
        if (found == null) {
            msg.message = play.i18n.Messages.get("address.no.user");
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        if ("Money".equalsIgnoreCase(key)) {
            if (found.currentResellerProfitMoney < 0) {
                msg.message = play.i18n.Messages.get("fundout.zero");
                return ok(Json.toJson(msg));
            }
            if (StrUtil.isNull(found.resellerProfitMoneyBank) || StrUtil.isNull(found.resellerProfitMoneyBankNo)
                    || StrUtil.isNull(found.resellerProfitMoneyBankAccount)) {
                msg.message = play.i18n.Messages.get("fundout.receive.issue");
                return ok(Json.toJson(msg));
            }
        } else {
            if (found.currentResellerProfitCoin < 0) {
                msg.message = play.i18n.Messages.get("fundout.zero");
                return ok(Json.toJson(msg));
            }
            if (StrUtil.isNull(found.resellerProfitCoinAddress)) {
                msg.message = play.i18n.Messages.get("fundout.receive.issue");
                return ok(Json.toJson(msg));
            }
        }

        Fundout newObj = new Fundout();
        newObj.name = DateUtil.Date2Str((new Date()), "yyyyMMddHHmmss") + found.id;
        newObj.refUserId = found.id;
        newObj.user = found;
        if ("Money".equalsIgnoreCase(key)) {
            newObj.money = found.currentResellerProfitMoney;
            newObj.resellerProfitMoneyBank = found.resellerProfitMoneyBank;
            newObj.resellerProfitMoneyBankNo = found.resellerProfitMoneyBankNo;
            newObj.resellerProfitMoneyBankAccount = found.resellerProfitMoneyBankAccount;
            found.currentResellerProfitMoney = 0;
        } else {
            newObj.coin = found.currentResellerProfitCoin;
            newObj.resellerProfitCoinAddress = found.resellerProfitCoinAddress;
            found.currentResellerProfitCoin = 0;
        }

        newObj.save();
        found.save();
        msg.flag = true;
        play.Logger.info("fundout success. uid: " + found.id + ", type: " + key + ", num: " + (newObj.money + newObj.coin));
        msg.message = play.i18n.Messages.get("action.success");
        return ok(Json.toJson(msg));
    }
}
