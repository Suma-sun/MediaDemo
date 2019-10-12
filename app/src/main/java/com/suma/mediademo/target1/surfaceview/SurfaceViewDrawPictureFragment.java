package com.suma.mediademo.target1.surfaceview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.suma.mediademo.BaseFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 使用SurfaceView,绘制图片 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-06-27]
 */
public class SurfaceViewDrawPictureFragment extends BaseFragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		LinearLayout view = new LinearLayout(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		view.setLayoutParams(params);
		view.addView(new DrawPictureSurfaceView(getContext()),new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		return view;
	}
}
