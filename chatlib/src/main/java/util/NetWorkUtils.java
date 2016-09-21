package util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络工具类
 * Created by Administrator on 2016/5/9.
 */
public class NetWorkUtils {
    /**
     * 判断网络是否连接
     * */
    public static boolean isConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if(info.isConnected() && info != null){
                if(info.getState() == NetworkInfo.State.CONNECTED){
                    return true ;
                }
            }
        }
        return false ;
    }
    /**
     * 判断是否为wifi连接
     * */
    public static boolean isWifi(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null){
            return false ;
        }
        return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }
    /**
     * 打开设置页面
     * */
    public static void openSettingActivity(Activity activity){
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent,0);
    }
    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }
}
