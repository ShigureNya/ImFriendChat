package entity;

import android.app.Activity;
import android.graphics.Bitmap;

import java.util.List;

import cc.jimblog.imfriendchat.R;

/**
 * Created by jimhao on 16/8/26.
 */
public class ContextSave {
    public static Activity MainActivity = null ;
    public static int [] defPicArray = new int[]{R.mipmap.default_1,R.mipmap.default_2,R.mipmap.default_3,
            R.mipmap.default_3,R.mipmap.default_4,R.mipmap.default_5,R.mipmap.default_6,R.mipmap.default_7,
            R.mipmap.default_8,R.mipmap.default_9,R.mipmap.default_10,R.mipmap.default_11};
    public static Bitmap userBitmap = null ;
    public static String userId = null;

    public static List<String> friendList = null;
}
