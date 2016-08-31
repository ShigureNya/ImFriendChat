package cc.jimblog.imfriendchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hyphenate.chat.EMClient;

import cn.bmob.v3.Bmob;
import util.LogUtils;
import util.PermissionCheckerUtil;

/**
 * 改进 此处可以检查Android 6.0用户权限 请将权限管理的相关方法移动到此处
 * -update 权限已统一授权 今后动态权限请在此处配置PERMISSIONS
 * Created by Kotori on 2016/8/5.
 */
public class WelcomeActivity extends AppCompatActivity {
    private static final String BMOB_KEY = "0d79f597ccb8897f2cbdb5493f5827b2";

    private static final int REQUEST_CODE = 0; // 请求码

    private PermissionCheckerUtil permissionUtils ; //授权管理类

    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.GET_TASKS
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //初始化BmobSDK
        Bmob.initialize(WelcomeActivity.this, BMOB_KEY);
        permissionUtils = PermissionCheckerUtil.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent();
        //此处可以修改为延迟跳转
        //判断是否登陆过服务器
        boolean isLogin = EMClient.getInstance().isLoggedInBefore();
        if (isLogin) {
            intent.setClass(WelcomeActivity.this, MainActivity.class);
        } else {
            intent.setClass(WelcomeActivity.this, LoginActivity.class);
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
    //统一授权管理
    @TargetApi(Build.VERSION_CODES.M)
    private void checkedPermission(){
        if(permissionUtils.lacksPermissions(WelcomeActivity.this,PERMISSIONS)){
            requestPermissions(PERMISSIONS,REQUEST_CODE);
        }
    }
}
