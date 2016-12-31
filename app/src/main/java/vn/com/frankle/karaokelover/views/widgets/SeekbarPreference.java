package vn.com.frankle.karaokelover.views.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;

/**
 * Created by duclm on 10/21/2016.
 */

public class SeekbarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

    private static final String DEBUG_TAG = SeekbarPreference.class.getSimpleName();

    // Namespaces to read attributes
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    // Attribute names
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    private static final String ATTR_MIN_VALUE = "kl_minValue";
    private static final String ATTR_MAX_VALUE = "kl_maxValue";
    private static final String ATTR_UNIT = "kl_unit";

    // Default values for defaults
    private static final int DEFAULT_CURRENT_VALUE = 40;
    private static final int DEFAULT_MIN_VALUE = 20;
    private static final int DEFAULT_MAX_VALUE = 70;
    private static final int DEFAULT_INTERVAL = 5;
    private static final String DEFAULT_UNIT = "%";

    // Real defaults
    private final int mDefaultValue;
    private final int mMaxValue;
    private final int mMinValue;
    private final int mInterval;
    private final String mUnit;

    @BindView(R.id.seekbar_prefs_current)
    TextView mTvCurrentValue;
    @BindView(R.id.seekbar_prefs_seekbar)
    SeekBar mSeekbar;

    private int mCurrentValue;

    public SeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Read parameters from attributes
        mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SeekbarPreference, 0, 0);
        mMinValue = typedArray.getInt(R.styleable.SeekbarPreference_kl_minValue, DEFAULT_MIN_VALUE);
        mMaxValue = typedArray.getInt(R.styleable.SeekbarPreference_kl_maxValue, DEFAULT_MAX_VALUE);
        mInterval = typedArray.getInt(R.styleable.SeekbarPreference_kl_interval, DEFAULT_INTERVAL);
        String unit = typedArray.getString(R.styleable.SeekbarPreference_kl_unit);
        mUnit = unit == null ? DEFAULT_UNIT : unit;

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_CURRENT_VALUE);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.widget_preference_seekbar, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        ButterKnife.bind(this, view);

        mCurrentValue = getPersistedInt(DEFAULT_CURRENT_VALUE);
        Log.d(DEBUG_TAG, "Current value: " + mCurrentValue);

        mSeekbar.setMax((mMaxValue - mMinValue) / mInterval);
        mSeekbar.incrementProgressBy(mInterval);
        mSeekbar.setProgress((mCurrentValue - mMinValue) / mInterval);
        mSeekbar.setOnSeekBarChangeListener(this);

        String currentValue = String.valueOf(mCurrentValue) + mUnit;
        mTvCurrentValue.setText(currentValue);

        setNegativeButtonText("Hủy");
        setPositiveButtonText("Đồng Ý");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String newCurrentValue = String.valueOf(mMinValue + progress * mInterval) + mUnit;
        mTvCurrentValue.setText(newCurrentValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public CharSequence getSummary() {
        return getPersistedInt(mDefaultValue) + mUnit;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);


        if (positiveResult) {
            int currentValue = mSeekbar.getProgress() * mInterval + mMinValue;
            persistInt(currentValue);

            notifyChanged();
        }
    }
}
