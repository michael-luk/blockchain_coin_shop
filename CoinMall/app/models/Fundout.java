package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;
import util.Comment;
import util.EnumMap;
import util.SearchField;
import util.TableComment;

import javax.persistence.*;
import java.util.Date;

@Entity
@TableComment("佣金提现")
public class Fundout extends Model implements IConst {

    @Id
    public long id;

    @Comment("提现单号")
    public String name;

    @Comment("申请用户ID")
    public long refUserId;

    @JsonIgnore
    @Comment("申请用户")
    @ManyToOne
    public User user;

    @Comment("法币佣金")
    @Column(columnDefinition = "Decimal(10,2)")
    public double money = 0;

    @Comment("虚拟币佣金")
    @Column(columnDefinition = "Decimal(18,8)")
    public double coin = 0;

    @Comment("法币返佣银行账号")
    public String resellerProfitMoneyBankNo;
    @Comment("法币返佣银行开户行")
    public String resellerProfitMoneyBank;
    @Comment("法币返佣银行户名")
    public String resellerProfitMoneyBankAccount;

    @Comment("虚拟币返佣地址")
    public String resellerProfitCoinAddress;

    @Comment("状态")
    @EnumMap(value = "0,1,2", name = "新申请,已放款,已拒绝")
    public int status;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("备注")
    public String comment;

    public Fundout() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, Fundout> find = new Finder(Long.class, Fundout.class);

    @Override
    public String toString() {
        return "ReturnInfo [No:" + name + "]";
    }
}

