package tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Created by Kotori on 2016/5/24.
 */
public class ImageLoader {
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;

    private static int sImageWidth;

    private static Context sContext;
    private static ImageLoader sInstance;
    private static ExecutorService sThreadPool = Executors.newCachedThreadPool();
    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskCache;

    /**
     * 初始化ImageLoader
     * @param context
     * @param width 预想的图片宽度
     */
    public static void init(Context context, int width) {
        sContext = context;
        sImageWidth = width;
    }

    /**
     * 单例 获取ImageLoader的实例
     * @return
     */
    public synchronized static ImageLoader getInstance() {
        if(null == sInstance) {
            sInstance = new ImageLoader();
        }
        return sInstance;
    }

    private ImageLoader() {
        // begin 初始化LruCache
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int maxSize = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        // end

        // begin 初始化DiskLruCache
        try {
            mDiskCache = DiskLruCache.open(getCacheDir(), getAppVersion(), 1, 10*1024*1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // end
    }

    public void load(final String url, final OnImageListener l) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS:
                        l.onResult((Bitmap) msg.obj);
                        break;
                    case FAILED:
                        l.onResult(null);
                        break;
                }
            }
        };

        // 从memory中获取
        Bitmap bmp = getFromLru(url);
        if(null != bmp) {
            System.out.println("--getFromLru");
            Message msg = handler.obtainMessage(SUCCESS, bmp);
            msg.sendToTarget();
            return;
        }

        // 如果memory中没有
        // 从disk中获取
        bmp = getFromDisk(url);
        if(null != bmp) {
            System.out.println("---getFromDisk");
            Message msg = handler.obtainMessage(SUCCESS, bmp);
            msg.sendToTarget();
            return;
        }

        // 如果disk中没有， 则从网络中下载
        sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("----getFromNet");
                DefaultHttpClient client = null;
                try {
                    client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(url);
                    HttpResponse response = client.execute(get);
                    if(200 == response.getStatusLine().getStatusCode()) {
                        InputStream in = response.getEntity().getContent();
                        Bitmap bmp = BitmapFactory.decodeStream(in);
                        bmp = scaleImage(bmp);

                        // 缓存到本地
                        addToDisk(url, bmp);
                        // 缓存到memory
                        addToLru(url, bmp);
                        Message msg = handler.obtainMessage(SUCCESS, bmp);
                        msg.sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(FAILED);
                } finally {
                    if(null != client) {
                        client.getConnectionManager().shutdown();
                    }
                }
            }
        });
    }

    // 缩放图片
    public Bitmap scaleImage(Bitmap bmp) {
        int sample = bmp.getWidth() / sImageWidth;
        int height = bmp.getHeight() / sample;
        return ThumbnailUtils.extractThumbnail(bmp, sImageWidth, height);
    }

    // 从memory中获取
    public Bitmap getFromLru(String url) {
        return mLruCache.get(MD5Utils.getInstance().md5(url));
    }

    // 添加到内存中
    public void addToLru(String url, Bitmap bmp) {
        if(getFromLru(url) == null) {
            System.out.println("++addToLru");
            mLruCache.put(MD5Utils.md5(url), bmp);
        }
    }

    // 从本地缓存获取
    public Bitmap getFromDisk(String url) {
        Bitmap bmp = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskCache.get(MD5Utils.md5(url));
            InputStream in = snapshot.getInputStream(0);
            bmp = BitmapFactory.decodeStream(in);
            addToLru(url, bmp); // 这里可以断言 内存中肯定没有
            in.close();
        } catch (Exception e) {
        }
        return bmp;
    }

    // 添加到本地缓存
    public void addToDisk(String url, Bitmap bmp) throws Exception {
        System.out.println("+++addtoDisk");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] buf = baos.toByteArray();

        DiskLruCache.Editor editor = mDiskCache.edit(MD5Utils.getInstance().md5(url));
        OutputStream out = editor.newOutputStream(0);
        out.write(buf);
        out.flush();
        editor.commit();
    }

    // 获取缓存目录
    public File getCacheDir() {
        File dir = null;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = new File(sContext.getExternalCacheDir().getPath() + File.separator + "images" + File.separator);
        }else {
            dir = new File(sContext.getCacheDir().getPath() + File.separator + "images" + File.separator);
        }
        return dir;
    }

    // 获取软件版本
    public int getAppVersion() {
        PackageInfo pi;
        try {
            pi = sContext.getPackageManager().getPackageInfo(sContext.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public interface OnImageListener {
        public void onResult(Bitmap bmp);
    }
}
