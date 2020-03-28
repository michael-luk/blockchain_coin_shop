package controllers.biz;

import LyLib.Utils.StrUtil;
import models.Admin;

public class AdminBiz {

    public static Admin findByloginName(String loginName) {
        if (StrUtil.isNull(loginName)) {
            return null;
        }
        return Admin.find.where().eq("name", loginName).findUnique();
    }

    public static Admin authenticate(String loginName, String password) {
        return Admin.find.where().eq("name", loginName).eq("password", password).eq("status", 0).findUnique();
    }

    public static boolean userExist(String loginName) {
        return Admin.find.where().eq("name", loginName).findRowCount() > 0;
    }
}
