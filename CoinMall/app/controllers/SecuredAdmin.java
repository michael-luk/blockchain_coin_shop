package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.StrUtil;
import controllers.biz.ConfigBiz;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Date;

public class SecuredAdmin extends Security.Authenticator implements IConst {

    @Override
    public String getUsername(Context ctx) {
        if (ConfigBiz.getBoolConfig("is.prod") && ConfigBiz.getBoolConfig("admin.login")) {
            String userName = ctx.session().get("name");
            if (userName == null) {
                return null;
            } else {
                if ("2".equals(ctx.session().get(SESSION_USER_ROLE)) || "1".equals(ctx.session().get(SESSION_USER_ROLE))) {
                    // check timeout if set
                    int adminTimeoutMinutes = ConfigBiz.getIntConfig("admin.timeout.minute");
                    if (adminTimeoutMinutes > 0) {
                        if (StrUtil.isNull(ctx.session().get("admin_login_timeout"))) return null;
                        long msDiff = (new Date()).getTime() - DateUtil.Str2Date(ctx.session().get("admin_login_timeout")).getTime();
                        if ((msDiff / 1000 / 60) > adminTimeoutMinutes) {
                            ctx.flash().put("logininfo", "登录超时, 请重新登录!");
                            return null;
                        } else {
                            ctx.session().put("admin_login_timeout", DateUtil.NowString());
                        }
                    }

                    // 返回非null代表验证成功
                    return userName;
                } else
                    return null;
            }
        }
        return "developing";
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.backendLogin());
    }

    // public static boolean isOwnerOf(String userName) {
    // return
    // Context.current().session().get(SESSION_USER_NAME).equals(userName);
    // }
    //
    // public static boolean isFromMobile() {
    // return
    // SESSION_DEVICE_MOBILE.equals(Context.current().session().get(SESSION_DESC));
    // }
    //
    // public static UserModel getCurrentUser() {
    // String name = Context.current().session().get(SESSION_USER_NAME);
    // return UserModel.findByloginName(name);
    // }
}