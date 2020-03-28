package controllers;

import LyLib.Utils.StrUtil;
import util.*;

import java.lang.reflect.Field;

public class TableInfoReader {

    public static String getFieldComment(Class<?> clazz, String fieldName) {
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return fieldName;
        }

        if (field != null && field.isAnnotationPresent(Comment.class)) {
            Comment annotation = field.getAnnotation(Comment.class);
            if (StrUtil.isNotNull(annotation.value())) return annotation.value();
        }
        return fieldName;
    }

    public static String getFieldComment(String className, String fieldName) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz == null) return fieldName;
        return getFieldComment(clazz, fieldName);
    }

    public static String getTableComment(Class<?> clazz) {
        if (clazz.isAnnotationPresent(TableComment.class)) {
            TableComment annotation = clazz.getAnnotation(TableComment.class);
            if (StrUtil.isNotNull(annotation.value())) return annotation.value();
        }
        return clazz.getName();
    }

    public static String getTableComment(String className) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz != null) return getTableComment(clazz);
        return className;
    }

    public static boolean getTableOnlyAdminGet(Class<?> clazz) {
        return clazz.isAnnotationPresent(OnlyAdminGet.class);
    }

    public static boolean getTableOnlyAdminGet(String className) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz != null) return getTableOnlyAdminGet(clazz);
        return false;
    }

    public static String getSearchField(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null) return "";
        if (fields.length == 0) return "";

        for (Field field : fields) {
            if (field != null && field.isAnnotationPresent(SearchField.class)) {
                return field.getName();
            }
        }

        // default search field is name, and finally set to id
        try {
            clazz.getDeclaredField("name");
            return "name";
        } catch (NoSuchFieldException ex) {
            return "id";
        }
    }

    public static String getSearchField(String className) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz == null) return "";
        return getSearchField(clazz);
    }

    public static String getUniqueFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null) return "";
        if (fields.length == 0) return "";

        String uniqueFieldStr = "";
        for (Field field : fields) {
            if (field != null && field.isAnnotationPresent(Unique.class)) {
                uniqueFieldStr += field.getName() + ",";
            }
        }
        return uniqueFieldStr;
    }

    public static String getUniqueFields(String className) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz == null) return "";
        return getUniqueFields(clazz);
    }

    public static boolean hasImageField(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null) return false;
        if (fields.length == 0) return false;

        for (Field field : fields) {
            if (field != null && field.getName() != null && field.getName().toLowerCase().contains("image")) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasImageField(String className) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz == null) return false;
        return hasImageField(clazz);
    }
}
