package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class BitmapUtils {
	private static final int DEFAULT_WIDTH = 350 ;
	private static final int DEFAULT_HEIGHT = 350 ;

	/*
	* 将Bitmap转换为Drawable
	*
	* */
	public static Drawable getBitmapToDrawable(Bitmap bitmap){
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
		return bitmapDrawable;
	}
	/**
	 * 从资源文件ID得到Drawable
	 *
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Drawable getDrawableById(Context context, int resId) {
		if (context == null) {
			return null;
		}
		return context.getResources().getDrawable(resId);
	}

	/**
	 * 从资源文件ID得到Bitmap
	 *
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap getBitmapById(Context context, int resId) {
		if (context == null) {
			return null;
		}
		return BitmapFactory.decodeResource(context.getResources(), resId);
	}

	/**
	 * 从文件地址中得到Bitmap
	 *
	 * @param filePath
	 * @return
	 */
	public static Bitmap getBitmapByFile(String filePath) {
		return getBitmapByFile(filePath, null);
	}
	/**
	 * 从文件中得到Bitmap
	 *
	 * @param file
	 * @return
	 */
	public static Bitmap getBitmapByFile(File file){
		String path = file.getPath() ;
		LogUtils.i("Path",path);
		return getBitmapByFile(path,null);
	}
	/**
	 * 从文件中得到Bitmap
	 *
	 * @param filePath
	 * @return
	 */
	public static Bitmap getBitmapByFile(String filePath,Options opts) {
		if (filePath == null) {
			return null;
		}
		return BitmapFactory.decodeFile(filePath,opts);
	}
	/**
	 * 从Drawable中得到Bitmap
	 * @param drawable
	 * @return
	 */
	public static Bitmap getBitmapByDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		//构建一个Bitmap
		Bitmap bitmap = Bitmap.createBitmap(
				drawable.getIntrinsicWidth(),	//从drawable中得到实际长度
				drawable.getIntrinsicHeight(),	//从drawable中得到实际宽度
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);	//绘制

		return bitmap;
	}
	/**
	 * 将Bitmap转换成字节数组
	 * @param bitmap
	 * @return
	 */
	public static byte[] bitmapToByteArray(Bitmap bitmap){
		ByteArrayOutputStream baos = null ;
		try {
			baos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 100, baos);
			byte[] array = baos.toByteArray();
			baos.flush();
			baos.close();
			return array ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null ;
	}
	/**
	 * 将字节数组转换成Bitmap
	 * @param data
	 * @return
	 */
	public static Bitmap byteArrayToBitmap(byte[] data){
		if(null == data){
			return null ;
		}
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
	/**
	 * 按指定宽高来缩放图片，不保证宽高比例
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap , int width ,int height){
		if (bitmap == null) {
			return null;
		}
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float)width/bitmapWidth);
		float scaleHeight = ((float)height/bitmapHeight);
		matrix.postScale(scaleWidth,scaleHeight);

		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight,matrix,true);

		return newBitmap;
	}
	/**
	 * 处理图像大小
	 * @param  bitmap 图片
	 * @return Bitmap 处理完成后的图片
	 **/
	public static HashMap<String,Integer> djustImageSize(Bitmap bitmap){
		int width = bitmap.getWidth() ;
		int height = bitmap.getHeight();
		LogUtils.d("Width:"+width+",height:"+height);
		//对图片的大小进行限制
		int displayWidth = 0 ;
		int displayHeight = 0 ;
		if(width > height){
			float dpi = (float)width / height ;
			if(width > DEFAULT_WIDTH){
				displayWidth = DEFAULT_WIDTH;
				displayHeight = (int) (displayWidth / dpi);
			}else{
				displayWidth = width ;
				displayHeight = height ;
			}
		}else{
			float dpi = (float)height / width ;
			if(height > DEFAULT_HEIGHT){
				displayHeight = DEFAULT_HEIGHT ;
				displayWidth = (int) (displayHeight / dpi);
			}else{
				displayWidth = width ;
				displayHeight = height ;
			}
		}
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		map.put("Width",displayWidth);
		map.put("Height",displayHeight);

		return map ;
	}
	/**
	 * 将Bitmap保存到硬盘
	 * @param bitmap
	 * @param filePath
	 * @return
	 */
	public static boolean saveBitmapToDisk(Bitmap bitmap,String filePath){
		CompressFormat format = CompressFormat.JPEG ;	//设置Bitmap扩展名
		final int quality = 100 ;	//品质
		OutputStream stream = null ;
		Log.i("FilePath",filePath);
		try {
			stream = new FileOutputStream(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap.compress(format, quality, stream);
	}
	/**
	 * 从File中加载压缩比例的Bitmap
	 * @return
	 */
	public static Bitmap getFileToBitmapSampleSize(String filePath,int reqWidth,int reqHeight){
		final Options opts = new Options();
		opts.inJustDecodeBounds = true ;	//BitmapFactory只会解析原始的宽高信息
		BitmapFactory.decodeFile(filePath);
		opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
		opts.inJustDecodeBounds = false ;
		return BitmapFactory.decodeFile(filePath, opts);

	}
	/**
	 * 从资源文件中加载压缩比例的Bitmap
	 * @return
	 */
	public static Bitmap getResIdToBitmapSampleSize(Context context,int resId,int reqWidth,int reqHeight){
		if (context == null) {
			return null;
		}
		final Options opts = new Options();
		opts.inJustDecodeBounds = true ;	//BitmapFactory只会解析原始的宽高信息
		BitmapFactory.decodeResource(context.getResources(), resId);
		opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
		opts.inJustDecodeBounds = false ;
		return BitmapFactory.decodeResource(context.getResources(), resId , opts);
	}
	/**
	 * 由控件宽高得到缩放比率
	 * @param opts
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(Options opts,int reqWidth,int reqHeight){
		final int width = opts.outWidth;
		final int height = opts.outHeight;
		int inSampleSize = 1;
		if(height > reqHeight || width > reqWidth){
			final int halfWidth = width / 2 ;
			final int halfHeight = height / 2 ;
			while((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth){
				inSampleSize *= 2 ;
			}
		}
		return inSampleSize ;
	}
}
