package cc.jimblog.imfriendchat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import service.MessageService;

/**
 * Created by Ran on 2016/8/21.
 */
public class TestActivity extends AppCompatActivity {

    @BindView(R.id.test_send)
    Button testSend;
    @BindView(R.id.test_notifi)
    Button testNotifi;
    @BindView(R.id.test_cancelNotifi)
    Button testCancelNotifi;

    private NotificationManager notificationManager;

    private static final int NOTIFICATION_ID = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @OnClick({R.id.test_send, R.id.test_notifi,R.id.test_cancelNotifi})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_send:
                Intent serviceIntent = new Intent(TestActivity.this, MessageService.class);
                serviceIntent.putExtra("Name", "测试用户");
                startService(serviceIntent);
                break;
            case R.id.test_notifi:
                sendNotification();
                break;
            case R.id.test_cancelNotifi:
                notificationManager.cancel(NOTIFICATION_ID);
                break;
        }
    }

    /**
     * 实现悬挂式Notification
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.user_image)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle("Headsup Notification")
                .setContentText("I am a Headsup notification.");
        Intent push = new Intent();
        push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        push.setClass(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentText("Heads-Up Notification on Android 5.0")
                .setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
