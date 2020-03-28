package controllers.biz;

import LyLib.Interfaces.IConst;
import LyLib.Utils.*;
import com.avaje.ebean.Ebean;
import controllers.Application;
import controllers.EmailController;
import controllers.Secured;
import models.ShipInfo;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UserBiz extends Controller implements IConst {

    public static User findByloginName(String loginName) {
        if (StrUtil.isNull(loginName)) {
            return null;
        }
        return User.find.where().eq("loginName", loginName).findUnique();
    }

    public static User findByEmail(String email) {
        if (StrUtil.isNull(email)) return null;

        User found = User.find.where().eq("loginName", email).findUnique();
        if (found != null) {
            return found;
        } else {
            Field[] fields = User.class.getDeclaredFields();
            if (fields == null) return null;
            if (fields.length == 0) return null;

            for (Field field : fields) {
                if (field != null && (java.lang.String.class == field.getType()) && field.getName().toLowerCase().contains("email")) {
                    found = User.find.where().eq(field.getName(), email).findUnique();
                    if (found != null) return found;
                }
            }
            return null;
        }
    }

    public static User authenticate(String loginName, String password) {
        return User.find.where().eq("loginName", loginName).eq("password", password).eq("status", 0).findUnique();
    }

    public static boolean userExist(String loginName) {
        return User.find.where().eq("loginName", loginName).findRowCount() > 0;
    }

    public static User getCurrentloginUser() {
        long currentUserId = 0;
        String currentSessionId = session().get(SESSION_USER_ID);

        try {
            currentUserId = Long.parseLong(currentSessionId);
        } catch (NumberFormatException ex) {
            play.Logger.error("获取当前登陆用户id时转换失败: " + currentSessionId);
        }

        return User.find.byId(currentUserId);
    }

    public static Result currentLoginUser() {
        Msg<User> msg = new Msg<>();

        User found = getCurrentloginUser();
        if (found != null) {
            msg.flag = true;
            msg.data = found;
        } else {
            msg.message = "未找到当前登陆用户";
        }
        return ok(Json.toJson(msg));
    }

    // 选择新默认地址
    public static void changeUserDefaultShipInfo(Long uid, Long newDefaultShipInfoId) {
        User found = User.find.byId(uid);
        if (found == null || found.shipInfos == null || found.shipInfos.size() == 0) {
            return;
        }
        for (ShipInfo shipInfo : found.shipInfos) {
            if (shipInfo.id != newDefaultShipInfoId && shipInfo.isDefault) {
                shipInfo.isDefault = false;
                shipInfo.save();
            }
        }
    }

    // 生成分销码
    public static String generateResellerCode() {
        // 时间+4位字母
        String code = DateUtil.Date2Str(new Date(), "yyyyMMddHHmmss") + getRamdonLetter()
                + getRamdonLetter() + getRamdonLetter() + getRamdonLetter();
        play.Logger.error("create reseller code: " + code);
        return code;
    }

    public static char getRamdonLetter() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return chars.charAt((int) (Math.random() * 52));
    }

    public static User findEarlyUserBy(String kw, String value) {
        List<User> tryFoundUser = User.find.where().eq(kw, value).orderBy("id").findList();
        if (tryFoundUser.size() > 0) return tryFoundUser.get(0);// 总是使用ID靠前的用户
        return null;
    }

    public static String getHideName(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            if (i == 0 || i == name.length() - 1)
                sb.append(name.charAt(i));
            else
                sb.append("*");
        }
        return sb.toString();
    }
}
