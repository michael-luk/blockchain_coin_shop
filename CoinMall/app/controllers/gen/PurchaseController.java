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
import models.Purchase;
import models.Theme;
import models.User;
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

public class PurchaseController extends Controller implements IConst {
    
    public static Result purchasePage(Integer status, Integer notStatus,
                                     String fieldOn, String fieldValue, boolean isAnd,
                                     String searchOn, String kw,
                                     String startTime, String endTime,
                                     String order, String sort,
                                     Integer page, Integer size) {
        Msg<List<Purchase>> msg = BaseController.doGetAll("Purchase",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                order, sort,
                page, size);
        
        if (msg.flag) {
            return ok(purchase.render(msg.data));
        } else {
            msg.data = new ArrayList<>();
            return ok(msg.message);
        }
    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result purchaseBackendPage() {
        return ok(purchase_backend.render());
    }
    
    public static Result getPurchaseThemes(Long refId, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<Theme>> msg = new Msg<>();

        Purchase found = Purchase.find.byId(refId);
        if (found != null) {
            if (found.themes.size() > 0) {
                Page<Theme> records;
                records = Theme.find.where().eq("purchases.id", refId).orderBy("id desc").findPagingList(size)
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
            play.Logger.info("purchase result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("新增_Purchase")
    @Role("create_purchase")
    public static Result add() {
        Msg<Purchase> msg = new Msg<>();

        Form<PurchaseParser> httpForm = form(PurchaseParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            PurchaseParser formObj = httpForm.get();            
            Purchase newObj = new Purchase();
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("Purchase", formObj);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("Purchase", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }

            newObj.name = formObj.name;
            newObj.status = formObj.status;
            newObj.quantity = formObj.quantity;
            newObj.pids = formObj.pids;
            newObj.tids = formObj.tids;
            newObj.amount = formObj.amount;
            newObj.coinAmount = formObj.coinAmount;
            newObj.coinPayAddr = formObj.coinPayAddr;
            newObj.images = formObj.images;
            newObj.coinPayTrans = formObj.coinPayTrans;
            newObj.useVipPoint = formObj.useVipPoint;
            newObj.vipPointDiscount = formObj.vipPointDiscount;
            newObj.useBalance = formObj.useBalance;
            newObj.balanceDiscount = formObj.balanceDiscount;
            newObj.shipName = formObj.shipName;
            newObj.shipPhone = formObj.shipPhone;
            newObj.shipProvince = formObj.shipProvince;
            newObj.shipCity = formObj.shipCity;
            newObj.shipZone = formObj.shipZone;
            newObj.shipLocation = formObj.shipLocation;
            newObj.buyerMessage = formObj.buyerMessage;
            newObj.shipTime = formObj.shipTime;
            newObj.shipNo = formObj.shipNo;
            newObj.payReturnCode = formObj.payReturnCode;
            newObj.payReturnMsg = formObj.payReturnMsg;
            newObj.payResultCode = formObj.payResultCode;
            newObj.payAmount = formObj.payAmount;
            newObj.payTime = formObj.payTime;
            newObj.payBank = formObj.payBank;
            newObj.payRefOrderNo = formObj.payRefOrderNo;
            newObj.paySign = formObj.paySign;
            newObj.description1 = formObj.description1;
            newObj.description2 = formObj.description2;
            newObj.comment = formObj.comment;

		    User parentUser = User.find.byId(formObj.refUserId);
		    newObj.user = parentUser;
		    newObj.refUserId = formObj.refUserId;
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
                    dbRefObj.purchases.add(newObj);
                    dbRefObj.save();
                }
                
                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
                sendWebSocketByChannelTag("purchase_backend", "new");
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
    @MethodName("修改_Purchase")
    @Role("update_purchase")
    public static Result update(long id) {
        Msg<Purchase> msg = new Msg<>();

        Purchase found = Purchase.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<PurchaseParser> httpForm = form(PurchaseParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            PurchaseParser formObj = httpForm.get();        
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("Purchase", formObj, 1);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("Purchase", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }
            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = Purchase.find.byId(id);
                            
                found.name = formObj.name;
                found.status = formObj.status;
                found.quantity = formObj.quantity;
                found.pids = formObj.pids;
                found.tids = formObj.tids;
                found.amount = formObj.amount;
                found.coinAmount = formObj.coinAmount;
                found.coinPayAddr = formObj.coinPayAddr;
                found.images = formObj.images;
                found.coinPayTrans = formObj.coinPayTrans;
                found.useVipPoint = formObj.useVipPoint;
                found.vipPointDiscount = formObj.vipPointDiscount;
                found.useBalance = formObj.useBalance;
                found.balanceDiscount = formObj.balanceDiscount;
                found.shipName = formObj.shipName;
                found.shipPhone = formObj.shipPhone;
                found.shipProvince = formObj.shipProvince;
                found.shipCity = formObj.shipCity;
                found.shipZone = formObj.shipZone;
                found.shipLocation = formObj.shipLocation;
                found.buyerMessage = formObj.buyerMessage;
                found.shipTime = formObj.shipTime;
                found.shipNo = formObj.shipNo;
                found.payReturnCode = formObj.payReturnCode;
                found.payReturnMsg = formObj.payReturnMsg;
                found.payResultCode = formObj.payResultCode;
                found.payAmount = formObj.payAmount;
                found.payTime = formObj.payTime;
                found.payBank = formObj.payBank;
                found.payRefOrderNo = formObj.payRefOrderNo;
                found.paySign = formObj.paySign;
                found.description1 = formObj.description1;
                found.description2 = formObj.description2;
                found.comment = formObj.comment;

		        User parentUser = User.find.byId(formObj.refUserId);
		        found.refUserId = formObj.refUserId;
		        found.user = parentUser;
                // 处理多对多 purchase <-> Theme, 先清掉对面的
                for (Theme refObj : found.themes) {
                    if (refObj.purchases.contains(found)) {
                        refObj.purchases.remove(found);
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
                                if (!dbRefObj.purchases.contains(found)) {
                                    dbRefObj.purchases.add(found);
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
                sendWebSocketByChannelTag("purchase_backend", "new");
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
    
    public static class PurchaseParser {

        public String name;
        public long refUserId;
        public int status;
        public String quantity;
        public String pids;
        public String tids;
        public double amount;
        public double coinAmount;
        public String coinPayAddr;
        public String images;
        public String coinPayTrans;
        public int useVipPoint;
        public double vipPointDiscount;
        public double useBalance;
        public double balanceDiscount;
        public String shipName;
        public String shipPhone;
        public String shipProvince;
        public String shipCity;
        public String shipZone;
        public String shipLocation;
        public String buyerMessage;
        public Date shipTime;
        public String shipNo;
        public String payReturnCode;
        public String payReturnMsg;
        public String payResultCode;
        public String payTransitionId;
        public double payAmount;
        public Date payTime;
        public String payBank;
        public String payRefOrderNo;
        public String paySign;
        public String description1;
        public String description2;
        public String comment;
        public List<Theme> themes;        

        public String validate() {

            if (User.find.byId(refUserId) == null) {
                return "无法找到上级, 请重试.";
            }
            return null;
        }
    }
    
    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("删除_Purchase")
    @Role("dalete_purchase")
    public static Result delete(long id) {
        Msg<Purchase> msg = new Msg<>();

        Purchase found = Purchase.find.byId(id);
        if (found != null) {
            Transaction txn = Ebean.beginTransaction();
            try{
                // 解除多对多的关联
                for (Theme theme : found.themes) {
                    theme.purchases.remove(found);
                    theme.save();
                }
                found.themes = new ArrayList<>();
                
                found.save();
                Ebean.delete(found);
                txn.commit();
                
                msg.flag = true;
                play.Logger.info("result: " + DELETE_SUCCESS);
                sendWebSocketByChannelTag("purchase_backend", "delete");
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
        Msg<Purchase> msg = new Msg<>();
        msg.flag = true;
        msg.data = new Purchase();
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
                
        Msg<List<Purchase>> msg = BaseController.doGetAll("Purchase",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                "createdAt", "desc",
                1, Purchase.find.findRowCount());
		List<Purchase> list = msg.data;        

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(Purchase.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//name
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//user_id
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//status
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//last_update_time
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//created_at
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//quantity
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//pids
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//tids
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//amount
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//coin_amount
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//coin_pay_addr
        sheet2.setColumnWidth(11, 4000);
        sheet2.setDefaultColumnStyle(11, cellStyle);//coin_pay_trans
        sheet2.setColumnWidth(12, 4000);
        sheet2.setDefaultColumnStyle(12, cellStyle);//use_vip_point
        sheet2.setColumnWidth(13, 4000);
        sheet2.setDefaultColumnStyle(13, cellStyle);//vip_point_discount
        sheet2.setColumnWidth(14, 4000);
        sheet2.setDefaultColumnStyle(14, cellStyle);//use_balance
        sheet2.setColumnWidth(15, 4000);
        sheet2.setDefaultColumnStyle(15, cellStyle);//balance_discount
        sheet2.setColumnWidth(16, 4000);
        sheet2.setDefaultColumnStyle(16, cellStyle);//ship_name
        sheet2.setColumnWidth(17, 4000);
        sheet2.setDefaultColumnStyle(17, cellStyle);//ship_phone
        sheet2.setColumnWidth(18, 4000);
        sheet2.setDefaultColumnStyle(18, cellStyle);//ship_province
        sheet2.setColumnWidth(19, 4000);
        sheet2.setDefaultColumnStyle(19, cellStyle);//ship_city
        sheet2.setColumnWidth(20, 4000);
        sheet2.setDefaultColumnStyle(20, cellStyle);//ship_zone
        sheet2.setColumnWidth(21, 4000);
        sheet2.setDefaultColumnStyle(21, cellStyle);//ship_location
        sheet2.setColumnWidth(22, 4000);
        sheet2.setDefaultColumnStyle(22, cellStyle);//buyer_message
        sheet2.setColumnWidth(23, 4000);
        sheet2.setDefaultColumnStyle(23, cellStyle);//ship_time
        sheet2.setColumnWidth(24, 4000);
        sheet2.setDefaultColumnStyle(24, cellStyle);//ship_no
        sheet2.setColumnWidth(25, 4000);
        sheet2.setDefaultColumnStyle(25, cellStyle);//pay_return_code
        sheet2.setColumnWidth(26, 4000);
        sheet2.setDefaultColumnStyle(26, cellStyle);//pay_return_msg
        sheet2.setColumnWidth(27, 4000);
        sheet2.setDefaultColumnStyle(27, cellStyle);//pay_result_code
        sheet2.setColumnWidth(28, 4000);
        sheet2.setDefaultColumnStyle(28, cellStyle);//pay_amount
        sheet2.setColumnWidth(29, 4000);
        sheet2.setDefaultColumnStyle(29, cellStyle);//pay_time
        sheet2.setColumnWidth(30, 4000);
        sheet2.setDefaultColumnStyle(30, cellStyle);//pay_bank
        sheet2.setColumnWidth(31, 4000);
        sheet2.setDefaultColumnStyle(31, cellStyle);//pay_ref_order_no
        sheet2.setColumnWidth(32, 4000);
        sheet2.setDefaultColumnStyle(32, cellStyle);//pay_sign
        sheet2.setColumnWidth(33, 4000);
        sheet2.setDefaultColumnStyle(33, cellStyle);//description1
        sheet2.setColumnWidth(34, 4000);
        sheet2.setDefaultColumnStyle(34, cellStyle);//description2
        sheet2.setColumnWidth(35, 4000);
        sheet2.setDefaultColumnStyle(35, cellStyle);//comment


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(Purchase.class) + "报表");
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
        sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 35));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "name"));//name
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "user"));//user_id
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "status"));//status
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "lastUpdateTime"));//last_update_time
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "createdAt"));//created_at
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "quantity"));//quantity
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "pids"));//pids
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "tids"));//tids
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "amount"));//amount
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "coinAmount"));//coin_amount
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "coinPayAddr"));//coin_pay_addr
        titleRow.createCell(11).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "coinPayTrans"));//coin_pay_trans
        titleRow.createCell(12).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "useVipPoint"));//use_vip_point
        titleRow.createCell(13).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "vipPointDiscount"));//vip_point_discount
        titleRow.createCell(14).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "useBalance"));//use_balance
        titleRow.createCell(15).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "balanceDiscount"));//balance_discount
        titleRow.createCell(16).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipName"));//ship_name
        titleRow.createCell(17).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipPhone"));//ship_phone
        titleRow.createCell(18).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipProvince"));//ship_province
        titleRow.createCell(19).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipCity"));//ship_city
        titleRow.createCell(20).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipZone"));//ship_zone
        titleRow.createCell(21).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipLocation"));//ship_location
        titleRow.createCell(22).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "buyerMessage"));//buyer_message
        titleRow.createCell(23).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipTime"));//ship_time
        titleRow.createCell(24).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "shipNo"));//ship_no
        titleRow.createCell(25).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "payReturnCode"));//pay_return_code
        titleRow.createCell(26).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "payReturnMsg"));//pay_return_msg
        titleRow.createCell(27).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "payResultCode"));//pay_result_code
        titleRow.createCell(28).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "payAmount"));//pay_amount
        titleRow.createCell(29).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "payTime"));//pay_time
        titleRow.createCell(30).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "payBank"));//pay_bank
        titleRow.createCell(31).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "payRefOrderNo"));//pay_ref_order_no
        titleRow.createCell(32).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "paySign"));//pay_sign
        titleRow.createCell(33).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "description1"));//description1
        titleRow.createCell(34).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "description2"));//description2
        titleRow.createCell(35).setCellValue(TableInfoReader.getFieldComment(Purchase.class, "comment"));//comment
		titleRow.createCell(36).setCellValue("产品编号");//comment
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
        int i;
		for (i = 0; i < list.size(); i++) {
			// 取出对象
			Purchase item = list.get(i);
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
            if (item.user == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.user.name);
            }
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(EnumInfoReader.getEnumName(Purchase.class, "status", item.status));
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(DateUtil.Date2Str(item.lastUpdateTime));
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell5 = row.createCell(5);
            if (item.quantity == null) {
                cell5.setCellValue("");
            } else {
                cell5.setCellValue(item.quantity);
            }
            HSSFCell cell6 = row.createCell(6);
            if (item.pids == null) {
                cell6.setCellValue("");
            } else {
                cell6.setCellValue(item.pids);
            }
            HSSFCell cell7 = row.createCell(7);
            if (item.tids == null) {
                cell7.setCellValue("");
            } else {
                cell7.setCellValue(item.tids);
            }
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue(item.amount);
            HSSFCell cell9 = row.createCell(9);
            cell9.setCellValue(item.coinAmount);
            HSSFCell cell10 = row.createCell(10);
            if (item.coinPayAddr == null) {
                cell10.setCellValue("");
            } else {
                cell10.setCellValue(item.coinPayAddr);
            }
            HSSFCell cell11 = row.createCell(11);
            if (item.coinPayTrans == null) {
                cell11.setCellValue("");
            } else {
                cell11.setCellValue(item.coinPayTrans);
            }
            HSSFCell cell12 = row.createCell(12);
            cell12.setCellValue(EnumInfoReader.getEnumName(Purchase.class, "useVipPoint", item.useVipPoint));
            HSSFCell cell13 = row.createCell(13);
            cell13.setCellValue(item.vipPointDiscount);
            HSSFCell cell14 = row.createCell(14);
            cell14.setCellValue(item.useBalance);
            HSSFCell cell15 = row.createCell(15);
            cell15.setCellValue(item.balanceDiscount);
            HSSFCell cell16 = row.createCell(16);
            if (item.shipName == null) {
                cell16.setCellValue("");
            } else {
                cell16.setCellValue(item.shipName);
            }
            HSSFCell cell17 = row.createCell(17);
            if (item.shipPhone == null) {
                cell17.setCellValue("");
            } else {
                cell17.setCellValue(item.shipPhone);
            }
            HSSFCell cell18 = row.createCell(18);
            if (item.shipProvince == null) {
                cell18.setCellValue("");
            } else {
                cell18.setCellValue(item.shipProvince);
            }
            HSSFCell cell19 = row.createCell(19);
            if (item.shipCity == null) {
                cell19.setCellValue("");
            } else {
                cell19.setCellValue(item.shipCity);
            }
            HSSFCell cell20 = row.createCell(20);
            if (item.shipZone == null) {
                cell20.setCellValue("");
            } else {
                cell20.setCellValue(item.shipZone);
            }
            HSSFCell cell21 = row.createCell(21);
            if (item.shipLocation == null) {
                cell21.setCellValue("");
            } else {
                cell21.setCellValue(item.shipLocation);
            }
            HSSFCell cell22 = row.createCell(22);
            if (item.buyerMessage == null) {
                cell22.setCellValue("");
            } else {
                cell22.setCellValue(item.buyerMessage);
            }
            HSSFCell cell23 = row.createCell(23);
            cell23.setCellValue(DateUtil.Date2Str(item.shipTime));
            HSSFCell cell24 = row.createCell(24);
            if (item.shipNo == null) {
                cell24.setCellValue("");
            } else {
                cell24.setCellValue(item.shipNo);
            }
            HSSFCell cell25 = row.createCell(25);
            if (item.payReturnCode == null) {
                cell25.setCellValue("");
            } else {
                cell25.setCellValue(item.payReturnCode);
            }
            HSSFCell cell26 = row.createCell(26);
            if (item.payReturnMsg == null) {
                cell26.setCellValue("");
            } else {
                cell26.setCellValue(item.payReturnMsg);
            }
            HSSFCell cell27 = row.createCell(27);
            if (item.payResultCode == null) {
                cell27.setCellValue("");
            } else {
                cell27.setCellValue(item.payResultCode);
            }
            HSSFCell cell28 = row.createCell(28);
            cell28.setCellValue(item.payAmount);
            HSSFCell cell29 = row.createCell(29);
            cell29.setCellValue(DateUtil.Date2Str(item.payTime));
            HSSFCell cell30 = row.createCell(30);
            if (item.payBank == null) {
                cell30.setCellValue("");
            } else {
                cell30.setCellValue(item.payBank);
            }
            HSSFCell cell31 = row.createCell(31);
            if (item.payRefOrderNo == null) {
                cell31.setCellValue("");
            } else {
                cell31.setCellValue(item.payRefOrderNo);
            }
            HSSFCell cell32 = row.createCell(32);
            if (item.paySign == null) {
                cell32.setCellValue("");
            } else {
                cell32.setCellValue(item.paySign);
            }
            HSSFCell cell33 = row.createCell(33);
            if (item.description1 == null) {
                cell33.setCellValue("");
            } else {
                cell33.setCellValue(item.description1);
            }
            HSSFCell cell34 = row.createCell(34);
            if (item.description2 == null) {
                cell34.setCellValue("");
            } else {
                cell34.setCellValue(item.description2);
            }
            HSSFCell cell35 = row.createCell(35);
            if (item.comment == null) {
                cell35.setCellValue("");
            } else {
                cell35.setCellValue(item.comment);
            }

            HSSFCell cell36 = row.createCell(36);
            List<Theme> themes = item.themes;
            if (themes.isEmpty()) {
                cell36.setCellValue("");
            } else {
                StringBuilder str = new StringBuilder(200);
                for (Theme theme : themes) {
                    if (themes.size() == 1) {
                        cell36.setCellValue(theme.product.showNo);
                    } else {
                        str.append(theme.product.showNo + ",");
                        String showNos = str.toString();
                        showNos = showNos.substring(0, str.length() - 1);
                        cell36.setCellValue(showNos);
					}
                }
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
    @MethodName("导出报表_Purchase")
    @Role("report_purchase")
	public static Result report(Integer status, Integer notStatus,
                                String fieldOn, String fieldValue, boolean isAnd,
                                String searchOn, String kw,
                                String startTime, String endTime,
                                String order, String sort) {
                                
		String fileName = TableInfoReader.getTableComment(Purchase.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";
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
