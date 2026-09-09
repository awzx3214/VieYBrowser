package kawaii.viey.browser;

import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.*;

public class BaseActivity extends AppCompatActivity {
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(VieYApp.applyLanguage(newBase));
	}
	
	public boolean isDark(){
		return VieYApp.isDarkMode(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		i.m(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CrashHandler.getInstance().init(this);
		i.m(this);
		boolean isDark = VieYApp.isDarkMode(this);
		if(isDark){
			setTheme(R.style.AppTheme_Dark);
		}
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		super.onCreate(savedInstanceState);
	}
}