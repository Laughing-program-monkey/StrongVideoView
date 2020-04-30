package example.strongview;

import android.app.Application;


/**
 * Created by cxf on 2017/8/3.
 */
public class App extends Application {
    public static App sInstance;
    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
    }
    public static App getInstance() {
        return sInstance;
    }
}
