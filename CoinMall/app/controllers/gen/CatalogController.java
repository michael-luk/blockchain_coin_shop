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
import models.Catalog;
import models.Product;
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

public class CatalogController extends Controller implements IConst {
    
    public static Result catalogPage(Integer status, Integer notStatus,
                                     String fieldOn, String fieldValue, boolean isAnd,
                                     String searchOn, String kw,
                                     String startTime, String endTime,
                                     String order, String sort,
                                     Integer page, Integer size) {
        Msg<List<Catalog>> msg = BaseController.doGetAll("Catalog",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                order, sort,
                page, size);
        
        if (msg.flag) {
            return ok(catalog.render(msg.data));
        } else {
            msg.data = new ArrayList<>();
            return ok(msg.message);
        }
    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result catalogBackendPage() {
        return ok(catalog_backend.render());
    }
    
    public static Result getCatalogProducts(Long refId, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<Product>> msg = new Msg<>();

        Catalog found = Catalog.find.byId(refId);
        if (found != null) {
            if (found.products.size() > 0) {
                Page<Product> records;
                records = Product.find.where().eq("catalogs.id", refId).orderBy("id desc").findPagingList(size)
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
                    play.Logger.info("products row result: " + NO_FOUND);
                }
            } else {
                msg.message = NO_FOUND;
                play.Logger.info("products result: " + NO_FOUND);
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("catalog result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("新增_Catalog")
    @Role("create_catalog")
    public static Result add() {
        Msg<Catalog> msg = new Msg<>();

        Form<CatalogParser> httpForm = form(CatalogParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            CatalogParser formObj = httpForm.get();            
            Catalog newObj = new Catalog();
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("Catalog", formObj);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("Catalog", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }

            newObj.showIndex = formObj.showIndex;
            newObj.name = formObj.name;
            newObj.nameEn = formObj.nameEn;
            newObj.nameHk = formObj.nameHk;
            newObj.nameJa = formObj.nameJa;
            newObj.images = formObj.images;
            newObj.smallImages = formObj.smallImages;
            newObj.imagesEn = formObj.imagesEn;
            newObj.smallImagesEn = formObj.smallImagesEn;
            newObj.imagesHk = formObj.imagesHk;
            newObj.smallImagesHk = formObj.smallImagesHk;
            newObj.imagesJa = formObj.imagesJa;
            newObj.smallImagesJa = formObj.smallImagesJa;
            newObj.description = formObj.description;
            newObj.descriptionTwo = formObj.descriptionTwo;
            newObj.descriptionEn = formObj.descriptionEn;
            newObj.descriptionTwoEn = formObj.descriptionTwoEn;
            newObj.descriptionHk = formObj.descriptionHk;
            newObj.descriptionTwoHk = formObj.descriptionTwoHk;
            newObj.descriptionJa = formObj.descriptionJa;
            newObj.descriptionTwoJa = formObj.descriptionTwoJa;
            newObj.comment = formObj.comment;

            if (formObj.products == null) {
                formObj.products = new ArrayList<>();
            }
            newObj.products = formObj.products;
        
            Transaction txn = Ebean.beginTransaction();
            try{
                SaveBiz.beforeSave(newObj);
                Ebean.save(newObj);
                
                for (Product jsonRefObj : formObj.products){
                    Product dbRefObj = Product.find.byId(jsonRefObj.id);
                    dbRefObj.catalogs.add(newObj);
                    dbRefObj.save();
                }
                
                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
                sendWebSocketByChannelTag("catalog_backend", "new");
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
    @MethodName("修改_Catalog")
    @Role("update_catalog")
    public static Result update(long id) {
        Msg<Catalog> msg = new Msg<>();

        Catalog found = Catalog.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<CatalogParser> httpForm = form(CatalogParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            CatalogParser formObj = httpForm.get();        
            
            String uniqueFieldIssue = BaseController.checkFieldUnique("Catalog", formObj, 1);
            if (StrUtil.isNotNull(uniqueFieldIssue)) {
                msg.message = "字段[" + TableInfoReader.getFieldComment("Catalog", uniqueFieldIssue)
                        + "]存在同名数据";
                return ok(Json.toJson(msg));
            }
            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = Catalog.find.byId(id);
                            
                found.showIndex = formObj.showIndex;
                found.name = formObj.name;
                found.nameEn = formObj.nameEn;
                found.nameHk = formObj.nameHk;
                found.nameJa = formObj.nameJa;
                found.images = formObj.images;
                found.smallImages = formObj.smallImages;
                found.imagesEn = formObj.imagesEn;
                found.smallImagesEn = formObj.smallImagesEn;
                found.imagesHk = formObj.imagesHk;
                found.smallImagesHk = formObj.smallImagesHk;
                found.imagesJa = formObj.imagesJa;
                found.smallImagesJa = formObj.smallImagesJa;
                found.description = formObj.description;
                found.descriptionTwo = formObj.descriptionTwo;
                found.descriptionEn = formObj.descriptionEn;
                found.descriptionTwoEn = formObj.descriptionTwoEn;
                found.descriptionHk = formObj.descriptionHk;
                found.descriptionTwoHk = formObj.descriptionTwoHk;
                found.descriptionJa = formObj.descriptionJa;
                found.descriptionTwoJa = formObj.descriptionTwoJa;
                found.comment = formObj.comment;

                // 处理多对多 catalog <-> Product, 先清掉对面的
                for (Product refObj : found.products) {
                    if (refObj.catalogs.contains(found)) {
                        refObj.catalogs.remove(found);
                        refObj.save();
                    }
                }

                // 清掉自己这边的
                found.products = new ArrayList<>();
                found.save();

                // 两边加回
                List<Product> allRefProducts = Product.find.all();
                if (formObj.products != null) {
                    for (Product jsonRefObj : formObj.products) {
                        for (Product dbRefObj : allRefProducts) {
                            if (dbRefObj.id == jsonRefObj.id) {
                                if (!found.products.contains(dbRefObj)) {
                                    found.products.add(dbRefObj);
                                }
                                if (!dbRefObj.catalogs.contains(found)) {
                                    dbRefObj.catalogs.add(found);
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
                sendWebSocketByChannelTag("catalog_backend", "new");
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
    
    public static class CatalogParser {

        public int showIndex;
        public String name;
        public String nameEn;
        public String nameHk;
        public String nameJa;
        public String images;
        public String smallImages;
        public String imagesEn;
        public String smallImagesEn;
        public String imagesHk;
        public String smallImagesHk;
        public String imagesJa;
        public String smallImagesJa;
        public String description;
        public String descriptionTwo;
        public String descriptionEn;
        public String descriptionTwoEn;
        public String descriptionHk;
        public String descriptionTwoHk;
        public String descriptionJa;
        public String descriptionTwoJa;
        public String comment;
        public List<Product> products;        

        public String validate() {
            return null;
        }
    }
    
    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("删除_Catalog")
    @Role("dalete_catalog")
    public static Result delete(long id) {
        Msg<Catalog> msg = new Msg<>();

        Catalog found = Catalog.find.byId(id);
        if (found != null) {
            Transaction txn = Ebean.beginTransaction();
            try{
                // 解除多对多的关联
                for (Product product : found.products) {
                    product.catalogs.remove(found);
                    product.save();
                }
                found.products = new ArrayList<>();
                
                found.save();
                Ebean.delete(found);
                txn.commit();
                
                msg.flag = true;
                play.Logger.info("result: " + DELETE_SUCCESS);
                sendWebSocketByChannelTag("catalog_backend", "delete");
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
        Msg<Catalog> msg = new Msg<>();
        msg.flag = true;
        msg.data = new Catalog();
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
                
        Msg<List<Catalog>> msg = BaseController.doGetAll("Catalog",
                status, notStatus,
                fieldOn, fieldValue, isAnd,
                searchOn, kw,
                startTime, endTime,
                "createdAt", "desc",
                1, Catalog.find.findRowCount());
		List<Catalog> list = msg.data;        

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(Catalog.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//show_index
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//name
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//name_en
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//name_hk
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//name_ja
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//last_update_time
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//created_at
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//description
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//description_two
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//description_en
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//description_two_en
        sheet2.setColumnWidth(11, 4000);
        sheet2.setDefaultColumnStyle(11, cellStyle);//description_hk
        sheet2.setColumnWidth(12, 4000);
        sheet2.setDefaultColumnStyle(12, cellStyle);//description_two_hk
        sheet2.setColumnWidth(13, 4000);
        sheet2.setDefaultColumnStyle(13, cellStyle);//description_ja
        sheet2.setColumnWidth(14, 4000);
        sheet2.setDefaultColumnStyle(14, cellStyle);//description_two_ja
        sheet2.setColumnWidth(15, 4000);
        sheet2.setDefaultColumnStyle(15, cellStyle);//comment


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(Catalog.class) + "报表");
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
        sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 15));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "showIndex"));//show_index
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "name"));//name
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "nameEn"));//name_en
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "nameHk"));//name_hk
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "nameJa"));//name_ja
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "lastUpdateTime"));//last_update_time
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "createdAt"));//created_at
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "description"));//description
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionTwo"));//description_two
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionEn"));//description_en
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionTwoEn"));//description_two_en
        titleRow.createCell(11).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionHk"));//description_hk
        titleRow.createCell(12).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionTwoHk"));//description_two_hk
        titleRow.createCell(13).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionJa"));//description_ja
        titleRow.createCell(14).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionTwoJa"));//description_two_ja
        titleRow.createCell(15).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "comment"));//comment
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
        int i;
		for (i = 0; i < list.size(); i++) {
			// 取出对象
			Catalog item = list.get(i);
			// 创建行
			HSSFRow row = sheet2.createRow(i + 2);
			// 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            cell0.setCellValue(EnumInfoReader.getEnumName(Catalog.class, "showIndex", item.showIndex));
            HSSFCell cell1 = row.createCell(1);
            if (item.name == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.name);
            }
            HSSFCell cell2 = row.createCell(2);
            if (item.nameEn == null) {
                cell2.setCellValue("");
            } else {
                cell2.setCellValue(item.nameEn);
            }
            HSSFCell cell3 = row.createCell(3);
            if (item.nameHk == null) {
                cell3.setCellValue("");
            } else {
                cell3.setCellValue(item.nameHk);
            }
            HSSFCell cell4 = row.createCell(4);
            if (item.nameJa == null) {
                cell4.setCellValue("");
            } else {
                cell4.setCellValue(item.nameJa);
            }
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(DateUtil.Date2Str(item.lastUpdateTime));
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell7 = row.createCell(7);
            if (item.description == null) {
                cell7.setCellValue("");
            } else {
                cell7.setCellValue(item.description);
            }
            HSSFCell cell8 = row.createCell(8);
            if (item.descriptionTwo == null) {
                cell8.setCellValue("");
            } else {
                cell8.setCellValue(item.descriptionTwo);
            }
            HSSFCell cell9 = row.createCell(9);
            if (item.descriptionEn == null) {
                cell9.setCellValue("");
            } else {
                cell9.setCellValue(item.descriptionEn);
            }
            HSSFCell cell10 = row.createCell(10);
            if (item.descriptionTwoEn == null) {
                cell10.setCellValue("");
            } else {
                cell10.setCellValue(item.descriptionTwoEn);
            }
            HSSFCell cell11 = row.createCell(11);
            if (item.descriptionHk == null) {
                cell11.setCellValue("");
            } else {
                cell11.setCellValue(item.descriptionHk);
            }
            HSSFCell cell12 = row.createCell(12);
            if (item.descriptionTwoHk == null) {
                cell12.setCellValue("");
            } else {
                cell12.setCellValue(item.descriptionTwoHk);
            }
            HSSFCell cell13 = row.createCell(13);
            if (item.descriptionJa == null) {
                cell13.setCellValue("");
            } else {
                cell13.setCellValue(item.descriptionJa);
            }
            HSSFCell cell14 = row.createCell(14);
            if (item.descriptionTwoJa == null) {
                cell14.setCellValue("");
            } else {
                cell14.setCellValue(item.descriptionTwoJa);
            }
            HSSFCell cell15 = row.createCell(15);
            if (item.comment == null) {
                cell15.setCellValue("");
            } else {
                cell15.setCellValue(item.comment);
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
    @MethodName("导出报表_Catalog")
    @Role("report_catalog")
	public static Result report(Integer status, Integer notStatus,
                                String fieldOn, String fieldValue, boolean isAnd,
                                String searchOn, String kw,
                                String startTime, String endTime,
                                String order, String sort) {
                                
		String fileName = TableInfoReader.getTableComment(Catalog.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";
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
