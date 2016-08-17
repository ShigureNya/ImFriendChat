package image;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

import util.LogUtils;

/**
 * 从内存读取数据速度是最快的，为了更大限度使用内存，这里使用了两层缓存。
 *  强引用缓存不会轻易被回收，用来保存常用数据，不常用的转入软引用缓存。
 */
public class ImageMemoryCache {
    private static final String TAG = ImageMemoryCache.class.getSimpleName() ;  //标记

    private static LruCache<String , Bitmap> mLruCache ;   //强引用缓存

    private static LinkedHashMap<String , SoftReference<Bitmap>> mSoftCache ; //软引用缓存

    private static final int LRU_CACHE_SIZE = 4 * 1024 * 1024 ;     //强引用缓存大小 4M

    private static final int SOFT_CACHE_SIZE = 20;   //软引用缓存个数 20个
    //初始化强引用和软引用
    public ImageMemoryCache(){
        mLruCache = new LruCache<String,Bitmap>(LRU_CACHE_SIZE){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if(value != null){
                    /**
                     * size返回为hashmap的缓存大小
                     * */
                    return value.getRowBytes() * value.getHeight();
                }else{
                    return 0 ;
                }
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if(oldValue != null){
                    /**
                     * 强引用缓存容量满的时候，会根据LRU算法把最近没有被使用的图片转入此软引用缓存
                     * */
                    LogUtils.e("缓存空间已满");
                    mSoftCache.put(key,new SoftReference<Bitmap>(oldValue));
                }
            }
        };
        mSoftCache = new LinkedHashMap<String,SoftReference<Bitmap>>(SOFT_CACHE_SIZE, 0.75f, true){
            private static final long serialVersionUID = 1L ;
            /**
             * 当软引用数量大于20的时候，最旧的软引用将会被从链式哈希表中移出
             */
            @Override
            protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
                if(size() > SOFT_CACHE_SIZE){
                    return true ;
                }
                return false ;
            }
        };
    }

    /**
     * 从缓存中得到图片
     * @param url    图片地址
     * @return 从缓存中找到的图片
     */
    public Bitmap getBitmap4Memory(String url){
        Bitmap bitmap = null ;
        //从强引用缓存中获取
        synchronized (mLruCache){
            bitmap = mLruCache.get(url);
            if(bitmap != null){
                // 如果找到的话，把元素移到LinkedHashMap的最前面，从而保证在LRU算法中是最后被删除
                mLruCache.remove(url);
                mLruCache.put(url,bitmap);
                LogUtils.i("Get Bitmap from LruCache url:"+url);
                return bitmap ;
            }
        }
        //如果强引用中找不到,就去软引用里找,找到后将其移动到强引用中
        synchronized (mSoftCache){
            SoftReference<Bitmap> bitmapSoftReference = mSoftCache.get(url);
            if(bitmapSoftReference != null){
                bitmap = bitmapSoftReference.get();
                if(bitmap != null){
                    //将图片移动到LruCache
                    mLruCache.put(url,bitmap);
                    mSoftCache.remove(url);
                    LogUtils.i("Get Bitmap from SoftCache url:"+url);
                    return bitmap ;
                }else{
                    mSoftCache.remove(url);
                }
            }
        }
        return null ;
    }

    /**
     * 将图片添加到LruCache缓存里
     * @param url
     * @param bitmap
     */
    public void addBitmapCache(String url , Bitmap bitmap){
        if(bitmap != null){
            synchronized (mLruCache){
                mLruCache.put(url,bitmap);
            }
        }
    }

    /**
     * 重置软引用缓存
     */
    public void clearCache(){
        mSoftCache.clear();
    }
}
