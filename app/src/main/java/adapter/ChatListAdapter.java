package adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMConversation;

import view.CircleImageView;

/**
 * Created by Ran on 2016/8/14.
 */
public class ChatListAdapter extends BaseAdapter {
    public Context mContext ;
    public EMConversation mConversation ;

    public ChatListAdapter(Context context , EMConversation conversation){
        this.mContext = context ;
        this.mConversation = conversation;
    }
    @Override
    public int getCount() {
        return mConversation.getAllMessages().size();
    }

    @Override
    public Object getItem(int i) {
        return mConversation.getAllMessages().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i ;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
    class ViewHolder{
        public CircleImageView userImg ;
        public TextView messageText ;
        public LinearLayout tokenLayout ;
        public TextView timeText ;
    }
}
