package com.suma.mediademo.target6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.suma.mediademo.R;
import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.StringUtils;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 纹理渲染器 <br>
 * 将屏幕渲染成一张图片
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2020-01-03]
 */
public class TextureRenderer implements GLSurfaceView.Renderer {

	public TextureRenderer(Context context) {
		this.mContext = new WeakReference<>(context);
	}

	private WeakReference<Context> mContext;
	/**
	 * 视图投影矩阵
	 */
	private float[] mVMPMatrix = new float[16];
	/**
	 * 相机矩阵
	 */
	private float[] mViewMatrix = new float[16];
	/**
	 * 投影矩阵
	 */
	private float[] mPojectionMatrix = new float[16];
	//全屏矩形，并绘制纹理
	private Texture mTexture;

	private Bitmap mBitmap;

	/**
	 * Called when the surface is created or recreated.
	 * <p>
	 * Called when the rendering thread
	 * starts and whenever the EGL context is lost. The EGL context will typically
	 * be lost when the Android device awakes after going to sleep.
	 * <p>
	 * Since this method is called at the beginning of rendering, as well as
	 * every time the EGL context is lost, this method is a convenient place to put
	 * code to create resources that need to be created when the rendering
	 * starts, and that need to be recreated when the EGL context is lost.
	 * Textures are an example of a resource that you might want to create
	 * here.
	 * <p>
	 * Note that when the EGL context is lost, all OpenGL resources associated
	 * with that context will be automatically deleted. You do not need to call
	 * the corresponding "glDelete" methods such as glDeleteTextures to
	 * manually delete these lost resources.
	 * <p>
	 *
	 * @param gl     the GL interface. Use <code>instanceof</code> to
	 *               test if the interface supports GL11 or higher interfaces.
	 * @param config the EGLConfig of the created surface. Can be used
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//设置背景颜色（未绘制）
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		Context context = mContext.get();
		if (context != null)
			mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.server);
		mTexture = new Texture(mBitmap);
	}

	/**
	 * Called when the surface changed size.
	 * <p>
	 * Called after the surface is created and whenever
	 * the OpenGL ES surface size changes.
	 * <p>
	 * Typically you will set your viewport here. If your camera
	 * is fixed then you could also set your projection matrix here:
	 * <pre class="prettyprint">
	 * void onSurfaceChanged(GL10 gl, int width, int height) {
	 *     gl.glViewport(0, 0, width, height);
	 *     // for a fixed camera, set the projection too
	 *     float ratio = (float) width / height;
	 *     gl.glMatrixMode(GL10.GL_PROJECTION);
	 *     gl.glLoadIdentity();
	 *     gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
	 * }
	 * </pre>
	 *
	 * @param gl     the GL interface. Use <code>instanceof</code> to
	 *               test if the interface supports GL11 or higher interfaces.
	 * @param width
	 * @param height
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		int bitmapW = mBitmap.getWidth();
		int bitmapH = mBitmap.getHeight();
		//图片的宽高比
		float bitmapRatio = (float) bitmapW / bitmapH;
		//surfaceview的宽高比
		float ratio = (float) width / height;
//		坐标系，x轴-1～1，y轴-1～1
		float left = -1;
		float right = 1;
		float top = 1;
		float bottom = -1;

		//等比例缩放，不变形
		if (width > height) {//横屏
//			width > height 说明 ratio > 1;
			if (bitmapRatio > ratio) {//图片宽高比大于控件宽高比
				//bitmapRatio > ratio 说明 bitmapRatio > 1,宽 > 高
				left = -ratio * bitmapRatio;
				right = ratio * bitmapRatio;
			} else {
				//bitmapRatio < ratio
				left = -ratio / bitmapRatio;
				right = ratio / bitmapRatio;
			}
		} else {//竖屏
//			width < height 说明 ratio < 1;
			if (bitmapRatio > ratio) {//图片宽高比大于控件宽高比
				bottom = -1/ratio * bitmapRatio;
				top = 1/ratio * bitmapRatio;
			} else {
				bottom = -bitmapRatio / ratio;
				top = bitmapRatio / ratio;
			}
		}

		Log.d(this, StringUtils.format("onSurfaceChanged left=%f,right=%f,top=%f,bottom=%f",left,right,top,bottom));
		//透视投影
//		Matrix.frustumM(mPojectionMatrix, 0, left, right, bottom, top, 3, 7);//左右翻转
		Matrix.frustumM(mPojectionMatrix, 0, right,left, bottom, top, 3, 7);//正常显示
		//未经过算饭等比例缩放
//		Matrix.frustumM(mPojectionMatrix, 0, -ratio,ratio, bottom, top, 3, 7);
		//相机位置
		Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1f, 0f);
		//视图投影矩阵转换
		Matrix.multiplyMM(mVMPMatrix, 0, mPojectionMatrix, 0, mViewMatrix, 0);
	}

	/**
	 * Called to draw the current frame.
	 * <p>
	 * This method is responsible for drawing the current frame.
	 * <p>
	 * The implementation of this method typically looks like this:
	 * <pre class="prettyprint">
	 * void onDrawFrame(GL10 gl) {
	 *     gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	 *     //... other gl calls to render the scene ...
	 * }
	 * </pre>
	 *
	 * @param gl the GL interface. Use <code>instanceof</code> to
	 *           test if the interface supports GL11 or higher interfaces.
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		//使用onSurfaceCreated中设置的颜色绘制背景
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		if (mTexture != null)
			mTexture.draw(mVMPMatrix);

	}


}
