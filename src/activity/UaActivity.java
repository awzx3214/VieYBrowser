package kawaii.viey.browser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UaActivity extends BaseActivity {
	
	private EditText etCustomUA;
	private LinearLayout layoutPreset;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ua);
		
		etCustomUA = findViewById(R.id.et_custom_ua);
		layoutPreset = findViewById(R.id.layout_preset);
		Button btnSave = findViewById(R.id.btn_save);
		
		
		String currentUA = VieYApp.getUserAgent(this);
		etCustomUA.setText(currentUA);
        
		String[][] p_ua = {
			{"Android", "Mozilla/5.0 (Android) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"},
			{"PC Chrome", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"},
			{"iPhone Safari", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"},
			{"iPad Safari", "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15"},
			{getString(R.string.defaultStr), ""}
		};
		
		for (String[] item : p_ua) {
			TextView tv = new TextView(this);
			tv.setText(item[0]);
			tv.setPadding(32,16,32,16);
			tv.setOnClickListener(v -> {
				etCustomUA.setText(item[1]);
			});
			layoutPreset.addView(tv);
		}
		
		btnSave.setOnClickListener(v -> {
			String ua = etCustomUA.getText().toString().trim();
			VieYApp.setUserAgent(UaActivity.this, ua);
			i.twi(R.string.saved);
			finish();
		});
		
		findViewById(R.id.back_tool).setOnClickListener(v->{
			finish();
		});
		findViewById(R.id.menu_tool).setOnClickListener(v->{
			i.utw(R.string.operation, "test");
		});
		if(isDark()) {
		    i.zs(findViewById(R.id.back_tool), "#ffffff");
		    i.zs(findViewById(R.id.menu_tool), "#ffffff");
		    i.zs(findViewById(R.id.sign_tool), "#ffffff");
		}
	}

}