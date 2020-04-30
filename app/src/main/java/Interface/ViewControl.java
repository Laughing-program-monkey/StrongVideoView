package Interface;

public interface ViewControl {
    void initData();//初始化数据
    void start();//开始播放
    void finish();//播放完成监听
    void dismiss();//弹框消失
    void show();//显示控制页面
    void changeVolume();//改变声音
    void changeBright();//改变亮度
    void changeProgress(int state);//改变进度
}
