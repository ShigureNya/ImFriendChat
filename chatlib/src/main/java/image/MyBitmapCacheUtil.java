package image;

import android.graphics.Bitmap;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.widget.ImageView;

import com.chengzi.chengzilib_master.R;

import java.util.HashMap;

import util.BitmapUtils;
import util.LogUtils;

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
     * 三级缓存策略-图像
     * @param ivPic 设置的Imageview
     * @param url 图像地址
     */
    public void disPlayImage(ImageView ivPic, String url , ContentLoadingProgressBar progress) {
        ivPic.setImageResource(R.mipmap.ic_launcher);
        //内存缓存
        Bitmap bitmap=memoryCacheUtil.getBitmapFromMemory(url);
        if (bitmap!=null){
            progress.hide();
            zoomBitmap(bitmap,ivPic);
            System.out.println("从内存获取图片啦.....");
            return;
        }

        //本地缓存
        bitmap = localCacheUtil.getBitmapFromLocal(url);
        if(bitmap !=null){
            progress.hide();
            //处理图片大小
            zoomBitmap(bitmap,ivPic);
            System.out.println("从本地获取图片啦.....");
            //从本地获取图片后,保存至内存中
            memoryCacheUtil.saveBitmapToMemory(url,bitmap);
            return;
        }
        //网络缓存
        netCacheUtil.downloadBitmapFromNet(url,ivPic,progress);
    }
    /**
     * 三级缓存策略-头像
     * @param ivPic 设置的Imageview
     * @param url 图像地址
     */
    public void disPlayImage(ImageView ivPic, String url) {
        ivPic.setImageResource(R.mipmap.ic_launcher);
        //内存缓存
        Bitmap bitmap=memoryCacheUtil.getBitmapFromMemory(url);
        if (bitmap!=null){
            zoomBitmap(bitmap,ivPic);
            System.out.println("从内存获取图片啦.....");
            return;
        }

        //本地缓存
        bitmap = localCacheUtil.getBitmapFromLocal(url);
        if(bitmap !=null){
            //处理图片大小
            zoomBitmap(bitmap,ivPic);
            System.out.println("从本地获取图片啦.....");
            //从本地获取图片后,保存至内存中
            memoryCacheUtil.saveBitmapToMemory(url,bitmap);
            return;
        }
        //网络缓存
        netCacheUtil.downloadBitmapFromNet(url,ivPic,null);
    }
    private void zoomBitmap(Bitmap bitmap,ImageView imageView){
        HashMap<String,Integer> hashMap = BitmapUtils.djustImageSize(bitmap);
        if(hashMap != null){
            //从HashMap获取长度宽度
            Integer width = hashMap.get("Width");
            Integer height = hashMap.get("Height");
            LogUtils.d("Width:"+width+",Height:"+height);
            //使用新的长宽定义Bitmap
            Bitmap newBitmap = BitmapUtils.zoomBitmap(bitmap,width,height);
            imageView.setImageBitmap(newBitmap);
        }
    }
}
