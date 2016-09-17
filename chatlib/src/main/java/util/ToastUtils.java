package util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Kotori on 2016/5/21.
 */
public class ToastUtils {
    private ToastUtils(){}

    public static boolean isShow = true ;
    /**
     * 短时间显示Toast
     * */
    public static void showShort(Context context, CharSequence message){
        if(isShow){
            Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 长时间显示Toast
     * */
    public static void showLong(Context context,CharSequence message){
        if(isShow){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
    /**
     * 自定义Toast时间
     * */
    public static void show(Context context,CharSequence message,int duration){
        if(isShow){
            Toast.makeText(context, message , duration).show();
        }
    }
}
