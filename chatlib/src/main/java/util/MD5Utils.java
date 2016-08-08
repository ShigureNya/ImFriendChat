package util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 可用于密码和文件进行单次加密运算，校验两次MD5值是否相同，如果相同则文件或数据没有进行更改
 要注意的是由于MD5算法的特殊性其自身不可进行逆推
 */
public class MD5Utils{
    private static MD5Utils instance = null;
    private MD5Utils(){

    }
    public static MD5Utils getInstance(){
        if(instance == null){
            instance = new MD5Utils();
        }
        return instance;
    }
    /**
     普通文本加密为MD5
     @params str待加密参数
     @return hex返回的MD5值
     */
    public static String md5(String str) {
        //String to byteArray
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}