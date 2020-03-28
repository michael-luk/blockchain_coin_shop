package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import com.riversoft.weixin.common.oauth2.AccessToken;
import com.riversoft.weixin.common.oauth2.OpenUser;
import com.riversoft.weixin.open.base.AppSetting;
import com.riversoft.weixin.open.oauth2.OpenOAuth2s;
import controllers.biz.ConfigBiz;
import controllers.biz.UserBiz;
import me.chanjar.weixin.common.exception.WxErrorException;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.UUID;

import static controllers.Application.noticePage;
import static controllers.WeiXinController.generateResellerCodeBarcode;

public class WxOpenController extends Controller implements IConst {
    public static OpenOAuth2s openOAuth2s;

    public static void init() {
        //        OpenOAuth2s openOAuth2s = OpenOAuth2s.defaultOpenOAuth2s();
//        //或者：
        AppSetting appSetting = new AppSetting(ConfigBiz.getStrConfig("wxopen.appid"),
                ConfigBiz.getStrConfig("wxopen.secret"));
        openOAuth2s = OpenOAuth2s.with(appSetting);
    }

    public static Result login() {
        //https://open.weixin.qq.com/connect/qrconnect?appid=wx6b1469bba63cddd9&redirect_uri=http%3A%2F%2Fwww.xwcmall.com%2Fwxopen&response_type=code&scope=snsapi_login#wechat_redirect
//        String uuid = UUID.randomUUID().toString();
//        session("WX_OPEN_STATE", uuid);
//
//        //生成授权链接,默认scope: snsapi_base
//        String oauthUrl = openOAuth2s.authenticationUrl("http://www.xwcmall.com/wxopen");
//
// 生成授权链接,指定scope: snsapi_base or snsapi_userinfo
//        String oauthUrl = openOAuth2s.authenticationUrl("http://www.xwcmall.com/wxopen/notify", "snsapi_login");

        // 生成授权链接,指定scope，指定state
        String oauthUrl = openOAuth2s.authenticationUrl(ConfigBiz.getStrConfig("protocol") + "://"
                        + ConfigBiz.getStrConfig("domain.name") + "/wxopen/notify",
                "snsapi_login", session().get("resellerCode"));
        return redirect(oauthUrl);
    }

    public static Result loginNotify(String code, String state) {
        //http://xwcmall.com/wxopen/notify?code=0719lRye1HHwot043Sxe1SvZye19lRyP&state=b8ff6201-f3b1-4f31-a49d-3960cce9765d

//        if (!state.equals(session().get("WX_OPEN_STATE"))) {
//            play.Logger.error("loginNotify state error: state=" + state + ", session: " + session().get("WX_OPEN_STATE"));
//            return noticePage(play.i18n.Messages.get("barcode.create.issue"), "",
//                    play.i18n.Messages.get("back.to.home"), "/");
//        }

//        //获取AccessToken
        AccessToken accessToken = openOAuth2s.getAccessToken(code);
        play.Logger.info("loginNotify accessToken: " + accessToken.getOpenId() + ", " + accessToken.getUnionId());

//        //获取用户信息
        OpenUser user = openOAuth2s.userInfo(accessToken.getAccessToken(), accessToken.getOpenId());
        play.Logger.info("loginNotify OpenUser: " + user.getNickName());
        session("nickName", user.getNickName());
        String resellerCode = state;
        handleOpenUser(user, resellerCode);
        return redirect("/");
    }

    private static void handleOpenUser(OpenUser user, String resellerCode) {
        List<User> tryFoundUser = User.find.where().eq("unionId", user.getUnionId()).orderBy("id").findList();
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

            User newObj = new User();
            newObj.wxOpenId = user.getOpenId();
            newObj.unionId = user.getUnionId();

            newObj.name = util.EmojiFilter.filterEmoji(user.getNickName());
            newObj.country = user.getCountry();
            newObj.province = user.getProvince();
            newObj.city = user.getCity();
            newObj.headImage = user.getHeadImgUrl();
            newObj.sexEnum = user.getSex().getCode();
            newObj.resellerCode = UserBiz.generateResellerCode();
            try {
                newObj.resellerCodeImage = generateResellerCodeBarcode(newObj.resellerCode);
            } catch (Exception e) {
                play.Logger.error("error on create reseller barcode: " + e.getMessage());
            } // 分销二维码自动生成
            play.Logger.info("open user register: " + newObj.name);

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
            session("OPEN_ID", newObj.wxOpenId);
            session("UNION_ID", newObj.unionId);
        } else {

            // handle上线用户
            if (found.uplineUserId == -1 && !StrUtil.isNull(resellerCode)) {
                if (found.setReseller(resellerCode)) {
                    play.Logger.info("用户的上线: " + found.uplineUserName);
                    found.save();
                    play.Logger.info("上线关联viaOpen: " + found.name);
                } else {
                    play.Logger.info("handle上线用户失败, 对方不是分销商, 或可能是互为上下线, 或可能是自己加自己: " + resellerCode);
                    play.Logger.info("用户的上线: -1");
                }
            } else {
                // 若这个用户扫码进来则形成上下线关系
                // 若是关注公众号进来则成为上帝子民
                play.Logger.info("用户的上线: -1");
            }

            if (StrUtil.isNull(found.wxOpenId)) {
                found.wxOpenId = user.getOpenId();
                found.save();
                play.Logger.info("用户关联viaOpen: " + found.name);
            }
            session(SESSION_USER_ID, Long.toString(found.id));
            session(SESSION_USER_NAME, found.name);
            session("OPEN_ID", found.wxOpenId);
            session("UNION_ID", found.unionId);
            session("facebookId", "");
        }
    }
}
