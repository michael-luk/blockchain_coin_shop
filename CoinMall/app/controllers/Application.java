package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import controllers.biz.AdminBiz;
import controllers.biz.ConfigBiz;
import controllers.biz.OrderBiz;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.exception.WxErrorException;
import models.*;
import play.data.Form;
import play.i18n.Lang;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.*;
import util.*;
import views.html.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static controllers.biz.UserBiz.findEarlyUserBy;
import static play.data.Form.form;

public class Application extends Controller implements IConst {
    public static boolean taskFlag = true;
    public static String dbType = "";
    public static String dbUser = "";
    public static String dbPsw = "";
    public static String dbName = "";
    public static String mysqlBinDir = "";

    public static boolean weixinFunction = false;
    public static boolean smsCaptchaFunction = false;

    // 即时通讯频道(可组装成hashmap来区分不同的channel)
    public static HashMap<String, WebSocket.Out> nameChannels = new HashMap<>();

    // connect & echo
    public static WebSocket<String> webSocket() {
        return WebSocket.whenReady((in, out) -> {
            // 收到消息事件
            in.onMessage((msg) -> {
                if (msg.startsWith("init,") && msg.split(",").length >= 2) {
                    nameChannels.put(msg.split(",")[1], out);
                    play.Logger.info("channel init: " + msg);
                } else if (msg.startsWith("close,") && msg.split(",").length >= 2) {
                    closeChannel(msg.split(",")[1]);
                } else
                    play.Logger.info("chat: " + msg);
            });

            // 断开事件
            in.onClose(() -> {
                play.Logger.info("WebSocket某个连接断开.");
            });

            // 连接成功事件(可不设置)
            play.Logger.info("WebSocket收到成功连接.");
//            out.write("您已连接!");
        });
    }

    // server push
    public static Result chat(String msg) {
        if (msg.startsWith("close,") && msg.split(",").length >= 2) {
            closeChannel(msg.split(",")[1]);
        } else {
            for (WebSocket.Out channel : nameChannels.values()) {
                channel.write(msg);
            }
        }
        return ok("Msg sent: " + msg);
    }

    public static void closeChannel(String channelKey) {
        if (StrUtil.isNull(channelKey)) return;
        if (!nameChannels.containsKey(channelKey)) return;

        WebSocket.Out channel = nameChannels.get(channelKey);
        if (channel != null) {
            nameChannels.get(channelKey).close();
            nameChannels.remove(channelKey);
            play.Logger.info("channel close: " + channelKey);
        }
    }

    public static Result getChannels() {
        Integer count = 0;
        String result = "";
        for (String key : nameChannels.keySet()) {
            count++;
            result += key + "\n";
        }
        result += "频道总数: " + count;
        play.Logger.info(result);
        return ok(result);
    }

    public static void sendWebSocketByChannelTag(String keyPrefix, String msg) {
        if (ConfigBiz.getBoolConfig("websocket"))
            for (String key : nameChannels.keySet()) if (key.startsWith(keyPrefix)) nameChannels.get(key).write(msg);
    }

    public static Result checkAlive() {
        return ok("alive");
    }

    public static Result cfgSelfCheck() {
        if (ConfigBiz.selfCheck4All()) {
            return ok("all cfg pass self check.");
        } else {
            return ok("all cfg NOT pass self check.");
        }
    }

    public static Result clearSession() {
        session().clear();
        return ok("session cleared");
    }

    public static Result login() {
//        session().clear();
//        session("login_user_name", admin.name);
//        session(SESSION_USER_ID, admin.id.toString());
        return Results.TODO;
    }

    public static Result logout() {
        session().clear();
        return redirect(routes.Application.index("", "", ""));
    }


    public static Result phoneBindPage() {
        return ok(phone_bind.render());
    }

    public static Result backendPage() {
        return redirect("/admin/admin");
    }

    public static Result backendLogin() {
        Admin admin = AdminBiz.findByloginName(session(SESSION_USER_NAME));
        if (admin != null && admin.userRoleEnum > 0) {
            return redirect("/admin/admin");
        } else
            return ok(backend_login.render(form(AdminLoginParser.class)));
    }

    public static F.Promise<Result> backendAuthenticate() {
        Form<AdminLoginParser> loginForm = form(AdminLoginParser.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            play.Logger.error("admin login form error: " + loginForm.errors().toString());
            flash("logininfo", loginForm.globalError().message());
            return F.Promise.promise(() -> redirect(routes.Application.backendLogin()));
        } else {
            session().clear();
            Admin admin = AdminBiz.findByloginName(loginForm.get().username);

            if (admin != null) {
                Integer role = admin.userRoleEnum;
                if (role > 0) {
                    // 1管理员, 2超级管理员
                    // 更新最后一次登录的IP
                    // 异步获取登录ip地
                    admin.lastLoginIp = request().remoteAddress();
                    play.Logger.info("admin login on ip: " + admin.lastLoginIp);
                    admin.lastLoginTime = new Date();
                    session("admin_login_timeout", LyLib.Utils.DateUtil.Date2Str(admin.lastLoginTime));
                    Ebean.update(admin);

                    session("name", admin.name);
                    session(SESSION_USER_ID, Long.toString(admin.id));
                    session(SESSION_USER_ROLE, role.toString());
                    play.Logger.info("admin login success: " + loginForm.get().username);

                    F.Promise<WSResponse> response
                            = WS.url("http://ip.taobao.com/service/getIpInfo.php?ip=" + admin.lastLoginIp).get();
                    return response.map(resp -> {
                        String respStr = resp.getBody();
                        play.Logger.info("response raw get admin login ip: " + respStr);

                        JsonNode respJson = resp.asJson();
                        if (respJson.get("code") != null && "0".equals(respJson.get("code").toString())) {
                            admin.lastLoginIpArea = respJson.get("data").get("country").asText()
                                    + respJson.get("data").get("region").asText()
                                    + respJson.get("data").get("city").asText()
                                    + respJson.get("data").get("isp").asText();
                        } else {
                            if ("0:0:0:0:0:0:0:1".equals(admin.lastLoginIp)) {
                                admin.lastLoginIpArea = "本机登录";
                            } else {
                                admin.lastLoginIpArea = "未知位置";
                            }
                        }
                        Ebean.update(admin);

                        // 登陆成功后的跳转(可以改成跳到其他界面)
                        return redirect("/admin/purchase");
                    }).recover(throwable -> redirect("/admin/admin"));
                } else {
                    play.Logger.error("admin login failed on role: " + loginForm.get().username);
                    return F.Promise.promise(() -> forbidden("您没有权限登录后台"));
                }
            } else {
                play.Logger.error("admin login not found: " + loginForm.get().username);
                return F.Promise.promise(() -> redirect(routes.Application.backendLogin()));
            }
        }
    }

    public static class AdminLoginParser {

        public String username;
        public String password;
        public String captchaField;

        public String validate() {
            if (StrUtil.isNull(captchaField)) return "请输入验证码";
            if (!captchaField.equals(session().get("admin_login"))) return "验证码错误, 请重试";
            session().remove("admin_login");

            if (!AdminBiz.userExist(username)) return "不存在此用户";
            if (password != null && password.length() < 32) password = LyLib.Utils.MD5.getMD5(password);
            if (AdminBiz.authenticate(username, password) == null) return "用户名或密码错误";
            return null;
        }
    }

    @MethodName("管理员注销")
    public static Result backendLogout() {
        session().clear();
        flash("logininfo", "您已登出, 请重新登录");
        return redirect(routes.Application.backendLogin());
    }

    public static Result captcha(String tag) {
        DefaultKaptcha captcha = new DefaultKaptcha();
        Properties props = new Properties();
        Config config = new Config(props);
        captcha.setConfig(config);

        String text = captcha.createText();
        BufferedImage img = captcha.createImage(text);

        session(tag, text);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpg", stream);
            stream.flush();
        } catch (IOException e) {
            play.Logger.error(e.getMessage());
        }
        return ok(stream.toByteArray()).as("image/jpg");
    }

    public static void changeLanguageDo(String lang) {
//        Controller.clearLang();
//        String title = Messages.get(new Lang(Lang.forCode("fr")), "home.title")
//        flash("lan",language);

        Controller.changeLang(lang);
        Lang currentLang = Controller.lang();

//        play.Logger.info("Accept-lang: " + request().acceptLanguages().toString());
//        play.Logger.info("网站语言改为：" + currentLang.code()); // eg. zh-CN, en, zh-HK, ja

        if ("zh-CN".equalsIgnoreCase(currentLang.code())) session("lang", "");
        if ("en".equalsIgnoreCase(currentLang.code())) session("lang", "En");
        if ("zh-HK".equalsIgnoreCase(currentLang.code())) session("lang", "Hk");
        if ("ja".equalsIgnoreCase(currentLang.code())) session("lang", "Ja");
    }

    public static Result changeLanguage(String lang) {
        changeLanguageDo(lang);
        return redirect("/");
    }

    public static Result wxAutoLogin(String resellerCode) {
//        String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + ConfigBiz.getStrConfig("weixin.appid")
//                + "&redirect_uri=" + ConfigBiz.getStrConfig("protocol") + "%3A%2F%2F" + ConfigBiz.getStrConfig("domain.name")
//                + "%2Fdowxuser%3FresellerCode=" + resellerCode
//                + "&response_type=code&scope=snsapi_base&state=" + request().uri() + "#wechat_redirect"; //
        String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + ConfigBiz.getStrConfig("weixin.appid")
                + "&redirect_uri=" + ConfigBiz.getStrConfig("protocol") + "://" + ConfigBiz.getStrConfig("domain.name")
                + "/dowxuser?resellerCode=" + resellerCode
                + "&response_type=code&scope=snsapi_base&state=" + request().uri() + "#wechat_redirect";
//        oauthUrl = URLEncoder.encode(oauthUrl);
        play.Logger.info("oauthUrl: " + oauthUrl);
        return redirect(oauthUrl);
    }

    public static Result test() {
        return ok(test.render());
    }

    public static Result index(String platform, String resellerCode, String lang) {
        play.Logger.info("loading " + request().uri() + " page with session: " + session("WX_OPEN_ID"));
        play.Logger.info("resellerCode: " + resellerCode);
        play.Logger.info("lang: " + lang);
        play.Logger.info("platform: " + platform);
        play.Logger.info("session platform: " + session().get("platform"));
        play.Logger.info("session UNION_ID: " + session("UNION_ID"));
        play.Logger.info("session facebookId: " + session("facebookId"));
        play.Logger.info("session WX_OPEN_ID: " + session("WX_OPEN_ID"));
        play.Logger.info("session userid: " + session("userid"));
        play.Logger.info("session nickName: " + session("nickName"));

        if (StrUtil.isNotNull(lang)) changeLanguageDo(lang);

        if (StrUtil.isNotNull(resellerCode))
            session("resellerCode", resellerCode);

        if (StrUtil.isNull(platform))// && StrUtil.isNull(session().get("platform")))
            return ok(index.render());
        else {
            if (StrUtil.isNull(session().get("platform")))
                session("platform", platform);
            else if (!session().get("platform").equalsIgnoreCase(platform))
                session("platform", platform);
        }

        List<Product> hotProducts = Product.find.where().ne("name" + session().get("lang"), "").eq("status", 0).findList().stream()
                .filter(o -> o.themes.stream().anyMatch(t -> t.soldNumber > 0))
                .sorted(Comparator.comparing(Product::getActualSoldOutNum).reversed()).collect(Collectors.toList());
        List<Product> saleProducts = Product.find.where().ne("name" + session().get("lang"), "").eq("status", 0).eq("isSale", true)
                .orderBy("soldNumber desc").setMaxRows(5).findList();

        session("CURRENT_TAB", "HOME");
        if ("pc".equals(session().get("platform")) || "wap".equals(session().get("platform"))) {
            play.Logger.info("union id on index: " + session("UNION_ID"));
            play.Logger.info("facebookId id on index: " + session("facebookId"));
            return ok(home.render(hotProducts, saleProducts));
        }
        if ("wx".equals(session().get("platform"))) {
            if (StrUtil.isNull(session("WX_OPEN_ID"))) {
                return wxAutoLogin(resellerCode);
            } else {
                play.Logger.info("wx open id on index: " + session("WX_OPEN_ID"));
                User found = findEarlyUserBy("openId", session("WX_OPEN_ID"));
                if (found == null) {
                    session().remove("WX_OPEN_ID");
                    return redirect(request().uri());
                } else {
                    // 页面加载业务
                    return ok(home.render(hotProducts, saleProducts));
                }
            }
        }
        return ok(index.render());
    }

    public static List<Catalog> getAllAvailableCatalogs() {
        List<Catalog> found = Catalog.find.where().ne("name" + session().get("lang"), "")
                .orderBy("showIndex asc, createdAt desc").findList();
        return found;
    }

    public static List<Info> getAllAvailableInfos(String classify) {
        List<Info> found = Info.find.where().eq("classify", classify)
                .eq("visible", true)
                .eq("status", 0)
                .orderBy("createdAt desc").findList();
        return found;
    }

    public static Result catalogPage(long id) {
        List<Product> found = Product.find.where().eq("catalogs.id", id).eq("status", 0)
                .ne("name" + session().get("lang"), "")
                .orderBy("price desc").findList();
        if (found.size() == 0) return noticePage(play.i18n.Messages.get("catalog.product.not.found"), "",
                play.i18n.Messages.get("back.to.home"), "/");

        Catalog catalog = Catalog.find.byId(id);
        String catalogName = "";
        if (catalog != null)
            catalogName = BaseController.getFieldValue(catalog, "name" + session().get("lang")).toString();
        return ok(catalog_products.render(found, catalogName));
    }

    public static List<Product> getCatalogProducts(long id) {
        List<Product> found = Product.find.where().eq("catalogs.id", id).eq("status", 0)
                .ne("name" + session().get("lang"), "")
                .orderBy("price desc").setMaxRows(5).findList();
        return found;
    }

    public static Boolean getSoldOut(List<Theme> themes) {
        int sum = 0;
        if (themes.isEmpty()) {
            play.Logger.info("getSoldOut:获取到的产品规格为空！");
            return false;
        }
        for (Theme theme : themes) {
            if (theme.inventory == 0 && theme.price != 0) {
                sum += 1;
            }
        }
        if (sum == themes.size()) {
            return true;
        }
        return false;
    }

    public static Result searchPage(String kw) {
        session("CURRENT_TAB", "SEARCH");
        List<Product> found = new ArrayList<>();
        if (StrUtil.isNull(kw)) {
            found = Product.find.where().eq("status", 0)
                    .ne("name" + session().get("lang"), "")
                    .orderBy("price desc").findList();
        } else {
            found = Product.find.where().eq("status", 0)
                    .like("name" + session().get("lang"), "%" + kw + "%")
                    .orderBy("price desc").findList();
        }
        if (found.size() == 0) return noticePage(play.i18n.Messages.get("no.product.search.result"), "",
                play.i18n.Messages.get("back.to.home"), "/");
        return ok(search.render(found, null));
    }

    public static Result productPage(long id, String lang) {
        if (StrUtil.isNotNull(lang)) changeLanguageDo(lang);

        Product found = Product.find.byId(id);
        if (found == null) return noticePage(play.i18n.Messages.get("product.not.found"), "",
                play.i18n.Messages.get("back.to.home"), "/");
        if (found.themes == null || found.themes.size() == 0)
            return noticePage(play.i18n.Messages.get("product.invalid"), "", play.i18n.Messages.get("back.to.home"), "/");

        String ticket = null;
        WxJsapiSignature signature = null;
        try {
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(ConfigBiz.getStrConfig("protocol") + "://"
                    + ConfigBiz.getStrConfig("domain.name") + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
        }

        if (StrUtil.isNull(ticket) || signature == null)
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");

        return ok(product_details.render(found, signature));
    }

    public static Result noticePage(String title, String text, String btn, String url) {
        return ok(notice.render(title, text, btn, url));
    }

    public static Result newsPage() {
        List<Info> found = Info.find.where().eq("classify", "公告")
                .eq("visible", true)
                .eq("status", 0)
                .orderBy("showIndex").findList();
        return ok(help.render(found));
    }

    public static List<Info> getNewsPage() {
        List<Info> found = Info.find.where().eq("classify", "公告")
                .eq("visible", true)
                .eq("status", 0)
                .orderBy("showIndex").findList();
        return found;
    }

    public static Result helpPage() {
        List<Info> found = Info.find.where().eq("classify", "帮助中心")
                .eq("visible", true)
                .eq("status", 0)
                .orderBy("showIndex").findList();
        return ok(help.render(found));
    }

    public static Result faqPage() {
        List<Info> found = Info.find.where().eq("classify", "常见问题")
                .eq("visible", true)
                .eq("status", 0)
                .orderBy("showIndex").findList();
        return ok(help.render(found));
    }

    @Security.Authenticated(Secured.class)
    public static Result memberPage() {
        session("CURRENT_TAB", "MEMBER");
        return ok(member.render(getCurrentLoginUser()));
//        return ok(member.render(User.find.findList().get(0)));
    }

    public static Result cartPage() {
        session("CURRENT_TAB", "CART");
        return ok(cart.render());
    }

    @Security.Authenticated(Secured.class)
    public static Result checkOutPage() {
        return ok(checkout.render());
    }

    @Security.Authenticated(Secured.class)
    public static Result addressCreatePage(String prePage) {
        return ok(address_new.render(getCurrentLoginUser(), null));
    }

    @Security.Authenticated(Secured.class)
    public static Result addressEditPage(Long id) {
        return ok(address_new.render(getCurrentLoginUser(), id));
    }

    @Security.Authenticated(Secured.class)
    public static Result myShipInfoPage() {
        return ok(my_shipinfo.render(getCurrentLoginUser()));
    }

    @Security.Authenticated(Secured.class)
    public static Result myCommentPage() {
        return ok(my_comment.render(getCurrentLoginUser()));
    }

    @Security.Authenticated(Secured.class)
    public static Result myReturnPage() {
        return ok(my_return.render(getCurrentLoginUser()));
    }

    @Security.Authenticated(Secured.class)
    public static Result myBalanceUsePage() {
        return ok(my_balance_use.render(getCurrentLoginUser()));
//        return ok(my_balance_use.render(User.find.findList().get(0)));
    }

    @Security.Authenticated(Secured.class)
    public static Result newReturnPage(long oid) {
        Purchase foundOrder = Purchase.find.byId(oid);
        return ok(new_return.render(getCurrentLoginUser(), foundOrder));
    }

    public static User getCurrentLoginUser() {
        if (StrUtil.isNotNull(session().get("UNION_ID")))
            return findEarlyUserBy("unionId", session().get("UNION_ID"));
        if (StrUtil.isNotNull(session().get("facebookId")))
            return findEarlyUserBy("facebookId", session().get("facebookId"));
        if (StrUtil.isNotNull(session().get("WX_OPEN_ID")))
            return findEarlyUserBy("openId", session().get("WX_OPEN_ID"));
        return null;
    }

    @Security.Authenticated(Secured.class)
    public static Result myOrderPage() {
        return ok(my_order.render(getCurrentLoginUser()));
    }

    @Security.Authenticated(Secured.class)
    public static Result myOrderDetailsPage(long id) {
        Purchase foundOrder = Purchase.find.byId(id);
        if (foundOrder == null) return noticePage(play.i18n.Messages.get("my.order.nothing"), "",
                play.i18n.Messages.get("notice.btn.back"), "/my/order");
        return ok(my_order_details.render(getCurrentLoginUser(), foundOrder));
    }

    @Security.Authenticated(Secured.class)
    public static Result myFavoritePage() {
        User found = getCurrentLoginUser();
        if (found.themes == null || found.themes.size() == 0)
            return noticePage(play.i18n.Messages.get("favorites.not.found"), "",
                    play.i18n.Messages.get("notice.btn.back"), "/member");
        return ok(my_favorites.render(found.themes));
    }

    @Security.Authenticated(Secured.class)
    public static Result myPromotePage() {
        return ok(my_promote.render(getCurrentLoginUser()));
    }

    public static Result servicePage() {
        return ok(service.render());
    }

    @Security.Authenticated(Secured.class)
    public static Result orderCommentPage(long id) {
        Purchase foundOrder = Purchase.find.byId(id);
        if (foundOrder == null) return noticePage(play.i18n.Messages.get("my.order.nothing"), "",
                play.i18n.Messages.get("notice.btn.back"), "/my/order");
        return ok(my_order_products.render(getCurrentLoginUser(), foundOrder));
    }

    @Security.Authenticated(Secured.class)
    public static Result addCommentPage(long oid, long tid) {
        Purchase order = Purchase.find.byId(oid);
        if (order == null) return noticePage(play.i18n.Messages.get("order.data.issue"), "",
                play.i18n.Messages.get("notice.btn.back"), "/order/comment/" + oid);
        Theme theme = Theme.find.byId(tid);
        if (theme == null) return noticePage(play.i18n.Messages.get("order.data.issue"), "",
                play.i18n.Messages.get("notice.btn.back"), "/order/comment/" + oid);

        HashMap<Long, Integer> checkResult = OrderBiz.checkOrderComment(order);
        if (checkResult.get(theme.id) == -1) return noticePage(play.i18n.Messages.get("product.comment.timeout"), "",
                play.i18n.Messages.get("notice.btn.back"), "/order/comment/" + oid);
        if (checkResult.get(theme.id) == 2) return noticePage(play.i18n.Messages.get("product.comment.limit"), "",
                play.i18n.Messages.get("notice.btn.back"), "/order/comment/" + oid);

        return ok(comment_add.render(getCurrentLoginUser(), order, theme));
    }

    public static Result wxMpNotice() {
        return noticePage(play.i18n.Messages.get("open.in.wx.mp"), "",
                play.i18n.Messages.get("back.to.home"), "/");
    }
}
