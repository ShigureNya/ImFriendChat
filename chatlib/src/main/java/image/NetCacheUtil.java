package image;

import android.database.Observable;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.chengzi.chengzilib_master.R;

import java.util.HashMap;

import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import util.BitmapUtils;
import util.LogUtils;

/**
 * Created by jimhao on 16/8/19.
 */
public class NetCacheUtil {
    public MemoryCacheUtil memoryCacheUtil ;
    public LocalCacheUtil localCacheUtil ;
    public NetCacheUtil(MemoryCacheUtil memoryCacheUtil , LocalCacheUtil localCacheUtil){
        this.memoryCacheUtil = memoryCacheUtil;
        this.localCacheUtil = localCacheUtil;
    }

    /**
     * 通过BitmapTask来实现图片的异步下载
     */
    public void bitmapTask(final String url , final ImageView imageView){
        imageView.setImageResource(R.mipmap.ic_launcher);
        rx.Observable<Bitmap> observable = rx.Observable.create(new rx.Observable.OnSubscribe<Bitmap>(){

            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = ImageDownload.getInstance().downLoadBitmap(url);
                if(bitmap != null){
                    subscriber.onNext(bitmap);
                    subscriber.onCompleted();
                }else{
                    subscriber.onError(new Throwable("下载文件失败"));
                }
            }
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
            @Override
            public void onCompleted() {
                LogUtils.i("从网络下载成功");
            }

            @Override
            public void onError(Throwable throwable) {
                LogUtils.e("从网络下载失败");
            }

            @Override
            public void onNext(Bitmap bitmap) {
                //得到Bitmap缩放后的长度
                HashMap<String,Integer> hashMap = BitmapUtils.djustImageSize(bitmap);
                if(hashMap != null){
                    //从HashMap获取长度宽度
                    Integer width = hashMap.get("Width");
                    Integer height = hashMap.get("Height");
                    //使用新的长宽定义Bitmap
                    Bitmap newBitmap = BitmapUtils.zoomBitmap(bitmap,width,height);
                    imageView.setImageBitmap(newBitmap);
                    LogUtils.i("从网络获取啦..");
                }
            }
        });
    }
}
