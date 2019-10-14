学习 Android 平台的 MediaExtractor 和 MediaMuxer API，知道如何解析和封装 mp4 文件

MediaExtractor 多媒体提取器 可用于分离视频文件的音轨和视频轨道
https://developer.android.google.cn/reference/android/media/MediaExtractor.html
https://blog.csdn.net/u010126792/article/details/86497571


MediaMuxer 多媒体合成器 可用于将视频文件,音频文件合成成mp4文件
https://developer.android.google.cn/reference/kotlin/android/media/MediaMuxer?hl=en
https://blog.csdn.net/u010126792/article/details/86510903



MediaFormat mime类型部分对照表
“video/x-vnd.on2.vp8” - VP8 video (i.e. video in .webm)
“video/x-vnd.on2.vp9” - VP9 video (i.e. video in .webm)
“video/avc” - H.264/AVC video
“video/mp4v-es” - MPEG4 video
“video/3gpp” - H.263 video
“audio/3gpp” - AMR narrowband audio
“audio/amr-wb” - AMR wideband audio
“audio/mpeg” - MPEG1/2 audio layer III
“audio/mp4a-latm” - AAC audio (note, this is raw AAC packets, not packaged in LATM!)
“audio/vorbis” - vorbis audio
“audio/g711-alaw” - G.711 alaw audio
“audio/g711-mlaw” - G.711 ulaw audio


mp4数据构成
https://blog.csdn.net/fireroll/article/details/80711664