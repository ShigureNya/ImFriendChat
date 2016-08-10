package application;

import android.app.Application;
import android.content.Context;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

import util.LogUtils;

/**
 * Created by Kotori on 2016/8/5.
 */
public class HuanXinApplication extends Application{
    private Context applicationContext ;
    @Override
    public void onCreate(){
        super.onCreate();
        if(applicationContext == null){
            applicationContext = getApplicationContext();
        }
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        //初始化
        EMClient.getInstance().init(applicationContext, options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
    }
}
