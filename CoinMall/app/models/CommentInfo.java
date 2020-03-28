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

@Entity
@Table(name = "comment_info")
@TableComment("商品评论")
public class CommentInfo extends Model implements IConst {

    @Id
    public long id;

    @SearchField
    @Comment("评论内容")
    @Column(columnDefinition = "varchar(2000)")
    public String name;

    @Comment("申请用户ID")
    public long refUserId;// 用户ID

    @Comment("申请用户")
    @ManyToOne
    @JsonIgnore
    public User user; // 购买用户

    @Comment("所属商品ID")
    public long refProductId;

    @JsonIgnore
    @Comment("所属商品")
    @ManyToOne
    public Product product;

    @Comment("所属订单ID")
    public long refPurchaseId;

    @JsonIgnore
    @Comment("所属订单")
    @ManyToOne
    public Purchase purchase;

    @Comment("状态")
    @EnumMap(value = "0,1,2", name = "正常,已隐藏,已删除")
    public int status;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("备注")
    public String comment;

    public CommentInfo() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, CommentInfo> find = new Finder(Long.class, CommentInfo.class);

    @Override
    public String toString() {
        return "CommentInfo [No:" + name + "]";
    }
}

