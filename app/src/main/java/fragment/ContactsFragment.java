package fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.jimblog.imfriendchat.R;
import cc.solart.wave.WaveSideBarView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.LogUtils;

/**
 * Created by Ran on 2016/8/10.
 */
public class ContactsFragment extends Fragment {
    @BindView(R.id.contacts_list)
    RecyclerView contactList;
    @BindView(R.id.side_view)
    WaveSideBarView sideView;
    private View mView;

    private List<String> mList ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, mView);
        initData();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void initData(){
        Observable<List<String>> observable = Observable.create(new Observable.OnSubscribe<List<String>>(){

            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                try {
                    List<String> list = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    subscriber.onError(new Throwable(e.toString()));
                }
            }
        });
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(List<String> strings) {
                        LogUtils.i("好友:"+strings);
                    }
                });
    }

    @Override
    public void onResume() {
        initData();
        super.onResume();
    }
}
