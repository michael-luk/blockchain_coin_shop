package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import play.db.ebean.Model;
import util.Comment;
import util.SearchField;
import util.TableComment;

@Entity
@Table(name = "ship_info")
@TableComment("收货地址")
public class ShipInfo extends Model implements IConst {

	@Id
	public long id;

	@Comment("所属用户ID")
	public long refUserId; //

	@JsonIgnore
	@ManyToOne
	@Comment("所属用户")
	public User user;// 所属的用户

	@Comment("是否默认")
	public boolean isDefault = false;//

	@SearchField
	@Comment("收货人")
	public String name; //

	@Comment("联系电话")
	public String phone; //

	@Comment("省")
	public String province; //

	@Comment("市")
	public String city; //

	@Comment("区")
	public String zone; //

	@Comment("地址")
	public String location; // 详细地址

	@Comment("修改时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public Date lastUpdateTime;

	@Comment("创建日期")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public Date createdAt;

	@Comment("备注")
	public String comment;

	public ShipInfo() {
		createdAt = new Date();
	}

	public void save() {
		lastUpdateTime = new Date();
		Ebean.save(this);
	}

	public static Finder<Long, ShipInfo> find = new Finder(Long.class, ShipInfo.class);

	@Override
	public String toString() {
		return "ShipInfo [name:" + name + "]";
	}
}