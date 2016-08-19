package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import cc.jimblog.imfriendchat.R;
import image.MyBitmapCacheUtil;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.LogUtils;
import view.CircleImageView;

/**
 * 使用RecyclerView重构
 * Created by Ran on 2016/8/18.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    public Context mContext ;
    public EMConversation mConversation ;
    public LayoutInflater mInflater ;
    public MyBitmapCacheUtil bitmapCacheUtil ;

    public ChatAdapter(Context context , EMConversation mConversation){
        mContext = context ;
        this.mConversation = mConversation ;

        if(mContext != null){
            mInflater = LayoutInflater.from(mContext);
        }
        bitmapCacheUtil = new MyBitmapCacheUtil();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_constaion_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EMMessage message = mConversation.getAllMessages().get(position);
        //如果是接收到的消息
        if(message.direct() == EMMessage.Direct.RECEIVE){
            holder.tokenRightLayout.setVisibility(View.GONE);   //首先关闭右侧的视图
            holder.tokenLeftLayout.setVisibility(View.VISIBLE); //并打开左侧视图
            //处理接收到的消息
            if(message.getType() == EMMessage.Type.TXT){
                holder.messageLeftImage.setVisibility(View.GONE);
                holder.messageLeftText.setVisibility(View.VISIBLE);
                EMTextMessageBody textBody = (EMTextMessageBody) message.getBody();
                String messageStr = textBody.getMessage();
                holder.messageLeftText.setText(messageStr);
            }else if(message.getType() == EMMessage.Type.IMAGE){
                holder.messageLeftImage.setVisibility(View.VISIBLE);
                holder.messageLeftText.setVisibility(View.GONE);
                EMImageMessageBody imageBody = (EMImageMessageBody) message.getBody();
                String url = imageBody.getThumbnailUrl() ;
                //设置默认替换的图片
                holder.messageLeftImage.setImageResource(R.mipmap.ic_launcher);
                //为图片设置Tag防止错误加载
                holder.messageLeftImage.setTag(url);
                if(holder.messageLeftImage.getTag()!=null && holder.messageLeftImage.getTag().equals(url)){
                    bitmapCacheUtil.disPlay(holder.messageLeftImage,url);
                }
            }
        }else{ //如果是自己发送的消息
            holder.tokenRightLayout.setVisibility(View.VISIBLE);
            holder.tokenLeftLayout.setVisibility(View.GONE);
            //处理接收到的消息
            if(message.getType() == EMMessage.Type.TXT){
                holder.messageRightImage.setVisibility(View.GONE);
                holder.messageRightText.setVisibility(View.VISIBLE);
                EMTextMessageBody textBody = (EMTextMessageBody) message.getBody();
                String messageStr = textBody.getMessage();
                holder.messageRightText.setText(messageStr);
            }else if(message.getType() == EMMessage.Type.IMAGE){
                holder.messageRightImage.setVisibility(View.VISIBLE);
                holder.messageRightText.setVisibility(View.GONE);
                EMImageMessageBody imageBody = (EMImageMessageBody) message.getBody();
                String url = imageBody.getThumbnailUrl() ;
                //设置默认替换的图片
                holder.messageRightImage.setImageResource(R.mipmap.ic_launcher);
                //为图片设置Tag防止错误加载
                holder.messageRightImage.setTag(url);
                if(holder.messageRightImage.getTag()!=null && holder.messageRightImage.getTag().equals(url)){
                    bitmapCacheUtil.disPlay(holder.messageRightImage,url);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mConversation.getAllMessages().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView userLeftImg ;    //对方用户头像
        public TextView messageLeftText ;  //对方消息实体
        public ImageView messageLeftImage ;     //对方消息图片实体
        public RelativeLayout tokenLeftLayout ;   //对方Layout 作显示和隐藏

        public CircleImageView userRightImg ;   //自己用户头像
        public TextView messageRightText ;  //自己消息实体
        public ImageView messageRightImage ;    //自己消息图片实体
        public RelativeLayout tokenRightLayout ;  //自己Layout 作显示和隐藏
        
        public ViewHolder(View itemView) {
            super(itemView);
            messageLeftText = (TextView) itemView.findViewById(R.id.chat_left_text);
            userLeftImg = (CircleImageView) itemView.findViewById(R.id.chat_left_img);
            tokenLeftLayout = (RelativeLayout) itemView.findViewById(R.id.chat_left_msg_layout);
            messageLeftImage = (ImageView) itemView.findViewById(R.id.chat_left_text_img);

            messageRightText = (TextView) itemView.findViewById(R.id.chat_right_text);
            userRightImg = (CircleImageView) itemView.findViewById(R.id.chat_right_img);
            tokenRightLayout = (RelativeLayout) itemView.findViewById(R.id.chat_right_msg_layout);
            messageRightImage = (ImageView) itemView.findViewById(R.id.chat_right_text_img);

        }
    }

}
