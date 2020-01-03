package com.suma.mediademo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.suma.mediademo.utils.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * framgent展示的activity <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-12]
 */
public class FragmentActivity extends androidx.fragment.app.FragmentActivity {
	/**
	 * 用于指定fragment
	 */
	public static final String EXTRA_FRAGMENT_NAME = "extra_fragment_name";

	/**
	 * 打开指定的fragment
	 * @param className 指定的fragment全局类名
	 */
	public static void openFragment(Context context, String className) {
		Intent intent = new Intent(context, FragmentActivity.class);
		intent.putExtra(EXTRA_FRAGMENT_NAME, className);
		context.startActivity(intent);
	}

	/**
	 * 打开指定的fragment
	 */
	public static void openFragment(Context context, Bundle data) {
		Intent intent = new Intent(context, FragmentActivity.class);
		intent.putExtras(data);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);
		Intent intent = getIntent();
		if (intent.getExtras() == null || !intent.getExtras().containsKey(EXTRA_FRAGMENT_NAME)) {
			throw new RuntimeException("Fragment is null");
		}
		String fragmentClass = intent.getStringExtra(EXTRA_FRAGMENT_NAME);
		try {
			Class cla = Class.forName(fragmentClass);
			Fragment fragment = (Fragment) cla.newInstance();
			FragmentManager manager = getSupportFragmentManager();
			manager.beginTransaction().add(R.id.fragment,fragment).commit();
		} catch (IllegalAccessException e) {
			Log.e(this,e);
		} catch (InstantiationException e) {
			Log.e(this,e);
		} catch (ClassNotFoundException e) {
			Log.e(this,e);
		}
	}
}
