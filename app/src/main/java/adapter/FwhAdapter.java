package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cc.jimblog.imfriendchat.R;
import view.CircleImageView;

/**
 * Created by jimhao on 16/9/3.
 */
public class FwhAdapter extends RecyclerView.Adapter<FwhAdapter.MyViewHolder> {
    private Context mContext ;
    private LayoutInflater mInflater ;
    private List<String> mList ;

    public static final int TYPE_HEADER = 0 ;
    public static final int TYPE_NORMAL = 1 ;

    private View mHeaderView ;

    public View getmHeaderView() {
        return mHeaderView;
    }

    public void setmHeaderView(View mHeaderView) {
        this.mHeaderView = mHeaderView;
        notifyItemInserted(0);
    }

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

    public FwhAdapter(Context mContext, List<String> mList) {
        this.mContext = mContext;
        this.mList = mList;
        mInflater = LayoutInflater.from(mContext);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER){
            return new MyViewHolder(mHeaderView);
        }
        View view = mInflater.inflate(R.layout.layout_group_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if(getItemViewType(position) == TYPE_HEADER){
            return ;
        }
        //公众号模块规划中
    }


    @Override
    public int getItemCount() {
        return mHeaderView == null ? mList.size() : mList.size() +1 ;
    }
    //这里因为设计要求 群组和公众号的内容是一致的
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private RelativeLayout itemLayout ;
        private CircleImageView itemUserImg ;
        private TextView itemName ;
        private TextView itemContent ;

        public MyViewHolder(View itemView) {
            super(itemView);
            //需要加上这句 否则会NPE
            if(itemView == mHeaderView) {
                return;
            }
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.item_group_layout);
            itemUserImg = (CircleImageView) itemView.findViewById(R.id.item_group_userimg);
            itemName = (TextView) itemView.findViewById(R.id.item_group_username);
            itemContent = (TextView) itemView.findViewById(R.id.item_group_user_content);

            itemLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(clickListener!=null){
                clickListener.onClick(view,getPosition());
            }
        }
    }
    public interface OnFwhClickListener{
        void onClick(View view , int position);
    }
    public OnFwhClickListener clickListener ;

    public void setOnGroupClickListener(OnFwhClickListener clickListener){
        this.clickListener = clickListener ;
    }
}
