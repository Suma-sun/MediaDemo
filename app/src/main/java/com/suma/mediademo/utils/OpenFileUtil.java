package com.suma.mediademo.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.util.Locale;

import androidx.core.content.FileProvider;

/**
 * intent打开指定文件工具 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-09-27]
 */
public class OpenFileUtil {
	public static Intent openFile(Context context, String filePath) {
		if (context == null)
			return null;
		File file = new File(filePath);
		if (!file.exists())
			return null;
		String authority = context.getApplicationContext().getPackageName() + ".FileProvider";
		/* 取得扩展名 */
		String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase(Locale.getDefault());
		/* 依扩展名的类型决定MimeType */
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			return getAudioFileIntent(context, authority, filePath);
		} else if (end.equals("3gp") || end.equals("mp4")) {
			return getVideoFileIntent(context, authority, filePath);
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
			return getImageFileIntent(context, authority, filePath);
		} else if (end.equals("apk")) {
			return getApkFileIntent(context, authority, filePath);
		} else if (end.equals("ppt")) {
			return getPptFileIntent(context, authority, filePath);
		} else if (end.equals("xls")) {
			return getExcelFileIntent(context, authority, filePath);
		} else if (end.equals("doc")) {
			return getWordFileIntent(context, authority, filePath);
		} else if (end.equals("pdf")) {
			return getPdfFileIntent(context, authority, filePath);
		} else if (end.equals("chm")) {
			return getChmFileIntent(context, authority, filePath);
		} else if (end.equals("txt")) {
			return getTextFileIntent(context, authority, filePath);
		} else {
			return getAllIntent(context, authority, filePath);
		}
	}

	// Android获取一个用于打开APK文件的intent
	public static Intent getAllIntent(Context context, String authority, String param) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "*/*");
		return intent;
	}

	// Android获取一个用于打开APK文件的intent
	public static Intent getApkFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		return intent;
	}

	// Android获取一个用于打开VIDEO文件的intent
	public static Intent getVideoFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	// Android获取一个用于打开AUDIO文件的intent
	public static Intent getAudioFileIntent(Context context, String authority, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "audio/*");
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		return intent;
	}

	/**
	 * 根据手机版本通过不同方式获取Uri
	 *
	 * @param context
	 * @param authority
	 * @param param
	 * @return
	 */
	private static Uri getUriForFile(Context context, String authority, String param) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			return FileProvider.getUriForFile(context, authority, new File(param));
		else
			return Uri.fromFile(new File(param));
	}

	// Android获取一个用于打开Html文件的intent
	public static Intent getHtmlFileIntent(String param) {

		Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	// Android获取一个用于打开图片文件的intent
	public static Intent getImageFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	// Android获取一个用于打开PPT文件的intent
	public static Intent getPptFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	// Android获取一个用于打开Excel文件的intent
	public static Intent getExcelFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	// Android获取一个用于打开Word文件的intent
	public static Intent getWordFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	// Android获取一个用于打开CHM文件的intent
	public static Intent getChmFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "application/x-chm");
		return intent;
	}

	// Android获取一个用于打开文本文件的intent
	public static Intent getTextFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri2 = getUriForFile(context, authority, param);
		intent.setDataAndType(uri2, "text/plain");
		return intent;
	}

	// Android获取一个用于打开PDF文件的intent
	public static Intent getPdfFileIntent(Context context, String authority, String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = getUriForFile(context, authority, param);
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}
}
