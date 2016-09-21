package image;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

import util.BitmapUtils;
import util.MD5Utils;

/**
 * Created by jimhao on 16/8/19.
 */
public class LocalCacheUtil {
    private static final String CACHE_PATH = Environment.
            getExternalStorageDirectory().
            getAbsolutePath()+
            File.separator+"JimBlog";

    /**
     * 取出图片在本机的地址
     * @param url 图片的网络地址
     * @return
     */
    public static String getBitmapNameURL(String url){
        String filename = null ;
        filename = MD5Utils.md5(url);
        File file = new File(CACHE_PATH,filename);
        return file.getAbsolutePath();
    }
    /**
     * 从本地得到图片
     * @param url 图片地址
     * @return 图片
     */
    public Bitmap getBitmapFromLocal(String url){
        String filename = null ;    //把图片的url作为文件名并用MD5加密
        File file = null ;
        //如果URL中包含了文件的头文件
        if(url.contains("/storage/emulated")){
            file = new File(url);
        }else{
            //如果是从网络下载的 直接转换成MD5
            filename = MD5Utils.md5(url);
            file = new File(CACHE_PATH,filename);
        }
        Bitmap bitmap = BitmapUtils.getBitmapByFile(file);
        if(bitmap!=null){
            return bitmap ;
        }
        return null ;
    }

    /**
     * 将图片保存到本地
     * @param url
     * @param bitmap
     */
    public void saveBitmapToLocal(String url , Bitmap bitmap){
        try {
            String fileName = MD5Utils.md5(url);
            File file = new File(CACHE_PATH,fileName);
            //通过得到文件的父文件,判断父文件是否存在
            File parentFile = file.getParentFile();
            if (!parentFile.exists()){
                parentFile.mkdirs();
            }
            //把图片保存到本地
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,new FileOutputStream(file));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
