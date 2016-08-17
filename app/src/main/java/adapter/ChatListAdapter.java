package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.io.IOException;
import java.util.List;

import cc.jimblog.imfriendchat.R;
import image.ImageManager;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import image.ImageDownload;
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
    public ImageManager imageManager ;

    public ChatListAdapter(Context context , List<EMMessage> list){
        this.mContext = context ;
        this.mMessageList = list;
        mInflater = LayoutInflater.from(context);
        imageManager = new ImageManager() ;
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
                imageLoader(imageBody,viewHolder.messageLeftImage);
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
                imageLoader(imageBody,viewHolder.messageRightImage);
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
     * 是用RxJava异步加载图像并进行相关处理
     * */
    public void imageLoader(EMImageMessageBody imageBody , final ImageView imageView){
        final String imgUrl = imageBody.getThumbnailUrl() ; //得到SDK图片的下载地址
        Observable<Bitmap> observable = Observable.create(new Observable.OnSubscribe<Bitmap>(){

            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = imageManager.getBitmap(imgUrl);
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
        //使用Google官方推荐的Glide图片加载库对图片进行加载
        try {
            byte[] bitmapBytes = imageManager.getByteArray4Bitmap(bitmap);
            Glide.with(mContext).load(bitmapBytes).override(displayWidth,displayHeight).into(imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
