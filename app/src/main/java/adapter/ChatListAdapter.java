package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.List;

import cc.jimblog.imfriendchat.R;
import util.LogUtils;
import view.CircleImageView;

/**
 * 聊天页的Adapter
 * Created by Ran on 2016/8/14.
 * 稳定起见 采用了标准的BaseAdapter形式实现
 */
public class ChatListAdapter extends BaseAdapter {
    public Context mContext ;   //上下文
    public List<EMMessage> mMessageList ; //处理消息和接收消息的对象
    public LayoutInflater mInflater = null ;

    public ChatListAdapter(Context context , List<EMMessage> list){
        this.mContext = context ;
        this.mMessageList = list;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int i) {
        return mMessageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i ;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null ;
        EMMessage emMessage = mMessageList.get(i);

        if(convertView == null){
            viewHolder = new ViewHolder() ;
            convertView = mInflater.inflate(R.layout.layout_constaion_item,viewGroup);
            viewHolder.messageLeftText = (TextView) convertView.findViewById(R.id.chat_left_text);
            viewHolder.userLeftImg = (CircleImageView) convertView.findViewById(R.id.chat_left_img);
            viewHolder.tokenLeftLayout = (RelativeLayout) convertView.findViewById(R.id.chat_left_msg_layout);

            viewHolder.messageRightText = (TextView) convertView.findViewById(R.id.chat_right_text);
            viewHolder.userRightImg = (CircleImageView) convertView.findViewById(R.id.chat_right_img);
            viewHolder.tokenRightLayout = (RelativeLayout) convertView.findViewById(R.id.chat_right_msg_layout);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(emMessage.direct() == EMMessage.Direct.RECEIVE){
            viewHolder.tokenRightLayout.setVisibility(View.GONE);   //首先关闭右侧的视图
            viewHolder.tokenLeftLayout.setVisibility(View.VISIBLE); //并打开左侧视图
            //处理接收到的消息
            if(emMessage.getType() == EMMessage.Type.TXT){
                EMTextMessageBody textBody = (EMTextMessageBody) emMessage.getBody();
                String message = textBody.getMessage();
                viewHolder.messageLeftText.setText(message);
            }else if(emMessage.getType() == EMMessage.Type.IMAGE){
                EMImageMessageBody imageBody = (EMImageMessageBody) emMessage.getBody();
                String url = imageBody.getThumbnailUrl(); //调用此方法SDK会自动下载图片到本地,此URL为本地存储的路径
                //Bitmap downloadBitmap = BitmapFactory.decodeFile(url);
                LogUtils.i("图片地址为:"+url);
            }
        }else{
            viewHolder.tokenRightLayout.setVisibility(View.VISIBLE);
            viewHolder.tokenLeftLayout.setVisibility(View.GONE);
            //处理接收到的消息
            if(emMessage.getType() == EMMessage.Type.TXT){
                EMTextMessageBody textBody = (EMTextMessageBody) emMessage.getBody();
                String message = textBody.getMessage();
                viewHolder.messageRightText.setText(message);
            }else if(emMessage.getType() == EMMessage.Type.IMAGE){
                EMImageMessageBody imageBody = (EMImageMessageBody) emMessage.getBody();
                String url = imageBody.getThumbnailUrl(); //调用此方法SDK会自动下载图片到本地,此URL为本地存储的路径
                //Bitmap downloadBitmap = BitmapFactory.decodeFile(url);
                LogUtils.i("图片地址为:"+url);
            }
        }

        return convertView;
    }
    //与RecylerView不同的是 标准的BaseAdapter不会内部封装ViewHolder需要开发者手动实现
    class ViewHolder{
        public CircleImageView userLeftImg ;    //对方用户头像
        public TextView messageLeftText ;  //对方消息实体
        public RelativeLayout tokenLeftLayout ;   //对方Layout 作显示和隐藏

        public CircleImageView userRightImg ;   //自己用户头像
        public TextView messageRightText ;  //自己消息实体
        public RelativeLayout tokenRightLayout ;  //自己Layout 作显示和隐藏
    }
}
