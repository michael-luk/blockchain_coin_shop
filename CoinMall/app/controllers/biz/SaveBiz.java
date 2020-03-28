package controllers.biz;

import models.*;

import java.util.Date;

public class SaveBiz {

    public static void beforeSave(Admin obj) {

    }

    public static void beforeUpdate(Admin obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(AdminJournal obj) {

    }

    public static void beforeUpdate(AdminJournal obj) {
    }

    public static void beforeSave(BalanceUse obj) {

    }

    public static void beforeUpdate(BalanceUse obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(Fundout obj) {

    }

    public static void beforeUpdate(Fundout obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(Catalog obj) {

    }

    public static void beforeUpdate(Catalog obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(Config obj) {

    }

    public static void beforeSave(CoinRecord obj) {

    }

    public static void beforeUpdate(CoinRecord obj) {
    }

    public static void beforeUpdate(Config obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(Info obj) {

    }

    public static void beforeUpdate(Info obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(Product obj) {

    }

    public static void beforeUpdate(Product obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(Purchase obj) {

    }

    public static void beforeUpdate(Purchase obj) {
        obj.lastUpdateTime = new Date();
        if (obj.status == 4) {
            // 若改成已发货, 记录发货时间
            obj.shipTime = new Date();
        }
    }

    public static void beforeSave(ShipInfo obj) {

    }

    public static void beforeUpdate(ShipInfo obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(SmsInfo obj) {

    }

    public static void beforeUpdate(SmsInfo obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(Theme obj) {

    }

    public static void beforeUpdate(Theme obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(ReturnInfo obj) {

    }

    public static void beforeUpdate(ReturnInfo obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(CommentInfo obj) {

    }

    public static void beforeUpdate(CommentInfo obj) {
        obj.lastUpdateTime = new Date();
    }

    public static void beforeSave(User obj) {

    }

    public static void beforeUpdate(User obj) {
        obj.lastUpdateTime = new Date();
    }
}