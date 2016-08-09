package fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.jimblog.imfriendchat.R;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.LogUtils;
import util.NetWorkUtils;
import util.StorageUtils;
import util.ToastUtils;
import view.GetVerificationCode;

/**
 * Created by Ran on 2016/8/8.
 */
public class InputPasswordFragment extends Fragment {
    @BindView(R.id.sign_input_pwd)
    EditText signInputPwd;
    @BindView(R.id.sign_checked_pwd)
    EditText signCheckedPwd;
    @BindView(R.id.sign_input_code)
    EditText signInputCode;
    @BindView(R.id.sign_show_code)
    ImageView signShowCode;
    @BindView(R.id.sign_checked_layout)
    LinearLayout signCheckedLayout;
    @BindView(R.id.sign_submit_button_text)
    TextView signSubmitButtonText;
    @BindView(R.id.sign_submit_btn)
    RippleView signSubmitBtn;
    private View mView = null;
    private Bitmap codeMap = null ; //验证码图片
    private String code = null; //验证码值

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_input_password, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        signInputPwd.addTextChangedListener(new InputPwdEditListener());
        signShowCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initCodeBitmap(signShowCode);
            }
        });
        initCodeBitmap(signShowCode);
    }

    @OnClick(R.id.sign_submit_btn)
    public void onClick() {
        if(!NetWorkUtils.isConnected(mView.getContext())){
            ToastUtils.showShort(getContext(),getString(R.string.login_connect_error_toast));
            return ;
        }
        String password = signInputPwd.getText().toString().trim();
        String checkedPassword = signCheckedPwd.getText().toString().trim();

        if(password == null || password.equals("")){
            ToastUtils.showShort(getContext(),getString(R.string.register_input_pwd_edit_hint));
            setAnimation(signInputPwd);
            return ;
        }
        if(checkedPassword == null || checkedPassword.equals("")){
            ToastUtils.showShort(getContext(),getString(R.string.register_input_code_hint));
            setAnimation(signCheckedPwd);
            return ;
        }
        if(!password.equals(checkedPassword)){
            ToastUtils.showShort(getContext(),getString(R.string.register_two_input_pwd_error_toast));
            setAnimation(signCheckedPwd);
            signCheckedPwd.setText("");
            return ;
        }
        String codeStr = signInputCode.getText().toString().trim();

        if(codeStr == null || codeStr.equals("")){
            ToastUtils.showShort(getContext(),getString(R.string.register_input_code_hint));
            setAnimation(signInputCode);
            return ;
        }

        if(!codeStr.equalsIgnoreCase(code)){
            ToastUtils.showShort(getContext(),getString(R.string.register_verificationCode_error_toast));
            setAnimation(signInputCode);
            signInputCode.setText("");
            return ;
        }
        HashMap<String,String> map = new HashMap<String,String>();
        String account = (String) StorageUtils.get(getContext(),"Account","");
        map.put("Account",account);
        map.put("Password",password);
        registerToHuanXin(map);
    }
    private void registerToHuanXin(HashMap<String,String> map){
        final String account = map.get("Account");
        final String password = map.get("Password");
        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                try {
                    EMClient.getInstance().createAccount(account,password);
                    subscriber.onNext(1);
                    subscriber.onCompleted();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    subscriber.onNext(2);
                    subscriber.onError(new Throwable(e.toString()));
                }
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                ToastUtils.showShort(mView.getContext(),"Sign Ok");
            }

            @Override
            public void onError(Throwable throwable) {
                ToastUtils.showShort(mView.getContext(),"Sign Error");
            }

            @Override
            public void onNext(Integer integer) {

            }
        });
    }
    private boolean isShow = false ;    //是否显示的标记

    public class InputPwdEditListener implements TextWatcher {
        CharSequence temp ;
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            temp = charSequence ;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(temp.length() > 0 ){
                if(!isShow){
                    signCheckedLayout.setVisibility(View.VISIBLE);
                    startAnimationSet(signCheckedLayout);
                    isShow = true ; 
                }
            }
        }
    }
    private void startAnimationSet(LinearLayout layout){
        LogUtils.d("以下方法已被执行");
        Animation translateAnimation = new TranslateAnimation(0,0,-110f,0f);
        Animation alphaAnimation = new AlphaAnimation(0f,1.0f);
        AnimationSet animSet = new AnimationSet(true);
        animSet.addAnimation(alphaAnimation);
        animSet.addAnimation(translateAnimation);
        animSet.setDuration(750);
        animSet.setFillAfter(true);                 //停留在最后的位置
        animSet.setFillEnabled(true);
        layout.setAnimation(animSet);                    //设置动画
        animSet.startNow();
    }

    /**
     * 用于初始化二维码图片
     */
    private void initCodeBitmap(ImageView v){
        if(codeMap != null){
            codeMap.recycle();
            this.codeMap = null;
        }
        codeMap = GetVerificationCode.getInstance().createBitmap();
        if(codeMap != null){
            v.setImageBitmap(codeMap);
            this.code = GetVerificationCode.getInstance().getCode();
        }
    }
    private void setAnimation(View v) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.view_shake);
        v.startAnimation(animation);
    }
}
