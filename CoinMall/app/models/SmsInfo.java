package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import play.db.ebean.Model;
import util.Comment;
import util.SearchField;
import util.TableComment;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sms_info")
@TableComment("短信验证码")
public class SmsInfo extends Model implements IConst {

    @Id
    public long id;

    @Comment("标题")
    public String name;

    @Comment("手机号")
    @SearchField
    public String phone;

    @Comment("短信验证码")
    public String checkCode;

    @Lob
    @Comment("发送xml")
    public String sendXml;

    @Comment("返回表")
    public String returnTable;

    @Lob
    @Comment("返回xml")
    public String receiveXml;

    @Comment("code")
    public String code;

    @Comment("返回信息")
    public String returnMsg;

    @Comment("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date lastUpdateTime;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt; // 创建日期

    @Comment("备注")
    public String comment;

    public SmsInfo() {
        createdAt = new Date();
    }

    public void save() {
        lastUpdateTime = new Date();
        Ebean.save(this);
    }

    public static Finder<Long, SmsInfo> find = new Finder(Long.class, SmsInfo.class);

    @Override
    public String toString() {
        return "SmsInfo [name:" + name + "]";
    }
}