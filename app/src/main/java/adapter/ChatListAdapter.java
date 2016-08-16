package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.Target;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.io.IOException;
import java.util.List;

import cc.jimblog.imfriendchat.R;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import util.ImageDownload;
import util.LogUtils;
import view.CircleImageView;

/**
 * 聊天页的Adapter
 * Created by Ran on 2016/8/14.
 * 稳定起见 采用了标准的BaseAdapter形式实现
 */
public class ChatListAdapter extends BaseAdapter {
    public Context mContext ;   //上下文
    public List<EMMessage> mMessageList ; //处理消息和接收消息的对象
    public LayoutInflater mInflater = null ;

    public ChatListAdapter(Context context , List<EMMessage> list){
        this.mContext = context ;
        this.mMessageList = list;
        mInflater = LayoutInflater.from(context);
    }
    private static final int DEFAULT_WIDTH = 350 ;
    private static final int DEFAULT_HEIGHT = 350 ;
    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int i) {
        return mMessageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i ;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null ;
        EMMessage emMessage = mMessageList.get(i);

        if(convertView == null){
            viewHolder = new ViewHolder() ;
            convertView = mInflater.inflate(R.layout.layout_constaion_item,null);
            viewHolder.messageLeftText = (TextView) convertView.findViewById(R.id.chat_left_text);
            viewHolder.userLeftImg = (CircleImageView) convertView.findViewById(R.id.chat_left_img);
            viewHolder.tokenLeftLayout = (RelativeLayout) convertView.findViewById(R.id.chat_left_msg_layout);
            viewHolder.messageLeftImage = (ImageView) convertView.findViewById(R.id.chat_left_text_img);

            viewHolder.messageRightText = (TextView) convertView.findViewById(R.id.chat_right_text);
            viewHolder.userRightImg = (CircleImageView) convertView.findViewById(R.id.chat_right_img);
            viewHolder.tokenRightLayout = (RelativeLayout) convertView.findViewById(R.id.chat_right_msg_layout);
            viewHolder.messageRightImage = (ImageView) convertView.findViewById(R.id.chat_right_text_img);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //如果是接收到的消息
        if(emMessage.direct() == EMMessage.Direct.RECEIVE){
            viewHolder.tokenRightLayout.setVisibility(View.GONE);   //首先关闭右侧的视图
            viewHolder.tokenLeftLayout.setVisibility(View.VISIBLE); //并打开左侧视图
            //处理接收到的消息
            if(emMessage.getType() == EMMessage.Type.TXT){
                viewHolder.messageLeftImage.setVisibility(View.GONE);
                viewHolder.messageLeftText.setVisibility(View.VISIBLE);
                EMTextMessageBody textBody = (EMTextMessageBody) emMessage.getBody();
                String message = textBody.getMessage();
                viewHolder.messageLeftText.setText(message);
            }else if(emMessage.getType() == EMMessage.Type.IMAGE){
                viewHolder.messageLeftImage.setVisibility(View.VISIBLE);
                viewHolder.messageLeftText.setVisibility(View.GONE);
                EMImageMessageBody imageBody = (EMImageMessageBody) emMessage.getBody();
                netLoadChatImage(imageBody,viewHolder.messageLeftImage);
            }
        }else{ //如果是自己发送的消息
            viewHolder.tokenRightLayout.setVisibility(View.VISIBLE);
            viewHolder.tokenLeftLayout.setVisibility(View.GONE);
            //处理接收到的消息
            if(emMessage.getType() == EMMessage.Type.TXT){
                viewHolder.messageRightImage.setVisibility(View.GONE);
                viewHolder.messageRightText.setVisibility(View.VISIBLE);
                EMTextMessageBody textBody = (EMTextMessageBody) emMessage.getBody();
                String message = textBody.getMessage();
                viewHolder.messageRightText.setText(message);
            }else if(emMessage.getType() == EMMessage.Type.IMAGE){
                viewHolder.messageRightImage.setVisibility(View.VISIBLE);
                viewHolder.messageRightText.setVisibility(View.GONE);
                EMImageMessageBody imageBody = (EMImageMessageBody) emMessage.getBody();
                netLoadChatImage(imageBody,viewHolder.messageRightImage);
            }
        }

        return convertView;
    }
    //与RecylerView不同的是 标准的BaseAdapter不会内部封装ViewHolder需要开发者手动实现
    class ViewHolder{
        public CircleImageView userLeftImg ;    //对方用户头像
        public TextView messageLeftText ;  //对方消息实体
        public ImageView messageLeftImage ;     //对方消息图片实体
        public RelativeLayout tokenLeftLayout ;   //对方Layout 作显示和隐藏

        public CircleImageView userRightImg ;   //自己用户头像
        public TextView messageRightText ;  //自己消息实体
        public ImageView messageRightImage ;    //自己消息图片实体
        public RelativeLayout tokenRightLayout ;  //自己Layout 作显示和隐藏
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    /**
     * 为了保证ListView流畅滚动,这里是用谷歌官方推荐的Glide图片加载库来实现
     * update 因为发现是用EMImageMessageBody 无法加载图片的实际大小
     * 决定用RxJava异步download图片,从Bitmap中得到准确的大小
     *
     * 这边的数据保存算法需要修改,需要增加三级缓存模型,即 cache → Storage → NetWork
     * @param imageBody 图像实体
     */
    private void netLoadChatImage(EMImageMessageBody imageBody, final ImageView imageView){
        final String url = imageBody.getThumbnailUrl(); //调用此方法SDK会自动下载图片到本地,此URL为本地存储的路径
        //创建被监听者对象
        Observable<Bitmap> observable = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                try {
                    Bitmap bitmap = ImageDownload.getBitmap(url);
                    subscriber.onNext(bitmap);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(new Throwable(e.toString()));
                }
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        LogUtils.i("图片下载成功");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LogUtils.e("图片下载失败:"+throwable.toString());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        int width = bitmap.getWidth() ;
                        int height = bitmap.getHeight();
                        LogUtils.d("Width:"+width+",height:"+height);
                        int displayWidth = 0 ;
                        int displayHeight = 0 ;
                        if(width > height){
                            float dpi = (float)width / height ;
                            if(width > DEFAULT_WIDTH){
                                displayWidth = DEFAULT_WIDTH;
                                displayHeight = (int) (displayWidth / dpi);
                            }else{
                                displayWidth = width ;
                                displayHeight = height ;
                            }
                        }else{
                            float dpi = (float)height / width ;
                            if(height > DEFAULT_HEIGHT){
                                displayHeight = DEFAULT_HEIGHT ;
                                displayWidth = (int) (displayHeight / dpi);
                            }else{
                                displayWidth = width ;
                                displayHeight = height ;
                            }
                        }
                        Glide.with(mContext)
                                .load(url)
                                .override(displayWidth,displayHeight)
                                .centerCrop()
                                .into(imageView);
                    }
                });
    }
}
