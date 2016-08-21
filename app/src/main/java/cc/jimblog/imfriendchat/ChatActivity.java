package cc.jimblog.imfriendchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.util.List;

import adapter.ChatAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
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
    RelativeLayout chatEditLayout;
    @BindView(R.id.chat_list)
    RecyclerView chatList;
    private String userName;       //用户名

    private EMConversation conversation;    //联系人对象

    private ChatAdapter adapter;   //适配器

    private SwipeBackLayout mBackLayout ;   //侧滑关闭Activity所用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_beta);
        ButterKnife.bind(this);
        initToolBar();
        initAdapter();

        mBackLayout = getSwipeBackLayout();
        mBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
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
                if(content == null || content.equals("")){
                    return ;
                }
                //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
                final EMMessage message = EMMessage.createTxtSendMessage(content, userName);
                //发送消息
                EMClient.getInstance().chatManager().sendMessage(message);

                if(conversation == null){
                    conversation = EMClient.getInstance().chatManager().getConversation(userName);
                }
                //将消息对象放入conversation中
                conversation.appendMessage(message);

                adapter.notifyDataSetChanged();
                chatList.scrollToPosition(adapter.getItemCount()-1);

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
        toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.chat_menu_person:
                        ToastUtils.showShort(ChatActivity.this,"点击了按钮");
                        break;
                }
                return true ;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        //创建menu菜单
        return true ;
    }

    /**
     * 初始化adapter
     */
    private void initAdapter() {
        //从chatManager中取出conversation对象，需要传递当前聊天用户的名字
        conversation = EMClient.getInstance().chatManager().getConversation(userName);
        //设置adapter
        if(conversation != null){
            adapter = new ChatAdapter(this, conversation);
        }else{
            adapter = new ChatAdapter(this,null);
        }
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        chatList.setLayoutManager(new LinearLayoutManager(this));
        //设置Item的过渡动画，使用默认的即可
        chatList.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        chatList.setHasFixedSize(true);
        //加载数据
        chatList.setAdapter(adapter);
        //当前adapter的位置在最后一页
        if(conversation != null){
            chatList.scrollToPosition(adapter.getItemCount()-1);
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
                if(!userFrom.equals(userName)){
                    return ;
                }
                if(conversation == null){
                    conversation = EMClient.getInstance().chatManager().getConversation(userName);
                }
                conversation.appendMessage(emsg);
            }
            adapter.notifyDataSetChanged();
            chatList.scrollToPosition(adapter.getItemCount()-1);

            LogUtils.i("状态", "刷新了ListView");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    //拦截处理软键盘弹出事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    //关闭软键盘的重要方法
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    //关闭后使EditText失去焦点
                    chatEditEditText.clearFocus();
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * 使软键盘隐藏
     * @param v
     * @param event
     * @return
     */
    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
