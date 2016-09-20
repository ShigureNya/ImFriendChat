package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import view.CircleImageView;

/**
 * 会话页面Adapter适配器
 * Created by Ran on 2016/8/11.
 */
public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {
    private Context mContext ;
    private List<EMConversation> mConversationList ;
    private LayoutInflater mInflater ;
    private List<EMConversation> copyConversationList ;

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;

    private Gson gson ;

    private MyBitmapCacheUtil cacheUtil ;

    private int currentSystemDays;

    private View mHeaderView ;

    public View getmHeaderView() {
        return mHeaderView;
    }

    public void setmHeaderView(View mHeaderView) {
        this.mHeaderView = mHeaderView;
        notifyItemInserted(0);
    }
    public void disMissHeaderView(){
        this.mHeaderView = null ;
        notifyItemInserted(0);
    }
    //判断ItemViewType来添加HeaderView
    @Override
    public int getItemViewType(int position) {
        if(mHeaderView == null){
            return TYPE_NORMAL;
        }
        if(position == 0){
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    //    此处加载布局
    public ChatRecyclerAdapter(Context mContext,List<EMConversation> list){
        this.mContext = mContext;
        //还需要加载会话列表实体
        mInflater = LayoutInflater.from(mContext);
        if(list != null){
            mConversationList = list ;
        }
        gson = new Gson();
        cacheUtil = new MyBitmapCacheUtil();

        SimpleDateFormat formatter = new SimpleDateFormat ("dd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        currentSystemDays = Integer.parseInt(formatter.format(curDate));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER){
            return new ViewHolder(mHeaderView);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_item,parent,false);
        return new ViewHolder(v);
    }

    //    建立ViewHolder的数据关联
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(getItemViewType(position) == TYPE_HEADER){
            return ;
        }
        holder.mLayout.setBackgroundResource(R.drawable.recycler_bg);
        EMConversation conversation = null ;
        if(mHeaderView!=null){
            conversation = (EMConversation) getItem(position-1);
        }else{
            conversation = (EMConversation) getItem(position);
        }
        //设置用户名显示  - update 更新群组设置
        String nickname = null ;
        if(conversation.isGroup()){
            String groupId = conversation.getUserName();
            //根据群组ID从服务器获取群组基本信息
            EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);
            nickname = group.getGroupName();
        }else{
            nickname = conversation.getUserName();
        }
        holder.mUserName.setText(nickname);
        //设置Message
        if(conversation.getAllMsgCount() != 0){
            //设置消息
            holder.mUserMsg.setText(getMessage(conversation.getLastMessage()));
            //设置时间
            Date serverDate = new Date(conversation.getLastMessage().getMsgTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            DateEntity dateEntity = getTimeFormatToEntity(sdf.format(serverDate));
            LogUtils.i(nickname+"当前日期",dateEntity.getDay()+"");
             int dateDay = dateEntity.getDay();
            if(currentSystemDays - dateDay == 1){
                holder.mMessageTime.setText("昨天"+dateEntity.getHour()+":"+dateEntity.getMinite());
            }else if(currentSystemDays - dateDay == 2){
                holder.mMessageTime.setText("前天"+dateEntity.getHour()+":"+dateEntity.getMinite());
            }else if(dateDay == currentSystemDays){
                holder.mMessageTime.setText(dateEntity.getHour()+":"+dateEntity.getMinite());
            }else{
                holder.mMessageTime.setText(dateEntity.getMonth()+"-"+dateEntity.getDay()+"\t"+dateEntity.getHour()+":"+dateEntity.getMinite());
            }
        }
        holder.mUserImg.setTag(nickname);
        holder.mUserImg.setImageResource(R.mipmap.user_image);
        if(holder.mUserImg.getTag()!=null && holder.mUserImg.getTag().equals(nickname)){
            queryUserImg(nickname,holder.mUserImg,holder.mUserName);
        }
    }

    public int getRealPosition(RecyclerView.ViewHolder holder){
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position -1 ;
    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? mConversationList.size() : mConversationList.size()+1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener{
        public TextView mUserName ;
        public TextView mUserMsg ;
        public TextView mMessageTime ;
        public CircleImageView mUserImg ;
        public CardView mLayout ;
        public ViewHolder(View itemView) {
            super(itemView);
            if(itemView == mHeaderView){
                return ;
            }
            mUserName = (TextView) itemView.findViewById(R.id.item_chat_username);
            mUserMsg = (TextView) itemView.findViewById(R.id.item_chat_usermsg);
            mMessageTime = (TextView) itemView.findViewById(R.id.item_chat_time);
            mUserImg = (CircleImageView) itemView.findViewById(R.id.item_chat_userimg);
            mLayout = (CardView) itemView.findViewById(R.id.item_chat_layout);
            mLayout.setOnClickListener(this);
            mLayout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //需要加入非空判断
            if(onItemClickListener != null){
                if(mHeaderView!=null){
                    onItemClickListener.onItemClick(view,getPosition()-1);
                }else{
                    onItemClickListener.onItemClick(view,getPosition());
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(onItemLongClickListener!=null){
                if(mHeaderView!=null){
                    onItemLongClickListener.onLongItemClick(view,getPosition()-1);
                }else{
                    onItemLongClickListener.onLongItemClick(view,getPosition());
                }
            }
            return true;
        }
    }
    //Item点击事件定义接口回调
    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }
    public OnItemClickListener onItemClickListener ;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener ;
    }
    public Object getItem(int position) {
        return mConversationList.get(position);
    }

    //Item长点击事件定义接口回调
    public interface OnItemLongClickListeners{
        void onLongItemClick(View view , int position);
    }
    public OnItemLongClickListeners onItemLongClickListener;

    public void setOnItemLongClickListeners(OnItemLongClickListeners longClickListener){
        this.onItemLongClickListener = longClickListener;
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
    /**
     * 查询Bmob服务器中的数据得到用户头像
     * */
    private synchronized void queryUserImg(String userId,final ImageView imageView , final TextView textView){
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
                            Bitmap bitmap = BitmapUtils.getBitmapById(mContext, ContextSave.defPicArray[position]);
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

}
