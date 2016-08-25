package adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import view.CircleImageView;

/**
 * Created by jimhao on 16/8/25.
 */
public class NewFriendAdapter extends RecyclerView.Adapter<NewFriendAdapter.MyViewHolder> {

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView userImage ;
        private TextView userName ;
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
