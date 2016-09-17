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

import org.json.JSONArray;

import java.util.List;

import cc.jimblog.imfriendchat.R;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import entity.ContextSave;
import entity.UserInfoEntity;
import image.MyBitmapCacheUtil;
import util.BitmapUtils;
import util.JsonUtil;
import util.LogUtils;
import view.CircleImageView;

/**
 * Created by jimhao on 16/9/14.
 */
public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MyViewHolder> {
    private List<String> mList ;
    private Context mContext ;
    private LayoutInflater mInflater ;
    private MyBitmapCacheUtil cacheUtil;

    public GroupMembersAdapter(List<String> mList , Context context){
        this.mList = mList ;
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        cacheUtil = new MyBitmapCacheUtil();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_group_member_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String name = mList.get(position);
        holder.userName.setText(name);
        queryUserImg(name,holder.userImage);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView userImage ;
        private TextView userName ;
        private RelativeLayout userLayout ;

        public MyViewHolder(View itemView) {
            super(itemView);
            userImage = (CircleImageView) itemView.findViewById(R.id.group_member_image);
            userName = (TextView) itemView.findViewById(R.id.group_member_text);
            userLayout = (RelativeLayout) itemView.findViewById(R.id.group_member_layout);
        }
    }
    /**
     * 查询Bmob服务器中的数据得到用户头像
     * */
    private void queryUserImg(String userId,final ImageView imageView){
        BmobQuery<UserInfoEntity> query = new BmobQuery<UserInfoEntity>("userinfo");
        query.addWhereEqualTo("userId",userId);
        query.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if(e == null){
                    List<UserInfoEntity> userInfo = new JsonUtil().jsonToList(jsonArray.toString());
                    for(UserInfoEntity entity : userInfo){
                        boolean flag = entity.isDefImg();
                        if(flag){   //是否使用默认的用户头像
                            int position = Integer.parseInt(entity.getDefImgPosition());
                            LogUtils.d("Position"+position);
                            Bitmap bitmap = BitmapUtils.getBitmapById(mContext, ContextSave.defPicArray[position]);
                            imageView.setImageBitmap(bitmap);
                        }else{
                            String url = entity.getUserImg().getUrl();
                            cacheUtil.disPlayImage(imageView,url);
                        }
                    }
                }else{
                    imageView.setImageResource(R.mipmap.user_image);
                }
            }
        });
    }
}
