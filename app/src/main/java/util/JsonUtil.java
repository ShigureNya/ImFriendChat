package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import entity.UserInfoEntity;

/**
 * Json工具类
 * Created by jimhao on 16/9/3.
 */
public class JsonUtil {
    private Gson gson ;
    public JsonUtil(){
        gson = new Gson();
    }
    /**
     * @param json 将JSON转换为List集合
     * @return 实体集合
     */
    public List<UserInfoEntity> jsonToList(String json) {
        LogUtils.i("Json数据:"+json);
        List<UserInfoEntity> entityList = gson.fromJson(json, new TypeToken<List<UserInfoEntity>>() {

        }.getType());
        return entityList;
    }
}
