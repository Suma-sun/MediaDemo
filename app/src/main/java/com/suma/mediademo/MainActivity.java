package com.suma.mediademo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.suma.mediademo.target1.surfaceview.SurfaceViewDrawPictureFragment;
import com.suma.mediademo.target1.view.ViewDrawPictureFragment;
import com.suma.mediademo.target2.AudioFragment;
import com.suma.mediademo.target3.CameraPreviewFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
	/**
	 * 权限请求code
	 */
	private static final int REQUEST_PERMISSIONS_CODE = 0x100;

	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
	private FrameLayout mRoot;
	private List<String> mTags = new ArrayList<String>() {{
		add("View draw picture");
		add("SurfaceView draw picture");
		add("AudioRecordPlay");
		add("Camera preview");
	}};
	//SDK是否>= M
	private boolean mIsNeedPermission = false;

	//读写权限
	private String[] mRWPermissions;
	//音频权限
	private String[] mAutoPermissions;
	//相机权限
	private String[] mCameraPermissions;


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
		mRoot = findViewById(R.id.fragment);
		mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		mAdapter.addAll(getItems());
		//text1
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		initPermisions();
	}

	private String[] getItems() {
		return new String[]{
				"自定义View绘制图片",
				"SurfaceView绘制图片",
				"音频录制播放",
				"Camera预览"
		};
	}

	private void showFragment(FragmentManager manager, Fragment fragment, String tag) {
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.fragment, fragment, tag);
//		transaction.add();
		transaction.commit();
		show(true);
	}

	private void show(boolean isShowFragment) {
		mListView.setVisibility(isShowFragment ? View.GONE : View.VISIBLE);
		mRoot.setVisibility(!isShowFragment ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment;
		String tag = mTags.get(i);
		fragment = manager.findFragmentByTag(tag);
		switch (i) {
			case 0:
				if (fragment == null) {
					fragment = new ViewDrawPictureFragment();
				}
				break;
			case 1:
				if (fragment == null) {
					fragment = new SurfaceViewDrawPictureFragment();
				}

			case 2:
				if (fragment == null) {
					fragment = new AudioFragment();
				}
				requestPremission(mAutoPermissions);
				break;
			case 3:
				if (fragment == null){
					fragment = new CameraPreviewFragment();
				}
				requestPermissionsImpl(mCameraPermissions);
				break;
			default:
				return;
		}
		showFragment(manager, fragment, tag);
	}

	@Override
	public void onBackPressed() {
		if (mRoot.getVisibility() == View.VISIBLE) {
			show(false);
			return;
		}
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
	}
}
