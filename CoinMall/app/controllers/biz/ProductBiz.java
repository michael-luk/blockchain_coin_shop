package controllers.biz;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.StrUtil;
import controllers.BaseController;
import models.Product;
import models.Purchase;
import models.Theme;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.List;

public class ProductBiz extends Controller implements IConst {

    public static double getCoinPrice(double amount) {
        float coinRate = ConfigBiz.getFloatConfig("coin.exchange.rate");
        float coinPriceDiscount = ConfigBiz.getFloatConfig("coin.price.discount");
        if (coinRate == 0f) coinRate = 1f;
        if (coinPriceDiscount == 0f) coinPriceDiscount = 1f;
        return amount * coinPriceDiscount / coinRate;
    }

    public static String getDefaultThemeFieldByLang(Product product, String fieldName, String langStr) {
        Theme defaultTheme = null;
        for (Theme theme : product.themes) {
            if (theme.status == 0) {
                defaultTheme = theme;
                break;
            }
        }
        if (defaultTheme == null) return "";

        Object result = BaseController.getFieldValue(defaultTheme, fieldName + langStr);
        return result == null ? "" : result.toString();
    }
}
