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
 * 根据传入的aac文件与视频mp4文件合成新的mp4文件 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-14]
 */
public class CreateMp4 implements Runnable {

	private WeakReference<Notifyable> reference;
	private File mNewFile;
	private File mAudioFile;
	private File mVideoFile;


	/**
	 * 根据音频,视频合成MP4
	 *
	 * @param outFile 输出的MP4文件
	 * @param audio   音频文件
	 * @param video   视频文件
	 */
	public CreateMp4(Notifyable reference, File outFile, File audio, File video) {
		this.reference = new WeakReference<>(reference);
		this.mNewFile = outFile;
		this.mAudioFile = audio;
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
		if (!mAudioFile.exists() || !mVideoFile.exists()) {
			notifyable.onNotify("音频或视频文件不存在");
			return;
		}
		FileUtils.createFileByDeleteOldFile(mNewFile);

		MediaExtractor audioExtractor = new MediaExtractor();
		MediaExtractor videoExtractor = new MediaExtractor();

		if (setAudioSource(notifyable, audioExtractor)) return;
		if (setVideoSource(notifyable, videoExtractor)) return;

		final int audioIndex = getAudioIndex(audioExtractor);
		final int videoIndex = getVideoIndex(videoExtractor);
		//校验音频\视频轨道是否存在
		if (audioIndex == -1 || videoIndex == -1) {
			notifyable.onNotify("视频/音频轨道提取失败");
			Log.w(this, StringUtils.format("video index = %d,audio index = %d", videoIndex, audioIndex));
			return;
		}

		final MediaFormat audioFormat = audioExtractor.getTrackFormat(audioIndex);
		final MediaFormat videoFormat = videoExtractor.getTrackFormat(videoIndex);
		//获取音轨数据每帧时间(单位微秒)
		long audioTimeUs = getTimeUs(audioExtractor, audioIndex);
		long videoTimeUs = getTimeUs(videoExtractor, videoIndex);
		//获取了时间,需要重新设置轨道
		audioExtractor.selectTrack(audioIndex);
		videoExtractor.selectTrack(videoIndex);

		MediaMuxer muxer = null;
		try {
			muxer = new MediaMuxer(mNewFile.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		} catch (IOException e) {
			Log.e(this, e);
			notifyable.onNotify("视频合成失败");
		}
		if (muxer == null) {
			notifyable.onNotify("视频合成失败");
			return;
		}


		int audioTrack = muxer.addTrack(audioFormat);
		int videoTrack = muxer.addTrack(videoFormat);
		muxer.start();
		Log.d(this, "start muxer");

		writeAudio(audioExtractor, audioIndex, audioFormat, audioTimeUs, muxer, audioTrack);


		writeVideo(videoExtractor, videoIndex, videoFormat, videoTimeUs, muxer, videoTrack);

		muxer.stop();
		muxer.release();
		notifyable.onNotify("视频合成完毕");
		Log.d(this, "create success");

	}

	/**
	 * 写入视频数据
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void writeVideo(MediaExtractor videoExtractor, int videoIndex, MediaFormat videoFormat, long timeUs, MediaMuxer muxer, int videoTrack) {
		MediaCodec.BufferInfo videoInfo = new MediaCodec.BufferInfo();
		ByteBuffer buffer = ByteBuffer.allocate(videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
		int count;
		while ((count = videoExtractor.readSampleData(buffer, 0)) > 0) {
			videoInfo.size = count;
			videoInfo.offset = 0;
			videoInfo.flags = videoExtractor.getSampleFlags();
			videoInfo.presentationTimeUs += timeUs;//需要转换单位为微秒
			muxer.writeSampleData(videoTrack, buffer, videoInfo);
			buffer.clear();
			videoExtractor.advance();
		}
		videoExtractor.unselectTrack(videoIndex);
		videoExtractor.release();
	}

	/**
	 * 写入音频数据
	 *
	 * @param audioTimeUs 每帧时间(微秒)
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void writeAudio(MediaExtractor audioExtractor, int audioIndex, MediaFormat audioFormat,
							long audioTimeUs, MediaMuxer muxer, int audioTrack) {

		MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();
		int count;
		//这个会报空指针,自己保存的aacm数据没有保存每帧数据大小
//		ByteBuffer buffer = ByteBuffer.allocate(audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));

		ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
		// TODO: 2019-10-22  循环累加获取每帧数据大小,可避免过度申请内存空间.但结果是传入的offset不起作用,需查看native层代码
//		int offset = 0;
//		while ((count = audioExtractor.readSampleData(buffer, offset)) > 0) {
//			offset += count;
//			Log.d(this, StringUtils.format("audio sample buffer offser %d", offset));
//		}
//		buffer = ByteBuffer.allocate(offset);

		int i = 0;
		while ((count = audioExtractor.readSampleData(buffer, 0)) > 0) {
			audioInfo.offset = 0;
			audioInfo.flags = audioExtractor.getSampleFlags();
			audioInfo.size = count;
			audioInfo.presentationTimeUs += audioTimeUs;
			muxer.writeSampleData(audioTrack, buffer, audioInfo);
			buffer.clear();
			Log.d(this,StringUtils.format("%d count %d",i,count));
			audioExtractor.advance();
		}
		audioExtractor.unselectTrack(audioIndex);
		audioExtractor.release();
	}

	/**
	 * 获取视频轨索引
	 *
	 * @return index or -1
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private int getVideoIndex(MediaExtractor videoExtractor) {
		final int videoCount = videoExtractor.getTrackCount();
		for (int i = 0; i < videoCount; i++) {
			MediaFormat format = videoExtractor.getTrackFormat(i);
			String mime = format.getString(MediaFormat.KEY_MIME);
			if (mime.contains("video/")) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取音频轨索引
	 *
	 * @return index or -1
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private int getAudioIndex(MediaExtractor audioExtractor) {
		final int audioCount = audioExtractor.getTrackCount();
		for (int i = 0; i < audioCount; i++) {
			MediaFormat format = audioExtractor.getTrackFormat(i);
			String mime = format.getString(MediaFormat.KEY_MIME);
			if (mime.contains("audio/")) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 设置视频文件
	 *
	 * @param notifyable
	 * @param videoExtractor
	 * @return true:执行失败
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private boolean setVideoSource(Notifyable notifyable, MediaExtractor videoExtractor) {
		try {
			videoExtractor.setDataSource(mVideoFile.getPath());
		} catch (IOException e) {
			Log.e(this, e);
			notifyable.onNotify("视频源文件异常");
			return true;
		}
		return false;
	}


	/**
	 * 设置音频文件
	 *
	 * @return true:执行失败
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private boolean setAudioSource(Notifyable notifyable, MediaExtractor audioExtractor) {
		try {
			audioExtractor.setDataSource(mAudioFile.getPath());
		} catch (IOException e) {
			Log.e(this, e);
			notifyable.onNotify("音频源文件异常");
			return true;
		}
		return false;
	}

	/**
	 * 获取单帧时间单位为微秒
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private long getTimeUs(MediaExtractor extractor, int index) {
		extractor.selectTrack(index);

		//获取第一帧的时间一般为0
//		long frist = extractor.getSampleTime();
		Log.d(this, StringUtils.format("==第一帧==sampleTime:%d==isSync:%b", extractor.getSampleTime(), extractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC));
		extractor.advance();
		Log.d(this, StringUtils.format("==第二帧==sampleTime:%d==isSync:%b", extractor.getSampleTime(), extractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC));
		//第二帧的数据一般是每帧的时间,us为单位
		long next = extractor.getSampleTime();
		extractor.unselectTrack(index);
		return next;
		//将第二帧与第一帧的差作为每帧的时间
//		return Math.abs(next-frist);
	}
}
