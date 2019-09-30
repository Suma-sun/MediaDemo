package com.suma.mediademo.target3;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.suma.mediademo.R;
import com.suma.mediademo.utils.Constant;
import com.suma.mediademo.utils.FileUtils;
import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.UiUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 使用SurfaceView,TextrueView回显Camera预览数据 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-09-09]
 */
public class CameraPreviewFragment extends Fragment implements SurfaceHolder.Callback, Camera.PreviewCallback, TextureView.SurfaceTextureListener {
	public static final String TAG = "CameraPreviewFragment";
	private static final int TYPE_NON = 0;

	private static final int TYPE_SURFACE = 1;

	private static final int TYPE_TEXTRUE = 2;


	private Camera mCamera;
	//摄像头的id号,用于切换. 0为后置摄像头,1为前置摄像头,后置多摄像头除外
	private int mFaceCameraId;
	private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
	private AtomicBoolean isSavePic = new AtomicBoolean(false);
	private File mDir;
	private int mWidth;
	private int mHeight;

	private int mCurrType = TYPE_NON;

	private Button mBtnSurface;
	private Button mBtnTexture;
	private Button mBtnSwitch;
	private Button mBtnTakePic;
//	private Button mBtnOpenDir;

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	private TextureView mTextureView;

	private ExecutorService mExecutor;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final int count = Camera.getNumberOfCameras();
		mFaceCameraId = count > Camera.CameraInfo.CAMERA_FACING_FRONT ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
		Log.d(this, "切换的相机id为" + mFaceCameraId);
		mDir = new File(Constant.getAppRootDir() + Constant.PICTURE_DIR);
		mExecutor = Executors.newCachedThreadPool();
		Log.d(this, mDir.getPath());
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.camera_preview_layout, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		bindView(view);
		initListener();

	}

	@Override
	public void onPause() {
		release();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mCurrType == TYPE_SURFACE)
			initSurface();
		else if (mCurrType == TYPE_TEXTRUE)
			initTextrue();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mExecutor.shutdown();
	}

	private void bindView(@NonNull View view) {
		mBtnSurface = view.findViewById(R.id.btn_surfaceview);
		mBtnTexture = view.findViewById(R.id.btn_textureview);
		mBtnSwitch = view.findViewById(R.id.btn_camera_switch);
		mBtnTakePic = view.findViewById(R.id.btn_take_pic);
//		mBtnOpenDir = view.findViewById(R.id.btn_open_dir);
		mSurfaceView = view.findViewById(R.id.view_surface);
		mTextureView = view.findViewById(R.id.view_texture);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
	}

	private void initListener() {
		mBtnSurface.setOnClickListener(v -> {
			mTextureView.setVisibility(View.GONE);
			mSurfaceView.setVisibility(View.VISIBLE);
			initSurface();
		});
		mBtnTexture.setOnClickListener(v -> {
			mSurfaceView.setVisibility(View.GONE);
			mTextureView.setVisibility(View.VISIBLE);
			initTextrue();
		});
		mBtnSwitch.setOnClickListener(v -> switchCamera());
		mBtnTakePic.setOnClickListener(v -> {
			if (!isSavePic.get()) {
				isSavePic.set(true);
			} else {
				UiUtils.showToast(getContext(), "拍照未结束");
			}
		});
//		mBtnOpenDir.setOnClickListener(v -> {
//			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			Uri uri = null;
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//				uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".FileProvider", mDir);
//				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//			} else
//				uri = Uri.fromFile(mDir);
//			intent.setDataAndType(uri, "*/*");
//			getContext().startActivity(intent);
//
//
//		});
	}

	/**
	 * 初始化相机,打开相机并且设置参数,设置方向
	 */
	private void initCamera() {
		mCamera = Camera.open(mCameraId);
		Camera.Parameters param = mCamera.getParameters();

		setCameraParameters();

		Camera.Size size = param.getPreviewSize();
		mWidth = size.width;
		mHeight = size.height;
		//默认为NV21数据
		param.setPreviewFormat(ImageFormat.NV21);
		try {
			mCamera.setParameters(param);
			mCamera.setPreviewCallback(this);
		} catch (Exception e) {
			Log.e(this, e);
		}
	}

	/**
	 * 释放相机相关资源
	 */
	private void release() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			if (mCurrType == TYPE_SURFACE) {
				try {
					mCamera.setPreviewDisplay(null);
				} catch (IOException e) {
					Log.e(this, e);
				}
			} else if (mCurrType == TYPE_TEXTRUE) {
				try {
					mCamera.setPreviewTexture(null);
				} catch (IOException e) {
					Log.e(this, e);
				}
			}
			mCamera.release();
			mCamera = null;
		}
	}

	private void initSurface() {
		mCurrType = TYPE_SURFACE;
		if (mCamera == null)
			initCamera();
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			Log.e(this, e);
		}
		mCamera.startPreview();
	}

	private void initTextrue() {
		mCurrType = TYPE_TEXTRUE;
		if (mCamera == null)
			initCamera();
		if (mTextureView.getSurfaceTextureListener() == null) {
			mTextureView.setSurfaceTextureListener(this);
		} else {
			mCamera.startPreview();
			try {
				mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 设置相机参数
	 */
	private void setCameraParameters() {
		Camera.Parameters parameters = mCamera.getParameters();
		List<String> modes = parameters.getSupportedFocusModes();
		if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			//设置连续自动对焦模式(对焦响应较快)
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		} else if (modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			//设置自动对焦模式
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}
		Camera.Size size = parameters.getPreviewSize();
		mWidth = size.width;
		mHeight = size.height;
		setCameraDisplayOrientation(getActivity(), mCameraId, mCamera);
		mCamera.setParameters(parameters);
	}

	/**
	 * 保证预览方向正确
	 *
	 * @param activity activity
	 * @param cameraId cameraId
	 * @param camera   camera
	 */
	public void setCameraDisplayOrientation(Activity activity,
											int cameraId, Camera camera) {
		android.hardware.Camera.CameraInfo info =
				new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
			default:
				break;
		}
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}
		//设置角度
		camera.setDisplayOrientation(result);
	}


	/**
	 * 切换摄像头
	 */
	private void switchCamera() {
		if (Camera.CameraInfo.CAMERA_FACING_BACK == mFaceCameraId) {
			UiUtils.showToast(getContext(), "无其他摄像头");
			return;
		}
		release();
		mCameraId = mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? mFaceCameraId : Camera.CameraInfo.CAMERA_FACING_BACK;
		if (mCurrType == TYPE_TEXTRUE)
			initTextrue();
		else if (mCurrType == TYPE_SURFACE)
			initSurface();
		else
			UiUtils.showToast(getContext(), "请选择预览方式");
	}

	/**
	 * 将帧数据保存为图片
	 *
	 * @param data
	 */
	private void onSavePicture(byte[] data) {
		if (!mExecutor.isShutdown()) {
			byte[] frame = new byte[data.length];
			System.arraycopy(data, 0, frame, 0, data.length);
			mExecutor.submit(new SavePictureThread(getContext(), frame, mDir, mWidth, mHeight));
			Log.d(TAG, "保存帧数据");
		} else {
			Log.d(TAG, "mExecutor isShutdown true");
		}
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(this, "surfaceCreated");
		mSurfaceHolder = holder;
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(this, "surfaceChanged");
		mSurfaceHolder = holder;
		if (mCamera != null) {
			setCameraParameters();
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				Log.e(this, e);
			}
			//重新启动预览
			mCamera.startPreview();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(this, "surfaceDestroyed");
		release();
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		Log.d(this, "onSurfaceTextureAvailable");
		try {
			mCamera.setPreviewTexture(surface);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.e(this, e);
		}
	}


	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		Log.d(this, "onSurfaceTextureSizeChanged");
	}


	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		Log.d(this, "onSurfaceTextureDestroyed");
		release();
		return true;
	}


	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		Log.d(this, "onSurfaceTextureUpdated");

	}


	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		//UI线程回调
		if (isSavePic.get()) {
			byte[] frame = new byte[data.length];
			System.arraycopy(data, 0, frame, 0, data.length);
			onSavePicture(frame);
			isSavePic.compareAndSet(true, false);
		}
	}

	/**
	 * 保存图片专用线程
	 */
	static class SavePictureThread extends Thread {
		byte[] data;
		File dir;
		int width;
		int height;
		WeakReference<Context> reference;

		SavePictureThread(Context context, byte[] data, File dir, int width, int height) {
			this.data = data;
			this.dir = dir;
			this.width = width;
			this.height = height;
			reference = new WeakReference<>(context);
		}

		@Override
		public void run() {
			//将帧数据保存为图片
			String name = FileUtils.getFileNameByTime("preview_", Constant.PICTURE_EXTENSION);
			File file = new File(dir, name);
			boolean isSuccess = FileUtils.createOrExistsFile(file);
			//一个可以将YUV数据压缩为JPEG数据的类
			YuvImage image = null;
			image = new YuvImage(data, ImageFormat.NV21, width, height, null);
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(file);
				image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()),
						80, fileOutputStream);//第三个参数0~100之间,0为最小大小,100最大原图存放,控制图片压缩
			} catch (FileNotFoundException e) {
				Log.e(this, e);
			} finally {
				if (fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						Log.e(this, e);
					}
				}
				if (reference.get() != null) {
					UiUtils.showToast(reference.get(), file.getPath());
				}
			}

		}
	}
}
