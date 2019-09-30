2. 在 Android 平台使用 AudioRecord 和 AudioTrack API 完成音频 PCM 数据的采集和播放，并实现读写音频 wav 文件

--PCM--
PCM是在由模拟信号向数字信号转化的一种常用的编码格式，称为脉冲编码调制，PCM将模拟信号按照一定的间距划分为多段，然后通过二进制去量化每一个间距的强度。
PCM表示的是音频文件中随着时间的流逝的一段音频的振幅。Android在WAV文件中支持PCM的音频数据。

--WAV--
WAV，MP3等比较常见的音频格式，不同的编码格式对应不通过的原始音频。为了方便传输，通常会压缩原始音频。
为了辨别出音频格式，每种格式有特定的头文件（header）。
WAV以RIFF为标准。RIFF是一种资源交换档案标准。RIFF将文件存储在每一个标记块中。
基本构成单位是trunk，每个trunk由标记位，数据大小，数据存储，三个部分构成。




--PCM打包成WAV--
PCM是原始音频数据，WAV是windows中常见的音频格式，只是在pcm数据中添加了一个文件头。



--其他概念--
https://blog.csdn.net/tanningzhong/article/details/72844559