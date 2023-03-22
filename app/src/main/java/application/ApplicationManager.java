package application;

import android.content.Context;

public class ApplicationManager {
    static public Context getApplicationContext(){
        return MyApplication.getInstance();
    }
}
