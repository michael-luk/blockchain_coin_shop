package controllers.gen;

import controllers.*;
import controllers.biz.*;
import play.mvc.WebSocket;
import util.*;
import views.html.*;
import views.html.gen.*;
import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.PageInfo;
import LyLib.Utils.StrUtil;
import LyLib.Utils.Msg;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import java.util.ArrayList;
import models.User;
import models.Theme;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.File;
import java.io.FileOutputStream;
import play.Play;

import javax.persistence.PersistenceException;

import static controllers.Application.nameChannels;
import static controllers.Application.sendWebSocketByChannelTag;
import static play.data.Form.form;

public class UserController extends Controller implements IConst {
    
    public static Result userPage(Integer status, Integer notStatus,
                                     String fieldOn, String fieldValue, boolean isAnd,
                                     String searchOn, String kw,
                                     String startTime, String endTime,
                                     String order, String sort,
                                     Integer page, Integer size) {
        Msg<List<User>> msg = BaseController.doGetAll("User",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                order, sort,
                page, size);
        
        if (msg.flag) {
            return ok(user.render(msg.data));
        } else {
            msg.data = new ArrayList<>();
            return ok(msg.message);
        }
    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result userBackendPage() {
        return ok(user_backend.render());
    }
    
    public static Result getUserThemes(Long refId, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<Theme>> msg = new Msg<>();

        User found = User.find.byId(refId);
        if (found != null) {
            if (found.themes.size() > 0) {
                Page<Theme> records;
                records = Theme.find.where().eq("users.id", refId).orderBy("id desc").findPagingList(size)
                        .setFetchAhead(false).getPage(page - 1);

                if (records.getTotalRowCount() > 0) {
                    msg.flag = true;

                    PageInfo pageInfo = new PageInfo();
                    pageInfo.current = page;
                    pageInfo.total = records.getTotalRowCount();
                    pageInfo.size = size;
                    if (records.hasPrev())
                        pageInfo.hasPrev = true;
                    if (records.hasNext())
                        pageInfo.hasNext = true;

                    msg.data = records.getList();
                    msg.page = pageInfo;
                    play.Logger.info("result: " + msg.data.size());
                } else {
                    msg.message = NO_FOUND;
                    play.Logger.info("themes row result: " + NO_FOUND);
                }
            } else {
                msg.message = NO_FOUND;
                play.Logger.info("themes result: " + NO_FOUND);
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("user result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("新增_User")
    @Role("create_user")
    public static Result add() {
        Msg<User> msg = new Msg<>();

        Form<UserParser> httpForm = form(UserParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            UserParser formObj = httpForm.get();            
            User newObj = new User();
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("User", formObj);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("User", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }

            newObj.name = formObj.name;
            newObj.openId = formObj.openId;
            newObj.wxOpenId = formObj.wxOpenId;
            newObj.unionId = formObj.unionId;
            newObj.facebookId = formObj.facebookId;
            newObj.email = formObj.email;
            newObj.isEmailVerified = formObj.isEmailVerified;
            newObj.emailKey = formObj.emailKey;
            newObj.emailOverTime = formObj.emailOverTime;
            newObj.headImage = formObj.headImage;
            newObj.images = formObj.images;
            newObj.sexEnum = formObj.sexEnum;
            newObj.phone = formObj.phone;
            newObj.vipPoint = formObj.vipPoint;
            newObj.balance = formObj.balance;
            newObj.coinAddress = formObj.coinAddress;
            newObj.coinBalance = formObj.coinBalance;
            newObj.cardNumber = formObj.cardNumber;
            newObj.country = formObj.country;
            newObj.province = formObj.province;
            newObj.city = formObj.city;
            newObj.zone = formObj.zone;
            newObj.address = formObj.address;
            newObj.birth = formObj.birth;
            newObj.lastLoginIp = formObj.lastLoginIp;
            newObj.lastLoginTime = formObj.lastLoginTime;
            newObj.lastLoginIpArea = formObj.lastLoginIpArea;
            newObj.status = formObj.status;
            newObj.userRoleEnum = formObj.userRoleEnum;
            newObj.comment = formObj.comment;
            newObj.uplineUserName = formObj.uplineUserName;
            newObj.becomeDownlineTime = formObj.becomeDownlineTime;
            newObj.resellerCode = formObj.resellerCode;
            newObj.resellerCodeImage = formObj.resellerCodeImage;
            newObj.downlineCount = formObj.downlineCount;
            newObj.downlineOrderCount = formObj.downlineOrderCount;
            newObj.downlineProductCount = formObj.downlineProductCount;
            newObj.currentTotalOrderAmount = formObj.currentTotalOrderAmount;
            newObj.currentResellerProfitMoney = formObj.currentResellerProfitMoney;
            newObj.currentResellerProfitCoin = formObj.currentResellerProfitCoin;
            newObj.resellerProfitMoneyBankNo = formObj.resellerProfitMoneyBankNo;
            newObj.resellerProfitMoneyBank = formObj.resellerProfitMoneyBank;
            newObj.resellerProfitMoneyBankAccount = formObj.resellerProfitMoneyBankAccount;
            newObj.resellerProfitCoinAddress = formObj.resellerProfitCoinAddress;

            if (formObj.themes == null) {
                formObj.themes = new ArrayList<>();
            }
            newObj.themes = formObj.themes;
        
            Transaction txn = Ebean.beginTransaction();
            try{
                SaveBiz.beforeSave(newObj);
                Ebean.save(newObj);
                
                for (Theme jsonRefObj : formObj.themes){
                    Theme dbRefObj = Theme.find.byId(jsonRefObj.id);
                    dbRefObj.users.add(newObj);
                    dbRefObj.save();
                }
                
                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
                sendWebSocketByChannelTag("user_backend", "new");
            } catch (PersistenceException ex){
                msg.message = CREATE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
                return ok(Json.toJson(msg));
            } finally {
                txn.end();
            }
            return ok(Json.toJson(msg));
        } else {        
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
            play.Logger.error("result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("修改_User")
    @Role("update_user")
    public static Result update(long id) {
        Msg<User> msg = new Msg<>();

        User found = User.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<UserParser> httpForm = form(UserParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            UserParser formObj = httpForm.get();        
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("User", formObj, 1);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("User", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }
            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = User.find.byId(id);
                            
                found.name = formObj.name;
                found.openId = formObj.openId;
                found.wxOpenId = formObj.wxOpenId;
                found.unionId = formObj.unionId;
                found.facebookId = formObj.facebookId;
                found.email = formObj.email;
                found.isEmailVerified = formObj.isEmailVerified;
                found.emailKey = formObj.emailKey;
                found.emailOverTime = formObj.emailOverTime;
                found.headImage = formObj.headImage;
                found.images = formObj.images;
                found.sexEnum = formObj.sexEnum;
                found.phone = formObj.phone;
                found.vipPoint = formObj.vipPoint;
                found.balance = formObj.balance;
                found.coinAddress = formObj.coinAddress;
                found.coinBalance = formObj.coinBalance;
                found.cardNumber = formObj.cardNumber;
                found.country = formObj.country;
                found.province = formObj.province;
                found.city = formObj.city;
                found.zone = formObj.zone;
                found.address = formObj.address;
                found.birth = formObj.birth;
                found.lastLoginIp = formObj.lastLoginIp;
                found.lastLoginTime = formObj.lastLoginTime;
                found.lastLoginIpArea = formObj.lastLoginIpArea;
                found.status = formObj.status;
                found.userRoleEnum = formObj.userRoleEnum;
                found.comment = formObj.comment;
                found.uplineUserName = formObj.uplineUserName;
                found.becomeDownlineTime = formObj.becomeDownlineTime;
                found.resellerCode = formObj.resellerCode;
                found.resellerCodeImage = formObj.resellerCodeImage;
                found.downlineCount = formObj.downlineCount;
                found.downlineOrderCount = formObj.downlineOrderCount;
                found.downlineProductCount = formObj.downlineProductCount;
                found.currentTotalOrderAmount = formObj.currentTotalOrderAmount;
                found.currentResellerProfitMoney = formObj.currentResellerProfitMoney;
                found.currentResellerProfitCoin = formObj.currentResellerProfitCoin;
                found.resellerProfitMoneyBankNo = formObj.resellerProfitMoneyBankNo;
                found.resellerProfitMoneyBank = formObj.resellerProfitMoneyBank;
                found.resellerProfitMoneyBankAccount = formObj.resellerProfitMoneyBankAccount;
                found.resellerProfitCoinAddress = formObj.resellerProfitCoinAddress;

                // 处理多对多 user <-> Theme, 先清掉对面的
                for (Theme refObj : found.themes) {
                    if (refObj.users.contains(found)) {
                        refObj.users.remove(found);
                        refObj.save();
                    }
                }

                // 清掉自己这边的
                found.themes = new ArrayList<>();
                found.save();

                // 两边加回
                List<Theme> allRefThemes = Theme.find.all();
                if (formObj.themes != null) {
                    for (Theme jsonRefObj : formObj.themes) {
                        for (Theme dbRefObj : allRefThemes) {
                            if (dbRefObj.id == jsonRefObj.id) {
                                if (!found.themes.contains(dbRefObj)) {
                                    found.themes.add(dbRefObj);
                                }
                                if (!dbRefObj.users.contains(found)) {
                                    dbRefObj.users.add(found);
                                    dbRefObj.save();
                                }
                                break;
                            }

                        }
                    }
                }
                SaveBiz.beforeUpdate(found);
                Ebean.update(found);
                txn.commit();
                msg.flag = true;
                msg.data = found;
                play.Logger.info("result: " + UPDATE_SUCCESS);
                sendWebSocketByChannelTag("user_backend", "new");
            } catch (Exception ex){
                msg.message = UPDATE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
            } finally {
                txn.end();
            }
            return ok(Json.toJson(msg));
        } else {     
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
            play.Logger.error("result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }
    
    public static class UserParser {

        public String name;
        public String openId;
        public String wxOpenId;
        public String unionId;
        public String facebookId;
        public String email;
        public boolean isEmailVerified;
        public String emailKey;
        public Date emailOverTime;
        public String headImage;
        public String images;
        public int sexEnum;
        public String phone;
        public int vipPoint;
        public double balance;
        public String coinAddress;
        public double coinBalance;
        public String cardNumber;
        public String country;
        public String province;
        public String city;
        public String zone;
        public String address;
        public Date birth;
        public String lastLoginIp;
        public Date lastLoginTime;
        public String lastLoginIpArea;
        public int status;
        public int userRoleEnum;
        public String comment;
        public long uplineUserId;
        public String uplineUserName;
        public Date becomeDownlineTime;
        public String resellerCode;
        public String resellerCodeImage;
        public int downlineCount;
        public int downlineOrderCount;
        public int downlineProductCount;
        public double currentTotalOrderAmount;
        public double currentResellerProfitMoney;
        public double currentResellerProfitCoin;
        public String resellerProfitMoneyBankNo;
        public String resellerProfitMoneyBank;
        public String resellerProfitMoneyBankAccount;
        public String resellerProfitCoinAddress;
        public List<Theme> themes;        

        public String validate() {

            return null;
        }
    }
    
    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("删除_User")
    @Role("dalete_user")
    public static Result delete(long id) {
        Msg<User> msg = new Msg<>();

        User found = User.find.byId(id);
        if (found != null) {
            Transaction txn = Ebean.beginTransaction();
            try{
                // 解除多对多的关联
                for (Theme theme : found.themes) {
                    theme.users.remove(found);
                    theme.save();
                }
                found.themes = new ArrayList<>();
                
                found.save();
                Ebean.delete(found);
                txn.commit();
                
                msg.flag = true;
                play.Logger.info("result: " + DELETE_SUCCESS);
                sendWebSocketByChannelTag("user_backend", "delete");
            } catch (PersistenceException ex){
                msg.message = DELETE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
            } finally {
                txn.end();
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    public static Result getNew() {
        Msg<User> msg = new Msg<>();
        msg.flag = true;
        msg.data = new User();
        return ok(Json.toJson(msg));
    }
    
    public static File getReportFile(String fileName,
                                     Integer status, Integer notStatus,
                                     String fieldOn, String fieldValue, boolean isAnd,
                                     String searchOn, String kw,
                                     String startTime, String endTime,
                                     String order, String sort) {
    
		// 创建工作薄对象
		HSSFWorkbook workbook2007 = new HSSFWorkbook();
		// 数据
                
        Msg<List<User>> msg = BaseController.doGetAll("User",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                "createdAt", "desc",
                1, User.find.findRowCount());
		List<User> list = msg.data;        

        if (list == null || list.size() == 0) return null;

		// 创建单元格样式
		HSSFCellStyle cellStyle = workbook2007.createCellStyle();
		// 设置边框属性
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		// 指定单元格居中对齐
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 指定单元格垂直居中对齐
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		// 指定当单元格内容显示不下时自动换行
		cellStyle.setWrapText(true);
		// // 设置单元格字体
		HSSFFont font = workbook2007.createFont();
		font.setFontName("宋体");
		// 大小
		font.setFontHeightInPoints((short) 10);
		// 加粗
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		cellStyle.setFont(font);

		HSSFCellStyle style = workbook2007.createCellStyle();
		// 指定单元格居中对齐
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 指定单元格垂直居中对齐
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		HSSFFont font1 = workbook2007.createFont();
		font1.setFontName("宋体");
		font1.setFontHeightInPoints((short) 10);
		// 加粗
		font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font1);

		// 创建工作表对象，并命名
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(User.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//name
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//email
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//is_email_verified
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//email_key
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//email_over_time
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//sex_enum
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//phone
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//vip_point
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//balance
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//coin_address
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//coin_balance
        sheet2.setColumnWidth(11, 4000);
        sheet2.setDefaultColumnStyle(11, cellStyle);//card_number
        sheet2.setColumnWidth(12, 4000);
        sheet2.setDefaultColumnStyle(12, cellStyle);//country
        sheet2.setColumnWidth(13, 4000);
        sheet2.setDefaultColumnStyle(13, cellStyle);//province
        sheet2.setColumnWidth(14, 4000);
        sheet2.setDefaultColumnStyle(14, cellStyle);//city
        sheet2.setColumnWidth(15, 4000);
        sheet2.setDefaultColumnStyle(15, cellStyle);//zone
        sheet2.setColumnWidth(16, 4000);
        sheet2.setDefaultColumnStyle(16, cellStyle);//address
        sheet2.setColumnWidth(17, 4000);
        sheet2.setDefaultColumnStyle(17, cellStyle);//birth
        sheet2.setColumnWidth(18, 4000);
        sheet2.setDefaultColumnStyle(18, cellStyle);//last_update_time
        sheet2.setColumnWidth(19, 4000);
        sheet2.setDefaultColumnStyle(19, cellStyle);//created_at
        sheet2.setColumnWidth(20, 4000);
        sheet2.setDefaultColumnStyle(20, cellStyle);//last_login_ip
        sheet2.setColumnWidth(21, 4000);
        sheet2.setDefaultColumnStyle(21, cellStyle);//last_login_time
        sheet2.setColumnWidth(22, 4000);
        sheet2.setDefaultColumnStyle(22, cellStyle);//last_login_ip_area
        sheet2.setColumnWidth(23, 4000);
        sheet2.setDefaultColumnStyle(23, cellStyle);//status
        sheet2.setColumnWidth(24, 4000);
        sheet2.setDefaultColumnStyle(24, cellStyle);//user_role_enum
        sheet2.setColumnWidth(25, 4000);
        sheet2.setDefaultColumnStyle(25, cellStyle);//comment
        sheet2.setColumnWidth(26, 4000);
        sheet2.setDefaultColumnStyle(26, cellStyle);//upline_user_name
        sheet2.setColumnWidth(27, 4000);
        sheet2.setDefaultColumnStyle(27, cellStyle);//become_downline_time
        sheet2.setColumnWidth(28, 4000);
        sheet2.setDefaultColumnStyle(28, cellStyle);//reseller_code
        sheet2.setColumnWidth(29, 4000);
        sheet2.setDefaultColumnStyle(29, cellStyle);//downline_count
        sheet2.setColumnWidth(30, 4000);
        sheet2.setDefaultColumnStyle(30, cellStyle);//downline_order_count
        sheet2.setColumnWidth(31, 4000);
        sheet2.setDefaultColumnStyle(31, cellStyle);//downline_product_count
        sheet2.setColumnWidth(32, 4000);
        sheet2.setDefaultColumnStyle(32, cellStyle);//current_total_order_amount
        sheet2.setColumnWidth(33, 4000);
        sheet2.setDefaultColumnStyle(33, cellStyle);//current_reseller_profit_money
        sheet2.setColumnWidth(34, 4000);
        sheet2.setDefaultColumnStyle(34, cellStyle);//current_reseller_profit_coin
        sheet2.setColumnWidth(35, 4000);
        sheet2.setDefaultColumnStyle(35, cellStyle);//reseller_profit_money_bank_no
        sheet2.setColumnWidth(36, 4000);
        sheet2.setDefaultColumnStyle(36, cellStyle);//reseller_profit_money_bank
        sheet2.setColumnWidth(37, 4000);
        sheet2.setDefaultColumnStyle(37, cellStyle);//reseller_profit_money_bank_account
        sheet2.setColumnWidth(38, 4000);
        sheet2.setDefaultColumnStyle(38, cellStyle);//reseller_profit_coin_address


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(User.class) + "报表");
        title.createCell(1).setCellValue("");
        title.createCell(2).setCellValue("");
        title.createCell(3).setCellValue("");
        title.createCell(4).setCellValue("");
        title.createCell(5).setCellValue("");
        title.createCell(6).setCellValue("");
        title.createCell(7).setCellValue("");
        title.createCell(8).setCellValue("");
        title.createCell(9).setCellValue("");
        title.createCell(10).setCellValue("");
        title.createCell(11).setCellValue("");
        title.createCell(12).setCellValue("");
        title.createCell(13).setCellValue("");
        title.createCell(14).setCellValue("");
        title.createCell(15).setCellValue("");
        title.createCell(16).setCellValue("");
        title.createCell(17).setCellValue("");
        title.createCell(18).setCellValue("");
        title.createCell(19).setCellValue("");
        title.createCell(20).setCellValue("");
        title.createCell(21).setCellValue("");
        title.createCell(22).setCellValue("");
        title.createCell(23).setCellValue("");
        title.createCell(24).setCellValue("");
        title.createCell(25).setCellValue("");
        title.createCell(26).setCellValue("");
        title.createCell(27).setCellValue("");
        title.createCell(28).setCellValue("");
        title.createCell(29).setCellValue("");
        title.createCell(30).setCellValue("");
        title.createCell(31).setCellValue("");
        title.createCell(32).setCellValue("");
        title.createCell(33).setCellValue("");
        title.createCell(34).setCellValue("");
        title.createCell(35).setCellValue("");
        title.createCell(36).setCellValue("");
        title.createCell(37).setCellValue("");
        title.createCell(38).setCellValue("");
        sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 38));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(User.class, "name"));//name
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(User.class, "email"));//email
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(User.class, "isEmailVerified"));//is_email_verified
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(User.class, "emailKey"));//email_key
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(User.class, "emailOverTime"));//email_over_time
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(User.class, "sexEnum"));//sex_enum
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(User.class, "phone"));//phone
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(User.class, "vipPoint"));//vip_point
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(User.class, "balance"));//balance
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(User.class, "coinAddress"));//coin_address
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(User.class, "coinBalance"));//coin_balance
        titleRow.createCell(11).setCellValue(TableInfoReader.getFieldComment(User.class, "cardNumber"));//card_number
        titleRow.createCell(12).setCellValue(TableInfoReader.getFieldComment(User.class, "country"));//country
        titleRow.createCell(13).setCellValue(TableInfoReader.getFieldComment(User.class, "province"));//province
        titleRow.createCell(14).setCellValue(TableInfoReader.getFieldComment(User.class, "city"));//city
        titleRow.createCell(15).setCellValue(TableInfoReader.getFieldComment(User.class, "zone"));//zone
        titleRow.createCell(16).setCellValue(TableInfoReader.getFieldComment(User.class, "address"));//address
        titleRow.createCell(17).setCellValue(TableInfoReader.getFieldComment(User.class, "birth"));//birth
        titleRow.createCell(18).setCellValue(TableInfoReader.getFieldComment(User.class, "lastUpdateTime"));//last_update_time
        titleRow.createCell(19).setCellValue(TableInfoReader.getFieldComment(User.class, "createdAt"));//created_at
        titleRow.createCell(20).setCellValue(TableInfoReader.getFieldComment(User.class, "lastLoginIp"));//last_login_ip
        titleRow.createCell(21).setCellValue(TableInfoReader.getFieldComment(User.class, "lastLoginTime"));//last_login_time
        titleRow.createCell(22).setCellValue(TableInfoReader.getFieldComment(User.class, "lastLoginIpArea"));//last_login_ip_area
        titleRow.createCell(23).setCellValue(TableInfoReader.getFieldComment(User.class, "status"));//status
        titleRow.createCell(24).setCellValue(TableInfoReader.getFieldComment(User.class, "userRoleEnum"));//user_role_enum
        titleRow.createCell(25).setCellValue(TableInfoReader.getFieldComment(User.class, "comment"));//comment
        titleRow.createCell(26).setCellValue(TableInfoReader.getFieldComment(User.class, "uplineUserName"));//upline_user_name
        titleRow.createCell(27).setCellValue(TableInfoReader.getFieldComment(User.class, "becomeDownlineTime"));//become_downline_time
        titleRow.createCell(28).setCellValue(TableInfoReader.getFieldComment(User.class, "resellerCode"));//reseller_code
        titleRow.createCell(29).setCellValue(TableInfoReader.getFieldComment(User.class, "downlineCount"));//downline_count
        titleRow.createCell(30).setCellValue(TableInfoReader.getFieldComment(User.class, "downlineOrderCount"));//downline_order_count
        titleRow.createCell(31).setCellValue(TableInfoReader.getFieldComment(User.class, "downlineProductCount"));//downline_product_count
        titleRow.createCell(32).setCellValue(TableInfoReader.getFieldComment(User.class, "currentTotalOrderAmount"));//current_total_order_amount
        titleRow.createCell(33).setCellValue(TableInfoReader.getFieldComment(User.class, "currentResellerProfitMoney"));//current_reseller_profit_money
        titleRow.createCell(34).setCellValue(TableInfoReader.getFieldComment(User.class, "currentResellerProfitCoin"));//current_reseller_profit_coin
        titleRow.createCell(35).setCellValue(TableInfoReader.getFieldComment(User.class, "resellerProfitMoneyBankNo"));//reseller_profit_money_bank_no
        titleRow.createCell(36).setCellValue(TableInfoReader.getFieldComment(User.class, "resellerProfitMoneyBank"));//reseller_profit_money_bank
        titleRow.createCell(37).setCellValue(TableInfoReader.getFieldComment(User.class, "resellerProfitMoneyBankAccount"));//reseller_profit_money_bank_account
        titleRow.createCell(38).setCellValue(TableInfoReader.getFieldComment(User.class, "resellerProfitCoinAddress"));//reseller_profit_coin_address
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
        int i;
		for (i = 0; i < list.size(); i++) {
			// 取出对象
			User item = list.get(i);
			// 创建行
			HSSFRow row = sheet2.createRow(i + 2);
			// 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            if (item.name == null) {
                cell0.setCellValue("");
            } else {
                cell0.setCellValue(item.name);
            }
            HSSFCell cell1 = row.createCell(1);
            if (item.email == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.email);
            }
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(item.isEmailVerified ? "是" : "否");
            HSSFCell cell3 = row.createCell(3);
            if (item.emailKey == null) {
                cell3.setCellValue("");
            } else {
                cell3.setCellValue(item.emailKey);
            }
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(DateUtil.Date2Str(item.emailOverTime));
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(EnumInfoReader.getEnumName(User.class, "sexEnum", item.sexEnum));
            HSSFCell cell6 = row.createCell(6);
            if (item.phone == null) {
                cell6.setCellValue("");
            } else {
                cell6.setCellValue(item.phone);
            }
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue(EnumInfoReader.getEnumName(User.class, "vipPoint", item.vipPoint));
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue(item.balance);
            HSSFCell cell9 = row.createCell(9);
            if (item.coinAddress == null) {
                cell9.setCellValue("");
            } else {
                cell9.setCellValue(item.coinAddress);
            }
            HSSFCell cell10 = row.createCell(10);
            cell10.setCellValue(item.coinBalance);
            HSSFCell cell11 = row.createCell(11);
            if (item.cardNumber == null) {
                cell11.setCellValue("");
            } else {
                cell11.setCellValue(item.cardNumber);
            }
            HSSFCell cell12 = row.createCell(12);
            if (item.country == null) {
                cell12.setCellValue("");
            } else {
                cell12.setCellValue(item.country);
            }
            HSSFCell cell13 = row.createCell(13);
            if (item.province == null) {
                cell13.setCellValue("");
            } else {
                cell13.setCellValue(item.province);
            }
            HSSFCell cell14 = row.createCell(14);
            if (item.city == null) {
                cell14.setCellValue("");
            } else {
                cell14.setCellValue(item.city);
            }
            HSSFCell cell15 = row.createCell(15);
            if (item.zone == null) {
                cell15.setCellValue("");
            } else {
                cell15.setCellValue(item.zone);
            }
            HSSFCell cell16 = row.createCell(16);
            if (item.address == null) {
                cell16.setCellValue("");
            } else {
                cell16.setCellValue(item.address);
            }
            HSSFCell cell17 = row.createCell(17);
            cell17.setCellValue(DateUtil.Date2Str(item.birth));
            HSSFCell cell18 = row.createCell(18);
            cell18.setCellValue(DateUtil.Date2Str(item.lastUpdateTime));
            HSSFCell cell19 = row.createCell(19);
            cell19.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell20 = row.createCell(20);
            if (item.lastLoginIp == null) {
                cell20.setCellValue("");
            } else {
                cell20.setCellValue(item.lastLoginIp);
            }
            HSSFCell cell21 = row.createCell(21);
            cell21.setCellValue(DateUtil.Date2Str(item.lastLoginTime));
            HSSFCell cell22 = row.createCell(22);
            if (item.lastLoginIpArea == null) {
                cell22.setCellValue("");
            } else {
                cell22.setCellValue(item.lastLoginIpArea);
            }
            HSSFCell cell23 = row.createCell(23);
            cell23.setCellValue(EnumInfoReader.getEnumName(User.class, "status", item.status));
            HSSFCell cell24 = row.createCell(24);
            cell24.setCellValue(EnumInfoReader.getEnumName(User.class, "userRoleEnum", item.userRoleEnum));
            HSSFCell cell25 = row.createCell(25);
            if (item.comment == null) {
                cell25.setCellValue("");
            } else {
                cell25.setCellValue(item.comment);
            }
            HSSFCell cell26 = row.createCell(26);
            if (item.uplineUserName == null) {
                cell26.setCellValue("");
            } else {
                cell26.setCellValue(item.uplineUserName);
            }
            HSSFCell cell27 = row.createCell(27);
            cell27.setCellValue(DateUtil.Date2Str(item.becomeDownlineTime));
            HSSFCell cell28 = row.createCell(28);
            if (item.resellerCode == null) {
                cell28.setCellValue("");
            } else {
                cell28.setCellValue(item.resellerCode);
            }
            HSSFCell cell29 = row.createCell(29);
            cell29.setCellValue(EnumInfoReader.getEnumName(User.class, "downlineCount", item.downlineCount));
            HSSFCell cell30 = row.createCell(30);
            cell30.setCellValue(EnumInfoReader.getEnumName(User.class, "downlineOrderCount", item.downlineOrderCount));
            HSSFCell cell31 = row.createCell(31);
            cell31.setCellValue(EnumInfoReader.getEnumName(User.class, "downlineProductCount", item.downlineProductCount));
            HSSFCell cell32 = row.createCell(32);
            cell32.setCellValue(item.currentTotalOrderAmount);
            HSSFCell cell33 = row.createCell(33);
            cell33.setCellValue(item.currentResellerProfitMoney);
            HSSFCell cell34 = row.createCell(34);
            cell34.setCellValue(item.currentResellerProfitCoin);
            HSSFCell cell35 = row.createCell(35);
            if (item.resellerProfitMoneyBankNo == null) {
                cell35.setCellValue("");
            } else {
                cell35.setCellValue(item.resellerProfitMoneyBankNo);
            }
            HSSFCell cell36 = row.createCell(36);
            if (item.resellerProfitMoneyBank == null) {
                cell36.setCellValue("");
            } else {
                cell36.setCellValue(item.resellerProfitMoneyBank);
            }
            HSSFCell cell37 = row.createCell(37);
            if (item.resellerProfitMoneyBankAccount == null) {
                cell37.setCellValue("");
            } else {
                cell37.setCellValue(item.resellerProfitMoneyBankAccount);
            }
            HSSFCell cell38 = row.createCell(38);
            if (item.resellerProfitCoinAddress == null) {
                cell38.setCellValue("");
            } else {
                cell38.setCellValue(item.resellerProfitCoinAddress);
            }
		}

		// 生成文件
		String path = Play.application().path().getPath() + "/public/report/" + fileName;
		File file = new File(path);
        
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			workbook2007.write(fos);
		} catch (Exception e) {
            play.Logger.error("生成报表出错: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
                    play.Logger.error("生成报表出错, 关闭流出错: " + e.getMessage());
				}
			}
		}
        return file;
    }

    @Security.Authenticated(SecuredAdmin.class)
    @MethodName("导出报表_User")
    @Role("report_user")
	public static Result report(Integer status, Integer notStatus,
                                String fieldOn, String fieldValue, boolean isAnd,
                                String searchOn, String kw,
                                String startTime, String endTime,
                                String order, String sort) {
                                
		String fileName = TableInfoReader.getTableComment(User.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";
        File file = getReportFile(fileName, status, notStatus, fieldOn, fieldValue, isAnd, searchOn, kw,
                startTime, endTime, order, sort);

        if (file == null) {
            if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)) {
                return ok("日期: " + startTime + " 至 " + endTime + ", 报表" + NO_FOUND + ", 请返回重试!");
            }
            return ok(NO_FOUND);
        }
        
        // 处理中文报表名
        String agent = request().getHeader("USER-AGENT");
        String downLoadName = null;
        try {
            if (null != agent && -1 != agent.indexOf("MSIE"))   //IE
            {
                downLoadName = java.net.URLEncoder.encode(fileName, "UTF-8");
            } else if (null != agent && -1 != agent.indexOf("Mozilla")) //Firefox
            {
                downLoadName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            } else {
                downLoadName = java.net.URLEncoder.encode(fileName, "UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            play.Logger.error("导出报表处理中文报表名出错: " + ex.getMessage());
        }
        if (downLoadName != null) {
            response().setHeader("Content-disposition", "attachment;filename="
                    + downLoadName);
            response().setContentType("application/vnd.ms-excel;charset=UTF-8");
        }
        
		return ok(file);
	}
}
