package com.suma.mediademo.target1.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.suma.mediademo.R;

/**
 * 绘制图片的自定义View <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-06-27]
 */
public class DrawPictureView extends View {

	Bitmap mDrawable;
	Paint mPaint;
	Matrix mMatrix;

	public DrawPictureView(Context context) {
		super(context);
		init(context);
	}

	public DrawPictureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DrawPictureView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		mPaint = new Paint();
		mMatrix = new Matrix();
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mDrawable == null) {
			int width = getMeasuredWidth();
			int height = getMeasuredHeight();

			//下面两种方式内存占用相同

			//图片压缩加载原理看UiUtils#getFileBitmap()
//			Bitmap bitmap = UiUtils.getResBitmap(getContext().getResources(), R.mipmap.small,width,height);
//			mDrawable = Bitmap.createScaledBitmap(bitmap,width,height,true);


			//createScaledBitmap,根据目标宽高创建一个缩放的位图,如果宽高与图片宽高一致,则返回原位图
			// 首先生成缩放比例,放入Matrix中, m.setScale(sx, sy);
			// 然后将缩放的结果放入deviceR中,  m.mapRect(deviceR, dstR);
			// 调用本地方法nativeCreate()创建Bitmap对象
			// 目标宽高<=0 会抛出异常
			mDrawable = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.server),width,height,true);

		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.drawBitmap(mDrawable,mMatrix,mPaint);
	}
}
