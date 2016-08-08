package cc.jimblog.imfriendchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import util.LogUtils;
import util.NetWorkUtils;
import util.ToastUtils;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginInputUsername.addTextChangedListener(new InputContentChangeListener());
        loginInputPassword.addTextChangedListener(new InputContentChangeListener());
    }

    @OnClick({R.id.login_ripple_button , R.id.login_signup_btn})
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.login_ripple_button:
               loginBtn();
               break;
           case R.id.login_signup_btn:
               Bundle intentBundle = new Bundle();
               Intent intent = new Intent(LoginActivity.this,SignActivity.class);
               String accountStr = loginInputUsername.getText().toString().trim();
               if(accountStr != null && !accountStr.equals("")){
                    intentBundle.putString("Account",accountStr);
               }else{
                    intentBundle.putString("Account","");
               }
               intent.putExtras(intentBundle);
               startActivity(intent);
               break;
       };
    }
    public void loginBtn(){
        String account = loginInputUsername.getText().toString().trim();
        String password = loginInputPassword.getText().toString().trim();

        if (account == null || account.trim().equals("")) {
            setAnimation(loginUsernameLayout);
            return;
        }
        if (password == null || password.trim().equals("")) {
            setAnimation(loginPasswordLayout);
            ToastUtils.showShort(this, getString(R.string.login_not_password_toast));
            return;
        }
        if (!NetWorkUtils.isConnected(this)) {
            ToastUtils.showShort(this, getString(R.string.login_connect_error_toast));
            return;
        }
        HashMap<String, String> userMap = new HashMap<String, String>();
        userMap.put("Account", account);
        userMap.put("Password", password);
        login(userMap);
    }

    //登录接口采用RxJava异步的方式构成 EMClient实现消息后台分发 Rxjava负责后台消息处理和前台接收
    public void login(HashMap<String, String> map) {
        final String account = map.get("Account");
        final String password = map.get("Password");
        //创建被监听者对象
        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                EMClient.getInstance().login(account, password, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        Integer position = 1;
                        subscriber.onNext(position);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(int i, String s) {
                        Integer position = 2;
                        subscriber.onNext(position);
                        subscriber.onError(new Throwable(s));
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
                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        LogUtils.d("main", "登录聊天服务器成功！");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LogUtils.e("main", "登录聊天服务器失败！" + throwable.toString());
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 1) {
                            //登陆聊天服务器成功
                            ToastUtils.showShort(getApplicationContext(), "登录聊天服务器成功");
                        } else {
                            ToastUtils.showShort(getApplicationContext(), "登录聊天服务器失败");
                        }
                    }
                });
    }
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

    private void setAnimation(View v) {
        Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.view_shake);
        v.startAnimation(animation);
    }
}
