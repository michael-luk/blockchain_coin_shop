
package util;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import models.*;
import play.libs.Yaml;

import java.util.List;
import java.util.Map;

public class DummyDataImport implements IConst {
    public static void insert() {
        play.Logger.info("start load dummy data");

        if (Ebean.find(Admin.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("admin");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Admin %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Admin done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Catalog.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("catalog");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Catalog %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Catalog done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Config.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("config");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Config %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Config done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Info.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("info");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Info %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Info done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Product.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("product");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Product %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Product done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Purchase.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("purchase");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Purchase %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Purchase done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(ShipInfo.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("shipInfo");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default ShipInfo %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default ShipInfo done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(SmsInfo.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("smsInfo");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default SmsInfo %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default SmsInfo done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Theme.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("theme");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Theme %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Theme done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(User.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("user");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default User %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default User done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(ReturnInfo.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("returnInfo");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default ReturnInfo %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default ReturnInfo done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(CommentInfo.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("commentInfo");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default CommentInfo %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default CommentInfo done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

    }
}
