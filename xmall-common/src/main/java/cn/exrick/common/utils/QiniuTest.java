package cn.exrick.common.utils;

import cn.exrick.common.exception.XmallUploadException;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Exrickx
 */
public class QiniuTest {

    private final static Logger log= LoggerFactory.getLogger(QiniuTest.class);

    /**
     * 生成上传凭证，然后准备上传
     */
    private static String accessKey = "tvYPuBwCygJdLW8cO3UGHKIC_F5gFqz3m1TEp8vx";
    private static String secretKey = "4G0d55OKNT70X9sozHT8uzcsiNqh8SEI_44UPf7K";
    private static String bucket = "upload-2020";
    private static String origin="http://q9te1ixg1.bkt.clouddn.com/";
    private static  Auth auth = Auth.create(accessKey, secretKey);


    public static String getUpToken() {
        return auth.uploadToken(bucket, null, 3600, new StringMap().put("insertOnly", 1));
    }

    public static void qiniuUpload(String filePath){

        //构造一个带指定Zone对象的配置类 zone2华南
        Configuration  cfg = new Configuration(Zone.zone2());

        UploadManager uploadManager = new UploadManager(cfg);

        String localFilePath = filePath;
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
//        new File(localFilePath);

        try {
            localFilePath= URLDecoder.decode(QiniuTest.class.getResource("/upload/1.jpg").getPath(),"utf-8");
            Response response = uploadManager.put(localFilePath, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
//            return origin+putRet.key;
        }catch(QiniuException ex){
           log.error("123",ex);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件流上传
     * @param file
     * @param key 文件名
     * @return
     */
    public static String qiniuInputStreamUpload(FileInputStream file, String key){

        //构造一个带指定Zone对象的配置类 zone2华南
        Configuration cfg = new Configuration(Zone.zone2());

        UploadManager uploadManager = new UploadManager(cfg);

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(file,key,upToken,null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return origin+putRet.key;
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.warn(r.toString());
            try {
                return r.bodyString();
            } catch (QiniuException e) {
                throw new XmallUploadException(e.toString());
            }
        }
    }

    public static String qiniuBase64Upload(String data64){

        String key = renamePic(".png");
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        //服务端http://up-z2.qiniup.com
        String url = "http://up-z2.qiniup.com/putb64/-1/key/"+ UrlSafeBase64.encodeToString(key);
        RequestBody rb = RequestBody.create(null, data64);
        Request request = new Request.Builder().
                url(url).
                addHeader("Content-Type", "application/octet-stream")
                .addHeader("Authorization", "UpToken " + getUpToken())
                .post(rb).build();
        OkHttpClient client = new OkHttpClient();
        okhttp3.Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return origin+key;
    }

    public static String base64Data(String data){

        if(data==null||data.isEmpty()){
            return "";
        }
        String base64 =data.substring(data.lastIndexOf(",")+1);
        return base64;
    }

    /**
     * 以UUID重命名
     * @param fileName
     * @return
     */
    public static String renamePic(String fileName){
        String extName = fileName.substring(fileName.lastIndexOf("."));
        return UUID.randomUUID().toString().replace("-","")+extName;
    }



    public static String checkExt(String fileName,String dirName){
        //定义允许上传的文件扩展名
        HashMap<String, String> extMap = new HashMap<String, String>();
        extMap.put("image", "gif,jpg,jpeg,png,bmp");
        extMap.put("flash", "swf,flv");
        extMap.put("media", "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb");

        //检查扩展名
        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if(!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(fileExt)){
            return "上传文件扩展名是不允许的扩展名\n只允许" + extMap.get(dirName) + "格式";
        }
        return "valid";
    }

    public static void main(String[] args){
//        base64Data("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2");
        qiniuUpload("./1.jpg");
    }
}
