package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.squareup.leakcanary.RefWatcher;

import application.HuanXinApplication;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.jimblog.imfriendchat.R;
import cc.jimblog.imfriendchat.TestActivity;
import cc.jimblog.imfriendchat.WebActivity;

/**
 * Created by Ran on 2016/8/10.
 */
public class FuncationFragment extends Fragment {
    @BindView(R.id.function_item_pyq)
    Button functionItemPyq;
    @BindView(R.id.function_item_hy)
    Button functionItemHy;
    @BindView(R.id.function_item_blog)
    Button functionItemBlog;
    @BindView(R.id.function_item_brows)
    Button functionItemBrows;
    @BindView(R.id.function_item_player)
    Button functionItemPlayer;
    @BindView(R.id.function_item_test)
    Button functionItemTest;
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_function, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @OnClick({R.id.function_item_pyq, R.id.function_item_hy, R.id.function_item_blog, R.id.function_item_brows, R.id.function_item_player,R.id.function_item_test})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.function_item_pyq:
                break;
            case R.id.function_item_hy:
                break;
            case R.id.function_item_blog:
                startActivity(new Intent(view.getContext(), WebActivity.class));
                break;
            case R.id.function_item_brows:
                Intent intent = new Intent();
                intent.putExtra("URL", "http://www.baidu.com");
                intent.setClass(view.getContext(), WebActivity.class);
                startActivity(intent);
                break;
            case R.id.function_item_player:

                break;
            case R.id.function_item_test:
                startActivity(new Intent(view.getContext(), TestActivity.class));
                break;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = HuanXinApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
