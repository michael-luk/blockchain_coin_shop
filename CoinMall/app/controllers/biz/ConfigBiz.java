package controllers.biz;

import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import models.Config;
import play.api.data.Field;

import java.util.List;

public class ConfigBiz {

    public static String getStrConfig(String name) {
        if (StrUtil.isNull(name)) return "";
        Config config = Config.find.where().eq("name", name).findUnique();
        if (config == null) return "";
        if (StrUtil.isNull(config.content)) return "";
        if (config.typeEnum != 0) return "";
        return config.content;
    }

    public static int getIntConfig(String name) {
        if (StrUtil.isNull(name)) return 0;
        Config config = Config.find.where().eq("name", name).findUnique();
        if (config == null) return 0;
        if (StrUtil.isNull(config.content)) return 0;
        if (config.typeEnum != 1) return 0;
        return Integer.valueOf(config.content);
    }

    public static float getFloatConfig(String name) {
        if (StrUtil.isNull(name)) return 0;
        Config config = Config.find.where().eq("name", name).findUnique();
        if (config == null) return 0;
        if (StrUtil.isNull(config.content)) return 0;
        if (config.typeEnum != 2) return 0;
        return Float.valueOf(config.content);
    }

    public static boolean getBoolConfig(String name) {
        if (StrUtil.isNull(name)) return false;
        Config config = Config.find.where().eq("name", name).findUnique();
        if (config == null) return false;
        if (StrUtil.isNull(config.content)) return false;
        if (config.typeEnum != 3) return false;
        return "yes".equals(config.content);
    }

    public static boolean setConfig(String name, String content) {
        if (StrUtil.isNull(name)) return false;

        Config config = Config.find.where().eq("name", name).findUnique();

        if (config == null) return false;

        config.content = content;
        config.save();

        return true;
    }

    public static boolean selfCheck4All() {
        List<Config> cfgList = Config.find.all();
        for (Config cfg : cfgList) {
            if (!selfCheck(cfg)) {
                play.Logger.error("Config all self check break on: " + cfg.toString());
                return false;
            }
        }
        play.Logger.info("All custom config pass self check.");
        return true;
    }

    public static boolean selfCheck(Config cfg) {
        if (cfg.typeEnum == 0) return true;   //string

        if (cfg.typeEnum == 1) {    //number
            if (StrUtil.isNull(cfg.content)) {
                return false;
            } else {
                boolean parseResult = false;
                try {
                    Integer.parseInt(cfg.content);
                    parseResult = true;
                } catch (NumberFormatException ex) {
                }
                try {
                    Long.parseLong(cfg.content);
                    parseResult = true;
                } catch (NumberFormatException ex) {
                }

                if (!parseResult) {
                    play.Logger.error("Config self check error: " + cfg.toString());
                }
                return parseResult;
            }
        }

        if (cfg.typeEnum == 2) {    //float
            if (StrUtil.isNull(cfg.content)) {
                return false;
            } else {
                try {
                    Float.parseFloat(cfg.content);
                    return true;
                } catch (NumberFormatException ex) {
                    play.Logger.error("Config self check error: " + cfg.toString());
                }
                return false;
            }
        }

        if (cfg.typeEnum == 3) {    //bool
            if (StrUtil.isNull(cfg.content)) {
                return false;
            } else {
                return "yes".equals(cfg.content) || "no".equals(cfg.content);
            }
        }
        return false;
    }
}
