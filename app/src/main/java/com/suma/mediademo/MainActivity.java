package com.suma.mediademo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.suma.mediademo.target1.surfaceview.SurfaceViewDrawPictureFragment;
import com.suma.mediademo.target1.view.ViewDrawPictureFragment;
import com.suma.mediademo.target2.AudioFragment;
import com.suma.mediademo.target3.CameraPreviewFragment;
import com.suma.mediademo.target4.MediaExtractorAndMuxerFragment;
import com.suma.mediademo.target5.ES20Util;
import com.suma.mediademo.target5.GLSurfaceViewFragment;
import com.suma.mediademo.utils.UiUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
	/**
	 * 权限请求code
	 */
	private static final int REQUEST_PERMISSIONS_CODE = 0x100;

	private ListView mListView;
	private ArrayAdapter<String> mAdapter;

	//SDK是否>= M
	private boolean mIsNeedPermission = false;

	//读写权限
	private String[] mRWPermissions;
	//音频权限
	private String[] mAutoPermissions;
	//相机权限
	private String[] mCameraPermissions;
	/**
	 * 指定打开的fragment的类名
	 */
	private String mTargetName;


	@TargetApi(Build.VERSION_CODES.M)
	private void initPermisions() {
		mIsNeedPermission = true;
		mRWPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE};
		mAutoPermissions = new String[]{Manifest.permission.RECORD_AUDIO,mRWPermissions[0],mRWPermissions[1]};
		mCameraPermissions = new String[]{Manifest.permission.CAMERA,mRWPermissions[1]};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = findViewById(R.id.list);
		mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		mAdapter.addAll(getItems());
		mListView.setAdapter(mAdapter);
//		mListView.setPadding(0,600,0,0);
		mListView.setOnItemClickListener(this);

		initPermisions();
	}

	private String[] getItems() {
		return new String[]{
				"自定义View绘制图片",
				"SurfaceView绘制图片",
				"音频录制播放",
				"Camera预览",
				"MP4解析与封装",
				"OpenGL绘制图形",
				"OpenGL使用矩阵绘制图形",
				"OpenGL使用矩阵绘制纹理"
		};
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		boolean isRequestPermission = false;
		mTargetName = null;
		switch (i) {
			case 0:
				mTargetName = ViewDrawPictureFragment.class.getName();
				break;
			case 1:
				mTargetName = SurfaceViewDrawPictureFragment.class.getName();
				break;
			case 2:
				mTargetName = AudioFragment.class.getName();
				requestPremission(mAutoPermissions);
				isRequestPermission = true;
				break;
			case 3:
				mTargetName = CameraPreviewFragment.class.getName();
				requestPermissionsImpl(mCameraPermissions);
				isRequestPermission = true;
				break;
			case 4:
				mTargetName = MediaExtractorAndMuxerFragment.class.getName();
				requestPermissionsImpl(mRWPermissions);
				isRequestPermission = true;
				break;
			case 5:
				startGLSurfaceFragment(ES20Util.TYPE_NORMAL);
				return;
//				mTargetName = GLSurfaceViewFragment.class.getName();
//				break;
			case 6:
				startGLSurfaceFragment(ES20Util.TYPE_MATRIX);
				return;
//				mTargetName = GLSurfaceViewFragment.class.getName();
//				break;
			case 7:
				startGLSurfaceFragment(ES20Util.TYPE_TEXTURE);
				return;
			default:
				return;
		}
		//需求请求权限,跳转为权限请求后执行
		if (!isRequestPermission) {
			startFragment(mTargetName);
		}
	}

	private void startFragment(String name) {
		if (!TextUtils.isEmpty(name))
			FragmentActivity.openFragment(this, name);
		else
			UiUtils.showToast(this,"目标为空");
	}

	/**
	 * 跳转OpenGL绘制图形
	 */
	private void startGLSurfaceFragment(int type) {
		Bundle data = new Bundle();
		data.putString(FragmentActivity.EXTRA_FRAGMENT_NAME,GLSurfaceViewFragment.class.getName());
		data.putInt(GLSurfaceViewFragment.EXTRA_TYPE,type);
		FragmentActivity.openFragment(this,data);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void requestPremission(String[] permissions) {
		if (!mIsNeedPermission) {
			return;
		}
		requestPermissionsImpl(permissions);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void requestPermissionsImpl(String[] permissions) {
		boolean hasPermission = checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED;
		if (!hasPermission) {
			requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
		} else {
			startFragment(mTargetName);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int grantResult : grantResults) {
			if (grantResult == PackageManager.PERMISSION_DENIED) {
				Toast.makeText(this, "请开启权限,才可正常使用使用", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		startFragment(mTargetName);
	}
}
