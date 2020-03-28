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
import models.CoinRecord;
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

public class CoinRecordController extends Controller implements IConst {
    
    public static Result coinRecordPage(Integer status, Integer notStatus,
                                     String fieldOn, String fieldValue, boolean isAnd,
                                     String searchOn, String kw,
                                     String startTime, String endTime,
                                     String order, String sort,
                                     Integer page, Integer size) {
        Msg<List<CoinRecord>> msg = BaseController.doGetAll("CoinRecord",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                order, sort,
                page, size);
        
        if (msg.flag) {
            return ok(coin_record.render(msg.data));
        } else {
            msg.data = new ArrayList<>();
            return ok(msg.message);
        }
    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result coinRecordBackendPage() {
        return ok(coin_record_backend.render());
    }
    

    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("新增_CoinRecord")
    @Role("create_coin_record")
    public static Result add() {
        Msg<CoinRecord> msg = new Msg<>();

        Form<CoinRecordParser> httpForm = form(CoinRecordParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            CoinRecordParser formObj = httpForm.get();            
            CoinRecord newObj = new CoinRecord();
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("CoinRecord", formObj);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("CoinRecord", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }

            newObj.account = formObj.account;
            newObj.name = formObj.name;
            newObj.category = formObj.category;
            newObj.amount = formObj.amount;
            newObj.fee = formObj.fee;
            newObj.confirmations = formObj.confirmations;
            newObj.blockhash = formObj.blockhash;
            newObj.blockindex = formObj.blockindex;
            newObj.blocktime = formObj.blocktime;
            newObj.txid = formObj.txid;
            newObj.time = formObj.time;
            newObj.timereceived = formObj.timereceived;
            newObj.comment = formObj.comment;
            newObj.toInfo = formObj.toInfo;
            newObj.fromInfo = formObj.fromInfo;
            newObj.message = formObj.message;

            Transaction txn = Ebean.beginTransaction();
            try{
                SaveBiz.beforeSave(newObj);
                Ebean.save(newObj);
                
                
                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
                sendWebSocketByChannelTag("coin_record_backend", "new");
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
    @MethodName("修改_CoinRecord")
    @Role("update_coin_record")
    public static Result update(long id) {
        Msg<CoinRecord> msg = new Msg<>();

        CoinRecord found = CoinRecord.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<CoinRecordParser> httpForm = form(CoinRecordParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            CoinRecordParser formObj = httpForm.get();        
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("CoinRecord", formObj, 1);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("CoinRecord", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }
            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = CoinRecord.find.byId(id);
                            
                found.account = formObj.account;
                found.name = formObj.name;
                found.category = formObj.category;
                found.amount = formObj.amount;
                found.fee = formObj.fee;
                found.confirmations = formObj.confirmations;
                found.blockhash = formObj.blockhash;
                found.blockindex = formObj.blockindex;
                found.blocktime = formObj.blocktime;
                found.txid = formObj.txid;
                found.time = formObj.time;
                found.timereceived = formObj.timereceived;
                found.comment = formObj.comment;
                found.toInfo = formObj.toInfo;
                found.fromInfo = formObj.fromInfo;
                found.message = formObj.message;

                SaveBiz.beforeUpdate(found);
                Ebean.update(found);
                txn.commit();
                msg.flag = true;
                msg.data = found;
                play.Logger.info("result: " + UPDATE_SUCCESS);
                sendWebSocketByChannelTag("coin_record_backend", "new");
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
    
    public static class CoinRecordParser {

        public String account;
        public String name;
        public String category;
        public double amount;
        public double fee;
        public int confirmations;
        public String blockhash;
        public int blockindex;
        public long blocktime;
        public String txid;
        public long time;
        public long timereceived;
        public String comment;
        public String toInfo;
        public String fromInfo;
        public String message;

        public String validate() {
            if (StrUtil.isNull(name)) {
                return TableInfoReader.getFieldComment(CoinRecord.class, "name") + "不能为空";
            }

            return null;
        }
    }
    

    public static Result getNew() {
        Msg<CoinRecord> msg = new Msg<>();
        msg.flag = true;
        msg.data = new CoinRecord();
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
                
        Msg<List<CoinRecord>> msg = BaseController.doGetAll("CoinRecord",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                "createdAt", "desc",
                1, CoinRecord.find.findRowCount());
		List<CoinRecord> list = msg.data;        

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(CoinRecord.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//account
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//name
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//category
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//amount
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//fee
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//confirmations
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//blockhash
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//blockindex
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//blocktime
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//txid
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//time
        sheet2.setColumnWidth(11, 4000);
        sheet2.setDefaultColumnStyle(11, cellStyle);//timereceived
        sheet2.setColumnWidth(12, 4000);
        sheet2.setDefaultColumnStyle(12, cellStyle);//created_at
        sheet2.setColumnWidth(13, 4000);
        sheet2.setDefaultColumnStyle(13, cellStyle);//comment
        sheet2.setColumnWidth(14, 4000);
        sheet2.setDefaultColumnStyle(14, cellStyle);//to_info
        sheet2.setColumnWidth(15, 4000);
        sheet2.setDefaultColumnStyle(15, cellStyle);//from_info
        sheet2.setColumnWidth(16, 4000);
        sheet2.setDefaultColumnStyle(16, cellStyle);//message


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(CoinRecord.class) + "报表");
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
        sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 16));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "account"));//account
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "name"));//name
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "category"));//category
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "amount"));//amount
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "fee"));//fee
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "confirmations"));//confirmations
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "blockhash"));//blockhash
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "blockindex"));//blockindex
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "blocktime"));//blocktime
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "txid"));//txid
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "time"));//time
        titleRow.createCell(11).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "timereceived"));//timereceived
        titleRow.createCell(12).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "createdAt"));//created_at
        titleRow.createCell(13).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "comment"));//comment
        titleRow.createCell(14).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "toInfo"));//to_info
        titleRow.createCell(15).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "fromInfo"));//from_info
        titleRow.createCell(16).setCellValue(TableInfoReader.getFieldComment(CoinRecord.class, "message"));//message
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
        int i;
		for (i = 0; i < list.size(); i++) {
			// 取出对象
			CoinRecord item = list.get(i);
			// 创建行
			HSSFRow row = sheet2.createRow(i + 2);
			// 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            if (item.account == null) {
                cell0.setCellValue("");
            } else {
                cell0.setCellValue(item.account);
            }
            HSSFCell cell1 = row.createCell(1);
            if (item.name == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.name);
            }
            HSSFCell cell2 = row.createCell(2);
            if (item.category == null) {
                cell2.setCellValue("");
            } else {
                cell2.setCellValue(item.category);
            }
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(item.amount);
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(item.fee);
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(EnumInfoReader.getEnumName(CoinRecord.class, "confirmations", item.confirmations));
            HSSFCell cell6 = row.createCell(6);
            if (item.blockhash == null) {
                cell6.setCellValue("");
            } else {
                cell6.setCellValue(item.blockhash);
            }
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue(EnumInfoReader.getEnumName(CoinRecord.class, "blockindex", item.blockindex));
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue(item.blocktime);
            HSSFCell cell9 = row.createCell(9);
            if (item.txid == null) {
                cell9.setCellValue("");
            } else {
                cell9.setCellValue(item.txid);
            }
            HSSFCell cell10 = row.createCell(10);
            cell10.setCellValue(item.time);
            HSSFCell cell11 = row.createCell(11);
            cell11.setCellValue(item.timereceived);
            HSSFCell cell12 = row.createCell(12);
            cell12.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell13 = row.createCell(13);
            if (item.comment == null) {
                cell13.setCellValue("");
            } else {
                cell13.setCellValue(item.comment);
            }
            HSSFCell cell14 = row.createCell(14);
            if (item.toInfo == null) {
                cell14.setCellValue("");
            } else {
                cell14.setCellValue(item.toInfo);
            }
            HSSFCell cell15 = row.createCell(15);
            if (item.fromInfo == null) {
                cell15.setCellValue("");
            } else {
                cell15.setCellValue(item.fromInfo);
            }
            HSSFCell cell16 = row.createCell(16);
            if (item.message == null) {
                cell16.setCellValue("");
            } else {
                cell16.setCellValue(item.message);
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
    @MethodName("导出报表_CoinRecord")
    @Role("report_coin_record")
	public static Result report(Integer status, Integer notStatus,
                                String fieldOn, String fieldValue, boolean isAnd,
                                String searchOn, String kw,
                                String startTime, String endTime,
                                String order, String sort) {
                                
		String fileName = TableInfoReader.getTableComment(CoinRecord.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";
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
