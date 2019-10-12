package com.suma.mediademo.target1.surfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.suma.mediademo.R;
import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.SafeHandler;
import com.suma.mediademo.utils.UiUtils;

/**
 * 绘制托盘的SurfaceView <br>
 * SurfaceView的绘制在另一个线程,故不会阻塞主线程,不会造成界面卡顿<br>
 * 其原理是先锁定画布,然后绘制,绘制完毕后解锁画布,显示到屏幕上
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-06-30]
 * @version [1.1 2019-10-12] 由Thread改为HanderThread和Hander执行绘制操作
 */
public class DrawPictureSurfaceView extends SurfaceView implements SurfaceHolder.Callback, SafeHandler.OnHandlerMessage {

	private static final int MSG_DRAW = 0X1001;
	private SurfaceHolder mHolder;
	private Canvas mCanvas;
	private HandlerThread mHandlerThread;
	private SafeHandler mHandler;

	private Bitmap mBitmap;

	public DrawPictureSurfaceView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mHolder = getHolder();
		mHolder.addCallback(this);//创建\销毁\改变回调
		setFocusable(true);//允许获得焦点
		setFocusableInTouchMode(true);//允许通过触摸获得焦点
		this.setKeepScreenOn(true);//保持常亮

	}


	/**
	 * This is called immediately after the surface is first created.
	 * Implementations of this should start up whatever rendering code
	 * they desire.  Note that only one thread can ever draw into
	 * a {@link Surface}, so you should not draw into the Surface here
	 * if your normal rendering will be in another thread.
	 *
	 * @param holder The SurfaceHolder whose surface is being created.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHandlerThread = new HandlerThread("SurfaceView Thread");
		mHandlerThread.start();
		mHandler = new SafeHandler(mHandlerThread.getLooper(), this);
		mHandler.sendEmptyMessage(MSG_DRAW);
	}

	/**
	 * This is called immediately after any structural changes (format or
	 * size) have been made to the surface.  You should at this point update
	 * the imagery in the surface.  This method is always called at least
	 * once, after {@link #surfaceCreated}.
	 *
	 * @param holder The SurfaceHolder whose surface has changed.
	 * @param format The new PixelFormat of the surface.
	 * @param width  The new width of the surface.
	 * @param height The new height of the surface.
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	/**
	 * This is called immediately before a surface is being destroyed. After
	 * returning from this call, you should no longer try to access this
	 * surface.  If you have a rendering thread that directly accesses
	 * the surface, you must ensure that thread is no longer touching the
	 * Surface before returning from this function.
	 *
	 * @param holder The SurfaceHolder whose surface is being destroyed.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHandlerThread.quit();
		mHandlerThread = null;
		mHandler = null;
	}

	private void draw() {
		//子线程绘制图片
		Log.d(this," is mainThread " + UiUtils.isRunOnUiThread());
		try {
			//锁住画布
			mCanvas = mHolder.lockCanvas();
			int width = getMeasuredWidth();
			int height = getMeasuredHeight();
			Bitmap bitmap = UiUtils.getResBitmap(getContext().getResources(), R.mipmap.server,width,height);
			mBitmap = Bitmap.createScaledBitmap(bitmap,width,height,false);
			mCanvas.drawBitmap(mBitmap,0,0,new Paint());
		} finally {
			if (mCanvas != null){
				//解除锁定,并提交绘制内容,绘制在主线程
				mHolder.unlockCanvasAndPost(mCanvas);
			}
		}
	}

	/**
	 * Implement this to do your drawing.
	 *
	 * @param canvas the canvas on which the background will be drawn
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}

	@Override
	public void onHandleMessage(Message msg) {
		final int what = msg.what;
		switch (what) {
			case MSG_DRAW:
				draw();
				break;
		}
	}
}
