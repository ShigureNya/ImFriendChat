package image;

import android.database.Observable;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.util.HashMap;

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
    public void downloadBitmapFromNet(String url , ImageView imageView){
        BitmapTask task = new BitmapTask();
        task.execute(url,imageView);    //启动AsyncTask
    }
    class BitmapTask extends AsyncTask<Object,Void,Bitmap>{
        public ImageView imageView ;
        public String url ;
        @Override
        protected Bitmap doInBackground(Object... params) {
            url = (String) params[0];
            imageView = (ImageView) params[1];
            Bitmap bitmap = ImageDownload.getInstance().downLoadBitmap(url);
            if(bitmap != null){
                return bitmap ;
            }
            return null ;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            HashMap<String,Integer> hashMap = BitmapUtils.djustImageSize(bitmap);
            if(hashMap != null){
                //从HashMap获取长度宽度
                Integer width = hashMap.get("Width");
                Integer height = hashMap.get("Height");
                //使用新的长宽定义Bitmap
                Bitmap newBitmap = BitmapUtils.zoomBitmap(bitmap,width,height);
                imageView.setImageBitmap(newBitmap);
                LogUtils.i("从网络获取啦..");
                //保存到本地
                localCacheUtil.saveBitmapToLocal(url,newBitmap);
                //保存到内存
                memoryCacheUtil.saveBitmapToMemory(url,newBitmap);
            }
        }
    }
}
