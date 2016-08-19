package image;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.chengzi.chengzilib_master.R;

/**
 * Created by jimhao on 16/8/19.
 */
public class MyBitmapCacheUtil {
    private NetCacheUtil netCacheUtil ;
    private MemoryCacheUtil memoryCacheUtil ;
    private LocalCacheUtil localCacheUtil ;

    public MyBitmapCacheUtil(){
        memoryCacheUtil = new MemoryCacheUtil() ;
        localCacheUtil = new LocalCacheUtil() ;
        netCacheUtil = new NetCacheUtil(memoryCacheUtil,localCacheUtil);
    }

    /**
     * 三级缓存策略
     * @param ivPic 设置的Imageview
     * @param url 图像地址
     */
    public void disPlay(ImageView ivPic, String url) {
        ivPic.setImageResource(R.mipmap.ic_launcher);
        Bitmap bitmap;
        //内存缓存
        bitmap=memoryCacheUtil.getBitmapFromMemory(url);
        if (bitmap!=null){
            ivPic.setImageBitmap(bitmap);
            System.out.println("从内存获取图片啦.....");
            return;
        }

        //本地缓存
        bitmap = localCacheUtil.getBitmapFromLocal(url);
        if(bitmap !=null){
            ivPic.setImageBitmap(bitmap);
            System.out.println("从本地获取图片啦.....");
            //从本地获取图片后,保存至内存中
            memoryCacheUtil.saveBitmapToMemory(url,bitmap);
            return;
        }
        //网络缓存
        netCacheUtil.bitmapTask(url,ivPic);
    }
}
