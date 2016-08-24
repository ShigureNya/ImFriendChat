package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hyphenate.chat.EMClient;

import cn.bmob.v3.Bmob;
import util.LogUtils;

/**
 * Created by Kotori on 2016/8/5.
 */
public class WelcomeActivity extends AppCompatActivity {
    private static final String BMOB_KEY = "0d79f597ccb8897f2cbdb5493f5827b2";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //判断是否登陆过服务器
        boolean isLogin = EMClient.getInstance().isLoggedInBefore();
        //初始化BmobSDK
        Bmob.initialize(WelcomeActivity.this,BMOB_KEY);
        Log.i("Tag",isLogin+"");
        Intent intent = new Intent();
        //此处可以修改为延迟跳转
        if(isLogin){
            intent.setClass(WelcomeActivity.this,MainActivity.class);
        }else{
            intent.setClass(WelcomeActivity.this,LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //测试！测试！测试！
        LogUtils.i("全舰弹幕装填！");
        LogUtils.i("　　 へ　　　　　／|\n" +
                "　　/＼7　　　 ∠＿/\n" +
                "　 /　│　　 ／　／                 皮卡丘！\n" +
                "　│　Z ＿,＜　／　　 /`ヽ\n" +
                "　│　　　　　ヽ　　 /　　〉\n" +
                "　 Y　　　　　`　 /　　/\n" +
                "　ｲ●　､　●　　⊂⊃〈　　/\n" +
                "　()　 へ　　　　|　＼〈\n" +
                "　　>ｰ ､_　 ィ　 │ ／／\n" +
                "　 / へ　　 /　ﾉ＜| ＼＼\n" +
                "　 ヽ_ﾉ　　(_／　 │／／\n" +
                "　　7　　　　　　　|／\n" +
                "　　＞―r￣￣`ｰ―＿");
    }
}
