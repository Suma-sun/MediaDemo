package com.suma.mediademo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 常量类 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-09-14]
 */
public class Constant {

	public static final String POJECT_NAME = "Meadia_Demo";
	private static String mRootPath;

	/** 音频文件夹名 */
	public static final String AUDIO_DIR = "audio";
	/** 图片文件夹名 */
	public static final String PICTURE_DIR = "picture";
	/** jpg文件扩展名 */
	public static final String PICTURE_EXTENSION = "jpg";
	/** PCM文件扩展名 */
	public static final String PCM_EXTENSION = "pcm";
	/** WAV文件扩展名 */
	public static final String WAV_EXTENSION = "wav";


	public static void init(Context context){
		Context app = context.getApplicationContext();
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File file = Environment.getExternalStorageDirectory();
			if (file != null)
				mRootPath = file.getAbsolutePath() + File.separator + POJECT_NAME + File.separator;
		} else {
			mRootPath = FileUtils.getRootPath(app);
		}
	}

	/**
	 * 获取应用存储根目录
	 */
	public static String getAppRootDir(){
		return mRootPath;
	}
}
