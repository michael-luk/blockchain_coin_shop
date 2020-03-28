package models;

import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import LyLib.Interfaces.IConst;
import play.db.ebean.Model;
import util.Comment;
import util.SearchField;
import util.TableComment;

@Entity
@Table(name = "catalog")
@TableComment("分类")
public class Catalog extends Model implements IConst {

    @Id
    public long id;

    @Comment("展示顺序")
    public int showIndex;

    @Comment("名称")
    @SearchField
    public String name;

    @Comment("英文名称")
    public String nameEn;

    @Comment("繁体名称")
    public String nameHk;

    @Comment("日文名称")
    public String nameJa;

    @Comment("图片")
    @Column(columnDefinition = "varchar(400)")
    public String images; // 图片(多个图片逗号分隔)

    @Comment("小图片")
    @Column(columnDefinition = "varchar(400)")
    public String smallImages;

    @Comment("英文图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesEn;

    @Comment("英文小图片")
    @Column(columnDefinition = "varchar(400)")
    public String smallImagesEn;

    @Comment("繁体图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesHk;

    @Comment("繁体小图片")
    @Column(columnDefinition = "varchar(400)")
    public String smallImagesHk;

    @Comment("日文图片")
    @Column(columnDefinition = "varchar(400)")
    public String imagesJa;

    @Comment("日文小图片")
    @Column(columnDefinition = "varchar(400)")
    public String smallImagesJa;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("描述1")
    public String description;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("描述2")
    public String descriptionTwo;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("英文描述1")
    public String descriptionEn;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("英文描述2")
    public String descriptionTwoEn;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("繁体描述1")
    public String descriptionHk;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("繁体描述2")
    public String descriptionTwoHk;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("日文描述1")
    public String descriptionJa;

    @Column(columnDefinition = "varchar(2000)")
    @Comment("日文描述2")
    public String descriptionTwoJa;

    @Comment("备注")
    public String comment;

    @JsonIgnore
    @Comment("分类下产品")
    @ManyToMany(targetEntity = Product.class)
    public List<Product> products; //

    public Catalog() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, Catalog> find = new Finder(Long.class, Catalog.class);

    @Override
    public String toString() {
        return "Catalog [name:" + name + "]";
    }
}