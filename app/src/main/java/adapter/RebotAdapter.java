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

import com.hyphenate.chat.EMClient;

import org.json.JSONArray;

import java.util.List;

import cc.jimblog.imfriendchat.R;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.ContextSave;
import entity.RebotEntity;
import entity.UserInfoEntity;
import image.MyBitmapCacheUtil;
import util.BitmapUtils;
import util.JsonUtil;
import util.LogUtils;
import view.CircleImageView;

/**
 * Created by jimhao on 16/9/4.
 */
public class RebotAdapter extends RecyclerView.Adapter<RebotAdapter.MyViewHolder>{
    private List<RebotEntity> mList ;
    private Context mContext ;
    private LayoutInflater mInflater ;
    private MyBitmapCacheUtil cacheUtil;
    public RebotAdapter(Context mContext,List<RebotEntity> mList) {
        this.mList = mList;
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
        cacheUtil = new MyBitmapCacheUtil();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_constaion_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RebotEntity entity = mList.get(position);
        String text = entity.getMsg();
        if(entity.getType() == RebotEntity.Type.INPUT){    //收到的
            holder.tokenRightLayout.setVisibility(View.GONE);
            holder.tokenLeftLayout.setVisibility(View.VISIBLE);
            holder.messageLeftText.setText(text);
            holder.userLeftImg.setImageResource(R.drawable.rebot_nako);
        }else{      //自己发送的
            holder.tokenLeftLayout.setVisibility(View.GONE);
            holder.tokenRightLayout.setVisibility(View.VISIBLE);
            holder.messageRightText.setText(text);
            holder.userRightImg.setImageBitmap(ContextSave.userBitmap);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView userLeftImg ;    //对方用户头像
        public TextView messageLeftText ;  //对方消息实体
        public ImageView messageLeftImage ;     //对方消息图片实体
        public RelativeLayout tokenLeftLayout ;   //对方Layout 作显示和隐藏

        public CircleImageView userRightImg ;   //自己用户头像
        public TextView messageRightText ;  //自己消息实体
        public ImageView messageRightImage ;    //自己消息图片实体
        public RelativeLayout tokenRightLayout ;  //自己Layout 作显示和隐藏

        public MyViewHolder(View itemView) {
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
    /**
     * 查询并设置用户头像
     *
     * @param imageView
     */
    private void queryUserInfoImage(final ImageView imageView) {
        String userId = EMClient.getInstance().getCurrentUser();
        ContextSave.userId = userId;
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId", userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if (e == null) {
                    List<UserInfoEntity> userInfo = new JsonUtil().jsonToList(jsonArray.toString());
                    for (UserInfoEntity entity : userInfo) {
                        boolean flag = entity.isDefImg();
                        if (flag) {   //是否使用默认的用户头像
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            LogUtils.d("Position" + position);
                            Bitmap bitmap = BitmapUtils.getBitmapById(mContext, ContextSave.defPicArray[position]);
                            imageView.setImageBitmap(bitmap);
                        } else {
                            String url = entity.getUserImg().getUrl();
                            cacheUtil.disPlayImage(imageView, url);
                        }
                    }
                } else {
                    imageView.setImageResource(R.mipmap.user_image);
                }
            }
        });
    }
}
