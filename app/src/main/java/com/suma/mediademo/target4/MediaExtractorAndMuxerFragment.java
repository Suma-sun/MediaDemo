package com.suma.mediademo.target4;

import android.content.Intent;
import android.content.res.AssetManager;
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
import com.suma.mediademo.utils.UiUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

	private static final String VIDEO_NAME = "test.mp4";
	private static final String AUDIO_NAME = "test.aac";
	private static final String NEW_NAME = "new.mp4";

	private Button mBtnPlayOrigin;
	private Button mBtnExtractAudio;
	private Button mBtnPlayAudio;
	private Button mBtnExtractVideo;
	private Button mBtnPlayVideo;
	private Button mBtnCreateMp4;
	private Button mBtnPlayNew;


	private File mDir;
	private File mOriginFile;
	private File mAudioFile;
	private File mVideoFile;
	private File mNewFile;//新和成的mp4文件
	private ExecutorService mThreadPool;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDir = new File(Constant.getAppRootDir() + DIR_NAME);
		mOriginFile = new File(mDir, SRC_FILE_NAME);
		mAudioFile = new File(mDir, AUDIO_NAME);
		mVideoFile = new File(mDir, VIDEO_NAME);
		mNewFile = new File(mDir, NEW_NAME);
		mThreadPool = Executors.newCachedThreadPool();
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
		view.addView(mBtnExtractAudio, params1);
		view.addView(mBtnPlayAudio, params1);
		view.addView(mBtnExtractVideo, params1);
		view.addView(mBtnPlayVideo, params1);
		view.addView(mBtnCreateMp4, params1);
		view.addView(mBtnPlayNew, params1);
		return view;
	}

	private void initView() {
		mBtnPlayOrigin = new Button(getContext());
		mBtnPlayOrigin.setText("播放原文件");
		mBtnExtractAudio = new Button(getContext());
		mBtnExtractAudio.setText("分离音频");
		mBtnPlayAudio = new Button(getContext());
		mBtnPlayAudio.setText("打开分离后的音频");
		mBtnExtractVideo = new Button(getContext());
		mBtnExtractVideo.setText("分离视频");
		mBtnPlayVideo = new Button(getContext());
		mBtnPlayVideo.setText("打开分离后的视频");
		mBtnCreateMp4 = new Button(getContext());
		mBtnCreateMp4.setText("音频视频合成mp4");
		mBtnPlayNew = new Button(getContext());
		mBtnPlayNew.setText("播放新合成的mp4");
	}

	private void bindListener() {
		mBtnPlayOrigin.setOnClickListener(v -> {
			Intent intent = OpenFileUtil.openFile(getContext(), mOriginFile.getPath());
			if (intent != null)
				getActivity().startActivity(intent);
		});
		mBtnExtractAudio.setOnClickListener(v -> {
//			mThreadPool.submit(new ExtractRunnable(this,mOriginFile,mAudioFile,mVideoFile));
			mThreadPool.submit(new ExtractAudio(this, mOriginFile.getPath(), mAudioFile));
		});
		mBtnPlayAudio.setOnClickListener(v -> {
			Intent intent = OpenFileUtil.openFile(getContext(), mAudioFile.getPath());
			if (intent != null)
				getActivity().startActivity(intent);
		});
		mBtnExtractVideo.setOnClickListener(v -> {
			mThreadPool.submit(new ExtractVideo(this, mOriginFile.getPath(), mVideoFile));
		});

		mBtnPlayVideo.setOnClickListener(v -> {
			Intent intent = OpenFileUtil.openFile(getContext(), mVideoFile.getPath());
			if (intent != null)
				getActivity().startActivity(intent);
		});
		mBtnCreateMp4.setOnClickListener(v->{
			mThreadPool.submit(new CreateMp4(this,mNewFile,mAudioFile,mVideoFile));
		});
		mBtnPlayNew.setOnClickListener(v->{
			Intent intent = OpenFileUtil.openFile(getContext(), mNewFile.getPath());
			if (intent != null)
				getActivity().startActivity(intent);
		});
	}

}
