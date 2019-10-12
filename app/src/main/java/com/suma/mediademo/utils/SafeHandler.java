package com.suma.mediademo.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 防泄漏的Handler  </br>
 *	
 * @author suma 284425176@qq.com
 * @version 1.0
 */
public class SafeHandler extends Handler {
	
	public interface OnHandlerMessage{
		void onHandleMessage(Message msg);
	}
	
	private WeakReference<OnHandlerMessage> ref;

	public SafeHandler(Looper looper, OnHandlerMessage handlerMessage) {
		super(looper);
		this.ref = new WeakReference<>(handlerMessage);
	}


	
	@Override
	public void handleMessage(Message msg) {
//		super.onHandleMessage(msg);
		if(ref.get() != null) {
			try {
				ref.get().onHandleMessage(msg);
			} catch (Exception e) {
				Log.e("SafeHandler",e);
			}
		}
	}

}
