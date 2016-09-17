package fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.jimblog.imfriendchat.R;

/**
 * 系统设置页面
 * Created by Ran on 2016/8/19.
 */
public class SettingFragment extends Fragment {
    @BindView(R.id.setting_layout_logout)
    TextView settingLayoutLogout;
    @BindView(R.id.setting_snackbar_layout)
    CoordinatorLayout settingSnackbarLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.setting_layout_logout)
    public void onClick() {
        //此方法为异步方法
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                clickListener.onClick();
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub
                showSnackBar(getString(R.string.setting_loginout_error_hint));
            }
        });
    }
    /**
     * 展示SnackBar
     *
     * @param message 消息对象
     */
    public void showSnackBar(String message) {
        Snackbar.make(settingSnackbarLayout, message, 1500).show();
    }
    public interface OnLogOutClickListener {
        void onClick();
    }
    //传递给MainActivity 使其调用退出登录方法
    public OnLogOutClickListener clickListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            clickListener = (OnLogOutClickListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getName()
                    +" must implements interface MyListener");
        }
    }
}
