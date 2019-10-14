package com.suma.mediademo.target4;

import android.annotation.TargetApi;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import com.suma.mediademo.Notifyable;
import com.suma.mediademo.utils.FileUtils;
import com.suma.mediademo.utils.Log;
import com.suma.mediademo.utils.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 抽取mp4的音频文件输出 <br>
 * 抽取音频,并在每一帧增加adts头
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-10-14]
 */
public class ExtractAudio implements Runnable {
	private WeakReference<Notifyable> reference;
	private String mPath;
	private File mAudioFile;

	public ExtractAudio(Notifyable notifyable, String srcPath, File audio) {
		this.reference = new WeakReference<>(notifyable);
		this.mPath = srcPath;
		this.mAudioFile = audio;
	}

	@Override
	public void run() {
		Notifyable notifyable = reference.get();
		if (notifyable == null)
			return;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			notifyable.onNotify("手机版本过低,需4.1及以上");
			return;
		}

		MediaExtractor extractor = new MediaExtractor();
		try {
			//亦可设置为url地址
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
			//筛选类型,
			// audio/mp4a-latm 对应aac
			// video/avc 对应h.264编码
			Log.d(this, "mime : " + mime);
			if (MediaFormat.MIMETYPE_AUDIO_AAC.equals(mime)) {
				//选中当前轨道
				extractor.selectTrack(i);
				if (!extract(extractor, format, mAudioFile)) {
					notifyable.onNotify("音频提取失败");
				}
				extractor.unselectTrack(i);
			}
		}
		extractor.release();
		notifyable.onNotify("音频提取完毕");

	}

	/**
	 * 抽取当前轨道并输出文件
	 *
	 * @param format 当前轨道信息
	 * @param file   输出的文件
	 * @return 是否抽取输出文件成功
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private boolean extract(MediaExtractor extractor, MediaFormat format, File file) {
		int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
		int channel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

		//打印当前轨道信息
		Log.d(this, "==audio==");
		Log.d(this, StringUtils.format("==audio==mime==%s", format.getString(MediaFormat.KEY_MIME)));
		Log.d(this, StringUtils.format("==audio==duration==%s", format.getLong(MediaFormat.KEY_DURATION)));
		Log.d(this, StringUtils.format("==audio==samplwRate==%d", sampleRate));
		Log.d(this, StringUtils.format("==audio==channelCount==%d", channel));
		Log.d(this, StringUtils.format("==audio==sampleTime==%d", extractor.getSampleTime()));
		//删除旧文件后创建
		FileUtils.createFileByDeleteOldFile(file);
		//这里的缓冲区大小使用Formate里的值是防止过度申请内存和避免一次读取数据不完整需要循环读取
		int size = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
		ByteBuffer buffer = ByteBuffer.allocate(size);

		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			int count;
			while ((count = extractor.readSampleData(buffer, 0)) > 0) {
				//mp4中抽取的aac数据是不带ADTS头文件的,需手动添加

				//7为头文件长度
				byte[] bytes = new byte[count + 7];
				//生成并插入头文件
				addADTStoPacket(bytes, count + 7,channel,sampleRate);
				//拷贝音频帧数据
				System.arraycopy(buffer.array(), 0, bytes, 7, count);

				outputStream.write(bytes, 0, bytes.length);
				buffer.clear();
				//下一帧
				extractor.advance();
			}
			outputStream.close();
			Log.d(this, "==audio==extract success");
			return true;
		} catch (FileNotFoundException e) {
			Log.e(this, e);
		} catch (IOException e) {
			Log.e(this, e);
		}
		return false;
	}

	/**
	 * 插入adts头文件
	 * @param packet 头文件载体
	 * @param packetLen 头+流的总数据长度
	 * @param channel 声道
	 * @param sampleRate 采样率
	 */
	private void addADTStoPacket(byte[] packet, int packetLen, int channel, int sampleRate) {
        /*
        标识使用AAC级别 当前选择的是LC
        一共有1: AAC Main 2:AAC LC (Low Complexity) 3:AAC SSR (Scalable Sample Rate) 4:AAC LTP (Long Term Prediction)
        */
		int profile = 2;//AAC LC,基本所有硬件均支持AAC LC

		int frequencyIndex = 0x04; //设置采样率44.1KHz

		switch (sampleRate) {
			case 48000:
				frequencyIndex = 0x03;
				break;
			case 44100:
				frequencyIndex = 0x04;
				break;
			case 64000:
				frequencyIndex = 0x02;
				break;
			case 32200:
				frequencyIndex = 0x05;
				break;
			case 16000:
				frequencyIndex = 0x08;
				break;

		}

//		syncword：					12bit	帧同步标识一个帧的开始，固定为0xFFF
//		ID：							1bit	MPEG 标示符。0表示MPEG-4，1表示MPEG-2
//		layer：						2bit	固定为'00'
//		protection_absent：			1bit 	标识是否进行误码校验。0表示有CRC校验，1表示没有CRC校验
//		profile：					2bit	标识使用哪个级别的AAC。1: AAC Main 2:AAC LC (Low Complexity) 3:AAC SSR (Scalable Sample Rate) 4:AAC LTP (Long Term Prediction)
//		sampling_frequency_index：	4bit 	标识使用的采样率的下标
//		private_bit：				1bit 	私有位，编码时设置为0，解码时忽略
//		channel_configuration：		3bit 	标识声道数
//		original_copy：				1bit 	编码时设置为0，解码时忽略
//		home：						1bit 	编码时设置为0，解码时忽略



		// fill in ADTS data
		packet[0] = (byte) 0xFF; //1111 1111
		// syncword
		packet[1] = (byte) 0xF9; //1111 1001
		// 1111 还是syncword
		// 1001 第一个1 代表MPEG-2,接着00为常量，最后一个1，标识没有CRC
		packet[2] = (byte) (((profile - 1) << 6) + (frequencyIndex << 2) + (channel >> 2));
		packet[3] = (byte) (((channel & 3) << 6) + (packetLen >> 11));
		packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
		packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
		packet[6] = (byte) 0xFC;
	}
}
