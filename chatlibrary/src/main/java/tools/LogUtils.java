package tools;

import android.util.Log;

/**
 * Created by Administrator on 2016/5/9.
 */
public class LogUtils {
    private LogUtils(){}
    public static boolean isDebug = true ; //是否需要打印bug　可以在application的onCreate方法中
    private static final String TAG = "Javways";

    public static void i(String msg){
        if(isDebug){
            Log.i(TAG,msg);
        }
    }
    public static void d(String msg){
        if(isDebug){
            Log.d(TAG,msg);
        }
    }
    public static void e(String msg){
        if(isDebug){
            Log.e(TAG,msg);
        }
    }
    public static void v(String msg){
        if(isDebug){
            Log.v(TAG,msg);
        }
    }
    public static void i(String tag , String msg){
        if(isDebug){
            Log.i(tag,msg);
        }
    }
    public static void d(String tag,String msg){
        if(isDebug){
            Log.d(tag,msg);
        }
    }
    public static void e(String tag,String msg){
        if(isDebug){
            Log.e(tag,msg);
        }
    }
    public static void v(String tag,String msg){
        if(isDebug){
            Log.v(tag,msg);
        }
    }
}