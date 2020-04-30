package example.strongview;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import Interface.VideoViewScrollListener;
import videoview.StrongVideoView;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements View.OnScrollChangeListener {
    private StrongVideoView video;
    private ScrollView scrollView;
    private int lastY;
    private float rate = 0.5f;
    private int provix;//x轴缩放的坐标
    private int proviy;//y轴缩放的坐标
    private VideoViewScrollListener videoViewScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        video = (StrongVideoView) findViewById(R.id.strongvideoview);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setOnScrollChangeListener(this);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.test;
        video.videoPath(path);
        video.setInnerVideoControl(new StrongVideoView.InnerVideoControl() {
            @Override
            public void completion() {
                //播放完成交由用户处理
                Toast.makeText(MainActivity.this,"播放完成",Toast.LENGTH_LONG).show();
            }

            @Override
            public void close() {
                finish();
            }

            @Override
            public void fullScreen() {
             //点击满屏交由用户处理
                Toast.makeText(MainActivity.this,"满屏功能",Toast.LENGTH_LONG).show();
            }
        });
        videoViewScrollListener = video.getScrollListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        video.clear();
    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        provix = 0;
        proviy = 0;
        video.setScaleAttr(provix, proviy);
        if (videoViewScrollListener != null) {
            videoViewScrollListener.scroll(scrollY, 100, 355);
        }
    }
}
