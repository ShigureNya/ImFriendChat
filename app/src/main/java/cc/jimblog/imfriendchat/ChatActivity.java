package cc.jimblog.imfriendchat;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.util.ArrayList;
import java.util.List;

import adapter.ChatListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import util.LogUtils;

/**
 * Created by Ran on 2016/8/11.
 */
public class ChatActivity extends AppCompatActivity {
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.chat_list)
    ListView chatList;
    @BindView(R.id.chat_edit_voice_btn)
    ImageButton chatEditVoiceBtn;
    @BindView(R.id.chat_edit_function_btn)
    ImageButton chatEditFunctionBtn;
    @BindView(R.id.chat_edit_send_btn)
    Button chatEditSendBtn;
    @BindView(R.id.chat_edit_editText)
    EditText chatEditEditText;
    @BindView(R.id.chat_edit_layout)
    RelativeLayout chatEditLayout;
    @BindView(R.id.chat_layout)
    DrawerLayout chatLayout;

    private String userName;       //用户名

    private EMConversation conversation;    //联系人对象

    private ChatListAdapter adapter;   //适配器

    private List<EMMessage> mList = new ArrayList<EMMessage>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        initToolBar();
        initAdapter();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        //将chatList设置为展示最后一条消息
        if (chatList != null) {
            chatList.setSelection(chatList.getCount() - 1);
        }
        chatList.setDivider(null);
    }

    @OnClick({R.id.chat_edit_voice_btn, R.id.chat_edit_function_btn, R.id.chat_edit_send_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chat_edit_voice_btn:
                break;
            case R.id.chat_edit_function_btn:
                break;
            case R.id.chat_edit_send_btn:
                String content = chatEditEditText.getText().toString();
                //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
                final EMMessage message = EMMessage.createTxtSendMessage(content, userName);
                //发送消息
                EMClient.getInstance().chatManager().sendMessage(message);
                mList.add(message);
                adapter.notifyDataSetChanged();
                chatList.setSelection(chatList.getCount() - 1);
                chatEditEditText.setText("");
                break;
        }
    }

    /**
     * 定义toolbar的相关属性
     */
    private void initToolBar() {
        userName = getIntent().getStringExtra("Username");
        toolBar.setTitle(userName);
        toolBar.setNavigationIcon(R.mipmap.ic_keyboard_arrow_left_white_36dp);
        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 初始化adapter
     */
    private void initAdapter() {
        //从chatManager中取出conversation对象，需要传递当前聊天用户的名字
        conversation = EMClient.getInstance().chatManager().getConversation(userName);
        //将conversation对象的EMMessage的List直接交给adapter初始化
        adapter = new ChatListAdapter(getApplicationContext(), mList);
        chatList.setAdapter(adapter);
        chatList.setSelection(chatList.getCount() - 1);   //当前adapter的位置在最后一页

        for (EMMessage msg : conversation.getAllMessages()) {     //从消息中遍历出每一条消息数据
            mList.add(msg);
        }
        adapter.notifyDataSetChanged(); //刷新数据
    }

    /**
     * 初始化广播接收器对象
     */
    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            //收到消息
            Message msg = mHandler.obtainMessage();
            msg.obj = messages;
            msg.sendToTarget();
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
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            List<EMMessage> list = (List<EMMessage>) msg.obj;
            //收到消息
            for (EMMessage emsg : list) {
                mList.add(emsg);
            }
            adapter.notifyDataSetChanged();
            chatList.setSelection(chatList.getCount() - 1);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }
}
