package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adapter.ChatRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import cc.jimblog.imfriendchat.ChatActivity;
import cc.jimblog.imfriendchat.R;
import util.LogUtils;
import view.DividerItemDecoration;

/**
 * Created by Ran on 2016/8/10.
 */
public class ChatFragment extends Fragment {
    @BindView(R.id.chat_listView)
    RecyclerView chatListView;
    @BindView(R.id.chat_refresh_layout)

    SwipeRefreshLayout chatRefreshLayout;

    //封装了本地的会话列表。包含用户头像，用户名和最后一条消息
    private List<EMConversation> conversationList = new ArrayList<EMConversation>();
    private View mView;
    //数据适配器
    private ChatRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, mView);
        //刷新数据
        chatUpdate();
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //直接加在会话列表
        conversationList.addAll(loadConversationWithRecentChat());
        //设置adapter
        adapter = new ChatRecyclerAdapter(mView.getContext(), conversationList);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        chatListView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        //设置Item的过渡动画，使用默认的即可
        chatListView.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        chatListView.setHasFixedSize(true);
        //设置分割线
        chatListView.addItemDecoration(new DividerItemDecoration(mView.getContext(), DividerItemDecoration.VERTICAL_LIST));
        //加载数据
        chatListView.setAdapter(adapter);
        //设置列表点击事件
        adapter.setOnItemClickListener(new OnItemClickListener());

        int count = 0;
        for (int i = 0; i < conversationList.size(); i++) {
            count += conversationList.get(i).getUnreadMsgCount();
        }
        LogUtils.v("Count Total", count + "");
        //设置下拉刷新
        chatRefreshLayout.setOnRefreshListener(new OnRefreshListener());

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * RecycleView点击事件回调
     */
    public class OnItemClickListener implements ChatRecyclerAdapter.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
            EMConversation conversation = conversationList.get(position);
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            String username = conversation.getUserName();
            intent.putExtra("Username", username);
            startActivity(intent);
        }
    }

    /**
     * Refresh刷新事件回调
     */
    public class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener{

        @Override
        public void onRefresh() {
            refresh();
        }
    }
    /**
     * 获得所有会话
     *
     * @return
     */
    private Collection<? extends EMConversation> loadConversationWithRecentChat() {
        Hashtable<String, EMConversation> conversations = (Hashtable<String, EMConversation>) EMClient.getInstance().chatManager().getAllConversations();

        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();

        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<Long, EMConversation>
                            (conversation.getLastMessage().getMsgTime(), conversation)
                    );
                }
            }
        }

        try {
            getConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    /**
     * 根据最后一条消息的时间排序
     *
     * @param sortList
     */
    private void getConversationByLastChatTime(List<Pair<Long, EMConversation>> sortList) {
        Collections.sort(sortList, new Comparator<Pair<Long, EMConversation>>() {

            @Override
            public int compare(Pair<Long, EMConversation> con1,
                               Pair<Long, EMConversation> con2) {
                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    public void refresh() {
        LogUtils.i("ConversationList:" + conversationList.size());
        conversationList.clear();   //先将会话列表清空
        conversationList.addAll(loadConversationWithRecentChat());  //加载会话列表
        if (adapter != null) {
            adapter.notifyDataSetChanged(); //刷新adapter
        }
        chatRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        //Fragment每次加载Resume方法时调用refrush方法
        refresh();
        super.onResume();
    }
    private void chatUpdate(){
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }
    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            //收到消息
            conversationList.clear();   //先将会话列表清空
            conversationList.addAll(loadConversationWithRecentChat());  //加载会话列表
            adapter.notifyDataSetChanged();
            //试试放在Handler里
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            //收到透传消息
        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> messages) {
            //收到已读回执
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            //收到已送达回执
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            //消息状态变动
        }
    };

    @Override
    public void onDestroyView() {
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        super.onDestroyView();
    }
}
