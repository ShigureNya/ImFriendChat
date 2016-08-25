package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import adapter.ContactsRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import cc.jimblog.imfriendchat.ChatActivity;
import cc.jimblog.imfriendchat.NewFriendActivity;
import cc.jimblog.imfriendchat.R;
import view.DividerItemDecoration;

/**
 * Created by Ran on 2016/8/10.
 */
public class ContactsFragment extends Fragment implements View.OnClickListener{
    @BindView(R.id.contacts_list)
    RecyclerView contactList;
    @BindView(R.id.contacts_refresh)
    SwipeRefreshLayout contactsRefresh;
    private View mView;

    private List<String> mList = new ArrayList<String>();

    private ContactsRecyclerAdapter adapter;

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
        contactList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        //加载数据
        contactList.setAdapter(adapter);
        //设置头部Adapter
        setHeaderView(contactList);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        refreshData();
        adapter.setItemClickListener(new ContactsRecyclerAdapter.OnContactsItemCLickListener() {
            @Override
            public void onClick(View view, int position) {
                String name = mList.get(position);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("Username", name);
                startActivity(intent);
            }
        });

        contactsRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    private void initData() {
        InitContactsThread thread = new InitContactsThread();
        thread.start();
    }

    class InitContactsThread extends Thread{
        @Override
        public void run() {
            Message msg = initContactsHandler.obtainMessage() ;
            try {
                List<String> list = EMClient.getInstance().contactManager().getAllContactsFromServer();
                msg.obj = list;
                msg.arg1 = 1;
            } catch (HyphenateException e) {
                e.printStackTrace();
                msg.obj = null ;
                msg.arg1 = 0 ;
            }
            msg.sendToTarget();
        }
    }
    Handler initContactsHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 1 && msg.obj != null){
                List<String> contactsLists = (List<String>) msg.obj;
                if (contactsLists != null) {
                    for (String name : contactsLists) {
                        if(!mList.contains(name)){
                            mList.add(name);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    contactsRefresh.setRefreshing(false);
                }
            }
        }
    };
    private void refreshData() {
        if (mList != null) {
            mList.clear();
        }
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private RelativeLayout addNewFriendlayout ;
    private RelativeLayout myGroupLayout ;
    private RelativeLayout myFwhLayout ;
    private void setHeaderView(RecyclerView view) {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.layout_contacts_add, view, false);
        adapter.setHeaderView(header);
        addNewFriendlayout = (RelativeLayout) header.findViewById(R.id.item_contacts_new_friend_layout);
        myGroupLayout = (RelativeLayout) header.findViewById(R.id.item_contacts_group_layout);
        myFwhLayout = (RelativeLayout) header.findViewById(R.id.item_contacts_fwh_layout);
        addNewFriendlayout.setOnClickListener(this);
        myGroupLayout.setOnClickListener(this);
        myFwhLayout.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.item_contacts_new_friend_layout:
                startActivity(new Intent(getActivity(), NewFriendActivity.class));
                break;
            case R.id.item_contacts_group_layout:

                break;
            case R.id.item_contacts_fwh_layout:

                break;
        }

    }
}
