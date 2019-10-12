package com.suma.mediademo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * UI相关工具类 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-06-27]
 */
public class UiUtils {

	private static Toast mToast = null;

	private UiUtils() throws Exception {
		throw new Exception("不允许创建实例");
	}

	/**
	 * 弹框显示,可在非UI线程调用
	 *
	 * @param context 上下文
	 * @param toastId 资源Id
	 */
	public static void showToast(@Nullable Context context, int toastId) {
		if (context == null)
			return;
		final Context application = context.getApplicationContext();
		showToast(application, application.getString(toastId));
	}

	/**
	 * 弹框显示,可在非UI线程调用
	 *
	 * @param context 上下文
	 * @param text    显示的文本
	 */
	public static void showToast(@Nullable Context context, String text) {
		if (context == null)
			return;
		if (!isRunOnUiThread()) {
			Looper.prepare();
			showCustomToast(context, text);
			Looper.loop();
		} else {
			showCustomToast(context, text);
		}
	}

	/**
	 * 弹框显示,不可在非UI线程调用
	 *
	 * @param context 上下文
	 * @param text    显示的文本
	 */
	private static void showCustomToast(@NonNull Context context, String text) {
		if (mToast == null) {
			mToast = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT);
		}
		mToast.setText(text);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.show();
	}


	/**
	 * 判断是都UI线程
	 */
	public static boolean isRunOnUiThread() {
		return Thread.currentThread().getId() == Looper.getMainLooper()
				.getThread().getId();
	}



	/**
	 * 获取屏幕宽高度
	 *
	 * @param context
	 * @return
	 * @CreateData 2016年9月13日 下午4:59:31
	 */
	public static Point getScreenPoin(@NonNull Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		Point point = new Point(dm.widthPixels, dm.heightPixels);
		return point;
	}


	/**
	 * 根据图片文件,获取缩放后的Bitmap
	 * @param path 文件路径
	 * @param width 目标宽度
	 * @param height 目标高度
	 */
	public static Bitmap getFileBitmap(String path, int width, int height){
		BitmapFactory.Options options = new BitmapFactory.Options();
		//inJustDecoedBounds设置为true的话，解码bitmap时可以只返回其高、宽和Mime类型，而不必为其申请内存，从而节省了内存空间。
		options.inJustDecodeBounds = true;
		//读取图片宽高
		BitmapFactory.decodeFile(path,options);
		//设置图片缩放比例
		options.inSampleSize = getInSampleSize(width,height,options);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path,options);
	}

	/**
	 * 根据图片资源,获取缩放后的bitmap
	 * @param resources 资源实例
	 * @param id 资源id
	 * @param width 目标宽度
	 * @param height 目标高度
	 */
	public static Bitmap getResBitmap(Resources resources, @DrawableRes int id, int width, int height){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources,id,options);
		options.inSampleSize = getInSampleSize(width,height,options);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(resources,id,options);
	}

	/**
	 * 将输入流转化为图片
	 * @param stream 图片输入流
	 * @param width 目标宽度
	 * @param height 目标高度
	 * @throws IOException
	 */
	public static Bitmap getStreamBitmap(InputStream stream, int width, int height) throws IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		byte[] bytes = readStream(stream);
		BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
		options.inSampleSize = getInSampleSize(width,height,options);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
	}

	/**
	 * 根据输入流,获取内容byte数组
	 * @throws IOException
	 */
	public static byte[] readStream(InputStream stream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte [] buffur = new byte[1024];
		int len = 0;
		while((len =stream.read(buffur)) > 0){
			outputStream.write(buffur,0,len);
		}
		stream.close();
		outputStream.close();
		return outputStream.toByteArray();
	}



	/**
	 * 获取缩放比例
	 * @param requestWidth
	 * @param requestHeight
	 * @param options
	 * @return
	 */
	public static int getInSampleSize(int requestWidth, int requestHeight, BitmapFactory.Options options){
		int inSampleSize = 1;
		if (options.outWidth > requestWidth || options.outHeight > requestHeight){
			int widthRation = Math.round((float) options.outWidth / (float)requestWidth);
			int heightRatio = Math.round((float)options.outHeight / (float)requestHeight);
			inSampleSize = Math.min(widthRation, heightRatio);
		}
		return inSampleSize;
	}



}
