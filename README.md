## 简介 
### 加强版的VideoView
> 1：将视频播放与视频控制页面进行分离；  
> 2：修改播放进度条的样式；  
> 3：增加视频音量控制；  
> 4：增加视频亮度控制；  
> 5：增加视频快进后退控制；
> 6：增加视频播放的小屏处理
### 采用全开放式样式
> a: 用户可以自定义视频控制页面;  
> b: 用户可以自定义音量控制弹窗样式;  
> c: 用户可以自定义亮度控制弹窗样式);  
> d: 用户可以自定义快进后退控制弹窗样式;  
> e: 用户可以控制视频的最大缩小比例
### HOW TO USE ?
#### first（添加maven）
` allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  `  
#### next(添加依赖)
`  dependencies {
	        implementation 'com.github.Laughing-program-monkey:StrongVideoView:版本号'
	}
  ` 
  
### Introducing attributes
Attribute  | Describe  | Type | Default value | Must
---- | ----- |  --- | ---- | -----
controlView  | 视频控制页样式布局 |  reference  | ---- | NO
volumeDilogView  | 声音弹窗样式布局 |  reference  | ---- | NO
brightDilogView  | 亮度弹窗样式布局 |  reference  | ---- | NO
progressDilogView  | 调节进度弹窗样式布局 |  reference  | ---- | NO
auto_play  | 是否自动播放 |  boolean  | false | NO
loop_play  | 是否循环播放 |  boolean  | false | NO
allow_small_window  | 是否允许小窗播放 |  boolean  | true | NO
allow_full_window  | 是否允许全屏播放 |  boolean  | false | NO
play_image  | 播放的图标 |  reference  | ---- | NO
pause_image | 暂停的图标 |  reference  | ---- | NO
hide_close_btn | 是否隐藏关闭按钮 |  boolean  | false | NO
max_scale_narrow_rate  | 小屏状态的最大缩放比 |  float  | 0.5f | NO

### Layout（以图片指示器为例）
```
<videoview.StrongVideoView
        android:id="@+id/strongvideoview"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        app:hide_close_btn="true"
        app:allow_full_window="false"
        app:allow_small_window="true"
        app:auto_play="false"
        ></videoview.StrongVideoView>

```
### Code
``` 
video.videoPath(path); 
> 若采用小屏缩放功能 则：
> 先拿到监听:videoViewScrollListener = video.getScrollListener();
> 其次外部滚动监听的onScrollChange方法里面添加
 video.setScaleAttr(provix, proviy);
        if (videoViewScrollListener != null) {
            videoViewScrollListener.scroll(scrollY, "缩小的起始值", "缩小的结束值");
        }
        
```
