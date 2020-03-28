package models;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;
import util.Comment;
import util.TableComment;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cart")
@TableComment("购物车")
public class Cart extends Model implements IConst {

	@Id
	public long id;

	@Comment("所属用户ID")
	public long refUserId; //

	@Comment("数据")
	@Column(columnDefinition = "varchar(1000)")
	public String name; //

	public void save() {
		Ebean.save(this);
	}

	public static Finder<Long, Cart> find = new Finder(Long.class, Cart.class);

	@Override
	public String toString() {
		return "Cart [name:" + name + "]";
	}
}