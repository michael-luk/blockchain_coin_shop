package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.Msg;
import LyLib.Utils.PageInfo;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import controllers.biz.ConfigBiz;
import play.db.ebean.Model;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import util.*;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static controllers.Application.sendWebSocketByChannelTag;

public class BaseController extends Controller implements IConst {

    public static F.Promise<Result> getAll(String modelName, Integer status, Integer notStatus,
                                           String fieldOn, String fieldValue, boolean isAnd,
                                           String searchOn, String kw,
                                           String startTime, String endTime,
                                           String order, String sort,
                                           Integer page, Integer size) {

        return F.Promise.promise(() -> ok(Json.toJson(
                doGetAll(modelName, status, notStatus,
                        fieldOn, fieldValue, isAnd,
                        searchOn, kw,
                        startTime, endTime,
                        order, sort,
                        page, size))));
    }

    public static Msg doGetAll(String modelName, Integer status, Integer notStatus,
                               String fieldOn, String fieldValue, boolean isAnd,
                               String searchOn, String kw,
                               String startTime, String endTime,
                               String order, String sort,
                               Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE; //默认size
        if (page <= 0)
            page = 1;
        if (StrUtil.isNull(order))
            order = "id desc";
        else {
            if (!StrUtil.isNull(sort))
                order += " " + sort;
        }

        Msg msg = new Msg();
        List<?> pageNum = new ArrayList<>();
        if ("UserModel".equals(modelName)) {
            msg.message = "无法查询";
            return msg;
        }

        if (status != -100 && notStatus != -100) {
            msg.message = "status/notStatus参数不正确";
            play.Logger.error(msg.message);
            return msg;
        }

        Class<?> t = getClass("models." + modelName);
        if (t == null) {
            msg.message = "无法获取类型";
            play.Logger.error(msg.message);
            return msg;
        }

        if (TableInfoReader.getTableOnlyAdminGet(t)) {
            if (!"2".equals(session().get(SESSION_USER_ROLE)) && !"1".equals(session().get(SESSION_USER_ROLE))) {
                msg.message = "您没有权限获取数据";
                return msg;
            }
        }

        Page<?> records;
        Query<?> query = Ebean.find(t);

        if (StrUtil.isNotNull(searchOn) && StrUtil.isNotNull(kw)) {
            if (searchOn.contains(",")) {
                String[] searchFields = searchOn.split(","); // a,b, -> [a,b]
                String[] kws = kw.split(",");

                if (searchFields.length == kws.length) {
                    for (int i = 0; i < searchFields.length; i++) {
                        query.where().like(searchFields[i], "%" + kws[i] + "%");
                    }
                }
            } else {
                query.where().like(searchOn, "%" + kw + "%");
            }
        }

        if (StrUtil.isNotNull(fieldOn) && StrUtil.isNotNull(fieldValue)) {
            if (fieldOn.contains(",")) {
                String[] fields = fieldOn.split(","); // a,b, -> [a,b]
                String[] values = fieldValue.split(",");
                if (fields.length == values.length) {
                    for (int i = 0; i < fields.length; i++) {
                        query.where().eq(fields[i], getCorrectValue4Query(t, fields[i], values[i]));
                    }
                }
            } else {
                query.where().eq(fieldOn, getCorrectValue4Query(t, fieldOn, fieldValue));
            }
        }
        if (status > -100) {
            query.where().eq("status", status);
        } else {
            if (notStatus > -100) query.where().ne("status", notStatus);
        }
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)) {
            query.where().between("createdAt", startTime, endTime);
        }
        records = query.orderBy(order).findPagingList(size).setFetchAhead(false).getPage(page - 1);

        if (records.getTotalRowCount() > 0) {
            msg.flag = true;
            PageInfo pageInfo = new PageInfo();
            pageInfo.current = page;
            pageInfo.total = records.getTotalRowCount();
//            pageInfo.total = records.getTotalPageCount();
//            pageInfo.desc = records.getDisplayXtoYofZ("-", "/");
//            pageInfo.desc = String.valueOf(records.getTotalRowCount());
            pageInfo.size = size;
            if (records.hasPrev())
                pageInfo.hasPrev = true;
            if (records.hasNext())
                pageInfo.hasNext = true;

            msg.data = records.getList();
            msg.page = pageInfo;
            play.Logger.info("result: " + records.getTotalRowCount());
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
        }
        return msg;
    }

    private static Object getCorrectValue4Query(Class<?> t, String fieldName, String fieldValueString) {
        String fieldType = "";
        try {
            fieldType = t.getDeclaredField(fieldName).getGenericType().getTypeName();
        } catch (NoSuchFieldException e) {
            if (fieldName.contains(".id")) {
                fieldType = "java.lang.Long";
            }
        }
        if (StrUtil.isNull(fieldType)) return fieldValueString;

        Object finalValue = fieldValueString;
        if ("java.lang.Long".equals(fieldType)) finalValue = Long.parseLong(fieldValueString);
        else if ("java.lang.Integer".equals(fieldType)) finalValue = Integer.parseInt(fieldValueString);
        else if ("boolean".equals(fieldType)) finalValue = Boolean.parseBoolean(fieldValueString);
        else if ("java.lang.Double".equals(fieldType)) finalValue = Double.parseDouble(fieldValueString);

        return finalValue;
    }

    public static F.Promise<Result> getOne(String modelName, Long id) {
        return F.Promise.promise(() -> ok(Json.toJson(doGetOne(modelName, id))));
    }

    public static Msg doGetOne(String modelName, Long id) {
        Msg msg = new Msg();
        if ("UserModel".equals(modelName)) {
            msg.message = "无法查询";
            return msg;
        }

        Class<?> t = getClass("models." + modelName);
        if (t == null) {
            msg.message = "无法获取类型";
            play.Logger.error(msg.message);
            return msg;
        }

        if (TableInfoReader.getTableOnlyAdminGet(t)) {
            if (!"2".equals(session().get(SESSION_USER_ROLE)) && !"1".equals(session().get(SESSION_USER_ROLE))) {
                msg.message = "您没有权限获取数据";
                return msg;
            }
        }

        Object found = Ebean.find(t, id);
        if (found == null) {
            msg.flag = false;
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
        } else {
            msg.flag = true;
            msg.data = found;
        }
        return msg;
    }

    // 仅处理无关联对象, 一对多对象的删除. 多对多对象删除功能在各自Controller
    @Security.Authenticated(SecuredSuperAdmin.class)
    @MethodName("删除_BASE")
    public static F.Promise<Result> delete(String modelName, Long id) {
        Msg msg = new Msg<>();
        if ("AdminJournal".equals(modelName)) {
            msg.message = TableInfoReader.getTableComment(modelName) + "不能删除";
            return F.Promise.promise(() -> ok(Json.toJson(msg)));
        }
        Class<?> t = getClass("models." + modelName);
        if (t == null) {
            msg.message = "无法获取类型";
            play.Logger.error(msg.message);
            return F.Promise.promise(() -> ok(Json.toJson(msg)));
        }

        Object found = Ebean.find(t, id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.error("result: " + msg.message);
        } else {
            try {
                Ebean.delete(t, id);
                msg.flag = true;
                msg.message = DELETE_SUCCESS;
                play.Logger.info(DELETE_SUCCESS + ": [" + id + "]");
                sendWebSocketByChannelTag(StrUtil.getSnakeFromCamel(modelName) + "_backend", "delete");
            } catch (PersistenceException ex) {
                play.Logger.error(DELETE_ISSUE + ": [" + id + "]" + ex.getMessage());
            }
        }
        return F.Promise.promise(() -> ok(Json.toJson(msg)));
    }

    public static Class<?> getClass(String modelName) {
        Class<?> t;
        try {
            t = Class.forName(modelName);
        } catch (ClassNotFoundException e) {
            play.Logger.error("无法通过反射获取类型: " + e.getMessage());
            return null;
        }

        if (t == null) {
            play.Logger.error("无法通过反射获取类型,类型为null");
            return null;
        }
        return t;
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        Field[] fields = obj.getClass().getDeclaredFields();
        if (fields == null) return null;
        if (fields.length == 0) return null;

        for (Field field : fields) {
            if (field != null && fieldName.equals(field.getName())) {
                try {
                    return field.get(obj);
                } catch (IllegalAccessException ex) {
                    play.Logger.error("无法通过反射获取字段值: " + obj.getClass().getName() + " - " + fieldName
                            + ", ex: " + ex.getMessage());
                }
            }
        }
        return null;
    }

    public static String checkFieldUnique(String className, Object obj, int uniqueCount) {
        Model.Finder finder = null;
        try {
            finder = new Model.Finder(Long.class, Class.forName("models." + className));
        } catch (ClassNotFoundException e) {
            play.Logger.error("查找同名数据时, 模型无法使用: " + className);
            return null;
        }

        String uniqueFieldStr = TableInfoReader.getUniqueFields(className);
        if (uniqueFieldStr.length() > 0) {
            String[] uniqueFields = uniqueFieldStr.split(",");
            if (uniqueFields.length > 0) {
                for (String uniqueField : uniqueFields) {
                    Object fieldValue = BaseController.getFieldValue(obj, uniqueField);
                    if (fieldValue == null) continue;

                    if (finder.where().eq(uniqueField, fieldValue).findRowCount() > uniqueCount) {
                        play.Logger.error("字段" + TableInfoReader.getTableComment(className) + "."
                                + uniqueField + "存在同名数据");
                        return uniqueField;
                    }
                }
            }
        }
        return "";
    }

    public static String checkFieldUnique(String className, Object obj) {
        return checkFieldUnique(className, obj, 0);
    }
}
