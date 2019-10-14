package com.suma.mediademo;

/**
 * 通知接口 <br>
 *     主要用于子线程发送通知给主线程展示
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-14]
 */
public interface Notifyable {

	void onNotify(String msg);
}
