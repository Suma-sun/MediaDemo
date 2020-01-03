package com.suma.mediademo.target5;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.suma.mediademo.utils.Log;

/**
 * 绘制三角形的GLSurfaceView <br>
 * 该View只是用来与Activity进行绑定,绘制工作在Renderer中<br>
 * https://developer.android.google.cn/training/graphics/opengl/environment
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-29]
 */
public class MyGLSurfaceView extends GLSurfaceView {

	private GLSurfaceView.Renderer mRenderer;


	/**
	 * Standard View constructor. In order to render something, you
	 * must call {@link #setRenderer} to register a renderer.
	 *
	 * @param context
	 */
	public MyGLSurfaceView(Context context, int type) {
		super(context);

		// Create an OpenGL ES 2.0 context
		setEGLContextClientVersion(2);
		Log.d(this,"MyGLSurfaceView type = " + type);
		if (type == ES20Util.TYPE_MATRIX)
			mRenderer = new GLMatrixRenderer();
		else
			mRenderer = new GLRenderer();

		// Set the Renderer for drawing on the GLSurfaceView
		setRenderer(mRenderer);

		// Render the view only when there is a change in the drawing data
		//懒惰渲染,需要手动调用glSurfeaceView.requestRender()
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		//积极渲染 不停的渲染
//		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

	}

}
