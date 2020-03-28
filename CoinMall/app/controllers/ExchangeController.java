package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import controllers.biz.ConfigBiz;
import controllers.biz.SaveBiz;
import models.*;
import play.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.imageio.ImageIO;
import javax.persistence.PersistenceException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static play.data.Form.form;

public class ExchangeController extends Controller implements IConst {

    public static F.Promise<Float> getCoinRate() {
        // https://api.exx.com/data/v1/ticker?currency=xwc_qc
        /*
        {
            ticker: {
                vol: "618898.61",
                last: "0.890000000",
                sell: "0.90",
                buy: "0.89",
                weekRiseRate: 14.11,
                riseRate: 2.3,
                high: "1.0",
                low: "0.8",
                monthRiseRate: -44.37
            },
            date: "1518680316380"
        }
         */
        String url = "https://api.exx.com/data/v1/ticker?currency=xwc_cnyt";
        F.Promise<WSResponse> response = WS.url(url).get();
        return response.map(resp -> {
            try {
                JsonNode jsonResult = resp.asJson();
                Double respValue = jsonResult.path("ticker").path("last").asDouble();
//                Boolean result = jsonResult.path("result").asBoolean();
                play.Logger.info("response getCoinRate: " + jsonResult.toString());
                if (respValue > 0) {
                    play.Logger.info("set CoinRate: " + respValue);
                    ConfigBiz.setConfig("coin.exchange.rate", respValue.toString());
                }
            } catch (Exception ex) {
                play.Logger.error("response getCoinRate ex: " + ex.getMessage());
            }
            return ConfigBiz.getFloatConfig("coin.exchange.rate");
        });
    }

    public static Result getCoinRateDo() {
        return ok(getCoinRate().get(60 * 1000).toString());
    }
}
