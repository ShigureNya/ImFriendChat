package cc.jimblog.imfriendchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

import adapter.GroupChatAdapter;
import adapter.GroupMembersAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.LogUtils;

/**
 * Created by jimhao on 16/9/13.
 */
public class GroupChatActivity extends AppCompatActivity {
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    @BindView(R.id.group_chat_edit_voice_btn)
    ImageButton groupChatEditVoiceBtn;
    @BindView(R.id.group_chat_edit_function_btn)
    ImageButton groupChatEditFunctionBtn;
    @BindView(R.id.group_chat_edit_send_btn)
    ImageButton groupChatEditSendBtn;
    @BindView(R.id.group_chat_edit_editText)
    EditText groupChatEditEditText;
    @BindView(R.id.group_chat_function_photo)
    Button groupChatFunctionPhoto;
    @BindView(R.id.group_chat_function_video)
    Button groupChatFunctionVideo;
    @BindView(R.id.group_chat_function_file)
    Button groupChatFunctionFile;
    @BindView(R.id.group_chat_function_location)
    Button groupChatFunctionLocation;
    @BindView(R.id.group_chat_function_layout)
    LinearLayout groupChatFunctionLayout;
    @BindView(R.id.group_chat_edit_layout)
    LinearLayout groupChatEditLayout;
    @BindView(R.id.group_chat_list)
    RecyclerView groupChatList;

    private String groupId = null;
    private EMGroup group;

    private SlidingMenu menu ;
    private GroupMembersAdapter adapter ;
    private GroupChatAdapter chatAdapter;
    private EMConversation converasation ;
    private List<String> mMembersList =  new ArrayList<String>();
    private List<EMMessage> chatLists ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);
        ButterKnife.bind(this);
        groupId = getIntent().getStringExtra("GroupId");
        LogUtils.i("GroupId:"+groupId);
        initToolbar();
        initSlidingMenu();
        initAdapter();
    }

    private void initToolbar() {
        String groupName = getIntent().getStringExtra("GroupName");
        int groupNum = getIntent().getIntExtra("GroupNum", 0);
        toolBar.setTitle(groupName + "(" + groupNum + ")");
        toolBar.setTitleTextColor(Color.WHITE);
        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, GroupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initAdapter(){
        converasation = EMClient.getInstance().chatManager().getConversation(groupId);
        if(converasation == null){
            chatLists = new ArrayList<EMMessage>();
        }else{
            chatLists = converasation.getAllMessages();
        }
        chatAdapter = new GroupChatAdapter(this,chatLists);
        groupChatList.setLayoutManager(new LinearLayoutManager(this));
        groupChatList.setItemAnimator(new DefaultItemAnimator());
        groupChatList.setHasFixedSize(true);
        groupChatList.setAdapter(chatAdapter);
        if(converasation != null){
            groupChatList.scrollToPosition(adapter.getItemCount()-1);
        }
    }
    private View mView ;
    private RecyclerView mGroupMembersChatList ;

    private void initSlidingMenu(){
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.RIGHT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.sliding_shadow);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        //为侧滑菜单设置布局
        menu.setMenu(R.layout.layout_sliding_menu);

        mView = menu.getMenu();
        mGroupMembersChatList = (RecyclerView) mView.findViewById(R.id.sliding_menu_list);
        //设置adapter
        adapter = new GroupMembersAdapter(mMembersList,GroupChatActivity.this);
        //通过New一个LinearLayoutManager的布局管理器对象来设置RecycleView的布局管理器
        mGroupMembersChatList.setLayoutManager(new LinearLayoutManager(GroupChatActivity.this));
        //设置Item的过渡动画，使用默认的即可
        mGroupMembersChatList.setItemAnimator(new DefaultItemAnimator());
        //设置item为固定大小
        mGroupMembersChatList.setHasFixedSize(true);
        //加载Adapter
        mGroupMembersChatList.setAdapter(adapter);
        //加载数据
        initSlidingMenuData();
    }
    @OnClick({R.id.group_chat_edit_voice_btn, R.id.group_chat_edit_function_btn, R.id.group_chat_edit_send_btn, R.id.group_chat_function_photo, R.id.group_chat_function_video, R.id.group_chat_function_file, R.id.group_chat_function_location})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.group_chat_edit_voice_btn:
                break;
            case R.id.group_chat_edit_function_btn:
                break;
            case R.id.group_chat_edit_send_btn:
                String content =  groupChatEditEditText.getText().toString().trim();
                if(content == null && content.equals("")){
                    return ;
                }
                sendTextMessage(content);
                break;
            case R.id.group_chat_function_photo:
                break;
            case R.id.group_chat_function_video:
                break;
            case R.id.group_chat_function_file:
                break;
            case R.id.group_chat_function_location:
                break;
        }
    }
    private void sendTextMessage(String content){
        //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(content, groupId);
        message.setChatType(EMMessage.ChatType.GroupChat);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        chatLists.add(message);
        if(converasation != null){
            converasation.appendMessage(message);
        }
        adapter.notifyDataSetChanged();
        groupChatEditEditText.setText("");
        groupChatList.scrollToPosition(adapter.getItemCount() - 1);
    }
    @Override
    protected void onResume() {
        super.onResume();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        //捕获ChatList的点击事件
        groupChatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(groupChatFunctionLayout.getVisibility() == View.VISIBLE){
                    groupChatFunctionLayout.setVisibility(View.GONE);
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    //关闭软键盘的重要方法
                    imm.hideSoftInputFromWindow(groupChatEditEditText.getWindowToken(), 0);
                    //关闭后使EditText失去焦点
                    groupChatEditEditText.clearFocus();
                }
                return false;
            }
        });
        //拦截editText或得到焦点的事件
        groupChatEditEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(groupChatFunctionLayout.getVisibility() == View.VISIBLE){
                        groupChatFunctionLayout.setVisibility(View.GONE);
                    }
                    if(adapter != null && adapter.getItemCount()!= 0){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                groupChatList.scrollToPosition(adapter.getItemCount() - 1);
                            }
                        },200);
                    }
                }
            }
        });
        groupChatEditEditText.addTextChangedListener(new MyEditContentListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_chat_menu, menu);
        return true;
    }
    private void initSlidingMenuData() {
        Observable<List<String>> observable = Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                //根据群组ID从服务器获取群组基本信息
                try {
                    EMGroup group = EMClient.getInstance().groupManager().getGroupFromServer(groupId);
                    List<String> members = group.getMembers();
                    subscriber.onNext(members);
                    subscriber.onCompleted();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    subscriber.onError(new Throwable("列表读取错误"));
                }
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onCompleted() {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(GroupChatActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<String> emGroups) {
                        mMembersList.clear();
                        for(String str : emGroups){
                            mMembersList.add(str);
                        }
                    }
                });
    }
    /**
     * 注册消息监听
     * */
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
                if (!userFrom.equals(groupId)) {
                    return;
                }
                chatLists.add(emsg);
                if(converasation != null){
                    converasation.appendMessage(emsg);
                }
            }
            chatAdapter.notifyDataSetChanged();
            groupChatList.scrollToPosition(adapter.getItemCount() - 1);

            LogUtils.i("状态", "刷新了ListView");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    /**
     * 输入框监听
     */
    class MyEditContentListener implements TextWatcher {
        private CharSequence temp ;
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            temp = charSequence;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(temp.length() == 0){
                groupChatEditSendBtn.setBackgroundResource(R.drawable.chat_edit_btn_not_shape);
            }else{
                groupChatEditSendBtn.setBackgroundResource(R.drawable.chat_edit_btn_shape);
            }
        }
    }
}
