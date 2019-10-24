package com.suma.mediademo.target4;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import com.suma.mediademo.Notifyable;
import com.suma.mediademo.utils.FileUtils;
import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 提取视频轨数据并输出文件 <br>
 * 提取视频轨数据再用视频数据合成mp4文件(无音频的视频)
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-14]
 */
public class ExtractVideo implements Runnable {
	private WeakReference<Notifyable> reference;
	private String mPath;
	private File mVideoFile;

	/**
	 * 抽取视频(不带音频数据)并输出
	 *
	 * @param srcPath 可以为本地文件的path,亦可为网络文件url
	 * @param video   输出的视频文件
	 */
	public ExtractVideo(Notifyable notify, String srcPath, File video) {
		this.reference = new WeakReference<>(notify);
		this.mPath = srcPath;
		this.mVideoFile = video;
	}

	@Override
	public void run() {
		Notifyable notifyable = reference.get();
		if (notifyable == null)
			return;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			notifyable.onNotify("手机版本过低,需4.3及以上");
			return;
		}

		MediaExtractor extractor = new MediaExtractor();
		MediaMuxer mediaMuxer = null;
		try {
			extractor.setDataSource(mPath);

		} catch (IOException e) {
			Log.e(this, e);
			notifyable.onNotify("提取原文件失败");
			return;
		}
		//获取轨道数量
		final int count = extractor.getTrackCount();
		for (int i = 0; i < count; i++) {
			MediaFormat format = extractor.getTrackFormat(i);
			//获取轨道类型
			String mime = format.getString(MediaFormat.KEY_MIME);
			// video/avc 对应h.264编码
			Log.d(this, "mime : " + mime);
			if (/*MediaFormat.MIMETYPE_VIDEO_AVC.equals(mime)*/
					mime.contains("video")) {
				//选中当前轨道
				extractor.selectTrack(i);

				if (!extractAndMux(extractor, format, mVideoFile)) {
					try {
						notifyable.onNotify("视频提取失败");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				extractor.unselectTrack(i);
			}
		}
		//释放资源
		extractor.release();
		notifyable.onNotify("视频提取完毕");

	}

	/**
	 * 抽取视频轨道并输出文件
	 *
	 * @param format 当前轨道信息
	 * @param file   输出的文件
	 * @return 是否抽取输出文件成功
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private boolean extractAndMux(MediaExtractor extractor, MediaFormat format, File file) {
		//打印当前轨道信息
		Log.d(this, "==video==");
		Log.d(this, StringUtils.format("==video==mime==%s", format.getString(MediaFormat.KEY_MIME)));
		Log.d(this, StringUtils.format("==video==duration==%s", format.getLong(MediaFormat.KEY_DURATION)));
		int frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);//以帧/秒为单位描述视频格式的帧速率的键
		Log.d(this, StringUtils.format("==video==frameRate==%d", frameRate));
		Log.d(this, StringUtils.format("==video==flags==%d", extractor.getSampleFlags()));
		Log.d(this, StringUtils.format("==video==sampleTime==%d", extractor.getSampleTime()));
		Log.d(this, StringUtils.format("==video==TrackIndex==%d", extractor.getSampleTrackIndex()));
		Log.d(this, StringUtils.format("==video==byteSize==%d", format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)));

		//删除旧文件后创建
		FileUtils.createFileByDeleteOldFile(file);

		MediaMuxer mediaMuxer;
		try {
			mediaMuxer = new MediaMuxer(file.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		} catch (IOException e) {
			Log.e(this, e);
			return false;
		}

		//添加视频轨
		int trackId = mediaMuxer.addTrack(format);

		mediaMuxer.start();

		//这里的缓冲区大小使用Formate里的值是防止过度申请内存和避免一次读取数据不完整需要循环读取
		int size = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
		ByteBuffer buffer = ByteBuffer.allocate(size);

		int count;
//		int i = 0;
		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
		while ((count = extractor.readSampleData(buffer, 0)) > 0) {

			//buffer开始的偏移量，通常设为0。
			info.offset = 0;
			//必须给出正确的时间戳，注意单位是 us，第二次getSampleTime()和首次getSampleTime()的时间差。
			info.presentationTimeUs += 1000 * 1000 / frameRate;
			//帧数据大小
			info.size = count;
			//需要给出是否为同步帧/关键帧
			info.flags = extractor.getSampleFlags();
			mediaMuxer.writeSampleData(trackId, buffer, info);
			//打印出每一帧数据的时间,是否关键帧
//			Log.d(this, StringUtils.format("==video:%d==sampleTime:%d==isSync:%b",i, extractor.getSampleTime(),extractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC));
			buffer.clear();
			//下一帧
			extractor.advance();
//			i++;
		}
		mediaMuxer.stop();
		mediaMuxer.release();
		Log.d(this, "==video==extract success");
		return true;
	}
}
