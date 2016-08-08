package util;

import android.content.Context;

import java.io.File;

/**
 * Created by Kotori on 2016/5/23.
 */
public class FileTools {
    private Context context ;

    public FileTools(Context context){
        this.context = context;
    }
    /*
    * 查看指定文件是否存在
    * */
    public static boolean isFoundFilePath(String filePath){
        File file = new File(filePath);
        if(isFoundFile(file)){
            return true;
        }
        return false ;
    }
    /*
    * 查看指定File文件是否存在
    * */
    public static boolean isFoundFile(File file){
        if(!file.exists()){
            return false ;
        }
        return true ;
    }
    /**
     * 获取文件的大小
     * @param fileName
     * @return
     */
    public static long getFileSize(String fileName) {
        return new File(fileName).length();
    }
}
