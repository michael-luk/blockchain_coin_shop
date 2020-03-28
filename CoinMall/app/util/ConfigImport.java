package util;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import controllers.biz.ConfigBiz;
import models.Config;
import play.libs.Yaml;

import java.util.List;
import java.util.Map;

public class ConfigImport implements IConst {
    public static void insertWithSelfCheck() {
        play.Logger.info("start load custom config");

        if (Ebean.find(Config.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("config-data.yml");
                List<Object> defaultObjs = initData.get("config");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        play.Logger.info("start all custom config self check");
                        boolean allPassSelfCheck = true;
                        for (Object obj : defaultObjs) {
                            if (!ConfigBiz.selfCheck((Config) obj)) {
                                play.Logger.error(String.format("load custom cfg break on: %s", (Config) obj).toString());
                                allPassSelfCheck = false;
                                break;
                            }
                        }

                        if (allPassSelfCheck) {
                            Ebean.save(defaultObjs);
                            play.Logger.info(String.format("load custom cfg %s", defaultObjs.size()));
                        }
                    }
                }
                play.Logger.info("load custom cfg done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        } else {
            ConfigBiz.selfCheck4All();
        }
    }
}
