package application;

import android.app.Application;

public class MyApplication extends Application
{
    private static Application m_thiz;

    public static Application getInstance() {
        return m_thiz;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_thiz = this;
    }
}
