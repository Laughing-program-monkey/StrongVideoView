package videoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.File;

import Interface.VideoControl;
import Interface.VideoViewScrollListener;
import Interface.ViewControl;
import example.strongview.App;
import example.strongview.R;

/**
 * date    : 2020/4/18 18:33
 */
public class StrongVideoView extends RelativeLayout implements View.OnClickListener, View.OnTouchListener, VideoControl, VideoViewScrollListener {
    private RelativeLayout root;
    private VideoView mVideoView;
    private LinearLayout controlContainer;
    private boolean changeVolume;//是否改变声音
    private boolean changeBright;//是否改变亮度
    private boolean changePosition;//是否快进；
    private float downX;//拿到点击下去的x坐标
    private float downY;//拿到点击下去的y坐标
    private int screenWidth;//拿到屏幕的宽度
    private boolean volumeState = true;//开关状态true开，false关
    private int privoX;//实现小屏时的缩放中心x坐标
    private int privoY;//实现小屏时的缩放中心的y坐标
    private float scaleX = 0.5f;//实现小屏的x方向缩放比例
    private float scaleY = 0.5f;//实现小屏的y方向的缩放比例
    private int play_image;//播放的图标
    private int pause_image;//暂停的图标
    private ViewControl mViewControl;//控制页面接口
    private VideoControlView videoControlView;//控制页面
    private int controlViewId;//控制页面的id；
    private int volumeDialogId;//声音弹框id；
    private int brightDialogId;//亮度弹框id;
    private int progressDialogId;//进度弹框id;
    private InnerVideoControl videoControl;//外部事件的监听
    private boolean auto_play;//是否自动播放
    private boolean loop_play;//是否循环播放
    private boolean allow_small_window;//是否允许小屏播放
    private boolean allow_full_window;//是否允许全屏播放
    private boolean hide_close_btn;//是否隐藏关闭按钮
    private Context mContext;
    private String videoPath;//暂存播放路径
    private boolean Finish = false;
    private AudioManager mAudioManager;
    private float rate;//缩放比例
    private int lastY;//上一次滚动的y轴距离
    private float MaxScaleRate;
    private ImageView thumb_image;
    public StrongVideoView(@NonNull Context context) {
        super(context);
    }
    public StrongVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initAttr(context, attrs);
        init(context);
    }

    //初始化view
    public void init(Context context) {
        View.inflate(context, R.layout.videoview_layout, this);
        root = (RelativeLayout) findViewById(R.id.root);
        mVideoView = (VideoView) findViewById(R.id.video);
        controlContainer = (LinearLayout) findViewById(R.id.controlContainer);
        thumb_image=(ImageView)findViewById(R.id.thumb_image);
        videoControlView = new VideoControlView(getContext())
                .setPlayView(mVideoView)
                .setPlay_image(play_image)
                .setPause_image(pause_image)
                .setControlViewId(controlViewId)
                .setVolumeDialogId(volumeDialogId)
                .setBrightDialogId(brightDialogId)
                .setProgressDialogId(progressDialogId)
                .setHide_close_btn(hide_close_btn)
                .setFull_Screen(allow_full_window)
                .setVideoControl(this)
                .init();
        controlContainer.addView(videoControlView);
        mViewControl = videoControlView;
        controlContainer.setOnTouchListener(this);
        mVideoView.setOnTouchListener(this);
        mVideoView.setOnClickListener(this);
        initListener();
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mAudioManager = (AudioManager) App.getInstance().getSystemService("audio");//获取音频管理器
    }

    //初始化控件属性
    public void initAttr(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.StrongVideoView);
        controlViewId = a.getResourceId(R.styleable.StrongVideoView_controlView, R.layout.videocontrol_layout);
        volumeDialogId = a.getResourceId(R.styleable.StrongVideoView_volumeDilogView, R.layout.volume_dialog_layout);
        brightDialogId = a.getResourceId(R.styleable.StrongVideoView_brightDilogView, R.layout.bright_dialog_layout);
        progressDialogId = a.getResourceId(R.styleable.StrongVideoView_progressDilogView, R.layout.fastforward_dialog);
        auto_play = a.getBoolean(R.styleable.StrongVideoView_auto_play, false);
        loop_play = a.getBoolean(R.styleable.StrongVideoView_loop_play, false);
        allow_small_window = a.getBoolean(R.styleable.StrongVideoView_allow_small_window, true);
        allow_full_window = a.getBoolean(R.styleable.StrongVideoView_allow_full_window, false);
        play_image = a.getResourceId(R.styleable.StrongVideoView_play_image, R.mipmap.video_play);
        pause_image = a.getResourceId(R.styleable.StrongVideoView_pause_image, R.mipmap.video_pause);
        hide_close_btn = a.getBoolean(R.styleable.StrongVideoView_hide_close_btn, false);
        MaxScaleRate=a.getFloat(R.styleable.StrongVideoView_max_scale_narrow_rate,0.5f);
    }
    //事件监听
    public void initListener() {
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Finish = true;
                if (mViewControl != null) {
                    mViewControl.finish();
                }
                if (loop_play) {
                    startPlay();
                }
                if (videoControl != null) {
                    videoControl.completion();
                }
            }
        });
    }

    //将点击事件交给外部处理
    public void setInnerVideoControl(InnerVideoControl videoControl) {
        this.videoControl = videoControl;
    }

    //设置播放路径
    public void videoPath(String path) {
        videoPath = path;
        if (videoPath != null && videoPath.length() > 0) {
            mVideoView.setVideoPath(videoPath);
            Glide.with(mContext).load(Uri.fromFile(new File(videoPath))).into(thumb_image);
            if (auto_play) {
                startPlay();
            }
        } else {
            Toast.makeText(mContext, "无效路径导致播放失败!", Toast.LENGTH_LONG).show();
        }
    }
    //开始播放视频
    public void startPlay() {
        if (videoPath != null && videoPath.length() > 0) {
            mVideoView.start();
            if (mViewControl != null) {
                mViewControl.initData();
            }
        } else {
            Toast.makeText(mContext, "无效路径导致播放失败!", Toast.LENGTH_LONG).show();
            return;
        }

        if (mViewControl != null) {
            mViewControl.start();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video:
                if (mViewControl != null) {
                    mViewControl.show();
                }
                videoControlView.setVisbleTim();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float y = event.getY(0);
        float x = event.getX(0);
        int dev;//暂时存储当前的声音或者亮度
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                changeBright = false;
                changePosition = false;
                changeVolume = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - downX;
                float deltaY = y - downY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (!changeVolume && !changeVolume && !changeBright && (absDeltaX > 80.0F || absDeltaY > 80.0F)) {
                    videoControlView.cancel1Timer();
                    if (absDeltaX > 80.0f) {//absDeltaX>80.0f代表是左右滑动，控制快进和后退
                        changePosition = true;
                        changeVolume = false;
                        changeBright = false;
                    } else if (downX < (float) (screenWidth * 0.5f)) {//手指在屏幕的左边上下滑动，所以是改变亮度
                        changeBright = true;
                        try {
                            videoControlView.setCurrentBright((float) Settings.System.getInt(getContext().getContentResolver(), "screen_brightness"));
                        } catch (Settings.SettingNotFoundException e) {
                            e.printStackTrace();
                        }

                    } else {//手指在屏幕的右边上下滑动是改变系统的声音
                        changeVolume = true;
                        videoControlView.setCurrentVolume(mAudioManager.getStreamVolume(3));
                    }
                }
                if (changePosition) {
                    if(mVideoView.isPlaying()){
                        mVideoView.pause();
                    }
                    videoControlView.setDeltaX(deltaX);
                    if (mViewControl != null) {
                        mViewControl.changeProgress(0);
                    }
                }
                if (changeBright) {
                    videoControlView.setDeltdY(deltaY);
                    if (mViewControl != null) {//受系统亮度开关的影响，亮度最低只能达到系统默认；
                        mViewControl.changeBright();
                    }
                }
                if (changeVolume) {
                    videoControlView.setDeltdY(deltaY);
                    if (mViewControl != null) {
                        mViewControl.changeVolume();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mViewControl != null) {
                    mViewControl.dismiss();
                }
                if (changePosition) {
                    if (mViewControl != null) {
                        mViewControl.changeProgress(1);
                    }
                }
                if (!Finish) {
                    if (!mVideoView.isPlaying()) {
                        mVideoView.start();
                    }
                }
                break;
        }
        return false;
    }

    //暂停视频
    @Override
    public void pause() {
        mVideoView.pause();
    }

    //播放视频
    @Override
    public void play() {
        if (videoPath != null && videoPath.length() > 0) {
            mVideoView.start();
            if (Finish) {
                if (mViewControl != null) {
                    mViewControl.initData();
                }
                Finish = false;
            }
            if (mViewControl != null) {
                mViewControl.start();
            }
        } else {
            Toast.makeText(mContext, "无效路径导致播放失败!", Toast.LENGTH_LONG).show();
        }

    }

    //关闭视频
    @Override
    public void close() {
        if (videoControl != null) {
            videoControl.close();
        }
    }
    @Override
    public void full_screen() {
        if (videoControl != null) {
            videoControl.fullScreen();
        }
    }

    //打开声音
    @Override
    public void openVolume() {
        volumeState = true;
    }

    //关闭声音
    @Override
    public void closeVolume() {
        volumeState = false;
    }

    //滚动缩屏监听
    @Override
    public void scroll(int scrOllY,int start_pointY,int end_poinY) {
        if(allow_small_window){
            setScaleRate(scrOllY,start_pointY,end_poinY);
        }
    }

    public interface InnerVideoControl {
       default void completion(){};
        void close();
        default void fullScreen(){};
    }

    //清除缓存
    public void clear() {
        mVideoView.suspend();
        root.clearAnimation();
    }
    //设置缩放中心和缩放比例
    public void setScaleAttr(int privoX, int privoY) {
        this.privoX = privoX;//默认0
        this.privoY = privoY;//默认0
    }

    //设置缩放中心和缩放比例
    public void setScaleAttr(int privoX, int privoY, float scaleX, float scaleY) {
        this.privoX = privoX;//默认0
        this.privoY = privoY;//默认0
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    //开启小屏
    public void startSmallScreen() {
        root.setPivotX(privoX);
        root.setPivotX(privoY);
        root.setScaleX(scaleX);
        root.setScaleY(scaleY);
    }

    //开启大屏
    public void satrtFullScreen() {
        root.setPivotX(privoX);
        root.setPivotX(privoY);
        root.setScaleX(scaleX);
        root.setScaleY(scaleY);
    }

    //计算缩放比例
    public void setScaleRate(int scrollY,int start_pointY,int end_pointY) {//scrollY为start_pointY开始缩小,scrollY为end_pointY停止缩小
        root.setPivotX(privoX);
        root.setPivotY(privoY);
        if (scrollY >= start_pointY && scrollY <= end_pointY) {
            if (scrollY - lastY > 0) {
                rate = rate > MaxScaleRate ? 1 - (scrollY - 100) / 255f : MaxScaleRate;
                scaleX = scaleY = rate;
            } else {
                rate = (rate < 1.0f && rate >= MaxScaleRate) ? 1 - (scrollY - 100) / 255f : MaxScaleRate;
                scaleX = scaleY = rate;
            }
            if(rate<MaxScaleRate){
                root.setScaleX(MaxScaleRate);
                root.setScaleY(MaxScaleRate);
            }else{
                root.setScaleX(scaleX);
                root.setScaleY(scaleY);
            }

        } else if (scrollY > end_pointY) {
                root.setScaleX(MaxScaleRate);
                root.setScaleY(MaxScaleRate);
        } else {
            root.setScaleX(1.0f);
            root.setScaleY(1.0f);
        }
        lastY = scrollY;
    }

    public VideoViewScrollListener getScrollListener() {
        return this;
    }
}
