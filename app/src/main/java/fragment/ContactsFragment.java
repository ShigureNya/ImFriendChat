package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import adapter.ContactsRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import cc.jimblog.imfriendchat.ChatActivity;
import cc.jimblog.imfriendchat.R;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.LogUtils;
import view.DividerItemDecoration;

/**
 * Created by Ran on 2016/8/10.
 */
public class ContactsFragment extends Fragment {
    @BindView(R.id.contacts_list)
    RecyclerView contactList;
    private View mView;

    private List<String> mList = new ArrayList<String>() ;

    private ContactsRecyclerAdapter adapter ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, mView);
        initAdapter();
        return mView;
    }

    private void initAdapter() {
        //设置adapter
        adapter = new ContactsRecyclerAdapter(getContext(), mList);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        contactList.setLayoutManager(new LinearLayoutManager(getContext()));
        //设置Item的过渡动画，使用默认的即可
        contactList.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        contactList.setHasFixedSize(true);
        //为recyclerView设置分割线
        contactList.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        //加载数据
        contactList.setAdapter(adapter);
        //设置头部Adapter
        setHeaderView(contactList);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter.setItemClickListener(new ContactsRecyclerAdapter.OnContactsItemCLickListener() {
            @Override
            public void onClick(View view, int position) {
                String name = mList.get(position);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("Username",name);
                startActivity(intent);
            }
        });
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
                        LogUtils.i("好友列表加载完成");
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(List<String> strings) {
                        LogUtils.i("好友:"+strings);
                        if(strings != null){
                           for(String name : strings){
                               mList.add(name);
                           }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
    private void refreshData(){
        if(mList != null){
            mList.clear();
        }
        initData();
    }

    @Override
    public void onResume() {
        refreshData();
        super.onResume();
    }

    private void setHeaderView(RecyclerView view){
        View header = LayoutInflater.from(getContext()).inflate(R.layout.layout_contacts_add,view,false);
        adapter.setHeaderView(header);
    }
}
