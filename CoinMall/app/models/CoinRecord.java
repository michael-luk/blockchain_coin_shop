package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import controllers.biz.ConfigBiz;
import controllers.biz.OrderBiz;
import play.db.ebean.Model;
import util.Comment;
import util.SearchField;
import util.TableComment;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "coin_record")
@TableComment("币交易记录")
public class CoinRecord extends Model implements IConst {

    @Id
    public long id;

    @Comment("account")
    public String account;

    @Comment("address")
    @NotNull
    @SearchField
    public String name;

    @Comment("address")
    @Transient
    public String address;

    @Comment("category")
    public String category;

    @Comment("amount")
    @Column(columnDefinition = "Decimal(18,8)")
    public double amount = 0D;

    @Comment("fee")
    @Column(columnDefinition = "Decimal(18,8)")
    public double fee = 0D;

    @Comment("confirmations")
    public int confirmations;

    @Comment("blockhash")
    public String blockhash;

    @Comment("blockindex")
    public int blockindex;

    @Comment("blocktime")
    public long blocktime;

    @Comment("txid")
    public String txid;

    @Comment("time")
    public long time;

    @Comment("timereceived")
    public long timereceived;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt; // 创建日期

    @Column(columnDefinition = "varchar(500)")
    @Comment("comment")
    public String comment;

    @Comment("toInfo")
    @Column(columnDefinition = "varchar(500)")
    public String toInfo;

    @Comment("fromInfo")
    @Column(columnDefinition = "varchar(500)")
    public String fromInfo;

    @Comment("to")
    @Transient
    public String to;

    @Comment("from")
    @Transient
    public String from;

    @Comment("message")
    @Column(columnDefinition = "varchar(500)")
    public String message;

    public CoinRecord() {
        createdAt = new Date();
    }

    public void save() {
        // 1.存在新记录, 保存
        // 2.老记录更新确认数, 更新
        List<CoinRecord> foundList = CoinRecord.find.where()
                .eq("txid", this.txid)
                .eq("name", this.address)
                .orderBy("time desc")
                .findList();

        CoinRecord targetRecord = null;

        if (foundList.size() == 0) {
            this.name = this.address;
            this.toInfo = this.to;
            this.fromInfo = this.from;
            Ebean.save(this);
            play.Logger.info("保存新tx记录: " + this.name);
            targetRecord = this;
        } else {
            if (foundList.get(0).confirmations < ConfigBiz.getIntConfig("coin.api.confirmations")
                    && this.confirmations > foundList.get(0).confirmations) {
                foundList.get(0).confirmations = this.confirmations;
                Ebean.update(foundList.get(0));
                play.Logger.info("更新tx记录: " + foundList.get(0).name + ", 到确认数: " + this.confirmations);
                targetRecord = foundList.get(0);
            }
        }
        if (targetRecord != null && targetRecord.confirmations >= ConfigBiz.getIntConfig("coin.api.confirmations")) {
            OrderBiz.updateOrderStatusOnCoinReceived(targetRecord.name, targetRecord.txid,
                    targetRecord.time, targetRecord.amount);
        }
    }

    public static Finder<Long, CoinRecord> find = new Finder(Long.class, CoinRecord.class);

    @Override
    public String toString() {
        return "CoinRecord [name:" + name + "]";
    }
}