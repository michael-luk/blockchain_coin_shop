package controllers.biz;

import LyLib.Interfaces.IConst;
import LyLib.Utils.*;
import controllers.EmailController;
import controllers.Secured;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static controllers.ApiController.doPayBalance;

public class OrderBiz extends Controller implements IConst {

    public static HashMap<Long, Integer> getOrderDetailThemeNumbers(Purchase order) {
        HashMap<Long, Integer> result = new HashMap<>();
        if (order == null || StrUtil.isNull(order.tids) || StrUtil.isNull(order.quantity)) return result;
        String[] tids = order.tids.split(",");
        String[] quantitis = order.quantity.split(",");
        if (tids.length != quantitis.length) return result;
        for (int i = 0; i < quantitis.length; i++) {
            result.put(Long.parseLong(tids[i]), Integer.parseInt(quantitis[i]));
        }
        return result;
    }

    public static boolean updateOrderStatusOnCoinReceived(String address, String txid, long time, double amount) {
        List<Purchase> orders = Purchase.find.where().eq("name", address).eq("status", 0)
                .orderBy("createdAt desc").findList();
        if (orders.size() == 0) return false;

        if (orders.get(0).payAmount + amount >= orders.get(0).coinAmount - ConfigBiz.getFloatConfig("coin.pay.minus")) {
            orders.get(0).status = 1;
            play.Logger.info("订单收款改状态: " + orders.get(0).name);
        } else {
            play.Logger.info("订单收款累积: " + amount);
        }
        orders.get(0).payAmount += amount;
        orders.get(0).coinPayTrans = txid;
        orders.get(0).payTime = DateUtil.UnixTime2Date(time * 1000);
        orders.get(0).save();

        // 处理余额
        if (orders.get(0).balanceDiscount > 0) {
            BalanceUse balanceUse = BalanceUse.find.where().eq("name", orders.get(0).name).findUnique();
            if (balanceUse != null) {
                if (balanceUse.status == 0) {
                    balanceUse.status = 1;
                    balanceUse.save();
                    play.Logger.info("微信支付成功更新余额使用记录状态: " + balanceUse.name);
                }
            }
            // 接口扣斗地主
            doPayBalance(orders.get(0).user.unionId, orders.get(0).useBalance, "", orders.get(0).user.id).get(5 * 1000);
            play.Logger.info("扣斗地主余额成功");
        }

        play.Logger.info("分销佣金: " + calculateReseller(orders.get(0), false));
        return true;
    }

    public static String genOrderNo(String orderNo, Long orderId) {
        if (orderNo.length() > 32) {
            return orderNo.substring(0, 19) + "_" + orderId;
        } else {
            return orderNo;
        }
    }

    public static double calculateReseller(Purchase order, boolean isMoneyPay) {
        play.Logger.info("执行分销计算，于订单id: " + order.id);

        User uplineUser = User.find.byId(order.user.uplineUserId);
        if (uplineUser == null) {
            play.Logger.info("订单用户无上线");
            return 0;
        }

        // 处理统计
        uplineUser.downlineOrderCount++;
        uplineUser.currentTotalOrderAmount += order.amount;
        for (int quantity : OrderBiz.getOrderDetailThemeNumbers(order).values()) {
            uplineUser.downlineProductCount += quantity;
        }

        // 处理佣金
        float rate = ConfigBiz.getFloatConfig("reseller.rate");
        double orderAvailableAmount = order.amount;
        if (!isMoneyPay) orderAvailableAmount = order.coinAmount;

        double profit = orderAvailableAmount * rate;

        if (isMoneyPay)
            uplineUser.currentResellerProfitMoney += profit;
        else
            uplineUser.currentResellerProfitCoin += profit;

        uplineUser.save();
        return profit;
    }

    public static HashMap<Long, Integer> checkOrderComment(Purchase order) {
        // 1. 检查是否在有效评论时间内
        // 2. 检查有无评论 -> 可评论
        // 3. 追评

        // -1 超时, 0, 评论, 1, 追评, 2, 超次数
        HashMap<Long, Integer> result = new HashMap<>();
        for (Theme theme : order.themes) result.put(theme.id, -1);

        if (order.shipTime == null) return result;
        if (DateUtil.compareTimeDiffInMillSecond((new Date()), order.shipTime) / 1000 / 60 / 60 / 24
                > ConfigBiz.getIntConfig("comment.day.limit")) return result;

        for (Theme theme : order.themes) {
            int commentCount = CommentInfo.find.where().ne("status", 2).eq("refPurchaseId", order.id)
                    .eq("refProductId", theme.refProductId).findRowCount();
            if (commentCount >= 2) result.put(theme.id, 2);
            else result.put(theme.id, commentCount);
        }
        return result;
    }
}
