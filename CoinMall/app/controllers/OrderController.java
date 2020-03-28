package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.PageInfo;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import controllers.biz.ConfigBiz;
import controllers.biz.SaveBiz;
import controllers.biz.UserBiz;
import models.*;
import org.apache.commons.lang3.ArrayUtils;
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
import views.html.gen.purchase_backend;

import javax.imageio.ImageIO;
import javax.persistence.PersistenceException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static controllers.ApiController.doPayBalance;
import static controllers.ApiController.payBalance;
import static controllers.Application.index;
import static controllers.Application.sendWebSocketByChannelTag;
import static controllers.biz.UserBiz.findEarlyUserBy;
import static play.data.Form.form;

public class OrderController extends Controller implements IConst {

    public static F.Promise<String> getNewWalletAddress() {
        // http://218.93.208.191:20001/Api.aspx?action=getnewaddr&uid=1&rid=1001&account=xwc2017&accountpass=myxwc2017adminok
        String url = String.format("http://%s?action=api_getnewaddr&uid=1&rid=1001&account=%s&accountpass=%s",
                ConfigBiz.getStrConfig("coin.api.host"), ConfigBiz.getStrConfig("coin.api.account"),
                ConfigBiz.getStrConfig("coin.api.psw"));
        play.Logger.info("生成新钱包地址url: " + url);

        F.Promise<WSResponse> response = WS.url(url).get();
        return response.map(resp -> {
            String respStr = "";
            try {
                JsonNode jsonResult = resp.asJson();
                respStr = jsonResult.get("result").asText();
                play.Logger.info("response getnewaddr: " + respStr);
            } catch (Exception ex) {
                play.Logger.error("response getnewaddr issue: " + respStr + ". ex: " + ex.getMessage());
            }
            return respStr;
        });
    }

    public static Result getCoinTxs(Integer count) {
        return ok(getCoinTxsDo(count).get(60 * 1000));
    }

    public static F.Promise<String> getCoinTxsDo(Integer count) {
        // http://218.93.208.191:20001/Api.aspx?action=api_listtransactions&count=10&from=0&account=xwc2017&accountpass=myxwc2017adminok
        String url = String.format("http://%s?action=api_listtransactions&count=%d&from=0&account=%s&accountpass=%s",
                ConfigBiz.getStrConfig("coin.api.host"),
                count,
                ConfigBiz.getStrConfig("coin.api.account"),
                ConfigBiz.getStrConfig("coin.api.psw"));
        play.Logger.info("获取最新交易url: " + url);

        F.Promise<WSResponse> response = WS.url(url).get();
        return response.map(resp -> {
            String respStr = "";
            try {
                JsonNode jsonResult = resp.asJson();
                if ("0".equals(jsonResult.get("code").asText())) {
                    ArrayNode results = (ArrayNode) jsonResult.get("result");
                    for (JsonNode jsonNode : results) {
                        CoinRecord coinRecord = Json.fromJson(jsonNode, CoinRecord.class);
                        if ("receive".equals(coinRecord.category)) {
                            coinRecord.save();
                        }
                    }
                    respStr = String.valueOf(results.size());
                }
                play.Logger.info("response listtransactions: " + respStr);
            } catch (Exception ex) {
                play.Logger.error("response listtransactions issue: " + respStr + ". ex: " + ex.getMessage());
            }
            return respStr;
        });
    }

    public static Result newOrder() {
        Msg<Purchase> msg = new Msg<>();

        Form<PurchaseParser> httpForm = form(PurchaseParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            PurchaseParser formObj = httpForm.get();
            Purchase newObj = new Purchase();

            if (formObj.pids.split(",").length != formObj.tids.split(",").length
                    || formObj.pids.split(",").length != formObj.quantity.split(",").length
                    || formObj.tids.split(",").length != formObj.quantity.split(",").length) {
                msg.message = play.i18n.Messages.get("order.data.issue");
                play.Logger.error(msg.message);
                return ok(Json.toJson(msg));
            }

            String newOrderNo = getNewWalletAddress().get(60 * 1000);
            if (StrUtil.isNull(newOrderNo)) {
                msg.message = play.i18n.Messages.get("order.no.create.issue");
                play.Logger.error(msg.message);
                return ok(Json.toJson(msg));
            }

            BitMatrix bitMatrix = null;
            try {
                bitMatrix = new MultiFormatWriter().encode(newOrderNo, BarcodeFormat.QR_CODE, 280, 280);
            } catch (WriterException e) {
                msg.message = play.i18n.Messages.get("barcode.create.issue");
                play.Logger.error(msg.message);
                return ok(Json.toJson(msg));
            }
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            String barcodeFilePath = Play.application().path().getPath() + "\\public\\barcode\\" + newOrderNo + ".png";
            File file = new File(barcodeFilePath);
            String format = "PNG";
            try {
                ImageIO.write(image, format, file);
            } catch (IOException e) {
                msg.message = play.i18n.Messages.get("barcode.save.issue");
                play.Logger.error(msg.message);
                return ok(Json.toJson(msg));
            }

            newObj.name = newOrderNo;
            newObj.coinPayAddr = newOrderNo;
            newObj.images = newOrderNo + ".png";

            newObj.quantity = formObj.quantity;
            newObj.pids = formObj.pids;
            newObj.tids = formObj.tids;
            newObj.amount = formObj.amount;
            newObj.coinAmount = formObj.coinAmount;

            newObj.shipName = formObj.shipName;
            newObj.shipPhone = formObj.shipPhone;
            newObj.shipProvince = formObj.shipProvince;
            newObj.shipCity = formObj.shipCity;
            newObj.shipZone = formObj.shipZone;
            newObj.shipLocation = formObj.shipLocation;
            newObj.buyerMessage = formObj.buyerMessage;
            newObj.description1 = formObj.description1;

            User parentUser = User.find.byId(formObj.refUserId);
            newObj.user = parentUser;
            newObj.refUserId = formObj.refUserId;

            newObj.useBalance = formObj.useBalance;

            double rate = ConfigBiz.getFloatConfig("coin.exchange.rate");
            double coinPayDiscount = ConfigBiz.getFloatConfig("coin.price.discount");
            double actualDiscount = newObj.useBalance * rate / coinPayDiscount;
            if (newObj.useBalance > 0) {
                newObj.balanceDiscount = actualDiscount;
            }
            SaveBiz.beforeSave(newObj);
            Ebean.save(newObj);

            Transaction txn = Ebean.beginTransaction();
            try {
                newObj.themes = new ArrayList<>();
                String[] quantities = formObj.quantity.split(",");
                String[] productIds = formObj.pids.split(",");
                String[] themeIds = formObj.tids.split(",");
                for (int i = 0; i < themeIds.length; i++) {
                    Product product = Product.find.byId(Long.parseLong(productIds[i]));
                    Theme theme = Theme.find.byId(Long.parseLong(themeIds[i]));
                    int quantity = Integer.parseInt(quantities[i]);

                    // 检查库存
                    if (quantity > theme.inventory) {
                        msg.message = BaseController.getFieldValue(product, "name" + session("lang")) + "-"
                                + BaseController.getFieldValue(theme, "name" + session("lang")) + " "
                                + play.i18n.Messages.get("inventory.issue");
                        play.Logger.info(msg.message);
                        return ok(Json.toJson(msg));
                    }

//                    product.soldNumber += quantity;
                    theme.soldNumber += quantity;
                    theme.inventory -= quantity;
                    if (theme.inventory < 0) theme.inventory = 0;

                    newObj.themes.add(theme);
                    theme.purchases.add(newObj);
//                    Ebean.update(product);
                    Ebean.update(theme);
                }
                Ebean.update(newObj);

                if (newObj.useBalance > 0) {

                    // 创建余额记录
                    BalanceUse balanceUse = new BalanceUse();
                    balanceUse.name = newObj.name;
                    balanceUse.coin = newObj.useBalance;
                    balanceUse.rate = rate;
                    balanceUse.money = actualDiscount;
                    balanceUse.user = parentUser;
                    balanceUse.refUserId = parentUser.id;

                    if (Math.abs(newObj.amount - actualDiscount) < ConfigBiz.getFloatConfig("coin.pay.range")) {
                        // 若余额使用刚好抵扣订单, 则不需要走微信/xwc支付, 直接生成订单
                        play.Logger.info("余额使用刚好抵扣订单, No: " + newObj.name);
                        newObj = Purchase.find.byId(newObj.id);
                        newObj.status = 1;
                        Ebean.update(newObj);
                        play.Logger.info("订单设为已支付, No: " + newObj.name);
                        msg.message = play.i18n.Messages.get("balance.pay.all");
                    } else {
                        // 正常抵扣
                        newObj = Purchase.find.byId(newObj.id);
                        newObj.payAmount += actualDiscount;
                        Ebean.update(newObj);
                        play.Logger.info("余额抵扣订单, No: " + newObj.name + ", 抵扣法币: " + actualDiscount);
                    }

                    // 接口扣斗地主
//                    doPayBalance(parentUser.unionId, newObj.useBalance, "", parentUser.id).get(5 * 1000);
//                    play.Logger.info("扣斗地主余额成功");

                    balanceUse.status = 1;
                    balanceUse.save();
                }
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

    public static class PurchaseParser {

        public long refUserId;
        public String quantity;
        public double amount;
        public double coinAmount;
        public String coinPayAddr;
        public String images;
        public String coinPayTrans;
        public int useVipPoint;
        public double vipPointDiscount;
        public double useBalance;
        public String shipName;
        public String shipPhone;
        public String shipProvince;
        public String shipCity;
        public String shipZone;
        public String shipLocation;
        public String buyerMessage;
        public String shipTime;
        public String payReturnCode;
        public String payReturnMsg;
        public String payResultCode;
        public String payTransitionId;
        public String payAmount;
        public String payTime;
        public String description1;
        public String description2;
        public String comment;
        public String pids;
        public String tids;

        public String validate() {
            User user = User.find.byId(refUserId);
            if (user == null) {
                return play.i18n.Messages.get("order.no.user.issue");
            }
//            if (StrUtil.isNull(user.unionId)) {
//                return play.i18n.Messages.get("order.use.balance.no.uid");
//            }
            if (amount < useBalance * ConfigBiz.getFloatConfig("coin.exchange.rate") / ConfigBiz.getFloatConfig("coin.price.discount") - ConfigBiz.getFloatConfig("coin.pay.range")) {
                return play.i18n.Messages.get("order.over.balance");
            }
            return null;
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result updateUserOrderStatus(long id, int newStatus) {
        Msg<Purchase> msg = new Msg<>();

        Purchase found = Purchase.find.byId(id);
        if (found == null) {
            msg.message = play.i18n.Messages.get("my.order.nothing");
            play.Logger.error(msg.message);
            return ok(Json.toJson(msg));
        }

        found.status = newStatus;


        Ebean.update(found);
        msg.flag = true;
        msg.message = play.i18n.Messages.get("action.success");
        msg.data = found;
        play.Logger.info(UPDATE_SUCCESS);

        //判断状态是否是删除状态，如果是则释放内存。
        if (newStatus == 3) {
            //订单更新之后返回库存数量。
            List<Theme> themes = found.themes;
            play.Logger.info("upload success：size" + themes.size());
            //字符串截取并转为数字。
            String quantity = found.quantity;
            String[] quantitys = quantity.split(",");
            play.Logger.info("upload success：str" + quantity);
            for (int i = 0; i < themes.size(); i++) {
                Theme theme = themes.get(i);
                String str = quantitys[i].toString();
                int num = 0;
                if (StrUtil.isNotNull(str)) {
                    num = Integer.parseInt(str);
                    play.Logger.info("upload success：num" + num);
                }
                //库存数。
                theme.inventory = theme.inventory + num;
                //已售数量
                theme.soldNumber = theme.soldNumber - num;
                theme.save();
                play.Logger.info("upload success：" + theme.inventory);
            }

            // 若已经支付部分余额, 则返还
            if (found.useBalance > 0) {
//                doPayBalance(found.user.unionId, found.useBalance, "refund", found.user.id).get(5 * 1000);
//                play.Logger.info("返还斗地主余额成功");

                // 创建余额记录
                BalanceUse balanceUse = new BalanceUse();
                balanceUse.name = found.name;
                balanceUse.coin = found.useBalance;
                balanceUse.user = found.user;
                balanceUse.refUserId = found.user.id;
                balanceUse.status = 3;
                balanceUse.save();
            }
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result updateOrderStatusByWxPay(long id) {
        Msg<Purchase> msg = new Msg<>();

        Purchase found = Purchase.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("updateOrderStatusByWxPay: " + msg.message);
            return ok(Json.toJson(msg));
        }

        // 更改订单状态, 只能从"0待支付"改为"12等待支付通知"
        if (found.status == 0) {
            found.status = 6;
            Ebean.update(found);

            msg.flag = true;
            msg.data = found;
            play.Logger.info("updateOrderStatusByWxPay: " + UPDATE_SUCCESS);
        } else {
            if (found.status == 1) {
                msg.message = "订单已支付";
                play.Logger.info("updateOrderStatusByWxPay: " + msg.message);
            } else {
                msg.message = "订单初始状态有误, status: " + found.status;
                play.Logger.error("updateOrderStatusByWxPay: " + msg.message);
            }
        }
        return ok(Json.toJson(msg));
    }

    public static Result checkAndKillUselessOrders() {
        Msg<Integer> msg = new Msg<>();
        if (!Application.taskFlag) return ok(Json.toJson(msg));

        List<Purchase> records = Purchase.find.where().eq("status", 0).findList();

        Date nowDate = new Date();
        long expireMillSecconds = ConfigBiz.getIntConfig("order.clean.interval.mins") * 60 * 1000L;
        if (expireMillSecconds == 0) expireMillSecconds = 3 * 60 * 1000L; // 默认3min

        int killOrderCount = 0;
        for (Purchase order : records) {
            Date orderDate = order.createdAt;
            long MillSeconds = nowDate.getTime() - orderDate.getTime();

            if (MillSeconds >= expireMillSecconds) {
                order.status = 2;
                order.save();
                //订单更新之后返回库存数量。
                List<Theme> themes = order.themes;
                //字符串截取并转为数字。
                String quantity = order.quantity;
                String[] quantitys = quantity.split(",");
                for (int i = 0; i < themes.size(); i++) {
                    Theme theme = themes.get(i);
                    String str = quantitys[i].toString();
                    int num = 0;
                    if (StrUtil.isNotNull(str)) {
                        num = Integer.parseInt(str);
                    }
                    //库存数。
                    theme.inventory = theme.inventory + num;
                    //已售数量
                    theme.soldNumber = theme.soldNumber - num;
                    theme.save();
                }

                // 若已经支付部分余额, 则返还
                if (order.useBalance > 0) {
//                    doPayBalance(order.user.unionId, order.useBalance, "refund", order.user.id).get(5 * 1000);
//                    play.Logger.info("返还斗地主余额成功");

                    // 创建余额记录
                    BalanceUse balanceUse = new BalanceUse();
                    balanceUse.name = order.name;
                    balanceUse.coin = order.useBalance;
                    balanceUse.user = order.user;
                    balanceUse.refUserId = order.user.id;
                    balanceUse.status = 3;
                    balanceUse.save();
                }
                killOrderCount++;
            }
        }

        records = Purchase.find.where().eq("status", 4).findList(); //找出已发货的订单, 超时设为确认收货

        expireMillSecconds = ConfigBiz.getIntConfig("order.confirm.days") * 24 * 60 * 60 * 1000L;
        if (expireMillSecconds == 0) expireMillSecconds = 10 * 24 * 60 * 60 * 1000L; // 默认10 day

        int confirmOrderCount = 0;
        for (Purchase order : records) {
            if (order.shipTime == null) continue;
            Date orderDate = order.shipTime;
            long MillSeconds = nowDate.getTime() - orderDate.getTime();

            if (MillSeconds >= expireMillSecconds) {
                order.status = 5;
                order.save();
                confirmOrderCount++;
            }
        }

        records = Purchase.find.where().eq("status", 5).findList(); //找出已确认收货的订单, 超时好评
        expireMillSecconds = ConfigBiz.getIntConfig("comment.day.limit") * 24 * 60 * 60 * 1000L;
        if (expireMillSecconds == 0) expireMillSecconds = 15 * 24 * 60 * 60 * 1000L; // 默认15 day
        int setDefaultCommentOrderCount = 0;
        for (Purchase order : records) {
            if (order.shipTime == null) continue;
            Date orderDate = order.shipTime;
            long MillSeconds = nowDate.getTime() - orderDate.getTime();

            if (MillSeconds >= expireMillSecconds) {
                // 遍历查看订单内theme有没有评论过, 没有评论的theme加一个默认好评
                for (Theme theme : order.themes) {
                    if (CommentInfo.find.where().eq("refUserId", order.user.id).eq("refPurchaseId", order.id)
                            .eq("refProductId", theme.refProductId).findRowCount() == 0) {

                        CommentInfo newObj = new CommentInfo();
                        newObj.name = ConfigBiz.getStrConfig("comment.default");
                        newObj.user = order.user;
                        newObj.refUserId = order.user.id;
                        newObj.product = theme.product;
                        newObj.refProductId = theme.refProductId;
                        newObj.refPurchaseId = order.id;
                        newObj.purchase = order;
                        newObj.comment = UserBiz.getHideName(order.user.name);
                        SaveBiz.beforeSave(newObj);
                        Ebean.save(newObj);
                        setDefaultCommentOrderCount++;
                        play.Logger.info("订单超时默认好评, 订单id: " + order.id);
                    }
                }
            }
        }

        if (killOrderCount > 0 || confirmOrderCount > 0 || setDefaultCommentOrderCount > 0) {
            msg.flag = true;
            msg.data = killOrderCount;
            play.Logger.info("checkAndKillUselessOrders: " + killOrderCount + ", confirm: " + confirmOrderCount
                    + ", set comment: " + setDefaultCommentOrderCount);
        } else {
            msg.message = NO_FOUND;
        }
        return ok(Json.toJson(msg));
    }

    public static Result checkUserBought(long pid) {
        Msg msg = new Msg<>();
        User user = null;
        if ("pc".equals(session().get("platform")) || "wap".equals(session().get("platform"))) {
            if (StrUtil.isNotNull(session("UNION_ID")))
                user = findEarlyUserBy("unionId", session("UNION_ID"));
            if (StrUtil.isNotNull(session("facebookId")))
                user = findEarlyUserBy("facebookId", session().get("facebookId"));
        }
        if ("wx".equals(session().get("platform"))) {
            if (StrUtil.isNotNull(session("WX_OPEN_ID"))) {
                user = findEarlyUserBy("openId", session("WX_OPEN_ID"));
            }
        }

        if (user != null) {
            for (Purchase purchase : user.purchases) {
                if (purchase.status == 5) {
                    // 已确认收货的才能发评论
                    for (Theme theme : purchase.themes) {
                        if (theme.product.id == pid || theme.refProductId == pid) {
                            msg.flag = true;
                            return ok(Json.toJson(msg));
                        }
                    }
                }
            }
        }
        return ok(Json.toJson(msg));
    }
}
