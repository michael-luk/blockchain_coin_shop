import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.*;
import controllers.biz.ConfigBiz;
import models.AdminJournal;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import play.Application;
import play.GlobalSettings;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Request;
import scala.concurrent.duration.FiniteDuration;
import util.ConfigImport;
import util.DummyDataImport;
import util.Journal;
import util.MethodName;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static play.mvc.Controller.session;

public class Global extends GlobalSettings implements IConst {

    public Action onRequest(Request request, Method actionMethod) {
        return new Action.Simple() {
            public F.Promise<play.mvc.Result> call(Http.Context ctx) throws Throwable {
                // handle admin journal
                if (actionMethod.isAnnotationPresent(MethodName.class) && ctx.session().get(SESSION_USER_ROLE) != null
                        && ("1".equals(ctx.session().get(SESSION_USER_ROLE))
                        || "2".equals(ctx.session().get(SESSION_USER_ROLE)))) {
                    MethodName annotation = actionMethod.getAnnotation(MethodName.class);
                    if (StrUtil.isNotNull(annotation.value())) {
                        String methodName = annotation.value();

                        if (methodName.contains("_")) {
                            if (methodName.contains("BASE") && request.uri().split("/").length > 2) {
                                methodName = methodName.replace
                                        ("BASE", TableInfoReader.getTableComment(request.uri().split("/")[2]));
                            } else {
                                String className = methodName.split("_")[1];
                                methodName = methodName.replace(className, TableInfoReader.getTableComment(className));
                            }
                        }

                        AdminJournal newAdminJournal = new AdminJournal();
                        newAdminJournal.name = methodName;
                        newAdminJournal.actor = ctx.session().get("name");
                        newAdminJournal.actorLevel = EnumInfoReader.getEnumName(
                                "Admin", "userRoleEnum", Integer.parseInt(ctx.session().get(SESSION_USER_ROLE)));
                        Ebean.save(newAdminJournal);
                    }
                }

                // RESTFul call log
                play.Logger.info(String.format("%s: %s | JSON: %s", request.method(), request.uri(),
                        request.body().asJson()));
                if (session().get("lang") == null) session("lang", "");
                return delegate.call(ctx);
            }
        };
    }

    public void onStart(Application app) {
        play.Logger.info(SYSTEM_LAUNCH_INFO);

        ConfigImport.insertWithSelfCheck();

        DummyDataImport.insert();

        loadAdminCfg();

        if (controllers.Application.weixinFunction) WeiXinController.wxInit();
        WxOpenController.init();

        // 处理删除无效订单
        Akka.system().scheduler().schedule(
                FiniteDuration.create(0, TimeUnit.SECONDS),//开始延时(秒)
                FiniteDuration.create(ConfigBiz.getIntConfig("order.clean.interval.mins"), TimeUnit.MINUTES),//间隔(分)
                new Runnable() {
                    @Override
                    public void run() {
                        if (controllers.Application.taskFlag) {
                            play.Logger.info("Orders check & kill START ---    " + System.currentTimeMillis());
                            OrderController.checkAndKillUselessOrders();
                        } else {
                            play.Logger.info("Orders check & kill - NOT RUN");
                        }
                    }
                },
                Akka.system().dispatcher()
        );

        // 获取币汇率
        Akka.system().scheduler().schedule(
                FiniteDuration.create(0, TimeUnit.SECONDS),//开始延时(秒)
                FiniteDuration.create(ConfigBiz.getIntConfig("coin.exchange.get.interval.seconds"), TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        if (controllers.Application.taskFlag) {
                            play.Logger.info("Get coin rate START ---    " + System.currentTimeMillis());
                            ExchangeController.getCoinRate();
                        } else {
                            play.Logger.info("Get coin rate - NOT RUN");
                        }
                    }
                },
                Akka.system().dispatcher()
        );

        // 检查币新交易
        Akka.system().scheduler().schedule(
                FiniteDuration.create(0, TimeUnit.SECONDS),//开始延时(秒)
                FiniteDuration.create(30, TimeUnit.SECONDS),//间隔(秒)
                new Runnable() {
                    @Override
                    public void run() {
                        if (controllers.Application.taskFlag) {
                            play.Logger.info("Coin tx check START ---    " + System.currentTimeMillis());
                            OrderController.getCoinTxsDo(10);
                        } else {
                            play.Logger.info("Coin tx check - NOT RUN");
                        }
                    }
                },
                Akka.system().dispatcher()
        );

        // 处理全体微信用户的昵称和头像更新
        Akka.system().scheduler().schedule(
                FiniteDuration.create(doInTime(3, 0), TimeUnit.SECONDS),//延迟执行指导某个时间(时,分)
                FiniteDuration.create(24, TimeUnit.HOURS),//间隔(小时)
                new Runnable() {
                    @Override
                    public void run() {
                        if (controllers.Application.taskFlag && controllers.Application.weixinFunction) {
                            play.Logger.info("SYNC WEXIN USER EVERY DAY AT 3:00");
                            WeiXinController.syncUserInfo();
                        } else {
                            play.Logger.info("SYNC WEXIN USER EVERY DAY - NOT RUN");
                        }
                    }
                },
                Akka.system().dispatcher()
        );
    }

    public void onStop(Application app) {
        play.Logger.info("Stopping tasks");
        controllers.Application.taskFlag = false;
        play.Logger.info("Stopping tasks done");
        super.onStop(app);
    }

    private void loadAdminCfg() {
        play.Logger.info("load admin cfg");
        Config config = ConfigFactory.load();

        if ("org.h2.Driver".equals(config.getString("db.default.driver"))) {
            controllers.Application.dbType = "h2";
            play.Logger.info("use H2 Database");
        }

        if ("com.mysql.jdbc.Driver".equals(config.getString("db.default.driver"))) {
            controllers.Application.dbType = "mysql";
            controllers.Application.dbName = config.getString("mysql.db.name");
            controllers.Application.dbUser = config.getString("db.default.user");
            controllers.Application.dbPsw = config.getString("db.default.password");
            controllers.Application.mysqlBinDir = config.getString("mysql.bin.dir");
            play.Logger.info("use Mysql Database");
        }

        controllers.Application.weixinFunction = "yes".equals(config.getString("weixin.function"));
        play.Logger.info("weixin function is: " + Boolean.toString(controllers.Application.weixinFunction));

        controllers.Application.smsCaptchaFunction = "yes".equals(config.getString("sms.captcha.function"));
        play.Logger.info("sms captcha function is: " + Boolean.toString(controllers.Application.smsCaptchaFunction));

        EmailController.emailUser = config.getString("smtp.user");
//        DbController.bakFileReceiveEmail = config.getString("dbbak.receive.email");

        play.Logger.info("load admin cfg done");
    }

    public static int doInTime(int hour, int minute) {
        return Seconds.secondsBetween(
                new DateTime(),
                nextExecution(hour, minute)
        ).getSeconds();
    }

    public static DateTime nextExecution(int hour, int minute) {
        DateTime next = new DateTime()
                .withHourOfDay(hour)
                .withMinuteOfHour(minute)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        return (next.isBeforeNow())
                ? next.plusHours(24)
                : next;
    }
}