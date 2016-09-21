package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.JianPanUtils;
import util.LogUtils;
import util.NetWorkUtils;
import util.SnackBarUtil;
import util.StorageUtils;

/**
 * Created by Kotori on 2016/8/6.
 */
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_input_username)
    EditText loginInputUsername;
    @BindView(R.id.login_username_layout)
    TextInputLayout loginUsernameLayout;
    @BindView(R.id.login_input_password)
    EditText loginInputPassword;
    @BindView(R.id.login_password_layout)
    TextInputLayout loginPasswordLayout;
    @BindView(R.id.login_ripple_button)
    RelativeLayout loginRippleButton;
    @BindView(R.id.login_signup_btn)
    TextView loginSignupBtn;
    @BindView(R.id.login_snackbar_layout)
    CoordinatorLayout loginSnackbarLayout;

    private Snackbar snackbar ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        //将数据从SharedPreferences中取出
        String saveAccount = (String) StorageUtils.get(this, "SaveAccount", "");
        //如果sharedPreferences中存在数据，则直接设置给EditText
        if (!saveAccount.equals("") || saveAccount != null) {
            loginInputUsername.setText(saveAccount);
        }
    }

    @Override
    protected void onResume() {
        //将EditText注册到 TextWatcher
        loginInputUsername.addTextChangedListener(new InputContentChangeListener());
        loginInputPassword.addTextChangedListener(new InputContentChangeListener());
        super.onResume();
    }

    @OnClick({R.id.login_ripple_button, R.id.login_signup_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_ripple_button:
                loginBtn();  //登陆按钮
                break;
            case R.id.login_signup_btn:
                Intent intent = new Intent(LoginActivity.this, SignActivity.class);
                startActivity(intent);   //跳转到页面
                break;
        }
        ;
    }

    /**
     * 登陆的各种判断
     */
    public void loginBtn() {
        //按下按钮时关闭软键盘
        JianPanUtils.closeKeybord(loginInputUsername, LoginActivity.this);
        JianPanUtils.closeKeybord(loginInputPassword, LoginActivity.this);
        //从EditText中取值
        String account = loginInputUsername.getText().toString().trim();
        String password = loginInputPassword.getText().toString().trim();

        if (account == null || account.trim().equals("")) {
            setAnimation(loginUsernameLayout);
            showSnackBar(getString(R.string.login_not_account_toast));
            return;
        }
        if (password == null || password.trim().equals("")) {
            setAnimation(loginPasswordLayout);
            showSnackBar(getString(R.string.login_not_password_toast));
            return;
        }
        if (!NetWorkUtils.isConnected(this)) {
            showSnackBar(getString(R.string.login_connect_error_toast));
            return;
        }
        //将账号和密码封装成HashMap
        HashMap<String, String> userMap = new HashMap<String, String>();
        userMap.put("Account", account);
        userMap.put("Password", password);
        login(userMap);
    }

    //登录接口采用RxJava异步的方式构成 EMClient实现消息后台分发 Rxjava负责后台消息处理和前台接收
    public void login(HashMap<String, String> map) {
        snackbar = SnackBarUtil.longSnackbar(loginSnackbarLayout,"登录中...", Color.WHITE);
        snackbar.setDuration(10000);
        snackbar.show();
        final String account = map.get("Account");
        final String password = map.get("Password");
        //创建被监听者对象
        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                EMClient.getInstance().login(account, password, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        LogUtils.i("登录成功");
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(int i, String s) {
                        LogUtils.i("抛异常了");
                        subscriber.onError(new Throwable("异常信息为登录失败:"+s));
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
            }
        });
        observable.subscribeOn(Schedulers.io()) //将登录方法放置在IO线程中执行
                .observeOn(AndroidSchedulers.mainThread())      //将被监听对象返回的结果回调给UI线程
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        snackbar.dismiss();
                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        LogUtils.d("main", "登录聊天服务器成功！");
                        //将数据存入SharedPreferences中
                        StorageUtils.put(LoginActivity.this, "SaveAccount", account);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        snackbar.dismiss();
                        LogUtils.e("main", "登录聊天服务器失败！" + throwable.toString());
                        showSnackBar("您可能是密码错误的受害者！Σ(っ°Д°;)っ");
                    }

                    @Override
                    public void onNext(Integer integer) {

                    }
                });
    }

    /**
     * 处理输入数据的回调
     */
    class InputContentChangeListener implements TextWatcher {
        CharSequence temp;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            temp = charSequence;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (temp.length() == 0) {
                loginRippleButton.setBackgroundResource(R.drawable.login_not_btn_shape);
            } else if (temp.length() > 0) {
                loginRippleButton.setBackgroundResource(R.drawable.login_edit_btn_shape);
            }
        }
    }

    /**
     * 开启一个动画
     *
     * @param v 开启动画的View
     */
    private void setAnimation(View v) {
        Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.view_shake);
        v.startAnimation(animation);
    }

    /**
     * 展示SnackBar
     * @param message 消息对象
     */
    public void showSnackBar(String message) {
        Snackbar.make(loginSnackbarLayout, message, 1300).show();
    }

}
