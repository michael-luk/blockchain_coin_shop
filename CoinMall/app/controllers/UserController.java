package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.PageInfo;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;
import com.avaje.ebean.Transaction;
import controllers.biz.SaveBiz;
import models.Theme;
import models.User;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.MethodName;
import util.Role;
import views.html.gen.user;
import views.html.gen.user_backend;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static controllers.Application.sendWebSocketByChannelTag;
import static play.data.Form.form;

public class UserController extends Controller implements IConst {

    @Security.Authenticated(Secured.class)
    public static Result updateUserFundoutAddress(long id, String addr) {
        Msg msg = new Msg<>();

        User found = User.find.byId(id);
        if (found == null) {
            msg.message = play.i18n.Messages.get("address.no.user");
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        found.resellerProfitCoinAddress = addr;
        found.save();
        msg.message = play.i18n.Messages.get("action.success");
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result updateUserFundoutBankInfo(long id) {
        Msg msg = new Msg<>();

        User found = User.find.byId(id);
        if (found == null) {
            msg.message = play.i18n.Messages.get("address.no.user");
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }
        Form<UserParser> httpForm = form(UserParser.class).bindFromRequest();
        UserParser formObj = httpForm.get();

        found.resellerProfitMoneyBank = formObj.resellerProfitMoneyBank;
        found.resellerProfitMoneyBankNo = formObj.resellerProfitMoneyBankNo;
        found.resellerProfitMoneyBankAccount = formObj.resellerProfitMoneyBankAccount;
        found.save();
        msg.message = play.i18n.Messages.get("action.success");
        return ok(Json.toJson(msg));
    }

    public static class UserParser {

        public String resellerProfitMoneyBankNo;
        public String resellerProfitMoneyBank;
        public String resellerProfitMoneyBankAccount;

        public String validate() {
            return null;
        }
    }
}
