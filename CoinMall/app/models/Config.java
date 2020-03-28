package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import play.db.ebean.Model;
import util.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "config")
@TableComment("设置项")
public class Config extends Model implements IConst {

    @Id
    public long id;

    @NotNull
    @Comment("名称")
    @SearchField
    @Unique
    public String name;

    @NotNull
    @Comment("值")
    public String content = "";

    @NotNull
    @Comment("类型")
    @EnumMap(value = "0,1,2,3", name = "文本,整数,小数,是否")
    public int typeEnum = 0;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("备注")
    public String comment;

    public Config() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, Config> find = new Finder(Long.class, Config.class);

    @Override
    public String toString() {
        return String.format("设置项: %s, type: %d, name: %s \n\n",
                name, typeEnum, content);
    }
}