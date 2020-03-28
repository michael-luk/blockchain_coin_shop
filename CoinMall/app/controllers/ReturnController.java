package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import controllers.biz.SaveBiz;
import models.Purchase;
import models.ReturnInfo;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.Date;

import static controllers.Application.sendWebSocketByChannelTag;
import static play.data.Form.form;

public class ReturnController extends Controller implements IConst {


    @Security.Authenticated(Secured.class)
    public static Result add() {
        Msg<ReturnInfo> msg = new Msg<>();

        Form<ReturnInfoParser> httpForm = form(ReturnInfoParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            ReturnInfoParser formObj = httpForm.get();
            ReturnInfo newObj = new ReturnInfo();

            newObj.contact = formObj.contact;
            newObj.phone = formObj.phone;
            newObj.comment = formObj.comment;
            newObj.returnType = formObj.returnType;

            long uid = Long.parseLong(session().get(SESSION_USER_ID));
            if (uid == 0)
                return ok(Json.toJson(msg));

            User parentUser = User.find.byId(uid);
            newObj.user = parentUser;
            newObj.refUserId = uid;
            Purchase parentPurchase = Purchase.find.byId(formObj.refPurchaseId);
            newObj.purchase = parentPurchase;
            newObj.refPurchaseId = formObj.refPurchaseId;
            newObj.name = DateUtil.Date2Str(new Date(), "yyyyMMddHHmmss") + parentPurchase.name.substring(0, 4);

            parentPurchase.status = 9;
            if (parentPurchase.returnInfos == null) parentPurchase.returnInfos = new ArrayList<>();
            parentPurchase.returnInfos.add(newObj);

            Transaction txn = Ebean.beginTransaction();
            try {
                SaveBiz.beforeSave(newObj);
                Ebean.save(newObj);
                parentPurchase.save();

                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
                sendWebSocketByChannelTag("return_info_backend", "new");
            } catch (PersistenceException ex) {
                msg.message = CREATE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
                return ok(Json.toJson(msg));
            } finally {
                txn.end();
            }
            return ok(Json.toJson(msg));
        } else {
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
            play.Logger.error("result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    public static class ReturnInfoParser {

        public String contact;
        public String phone;
        public String comment;
        public int returnType;
        public long refPurchaseId;

        public String validate() {

            if (StrUtil.isNull(contact)) {
                return play.i18n.Messages.get("order.return.contract.notfound");
            }

            if (StrUtil.isNull(phone)) {
                return play.i18n.Messages.get("order.return.phone.notfound");
            }
            return null;
        }
    }
}
