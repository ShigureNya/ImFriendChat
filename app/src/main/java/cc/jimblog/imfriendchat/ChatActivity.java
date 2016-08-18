package cc.jimblog.imfriendchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.List;

import adapter.ChatAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Ran on 2016/8/11.
 */
public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.chat_toolbar_layout)
    RelativeLayout chatToolbarLayout;
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
    @BindView(R.id.chat_list)
    RecyclerView chatList;
    private String userName;       //用户名

    private EMConversation conversation;    //联系人对象

    private ChatAdapter adapter;   //适配器

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_beta);
        ButterKnife.bind(this);
        insertStorgePermissionWrapper();
        initToolBar();
        initAdapter();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }
    // new 为Android6.0适配动态权限
    @TargetApi(Build.VERSION_CODES.M)
    private void insertStorgePermissionWrapper() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
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
                //发送消息
                sendMsg(content);
                chatEditEditText.setText("");
                break;
        }
    }
    private void sendMsg(String messageStr){
        //获取到与聊天人的会话对象。参数username为聊天人的userid或者groupid，后文中的username皆是如此
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(userName);
        //创建一条文本消息
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
        //设置消息body
        EMTextMessageBody txtBody = new EMTextMessageBody(messageStr);
        message.addBody(txtBody);
        //设置接收人
        message.setReceipt(userName);
        //把消息加入到此会话对象中
        conversation.appendMessage(message);

        adapter.notifyDataSetChanged();
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
        //设置adapter
        adapter = new ChatAdapter(this, conversation);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        chatList.setLayoutManager(new LinearLayoutManager(this));
        //设置Item的过渡动画，使用默认的即可
        chatList.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        chatList.setHasFixedSize(false);
        //加载数据
        chatList.setAdapter(adapter);
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
                //如果消息不是发送给当前会话则返回
                if(!emsg.getFrom().equals(userName)){
                    return ;
                }
                conversation.appendMessage(emsg);
            }
            adapter.notifyDataSetChanged();
            chatList.setAdapter(adapter);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatEditEditText.addTextChangedListener(new EditTextListener());
    }
    class EditTextListener implements TextWatcher{
        public CharSequence temp ;
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            temp = charSequence ;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(temp.length() >  0 ){
                chatEditSendBtn.setText(R.string.chat_send_btn_text);
            }
        }
    }
}
