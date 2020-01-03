package com.suma.mediademo.target5;

import android.opengl.GLES20;
import android.util.SparseIntArray;

import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 工具类<br>
 * 1.顶点缓冲区生成器
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-11-25]
 */
public class ES20Util {
	/**
	 * 普通模式，不带举证，默认模式
	 */
	public static final int TYPE_NORMAL = 0;
	/**
	 * 矩阵模式
	 */
	public static final int TYPE_MATRIX = 1;
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

	private static SparseIntArray mProgramArray;

	/**
	 * 顶点着色器代码
	 */
	private static final String vertexShaderCode =
			"attribute vec4 vPosition;" +
					"void main() {" +
					"  gl_Position = vPosition;" +
					"}";

	/**
	 * 使用矩阵的顶点着色器代码
	 */
	private static final String vertexShaderCodeForMatrix =
			// 矩阵成员变量，通过钩子进行操作
			"uniform mat4 uMVPMatrix;" +
					// 使用着色器的对象坐标对象
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
					"uniform vec4 vColor;" +
					"void main() {" +
					"  gl_FragColor = vColor;" +
					"}";

	/**
	 * 获取默认着色器的es程序
	 *
	 * @return
	 */
	public static int getESProgram() {
//		return getESProgram(TYPE_NORMAL);
		//不使用缓存，会出现卡死
		return createProgram(TYPE_NORMAL);
	}

	/**
	 * 获取指定着色器的es程序
	 *
	 * @return
	 */
	public static int getESProgram(int type) {
		//不使用缓存，会出现卡死
		return createProgram(type);

//		if (mProgramArray == null) {
//			mProgramArray = new SparseIntArray(2);
//			Log.d(ES20Util.class.getSimpleName(),"init es20 array");
//		}
//		//着色器创建关联等只需执行一次
//		int program = mProgramArray.get(type);
//		if (program != 0) {
//			Log.d(ES20Util.class.getSimpleName(),"return es cache");
//			return program;
//		}
//		Log.d(ES20Util.class.getSimpleName(),"create es start");
//		program = createProgram(type);
//		Log.d(ES20Util.class.getSimpleName(),"create es end");
//		mProgramArray.put(type,program);
//		return program;
	}

	/**
	 * 创建es程序
	 * @param type 顶点着色器类型
	 */
	private static int createProgram(int type) {
		int shader;
		//加载顶点着色器
		if (type == TYPE_MATRIX)
			shader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCodeForMatrix);
		else
			shader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		Log.d(ES20Util.class.getSimpleName(),"load vertex shader end");
		//加载片元着色器
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		Log.d(ES20Util.class.getSimpleName(),"load fragment shader end");
		//创建一个空的OpenGL ES 2.0程序
		int program = GLES20.glCreateProgram();
		ES20Util.checkError("glCreateProgram");
		//将顶点着色器添加至OpenGL程序中
		GLES20.glAttachShader(program, shader);
		ES20Util.checkError("glAttachShader");
		//将片元着色器添加至OpenGL程序中
		GLES20.glAttachShader(program, fragmentShader);
		ES20Util.checkError("glAttachShader");
		//创建OpenGL可执行文件
		GLES20.glLinkProgram(program);
		ES20Util.checkError("glLinkProgram");
		return program;
	}


	/**
	 * 根据顶点数组，生成对应的缓冲区对象
	 *
	 * @param coords
	 * @return
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
	 * @param shardCode
	 * @return
	 */
	public static int loadShader(int type, String shardCode) {
		//创建一个指定类型的着色器
		int shader = GLES20.glCreateShader(type);
		//将着色器代码添加到着色器中
		GLES20.glShaderSource(shader, shardCode);
		//编译着色器
		GLES20.glCompileShader(shader);
		return shader;
	}

	/**
	 * 非常重要的错误检查，ES底层报错无法得知，只能获取日志得知
	 *
	 * @param op
	 */
	public static void checkError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			String err = StringUtils.format("%s : glError %s", op, error);
			Log.e(ES20Util.class.getSimpleName(), err);
			throw new RuntimeException(err);
		}
	}
}
