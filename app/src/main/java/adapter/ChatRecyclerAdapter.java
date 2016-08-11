package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cc.jimblog.imfriendchat.R;
import view.CircleImageView;

/**
 * Created by Ran on 2016/8/11.
 */
public class ChatRecyclerAdapter extends RecyclerView.Adapter {
    private Context mContext ;

    private LayoutInflater mInflater ;
//    此处加载布局
    public ChatRecyclerAdapter(Context mContext){
        this.mContext = mContext;
        //还需要加载会话列表实体
        mInflater = LayoutInflater.from(mContext);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_item,parent,false);
        return new ViewHolder(v);
    }
//    建立ViewHolder的数据关联
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mUserName ;
        public TextView mUserMsg ;
        public TextView mMessageTime ;
        public CircleImageView mUserImg ;

        public ViewHolder(View itemView) {
            super(itemView);
            mUserName = (TextView) itemView.findViewById(R.id.item_chat_username);
            mUserMsg = (TextView) itemView.findViewById(R.id.item_chat_usermsg);
            mMessageTime = (TextView) itemView.findViewById(R.id.item_chat_time);
            mUserImg = (CircleImageView) itemView.findViewById(R.id.item_chat_userimg);
            //下午的工作  创建Item的回调实例，根据Demo源码加载会话列表
        }

        @Override
        public void onClick(View view) {

        }
    }
}
