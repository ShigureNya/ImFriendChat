package cc.jimblog.imfriendchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.util.ArrayList;
import java.util.List;

import adapter.ChatAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import util.JianPanUtils;
import util.LogUtils;
import util.ToastUtils;

/**
 * Created by Ran on 2016/8/11.
 */
public class ChatActivity extends SwipeBackActivity {

    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.chat_toolbar_layout)
    RelativeLayout chatToolbarLayout;
    @BindView(R.id.chat_edit_voice_btn)
    ImageButton chatEditVoiceBtn;
    @BindView(R.id.chat_edit_function_btn)
    ImageButton chatEditFunctionBtn;
    @BindView(R.id.chat_edit_send_btn)
    ImageButton chatEditSendBtn;
    @BindView(R.id.chat_edit_editText)
    EditText chatEditEditText;
    @BindView(R.id.chat_edit_layout)
    LinearLayout chatEditLayout;
    @BindView(R.id.chat_list)
    RecyclerView chatList;
    @BindView(R.id.chat_function_photo)
    Button chatFunctionPhoto;
    @BindView(R.id.chat_function_video)
    Button chatFunctionVideo;
    @BindView(R.id.chat_function_file)
    Button chatFunctionFile;
    @BindView(R.id.chat_function_location)
    Button chatFunctionLocation;
    @BindView(R.id.chat_function_layout)
    LinearLayout chatFunctionLayout;
    private String userName;       //用户名

    private EMConversation conversation;    //联系人对象

    private ChatAdapter adapter;   //适配器

    private List<EMMessage> mList ;     //数据集合
    private SwipeBackLayout mBackLayout;   //侧滑关闭Activity所用

    private int PIC_RESULT_CODE = 200 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_beta);
        ButterKnife.bind(this);
        initToolBar();
        initAdapter();
        //侧滑关闭Activity的重要方法
        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }


    @Override
    protected void onResume() {
        super.onResume();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        //捕获ChatList的点击事件
        chatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(chatFunctionLayout.getVisibility() == View.VISIBLE){
                    chatFunctionLayout.setVisibility(View.GONE);
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    //关闭软键盘的重要方法
                    imm.hideSoftInputFromWindow(chatEditEditText.getWindowToken(), 0);
                    //关闭后使EditText失去焦点
                    chatEditEditText.clearFocus();
                }
                return false;
            }
        });
        //拦截editText或得到焦点的事件
        chatEditEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(chatFunctionLayout.getVisibility() == View.VISIBLE){
                        chatFunctionLayout.setVisibility(View.GONE);
                    }
                    if(adapter != null && adapter.getItemCount()!= 0){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                chatList.scrollToPosition(adapter.getItemCount() - 1);
                            }
                        },200);
                    }
                }
            }
        });
        chatFunctionPhoto.setOnClickListener(new OnChatFunctionClickListener());
        chatFunctionFile.setOnClickListener(new OnChatFunctionClickListener());
        chatFunctionLocation.setOnClickListener(new OnChatFunctionClickListener());
        chatFunctionVideo.setOnClickListener(new OnChatFunctionClickListener());
    }

    @OnClick({R.id.chat_edit_voice_btn, R.id.chat_edit_function_btn, R.id.chat_edit_send_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chat_edit_voice_btn:
                break;
            case R.id.chat_edit_function_btn:
                JianPanUtils.closeKeybord(chatEditEditText, this);
                if (chatFunctionLayout.getVisibility() == View.VISIBLE) {
                    chatFunctionLayout.setVisibility(View.GONE);
                } else {
                    chatEditEditText.clearFocus();
                    chatFunctionLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.chat_edit_send_btn:
                String content = chatEditEditText.getText().toString();
                if (content == null || content.equals("")) {
                    return;
                }
                sendTextMessage(content);
                break;
        }
    }

    /**
     * 定义toolbar的相关属性
     */
    private void initToolBar() {
        userName = getIntent().getStringExtra("Username");
        LogUtils.i("ChatUserName",userName);
        toolBar.setTitle(userName);
        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
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
        toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chat_menu_person:
                        ToastUtils.showShort(ChatActivity.this, "点击了按钮");
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        //创建menu菜单
        return true;
    }

    /**
     * 初始化adapter
     */
    private void initAdapter() {
        //从chatManager中取出conversation对象，需要传递当前聊天用户的名字 获得聊天记录~
        conversation = EMClient.getInstance().chatManager().getConversation(userName);
        //重写消息读取策略
        if(conversation == null){
            mList = new ArrayList<EMMessage>();
        }else{
            mList = conversation.getAllMessages();
        }
        //设置adapter
        adapter = new ChatAdapter(this, mList);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        chatList.setLayoutManager(new LinearLayoutManager(this));
        //设置Item的过渡动画，使用默认的即可
        chatList.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        chatList.setHasFixedSize(true);
        //加载数据
        chatList.setAdapter(adapter);
        //当前adapter的位置在最后一页
        if (conversation != null) {
            chatList.scrollToPosition(adapter.getItemCount() - 1);
        }
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
                String userFrom = emsg.getFrom();
                if (!userFrom.equals(userName)) {
                    return;
                }
                mList.add(emsg);
                if(conversation != null){
                    conversation.appendMessage(emsg);
                }
            }
            adapter.notifyDataSetChanged();
            chatList.scrollToPosition(adapter.getItemCount() - 1);

            LogUtils.i("状态", "刷新了ListView");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    class OnChatFunctionClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.chat_function_photo:
                    Intent picture = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(picture,PIC_RESULT_CODE);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //通过RequestCode判断
        if(requestCode == PIC_RESULT_CODE && resultCode == Activity.RESULT_OK && null != data) {
            //发送图片的方法
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = this.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            LogUtils.i("PicPath:"+picturePath);
            sendPicMessage(picturePath);
        }
    }

    /**
     * 发送文本消息
     * @param content 文本
     */
    private void sendTextMessage(String content){
        //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        final EMMessage message = EMMessage.createTxtSendMessage(content, userName);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        mList.add(message);
        if(conversation != null){
            conversation.appendMessage(message);
        }
        adapter.notifyDataSetChanged();
        chatEditEditText.setText("");
        chatList.scrollToPosition(adapter.getItemCount() - 1);
    }

    /**
     * 发送图片消息
     * @param picPath 图片地址
     */
    private void sendPicMessage(String picPath){
        //imagePath为图片本地路径，false为不发送原图（默认超过100k的图片会压缩后发给对方），需要发送原图传true
        final EMMessage message = EMMessage.createImageSendMessage(picPath, true, userName);
        EMClient.getInstance().chatManager().sendMessage(message);
        mList.add(message);
        if(conversation != null){
            conversation.appendMessage(message);
        }
        adapter.notifyDataSetChanged();
        chatList.scrollToPosition(adapter.getItemCount()-1);
    }
}
