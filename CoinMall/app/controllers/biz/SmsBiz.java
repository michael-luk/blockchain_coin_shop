package controllers.biz;

import LyLib.Interfaces.IConst;
import LyLib.Utils.*;
import com.avaje.ebean.Ebean;
import controllers.EmailController;
import controllers.Secured;
import models.SmsInfo;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SmsBiz extends Controller implements IConst {
    public static SmsInfo getLatestCode(String phone, String checkCode) {
        List<SmsInfo> codes = SmsInfo.find.where().eq("phone", phone).eq("checkCode", checkCode).orderBy("id desc").findList();
        if (codes.size() > 0) {
            return codes.get(0);
        }
        return null;
    }
}
