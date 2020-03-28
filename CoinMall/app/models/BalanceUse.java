package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;
import util.Comment;
import util.EnumMap;
import util.TableComment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@TableComment("余额使用记录")
public class BalanceUse extends Model implements IConst {

    @Id
    public long id;

    @Comment("对应订单号")
    public String name;

    @Comment("对应用户ID")
    public long refUserId;

    @JsonIgnore
    @Comment("对应用户")
    @ManyToOne
    public User user;

    @Comment("金额")
    @Column(columnDefinition = "Decimal(18,8)")
    public double coin = 0;

    @Comment("当时法币金额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double money = 0;

    @Comment("当时汇率")
    @Column(columnDefinition = "Decimal(10,2)")
    public double rate = 0;

    @Comment("状态")
    @EnumMap(value = "0,1,2,3", name = "新建,已确认,已失效,已退还")
    public int status;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("备注")
    public String comment;

    public BalanceUse() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, BalanceUse> find = new Finder(Long.class, BalanceUse.class);

    @Override
    public String toString() {
        return "BalanceUse [No:" + name + "]";
    }
}

