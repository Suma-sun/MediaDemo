package com.suma.mediademo.target2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.suma.mediademo.utils.Constant;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

/**
 * 录制音频写入文件,读取文件播放音频 <br>
 *
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-07-01]
 */
public class AudioFragment extends Fragment implements View.OnClickListener {

	private Button mBtnRecord;
	private Button mBtnPlayPCM;
	private Button mBtnPlayWAV;
	private Button mBtnStop;
	private AudioImpl mAudioImpl;
	private static final String DIR = Constant.AUDIO_DIR;
	private static final String PCM_FILE_NAME = Constant.AUDIO_DIR + "." + Constant.PCM_EXTENSION;
	private static final String WAV_FILE_NAME = Constant.AUDIO_DIR + "." + Constant.WAV_EXTENSION;
	private String mPCMFileName;
	private String mWAVFileName;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String path = Constant.getAppRootDir();
		mPCMFileName = path + File.separator + DIR + File.separator + PCM_FILE_NAME;
		mWAVFileName = path + File.separator + DIR + File.separator + WAV_FILE_NAME;
		mAudioImpl = new AudioImpl(getContext(), mPCMFileName,mWAVFileName);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		LinearLayout view = new LinearLayout(getContext());
		view.setOrientation(LinearLayout.VERTICAL);
		view.setPadding(0,200,0,0);
		ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		view.setLayoutParams(params);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//		params1.setMargins(16,0,16,200);
		mBtnRecord = new Button(getContext());
		mBtnRecord.setText("录制音频");
		mBtnPlayPCM = new Button(getContext());
		mBtnPlayPCM.setText("播放PCM音频");
		mBtnPlayWAV = new Button(getContext());
		mBtnPlayWAV.setText("播放WAV音频");
		mBtnStop = new Button(getContext());
		mBtnStop.setText("停止");
		view.addView(mBtnRecord,params1);
		view.addView(mBtnPlayPCM,params1);
		view.addView(mBtnPlayWAV,params1);
		view.addView(mBtnStop,params1);
		mBtnRecord.setOnClickListener(this);
		mBtnPlayPCM.setOnClickListener(this);
		mBtnPlayWAV.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);
		return view;
	}

	/**
	 * Called when a view has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(View v) {
		if (v == mBtnPlayPCM){
			mAudioImpl.play(mPCMFileName);
		}else if (v == mBtnPlayWAV){
			mAudioImpl.play(mWAVFileName);
		}else if (v == mBtnRecord){
			mAudioImpl.record(mPCMFileName);
		}else if (v == mBtnStop){
			mAudioImpl.stop();
		}
	}
}
