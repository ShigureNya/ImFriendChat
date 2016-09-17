package adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import org.json.JSONArray;

import java.io.File;
import java.util.List;

import cc.jimblog.imfriendchat.R;
import cc.jimblog.imfriendchat.TouchImageActivity;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.ContextSave;
import entity.UserInfoEntity;
import image.LocalCacheUtil;
import image.MyBitmapCacheUtil;
import util.BitmapUtils;
import util.LogUtils;
import view.CircleImageView;

/**
 * Created by jimhao on 16/9/14.
 */
public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.MyGroupChatAdapter> {
    private Context mContext ;
    private List<EMMessage> mList ;
    private LayoutInflater mInflater ;
    public MyBitmapCacheUtil bitmapCacheUtil ;
    public Gson gson ;
    public LocalCacheUtil localUtil ;
    public static Bitmap leftBitmap ;
    public static Bitmap rightBitmap ;

    public GroupChatAdapter(Context mContext, List<EMMessage> mList) {
        this.mContext = mContext;
        this.mList = mList;
        mInflater = LayoutInflater.from(mContext);
        bitmapCacheUtil = new MyBitmapCacheUtil();
        gson = new Gson();
        localUtil = new LocalCacheUtil();
    }

    @Override
    public MyGroupChatAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_constaion_item,parent,false);
        return new MyGroupChatAdapter(view);
    }

    @Override
    public void onBindViewHolder(MyGroupChatAdapter holder, int position) {
        EMMessage message = mList.get(position);
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
                //为图片设置Tag防止错误加载
                holder.messageLeftImage.setTag(url);
                if(holder.messageLeftImage.getTag()!=null && holder.messageLeftImage.getTag().equals(url)){
                    bitmapCacheUtil.disPlayImage(holder.messageLeftImage , url );
                }
            }
            String userId = message.getUserName();
            holder.userLeftImg.setTag(userId);
            holder.userLeftImg.setImageResource(R.mipmap.user_image);
            if(holder.userLeftImg.getTag()!=null && holder.userLeftImg.getTag().equals(userId)){
                if(leftBitmap == null){
                    queryLeftUserImg(userId,holder.userLeftImg);
                }else{
                    holder.userLeftImg.setImageBitmap(leftBitmap);
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
                String url = imageBody.getLocalUrl() ;
                //为图片设置Tag防止错误加载
                holder.messageRightImage.setTag(url);
                if(holder.messageRightImage.getTag()!=null && holder.messageRightImage.getTag().equals(url)){
                    bitmapCacheUtil.disPlayImage(holder.messageRightImage , url);
                }
            }
            String userId = EMClient.getInstance().getCurrentUser();
            holder.userRightImg.setTag(userId);
            holder.userRightImg.setImageResource(R.mipmap.user_image);
            if(holder.userRightImg.getTag()!=null && holder.userRightImg.getTag().equals(userId)){
                if(rightBitmap == null){
                    queryRightUserImg(userId,holder.userRightImg);
                }else{
                    holder.userRightImg.setImageBitmap(rightBitmap);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyGroupChatAdapter extends RecyclerView.ViewHolder{

        public CircleImageView userLeftImg ;    //对方用户头像
        public TextView messageLeftText ;  //对方消息实体
        public ImageView messageLeftImage ;     //对方消息图片实体
        public RelativeLayout tokenLeftLayout ;   //对方Layout 作显示和隐藏

        public CircleImageView userRightImg ;   //自己用户头像
        public TextView messageRightText ;  //自己消息实体
        public ImageView messageRightImage ;    //自己消息图片实体
        public RelativeLayout tokenRightLayout ;  //自己Layout 作显示和隐藏

        public MyGroupChatAdapter(View itemView) {
            super(itemView);
            messageLeftText = (TextView) itemView.findViewById(R.id.chat_left_text);
            userLeftImg = (CircleImageView) itemView.findViewById(R.id.chat_left_img);
            tokenLeftLayout = (RelativeLayout) itemView.findViewById(R.id.chat_left_msg_layout);
            messageLeftImage = (ImageView) itemView.findViewById(R.id.chat_left_text_img);

            messageRightText = (TextView) itemView.findViewById(R.id.chat_right_text);
            userRightImg = (CircleImageView) itemView.findViewById(R.id.chat_right_img);
            tokenRightLayout = (RelativeLayout) itemView.findViewById(R.id.chat_right_msg_layout);
            messageRightImage = (ImageView) itemView.findViewById(R.id.chat_right_text_img);
            messageLeftImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getPosition();
                    goToImageClick(position);
                }
            });
            messageRightImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getPosition();
                    goToImageClick(position);
                }
            });
        }
    }
    /**
     * 打开图像查看器
     * @param position
     */
    private void goToImageClick(int position){
        EMMessage message = mList.get(position);
        if(message.getType() == EMMessage.Type.IMAGE){
            EMImageMessageBody imageBody = (EMImageMessageBody) message.getBody();
            String url = imageBody.getThumbnailUrl().trim() ;
            if(url != null && !url.equals("") && message.direct() == EMMessage.Direct.RECEIVE){
                Intent intent = new Intent();
                intent.setClass(mContext, TouchImageActivity.class);
                intent.setData(Uri.fromFile(new File(LocalCacheUtil.getBitmapNameURL(url))));
                mContext.startActivity(intent);
            }else{
                String localUrl = imageBody.getLocalUrl();
                LogUtils.i("LocalURL:"+localUrl);
                Intent intent = new Intent();
                intent.setClass(mContext, TouchImageActivity.class);
                intent.setData(Uri.fromFile(new File(localUrl)));
                mContext.startActivity(intent);
            }
        }
    }
    /**
     * 查询Bmob服务器中的数据得到用户头像-左边消息
     * */
    private void queryLeftUserImg(String userId,final ImageView imageView){
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId",userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if(e == null){
                    if(jsonArray != null){
                        List<UserInfoEntity> userInfo = jsonToList(jsonArray.toString());
                        for(UserInfoEntity entity : userInfo){
                            boolean flag = entity.isDefImg();
                            if(flag){   //是否使用默认的用户头像
                                int position = Integer.parseInt(entity.getDefImgPosition());
                                LogUtils.d("Position"+position);
                                leftBitmap = BitmapUtils.getBitmapById(mContext, ContextSave.defPicArray[position]);
                                imageView.setImageBitmap(leftBitmap);
                            }else{
                                String url = entity.getUserImg().getUrl();
                                bitmapCacheUtil.disPlayImage(imageView,url);
                                leftBitmap = localUtil.getBitmapFromLocal(url);
                            }
                        }
                    }
                }else{
                    LogUtils.e("错误Chat:"+e.toString());
                }
            }
        });
    }
    /**
     * 查询Bmob服务器中的数据得到用户头像-右边消息
     * */
    private void queryRightUserImg(String userId,final ImageView imageView){
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId",userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                List<UserInfoEntity> userInfo = jsonToList(jsonArray.toString());
                for(UserInfoEntity entity : userInfo){
                    boolean flag = entity.isDefImg();
                    if(flag){   //是否使用默认的用户头像
                        int position = Integer.parseInt(entity.getDefImgPosition());
                        LogUtils.d("Position"+position);
                        rightBitmap = BitmapUtils.getBitmapById(mContext, ContextSave.defPicArray[position]);
                        imageView.setImageBitmap(rightBitmap);
                    }else{
                        String url = entity.getUserImg().getUrl();
                        bitmapCacheUtil.disPlayImage(imageView,url);
                        rightBitmap = localUtil.getBitmapFromLocal(url);
                    }
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
}
