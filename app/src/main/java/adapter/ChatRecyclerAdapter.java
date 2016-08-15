package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cc.jimblog.imfriendchat.R;
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
//    此处加载布局
    public ChatRecyclerAdapter(Context mContext,List<EMConversation> list){
        this.mContext = mContext;
        //还需要加载会话列表实体
        mInflater = LayoutInflater.from(mContext);
        if(list != null){
            mConversationList = list ;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_item,parent,false);
        return new ViewHolder(v);
    }

    //    建立ViewHolder的数据关联
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EMConversation conversation = (EMConversation) getItem(position);
        //设置用户名显示
        String nickname = conversation.getUserName();
        holder.mUserName.setText(nickname);
        //设置Message
        if(conversation.getAllMsgCount() != 0){
            //设置消息
            holder.mUserMsg.setText(getMessage(conversation.getLastMessage()));
            //设置时间
            Date date = new Date(conversation.getLastMessage().getMsgTime());
            //此处最好可以优化一下  优化为当天则显示时间  昨天则显示 昨天12:50 前天12:50  在此之前则显示日期2016-12-2
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            holder.mMessageTime.setText(sdf.format(date));
        }
        /**
         * 测试用例
         */
        Log.v("count", conversation.getUnreadMsgCount()+"");
    }

    @Override
    public int getItemCount() {
        return mConversationList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mUserName ;
        public TextView mUserMsg ;
        public TextView mMessageTime ;
        public CircleImageView mUserImg ;
        public RelativeLayout mLayout ;
        public ViewHolder(View itemView) {
            super(itemView);
            mUserName = (TextView) itemView.findViewById(R.id.item_chat_username);
            mUserMsg = (TextView) itemView.findViewById(R.id.item_chat_usermsg);
            mMessageTime = (TextView) itemView.findViewById(R.id.item_chat_time);
            mUserImg = (CircleImageView) itemView.findViewById(R.id.item_chat_userimg);
            mLayout = (RelativeLayout) itemView.findViewById(R.id.item_chat_layout);
            mLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //需要加入非空判断
            if(onItemClickListener != null){
                onItemClickListener.onItemClick(view,getPosition());
            }
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
                str = "[picture]" + imageBody.getFileName();
                break;
            }
            case TXT:{
                EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
                str = txtBody.getMessage();

                break;
            }
            case FILE:
                EMFileMessageBody fileBody = (EMFileMessageBody) message.getBody();
                str = "[File]" + fileBody.getFileName();
                break;
        }
        return str;
    }

}
