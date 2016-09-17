package fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.jimblog.imfriendchat.R;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import entity.UserInfoEntity;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.LogUtils;
import util.NetWorkUtils;
import util.StorageUtils;
import util.ToastUtils;
import view.GetVerificationCode;
import view.RippleView;

/**
 * 输入密码的Fragment页面
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
    @BindView(R.id.sign_progress_layout)
    RelativeLayout signProgressLayout;
    private View mView = null;
    private Bitmap codeMap = null; //验证码图片
    private String code = null; //验证码值
    public interface MyInputPwdListener {
        void closeMessage(int index);
    }

    private MyInputPwdListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        if (!NetWorkUtils.isConnected(mView.getContext())) {
            ToastUtils.showShort(getContext(), getString(R.string.login_connect_error_toast));
            return;
        }
        String password = signInputPwd.getText().toString().trim();
        String checkedPassword = signCheckedPwd.getText().toString().trim();

        if (password == null || password.equals("")) {
            ToastUtils.showShort(getContext(), getString(R.string.register_input_pwd_edit_hint));
            setAnimation(signInputPwd);
            return;
        }
        if (checkedPassword == null || checkedPassword.equals("")) {
            ToastUtils.showShort(getContext(), getString(R.string.register_input_code_hint));
            setAnimation(signCheckedPwd);
            return;
        }
        if (!password.equals(checkedPassword)) {
            ToastUtils.showShort(getContext(), getString(R.string.register_two_input_pwd_error_toast));
            setAnimation(signCheckedPwd);
            signCheckedPwd.setText("");
            return;
        }
        String codeStr = signInputCode.getText().toString().trim();

        if (codeStr == null || codeStr.equals("")) {
            ToastUtils.showShort(getContext(), getString(R.string.register_input_code_hint));
            setAnimation(signInputCode);
            return;
        }

        if (!codeStr.equalsIgnoreCase(code)) {
            ToastUtils.showShort(getContext(), getString(R.string.register_verificationCode_error_toast));
            setAnimation(signInputCode);
            signInputCode.setText("");
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        String account = (String) StorageUtils.get(getContext(), "Account", "");
        map.put("Account", account);
        map.put("Password", password);
        registerToHuanXin(map);
    }

    /**
     * 注册方法
     * @param map 封装了HashMap
     */
    private void registerToHuanXin(HashMap<String, String> map) {
        signProgressLayout.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimationFrom = new AlphaAnimation(0f,1.0f);
        alphaAnimationFrom.setDuration(700);
        signProgressLayout.setAnimation(alphaAnimationFrom);

        final String account = map.get("Account");
        final String password = map.get("Password");
        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                try {
                    EMClient.getInstance().createAccount(account, password);
                    subscriber.onCompleted();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    subscriber.onError(new Throwable(e.toString()));
                }
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                int defaultNum = buildRandomNum();
                //插入数据的方法
                UserInfoEntity entity = new UserInfoEntity();
                entity.setUserId(account);
                entity.setUserName(account);
                entity.setDefImg(true); //默认使用系统提供的头像
                entity.setDefImgPosition(String.valueOf(defaultNum));
                entity.setUserSign(getString(R.string.default_sign));
                entity.setSex(true);
                entity.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        AlphaAnimation alphaAnimationFrom = new AlphaAnimation(1.0f,0f);
                        alphaAnimationFrom.setDuration(700);
                        signProgressLayout.setAnimation(alphaAnimationFrom);
                        signProgressLayout.setVisibility(View.GONE);
                    }
                });
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mListener.closeMessage(2);
                    }
                },500,500);
            }

            @Override
            public void onError(Throwable throwable) {
                AlphaAnimation alphaAnimationFrom = new AlphaAnimation(1.0f,0f);
                alphaAnimationFrom.setDuration(700);
                signProgressLayout.setAnimation(alphaAnimationFrom);
                signProgressLayout.setVisibility(View.GONE);

                ToastUtils.showShort(mView.getContext(), "User already exist"+throwable.toString());
                LogUtils.e(throwable.toString());
            }

            @Override
            public void onNext(Integer integer) {

            }
        });
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MyInputPwdListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getName()
                    +" must implements interface MyListener");
        }
    }
    private boolean isShow = false;    //是否显示的标记

    public class InputPwdEditListener implements TextWatcher {
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
            if (temp.length() > 0) {
                if (!isShow) {
                    signCheckedLayout.setVisibility(View.VISIBLE);
                    startAnimationSet(signCheckedLayout);
                    isShow = true;
                }
            }
        }
    }

    /**
     * Layout动画
     * @param layout
     */
    private void startAnimationSet(LinearLayout layout) {
        LogUtils.d("以下方法已被执行");
        Animation translateAnimation = new TranslateAnimation(0, 0, -110f, 0f);
        Animation alphaAnimation = new AlphaAnimation(0f, 1.0f);
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
    private void initCodeBitmap(ImageView v) {
        if (codeMap != null) {
            codeMap.recycle();
            this.codeMap = null;
        }
        codeMap = GetVerificationCode.getInstance().createBitmap();
        if (codeMap != null) {
            v.setImageBitmap(codeMap);
            this.code = GetVerificationCode.getInstance().getCode();
        }
    }

    /**
     * 抖动动画
     * @param v
     */
    private void setAnimation(View v) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.view_shake);
        v.startAnimation(animation);
    }
    private int buildRandomNum(){
        Random random = new Random();
        int num = random.nextInt(12);
        return num ;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
