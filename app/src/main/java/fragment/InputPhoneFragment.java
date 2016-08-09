package fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.andexert.library.RippleView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.jimblog.imfriendchat.R;

/**
 * Created by Ran on 2016/8/8.
 */
public class InputPhoneFragment extends Fragment {
    @BindView(R.id.sign_input_phone)
    EditText signInputPhone;
    @BindView(R.id.sign_send_btn)
    RippleView signSendBtn;
    private View mView = null;

    public interface MyInputPhoneListener {
        public void showMessage(int index);
    }

    private MyInputPhoneListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_input_phone, container);
        ButterKnife.bind(this, mView);
        return mView;
    }
    @OnClick(R.id.sign_send_btn)
    public void onClick() {
        String phoneStr = signInputPhone.getText().toString().trim();
        if(phoneStr != null && !phoneStr.equals("")){
            mListener.showMessage(1);
        }
    }

    /**
     * 判断宿主activity是否实现了接口MyListener
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MyInputPhoneListener) context;
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
}
