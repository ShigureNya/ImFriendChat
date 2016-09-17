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
 * 联系人页面Adapter适配器
 * Created by Ran on 2016/8/12.
 */
public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.MyViewHolder> {
    private Context mContext ;
    private LayoutInflater mInflater ;
    private List<String> mList ;

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;

    private View mHeaderView ;  //头部View

    private MyBitmapCacheUtil cacheUtil ;
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);  //设置头部View时同时刷新布局
    }

    public View getHeaderView() {
        return mHeaderView;
    }
    //通过判断ItemViewType来添加headerView
    @Override
    public int getItemViewType(int position) {
        if(mHeaderView == null) {
            return TYPE_NORMAL;
        }
        if(position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    public ContactsRecyclerAdapter(Context context , List<String> list){
        this.mContext = context ;
        if(mContext != null){
            mInflater = LayoutInflater.from(mContext);
        }
        this.mList = list ;
        cacheUtil = new MyBitmapCacheUtil();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER){
            return new MyViewHolder(mHeaderView);
        }
        View view = mInflater.inflate(R.layout.layout_contacts_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if(getItemViewType(position) == TYPE_HEADER){
            return ;
        }
        if(!mList.isEmpty()){
            String name = mList.get(position-1);
            holder.userName.setText(name);
            holder.userImage.setImageResource(R.mipmap.user_image);
            holder.userImage.setTag(name);
            if(holder.userImage.getTag()!=null && holder.userImage.getTag().equals(name)){
                queryUserImg(name,holder.userImage);
            }
        }
    }
    public int getRealPosition(RecyclerView.ViewHolder holder){
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position -1 ;
    }
    @Override
    public int getItemCount() {
        return mHeaderView == null ? mList.size() : mList.size() +1 ;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener{
        public TextView userName ;
        public CircleImageView userImage ;
        public CardView userLayout ;

        public MyViewHolder(View itemView) {
            super(itemView);
            if(itemView == mHeaderView) {
                return;
            }
            userName = (TextView) itemView.findViewById(R.id.item_contacts_name);
            userImage = (CircleImageView) itemView.findViewById(R.id.item_contacts_img);
            userLayout = (CardView) itemView.findViewById(R.id.item_contacts_layout);

            userLayout.setOnClickListener(this);
            userLayout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //此处因为加了一个HeaderView，所以position在取值时需要-1
            if(itemClickListener != null){
                itemClickListener.onClick(view,getPosition()-1);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            longItemClickListener.onLongClick(view,getPosition()-1);
            return true;
        }
    }

    public interface OnContactsItemCLickListener {
        void onClick(View view , int position);
    }
    public OnContactsItemCLickListener itemClickListener ;

    public void setItemClickListener(OnContactsItemCLickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public interface OnContactsItemLongClickListener {
        void onLongClick(View view , int position);
    }
    public OnContactsItemLongClickListener longItemClickListener;

    public void setOnContactsItemLongClickListener(OnContactsItemLongClickListener longItemContactsListener){
        this.longItemClickListener = longItemContactsListener;
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
