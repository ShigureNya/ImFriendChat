package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hyphenate.chat.EMClient;

/**
 * Created by Kotori on 2016/8/5.
 */
public class WelcomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_welcome);
        //判断是否登陆过服务器
        boolean isLogin = EMClient.getInstance().isConnected();
        Log.i("Tag",isLogin+"");
        Intent intent = new Intent();
        //此处可以修改为延迟跳转
        if(isLogin){
            intent.setClass(WelcomeActivity.this,MainActivity.class);
        }else{
            intent.setClass(WelcomeActivity.this,LoginActivity.class);
        }
        startActivity(intent);
    }
}
