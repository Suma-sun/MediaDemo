package com.suma.mediademo.target5;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * 正方形 <br>
 * 〈功能详细描述〉
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-12-30]
 */
public class Suqare implements DrawAble{

	/**
	 * 每一次读取顶点的数量，xyz
	 */
	private static final int COORDS_PER_VERTEX = 3;

	private FloatBuffer mVertexBuffer;

	private static final float[] mSuqareCoords = {

			0.5f,0f,0f,//正方形右上角
			-0.5f,0f,0f,//正方形左上角
			-0.5f,-0.5f,0f,//正方形左下角

			-0.5f,-0.5f,0f,//正方形左下角
			0.5f,-0.5f,0f,//正方形右下角
			0.5f,0f,0f//正方形右上角

	};


	private static final float[] mColor = { 0.73671875f, 0.46953125f, 0.62265625f, 1.0f };

	private final int mProgram;
	//vPosition成员的句柄
	private int mPsitionHandle;
	//vColor成员的句柄
	private int mColorHandle;
	//uMVPMatrix成员句柄，用于设置视图转换
	private int mMVPMatrixHandle;
	//顶点数据所需字节数，每个int类型4个字节
	private final int mVertexStride = COORDS_PER_VERTEX * 4;
	//顶点的数量
	private final int mVertexCount = mSuqareCoords.length;

	public Suqare(int type) {
		mVertexBuffer = ES20Util.createCoordsBuffer(mSuqareCoords);
		mProgram = ES20Util.getESProgram(type);
	}

	/**
	 * 绘制图形
	 */
	public void draw() {
//		//将程序添加至OpenGL环境
//		GLES20.glUseProgram(mProgram);
//		//获取顶点着色器的vPosition成员句柄
//		mPsitionHandle = GLES20.glGetAttribLocation(mProgram,ES20Util.NAME_POSITION);
//		//启用三角形顶点
//		GLES20.glEnableVertexAttribArray(mPsitionHandle);
//		//准备三角形坐标数据
//		GLES20.glVertexAttribPointer(mPsitionHandle,COORDS_PER_VERTEX, GLES20.GL_FLOAT,false,mVertexStride,mVertexBuffer);
//		//获取片段着色器的vColor成员句柄
//		mColorHandle = GLES20.glGetUniformLocation(mProgram,ES20Util.NAME_COLOR);
//		//设置绘制三角形的颜色
//		GLES20.glUniform4fv(mColorHandle,1,mColor,0);
//		//绘制三角形
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,mVertexCount);
////		GL_TRIANGLES：每三个顶之间绘制三角形，之间不连接
////		GL_TRIANGLE_FAN：以V0 V1 V2,V0 V2 V3,V0 V3 V4，……的形式绘制三角形
////		GL_TRIANGLE_STRIP：顺序在每三个顶点之间均绘制三角形。这个方法可以保证从相同的方向上所有三角形均被绘制。以V0 V1 V2 ,V1 V2 V3,V2 V3 V4,……的形式绘制三角形
//		//禁用顶点数组
//		GLES20.glDisableVertexAttribArray(mPsitionHandle);
		draw(null);
	}

	/**
	 * 带矩阵功能绘制图形
	 *
	 * @param matrix 总变换矩阵
	 */
	@Override
	public void draw(float[] matrix) {
		//将程序添加至OpenGL环境
		GLES20.glUseProgram(mProgram);
		//获取顶点着色器的vPosition成员句柄
		mPsitionHandle = GLES20.glGetAttribLocation(mProgram,ES20Util.NAME_POSITION);
		//启用三角形顶点
		GLES20.glEnableVertexAttribArray(mPsitionHandle);
		//准备三角形坐标数据
		GLES20.glVertexAttribPointer(mPsitionHandle,COORDS_PER_VERTEX, GLES20.GL_FLOAT,false, mVertexStride, mVertexBuffer);
		//获取片段着色器的vColor成员句柄
		mColorHandle = GLES20.glGetUniformLocation(mProgram,ES20Util.NAME_COLOR);
		//设置绘制三角形的颜色
		GLES20.glUniform4fv(mColorHandle,1, mColor,0);

		if (matrix != null) {
			//获取图形转换矩阵句柄
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram,ES20Util.NAME_MVP_MATRIX);
			//将投影和视图转换传递到着色器
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle,1,false,matrix,0);
		}


		//绘制三角形
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0, mVertexCount);
//		GL_TRIANGLES：每三个顶之间绘制三角形，之间不连接
//		GL_TRIANGLE_FAN：以V0 V1 V2,V0 V2 V3,V0 V3 V4，……的形式绘制三角形
//		GL_TRIANGLE_STRIP：顺序在每三个顶点之间均绘制三角形。这个方法可以保证从相同的方向上所有三角形均被绘制。以V0 V1 V2 ,V1 V2 V3,V2 V3 V4,……的形式绘制三角形
		//禁用顶点数组
		GLES20.glDisableVertexAttribArray(mPsitionHandle);


	}
}
