package turing;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jimhao on 16/9/5.
 */
public class AesUtil {
    /**
     * @param secret 唯一地址
     * @param apiKey API键
     * @param cmd 名称
     * @return 返回的Aes
     * @throws JSONException
     */
    public static String testAes(String secret , String apiKey , String cmd) throws JSONException {
        //待加密的json数据
        String data = "{\"key\":\""+apiKey+"\",\"info\":\""+cmd+"\"}";
        //获取时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());

        //生成密钥
        String keyParam = secret+timestamp+apiKey;
        String key = Md5.MD5(keyParam);

        //加密
        Aes mc = new Aes(key);
        data = mc.encrypt(data);

        //封装请求参数
        JSONObject json = new JSONObject();
        json.put("key", apiKey);
        json.put("timestamp", timestamp);
        json.put("data", data);
        //请求图灵api
        String result = PostServer.SendPost(json.toString(), "http://www.tuling123.com/openapi/api");

        return result ;
    }
}
