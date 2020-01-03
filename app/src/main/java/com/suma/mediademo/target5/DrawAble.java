package com.suma.mediademo.target5;

/**
 * 绘制图形能力 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2020-01-03]
 */
public interface DrawAble {
	/**
	 * 绘制图形
	 */
	void draw();

	/**
	 * 带矩阵功能绘制图形
	 * @param matrix
	 */
	void draw(float[] matrix);
}
