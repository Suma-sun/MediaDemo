package com.suma.mediademo.target5;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.suma.mediademo.target6.TextureRenderer;
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
	 * @param type {@link ES20Util#TYPE_NORMAL} {@link ES20Util#TYPE_MATRIX} {@link ES20Util#TYPE_TEXTURE}
	 */
	public MyGLSurfaceView(Context context, int type) {
		super(context);

		// 设置使用es 2.0版本
		setEGLContextClientVersion(2);
		Log.d(this,"MyGLSurfaceView type = " + type);
		if (type == ES20Util.TYPE_MATRIX)
			mRenderer = new GLMatrixRenderer();
		else if (type == ES20Util.TYPE_TEXTURE)
			mRenderer = new TextureRenderer(getContext());
		else
			mRenderer = new GLRenderer();

		// 设置负责绘制的渲染器
		setRenderer(mRenderer);

		//懒惰渲染,需要手动调用glSurfeaceView.requestRender()
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		//积极渲染 不停的渲染，适合有交互、播放帧数据的场景
//		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

	}

}
