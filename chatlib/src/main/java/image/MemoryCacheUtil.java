package image;

import android.graphics.Bitmap;
import android.util.LruCache;

import util.LogUtils;

/**
 * Created by jimhao on 16/8/19.
 */
public class MemoryCacheUtil {
    //得到手机最大允许内存的1/8,即超过指定内存,则开始回收
    long maxMemory = Runtime.getRuntime().maxMemory()/8;

    private LruCache<String,Bitmap> mMemoryCache ;

    public MemoryCacheUtil(){
        //初始化LruCache缓存
        mMemoryCache = new LruCache<String,Bitmap>((int)maxMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    /**
     * 从内存中取出图片
     * @param url
     * @return
     */
    public Bitmap getBitmapFromMemory(String url){
        Bitmap bitmap = mMemoryCache.get(url);
        return bitmap ;
    }

    /**
     * 存储到内存中
     * @param url
     * @param bitmap
     */
    public void saveBitmapToMemory(String url , Bitmap bitmap ){
        if(getBitmapFromMemory(url) == null){
            LogUtils.i("存到本地了");
            mMemoryCache.put(url,bitmap);
        }
    }
}
