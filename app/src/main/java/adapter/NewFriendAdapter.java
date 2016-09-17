package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import cc.jimblog.imfriendchat.R;
import cn.bmob.v3.datatype.BmobFile;
import entity.ContextSave;
import entity.UserInfoEntity;
import image.MyBitmapCacheUtil;
import util.BitmapUtils;
import view.CircleImageView;

/**
 * Created by jimhao on 16/8/25.
 */
public class NewFriendAdapter extends RecyclerView.Adapter<NewFriendAdapter.MyViewHolder> {

    private List<UserInfoEntity> mList ;
    private Context context ;
    private LayoutInflater mInflater ;

    private MyBitmapCacheUtil myBitmapUtil ;
    public NewFriendAdapter(Context context , List<UserInfoEntity> list){
        this.context = context ;
        this.mList = list ;
        mInflater = LayoutInflater.from(context);
        myBitmapUtil = new MyBitmapCacheUtil() ;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_new_friend_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UserInfoEntity entity = mList.get(position);
        String userName = entity.getUserName();
        holder.userImage.setImageResource(R.mipmap.ic_launcher);
        if(entity.isDefImg()){
            int defImgPosition =  Integer.parseInt(entity.getDefImgPosition());
            Bitmap bitmap = BitmapUtils.getBitmapById(context, ContextSave.defPicArray[defImgPosition]);
            holder.userImage.setImageBitmap(bitmap);
        }else{
            BmobFile userImg = entity.getUserImg();
            if(userImg != null){
                String imageUrl = userImg.getUrl();
                holder.userImage.setTag(imageUrl);
                if(holder.userImage.getTag()!=null && holder.userImage.getTag().equals(imageUrl)){
                    myBitmapUtil.disPlayImage(holder.userImage,imageUrl);
                }
            }
        }
        holder.userName.setText(userName);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private CircleImageView userImage ;
        private TextView userName ;
        private Button addBtn ;
        public MyViewHolder(View itemView) {
            super(itemView);
            userImage = (CircleImageView) itemView.findViewById(R.id.newfriend_item_img);
            userName = (TextView) itemView.findViewById(R.id.newfriend_item_name);
            addBtn = (Button) itemView.findViewById(R.id.newfriend_add_btn);
            addBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onClickListener.onClick(view,getPosition());
        }
    }
    public interface BtnOnClickListener{
        void onClick(View view , int position);
    }
    public BtnOnClickListener onClickListener ;

    public void setOnBtnClickListener(BtnOnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
}
