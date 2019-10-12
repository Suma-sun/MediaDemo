package com.suma.mediademo.target2;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.suma.mediademo.utils.Constant;
import com.suma.mediademo.utils.FileUtils;
import com.suma.mediademo.utils.OpenFileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 音频能力实现 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-07-01]
 */
public class AudioImpl implements Audioable {
	private static final String TAG = "AudioImpl";

	public enum State {
		/**
		 * 异常
		 */
		ERROR,
		/**
		 * 闲置
		 */
		IDLE,
		/**
		 * 录音中
		 */
		RECORDING,
		/**
		 * 停止录音
		 */
		STOP_RECORD,
		/**
		 * 播放录音
		 */
		PLAYING,
		/**
		 * 停止播放
		 */
		STOP_PLAY
	}


	private static final int BYTE_SIZE = /*4096*/10240;
	/**
	 * 所有设备均可用的最大HZ,拥有CD级音质
	 */
	private static final int MAX_HZ = 44100;

	private WeakReference<Context> mContext;

	AtomicReference<State> mState;

	private Handler mHandler;
	/**
	 * 读写的PCM目标文件
	 */
	private File mPCMFile;
	/**
	 * 读写的WAV目标文件
	 */
	private File mWAVFile;

	private ExecutorService mThreadPool;

	public AudioImpl(Context context, String pcmFilePath, String wavFilePath) {
		this.mContext = new WeakReference<>(context);
		Log.d(TAG, "pcmFilePath = " + pcmFilePath);
		Log.d(TAG, "wavFilePath = " + wavFilePath);
		mState = new AtomicReference<State>(State.IDLE);
		mPCMFile = new File(pcmFilePath);
		mWAVFile = new File(wavFilePath);
		mHandler = new Handler(context.getMainLooper());
		mThreadPool = Executors.newSingleThreadExecutor();
	}


	private void toast(String mess) {
		if (mContext != null && mContext.get() != null) {
			Toast.makeText(mContext.get(), mess, Toast.LENGTH_SHORT).show();
		}
	}

	private synchronized void notityState(final State state) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				toast(state.name());
			}
		});
	}

	/**
	 * 播放音频
	 *
	 * @param fileName 播放文件路径
	 */
	@Override
	public void play(String fileName) {
		if (mState.get() != State.IDLE) {
			toast("播放失败,当前状态为:" + mState.get().name());
			return;
		}
		if (fileName.endsWith(Constant.PCM_EXTENSION)) {
			mThreadPool.submit(new AudioTrackPlayThread(this,fileName));
			return;
		}

		File file = new File(fileName);
		if (!file.exists()){
			toast("播放失败,无文件可播放");
			return;
		}
		if (mContext.get() != null) {
			Context context = mContext.get();
			//不止为何,当前录制的音频有权限播放,关闭重启应用后无权访问
			Intent intent = OpenFileUtil.openFile(context,file.getPath());
			if (intent == null) {
				toast("播放失败,未知文件类型");
			} else {
				context.startActivity(intent);
			}
		}
	}

	/**
	 * 录制音频
	 *
	 * @param fileName 保存的文件路径
	 */
	@Override
	public void record(String fileName) {
		if (mState.get() != State.IDLE) {
			toast("录音失败,当前状态为:" + mState.get().name());
			return;
		}
		mThreadPool.submit(new AudioRecordThread(this,mPCMFile,mWAVFile));

	}

	/**
	 * 停止录制/播放
	 */
	@Override
	public void stop() {
		if (mState.get() == State.PLAYING) {
			mState.set(State.STOP_PLAY);
		} else if (mState.get() == State.RECORDING) {
			mState.set(State.STOP_RECORD);
		} else {
			toast("音频未录制/播放");
		}
	}

	/**
	 * 释放资源
	 */
	@Override
	public void release() {
		if (mThreadPool != null && !mThreadPool.isShutdown()) {
			mThreadPool.shutdown();
		}
	}

	/**
	 * 音频录制线程
	 */
	private static class AudioRecordThread extends Thread {
		//wav文件头最少44位(不包含附加信息)
		private static final int WAV_HEADER_LEN = 44;
		private AudioRecord audioRecord;
		private File pcmFile;
		private File wavFile;
		private WeakReference<AudioImpl> reference;

		public AudioRecordThread(AudioImpl impl, File pcm, File wav) {
			reference = new WeakReference<AudioImpl>(impl);
			pcmFile = pcm;
			wavFile = wav;
			audioRecord = createRecord();
		}

		private AudioRecord createRecord() {
			//缓冲区大小
			int recrodBufferSize = AudioRecord.getMinBufferSize(MAX_HZ, AudioFormat.CHANNEL_OUT_STEREO,
					AudioFormat.ENCODING_PCM_16BIT);
			return new AudioRecord(MediaRecorder.AudioSource.MIC,//音频来源
					MAX_HZ,//采样的频率
					AudioFormat.CHANNEL_IN_STEREO,//音频通道,单声道为AudioFormat.CHANNEL_IN_MONO,立体声为AudioFormat.CHANNEL_IN_STEREO
					AudioFormat.ENCODING_PCM_16BIT,//采样位数
					recrodBufferSize);
		}

		@Override
		public void run() {
			AudioImpl instance = reference.get();
			if (instance == null)
				return;
			instance.mState.set(AudioImpl.State.RECORDING);//设置当前状态为录音中
			instance.notityState(AudioImpl.State.RECORDING);
			FileUtils.createFileByDeleteOldFile(pcmFile);
			FileUtils.createFileByDeleteOldFile(wavFile);
			audioRecord.startRecording();//开始录音
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(pcmFile);//创建输出的文件
				byte[] buffer = new byte[BYTE_SIZE];
				int len;
				int fileSize = 0;
				while (instance.mState.get() == AudioImpl.State.RECORDING && !interrupted()) {
					len = audioRecord.read(buffer, 0, buffer.length);
					outputStream.write(buffer, 0, len);
					fileSize += len;
				}
				outputStream.close();//关闭流
				convertWav(pcmFile, wavFile, fileSize);
			} catch (IOException e) {
				e.printStackTrace();
				instance.mState.set(AudioImpl.State.ERROR);
				instance.notityState(AudioImpl.State.ERROR);
				return;
			} finally {
				audioRecord.stop();//停止录制
				audioRecord.release();
				audioRecord = null;
			}
			instance.mState.set(AudioImpl.State.IDLE);
			instance.notityState(AudioImpl.State.IDLE);
		}

		private void convertWav(File src, File des, int fileSize) {
			try {
				FileInputStream inputStream = new FileInputStream(src);
				FileOutputStream outputStream = new FileOutputStream(des);
				BufferedOutputStream out = new BufferedOutputStream(outputStream);
				byte[] header = generateWavFileHeader(fileSize, audioRecord.getChannelCount(),
						audioRecord.getAudioFormat(), audioRecord.getSampleRate());
				out.write(header, 0, header.length);//写入头文件
				out.flush();
				byte[] buffer = new byte[BYTE_SIZE];
				int len = 0;
				while ((len = inputStream.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				//outputStream为空实现
				//只有使用BufferedOutputStream时有具体的实现,内置一个缓冲区,当数据大于缓冲区的时候会把数据写入磁盘
				out.flush();//故循环外需要手动调用,避免缓冲区未满未写入磁盘
				inputStream.close();
				out.close();
				outputStream.close();
			} catch (IOException e) {
				Log.e(TAG,e.getMessage());
			}

		}


		/**
		 * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
		 * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
		 * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的
		 *
		 * @param pcmSize       不包括header的音频数据总长度
		 * @param channels      audioRecord的频道数量
		 * @param audioFormat  pcm位宽{@link AudioFormat#ENCODING_PCM_8BIT} {@link AudioFormat#ENCODING_PCM_16BIT}
		 * @param samplesPerSec 采样频率
		 */
		private byte[] generateWavFileHeader(int pcmSize, int channels, int audioFormat, int samplesPerSec) {
			WaveHeader header = new WaveHeader();
			//长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
			header.fileLength = pcmSize + (WAV_HEADER_LEN - 8);
			header.FmtHdrLeth = 16;//00000010H  无附加信息
			int bitPerSample = 16;
			if (audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
				bitPerSample = 8;
			}
			header.BitsPerSample = (short) bitPerSample;//PCM位宽
			header.Channels = (short) channels;
			header.FormatTag = 0x0001;
			header.SamplesPerSec = samplesPerSec;
			header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
			header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
			header.DataHdrLeth = pcmSize;

			byte[] h = new byte[44];
			try {
				h = header.getHeader();
//				for (int i = 0; i < h.length; i++) {
//					System.out.print(Integer.toHexString(h[i]) + " ");
//				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return h;
		}

	}

	/**
	 * 音频播放线程
	 */
	private static class AudioTrackPlayThread extends Thread {
		private WeakReference<AudioImpl> reference;
		private AudioTrack audioTrack;
		private File file;

		public AudioTrackPlayThread(AudioImpl impl, String filePath) {
			reference = new WeakReference<AudioImpl>(impl);
			file = new File(filePath);
			if (!file.exists()) {
				throw new RuntimeException("播放文件不能为空");
			}
			audioTrack = createTrack();
		}

		@Override
		public void run() {
			AudioImpl instance = reference.get();
			if (instance == null)
				return;
			instance.mState.set(AudioImpl.State.PLAYING);//设置状态播放中
			instance.notityState(AudioImpl.State.PLAYING);
			try {
				FileInputStream inputStream = new FileInputStream(file);
				byte[] buffer = new byte[BYTE_SIZE];
				int len = 0;
				audioTrack.play();
				while (instance.mState.get() == AudioImpl.State.PLAYING && !interrupted()
						&& (len = inputStream.read(buffer, 0, buffer.length)) > 0) {
					audioTrack.write(buffer, 0, len);
				}
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
				instance.mState.set(AudioImpl.State.ERROR);
				instance.notityState(AudioImpl.State.ERROR);
				return;
			} finally {
				audioTrack.stop();//停止播放音频流数据
				audioTrack.release();//释放底层音频数据
				audioTrack = null;
			}
			instance.mState.set(AudioImpl.State.IDLE);
			instance.notityState(AudioImpl.State.IDLE);
		}

		private AudioTrack createTrack() {
			//缓冲区大小
			int playBufferSize = AudioTrack.getMinBufferSize(MAX_HZ, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
			AudioTrack audioTrack;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				AudioAttributes.Builder builder = new AudioAttributes.Builder();
				builder.setUsage(AudioAttributes.USAGE_MEDIA);
				//音频流的类型
				builder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
				//具体输出类型
				builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
				//系统确保声音可听性的行为的标志
				builder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);

				AudioFormat.Builder formatBuilder = new AudioFormat.Builder();
				formatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT);//采样位数
				formatBuilder.setSampleRate(MAX_HZ);//采样率
				formatBuilder.setChannelMask(AudioFormat.CHANNEL_OUT_STEREO);//立体声
				audioTrack = new AudioTrack(builder.build(),
						formatBuilder.build(),
						playBufferSize,
						AudioTrack.MODE_STREAM,//流模式,静态模式适合短音频
						AudioManager.AUDIO_SESSION_ID_GENERATE);
			} else {
				audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, MAX_HZ,
						AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
						playBufferSize, AudioTrack.MODE_STREAM);
			}
			return audioTrack;
		}
	}
}
