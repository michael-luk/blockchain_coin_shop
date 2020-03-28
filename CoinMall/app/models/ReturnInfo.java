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
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "return_info")
@TableComment("退换货")
public class ReturnInfo extends Model implements IConst {

    @Id
    public long id;

    @SearchField
    @Comment("退换单号")
    public String name;

    @Comment("联系人")
    public String contact;

    @Comment("联系电话")
    public String phone;

    @Comment("申请用户ID")
    public long refUserId;// 用户ID

    @JsonIgnore
    @Comment("申请用户")
    @ManyToOne
    public User user; // 购买用户

    @Comment("所属订单ID")
    public long refPurchaseId;// 用户ID

    @JsonIgnore
    @Comment("所属订单")
    @ManyToOne
    public Purchase purchase; // 购买用户

    @Comment("状态")
    @util.EnumMap(value = "0,1,2,3,4", name = "新申请,已批准,已拒绝,已换货,已退货退款")
    public int status;

    @Comment("分类")
    @EnumMap(value = "0,1", name = "换货,退货")
    public int returnType;

    @Comment("快递单号")
    public String shipNo;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("备注")
    public String comment;

    public ReturnInfo() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, ReturnInfo> find = new Finder(Long.class, ReturnInfo.class);

    @Override
    public String toString() {
        return "ReturnInfo [No:" + name + "]";
    }
}

