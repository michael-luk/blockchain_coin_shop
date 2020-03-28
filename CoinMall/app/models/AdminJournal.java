package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import play.db.ebean.Model;
import util.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "admin_journal")
@TableComment("管理员日志")
@OnlyAdminGet
public class AdminJournal extends Model implements IConst {

    @Id
    public long id;

    @Comment("日志")
    @NotNull
    @SearchField
    public String name;

    @Comment("来自")
    public String actor;

    @Comment("等级")
    public String actorLevel; // 英文名称

    @Comment("时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt; // 创建日期

    public AdminJournal() {
        createdAt = new Date();
    }

    public void save() {
        Ebean.save(this);
    }

    public static Finder<Long, AdminJournal> find = new Finder(Long.class, AdminJournal.class);

    @Override
    public String toString() {
        return "管理员日志 [name:" + name + "]";
    }
}