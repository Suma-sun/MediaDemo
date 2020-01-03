package com.suma.mediademo.target5;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.suma.mediademo.BaseFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 〈一句话功能简述〉 <br>
 * 〈功能详细描述〉
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-30]
 */
public class GLSurfaceViewFragment extends BaseFragment {
	/**
	 * OpenGL类型,值参考：{@link ES20Util#TYPE_NORMAL} {@link ES20Util#TYPE_MATRIX}
	 */
	public static final String EXTRA_TYPE = "extra_type";

	private MyGLSurfaceView mView;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (isSupported()) {
			return mView = new MyGLSurfaceView(getContext(),getActivity().getIntent().getIntExtra(EXTRA_TYPE,ES20Util.TYPE_NORMAL));
		} else {
			showToast("该设备不兼容ES2.0");
			//不兼容显示空
			return new LinearLayout(getContext());
		}


	}

	private boolean isSupported() {
		ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		if (activityManager == null)
			return false;
		ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x2000;

		boolean isEmulator = Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
				&& (Build.FINGERPRINT.startsWith("generic")
				|| Build.FINGERPRINT.startsWith("unknown")
				|| Build.MODEL.contains("google_sdk")
				|| Build.MODEL.contains("Emulator")
				|| Build.MODEL.contains("Android SDK built for x86"));

		supportsEs2 = supportsEs2 || isEmulator;
		return supportsEs2;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mView != null) {
			mView.onResume();
			mView.requestRender();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mView != null)
			mView.onPause();
	}
}
