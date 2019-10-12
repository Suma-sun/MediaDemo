package com.suma.mediademo.target2;

/**
 * 音频功能接口 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-07-01]
 */
public interface Audioable {

	/**
	 * 播放音频
	 * @param fileName 播放文件路径
	 */
	void play(String fileName);

	/**
	 * 录制音频
	 * @param fileName 保存的文件路径
	 */
	void record(String fileName);

	/**
	 * 停止录制/播放
	 */
	void stop();

	/**
	 * 释放资源
	 */
	void release();



}
