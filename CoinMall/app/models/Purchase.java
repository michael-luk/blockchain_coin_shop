package models;

import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;
import util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "purchase")
@TableComment("订单")
public class Purchase extends Model implements IConst {

    @Id
    public long id;

    @SearchField
    @Comment("订单号")
    public String name; // 订单号

    @Comment("购买用户ID")
    public long refUserId;// 用户ID

    @JsonIgnore
    @Comment("购买用户")
    @ManyToOne
    public User user; // 购买用户

    @Comment("状态")
    @util.EnumMap(value = "0,1,2,3,4,5,6,7,8,9,10", name = "待支付,已支付,已取消,已删除,已发货,已确认,支付中,支付失败,数据有误,退换货协商中,退换货成功")
    public int status;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("订单商品规格")
    @ManyToMany(targetEntity = Theme.class)
    public List<Theme> themes; //

    @Comment("订单商品数量")
    public String quantity;// 数量 逗号分隔如 1,3,5 表示orderProducts里面的1号产品有1份,
    // 2号产品有3份, 3号产品有5份

    @Comment("订单商品ids")
    public String pids;

    @Comment("订单规格ids")
    public String tids;

    @Comment("订单法币金额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double amount; // 订单总额(含运费)

    @Comment("订单虚拟币金额")
    @Column(columnDefinition = "Decimal(18,8)")
    public double coinAmount;

    @Comment("订单虚拟币支付地址")
    public String coinPayAddr;

    @Comment("订单虚拟币支付二维码")
    public String images;

    @Comment("订单虚拟币交易号")
    public String coinPayTrans;

    @Comment("使用积分")
    public int useVipPoint;

    @Comment("积分抵扣金额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double vipPointDiscount;

    @Comment("使用余额")
    @Column(columnDefinition = "Decimal(18,8)")
    public double useBalance;

    @Comment("余额抵扣金额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double balanceDiscount;

    @Comment("收货人")
    public String shipName; // 名称

    @Comment("联系电话")
    public String shipPhone; //

    @Comment("省")
    public String shipProvince; //

    @Comment("市")
    public String shipCity; //

    @Comment("区")
    public String shipZone; //

    @Comment("地址")
    public String shipLocation; //

    @Comment("买家留言")
    public String buyerMessage; //

    @Comment("发货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date shipTime; //  日期字符串表示

    @Comment("快递单号")
    public String shipNo;

    @Comment("支付返回码")
    public String payReturnCode; // 支付执行后第三方的返回码SUCCESS/FAIL

    @Comment("支付返回信息")
    public String payReturnMsg; // 支付执行后第三方的返回消息(return code=FAIL才显示)

    @Comment("支付业务结果")
    public String payResultCode; // 支付执行后第三方的业务结果SUCCESS/FAIL

    @Comment("支付流水ID")
    public String payTransitionId; // 支付执行后第三方的流水ID

    @Comment("实际支付金额")
    @Column(columnDefinition = "Decimal(18,8)")
    public double payAmount;

    @Comment("实际支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date payTime; // 支付执行后第三方的支付时间

    @Comment("支付银行")
    public String payBank;

    @Comment("回传订单号")
    public String payRefOrderNo;

    @Comment("支付签名")
    public String paySign;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("描述1")
    public String description1;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("描述2")
    public String description2;

    @Comment("备注")
    public String comment;

    @Comment("退换货申请")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "purchase")
    public List<ReturnInfo> returnInfos;

    @Comment("商品评论")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "purchase")
    public List<CommentInfo> commentInfos;

    public Purchase() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, Purchase> find = new Finder(Long.class, Purchase.class);

//    public static String getCorrectQuantityStr(List<Product> orderProducts, String quantityStr) {
//        if (orderProducts.size() <= 1) {
//            return quantityStr;
//        }
//
//        List<ShoppingCartItem> cartItems = new ArrayList<>();
//        List<Integer> quantityList = StrUtil.getIntegerListFromSplitStr(quantityStr);
//
//        for (int i = 0; i < orderProducts.size(); i++) {
//            cartItems.add(new ShoppingCartItem(orderProducts.get(i), quantityList.get(i)));
//        }
//
//        // 产品按ID排序
//        Collections.sort(cartItems, new Comparator<ShoppingCartItem>() {
//            public int compare(ShoppingCartItem arg0, ShoppingCartItem arg1) {
//                return arg0.product.id.compareTo(arg1.product.id);
//            }
//        });
//
//        String correctQuantityStr = "";
//        for (ShoppingCartItem cartItem : cartItems) {
//            correctQuantityStr += cartItem.quantity + ",";
//            play.Logger.info("当前订单产品: " + cartItem.toString());
//        }
//        correctQuantityStr = correctQuantityStr.substring(0, correctQuantityStr.length() - 1);
//        play.Logger.info("当前订单产品数量Str: " + correctQuantityStr);
//        return correctQuantityStr;
//    }

    @Override
    public String toString() {
        return "Order [No:" + name + "]";
    }
}

