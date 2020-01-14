package com.suma.mediademo.target5;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL渲染器 <br>
 * 通过投影及相机修正图形比例异常
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-29]
 */
public class GLMatrixRenderer implements GLSurfaceView.Renderer {

	private Triangle mTriangle;
	private Suqare mSuqare;
	/**
	 * Model View Projection Matrix（模型视图投影矩阵）
	 */
	private final float[] mVPMatrix = new float[16];
	/**
	 * 投影矩阵
	 */
	private final float[] mProjectionMatrix = new float[16];
	/**
	 * 相机矩阵
	 */
	private final float[] mViewMatrix = new float[16];

	public GLMatrixRenderer() {
		//不能在此创建该对象，该对象初始化ES相关程序会造成卡死，必须在Renderer的回调内即GLSurfaceView的执行线程内执行es初始化
//		mTriangle = new Triangle();
	}


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
		//在Surface被创建时回调,当设备被唤醒或从其他activity返回时可能被调用，用来配置 View 的 OpenGL ES 环境

		// 设置清空屏幕用的颜色，接收四个参数分别是：红色、绿色、蓝色和透明度分量，0表示透明，1.0f相反
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		mTriangle = new Triangle(ES20Util.TYPE_MATRIX);
//		mSuqare = new Suqare(ES20Util.TYPE_MATRIX);
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
		//在每次Surface尺寸变化时回调，例如当设备的屏幕方向发生改变时。

		//设置视图的尺寸，告诉OpenGL可以用来渲染surface的大小。
		GLES20.glViewport(0, 0, width, height);
		//计算宽高比例
		float ratio = (float) width / height;
		//透视投影，物体离视点越远，呈现出来的越小。离视点越近，呈现出来的越大
		//正交投影，物体呈现出来的大小不会随着其距离视点的远近而发生变化

		//传入透视投影矩阵对象及参数，onDrawFrame中会使用传该对象与相机视图转换合并
		Matrix.frustumM(mProjectionMatrix,0,-ratio,ratio,-1,1,3,7);
		//设置相机位置
		Matrix.setLookAtM(mViewMatrix,0,0,0,-3,
				0f,0f,0f,
				0f,1.0f,0.0f);
		//计算投影和视图转换
		Matrix.multiplyMM(mVPMatrix,0,mProjectionMatrix,0,mViewMatrix,0);
	}
	/*
	透视投影
	Matrix.frustumM (float[] m,         //接收透视投影的变换矩阵
                int mOffset,        //变换矩阵的起始位置（偏移量）
                float left,         //相对观察点近面的左边距
                float right,        //相对观察点近面的右边距
                float bottom,       //相对观察点近面的下边距
                float top,          //相对观察点近面的上边距
                float near,         //相对观察点近面距离
                float far)          //相对观察点远面距离
	 */

	/*
	正交投影
	Matrix.orthoM (float[] m,           //接收正交投影的变换矩阵
	int mOffset,        //变换矩阵的起始位置（偏移量）
	float left,         //相对观察点近面的左边距
	float right,        //相对观察点近面的右边距
	float bottom,       //相对观察点近面的下边距
	float top,          //相对观察点近面的上边距
	float near,         //相对观察点近面距离
	float far)          //相对观察点远面距离
	*/

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
		//在绘制每一帧的时候会被GLSurfaceView回调,在该方法中我们需要绘制一些东西,即使只是清空屏幕,如果不做则会出现闪屏

		//清空屏幕，清空屏幕后调用glClearColor(）中设置的颜色填充屏幕；
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		if (mTriangle != null)
			mTriangle.draw(mVPMatrix);
		if (mSuqare != null)
			mSuqare.draw(mVPMatrix);
	}

	/*
	Matrix.setLookAtM (float[] rm,      //接收相机变换矩阵
                int rmOffset,       //变换矩阵的起始位置（偏移量）
                float eyeX,float eyeY, float eyeZ,   //相机位置
                float centerX,float centerY,float centerZ,  //观测点位置
                float upX,float upY,float upZ)  //up向量在xyz上的分量
	 */
}
