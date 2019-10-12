学习 Android 平台的 MediaExtractor 和 MediaMuxer API，知道如何解析和封装 mp4 文件

MediaExtractor 多媒体提取器 https://developer.android.google.cn/reference/android/media/MediaExtractor.html
可用于分离视频文件的音轨和视频轨道，如果你只想要视频，那么用selectTrack方法选中视频轨道，然后用readSampleData读出数据，这样你就得到了一个没有声音的视频，想得到音频也可以用同样的方法。