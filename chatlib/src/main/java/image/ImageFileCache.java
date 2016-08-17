package image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import util.LogUtils;

/**
 * Created by jimhao on 16/8/17.
 */
public class ImageFileCache {
    public static final String TAG = ImageFileCache.class.getSimpleName() ;
    //图片缓存目录
    private static final String IMAGE_CACHE_DIR = Environment.getExternalStorageDirectory().getPath()+"/"+"JimBlog";
    //保存的cache文件拓展名
    private static final String CACHE_FILE_NAME = ".cache";
    //定义M的大小
    private static final int MB = 1024 * 1024 ;
    //定义缓存的大小
    private static final int CACHE_SIZE = 1 ;
    //当SDK卡剩余10M时清空缓存
    private static final int FREE_SD_TO_CAHCE = 10 ;
    public ImageFileCache(){
        removeCache(IMAGE_CACHE_DIR);
    }
    /**
     * 将图片保存进缓存
     * @param bitmap 图片
     * @param url 图片URL
     * */
    public void saveBitmap4File(Bitmap bitmap , String url){
        if(bitmap == null){
            LogUtils.e("图片为空");
            return ;
        }
        //判断sdcard上的空间
        if(FREE_SD_TO_CAHCE > SdCardFreeSpace()){
            LogUtils.e("内存空间不足");
            return;
        }
        String filename = convertUrlToFileName(url);
        File dirFile = new File(IMAGE_CACHE_DIR);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }
        File file = new File(IMAGE_CACHE_DIR + "/" + filename);
        try {
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();
        }catch (IOException e){
            LogUtils.e("保存错误:"+e.toString());
        }
    }
    /**
     * 清空缓存
     * @param url 从缓存中取出bitmap
     * */
    public Bitmap getBitmap4File(final String url){
        final String path = IMAGE_CACHE_DIR+"/"+convertUrlToFileName(url);
        File file = new File(path);
        if(file != null && file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if(bitmap == null){
                file.delete();
            }else{
                updateFileTime(path);
                return bitmap ;
            }
        }
        return null ;
    }
    /**
     * 清空缓存
     * @param dirPath 文件地址
     * */
    public boolean removeCache(String dirPath){
        File dir = new File(dirPath);
        File[] files = dir.listFiles() ;
        //如果当前缓存目录为空的话 则表示没有图片 返回True
        if(files == null){
            return true ;
        }
        //如果SDK卡不存在 返回False
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            return false;
        }
        int dirSize = 0 ;
        for(int i = 0 ; i < files.length ; i++){
            //如果当前缓存列表中文件名存在.cache目录结尾的
            if(files[i].getName().contains(CACHE_FILE_NAME)){
                dirSize +=files[i].length();
            }
        }
        if (dirSize > CACHE_SIZE * MB || FREE_SD_TO_CAHCE > SdCardFreeSpace())
        {
            int removeFactor = (int) (0.4 * files.length);
            Arrays.sort(files, new FileLastModifSort());
            for (int i = 0; i < removeFactor; i++)
            {
                if (files[i].getName().contains(CACHE_FILE_NAME))
                {
                    files[i].delete();
                }
            }
        }
        if (SdCardFreeSpace() <= CACHE_SIZE)
        {
            return false;
        }

        return true;
    }
    /**
     * 计算SD卡上的剩余空间
     */
    private int SdCardFreeSpace()
    {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
        return (int) sdFreeMB;
    }
    /**
     * 根据文件的最后修改时间进行排序
     */
    private class FileLastModifSort implements Comparator<File>
    {
        public int compare(File file0, File file1)
        {
            if (file0.lastModified() > file1.lastModified())
            {
                return 1;
            }
            else if (file0.lastModified() == file1.lastModified())
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
    }
    /**
     * 将url转成文件名
     */
    private String convertUrlToFileName(String url)
    {
        return url.hashCode() + CACHE_FILE_NAME;
    }
    /**
     * 修改文件的最后修改时间
     */
    public void updateFileTime(String path)
    {
        File file = new File(path);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }
}
