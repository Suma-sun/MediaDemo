package com.suma.mediademo.target5;

import android.opengl.GLES20;
import android.util.SparseIntArray;

import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * 工具类<br>
 * 1.顶点缓冲区生成器
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-11-25]
 */
public class ES20Util {

	private static final String TAG = ES20Util.class.getSimpleName();
	/**
	 * 普通模式，不带举证，默认模式
	 */
	public static final int TYPE_NORMAL = 0;
	/**
	 * 矩阵模式
	 */
	public static final int TYPE_MATRIX = 1;
	/**
	 * 纹理模式
	 */
	public static final int TYPE_TEXTURE = 2;
	/**
	 * 着色器坐标成员名
	 */
	public static final String NAME_POSITION = "vPosition";
	/**
	 * 着色器颜色成员名
	 */
	public static final String NAME_COLOR = "vColor";
	/**
	 * 着色器矩阵成员名
	 */
	public static final String NAME_MVP_MATRIX = "uMVPMatrix";
	/**
	 * 着色器纹理坐标成员名
	 */
	public static final String NAME_V_COORDINATE = "vCoordinate";
	/**
	 * 纹理片元着色器的坐标成员名
	 */
	public static final String NAME_A_COORDINATE = "aCoordinate";
	/**
	 * 着色器2D纹理采样器
	 */
	public static final String NAME_TEXTURE = "vTexture";

	private static SparseIntArray mProgramArray;

	/*
		关于着色器语言请参考：https://blog.csdn.net/junzia/article/details/52830604
	 */

//	attritude：一般用于各个顶点各不相同的量。如顶点颜色、坐标等。
//	uniform：一般用于对于3D物体中所有顶点都相同的量。比如光源位置，统一变换矩阵等。
//	varying：表示易变量，一般用于顶点着色器传递到片元着色器的量。
//	const：常量。
//	sampler2D： 采样器，专门用来进行纹理采样的相关操作。一般情况下，一个采样器变量代表一幅或一套纹理贴图

	/**
	 * 顶点着色器代码
	 */
	private static final String vertexShaderCode =
			//坐标成员变量，用于传递顶点坐标数组
			"attribute vec4 vPosition;" +
					"void main() {" +
					//给着色器内置成员传递顶点坐标向量(数组)
					"  gl_Position = vPosition;" +
					"}";

	/**
	 * 使用矩阵的顶点着色器代码
	 */
	private static final String vertexShaderCodeForMatrix =
			// 矩阵成员变量，通过钩子进行操作
			"uniform mat4 uMVPMatrix;" +
					// 使用着色器的对象坐标向量(数组)
					"attribute vec4 vPosition;" +
					"void main() {" +
//			OpenGL中使用的是列向量，如[xyzx]T，所以与矩阵相乘时，矩阵在前，向量在后。
					"  gl_Position = uMVPMatrix * vPosition;" +
					"}";

	/**
	 * 片元着色器代码
	 */
	private static final String fragmentShaderCode =
			"precision mediump float;" +
					//颜色成员变量，用于传递颜色向量（数组）
					"uniform vec4 vColor;" +
					"void main() {" +
					//给着色器内置成员（当前片元颜色）传递颜色向量(数组)
					"  gl_FragColor = vColor;" +
					"}";

	/**
	 * 纹理顶点着色器代码
	 */
	private static final String vertexShaderCodeForTextrue =
			//坐标成员变量，用于传递顶点坐标数组
			"attribute vec4 vPosition;" +
					//纹理坐标成员变量，用于传递纹理坐标向量（数组）
					"attribute vec2 vCoordinate;" +
					//矩阵变量
					"uniform mat4 uMVPMatrix;" +
					//顶点着色器传递到片元着色器的纹理坐标向量（数组）
					"varying vec2 aCoordinate;" +
					"void main(){" +
					//给着色器内置成员传递 矩阵变换后的坐标向量（数组）
					"gl_Position=uMVPMatrix*vPosition;" +
					//用于给片元着色器传递纹理坐标
					"aCoordinate=vCoordinate;" +
					"}";

	/**
	 * 纹理片元着色器代码
	 */
	private static final String fragmentShaderCodeForTextrue =
//			precision <精度> <类型> 制定默认精度，精度越高，画质越好，使用的资源也越多
//			lowp：低精度。8位。
//			mediump：中精度。10位。
//			highp：高精度。16位。
			"precision mediump float;" +
					//用于访问2维纹理的采样器
					"uniform sampler2D vTexture;" +
					//顶点着色器传递的纹理坐标向量（数组）
					"varying vec2 aCoordinate;" +

					"void main(){" +
					//texture2D：对2d纹理进行采样
					//给着色器内置成员（当前片元颜色）传递颜色向量(数组)
					"gl_FragColor=texture2D(vTexture,aCoordinate);" +
					"}";


	/**
	 * 获取默认着色器的es程序
	 */
	public static int getESProgram() {
//		return getESProgram(TYPE_NORMAL);
		//不使用缓存，会出现卡死
		return createProgram(TYPE_NORMAL);
	}

	/**
	 * 获取指定着色器的es程序
	 */
	public static int getESProgram(int type) {
		//不使用缓存，会出现卡死
		return createProgram(type);

//		if (mProgramArray == null) {
//			mProgramArray = new SparseIntArray(2);
//			Log.d(TAG,"init es20 array");
//		}
//		//着色器创建关联等只需执行一次
//		int program = mProgramArray.get(type);
//		if (program != 0) {
//			Log.d(TAG,"return es cache");
//			return program;
//		}
//		Log.d(TAG,"create es start");
//		program = createProgram(type);
//		Log.d(TAG,"create es end");
//		mProgramArray.put(type,program);
//		return program;
	}

	/**
	 * 创建es程序
	 *
	 * @param type 顶点着色器类型
	 */
	private static int createProgram(int type) {
		int shader;
		int fragmentShader;
		//加载顶点着色器及加载片元着色器
		if (type == TYPE_MATRIX) {
			shader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCodeForMatrix);
			fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		} else if (type == TYPE_TEXTURE) {
			shader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCodeForTextrue);
			fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCodeForTextrue);
		} else {
			shader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		}
		Log.i(TAG, "load shader end");
		//创建一个空的OpenGL ES 2.0程序
		int program = GLES20.glCreateProgram();
		ES20Util.checkError("glCreateProgram");
		//将顶点着色器添加至OpenGL程序中
		GLES20.glAttachShader(program, shader);
		ES20Util.checkError("glAttachShader");
		Log.i(TAG, "attach shader end");
		//将片元着色器添加至OpenGL程序中
		GLES20.glAttachShader(program, fragmentShader);
		ES20Util.checkError("glAttachShader");
		Log.i(TAG, "attach fragmentShader end");
		//创建OpenGL可执行文件
		GLES20.glLinkProgram(program);
		ES20Util.checkError("glLinkProgram");
		Log.i(TAG, "LinkProgram end");
		Log.d(TAG,StringUtils.format("createProgram end type=%d, program=%d, shader=%d, fragmentShader=%d",type,program,shader,fragmentShader));
		return program;
	}


	/**
	 * 根据顶点数组，生成对应的缓冲区对象
	 *
	 * @param coords 顶点数组
	 */
	public static FloatBuffer createCoordsBuffer(float[] coords) {
		//为什么*4，每个数据占4个字节
		ByteBuffer buffer = ByteBuffer.allocateDirect(coords.length * 4);
		//使用设备硬件的本地字节顺序，目的是提高效率
		buffer.order(ByteOrder.nativeOrder());
		//通过buffer创建FloatBuffer
		FloatBuffer vertexBuffer = buffer.asFloatBuffer();
		//添加坐标
		vertexBuffer.put(coords);
		//设置从0开始读取数据
		vertexBuffer.position(0);
		return vertexBuffer;
	}

	/**
	 * 加载着色器
	 *
	 * @param type      {@link GLES20#GL_VERTEX_SHADER} {@link GLES20#GL_FRAGMENT_SHADER}
	 * @param shardCode 着色器代码
	 */
	public static int loadShader(int type, String shardCode) {
		//创建一个指定类型的着色器
		int shader = GLES20.glCreateShader(type);
		if (shader == 0) {
			Log.e(TAG, "glCreateShader fail");
		}
		//将着色器代码添加到着色器中
		GLES20.glShaderSource(shader, shardCode);
		//编译着色器
		GLES20.glCompileShader(shader);
//		int[] compile = new int[1];
		IntBuffer compile = IntBuffer.allocate(1);
		//从着色器对象返回一个参数
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile);
		if (compile.get(0) != GLES20.GL_TRUE) {
			GLES20.glDeleteShader(shader);
			shader = 0;
			Log.e(TAG, "glCompileShader fail");
		}
		return shader;
	}

	/**
	 * 非常重要的错误检查，ES底层报错无法得知，只能获取日志得知
	 *
	 * @param op 操作标记字符串
	 */
	public static void checkError(String op) {
		int error;
		//获取错误代码时会清除该代码，如果还有其他错误，需要循环获取
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			String err = StringUtils.format("%s : glError %s", op, error);
			Log.e(TAG, err);
			throw new RuntimeException(err);
		}
	}
}
