package dev.dworks.libs.actionbarplus.app;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.ActionBarPolicy;
import android.view.KeyEvent;

public class ActionBarActivityPlus extends ActionBarActivity {

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (ActionBarPolicy.get(this).showsOverflowMenuButton() && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if (event.getAction() == KeyEvent.ACTION_UP
					&& keyCode == KeyEvent.KEYCODE_MENU) {
				openOptionsMenu();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
}
