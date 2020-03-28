package models;

import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
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
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "theme")
@TableComment("产品规格")
public class Theme extends Model implements IConst {

    @Id
    public long id;

    @Comment("展示顺序")
    public int showIndex;

    @Comment("名称")
    @SearchField
    public String name; // 名称

    @Comment("英文名称")
    public String nameEn;

    @Comment("繁体名称")
    public String nameHk;

    @Comment("日文名称")
    public String nameJa;

    @Comment("卖出数")
    public int soldNumber; //

    @Comment("库存")
    public int inventory; //

    @Comment("状态")
    @EnumMap(value = "0,1,2", name = "正常,隐藏,删除")
    public int status = 0;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("图片")
    @Column(columnDefinition = "varchar(400)")
    public String images; // 图片(多个图片逗号分隔)

    @Comment("详情图")
    @Column(columnDefinition = "varchar(400)")
    public String smallImages;

    @Comment("英文图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesEn;

    @Comment("英文详情图")
    @Column(columnDefinition = "varchar(400)")
    public String smallImagesEn;

    @Comment("繁体图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesHk;

    @Comment("繁体详情图")
    @Column(columnDefinition = "varchar(400)")
    public String smallImagesHk;

    @Comment("日文图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesJa;

    @Comment("日文详情图")
    @Column(columnDefinition = "varchar(400)")
    public String smallImagesJa;

    @Lob
    @Comment("描述1")
    public String description;

    @Lob
    @Comment("描述2")
    public String descriptionTwo;

    @Lob
    @Comment("英文描述1")
    public String descriptionEn;

    @Lob
    @Comment("英文描述2")
    public String descriptionTwoEn;

    @Lob
    @Comment("繁体描述1")
    public String descriptionHk;

    @Lob
    @Comment("繁体描述2")
    public String descriptionTwoHk;

    @Lob
    @Comment("日文描述1")
    public String descriptionJa;

    @Lob
    @Comment("日文描述2")
    public String descriptionTwoJa;

    @Comment("备注")
    public String comment;

    @Comment("单价")
    @Column(columnDefinition = "Decimal(10,2)")
    public double price;// 单价

    @Comment("币价")
    @Column(columnDefinition = "Decimal(18,8)")
    public double coinPrice = 0D;

    @Comment("所属产品ID")
    public long refProductId; // 所属产品的ID

    @JsonIgnore
    @ManyToOne
    @Comment("所属产品")
    public Product product;// 所属的产品

    @JsonIgnore
    @Comment("对应订单")
    @ManyToMany(targetEntity = Purchase.class)
    public List<Purchase> purchases;

    @Comment("用户收藏")
    @JsonIgnore
    @ManyToMany(targetEntity = models.User.class)
    public List<User> users;

    public Theme() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, Theme> find = new Finder(Long.class, Theme.class);

    @Override
    public String toString() {
        return "Theme [name:" + name + "]";
    }
}