package com.suma.mediademo.target4;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.suma.mediademo.BaseFragment;
import com.suma.mediademo.utils.Constant;
import com.suma.mediademo.utils.FileIOUtils;
import com.suma.mediademo.utils.FileUtils;
import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.OpenFileUtil;
import com.suma.mediademo.utils.StringUtils;
import com.suma.mediademo.utils.UiUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * MP4提取器与合成 <P>
 * 将mp4文件分离为独立的音频与视频文件<br>
 * 将音频文件与视频文件封装成mp4文件<br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-09]
 */
public class MediaExtractorAndMuxerFragment extends BaseFragment {
	private static final String SRC_FILE_NAME = "origin.mp4";
	private static final String DIR_NAME = "target4";
	private static final String ERR_MSG_COPY_FAIL = "拷贝原文件失败";
	private static final String ERR_MSG_EXTRACT = "提取原文件失败";
	/**
	 * 缓冲区大小
	 */
	private static final int BUFFER_SIZE = 10240;
	private static final String TAG_VIDEO = "video";
	private static final String TAG_AUDIO = "audio";

	private static final String VIDEO_NAME = "test.h264";
	private static final String AUDIO_NAME = "test.aac";

	private Button mBtnPlayOrigin;
	private Button mBtnSeparate;


	private File mDir;
	private File mOriginFile;
	private File mAudioFile;
	private File mVideoFile;
	private ExecutorService mThreadPool;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDir = new File(Constant.getAppRootDir() + DIR_NAME);
		mOriginFile = new File(mDir, SRC_FILE_NAME);
		mAudioFile = new File(mDir,AUDIO_NAME);
		mVideoFile = new File(mDir,VIDEO_NAME);
		mThreadPool = Executors.newSingleThreadExecutor();
		//将视频文件拷出指定目录
		AssetManager assetManager = getActivity().getAssets();
		if (assetManager == null) {
			toastCopyErr("AssetManager is null", ERR_MSG_COPY_FAIL);
			return;
		}
		if (mOriginFile.exists()) {
			if (!mOriginFile.delete()) {
				toastCopyErr("delete origin file fail", ERR_MSG_COPY_FAIL);
			}

		}
		if (!FileUtils.createOrExistsFile(mOriginFile)) {
			toastCopyErr("create origin file fail", ERR_MSG_COPY_FAIL);
			return;
		}
		try {
			InputStream inputStream = assetManager.open(SRC_FILE_NAME);
			boolean result = FileIOUtils.writeFileFromIS(mOriginFile, inputStream);
			if (result) {
				Log.i(this, "copy src success");
				return;
			}
		} catch (IOException e) {
			Log.e(this, e);
		}
		toastCopyErr("copy origin file fail", ERR_MSG_COPY_FAIL);
	}

	private void toastCopyErr(String logMsg, String errMsg) {
		Log.e(this, new Exception(logMsg));
		UiUtils.showToast(getActivity(), errMsg);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		LinearLayout view = new LinearLayout(getContext());
		view.setOrientation(LinearLayout.VERTICAL);
		view.setPadding(0, 500, 0, 0);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		view.setLayoutParams(params);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		initView();
		bindListener();
		view.addView(mBtnPlayOrigin, params1);
		view.addView(mBtnSeparate, params1);
		return view;
	}

	private void initView() {
		mBtnPlayOrigin = new Button(getContext());
		mBtnPlayOrigin.setText("播放原文件");
		mBtnSeparate = new Button(getContext());
		mBtnSeparate.setText("分离视频");
	}

	private void bindListener() {
		mBtnPlayOrigin.setOnClickListener(v -> {
			Intent intent = OpenFileUtil.openFile(getContext(), mOriginFile.getPath());
			if (intent != null)
				getActivity().startActivity(intent);
		});
		mBtnSeparate.setOnClickListener(v -> {
			mThreadPool.submit(new ExtractRunnable(this,mOriginFile,mAudioFile,mVideoFile));
		});
	}


	/**
	 * 提取目标文件中的音频\视频轨道数据,并输出文件
	 */
	private static class ExtractRunnable implements Runnable {
		private WeakReference<BaseFragment> reference;
		private File mSrcFile;
		private File mAudioFile;
		private File mVideoFile;

		public ExtractRunnable(BaseFragment fragment, File src, File audio, File video) {
			this.reference = new WeakReference<>(fragment);
			this.mSrcFile = src;
			this.mAudioFile = audio;
			this.mVideoFile = video;
		}

		@Override
		public void run() {
			BaseFragment fragment = reference.get();
			if (fragment == null)
				return;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				fragment.showToast("手机版本过低,需4.1及以上");
				return;
			}

			MediaExtractor extractor = new MediaExtractor();
			try {
				//亦可设置为url地址
				extractor.setDataSource(mSrcFile.getPath());
			} catch (IOException e) {
				Log.e(this, e);
				fragment.showToast(ERR_MSG_EXTRACT);
				return;
			}
			//获取轨道数量
			final int count = extractor.getTrackCount();
			for (int i = 0; i < count; i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				//获取轨道类型
				String mime = format.getString(MediaFormat.KEY_MIME);
				//筛选类型,
				// audio/mp4a-latm 对应aac
				// video/avc 对应h.264编码
				Log.d(this, "mime : " + mime);
				if (MediaFormat.MIMETYPE_AUDIO_AAC.equals(mime)) {
					//选中当前轨道
					extractor.selectTrack(i);
					if (!extract(extractor,format,mAudioFile,TAG_AUDIO)) {
						fragment.showToast("音频提取失败");
					}
				} else if (MediaFormat.MIMETYPE_VIDEO_AVC.equals(mime)) {
					//选中当前轨道
					extractor.selectTrack(i);
					if (extract(extractor,format,mVideoFile,TAG_VIDEO)) {
						fragment.showToast("视频提取失败");
					}
				}
			}
			extractor.release();
			fragment.showToast("提取完毕");

		}

		/**
		 * 抽取当前轨道并输出文件
		 * @param format 当前轨道信息
		 * @param file 输出的文件
		 * @param tag {@link #TAG_AUDIO} or {@link #TAG_VIDEO}
		 * @return 是否抽取输出文件成功
		 */
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		private boolean extract(MediaExtractor extractor, MediaFormat format, File file, String tag) {
			//打印当前轨道信息
			Log.d(this, StringUtils.format("==%s==", tag));
			Log.d(this, StringUtils.format("==%s==mime==%s", tag, format.getString(MediaFormat.KEY_MIME)));
			Log.d(this, StringUtils.format("==%s==duration==%s", tag, format.getLong(MediaFormat.KEY_DURATION)));
			Log.d(this, StringUtils.format("==%s==flags==%d", tag, extractor.getSampleFlags()));
			Log.d(this, StringUtils.format("==%s==sampleTime==%d", tag, extractor.getSampleTime()));
			Log.d(this, StringUtils.format("==%s==TrackIndex==%d", tag, extractor.getSampleTrackIndex()));
			//删除旧文件后创建
			FileUtils.createFileByDeleteOldFile(file);

			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			try {
				FileOutputStream outputStream = new FileOutputStream(file);
				int count;
				while ((count = extractor.readSampleData(buffer, 0)) > 0) {
					outputStream.write(buffer.array(), 0, count);
					buffer.clear();
					//下一帧
					extractor.advance();
				}
				outputStream.close();
				return true;
			} catch (FileNotFoundException e) {
				Log.e(this, e);
			} catch (IOException e) {
				Log.e(this, e);
			}
			return false;
		}
	}

}
