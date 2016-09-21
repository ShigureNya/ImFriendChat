package fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import adapter.ChatRecyclerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import cc.jimblog.imfriendchat.ChatActivity;
import cc.jimblog.imfriendchat.GroupChatActivity;
import cc.jimblog.imfriendchat.PersonCenterActivity;
import cc.jimblog.imfriendchat.R;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.ContextSave;
import entity.DateEntity;
import entity.UserInfoEntity;
import image.MyBitmapCacheUtil;
import util.BitmapUtils;
import util.LogUtils;
import util.NetWorkUtils;
import util.StorageUtils;
import view.CircleImageView;

/**
 * 问题- 在设置顶后其他Item消失，为其添加数据库关联，打开APP自动加载本地信息列表
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

    private String mHeaderName ;
    private int [] refreshColors = new int[]{android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light};
    private Gson gson ;

    private MyBitmapCacheUtil cacheUtil ;
    private int currentSystemDays;
    private boolean isHeader = false ;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, mView);
        LogUtils.i("触发了:onCreateView");
        //刷新数据
        chatUpdate();
        gson = new Gson();
        cacheUtil = new MyBitmapCacheUtil();

        SimpleDateFormat formatter = new SimpleDateFormat ("dd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        currentSystemDays = Integer.parseInt(formatter.format(curDate));
        return mView;
    }
    //再Fragment加载完成后加载会话,判断可能由于此方法的原因导致Fragment被创建后就会重载List数据
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.i("触发了:onActivityCreate");
        if(NetWorkUtils.isNetworkAvailable(mView.getContext())){
            initData(); //初始化数据
            initUserHeaderView(chatListView); //初始化顶部布局
            getUserHeaderView();    //设置顶部布局
            //设置下拉刷新
            chatRefreshLayout.setOnRefreshListener(new OnRefreshListener());
            chatRefreshLayout.setColorScheme(refreshColors);
            //设置列表点击事件
            adapter.setOnItemClickListener(new OnItemClickListener());
            adapter.setOnItemLongClickListeners(new OnItemLongClickListeners());
        }
    }
    private void initData(){
        //先将会话列表清空
        conversationList.clear();
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
//        chatListView.addItemDecoration(new DividerItemDecoration(mView.getContext(), DividerItemDecoration.VERTICAL_LIST));
        //加载数据
        chatListView.setAdapter(adapter);
        int count = 0;
        for (int i = 0; i < conversationList.size(); i++) {
            count += conversationList.get(i).getUnreadMsgCount();
        }
    }
    private CardView mHeaderLayout ;
    private CircleImageView mHeaderImage;
    private TextView mHeaderUsername ;
    private TextView mHeaderContent ;
    private TextView mHeaderTime ;
    private View myHeaderUserView ;

    //从缓存中取出置顶用户
    private void getUserHeaderView(){
        mHeaderName = (String) StorageUtils.get(getContext(),"HeaderUserName","");
        if(mHeaderName!= null && !mHeaderName.equals("")){
            isHeader = true ;
            adapter.setmHeaderView(myHeaderUserView);
            refresh();
        }
    }
    //设置置顶用户
    private void setUserHeaderView(String userName){
        isHeader = true ;
        adapter.setmHeaderView(myHeaderUserView);
        mHeaderName = userName;
        StorageUtils.put(getContext(),"HeaderUserName",userName);
        refresh();
    }
    //将指定ID的用户置顶
    private void initUserHeaderView(RecyclerView view){
        myHeaderUserView = LayoutInflater.from(getContext()).inflate(R.layout.layout_chat_item, view, false);
        mHeaderLayout = (CardView) myHeaderUserView.findViewById(R.id.item_chat_layout);
        mHeaderImage = (CircleImageView) myHeaderUserView.findViewById(R.id.item_chat_userimg);
        mHeaderUsername = (TextView) myHeaderUserView.findViewById(R.id.item_chat_username);
        mHeaderContent = (TextView) myHeaderUserView.findViewById(R.id.item_chat_usermsg);
        mHeaderTime = (TextView) myHeaderUserView.findViewById(R.id.item_chat_time);
        mHeaderLayout.setCardBackgroundColor(Color.parseColor("#eceff1"));
        mHeaderLayout.setBackgroundResource(R.drawable.recycler_header_bg);
        mHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("Username", mHeaderName);
                startActivity(intent);
            }
        });
        mHeaderLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
                builder.setTitle(mHeaderName);
                builder.setItems(getResources().getStringArray(R.array.chat_header_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0 :
                                StorageUtils.put(getContext(),"HeaderUserName",""); //取消置顶时将HeaderUsername制空
                                isHeader= false ;
                                mHeaderName = null ;
                                adapter.disMissHeaderView();
                                refresh();
                                break;
                            case 1:
                                Intent startUserInfoIntent = new Intent(getActivity(), PersonCenterActivity.class);
                                startUserInfoIntent.putExtra("UserName",mHeaderName);
                                startActivity(startUserInfoIntent);
                                break;
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }
    /**
     * RecycleView点击事件回调
     */
    public class OnItemClickListener implements ChatRecyclerAdapter.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
            view.setBackgroundResource(R.drawable.recycler_bg);
            EMConversation conversation = conversationList.get(position);
            boolean isGroup = conversation.isGroup();   //如果是聊天室或者群聊的话则会返回True
            if(!isGroup){
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                String username = conversation.getUserName();
                LogUtils.i("UserName:"+username);
                intent.putExtra("Username", username);
                startActivity(intent);
            }else{
                Intent intent = new Intent(getActivity(), GroupChatActivity.class);
                String groupId = conversation.getUserName();
                //根据群组ID从服务器获取群组基本信息
                EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);
                String groupName = group.getGroupName();
                int groupNum = group.getMembers().size();
                intent.putExtra("GroupId",groupId);
                intent.putExtra("GroupName",groupName);
                intent.putExtra("GroupNum",groupNum);
                startActivity(intent);
            }
        }
    }

    /**
     * 长点击事件
     */
    public class OnItemLongClickListeners implements ChatRecyclerAdapter.OnItemLongClickListeners{

        @Override
        public void onLongItemClick(final View view, final int position) {
            final EMConversation userConversation = conversationList.get(position);
            final String userName = conversationList.get(position).getUserName();
            view.setBackgroundResource(R.drawable.recycler_bg);
            final AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
            if(userConversation.isGroup()){
                EMGroup group = EMClient.getInstance().groupManager().getGroup(userName);
                builder.setTitle(group.getGroupName());
                builder.setItems(getResources().getStringArray(R.array.chat_user_group_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0 :
                                Intent startUserInfoIntent = new Intent(getActivity(), PersonCenterActivity.class);
                                startUserInfoIntent.putExtra("UserName",userName);
                                startActivity(startUserInfoIntent);
                                break;
                            case 1:
                                delectConversation(userName);
                                break;
                        }
                    }
                });
            }else{
                builder.setTitle(userName);
                if(!isHeader){
                    builder.setItems(getResources().getStringArray(R.array.chat_user_list_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0 :
                                    setUserHeaderView(userName);
                                    break;
                                case 1:
                                    Intent startUserInfoIntent = new Intent(getActivity(), PersonCenterActivity.class);
                                    startUserInfoIntent.putExtra("UserName",userName);
                                    startActivity(startUserInfoIntent);
                                    break;
                                case 2 :
                                    delectConversation(userName);
                                    break;
                            }
                        }
                    });
                }else{
                    builder.setItems(getResources().getStringArray(R.array.chat_user_group_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0 :
                                    Intent startUserInfoIntent = new Intent(getActivity(), PersonCenterActivity.class);
                                    startUserInfoIntent.putExtra("UserName",userName);
                                    startActivity(startUserInfoIntent);
                                    break;
                                case 1:
                                    delectConversation(userName);
                                    break;
                            }
                        }
                    });
                }
            }
            builder.create().show();
        }
    }

    /**
     * 删除指定的用户会话 不删除消息 删除完成后刷新消息
     * @param username 删除的用户ID
     */
    private void delectConversation(String username){
        boolean isDelete = EMClient.getInstance().chatManager().deleteConversation(username,false);
        if(isDelete){
            refresh();
        }else{
            LogUtils.e("删除失败");
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
        Hashtable<String, EMConversation> conversations = (Hashtable<String, EMConversation>)
                EMClient.getInstance().chatManager().getAllConversations();

        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();

        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
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
            EMConversation conversation = sortItem.second;
            String name = conversation.getUserName();
            LogUtils.i("名字",name);
            if(name.equals("") ||  name.contentEquals("")){ //此处需要contentEquals方法去过滤空值内容
                break ;
            }
            //当HeaderName和当前的Name相等时为HeaderView填充数据 并清除当前数据
            if(mHeaderName!=null && name.equals(mHeaderName)){
                mHeaderUsername.setText(name);
                queryUserImg(name,mHeaderImage,mHeaderUsername);
                //设置消息
                mHeaderContent.setText(getMessage(conversation.getLastMessage()));
                //设置时间
                Date serverDate = new Date(conversation.getLastMessage().getMsgTime());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateEntity dateEntity = getTimeFormatToEntity(sdf.format(serverDate));
                if(dateEntity.getDay()-1 == currentSystemDays){
                    mHeaderTime.setText("昨天"+dateEntity.getHour()+":"+dateEntity.getMinite());
                }else if(dateEntity.getDay()-2 == currentSystemDays){
                    mHeaderTime.setText("前天"+dateEntity.getHour()+":"+dateEntity.getMinite());
                }else if(dateEntity.getDay() == currentSystemDays){
                    mHeaderTime.setText(dateEntity.getHour()+":"+dateEntity.getMinite());
                }else{
                    mHeaderTime.setText(dateEntity.getMonth()+"-"+dateEntity.getDay()+" "+dateEntity.getHour()+":"+dateEntity.getMinite());
                }
                continue;   //这里不能用break_(:з」∠)_
            }
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
        conversationList.clear();   //先将会话列表清空
        conversationList.addAll(loadConversationWithRecentChat());  //加载会话列表
        if (adapter != null) {
            adapter.notifyDataSetChanged(); //刷新adapter
        }
        chatRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.i("触发了:onResume");
        refresh();
    }

    /**
     * 注册聊天数据更新接口
     * */
    private void chatUpdate(){
        if(NetWorkUtils.isNetworkAvailable(mView.getContext())){
            EMClient.getInstance().chatManager().addMessageListener(msgListener);
        }
    }
    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            Message msg = mMsgHandler.obtainMessage() ;
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
    Handler mMsgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //收到消息
            conversationList.clear();   //先将会话列表清空
            conversationList.addAll(loadConversationWithRecentChat());  //加载会话列表
            adapter.notifyDataSetChanged();
            //试试放在Handler里
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    /**
     * 查询Bmob服务器中的数据得到用户头像
     * */
    private void queryUserImg(String userId,final ImageView imageView , final TextView textView){
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId",userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if(e == null){
                    List<UserInfoEntity> userInfo = jsonToList(jsonArray.toString());
                    for(UserInfoEntity entity : userInfo){
                        LogUtils.i("实体:"+entity.toString());
                        boolean flag = entity.isDefImg();
                        if(flag){   //是否使用默认的用户头像
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            Bitmap bitmap = BitmapUtils.getBitmapById(getContext(), ContextSave.defPicArray[position]);
                            imageView.setImageBitmap(bitmap);
                        }else{
                            String url = entity.getUserImg().getUrl();
                            cacheUtil.disPlayImage(imageView,url);
                        }
                        String name = entity.getUserName();
                        textView.setText(name);
                    }
                }else{
                    imageView.setImageResource(R.mipmap.user_image);
                }
            }
        });
    }
    /**
     * @param json 将JSON转换为List集合
     * @return 实体集合
     */
    public List<UserInfoEntity> jsonToList(String json) {
        LogUtils.i("Json数据:"+json);
        List<UserInfoEntity> entityList = gson.fromJson(json, new TypeToken<List<UserInfoEntity>>() {

        }.getType());
        return entityList;
    }
    /**
     * 手动栅格化时间
     * */
    private DateEntity getTimeFormatToEntity(String dateStr){
        DateEntity entity = new DateEntity();
        LogUtils.i(dateStr);
        String year = dateStr.substring(0,4);
        String month = dateStr.substring(5,7);
        String day = dateStr.substring(8,10);
        String hour = dateStr.substring(11,13);
        String minite = dateStr.substring(14,16);
        boolean isAM = false ;
        int hourInteger = Integer.parseInt(hour);
        if(hourInteger <= 12){
            isAM = true ;   //则为上午
        }else{
            isAM = false ;  //下午
        }
        entity.setAM(isAM);
        entity.setYear(year);
        entity.setMonth(Integer.parseInt(month));
        entity.setDay(Integer.parseInt(day));
        entity.setHour(hour);
        entity.setMinite(minite);
        return entity ;
    }
    /**
     * 得到消息
     * @param message 消息体对象
     * @return
     */
    public String getMessage(EMMessage message){
        String str = "" ;
        switch(message.getType()){
            //图片消息
            case IMAGE:{
                EMImageMessageBody imageBody = (EMImageMessageBody) message.getBody();
                str = "[图片]";
                break;
            }
            case TXT:{
                EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
                str = txtBody.getMessage();

                break;
            }
            case FILE:
                EMFileMessageBody fileBody = (EMFileMessageBody) message.getBody();
                str = "[文件]";
                break;
        }
        return str;
    }

}
