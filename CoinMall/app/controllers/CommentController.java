package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import controllers.biz.OrderBiz;
import controllers.biz.SaveBiz;
import controllers.biz.UserBiz;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.persistence.PersistenceException;

import static controllers.Application.sendWebSocketByChannelTag;
import static play.data.Form.form;

public class CommentController extends Controller implements IConst {


    @Security.Authenticated(Secured.class)
    public static Result add() {
        Msg<CommentInfo> msg = new Msg<>();

        Form<CommentInfoParser> httpForm = form(CommentInfoParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            CommentInfoParser formObj = httpForm.get();
            CommentInfo newObj = new CommentInfo();
            newObj.name = formObj.name;

            User parentUser = User.find.byId(Long.parseLong(session().get(SESSION_USER_ID)));
            if (parentUser == null) {
                msg.message = play.i18n.Messages.get("address.no.user");
                return ok(Json.toJson(msg));
            }
            newObj.user = parentUser;
            newObj.refUserId = parentUser.id;

            Theme theme = Theme.find.byId(formObj.themeId);
            if (theme.product == null) {
                msg.message = play.i18n.Messages.get("product.comment.product.notfound");
                return ok(Json.toJson(msg));
            }
            newObj.product = theme.product;
            newObj.refProductId = theme.refProductId;

            Purchase order = Purchase.find.byId(formObj.refPurchaseId);
            if (OrderBiz.checkOrderComment(order).get(theme.id) == -1){
                msg.message = play.i18n.Messages.get("product.comment.timeout");
                return ok(Json.toJson(msg));
            }
            if (OrderBiz.checkOrderComment(order).get(theme.id) == 2){
                msg.message = play.i18n.Messages.get("product.comment.limit");
                return ok(Json.toJson(msg));
            }

            newObj.refPurchaseId = formObj.refPurchaseId;
            newObj.purchase = order;
            newObj.comment = UserBiz.getHideName(parentUser.name);

            Transaction txn = Ebean.beginTransaction();
            try {
                SaveBiz.beforeSave(newObj);
                Ebean.save(newObj);


                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
                sendWebSocketByChannelTag("comment_info_backend", "new");
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

    public static class CommentInfoParser {

        public String name;
        public long themeId;
        public long refPurchaseId;

        public String validate() {
            if (StrUtil.isNull(name))
                return play.i18n.Messages.get("product.comment.null");

            if (Theme.find.byId(themeId) == null) {
                return play.i18n.Messages.get("product.comment.product.notfound");
            }
            if (Purchase.find.byId(refPurchaseId) == null) {
                return play.i18n.Messages.get("product.comment.order.notfound");
            }
            return null;
        }
    }
}
