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
import models.ShipInfo;
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

public class ShipInfoController extends Controller implements IConst {
    
    public static Result shipInfoPage(Integer status, Integer notStatus,
                                     String fieldOn, String fieldValue, boolean isAnd,
                                     String searchOn, String kw,
                                     String startTime, String endTime,
                                     String order, String sort,
                                     Integer page, Integer size) {
        Msg<List<ShipInfo>> msg = BaseController.doGetAll("ShipInfo",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                order, sort,
                page, size);
        
        if (msg.flag) {
            return ok(ship_info.render(msg.data));
        } else {
            msg.data = new ArrayList<>();
            return ok(msg.message);
        }
    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result shipInfoBackendPage() {
        return ok(ship_info_backend.render());
    }
    

    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("新增_ShipInfo")
    @Role("create_ship_info")
    public static Result add() {
        Msg<ShipInfo> msg = new Msg<>();

        Form<ShipInfoParser> httpForm = form(ShipInfoParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            ShipInfoParser formObj = httpForm.get();            
            ShipInfo newObj = new ShipInfo();
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("ShipInfo", formObj);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("ShipInfo", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }

            newObj.isDefault = formObj.isDefault;
            newObj.name = formObj.name;
            newObj.phone = formObj.phone;
            newObj.province = formObj.province;
            newObj.city = formObj.city;
            newObj.zone = formObj.zone;
            newObj.location = formObj.location;
            newObj.comment = formObj.comment;

		    User parentUser = User.find.byId(formObj.refUserId);
		    newObj.user = parentUser;
		    newObj.refUserId = formObj.refUserId;
            Transaction txn = Ebean.beginTransaction();
            try{
                SaveBiz.beforeSave(newObj);
                Ebean.save(newObj);
                
                
                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
                sendWebSocketByChannelTag("ship_info_backend", "new");
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
    @MethodName("修改_ShipInfo")
    @Role("update_ship_info")
    public static Result update(long id) {
        Msg<ShipInfo> msg = new Msg<>();

        ShipInfo found = ShipInfo.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<ShipInfoParser> httpForm = form(ShipInfoParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            ShipInfoParser formObj = httpForm.get();        
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("ShipInfo", formObj, 1);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("ShipInfo", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }
            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = ShipInfo.find.byId(id);
                            
                found.isDefault = formObj.isDefault;
                found.name = formObj.name;
                found.phone = formObj.phone;
                found.province = formObj.province;
                found.city = formObj.city;
                found.zone = formObj.zone;
                found.location = formObj.location;
                found.comment = formObj.comment;

		        User parentUser = User.find.byId(formObj.refUserId);
		        found.refUserId = formObj.refUserId;
		        found.user = parentUser;
                SaveBiz.beforeUpdate(found);
                Ebean.update(found);
                txn.commit();
                msg.flag = true;
                msg.data = found;
                play.Logger.info("result: " + UPDATE_SUCCESS);
                sendWebSocketByChannelTag("ship_info_backend", "new");
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
    
    public static class ShipInfoParser {

        public long refUserId;
        public boolean isDefault;
        public String name;
        public String phone;
        public String province;
        public String city;
        public String zone;
        public String location;
        public String comment;

        public String validate() {

            if (User.find.byId(refUserId) == null) {
                return "无法找到上级, 请重试.";
            }
            return null;
        }
    }
    

    public static Result getNew() {
        Msg<ShipInfo> msg = new Msg<>();
        msg.flag = true;
        msg.data = new ShipInfo();
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
                
        Msg<List<ShipInfo>> msg = BaseController.doGetAll("ShipInfo",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                "createdAt", "desc",
                1, ShipInfo.find.findRowCount());
		List<ShipInfo> list = msg.data;        

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(ShipInfo.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//user_id
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//is_default
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//name
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//phone
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//province
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//city
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//zone
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//location
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//last_update_time
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//created_at
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//comment


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(ShipInfo.class) + "报表");
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
        sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "user"));//user_id
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "isDefault"));//is_default
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "name"));//name
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "phone"));//phone
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "province"));//province
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "city"));//city
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "zone"));//zone
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "location"));//location
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "lastUpdateTime"));//last_update_time
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "createdAt"));//created_at
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(ShipInfo.class, "comment"));//comment
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
        int i;
		for (i = 0; i < list.size(); i++) {
			// 取出对象
			ShipInfo item = list.get(i);
			// 创建行
			HSSFRow row = sheet2.createRow(i + 2);
			// 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            if (item.user == null) {
                cell0.setCellValue("");
            } else {
                cell0.setCellValue(item.user.name);
            }
            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(item.isDefault ? "是" : "否");
            HSSFCell cell2 = row.createCell(2);
            if (item.name == null) {
                cell2.setCellValue("");
            } else {
                cell2.setCellValue(item.name);
            }
            HSSFCell cell3 = row.createCell(3);
            if (item.phone == null) {
                cell3.setCellValue("");
            } else {
                cell3.setCellValue(item.phone);
            }
            HSSFCell cell4 = row.createCell(4);
            if (item.province == null) {
                cell4.setCellValue("");
            } else {
                cell4.setCellValue(item.province);
            }
            HSSFCell cell5 = row.createCell(5);
            if (item.city == null) {
                cell5.setCellValue("");
            } else {
                cell5.setCellValue(item.city);
            }
            HSSFCell cell6 = row.createCell(6);
            if (item.zone == null) {
                cell6.setCellValue("");
            } else {
                cell6.setCellValue(item.zone);
            }
            HSSFCell cell7 = row.createCell(7);
            if (item.location == null) {
                cell7.setCellValue("");
            } else {
                cell7.setCellValue(item.location);
            }
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue(DateUtil.Date2Str(item.lastUpdateTime));
            HSSFCell cell9 = row.createCell(9);
            cell9.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell10 = row.createCell(10);
            if (item.comment == null) {
                cell10.setCellValue("");
            } else {
                cell10.setCellValue(item.comment);
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
    @MethodName("导出报表_ShipInfo")
    @Role("report_ship_info")
	public static Result report(Integer status, Integer notStatus,
                                String fieldOn, String fieldValue, boolean isAnd,
                                String searchOn, String kw,
                                String startTime, String endTime,
                                String order, String sort) {
                                
		String fileName = TableInfoReader.getTableComment(ShipInfo.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";
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
