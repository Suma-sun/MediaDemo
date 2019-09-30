package com.suma.mediademo.target2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * WavHeader辅助类,用于生成头部信息 <br>
 *
 * @author suma 284425176@qq.com
 * @version [1.0, 2019-09-04]
 */
public class WaveHeader {

	public final char fileID[] = {'R', 'I', 'F', 'F'};
	/** 从下个地址开始到文件尾的总字节数,文件长度-8*/
	public int fileLength;
	public final char wavTag[] = {'W', 'A', 'V', 'E'};

	public final char FmtHdrID[] = {'f', 'm', 't', ' '};
	/** 过滤字节（一般为00000010H），若为00000012H则说明数据头携带附加信息（见“附加信息”）*/
	public int FmtHdrLeth = 16;//默认无附加信息
	/** 格式种类（值为1时，表示数据为线性PCM编码）*/
	public short FormatTag = 1;
	/** 通道数*/
	public short Channels;
	/** 采样频率*/
	public int SamplesPerSec;
	/** 波形数据传输速率*/
	public int AvgBytesPerSec;
	/** DATA数据块长度，系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数*/
	public short BlockAlign;
	/** PCM位宽*/
	public short BitsPerSample;
	public char DataHdrID[] = {'d', 'a', 't', 'a'};
	/** 音频数据的总字节长度*/
	public int DataHdrLeth;

	public byte[] getHeader() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		WriteChar(bos, fileID);// "RIFF"
		WriteInt(bos, fileLength);// 从下个地址开始到文件尾的总字节数,文件长度-8
		WriteChar(bos, wavTag);// "WAVE"
		WriteChar(bos, FmtHdrID);// "fmt "
		WriteInt(bos, FmtHdrLeth);// 过滤字节（一般为00000010H），若为00000012H则说明数据头携带附加信息（见“附加信息”）。
		WriteShort(bos, FormatTag);// 格式种类（值为1时，表示数据为线性PCM编码）
		WriteShort(bos, Channels);// 通道数，单声道为1，双声道为2
		WriteInt(bos, SamplesPerSec);// 采样频率
		WriteInt(bos, AvgBytesPerSec);// 波形数据传输速率（每秒平均字节数）
		WriteShort(bos, BlockAlign);// DATA数据块长度，字节。
		WriteShort(bos, BitsPerSample);// PCM位宽
		WriteChar(bos, DataHdrID);// "data"
		WriteInt(bos, DataHdrLeth);// DATA总数据长度字节
		bos.flush();
		byte[] r = bos.toByteArray();
		bos.close();
		return r;
	}

	private void WriteShort(ByteArrayOutputStream bos, int s) throws IOException {
		byte[] mybyte = new byte[2];
		mybyte[1] = (byte) ((s << 16) >> 24);
		mybyte[0] = (byte) ((s << 24) >> 24);
		bos.write(mybyte);
	}


	private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException {
		byte[] buf = new byte[4];
		buf[3] = (byte) (n >> 24);
		buf[2] = (byte) ((n << 8) >> 24);
		buf[1] = (byte) ((n << 16) >> 24);
		buf[0] = (byte) ((n << 24) >> 24);
		bos.write(buf);
	}

	private void WriteChar(ByteArrayOutputStream bos, char[] id) {
		for (int i = 0; i < id.length; i++) {
			char c = id[i];
			bos.write(c);
		}
	}

}

