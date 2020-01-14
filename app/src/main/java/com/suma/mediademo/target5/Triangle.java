package com.suma.mediademo.target5;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * 三角形 <br>
 * opengl实际绘制逻辑
 *
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-12-30]
 */
public class Triangle implements DrawAble{

	/**
	 * 为了提高效率,将顶点的数组放入byteBuffer中
	 */
	private FloatBuffer mVertexBuffer;
	/**
	 * 每一次读取顶点的数量，xyz
	 */
	private static final int COORDS_PER_VERTEX = 3;

	/**
	 * 三个顶点的坐标,想象一个-2～2的坐标体系
	 */
	private static final float[] TRIANGLE_COORDS = {
			//x,y,z右手定则，opengl默认0,0为中点,1为屏幕边缘
//			0f,0.5f,0f, //中间上方的顶点
//			-0.5f,0f,0f,//左下方顶点
//			0.5f,0f,0f//右下方顶点

			0.5f,  0.5f, 0.0f, // top
			-0.5f, -0.5f, 0.0f, // bottom left
			0.5f, -0.5f, 0.0f  // bottom right


			//正面的顶点绘制顺序是按照逆时针方向的,反面按顺时针方向
	};


	// 红，绿，蓝三个颜色，及透明度值
	private static final float[] mColor = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

	private final int mProgram;
	//vPosition成员的句柄
	private int mPositionHandle;
	/* vColor成员的句柄 */
	private int mColorHandle;
	//uMVPMatrix成员的句柄，用于设置视图转换
	private int mMVPMatrixHandle;
	//顶点数据所需字节数，每个int类型4个字节
	private final int mVertexStride = COORDS_PER_VERTEX * 4;
	//顶点的数量
	private final int mVertexCount = TRIANGLE_COORDS.length;

	public Triangle(int type) {
		//创建顶点数组缓冲区
		mVertexBuffer = ES20Util.createCoordsBuffer(TRIANGLE_COORDS);
		mProgram = ES20Util.getESProgram(type);
	}


	public void draw() {
//		//将程序添加至OpenGL环境
//		GLES20.glUseProgram(mProgram);
//		//获取顶点着色器的vPosition成员句柄
//		mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
//		//启用三角形顶点
//		GLES20.glEnableVertexAttribArray(mPositionHandle);
//		//准备三角形坐标数据
//		GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX, GLES20.GL_FLOAT,false,mVertexStride,mVertexBuffer);
//		//获取片元着色器的vColor成员句柄
//		mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
//		//设置绘制三角形的颜色
//		GLES20.glUniform4fv(mColorHandle,1,mColor,0);
//		//绘制三角形
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,mVertexCount);
//		//禁用顶点数组
//		GLES20.glDisableVertexAttribArray(mPositionHandle);
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
		mPositionHandle = GLES20.glGetAttribLocation(mProgram,ES20Util.NAME_POSITION);
		//启用三角形顶点
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		//准备三角形坐标数据
		GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX, GLES20.GL_FLOAT,false, mVertexStride, mVertexBuffer);
		//获取片元着色器的vColor成员句柄
		mColorHandle = GLES20.glGetUniformLocation(mProgram,ES20Util.NAME_COLOR);
		//设置绘制三角形的颜色
		GLES20.glUniform4fv(mColorHandle,1, mColor,0);

		if (matrix != null) {
			//获取图形转换矩阵句柄
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram,ES20Util.NAME_MVP_MATRIX);
			ES20Util.checkError("glGetUniformLocation matrix");
			//设置矩阵
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle,1,false,matrix,0);
			ES20Util.checkError("glUniformMatrix4fv");
		}

		//绘制三角形
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0, mVertexCount);
		//禁用顶点数组
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}
}
