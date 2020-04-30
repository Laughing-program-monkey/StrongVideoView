package videoview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Interface.VideoControl;
import Interface.ViewControl;
import customview.VerticalSeekBar;
import example.strongview.App;
import example.strongview.R;
import util.MVUtils;
//将视频的控制页面单独抽出来
public class VideoControlView extends FrameLayout implements ViewControl,SeekBar.OnSeekBarChangeListener,View.OnClickListener {
    private VideoView playView;
    private int ControlViewId;//用户自定义控制页面的id
    private int VolumeDialogId;//用户自定义声音弹框页面的id
    private int BrightDialogId;//用户自定义亮度弹框的id
    private int ProgressDialogId;//用户自定义进度弹框的id
    private Context mContext;
    private LayoutInflater inflater;
    private ImageView play_img;//播放按钮
    private ImageView close_video;//关闭按钮
    private TextView leftProgress;//当前进度
    private TextView totalProgress;//总时间
    private SeekBar progress;//进度条
    private ImageView volume;//静音按钮
    private boolean hide_close_btn;//是否隐藏关闭按钮
    private VideoControl videoControl;
   private int play_image;//播放图标
   private int pause_image;//暂停图标
    private int systemBright;
    private Handler handler;
    private Timer UpdateTimer;
    private ProgressTimer mProgressTimer;
    private VisibleTimer mVisibleTimer;
    private Timer visibleTimer;
    private Dialog BrightDialog;//亮度弹出框
    private Dialog VolumeDialog;//声音弹出框
    private Dialog FastforwardDialog;//快进弹出框
    private VerticalSeekBar seekBar, volumeSeekBar;
    private SeekBar FastSeek;
    private TextView per_text, volumePer_text,Fastper_Text,fast_total_per;
    private AudioManager mAudioManager;
    private int lastVolume;
    private int currentPosition;//拿到手指触摸下去的当前的进度；
    private float currentBright;//获取当前屏幕的亮度
    private int currentVolume;//获取当前系统的音量；
    private boolean volumeState;//当前声音的状态
    private int screenWidth;//拿到屏幕的宽度
    private int screenHeight;//拿到屏幕的高度
    private boolean has_fullScreen=false;//是否有全屏
    private float deltaX;
    private float deltaY;
    private ImageView full_screen;
    private ProgressBar bottomProgreeBar;
    private RelativeLayout control_view;
    public VideoControlView(@NonNull Context context) {
        super(context);
        this.mContext = context;
        inflater = LayoutInflater.from(context);
    }

    public VideoControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoControlView setVideoControl(VideoControl videoControl) {
        this.videoControl = videoControl;
        return  this;
    }
    //获取播放的图标
    public VideoControlView setPlay_image(int play_image) {
        this.play_image = play_image;
        return  this;
    }
   //获取暂停的图标
    public VideoControlView setPause_image(int pause_image) {
        this.pause_image = pause_image;
        return this;
    }
   //获取播放器
    public VideoControlView setPlayView(VideoView playView) {
        this.playView = playView;
        return this;
    }
    //获取用户自定义的控制页面的id
    public VideoControlView setControlViewId(int controlViewId) {
        ControlViewId = controlViewId;
        return  this;
    }
    //获取用户自定义的声音调节弹框id
    public VideoControlView setVolumeDialogId(int volumeDialogId) {
        VolumeDialogId = volumeDialogId;
        return this;
    }
//获取用户自定义的亮度弹框id
    public VideoControlView setBrightDialogId(int brightDialogId) {
        BrightDialogId = brightDialogId;
        return this;
    }
  //获取用户自定义的进度弹框id
    public VideoControlView setProgressDialogId(int progressDialogId) {
        ProgressDialogId = progressDialogId;
        return this;
    }
    //是否隐藏关闭按钮
    public VideoControlView setHide_close_btn(boolean hide_close_btn) {
        this.hide_close_btn = hide_close_btn;
        return this;
    }
    //是否隐藏关闭按钮
    public VideoControlView setFull_Screen(boolean has_fullScreen) {
        this.has_fullScreen = has_fullScreen;
        return this;
    }
    //页面初始化
    public VideoControlView init() {
        View view = inflater.inflate(ControlViewId, this);
        play_img = (ImageView) view.findViewById(R.id.play_img);
        close_video = (ImageView) view.findViewById(R.id.close_video);
        leftProgress = (TextView) view.findViewById(R.id.time1);
        totalProgress = (TextView) view.findViewById(R.id.time2);
        progress = (SeekBar) view.findViewById(R.id.progress);
        volume = (ImageView) view.findViewById(R.id.volume);
        full_screen=(ImageView)view.findViewById(R.id.full_screen);
        bottomProgreeBar=(ProgressBar)view.findViewById(R.id.bottomProgreeBar);
        control_view=(RelativeLayout)view.findViewById(R.id.control_view);
        play_img.setOnClickListener(this);
        close_video.setOnClickListener(this);
        volume.setOnClickListener(this);
        full_screen.setOnClickListener(this);
        progress.setOnSeekBarChangeListener(this);
        handler = new Handler();
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        if(hide_close_btn){
            close_video.setVisibility(INVISIBLE);
        }
        if(has_fullScreen){
            volume.setVisibility(INVISIBLE);
            full_screen.setVisibility(VISIBLE);
        }
        mAudioManager = (AudioManager) App.getInstance().getSystemService("audio");//获取音频管理器
        return this;
    }
    @Override
    //点击事件(注意踩坑，开始是在布局文件里面定义一个onClick属性值，结果测试抛出异常，onClick在自定义view的布局里面谨慎使用，具体情况可以去查看View.class文件)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_video:
                if (videoControl != null) {
                    videoControl.close();
                }
                break;
            case R.id.play_img:
                if (videoControl != null && playView != null) {
                    if (playView.isPlaying()) {
                        videoControl.pause();
                        play_img.setImageResource(pause_image);
                    } else {
                         videoControl.play();
                         play_img.setImageResource(play_image);
                    }
                }
                break;
            case R.id.volume:
                if(!volumeState) {
                    volume.setImageResource(R.mipmap.open_volume);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0);
                    volumeState=true;
                    if(videoControl!=null){
                        videoControl.openVolume();
                    }
                }else{
                    volume.setImageResource(R.mipmap.close_volume);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    volumeState=false;
                    if(videoControl!=null){
                        videoControl.closeVolume();
                    }
                }
                break;
            case R.id.full_screen:
                if(videoControl!=null){
                    videoControl.full_screen();
                }
                break;
        }
    }
   //获取手指滑动的offsetx
    public void setDeltaX(float deltaX) {
        this.deltaX = deltaX;
    }
   //获取手指滑动的offsetY
    public void setDeltdY(float deltdY) {
        this.deltaY = deltdY;
    }
   //拿到当前的声音大小
    public void setCurrentVolume(int currentVolume) {
        this.currentVolume = currentVolume;
    }
    //拿到当前的亮度
    public void setCurrentBright(float currentBright) {
        this.currentBright = currentBright;
    }
    //设置初始化的数据
    public void setInitData() {
        leftProgress.setText("00:00");
        totalProgress.setText(MVUtils.stringForTime(getDuration()));
        //  bottomSeek.setProgress(0);
        progress.setProgress(0);
        bottomProgreeBar.setProgress(0);
        bottomProgreeBar.setSecondaryProgress(0+(5+(new Random().nextInt(10))));
        getSystemBright();
        lastVolume=mAudioManager.getStreamVolume(3);
        play_img.setImageResource(play_image);
    }
    //获取视频时间
    public int getDuration() {
        int duration = 0;
        try {
            duration = playView.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    //显示亮度弹出框
    public void showBrightDialog(int percent) {
        if (BrightDialog == null) {
            BrightDialog = new Dialog(getContext(), R.style.CustomDialog);
            View view = inflater.inflate(BrightDialogId, null);
            seekBar = (VerticalSeekBar) view.findViewById(R.id.seek_per);
            per_text = (TextView) view.findViewById(R.id.per_text);
            BrightDialog.setCancelable(true);
            BrightDialog.setContentView(view);
            Window window = BrightDialog.getWindow();
            window.setGravity(Gravity.RIGHT | Gravity.TOP);
        }
        if (percent >= 100) {
            percent = 100;
        }
        if (percent <= 0) {
            percent = 0;
        }
        seekBar.setProgress(percent);
        per_text.setText("亮度:" + (percent+systemBright) + "%");
        if (!BrightDialog.isShowing()) {
            BrightDialog.show();
        }
        if (VolumeDialog != null) {
            VolumeDialog.dismiss();
        }
        if (FastforwardDialog != null) {
            FastforwardDialog.dismiss();
        }
    }

    //显示声量弹出框
    public void showVolumeDialog(int percent) {
        if (VolumeDialog == null) {
            VolumeDialog = new Dialog(getContext(), R.style.CustomDialog);
            View view = inflater.inflate(VolumeDialogId, null);
            volumeSeekBar = (VerticalSeekBar) view.findViewById(R.id.seek_per);
            volumePer_text = (TextView) view.findViewById(R.id.per_text);
            VolumeDialog.setCancelable(true);
            VolumeDialog.setContentView(view);
            Window window = VolumeDialog.getWindow();
            window.setGravity(Gravity.LEFT | Gravity.TOP);
        }
        if (percent >= 100) {
            percent = 100;
        }
        if (percent <= 0) {
            percent = 0;
        }
        volumeSeekBar.setProgress(percent);
        volumePer_text.setText("音量:" + percent + "%");
        if (!VolumeDialog.isShowing()) {
            VolumeDialog.show();
        }
        if (BrightDialog != null) {
            BrightDialog.dismiss();
        }
        if (FastforwardDialog != null) {
            FastforwardDialog.dismiss();
        }
    }
    //显示快进弹出框
    public void showFastforwardDialog(int seekTimePosition,int totalTime,String seekTime,String totalDuration){
        if(FastforwardDialog==null) {
            FastforwardDialog = new Dialog(getContext(), R.style.CustomDialog);
            View view =inflater.inflate(ProgressDialogId, null);
            FastSeek = (SeekBar) view.findViewById(R.id.fast_seek);
            Fastper_Text = (TextView) view.findViewById(R.id.fast_per);
            fast_total_per = (TextView) view.findViewById(R.id.fast_total_per);
            FastforwardDialog.setContentView(view);
            FastforwardDialog.setCancelable(true);
        }
        Fastper_Text.setText(seekTime);
        fast_total_per.setText("/"+totalDuration);
        FastSeek.setProgress(totalTime<=0?0:(seekTimePosition*100/totalTime));
        if(!FastforwardDialog.isShowing()){
            FastforwardDialog.show();
        }
        if (BrightDialog != null) {
            BrightDialog.dismiss();
        }
        if (VolumeDialog != null) {
            VolumeDialog.dismiss();
        }
    }
   //监听初始化的回调
    @Override
    public void initData() {
        setInitData();
    }
 //监听开始播放的回调
    @Override
    public void start() {
        setTimeTask();
        setVisbleTim();
    }
  //监听播放完成的回调
    @Override
    public void finish() {
        if(control_view.getVisibility()!=VISIBLE) {
            control_view.setVisibility(VISIBLE);
        }
        if(bottomProgreeBar.getVisibility()==VISIBLE){
            bottomProgreeBar.setVisibility(GONE);
        }
        progress.setProgress(100);//解决播放完成之后进度条不到底的问题
        bottomProgreeBar.setProgress(100);
        bottomProgreeBar.setSecondaryProgress(100);
        play_img.setImageResource(pause_image);
        if (BrightDialog != null) {
            BrightDialog.dismiss();
        }
        if (VolumeDialog != null) {
            VolumeDialog.dismiss();
        }
        if(FastforwardDialog!=null){
            FastforwardDialog.dismiss();
        }
        cancel();
    }
    //监听弹框消失的回调
    @Override
    public void dismiss() {
        if (BrightDialog != null) {
            BrightDialog.dismiss();
        }
        if (VolumeDialog != null) {
            VolumeDialog.dismiss();
        }
        if (FastforwardDialog != null) {
            FastforwardDialog.dismiss();
        }
    }

    @Override
    public void show() {
        control_view.setVisibility(VISIBLE);
        bottomProgreeBar.setVisibility(GONE);
    }

    //监听改变声音的回调
    @Override
    public void changeVolume() {
        int dev;
        deltaY = -deltaY;
        //currentVolume = mAudioManager.getStreamVolume(3);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        dev = (int) (max * deltaY * 3 / screenHeight);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + dev, 0);
        lastVolume = currentVolume + dev;
        int volumePercent = (int) (currentVolume * 100 / max + deltaY * 3 * 100 / screenHeight);
        showVolumeDialog(volumePercent);
    }
//监听改变亮度的回调
    @Override
    public void changeBright() {
        deltaY = -deltaY;//亮度的增加方向与deltay数值符号相反
        int deval = (int) (255 * deltaY * 3 / screenHeight);
        WindowManager.LayoutParams params = MVUtils.getAppCompActivity(getContext()).getWindow().getAttributes();
        if ((currentBright + deval) / 255 >= 1) {
            params.screenBrightness = 1;
        } else if (currentBright + deval / 255 <= 0) {
            params.screenBrightness = 0.01f;
        } else {
            params.screenBrightness = (currentBright + deval) / 255.0f;
        }
        MVUtils.getAppCompActivity(getContext()).getWindow().setAttributes(params);
        int bright = (int) (currentBright * 100 / 255 + deltaY * 3 * 100 / screenHeight);
        showBrightDialog(bright);
    }
  //监听改变进度的回调
    @Override
    public void changeProgress(int state) {
        if(state==0) {
            currentPosition = playView.getCurrentPosition();
            if (playView.isPlaying()) {
                playView.pause();
            }
            cancelTimer();
            currentPosition = (int) ((float) currentPosition + deltaX * (float) getDuration() / (float) screenWidth);
            if (currentPosition > getDuration()) {
                currentPosition = getDuration();
            }
            showFastforwardDialog(currentPosition, getDuration(), MVUtils.stringForTime(currentPosition), MVUtils.stringForTime(getDuration()));
        }else{
            playView.seekTo(currentPosition);
            int dev;
            dev = currentPosition * 100 / (getDuration() == 0 ? 1 : getDuration());
            progress.setProgress(dev);
            bottomProgreeBar.setProgress(dev);
            int secondPro=dev+(5+(new Random().nextInt(10)));
            if(secondPro>=100){
                secondPro=100;
            }
            bottomProgreeBar.setSecondaryProgress(secondPro);
            setTimeTask();
            setVisbleTim();
        }
    }

    //更换进度定时器
    public class ProgressTimer extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateProgress();
                }
            });
        }
    }
   //控制页面自动隐藏的定时器
    public class VisibleTimer extends TimerTask {
        @Override
        public void run() {
            ((Activity) VideoControlView.this.getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   control_view.setVisibility(GONE);
                    bottomProgreeBar.setVisibility(VISIBLE);
                }
            });
        }
    }
    //设置进度更新的计时器任务
    public void setTimeTask() {
        cancelTimer();
        UpdateTimer = new Timer();
        mProgressTimer = new ProgressTimer();
        UpdateTimer.schedule(mProgressTimer, 0, 300);
    }

    //添加controlContainer隐藏的倒计时任务
    public void setVisbleTim() {
        cancel1Timer();
        visibleTimer = new Timer();
        mVisibleTimer = new VisibleTimer();
        visibleTimer.schedule(mVisibleTimer, 3500);
    }
    //进度定时器的取消
    public void cancelTimer() {
        if (UpdateTimer != null) {
            UpdateTimer.cancel();
        }
        if (mProgressTimer != null) {
            mProgressTimer.cancel();
        }
    }

    //控制页面状态定时器的取消
    public void cancel1Timer() {
        if (visibleTimer != null) {
            visibleTimer.cancel();
        }
        if (mVisibleTimer != null) {
            mVisibleTimer.cancel();
        }
    }

    //更新进度信息
    public void updateProgress() {
        int current = playView.getCurrentPosition();
        int duration = playView.getDuration();
        int pro = current * 100 / (duration == 0 ? 1 : duration);
        if (pro != 0) {
            progress.setProgress(pro);
            bottomProgreeBar.setProgress(pro);
            int secondPro=pro+(5+(new Random().nextInt(10)));
            if(secondPro>=100){
                secondPro=100;
            }
            bottomProgreeBar.setSecondaryProgress(secondPro);
        }
        if (current != 0) {
            leftProgress.setText(MVUtils.stringForTime(current));
        }
        if (current != 0)
            totalProgress.setText(MVUtils.stringForTime(duration));
    }
    //
    private void cancel() {
        cancel1Timer();
        cancelTimer();
    }
    //获取系统当前的亮度
    public int  getSystemBright(){
        try {
            systemBright = Settings.System.getInt(getContext().getContentResolver(), "screen_brightness");;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBright;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancel();
        for (ViewParent group = this.getParent(); group != null; group = group.getParent()) {
            group.requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        setTimeTask();
        for (ViewParent vpup = this.getParent(); vpup != null; vpup = vpup.getParent()) {
            vpup.requestDisallowInterceptTouchEvent(false);
        }
        int time = seekBar.getProgress() * getDuration() / 100;
        playView.seekTo(time);
        setVisbleTim();
    }
}
