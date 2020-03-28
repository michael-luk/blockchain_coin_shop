package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import models.Product;
import models.Theme;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static play.data.Form.form;

public class FavoriteController extends Controller implements IConst {

    @Security.Authenticated(Secured.class)
    public static Result checkFavorite(long tid) {
        Msg msg = new Msg<>();
        if (StrUtil.isNull(session().get(SESSION_USER_ID)))
            return ok(Json.toJson(msg));

        long uid = Long.parseLong(session().get(SESSION_USER_ID));
        if (uid == 0 || tid == 0)
            return ok(Json.toJson(msg));

        User found = User.find.byId(uid);
        Theme foundTheme = Theme.find.byId(tid);

        if (found == null || foundTheme == null)
            return ok(Json.toJson(msg));

        msg.flag = found.themes.contains(foundTheme);
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result addFavoriteProduct(long tid) {
        Msg msg = new Msg<>();
        if (StrUtil.isNull(session().get(SESSION_USER_ID)))
            return ok(Json.toJson(msg));
        long uid = Long.parseLong(session().get(SESSION_USER_ID));
        if (uid == 0 || tid == 0)
            return ok(Json.toJson(msg));

        User found = User.find.byId(uid);
        Theme foundTheme = Theme.find.byId(tid);

        if (found == null || foundTheme == null)
            return ok(Json.toJson(msg));

        if (found.themes.contains(foundTheme)) {
            msg.message = play.i18n.Messages.get("add.favorite.fail.due.to.already");
            play.Logger.info("已收藏过该商品");
        } else {
            found.themes.add(foundTheme);
            Ebean.update(found);
            foundTheme.users.add(found);
            Ebean.update(foundTheme);

            msg.flag = true;
            msg.message = play.i18n.Messages.get("add.favorite.success");
            play.Logger.info("添加收藏成功");
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result cancelFavoriteProduct(long tid) {
        Msg msg = new Msg<>();
        if (StrUtil.isNull(session().get(SESSION_USER_ID)))
            return ok(Json.toJson(msg));
        long uid = Long.parseLong(session().get(SESSION_USER_ID));
        if (uid == 0 || tid == 0)
            return ok(Json.toJson(msg));

        User found = User.find.byId(uid);
        Theme foundTheme = Theme.find.byId(tid);

        if (found == null || foundTheme == null)
            return ok(Json.toJson(msg));

        if (!found.themes.contains(foundTheme)) {
            msg.message = play.i18n.Messages.get("cancel.favorite.fail.due.to.not");
            play.Logger.info("没收藏过该商品");
        } else {
            found.themes.remove(foundTheme);
            foundTheme.users.remove(found);
            Ebean.update(found);
            Ebean.update(foundTheme);

            msg.flag = true;
            msg.message = play.i18n.Messages.get("cancel.favorite");
            play.Logger.info("取消收藏成功");
        }
        return ok(Json.toJson(msg));
    }
}
