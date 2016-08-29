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
 * 此处需要去查询环信SDK相关群聊天的数据
 * Created by jimhao on 16/8/29.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {
    private List<String> mList = null ;
    private Context mContext = null ;
    private LayoutInflater mInflater = null ;

    public GroupAdapter(List<String> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_group_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private RelativeLayout itemLayout ;
        private CircleImageView itemUserImg ;
        private TextView itemName ;
        private TextView itemContent ;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.item_group_layout);
            itemUserImg = (CircleImageView) itemView.findViewById(R.id.item_group_userimg);
            itemName = (TextView) itemView.findViewById(R.id.item_group_username);
            itemContent = (TextView) itemView.findViewById(R.id.item_group_user_content);

            itemLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view,getPosition());
        }
    }
    public interface OnGroupClickListener{
        void onClick(View view , int position);
    }
    public OnGroupClickListener clickListener ;

    public void setOnGroupClickListener(OnGroupClickListener clickListener){
        this.clickListener = clickListener ;
    }
}
