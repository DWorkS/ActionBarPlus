package dev.dworks.libs.actionbarplus.app;

import java.lang.reflect.Field;

import android.app.Application;
import android.os.Build;
import android.view.ViewConfiguration;

public class ActionBarApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// show overflow menu post-honeycomb devices with menu button
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
		    try {
		        ViewConfiguration config = ViewConfiguration.get(this);
		        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
		        if(menuKeyField != null) {
		            menuKeyField.setAccessible(true);
		            menuKeyField.setBoolean(config, false);
		        }
		    } catch (Exception ex) {
		        // Ignore
		    }	
		}
	}
}
