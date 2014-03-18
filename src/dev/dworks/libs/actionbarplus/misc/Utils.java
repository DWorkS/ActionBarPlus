package dev.dworks.libs.actionbarplus.misc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Toast;
import dev.dworks.libs.actionbarplus.BuildConfig;

public class Utils {

	public Utils() {
	}

	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(Bundle bundle);
	}
	
	public interface OnPickerInteractionListener {
		public void onPickerInteraction(Bundle bundle);
	}
	
    public static void log(String t, String s) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        String topic = "PRACTO-DROID";
        if (t != null) {
            topic = topic + " " + t;
        }
        if (!TextUtils.isEmpty(s)) {
            android.util.Log.w(topic, s);
        }
    }

    public static void log(String s) {
        log(null, s);
    }
    
    /**
     * Interface for components that are internally scrollable left-to-right.
     */
    public static interface HorizontallyScrollable {
        /**
         * Return {@code true} if the component needs to receive right-to-left
         * touch movements.
         *
         * @param origX the raw x coordinate of the initial touch
         * @param origY the raw y coordinate of the initial touch
         */

        public boolean interceptMoveLeft(float origX, float origY);

        /**
         * Return {@code true} if the component needs to receive left-to-right
         * touch movements.
         *
         * @param origX the raw x coordinate of the initial touch
         * @param origY the raw y coordinate of the initial touch
         */
        public boolean interceptMoveRight(float origX, float origY);
    }


    
    /*public static void crossfade(View fadeIn, final View fadeOut, int duration, final AnimatorListenerAdapter listener) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        Log.i("FadeIn", "FadeOut alpha: " + String.valueOf(ViewHelper.getAlpha(fadeOut)));
        ViewHelper.setAlpha(fadeIn, 0f);
        fadeIn.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        ViewPropertyAnimator.animate(fadeIn)
        .alpha(1f)
        .setDuration(duration)
        .setListener(new AnimatorListenerAdapter() {
        	@Override
        	public void onAnimationEnd(Animator animation) {
                if(null != listener)
                listener.onAnimationEnd(animation);
        		super.onAnimationEnd(animation);
        	}
        }).start();
        
        // Zoom content view
        ViewPropertyAnimator.animate(fadeIn)
        .scaleX((float) 1.10)
        .scaleY((float) 1.10)
        .setDuration(duration - 50).start();
             
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        Log.i("FadeOut", "FadeIn alpha: " + String.valueOf(ViewHelper.getAlpha(fadeIn)));
        ViewPropertyAnimator.animate(fadeOut)
        .alpha(0f)
        .setDuration(duration)
        .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                    fadeOut.setVisibility(View.GONE);
                    ViewHelper.setScaleX(fadeOut, 1.0f);
                    ViewHelper.setScaleY(fadeOut, 1.0f);
            }
        }).start();
    }*/

    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
    
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
    public static boolean hasMoreHeap(){
    	return Runtime.getRuntime().maxMemory() > 20971520;
    }
    
    public static String getBooleanValue(boolean bool){
    	return bool ? "1" : "0";
    }
    
    public static String getBooleanStringValue(int value){
    	return value == 1 ? "true" : "false";
    }
    
    public static boolean isNetConnected(Context context){
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            return false;
        }
        return true;
    }

    /**
    * Simple network connection check.
    *
    * @param context
     * @return true if connection is present else false 
    */
    public static boolean checkConnection(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(context, "No Internet", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}