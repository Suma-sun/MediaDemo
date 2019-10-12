package com.suma.mediademo.target1.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.suma.mediademo.BaseFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 自定义View,绘制图片 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-06-27]
 */
public class ViewDrawPictureFragment extends BaseFragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		LinearLayout view = new LinearLayout(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		view.setLayoutParams(params);
		view.addView(new DrawPictureView(getContext()),new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		return view;
	}
}
