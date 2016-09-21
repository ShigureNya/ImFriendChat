package service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import com.google.gson.Gson;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.HashMap;
import java.util.List;

import cc.jimblog.imfriendchat.ChatActivity;
import cc.jimblog.imfriendchat.NewFriendActivity;
import cc.jimblog.imfriendchat.R;
import entity.ContextSave;
import util.LogUtils;
import util.ScreenUtils;

/**
 * Created by jimhao on 16/8/27.
 */
public class MessageService extends Service {
    private NotificationManager notificationManager;
    private Gson gson ;

    private static final int NOTIFICATION_ID = 12;
    private static final int NOTIFICATION_ADD_FRIEND_ID = 145 ;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        LogUtils.d("后台消息服务正常运行");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        LogUtils.d("后台消息服务已关闭");
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        gson = new Gson();
        registerAddFriendCallBack();
        return START_STICKY ;
    }
    private void registerAddFriendCallBack(){
        EMClient.getInstance().contactManager().setContactListener(new EMContactListener() {

            @Override
            public void onContactAgreed(String username) {
                //好友请求被同意
            }

            @Override
            public void onContactRefused(String username) {
                //好友请求被拒绝
            }

            @Override
            public void onContactInvited(String username, String reason) {
                //收到好友邀请
                HashMap<String,String> hashMap = new HashMap<String, String>();
                hashMap.put("Name",username);
                hashMap.put("Reason",reason);
                receiveNotification(hashMap);
            }

            @Override
            public void onContactDeleted(String username) {
                //被删除时回调此方法
            }


            @Override
            public void onContactAdded(String username) {
                //增加了联系人时回调此方法
            }
        });
    }
    /**
     * 初始化广播接收器对象
     */
    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            for(EMMessage emMessage : messages){
                //如果在后台运行 则发送Notification
                LogUtils.d("是否在后台:"+String.valueOf(ScreenUtils.isForeground(ContextSave.MainActivity)));
                if(!ScreenUtils.isForeground(ContextSave.MainActivity)){
                    setMessageType(emMessage);
                }
            }
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            //收到透传消息
        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> messages) {
            //收到已读回执
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            //收到已送达回执
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            //消息状态变动
        }
    };
    private void setMessageType(EMMessage message){
        HashMap<String,String> hashMap = new HashMap<String,String>();
        hashMap.put("Name",message.getUserName());
        if(message.getType() == EMMessage.Type.TXT){
            EMTextMessageBody textBody = (EMTextMessageBody) message.getBody();
            String msg = textBody.getMessage();
            LogUtils.i("Msg:"+msg);
            hashMap.put("Content",msg);
        }else if(message.getType() == EMMessage.Type.IMAGE){
            hashMap.put("Content","[图片]");
        }else if(message.getType() == EMMessage.Type.FILE){
            hashMap.put("Content","[文件]");
        }else if(message.getType() == EMMessage.Type.VOICE){
            hashMap.put("Content","[语音]");
        }else if(message.getType() == EMMessage.Type.LOCATION){
            hashMap.put("Content","[位置]");
        }
        //判断SDK的版本
        if(Build.VERSION.SDK_INT >= 21){
            sendNotification(hashMap);
        }else{
            showDefaultNotification(hashMap);
        }
    }

    /**
     * 收到好友消息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void receiveNotification(HashMap<String,String> hashMap){
        String name = hashMap.get("Name");
        String content = hashMap.get("Reason");
        Notification.Builder receiveBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.small_icon)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle("收到了来自"+name+"的好友请求")
                .setContentText(content);
        Intent push = new Intent();
        push.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);// 关键的一步，设置启动模式
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
        receiveBuilder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(NOTIFICATION_ID, receiveBuilder.build());
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notificationManager.cancel(NOTIFICATION_ID);
        showFriendNotification(hashMap);
    }
    /**
     * 接收好友请求时监听 - 普通Notification
     * @param map
     */
    private void showFriendNotification(HashMap<String,String> map){
        String name = map.get("Name");
        String content = map.get("Reason");
        Notification.Builder builder = new Notification.Builder(this);
        Intent mIntent = new Intent(MessageService.this,NewFriendActivity.class);
        mIntent.putExtra("Name",name);
        mIntent.putExtra("Reason",content);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setWhen(System.currentTimeMillis()); //设置时间发生时间
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.small_icon);
        builder.setDefaults(Notification.DEFAULT_SOUND);   //设置默认声音
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_lanuch));
        builder.setAutoCancel(true);
        builder.setContentTitle("来自"+name+"的新消息");
        builder.setContentText(content);
        notificationManager.notify(21,builder.build());
    }

    /**
     * 实现悬挂式Notification
     * @param hashMap 消息体HashMap
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendNotification(final HashMap<String,String> hashMap) {
        String name = hashMap.get("Name");
        String content = hashMap.get("Content");
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_lanuch)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle("来自"+name+"的新消息")
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_SOUND);   //设置默认声音
        Intent push = new Intent();
        push.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);// 关键的一步，设置启动模式
        push.setClass(MessageService.this, ChatActivity.class);
        push.putExtra("Username",name);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notificationManager.cancel(NOTIFICATION_ID);
        showDefaultNotification(hashMap);
    }

    /**
     * 接收到消息时监听
     * @param map
     */
    private void showDefaultNotification(HashMap<String,String> map){
        String name = map.get("Name");
        String content = map.get("Content");
        Notification.Builder builder = new Notification.Builder(this);
        Intent mIntent = new Intent(MessageService.this,ChatActivity.class);
        mIntent.putExtra("Username",name);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mIntent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_lanuch);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_lanuch));
        builder.setAutoCancel(true);
        builder.setContentTitle("来自"+name+"的新消息");
        builder.setContentText(content);
        notificationManager.notify(15,builder.build());
    }

}
