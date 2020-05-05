package edu.cmu.pocketsphinx.demo;

import android.app.Application;

public class AtsApplication extends Application
{
    // Singleton
    private static AtsApplication sApplication;
    
    public static AtsApplication getApplication()
    {
        return sApplication;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        sApplication = this;
    }
}
