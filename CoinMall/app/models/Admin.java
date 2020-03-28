package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import play.db.ebean.Model;
import util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "admin")
@TableComment("管理员")
@OnlyAdminGet
public class Admin extends Model implements IConst {

    @Id
    public long id;

    @NotNull
    @Comment("登录名")
    @SearchField
    @Unique
    public String name;

    @NotNull
    @Comment("密码")
    public String password;

    @Comment("描述")
    public String descriptions;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("最后登录IP")
    public String lastLoginIp;// 最后一次登录的IP(只是管理员登录才会记录)

    @Comment("最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastLoginTime;

    @Comment("最后登录IP区域")
    public String lastLoginIpArea; // IP所在区域

    @Comment("状态")
    @EnumMap(value = "0,1", name = "正常,冻结")
    public int status = 0;     // 0-正常, 1-冻结

    @Comment("等级")
    @EnumMap(value = "0,1,2", name = "普通用户,管理员,超级管理员")
    public int userRoleEnum = 1; // 状态: 0普通用户, 1管理员, 2超级管理员

    @Comment("备注")
    public String comment;

    public Admin() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, Admin> find = new Finder(Long.class, Admin.class);

    @Override
    public String toString() {
        return "管理员 [name:" + name + "]";
    }
}
