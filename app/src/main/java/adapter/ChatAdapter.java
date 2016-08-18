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

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.io.IOException;

import cc.jimblog.imfriendchat.R;
import image.ImageManager;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.BitmapUtils;
import util.LogUtils;
import view.CircleImageView;

/**
 * 使用RecyclerView重构
 * Created by Ran on 2016/8/18.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    public Context mContext ;
    public EMConversation mConversation ;
    public LayoutInflater mInflater ;
    public ImageManager manager ;

    private static final int DEFAULT_WIDTH = 350 ;
    private static final int DEFAULT_HEIGHT = 350 ;

    public ChatAdapter(Context context , EMConversation mConversation){
        mContext = context ;
        this.mConversation = mConversation ;

        if(mContext != null){
            mInflater = LayoutInflater.from(mContext);
        }
        manager = new ImageManager();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_constaion_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EMMessage message = mConversation.getAllMessages().get(position);
        //如果是接收到的消息
        if(message.direct() == EMMessage.Direct.RECEIVE){
            holder.tokenRightLayout.setVisibility(View.GONE);   //首先关闭右侧的视图
            holder.tokenLeftLayout.setVisibility(View.VISIBLE); //并打开左侧视图
            //处理接收到的消息
            if(message.getType() == EMMessage.Type.TXT){
                holder.messageLeftImage.setVisibility(View.GONE);
                holder.messageLeftText.setVisibility(View.VISIBLE);
                EMTextMessageBody textBody = (EMTextMessageBody) message.getBody();
                String messageStr = textBody.getMessage();
                holder.messageLeftText.setText(messageStr);
            }else if(message.getType() == EMMessage.Type.IMAGE){
                holder.messageLeftImage.setVisibility(View.VISIBLE);
                holder.messageLeftText.setVisibility(View.GONE);
                EMImageMessageBody imageBody = (EMImageMessageBody) message.getBody();
                String url = imageBody.getThumbnailUrl() ;
                //设置默认替换的图片
                holder.messageLeftImage.setImageResource(R.mipmap.ic_launcher);
                //为图片设置Tag防止错误加载
                holder.messageLeftImage.setTag(url);
                if(holder.messageLeftImage.getTag()!=null && holder.messageLeftImage.getTag().equals(url)){
                    imageLoader(url,holder.messageLeftImage);
                }
            }
        }else{ //如果是自己发送的消息
            holder.tokenRightLayout.setVisibility(View.VISIBLE);
            holder.tokenLeftLayout.setVisibility(View.GONE);
            //处理接收到的消息
            if(message.getType() == EMMessage.Type.TXT){
                holder.messageRightImage.setVisibility(View.GONE);
                holder.messageRightText.setVisibility(View.VISIBLE);
                EMTextMessageBody textBody = (EMTextMessageBody) message.getBody();
                String messageStr = textBody.getMessage();
                holder.messageRightText.setText(messageStr);
            }else if(message.getType() == EMMessage.Type.IMAGE){
                holder.messageRightImage.setVisibility(View.VISIBLE);
                holder.messageRightText.setVisibility(View.GONE);
                EMImageMessageBody imageBody = (EMImageMessageBody) message.getBody();
                String url = imageBody.getThumbnailUrl() ;
                //设置默认替换的图片
                holder.messageRightImage.setImageResource(R.mipmap.ic_launcher);
                //为图片设置Tag防止错误加载
                holder.messageRightImage.setTag(url);
                if(holder.messageRightImage.getTag()!=null && holder.messageRightImage.getTag().equals(url)){
                    imageLoader(url,holder.messageRightImage);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mConversation.getAllMessages().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView userLeftImg ;    //对方用户头像
        public TextView messageLeftText ;  //对方消息实体
        public ImageView messageLeftImage ;     //对方消息图片实体
        public RelativeLayout tokenLeftLayout ;   //对方Layout 作显示和隐藏

        public CircleImageView userRightImg ;   //自己用户头像
        public TextView messageRightText ;  //自己消息实体
        public ImageView messageRightImage ;    //自己消息图片实体
        public RelativeLayout tokenRightLayout ;  //自己Layout 作显示和隐藏
        
        public ViewHolder(View itemView) {
            super(itemView);
            messageLeftText = (TextView) itemView.findViewById(R.id.chat_left_text);
            userLeftImg = (CircleImageView) itemView.findViewById(R.id.chat_left_img);
            tokenLeftLayout = (RelativeLayout) itemView.findViewById(R.id.chat_left_msg_layout);
            messageLeftImage = (ImageView) itemView.findViewById(R.id.chat_left_text_img);

            messageRightText = (TextView) itemView.findViewById(R.id.chat_right_text);
            userRightImg = (CircleImageView) itemView.findViewById(R.id.chat_right_img);
            tokenRightLayout = (RelativeLayout) itemView.findViewById(R.id.chat_right_msg_layout);
            messageRightImage = (ImageView) itemView.findViewById(R.id.chat_right_text_img);

        }
    }
    /**
     * 是用RxJava异步加载图像并进行相关处理
     * */
    public void imageLoader(final String url , final ImageView imageView){
        Observable<Bitmap> observable = Observable.create(new Observable.OnSubscribe<Bitmap>(){

            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = manager.getBitmap(url);
                if(bitmap != null){
                    subscriber.onNext(bitmap);
                    subscriber.onCompleted();
                }else{
                    subscriber.onError(new Throwable("图像下载失败"));
                }
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        LogUtils.i("图像下载完成");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LogUtils.e(throwable.toString());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        djustImageSize(bitmap,imageView);   //如果得到了这个Bitmap 则将其交给处理方法处理大小
                    }
                });
    }
    /**
     * 处理图像大小
     * @param  bitmap 图片
     * @return Bitmap 处理完成后的图片
     **/
    private void djustImageSize(Bitmap bitmap,ImageView imageView){
        int width = bitmap.getWidth() ;
        int height = bitmap.getHeight();
        LogUtils.d("Width:"+width+",height:"+height);
        //对图片的大小进行限制
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
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth,displayHeight));
        imageView.setImageBitmap(bitmap);
    }
}
