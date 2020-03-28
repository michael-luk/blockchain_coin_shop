package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import controllers.biz.SaveBiz;
import controllers.biz.UserBiz;
import models.ShipInfo;
import models.User;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.MethodName;
import util.Role;
import views.html.gen.ship_info;
import views.html.gen.ship_info_backend;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static controllers.Application.sendWebSocketByChannelTag;
import static play.data.Form.form;

public class ShipInfoController extends Controller implements IConst {

    @Security.Authenticated(Secured.class)
    public static Result add() {
        Msg<ShipInfo> msg = new Msg<>();

        Form<ShipInfoParser> httpForm = form(ShipInfoParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            ShipInfoParser formObj = httpForm.get();
            ShipInfo newObj = new ShipInfo();

            newObj.isDefault = formObj.isDefault;
            newObj.name = formObj.name;
            newObj.phone = formObj.phone;
            newObj.province = formObj.province;
            newObj.city = formObj.city;
            newObj.zone = formObj.zone;
            newObj.location = formObj.location;
//            newObj.comment = formObj.comment;

            User parentUser = User.find.byId(formObj.refUserId);
            newObj.user = parentUser;
            newObj.refUserId = formObj.refUserId;
            Transaction txn = Ebean.beginTransaction();
            try {
                SaveBiz.beforeSave(newObj);
                Ebean.save(newObj);
                if (newObj.isDefault)
                    UserBiz.changeUserDefaultShipInfo(newObj.refUserId, newObj.id);

                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
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

    @Security.Authenticated(Secured.class)
    public static Result update(long id) {
        Msg<ShipInfo> msg = new Msg<>();

        ShipInfo found = ShipInfo.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<ShipInfoParser> httpForm = form(ShipInfoParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            ShipInfoParser formObj = httpForm.get();

            Transaction txn = Ebean.beginTransaction();
            try {
                found = ShipInfo.find.byId(id);

                found.isDefault = formObj.isDefault;
                found.name = formObj.name;
                found.phone = formObj.phone;
                found.province = formObj.province;
                found.city = formObj.city;
                found.zone = formObj.zone;
                found.location = formObj.location;
                found.comment = formObj.comment;

                SaveBiz.beforeUpdate(found);
                Ebean.update(found);
                if (found.isDefault)
                    UserBiz.changeUserDefaultShipInfo(found.refUserId, found.id);
                txn.commit();

                msg.flag = true;
                msg.data = found;
                play.Logger.info("result: " + UPDATE_SUCCESS);
            } catch (Exception ex) {
                msg.message = UPDATE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
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

    @Security.Authenticated(Secured.class)
    public static Result delete(long id) {
        Msg<ShipInfo> msg = new Msg<>();

        ShipInfo found = ShipInfo.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        try {
            Ebean.delete(found);
            msg.flag = true;
            play.Logger.info("result: " + DELETE_SUCCESS);
        } catch (Exception ex) {
            msg.message = DELETE_ISSUE + ", ex: " + ex.getMessage();
            play.Logger.error(msg.message);
        }
        return ok(Json.toJson(msg));
    }

    public static class ShipInfoParser {

        public long refUserId;
        public boolean isDefault;
        public String name;
        public String phone;
        public String province;
        public String city;
        public String zone;
        public String location;
        public String comment;

        public String validate() {

            if (User.find.byId(refUserId) == null) {
                return play.i18n.Messages.get("address.no.user");
            }
            return null;
        }
    }
}
