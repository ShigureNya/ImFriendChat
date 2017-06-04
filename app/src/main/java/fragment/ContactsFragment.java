package fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import adapter.ContactsRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import cc.jimblog.imfriendchat.ChatActivity;
import cc.jimblog.imfriendchat.FwhActivity;
import cc.jimblog.imfriendchat.GroupActivity;
import cc.jimblog.imfriendchat.NewFriendActivity;
import cc.jimblog.imfriendchat.PersonCenterActivity;
import cc.jimblog.imfriendchat.R;
import entity.ContextSave;
import util.LogUtils;
import util.NetWorkUtils;
import util.ToastUtils;

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

    private int [] refreshColors = new int[]{android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, mView);
        initAdapter();
        return mView;
    }

    /**
     * 初始化Adapter对象
     */
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
//        contactList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        //加载数据
        contactList.setAdapter(adapter);
        //设置头部Adapter
        setHeaderView(contactList);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //如网络正常连接则加载数据
        if(NetWorkUtils.isNetworkAvailable(mView.getContext())){
            EMClient.getInstance().contactManager().setContactListener(friendListener);
            refreshData();
            adapter.setItemClickListener(new ContactsRecyclerAdapter.OnContactsItemCLickListener() {
                @Override
                public void onClick(View view, int position) {
                    view.setBackgroundResource(R.drawable.recycler_bg);
                    String name = mList.get(position);
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("Username", name);
                    startActivity(intent);
                }
            });
            adapter.setOnContactsItemLongClickListener(new OnContactsItemLongClickListener());
        }

        contactsRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        contactsRefresh.setColorScheme(refreshColors);
    }

    class OnContactsItemLongClickListener implements ContactsRecyclerAdapter.OnContactsItemLongClickListener{

        @Override
        public void onLongClick(View view, final int position) {
            LogUtils.i("触发了长点击");
            view.setBackgroundResource(R.drawable.recycler_bg);
            final AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
            builder.setTitle(mList.get(position));
            builder.setItems(R.array.contact_long_function_array, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String name = mList.get(position);
                    switch (i){
                        case 0 :
                            //发起会话
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("Username", name);
                            startActivity(intent);
                            break;
                        case 1:
                            Intent startUserInfoIntent = new Intent(getActivity(), PersonCenterActivity.class);
                            startUserInfoIntent.putExtra("UserName",name);
                            startActivity(startUserInfoIntent);
                            break;
                        case 2:
                            //加入黑名单
                            //第二个参数如果为true，则把用户加入到黑名单后双方发消息时对方都收不到；
                            // false，则我能给黑名单的中用户发消息，但是对方发给我时我是收不到的
                            try {
                                EMClient.getInstance().contactManager().addUserToBlackList(name,true);
                                ToastUtils.showShort(mView.getContext(),getString(R.string.contact_add_ok_black_port_toast));
                            } catch (HyphenateException e) {
                                ToastUtils.showShort(mView.getContext(),getString(R.string.contact_add_error_black_port_toast));
                                e.printStackTrace();
                            }
                            refreshData();
                            break;
                        case 3:
                            //删除好友
                            try {
                                EMClient.getInstance().contactManager().deleteContact(name);
                                ToastUtils.showShort(mView.getContext(),getString(R.string.contact_del_ok_toast));
                            } catch (HyphenateException e) {
                                ToastUtils.showShort(mView.getContext(),getString(R.string.contact_del_error_toast));
                                e.printStackTrace();
                            }
                            refreshData();
                            break;
                    }
                }
            });
            builder.create().show();
        }
    }
    /**
     * 初始化数据
     */
    private void initData() {
        InitContactsThread thread = new InitContactsThread();
        thread.start();
    }

    /**
     * 初始化联系人数据线程
     */
    class InitContactsThread extends Thread{
        @Override
        public void run() {
            Message msg = initContactsHandler.obtainMessage() ;
            try {
                //从环信SDK中得到对应联系人数据
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

    /**
     * 处理由Thread传递过来的联系人消息
     */
    Handler initContactsHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 1 && msg.obj != null){
                List<String> contactsLists = (List<String>) msg.obj;
                ContextSave.friendList = contactsLists;
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

    /**
     * 刷新数据
     */
    private void refreshData() {
        if (mList != null) {
            mList.clear();
        }
        initData();
    }

    private RelativeLayout addNewFriendlayout ;
    private RelativeLayout myGroupLayout ;
    private RelativeLayout myFwhLayout ;

    /**
     * @param view RelativeLayout的头部Layout
     */
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
                startActivity(new Intent(getActivity(), GroupActivity.class));
                break;
            case R.id.item_contacts_fwh_layout:
                startActivity(new Intent(getActivity(), FwhActivity.class));
                break;
        }

    }
    Handler mMsgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //收到消息
            refreshData();
        }
    };

    EMContactListener friendListener = new EMContactListener(){

        @Override
        public void onContactInvited(String username, String reason) {
            //收到好友邀请
        }

        @Override
        public void onFriendRequestAccepted(String s) {
            //好友请求被同意
            Message msg = mMsgHandler.obtainMessage() ;
            msg.sendToTarget();
        }

        @Override
        public void onFriendRequestDeclined(String s) {

        }

        @Override
        public void onContactDeleted(String username) {
            //被删除时回调此方法
            //好友请求被同意
            Message msg = mMsgHandler.obtainMessage() ;
            msg.sendToTarget();
        }


        @Override
        public void onContactAdded(String username) {
            //增加了联系人时回调此方法
        }
    };
}
