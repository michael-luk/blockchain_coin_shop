package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.PageInfo;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;
import com.avaje.ebean.Transaction;
import controllers.biz.ConfigBiz;
import controllers.biz.SaveBiz;
import models.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.w3c.dom.Document;
import play.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.XPath;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.MethodName;
import util.Role;
import views.html.gen.purchase;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static controllers.Application.sendWebSocketByChannelTag;
import static play.data.Form.form;

public class CartController extends Controller implements IConst {

    public static Result getCart() {
        Msg<String> msg = new Msg<>();

        // 检查session, 若有, 读取出来
        // 检查登录
        // 若已登录, 看看数据库有没有cart, 有的话和session合并
        // 没有的话采用session
        // 若未登录, 采用session

        String sessionCartStr = session("CART");

        if (StrUtil.isNull(sessionCartStr)) {
            sessionCartStr = "{\"items\":[]}";
            play.Logger.info("创建session空购物车");
            session("CART", sessionCartStr);
        }

        if (StrUtil.isNotNull(session(SESSION_USER_ID))) {
            // 已登录
            long uid = Long.parseLong(session(SESSION_USER_ID));
            Cart cart = Cart.find.where().eq("refUserId", uid).findUnique();
            if (cart == null) {
                cart = new Cart();
                cart.refUserId = uid;
                cart.name = sessionCartStr;
                cart.save();
                play.Logger.info("创建db空购物车");
            } else {
//                // 合并
//                CartParser sessionCartObj = Json.fromJson(Json.parse(sessionCartStr), CartParser.class);
//                CartParser dbCartObj = Json.fromJson(Json.parse(cart.name), CartParser.class);
//
//                CartParser mergedCartObj = new CartParser();
//                mergedCartObj.items = new ArrayList<>();
//
//                for (CartItemParser sessionItem : sessionCartObj.items) {
//                    if (!hasCartItem(sessionItem, mergedCartObj)) {
//                        mergedCartObj.items.add(sessionItem);
//                    }
//                }
//                for (CartItemParser dbItem : dbCartObj.items) {
//                    if (!hasCartItem(dbItem, mergedCartObj))
//                        mergedCartObj.items.add(dbItem);
//                    else {
//                        for (CartItemParser mergedItem : mergedCartObj.items) {
//                            if (mergedItem.pid == dbItem.pid) {
//                                mergedItem.num = dbItem.num;
//                                mergedItem.select = dbItem.select;
//                            }
//                        }
//                    }
//                }
//
//                int cartNum = 0;
//                // 防止session过长, 计算cartnum
//                for (CartItemParser item : mergedCartObj.items) {
//                    item.theme = null;
//                    cartNum += item.num;
//                }
//
//                sessionCartStr = Json.toJson(mergedCartObj).toString();
//                cart.name = sessionCartStr;
//                cart.save();
//                play.Logger.info("合并session和db购物车");

                // 更新session
                sessionCartStr = cart.name;
                int cartNum = 0;
                // 防止session过长, 计算cartnum
                for (CartItemParser item : (Json.fromJson(Json.parse(cart.name), CartParser.class)).items) {
                    item.theme = null;
                    cartNum += item.num;
                }
                play.Logger.info("读取db购物车");
                session("CART", sessionCartStr);
                session("CART_NUM", Integer.toString(cartNum));
            }
        }

        msg.flag = true;
        msg.data = sessionCartStr;
        play.Logger.info("获取购物车 - " + sessionCartStr);
        return ok(Json.toJson(msg));
    }

//    public static boolean hasCartItem(CartItemParser checkItem, CartParser cartParser) {
//        for (CartItemParser item : cartParser.items) {
//            if (item.pid == checkItem.pid)
//                return true;
//        }
//        return false;
//    }

    public static Result setCart() {
        Msg<CartParser> msg = new Msg<>();
        Form<CartParser> httpForm = form(CartParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            CartParser formObj = httpForm.get();

            int cartNum = 0;
            if (formObj.items == null) {
                formObj.items = new ArrayList<>();
            } else {
                // 防止session过长, 计算cartnum
                for (CartItemParser item : formObj.items) {
                    item.theme = null;
                    cartNum += item.num;
                }
            }

            String currentCartStr = Json.toJson(formObj).toString();

            if (StrUtil.isNotNull(session(SESSION_USER_ID))) {
                // 已登录
                long uid = Long.parseLong(session(SESSION_USER_ID));
                Cart cart = Cart.find.where().eq("refUserId", uid).findUnique();
                if (cart == null) {
                    cart = new Cart();
                    cart.refUserId = uid;
                    cart.name = currentCartStr;
                    cart.save();
                    play.Logger.info("创建db空购物车");
                } else {
                    cart.name = currentCartStr;
                    cart.save();
                    play.Logger.info("更新db购物车");
                }
            }

            session("CART", currentCartStr);
            session("CART_NUM", Integer.toString(cartNum));
            msg.flag = true;
            msg.data = formObj;
            play.Logger.info("更新购物车");
        } else {
            play.Logger.error("更新购物车失败");
            msg.message = httpForm.errors().toString();
        }
        return ok(Json.toJson(msg));
    }

    public static class CartParser {
        public List<CartItemParser> items;
    }

    public static class CartItemParser {
        public long pid;
        public Integer num;
        public Theme theme;
        public String productName;
        public Boolean select;
    }
}
