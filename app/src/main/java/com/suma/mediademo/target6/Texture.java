package com.suma.mediademo.target6;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.suma.mediademo.target5.DrawAble;
import com.suma.mediademo.target5.ES20Util;
import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.StringUtils;

import java.nio.FloatBuffer;

/**
 * 纹理绘制实现类 <br>
 *	实际上是绘制一个全屏的矩形，然后在矩形上绘制纹理（图片）
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2020-01-03]
 */
public class Texture implements DrawAble {

//	参考https://blog.csdn.net/junzia/article/details/52842816

//	v1 -------------- v3
//	   |		    |
//	   |			|
//	   |			|
//	   |          	|
//	v2 -------------- v4

//	将顶点按照V2V1V4V3传入，以三角形条带方式绘制，则纹理坐标应按照V2V1V4V3传入。(v2v1v4v3 = v2v4v1v4，只要前三个顶点与后三个顶点均组成三角形)
//	如果按照V3V4V1V2传入，会得到一个旋转了180度的纹理。如果按照V4V3V2V1传入，则会得到一个左右翻转的纹理。

	//纹理坐标如同屏幕坐标，左上角为原点。
	// v1（0，0） v2（0，1） v3（1，0） v4（1，1）

	/**
	 * 顶点坐标
	 */
	private float[] VERTEX_COORDINATE = {
			-1f,-1f,0f,//v2
			-1f,1f,0f,//v1
			1f,-1f,0f,//v4
			1f,1f,0f//v3
	};

	/**
	 * 纹理坐标
	 */
	private float[] TEXTURE_COORDINAT = {
			0f,1f,0f,//v2
			0f,0f,0f,//v1
			1f,1f,0f,//v4
			1f,0f,0f//v3
	};

	/**
	 * 每次读取的顶点数量，即每个顶点的数据量，xyz坐标为3，xy坐标为2
	 */
	private static final int COORDS_PER_VERTEX = 3;

	private Bitmap mBitmap;

	private FloatBuffer mVertexBuffer;

	private FloatBuffer mTextureBuffer;

	private int mProgarm;
	//顶点坐标句柄
	private int mPositionHandle;
	//变换矩阵句柄
	private int mVMPMatrixHandle;
	//纹理坐标句柄
	private int mCoordinateHandle;
	//纹理采样器句柄
	private int mTextureHandle;

	private int mTextureId;


	public Texture(Bitmap bitmap) {
		mVertexBuffer = ES20Util.createCoordsBuffer(VERTEX_COORDINATE);
		mTextureBuffer = ES20Util.createCoordsBuffer(TEXTURE_COORDINAT);
		this.mBitmap = bitmap;

	}

	/**
	 * 绘制图形
	 */
	@Override
	public void draw() {
		throw new RuntimeException("is empty implements");
	}

	/**
	 * 带矩阵功能绘制图形
	 *
	 * @param matrix 总变换矩阵
	 */
	@Override
	public void draw(float[] matrix) {
		mProgarm = ES20Util.getESProgram(ES20Util.TYPE_TEXTURE);
		GLES20.glUseProgram(mProgarm);
		//获取着色器中的成员句柄
		//关于使用glGetAttribLocation还是glGetUniformLocation，主要看着色器代码里，该成员的类型
		mPositionHandle = GLES20.glGetAttribLocation(mProgarm,ES20Util.NAME_POSITION);
		mCoordinateHandle = GLES20.glGetAttribLocation(mProgarm,ES20Util.NAME_V_COORDINATE);
		mVMPMatrixHandle = GLES20.glGetUniformLocation(mProgarm,ES20Util.NAME_MVP_MATRIX);
		mTextureHandle = GLES20.glGetUniformLocation(mProgarm,ES20Util.NAME_TEXTURE);
		Log.d(this, StringUtils.format("getHandle end position=%d, coordinate=%d, matrix=%d, texture=%d",
				mPositionHandle,mCoordinateHandle,mVMPMatrixHandle,mTextureHandle));
		//设置OpenGL程序设置统一的矩阵
		GLES20.glUniformMatrix4fv(mVMPMatrixHandle,1,false,matrix,0);

		//启用顶点数组，客户端默认是禁用
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glEnableVertexAttribArray(mCoordinateHandle);

		GLES20.glUniform1f(mTextureHandle,0);
		mTextureId = createTexture();
		Log.d(this,"createTexture end textureId=" + mTextureId);

		//传入顶点坐标数据
		GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT,
				false,
				0,//这里传入0是因为绘制的模式GL_TRIANGLE_STRIP，连续的顶点绘制三角形，故不存在偏移量
				mVertexBuffer);
		//传入纹理坐标数据
		GLES20.glVertexAttribPointer(mCoordinateHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT,
				false,
				0,//这里传入0是因为绘制的模式GL_TRIANGLE_STRIP，连续的顶点绘制三角形，故不存在偏移量
				mTextureBuffer);
		//渲染数据
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,
				VERTEX_COORDINATE.length/COORDS_PER_VERTEX//需要渲染的顶点的数量，
		);
		Log.i(this,"glDrawArrays end ");

		//禁用顶点数组
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mCoordinateHandle);
	}

	private int createTexture() {
		if(mBitmap != null && !mBitmap.isRecycled()) {
			int[] texture = new int[1];
			//生成纹理
			GLES20.glGenTextures(
					1,//产生的纹理id数量
					texture,//纹理id接收数组
					0//数组偏移量
			);
			//绑定纹理id
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
			//设置纹理参数
			//设置缩小过滤为使用纹理坐标中最接近的一个像素的颜色作为需要绘制的的像素颜色
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
			//设置放大过滤为使用纹理坐标中最接近的若干个颜色，通过加权平衡算法得到需要绘制的颜色
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
			//设置环绕方向s，截取纹理坐标[1/2n,1-1/2n],将导致永远不会与border融合
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
			//设置环绕方向T，截取纹理坐标[1/2n,1-1/2n],将导致永远不会与border融合
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
			//根据以上指定的参数，生成一个2D纹理
			GLUtils.texImage2D(
					GLES20.GL_TEXTURE_2D,//纹理类型
					0,//纹理层次
					mBitmap,//纹理图形
					0//纹理边框尺寸
			);
			return texture[0];
		} else
			return 0;
	}
}
