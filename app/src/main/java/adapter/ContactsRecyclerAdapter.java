package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMContact;

import java.util.List;

/**
 * 联系人页面Adapter适配器
 * Created by Ran on 2016/8/12.
 */
public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.MyViewHolder> {
    private Context mContext ;
    private LayoutInflater mInflater ;
    private List<EMContact> mList ;
    public ContactsRecyclerAdapter(Context context , List<EMContact> list){
        this.mContext = context ;
        if(mContext != null){
            mInflater = LayoutInflater.from(mContext);
        }
        this.mList = list ;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public MyViewHolder(View itemView) {
            super(itemView);


        }

        @Override
        public void onClick(View view) {

        }
    }

}
