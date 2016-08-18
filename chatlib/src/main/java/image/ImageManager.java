package image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import util.LogUtils;

/*
 * 图片管理
 * 异步获取图片，直接调用loadImage()函数，该函数自己判断是从缓存还是网络加载
 * 同步获取图片，直接调用getBitmap()函数，该函数自己判断是从缓存还是网络加载
 * 仅从本地获取图片，调用getBitmapFromNative()
 * 仅从网络加载图片，调用getBitmapFromHttp()
 *
 */
public class ImageManager {
    private final static String TAG = ImageManager.class.getSimpleName();

    private ImageFileCache imageFileCache ; //文件缓存

    private ImageMemoryCache imageMemoryCache ; //内存缓存
    //正在下载的Image列表
    public static HashMap<String, android.os.Handler> downloadingImgTaskMap = new HashMap<String, android.os.Handler>();
    //等待下载的Image列表
    public static HashMap<String, android.os.Handler> waitingImgTaskMap = new HashMap<String, android.os.Handler>();
    //同时下载的线程个数
    public static final int MAX_DOWNLOAD_IMAGE_THREAD = 4 ;


    public ImageManager(){
        imageFileCache = new ImageFileCache() ;
        imageMemoryCache = new ImageMemoryCache() ;
    }
    private final android.os.Handler downloadStatusHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            startDownloadNext();
        }
    };

    /**
     * 获取图片，多线程的入口
     */
    public void loadBitmap(String url, android.os.Handler handler)
    {
        //先从内存缓存中获取，取到直接加载
        Bitmap bitmap = getBitmapFromNative(url);

        if (bitmap != null)
        {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            msg.obj = bitmap;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
        else
        {
            downloadBmpOnNewThread(url, handler);
        }
    }

    /**
     * 新起线程下载图片
     */
    private void downloadBmpOnNewThread(final String url, final android.os.Handler handler)
    {

        if (downloadingImgTaskMap.size() >= MAX_DOWNLOAD_IMAGE_THREAD)
        {
            synchronized (waitingImgTaskMap)
            {
                waitingImgTaskMap.put(url, handler);
            }
        }
        else
        {
            synchronized (downloadingImgTaskMap)
            {
                downloadingImgTaskMap.put(url, handler);
            }

            new Thread()
            {
                public void run()
                {
                    Bitmap bmp = getBitmapFromHttp(url);

                    // 不论下载是否成功，都从下载队列中移除,再由业务逻辑判断是否重新下载
                    // 下载图片使用了httpClientRequest，本身已经带了重连机制
                    synchronized (downloadingImgTaskMap)
                    {
                        downloadingImgTaskMap.remove(url);
                    }

                    if(downloadStatusHandler != null)
                    {
                        downloadStatusHandler.sendEmptyMessage(0);

                    }

                    Message msg = Message.obtain();
                    msg.obj = bmp;
                    Bundle bundle = new Bundle();
                    bundle.putString("url", url);
                    msg.setData(bundle);

                    if(handler != null)
                    {
                        handler.sendMessage(msg);
                    }

                }
            }.start();
        }
    }


    /**
     * 依次从内存，缓存文件，网络上加载单个bitmap,不考虑线程的问题
     */
    public Bitmap getBitmap(String url)
    {
        // 从内存缓存中获取图片
        Bitmap bitmap = imageMemoryCache.getBitmap4Memory(url);
        if (bitmap == null)
        {
            // 文件缓存中获取
            bitmap = imageFileCache.getBitmap4File(url);
            LogUtils.d("1.从文件缓存里得到图片");
            if (bitmap != null){
                // 添加到内存缓存
                imageMemoryCache.addBitmapCache(url, bitmap);
                LogUtils.i("2.添加到内存缓存");
            }else
            {
                // 从网络获取
                bitmap = getBitmapFromHttp(url);
                LogUtils.i("3.从网络获取图片");
            }
        }
        return bitmap;
    }

    /**
     * 从内存或者缓存文件中获取bitmap
     */
    public Bitmap getBitmapFromNative(String url)
    {
        Bitmap bitmap = null;
        bitmap = imageMemoryCache.getBitmap4Memory(url);

        if(bitmap == null)
        {
            bitmap = imageFileCache.getBitmap4File(url);
            if(bitmap != null)
            {
                // 添加到内存缓存
                imageMemoryCache.addBitmapCache(url, bitmap);
            }
        }
        return bitmap;
    }

    /**
     * 通过网络下载图片,与线程无关
     */
    public Bitmap getBitmapFromHttp(String url)
    {
        Bitmap bmp = null;

        try
        {
            byte[] tmpPicByte = getImageBytes(url);

            if (tmpPicByte != null)
            {
                bmp = BitmapFactory.decodeByteArray(tmpPicByte, 0,
                        tmpPicByte.length);
            }
            tmpPicByte = null;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        if(bmp != null)
        {
            // 添加到文件缓存
            imageFileCache.saveBitmap4File(bmp, url);
            // 添加到内存缓存
            imageMemoryCache.addBitmapCache(url, bmp);
        }

        return bmp;
    }

    /**
     * 下载链接的图片资源
     *
     * @param url
     *
     * @return 图片
     */
    public byte[] getImageBytes(String url) {
        try {
            URL str = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)str.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
            byte[] btImg = readInputStream(inStream);//得到图片的二进制数据
            return btImg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    /**
     * 从输入流中获取数据
     * @param inStream 输入流
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }
    /**
     * 取出等待队列第一个任务，开始下载
     */
    private void startDownloadNext()
    {
        synchronized(waitingImgTaskMap)
        {
            Iterator iter = waitingImgTaskMap.entrySet().iterator();

            while (iter.hasNext())
            {

                Map.Entry entry = (Map.Entry) iter.next();

                if(entry != null)
                {
                    waitingImgTaskMap.remove(entry.getKey());
                    downloadBmpOnNewThread((String)entry.getKey(), (android.os.Handler) entry.getValue());
                }
                break;
            }
        }
    }

    public String startDownloadNext_ForUnitTest()
    {
        String urlString = null;
        synchronized(waitingImgTaskMap)
        {
            Iterator iter = waitingImgTaskMap.entrySet().iterator();

            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();
                urlString = (String)entry.getKey();
                waitingImgTaskMap.remove(entry.getKey());
                break;
            }
        }
        return urlString;
    }

    /**
     * 图片变为圆角
     * @param bitmap:传入的bitmap
     * @param pixels：圆角的度数，值越大，圆角越大
     * @return bitmap:加入圆角的bitmap
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels)
    {
        if(bitmap == null)
            return null;

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    public byte[] getByteArray4Bitmap(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes=baos.toByteArray();
        baos.flush();
        baos.close();

        return bytes;
    }

}
