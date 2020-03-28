package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import controllers.biz.ConfigBiz;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static play.data.Form.form;

public class ImageController extends Controller implements IConst {

    public static Result showImage(String filename) {
        String path = Play.application().path().getPath() + "/public/upload/" + filename;

        try {
            response().setContentType("image");
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    IOUtils.toByteArray(new FileInputStream(new File(path))));
            return ok(bais);
        } catch (IOException e) {
            play.Logger.error(": " + e.getMessage());
        }
        return notFound(filename + " is Not Found!");
    }

    // @Security.Authenticated(Secured.class)
    // @Cached(key = "showImg")
    public static Result showImg(String folder, String filename) {
        String path = Play.application().path().getPath() + "/public/" + folder + "/" + filename;

        try {
            response().setContentType("image");
            return ok(getImageByte(path));
        } catch (IOException ex) {
            play.Logger.error("找: " + folder + filename + "ex: " + ex.getMessage());
        }
        return notFound(folder + filename + " is Not Found!");
    }

    // @Security.Authenticated(Secured.class)
    public static Result showBarcode(String filename) {
        String path = Play.application().path().getPath() + "/public/barcode/" + filename;

        try {
            response().setContentType("image");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    IOUtils.toByteArray(new FileInputStream(new File(path))));
            return ok(byteArrayInputStream);
        } catch (IOException e) {
            play.Logger.error("找不到二维码: " + e.getMessage());
        }
        return notFound(filename + " barcode is Not Found!");
    }

    public static boolean GenerateThumbNailImg(String baseFilePath, String thumbNailPath, float tagsize)
            throws Exception {
        if (tagsize == 0)
            tagsize = 100;

        String newUrl = thumbNailPath;
        java.awt.Image bigJpg = ImageIO.read(new File(baseFilePath));

        if (bigJpg == null) {
            return false;
        }

        int old_w = bigJpg.getWidth(null);
        int old_h = bigJpg.getHeight(null);
        int new_w = 0;
        int new_h = 0;

        float tempDouble;
        tempDouble = old_w > old_h ? old_w / tagsize : old_h / tagsize;
        new_w = Math.round(old_w / tempDouble);
        new_h = Math.round(old_h / tempDouble);

        java.awt.image.BufferedImage tag = new java.awt.image.BufferedImage(new_w, new_h,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        tag.getGraphics().drawImage(bigJpg, 0, 0, new_w, new_h, null);

        try {
            File outputFile = new File(newUrl);
            ImageIO.write(tag, "png", outputFile);
        } catch (IOException e) {
            play.Logger.error("生成: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result generateAllThumbNailImg(float tagSize) {
        if (tagSize == 0)
            tagSize = 300;

        String path = Play.application().path().getPath() + "/public/upload/";
        File file = new File(path);
        String[] fileNameList = file.list();

        String thumbNailPath = Play.application().path().getPath() + "/public/thumb/";
        for (String fileName : fileNameList) {
            play.Logger.info("generate thumb nail img: " + fileName);
            // 
            try {
                if (!GenerateThumbNailImg(path + fileName, thumbNailPath + fileName, tagSize))
                    play.Logger.error("generate thumbnail img issue: unknown");
                else
                    play.Logger.info("generate thumbnail img success");
            } catch (Exception ex) {
                play.Logger
                        .error("generate thumbnail img issue: " + ex.getMessage());
                return notFound("生成图片的缩略图出错");
            }
        }
        return ok("");
    }

    public static ByteArrayInputStream getImageByte(String path) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(new FileInputStream(new File(path))));
        return byteArrayInputStream;
    }

    public static Result uploadImage() {
        Msg<String> msg = new Msg<>();

        Http.MultipartFormData body = request().body().asMultipartFormData();

        Http.MultipartFormData.FilePart imgFile = body.getFile("file");
        if (imgFile != null) {
            // 毫秒命名
            String path = Play.application().path().getPath() + "/public/upload/";
            String destFileName = String.valueOf(System.currentTimeMillis());

            String contentType = imgFile.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                msg.message = "上传的不是图片文件";
                play.Logger.error("result: " + msg.message);
                return ok(Json.toJson(msg));
            }

            File file = imgFile.getFile();
            try {
                // 
                FileUtils.copyFile(file, new File(path + destFileName));
                play.Logger.info("upload img success");

                // 
                String thumbNailPath = Play.application().path().getPath() + "/public/thumb/";
                try {
                    if (!GenerateThumbNailImg(path + destFileName, thumbNailPath + destFileName, ConfigBiz.getFloatConfig("pic.thumb.size")))
                        play.Logger.error("generate thumbnail img issue: unknown");
                    else
                        play.Logger.info("generate thumbnail img success");

                } catch (Exception ex) {
                    play.Logger.error(
                            "generate thumbnail img issue: " + ex.getMessage());
                    msg.message = "图片转换缩略图出错, 请检查图片的后缀名.";
                    return ok(Json.toJson(msg));
                }

                String thumbMidNailPath = Play.application().path().getPath() + "/public/mid_thumb/";
                try {
                    if (!GenerateThumbNailImg(path + destFileName, thumbMidNailPath + destFileName, ConfigBiz.getFloatConfig("pic.mid.thumb.size")))
                        play.Logger.error("generate mid thumbnail img issue: unknown");
                    else
                        play.Logger.info("generate mid thumbnail img success");

                } catch (Exception ex) {
                    play.Logger.error(
                            "generate thumbnail img issue: " + ex.getMessage());
                    msg.message = "图片转换中等缩略图出错, 请检查图片的后缀名.";
                    return ok(Json.toJson(msg));
                }

                msg.flag = true;
                msg.data = destFileName;
                play.Logger.info("result: " + destFileName);
                return ok(Json.toJson(msg));
            } catch (IOException e) {
                msg.message = e.getMessage();
                play.Logger.info("result: " + msg.message);
                return ok(Json.toJson(msg));
            }
        }

        msg.message = "错误: 找不到图片";
        play.Logger.info("result: " + msg.message);
        return ok(Json.toJson(msg));
    }

    public static String getImageString(String imageStr) {
        if (StrUtil.isNull(imageStr)) return imageStr;
        if (imageStr.contains(",")) return imageStr.split(",")[0];
        return imageStr;
    }

    public static String getImageStringWithI18N(Object obj, String imageField) {
        if (StrUtil.isNull(imageField)) return "";
        Object result = BaseController.getFieldValue(obj, imageField);
        if (result == null) return "";
        String imageStr = result.toString();
        if (imageStr.contains(",")) return imageStr.split(",")[0];
        return imageStr;
    }
}
