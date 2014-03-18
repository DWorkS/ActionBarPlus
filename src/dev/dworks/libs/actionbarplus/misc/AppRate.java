package dev.dworks.libs.actionbarplus.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import dev.dworks.libs.actionbarplus.R;

/**
 * Created by nicolas on 06/03/14.
 */
public class AppRate {

    private static final String PREFS_NAME = "app_rate_prefs";
    private final String KEY_COUNT = "count";
    private final String KEY_CLICKED = "clicked";
    private Activity activity;
    private String text;
    private int initialLaunchCount = 5;
    private RetryPolicy policy = RetryPolicy.EXPONENTIAL;
    private OnShowListener onShowListener;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private int delay = 0;

    private AppRate(Activity activity) {
        this.activity = activity;
    }

    public static AppRate with(Activity activity) {
        AppRate instance = new AppRate(activity);
        instance.text = "Like App? Rate It!";//activity.getString(R.string.dra_rate_app);
        instance.settings = activity.getSharedPreferences(PREFS_NAME, 0);
        instance.editor = instance.settings.edit();
        return instance;
    }


    /**
     * Text to be displayed in the view
     *
     * @param text text to be displayed
     * @return the {@link AppRate} instance
     */
    public AppRate text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Text to be displayed in the view
     *
     * @param textRes text ressource to be displayed
     * @return the {@link AppRate} instance
     */
    public AppRate text(int textRes) {
        this.text = activity.getString(textRes);
        return this;
    }

    /**
     * Initial times {@link AppRate} has to be called before the view is shown
     *
     * @param initialLaunchCount times count
     * @return the {@link AppRate} instance
     */
    public AppRate initialLaunchCount(int initialLaunchCount) {
        this.initialLaunchCount = initialLaunchCount;
        return this;
    }

    /**
     * Policy to use to show the {@link AppRate} again
     *
     * @param policy the {@link RetryPolicy} to be used
     * @return the {@link AppRate} instance
     */
    public AppRate retryPolicy(RetryPolicy policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Listener used to get {@link AppRate} lifecycle
     *
     * @param onShowListener the listener
     * @return the {@link AppRate} instance
     */
    public AppRate listener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    /**
     * Delay the {@link AppRate showing time}
     * @param delay the delay in ms
     * @return the {@link AppRate} instance
     */
    public AppRate delay(int delay) {
        this.delay = delay;
        return this;
    }


    /**
     * Check and show if showing the view is needed
     */
    public void checkAndShow() {
        incrementViews();

        boolean clicked = settings.getBoolean(KEY_CLICKED, false);
        if (clicked) return;
        int count = settings.getInt(KEY_COUNT, 0);
        if (count == initialLaunchCount) {
            showAppRate();
        } else if (policy == RetryPolicy.INCREMENTAL && count % initialLaunchCount == 0) {
            showAppRate();
        }else if (policy == RetryPolicy.EXPONENTIAL && count % initialLaunchCount == 0 && isPowerOfTwo(count / initialLaunchCount)) {
            showAppRate();
        }
    }

    /**
     * Reset the count to start over
     */
    public void reset() {
        editor.putInt(KEY_COUNT, 0);
        editor.commit();
    }

    /**
     * Will force the {@link AppRate} to show
     */
    public void forceShow() {
        showAppRate();
    }

    private void incrementViews() {

        editor.putInt(KEY_COUNT, settings.getInt(KEY_COUNT, 0) + 1);
        editor.commit();
    }

    private void showAppRate() {
        final ViewGroup mainView = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.app_rate, null);

        ImageView close = (ImageView) mainView.findViewById(R.id.close);
        TextView textView = (TextView) mainView.findViewById(R.id.text);

        textView.setText(text);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllViews(mainView);
                if (onShowListener != null)onShowListener.onRateAppDismissed();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
                if (onShowListener != null)onShowListener.onRateAppClicked();
                hideAllViews(mainView);
                editor.putBoolean(KEY_CLICKED, true);
                editor.commit();

            }
        });


        if (delay > 0) {
            activity.getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayViews(mainView);
                }
            }, delay);
        } else {
            displayViews(mainView);
        }

    }

    private void hideAllViews(final ViewGroup mainView) {
        Animation hideAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainView.removeAllViews();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mainView.startAnimation(hideAnimation);
    }

    private void displayViews(ViewGroup mainView) {
    	LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        activity.addContentView(mainView, params);


        Animation fadeInAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in);
        mainView.startAnimation(fadeInAnimation);

        if (onShowListener != null) onShowListener.onRateAppShowing();
    }

    public interface OnShowListener {
        void onRateAppShowing();

        void onRateAppDismissed();

        void onRateAppClicked();
    }
    
    public enum  RetryPolicy {
        /**
         * Will retry each time initial count has been triggered
         * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 9th, ... times
         */
        INCREMENTAL,
        /**
         * Will retry exponentially to be less intrusive
         * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 12th, ... times
         */
        EXPONENTIAL,
        /**
         * Will never retry
         */
        NONE;
    }
    
    /**
     * Convert a size in dp to a size in pixels
     * @param context the {@link android.content.Context} to be used
     * @param dpi size in dp
     * @return the size in pixels
     */
    public static int convertDPItoPixels(Context context, int dpi) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpi * scale + 0.5f);
    }

    public static boolean isPowerOfTwo(int x)    {
        return (x & (x - 1)) == 0;
    }
}