package controllers;

import LyLib.Utils.StrUtil;
import util.EnumMap;

import java.lang.reflect.Field;
import java.util.HashMap;

public class EnumInfoReader {

    public static String getEnumName(Class<?> clazz, String fieldName, int enumValue) {
        String defaultResult = String.valueOf(enumValue);
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return defaultResult;
        }

        if (field != null && field.isAnnotationPresent(EnumMap.class)) {
            EnumMap annotation = (EnumMap) field.getAnnotation(EnumMap.class);
            if (StrUtil.isNotNull(annotation.value()) && StrUtil.isNotNull(annotation.name())) {
                String[] values = annotation.value().split(",");
                String[] names = annotation.name().split(",");

                if (values.length != names.length) return defaultResult;

                HashMap<Integer, String> enumMap = new HashMap<>();
                for (int i = 0; i < names.length; i++) {
                    try {
                        enumMap.put(Integer.valueOf(values[i]), names[i]);
                    } catch (NumberFormatException ex) {
                        play.Logger.error("Enum parse issue: " + clazz.getName() + "." + fieldName);
                    }
                }
                return StrUtil.isNull(enumMap.get(enumValue)) ? defaultResult : enumMap.get(enumValue);
            }
        }
        return defaultResult;
    }

    public static String getEnumName(String className, String fieldName, int enumValue) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz == null) return String.valueOf(enumValue);
        return getEnumName(clazz, fieldName, enumValue);
    }

    // 得到枚举的串起来的字符串, 给前端用, 如: "'有效','无效'
    public static String getEnumNameLinkString(Class<?> clazz, String fieldName) {
        String defaultResult = "";
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return defaultResult;
        }

        if (field != null && field.isAnnotationPresent(EnumMap.class)) {
            EnumMap annotation = (EnumMap) field.getAnnotation(EnumMap.class);
            if (StrUtil.isNotNull(annotation.value()) && StrUtil.isNotNull(annotation.name())) {
                return annotation.name();
//                String resultStr = "'" + annotation.name() + "'";
//                return resultStr.replace(",", "','");
            }
        }
        return defaultResult;
    }

    public static String getEnumNameLinkString(String className, String fieldName) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz == null) return "";
        return getEnumNameLinkString(clazz, fieldName);
    }

    public static int getEnumCount(Class<?> clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return 4;
        }

        if (field != null && field.isAnnotationPresent(EnumMap.class)) {
            EnumMap annotation = (EnumMap) field.getAnnotation(EnumMap.class);
            if (StrUtil.isNotNull(annotation.value()) && StrUtil.isNotNull(annotation.name())) {
                String[] values = annotation.value().split(",");
                String[] names = annotation.name().split(",");

                if (values.length != names.length) return 4;

                HashMap<Integer, String> enumMap = new HashMap<>();
                for (int i = 0; i < names.length; i++) {
                    try {
                        enumMap.put(Integer.valueOf(values[i]), names[i]);
                    } catch (NumberFormatException ex) {
                        play.Logger.error("Enum parse issue: " + clazz.getName() + "." + fieldName);
                    }
                }
                return values.length;
            }
        }
        return 4;
    }

    public static int getEnumCount(String className, String fieldName) {
        Class<?> clazz = BaseController.getClass("models." + className);
        if (clazz == null) return 4;
        return getEnumCount(clazz, fieldName);
    }
}
