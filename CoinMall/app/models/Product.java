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
@Table(name = "product")
@TableComment("产品")
public class Product extends Model implements IConst {

    @Id
    public long id;

    @Comment("产品编号")
    public String showNo; //

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
    public int soldNumber;

    @Comment("单位")
    public String unit; //

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("图片")
    @Column(columnDefinition = "varchar(400)")
    public String images; // 图片(多个图片逗号分隔)

    @Comment("英文图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesEn;

    @Comment("繁体图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesHk;

    @Comment("日文图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesJa;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("描述")
    public String description;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("英文描述")
    public String descriptionEn;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("繁体描述")
    public String descriptionHk;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("日文描述")
    public String descriptionJa;

    @Comment("备注")
    public String comment;

    @Comment("价格")
    @Column(columnDefinition = "Decimal(10,2)")
    public double price = 0D; //

    @Comment("原价")
    @Column(columnDefinition = "Decimal(10,2)")
    public double originalPrice = 0D; //

    @Comment("币价")
    @Column(columnDefinition = "Decimal(18,8)")
    public double coinPrice = 0D; //

    @Comment("是否特价商品")
    public boolean isSale = false; //

    @Comment("状态")
    @EnumMap(value = "0,1,2", name = "正常,隐藏,删除")
    public int status = 0; //  0正常, 1隐藏, 2删除

    @Comment("所属分类")
    @ManyToMany(targetEntity = Catalog.class)
    public List<Catalog> catalogs; //

    @Comment("规格")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    public List<Theme> themes; //

    @Comment("商品评论")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    public List<CommentInfo> commentInfos;

    public Product() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public int getActualSoldOutNum() {
        int result = 0;
        for (Theme theme : themes) {
            if (theme.status == 0) {
                result += theme.soldNumber;
            }
        }
        return result;
    }

    public static Finder<Long, Product> find = new Finder(Long.class, Product.class);

    public String toString() {
        return "Product [name:" + name + "]";
    }
}
