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
import java.util.Date;
import java.util.List;

import static controllers.biz.UserBiz.findEarlyUserBy;

@Entity
@Table(name = "user")
@TableComment("用户")
@OnlyAdminGet
public class User extends Model implements IConst {

    @Id
    public long id;

    @Comment("昵称")
    @SearchField
    public String name;

    @Comment("公众号ID")
    public String openId;

    @Comment("开放平台ID")
    public String wxOpenId;

    @Comment("UID")
    public String unionId;

    @Comment("FacebookID")
    public String facebookId;

    @Comment("email")
    public String email;        //用于找回密码, 如果loginName约定是email, 则此字段可以不要

    @Comment("email已验证")
    public boolean isEmailVerified;

    @Comment("email临时key")
    public String emailKey;     //找回密码, 或验证邮箱时暂存的key

    @Comment("email超时")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date emailOverTime;  //找回密码, 或验证邮箱时暂存的过期时间

    @Comment("头像")
    public String headImage;

    @Comment("图片")
    @Column(columnDefinition = "varchar(400)")
    public String images; // 图片(多个图片逗号分隔)

    @Comment("性别")
    @EnumMap(value = "0,1,2", name = "未设定,男,女")
    public int sexEnum;

    @Comment("联系电话")
    public String phone;

    @Comment("积分")
    public int vipPoint;

    @Comment("斗地主余额")
    @Column(columnDefinition = "Decimal(18,8)")
    public double balance;

    @Comment("白币地址")
    public String coinAddress;

    @Comment("白币余额")
    @Column(columnDefinition = "Decimal(18,8)")
    public double coinBalance;

    @Comment("身份证")
    public String cardNumber;

    @Comment("国家")
    public String country;

    @Comment("省份")
    public String province;

    @Comment("城市")
    public String city;

    @Comment("区域")
    public String zone;

    @Comment("地址")
    public String address;

    @Comment("出生")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date birth;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("最后登录IP")
    public String lastLoginIp;// 最后一次登录的IP(只是管理员登录才会记录)

    @Comment("最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastLoginTime;

    @Comment("最后登录IP区域")
    public String lastLoginIpArea; // IP所在区域

    @Comment("状态")
    @EnumMap(value = "0,1", name = "正常,冻结")
    public int status = 0;     // 0-正常, 1-冻结

    @Comment("等级")
    @EnumMap(value = "0,1", name = "普通用户,高级用户")
    public int userRoleEnum = 1; // 状态: 0普通用户, 1管理员, 2超级管理员

    @Column(columnDefinition = "varchar(2000)")
    @Comment("备注")
    public String comment;

    @Comment("收货地址")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<ShipInfo> shipInfos;

    @Comment("所下订单")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<Purchase> purchases;

    @Comment("退换货申请")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<ReturnInfo> returnInfos;

    @Comment("商品评论")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<CommentInfo> commentInfos;

    @Comment("收藏商品")
    @ManyToMany(targetEntity = models.Theme.class)
    public List<Theme> themes;

    @Comment("佣金提现")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<Fundout> fundouts;

    @Comment("余额记录")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<BalanceUse> balanceUses;


    // 分销相关
    @Comment("上线用户ID")
    public long uplineUserId = -1L; //(默认是上帝子民, 上帝不抽佣金)

    @Comment("上线用户名")
    public String uplineUserName; // 上线用户name

    @Comment("成为下线时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date becomeDownlineTime; // 成为下线时间

    @Comment("分销码")
    public String resellerCode;// 分销码

    @Comment("分销二维码图片")
    public String resellerCodeImage;// 分销二维码图片

    @Comment("推荐人数")
    public int downlineCount;

    @Comment("累计订单数")
    public int downlineOrderCount;

    @Comment("累计采购产品数")
    public int downlineProductCount;

    @Comment("累计采购金额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double currentTotalOrderAmount = 0; // 当前有效订单总额

    @Comment("法币佣金")
    @Column(columnDefinition = "Decimal(10,2)")
    public double currentResellerProfitMoney = 0;

    @Comment("虚拟币佣金")
    @Column(columnDefinition = "Decimal(18,8)")
    public double currentResellerProfitCoin = 0;

    @Comment("法币返佣银行账号")
    public String resellerProfitMoneyBankNo;
    @Comment("法币返佣银行开户行")
    public String resellerProfitMoneyBank;
    @Comment("法币返佣银行户名")
    public String resellerProfitMoneyBankAccount;

    @Comment("虚拟币返佣地址")
    public String resellerProfitCoinAddress;


    public User() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, User> find = new Finder(Long.class, User.class);

    @Override
    public String toString() {
        return "user [name:" + name + "]";
    }

    public boolean setReseller(String uplineResellerCode) {
        if (!StrUtil.isNull(uplineResellerCode)) {
            User uplineUser = findEarlyUserBy("resellerCode", uplineResellerCode);
            if (uplineUser == null) {
                return false;
            }
            if (uplineUser.status > 0) {// 已冻结, 已删除用户不能作为上线
                return false;
            }
            if (uplineUser.uplineUserId != id && !uplineResellerCode.equals(resellerCode)) {// 防止互相加上下线循环
                uplineUserId = uplineUser.id;
                uplineUserName = uplineUser.name;
                uplineUser.downlineCount++;
                uplineUser.save();
                becomeDownlineTime = new Date();
                return true;
            }
        }
        return false;
    }
}
