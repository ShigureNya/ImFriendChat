package fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.jimblog.imfriendchat.R;

/**
 * Created by Ran on 2016/8/10.
 */
public class FuncationFragment extends Fragment {
    private View mView ;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_function,container,false);
        return mView;
    }
}
