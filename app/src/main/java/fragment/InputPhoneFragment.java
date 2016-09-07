package fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.squareup.leakcanary.RefWatcher;

import application.HuanXinApplication;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.jimblog.imfriendchat.R;
import util.JianPanUtils;
import util.StorageUtils;
import util.ToastUtils;
import view.RippleView;

/**
 * 输入帐号的Fragment页面
 * Created by Ran on 2016/8/8.
 */
public class InputPhoneFragment extends Fragment {
    @BindView(R.id.sign_input_phone)
    EditText signInputPhone;
    @BindView(R.id.sign_send_btn)
    RippleView signSendBtn;
    private View mView = null;

    public interface MyInputPhoneListener {
        void showMessage(int index);
    }

    private MyInputPhoneListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_input_phone, container,false);
        ButterKnife.bind(this, mView);
        JianPanUtils.openKeybord(signInputPhone,mView.getContext());    //自动弹出软键盘
        return mView;
    }

    @OnClick(R.id.sign_send_btn)
    public void onClick() {
        String phoneStr = signInputPhone.getText().toString().trim();
        if(phoneStr != null && !phoneStr.equals("")){
            StorageUtils.put(getContext(),"Account",phoneStr);
            mListener.showMessage(1);
        }else{
            ToastUtils.showShort(mView.getContext(),getString(R.string.register_input_number_edit_hint));
            setAnimation(signInputPhone);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MyInputPhoneListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getName()
                    +" must implements interface MyListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        signInputPhone.addTextChangedListener(new EditPhoneListener());
    }
    class EditPhoneListener implements TextWatcher{
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
            if(temp.length() == 0){
                signSendBtn.setBackgroundResource(R.drawable.login_not_btn_shape);
            }else if(temp.length() > 0 ){
                signSendBtn.setBackgroundResource(R.drawable.login_edit_btn_shape);
            }
        }
    }
    private void setAnimation(View v) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.view_shake);
        v.startAnimation(animation);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = HuanXinApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
