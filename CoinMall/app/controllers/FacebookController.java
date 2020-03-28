package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import controllers.biz.UserBiz;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import java.util.List;

import static controllers.WeiXinController.generateResellerCodeBarcode;

public class FacebookController extends Controller implements IConst {

    public static Result fbLogin(String id, String name, String accessToken, String resellerCode) {
        Msg msg = new Msg();
        if (StrUtil.isNull(id) || StrUtil.isNull(name)) {
            return ok(Json.toJson(msg));
        }
        handleFbUser(id, name, accessToken, resellerCode);
        msg.flag = true;
        return ok(Json.toJson(msg));
    }

    private static void handleFbUser(String id, String name, String accessToken, String resellerCode) {
        List<User> tryFoundUser = User.find.where().eq("facebookId", id).orderBy("id").findList();
        User found = null;
        if (tryFoundUser.size() > 0) {
            found = tryFoundUser.get(0);// 总是使用ID靠前的用户
        }

        if (found == null) {
            // 无则注册
            session(SESSION_USER_NAME, "");
            session(SESSION_USER_ID, "");
            session("OPEN_ID", "");
            session("UNION_ID", "");
            session("facebookId", "");
            session("nickName", "");

            User newObj = new User();
            newObj.facebookId = id;

            newObj.name = util.EmojiFilter.filterEmoji(name);
            newObj.headImage = "//graph.facebook.com/" + id + "/picture";
            newObj.resellerCode = UserBiz.generateResellerCode();
            try {
                newObj.resellerCodeImage = generateResellerCodeBarcode(newObj.resellerCode);
            } catch (Exception e) {
                play.Logger.error("error on create reseller barcode: " + e.getMessage());
            } // 分销二维码自动生成
            play.Logger.info("fb user register: " + newObj.name);

            // handle上线用户
            if (!StrUtil.isNull(resellerCode)) {
                if (newObj.setReseller(resellerCode)) {
                    play.Logger.info("用户的上线: " + newObj.uplineUserName);
                } else {
                    play.Logger.info("handle上线用户失败, 对方不是分销商, 或可能是互为上下线, 或可能是自己加自己: " + resellerCode);
                    play.Logger.info("用户的上线: -1");
                }
            } else {
                // 若这个用户扫码进来则形成上下线关系
                // 若是关注公众号进来则成为上帝子民
                play.Logger.info("用户的上线: -1");
            }

            try {
                newObj.save();
            } catch (PersistenceException ex) {
                play.Logger.error("创建用户遇到昵称火星文错误: " + ex.getMessage());
                newObj.name = "[表情]";
                newObj.save();
            }

            session(SESSION_USER_ID, Long.toString(newObj.id));
            session(SESSION_USER_NAME, newObj.name);
            session("nickName", name);
            session("facebookId", id);
        } else {
            // handle上线用户
            if (found.uplineUserId == -1 && !StrUtil.isNull(resellerCode)) {
                if (found.setReseller(resellerCode)) {
                    play.Logger.info("用户的上线: " + found.uplineUserName);
                    found.save();
                    play.Logger.info("上线关联viaFacebook: " + found.name);
                } else {
                    play.Logger.info("handle上线用户失败, 对方不是分销商, 或可能是互为上下线, 或可能是自己加自己: " + resellerCode);
                    play.Logger.info("用户的上线: -1");
                }
            } else {
                // 若这个用户扫码进来则形成上下线关系
                // 若是关注公众号进来则成为上帝子民
                play.Logger.info("用户的上线: -1");
            }

            if (StrUtil.isNull(found.facebookId)) {
                found.facebookId = id;
                found.save();
                play.Logger.info("用户关联viaFacebook: " + found.name);
            }
            session(SESSION_USER_ID, Long.toString(found.id));
            session(SESSION_USER_NAME, found.name);
            session("nickName", name);
            session("facebookId", id);
        }
    }

    public static Result fbLogout() {
        session().clear();
        return ok();
    }
}
