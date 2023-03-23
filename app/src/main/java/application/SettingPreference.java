package application;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingPreference {
    private static final String SP_NAME = "newbie02030301_SP";
    private static final String SAMPLE = "Photo";
    private static final String SAMPLE2 = "count";
    private static SettingPreference s_singleton;
    private SharedPreferences m_sharePref;

    public static SettingPreference getInstance(){
        if (s_singleton == null){
            s_singleton = new SettingPreference(ApplicationManager.getApplicationContext());
        }
        return s_singleton;
    }

    private SettingPreference( Context context) {
        m_sharePref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public String getSample() {
        return m_sharePref.getString( SAMPLE, "[]");
    }

    public void setSample(String value) {
        m_sharePref.edit().putString(SAMPLE, value ).apply();
    }


    public int getSample2() {
        return m_sharePref.getInt( SAMPLE2, 0);
    }

    public void setSample2(int value) {
        m_sharePref.edit().putInt(SAMPLE2, value ).apply();
    }
}