package kawaii.viey.browser;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

public class EditViey extends EditText {

    public EditViey(Context context) {
        this(context, null);
    }

    public EditViey(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public EditViey(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        setMinHeight(Math.round(56 * getResources().getDisplayMetrics().density));
        setBackgroundResource(R.drawable.edittext_selector);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackgroundTintList(null);
            setStateListAnimator(null);
        }
    }
}