package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import play.db.ebean.Model;
import util.Comment;
import util.EnumMap;
import util.SearchField;
import util.TableComment;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "info")
@TableComment("信息")
public class Info extends Model implements IConst {

    @Id
    public long id;

    @Comment("标题")
    @NotNull
    @SearchField
    public String name; // 名称

    @Comment("展示顺序")
    public int showIndex;

    @Comment("分类")
    public String classify;// 分类

    @Comment("英文名称")
    public String nameEn; // 英文名称

    @Comment("繁体名称")
    public String nameHk; // 英文名称

    @Comment("日文名称")
    public String nameJa; // 英文名称

    @Comment("联系电话")
    public String phone; // 联系电话

    @Comment("URL")
    public String url; // url

    @Comment("可见")
    public boolean visible = true; // test using

    @Comment("状态")
    @EnumMap(value = "0,1", name = "正常,删除")
    public int status; // test using

    @Comment("图片")
    @Column(columnDefinition = "varchar(400)")
    public String images;

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
    public Date createdAt; // 创建日期

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

    public Info() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, Info> find = new Finder(Long.class, Info.class);

    @Override
    public String toString() {
        return "Info [name:" + name + "]";
    }
}