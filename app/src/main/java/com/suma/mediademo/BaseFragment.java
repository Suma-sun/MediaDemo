package com.suma.mediademo;

import com.suma.mediademo.utils.UiUtils;

import androidx.fragment.app.Fragment;

/**
 * Fragment基类 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-12]
 */
public abstract class BaseFragment extends Fragment implements Notifyable{


	public void showToast(String msg) {
		UiUtils.showToast(getContext(),msg);
	}

	@Override
	public void onNotify(String msg) {
		showToast(msg);
	}
}
