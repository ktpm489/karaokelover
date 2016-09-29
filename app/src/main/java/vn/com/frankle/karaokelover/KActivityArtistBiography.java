package vn.com.frankle.karaokelover;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtistDetail;
import vn.com.frankle.karaokelover.views.widgets.BottomSheet;

import static vn.com.frankle.karaokelover.util.AnimUtils.getLinearOutSlowInInterpolator;

public class KActivityArtistBiography extends AppCompatActivity {
    private static final String DEBUG_TAG = KActivityArtistBiography.class.getSimpleName();

    private static final int MODE_ARTIST_BIOGRAPHY = 1;
    private static final int DISMISS_DOWN = 0;
    private static final int DISMISS_CLOSE = 1;
    private static final String EXTRA_MODE = "EXTRA_MODE";
    private static final String EXTRA_ARTIST_BIO = "EXTRA_ARTIST_BIO";

    @BindView(R.id.bottom_sheet)
    BottomSheet mBottomSheet;
    @BindView(R.id.sheet_title)
    TextView mSheetTitle;
    @BindView(R.id.close)
    ImageView close;
    @BindView(R.id.tv_artist_biography_full)
    TextView mTvArtistBio;
    @BindView(R.id.bottom_sheet_nestedscrollview)
    NestedScrollView mNestedScrollView;
    @BindView(R.id.title_bar)
    ViewGroup mTitleBar;
    @BindView(R.id.tv_birthname)
    TextView mTvBirthname;
    @BindView(R.id.tv_birthday)
    TextView mTvBirthday;
    @BindView(R.id.tv_nationality)
    TextView mTvNationality;

    private ZingArtistDetail mArtistInfo;
    private int dismissState = DISMISS_DOWN;
    private NestedScrollView.OnScrollChangeListener titleElevation = new NestedScrollView.OnScrollChangeListener() {
        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            final boolean raiseTitleBar = scrollY > 0;
            mTitleBar.setActivated(raiseTitleBar); // animated via a StateListAnimator
        }
    };

    public static void start(Activity launching, ZingArtistDetail artistInfo) {
        Intent starter = new Intent(launching, KActivityArtistBiography.class);
        starter.putExtra(EXTRA_MODE, MODE_ARTIST_BIOGRAPHY);
        starter.putExtra(EXTRA_ARTIST_BIO, artistInfo);
        launching.startActivity(starter,
                ActivityOptions.makeSceneTransitionAnimation(launching).toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_artist_biography);

        ButterKnife.bind(this);

        final Intent intent = getIntent();
        final @ContentSheetMode int mode = intent.getIntExtra(EXTRA_MODE, -1);

        switch (mode) {
            case MODE_ARTIST_BIOGRAPHY:
                Log.d("duclm", "PlayerSheet: MODE_SHOT_LIKES");
                mArtistInfo = intent.getParcelableExtra(EXTRA_ARTIST_BIO);
                mTvArtistBio.setText(mArtistInfo.getBiography());
                mSheetTitle.setText(mArtistInfo.getName());
                mTvBirthname.setText("Tên thật: " + mArtistInfo.getBirthname());
                mTvBirthday.setText("Ngày sinh: " + mArtistInfo.getBirthday());
                mTvNationality.setText("Quốc tịch: " + mArtistInfo.getNationalName());
                break;
            default:
                throw new IllegalArgumentException("Unknown launch mode.");
        }

        mBottomSheet.registerCallback(new BottomSheet.Callbacks() {
            @Override
            public void onSheetDismissed() {
                finishAfterTransition();
            }

            @Override
            public void onSheetPositionChanged(int sheetTop, boolean interacted) {
                if (interacted && close.getVisibility() != View.VISIBLE) {
                    close.setVisibility(View.VISIBLE);
                    close.setAlpha(0f);
                    close.animate()
                            .alpha(1f)
                            .setDuration(400L)
                            .setInterpolator(getLinearOutSlowInInterpolator(KActivityArtistBiography.this))
                            .start();
                }
                if (sheetTop == 0) {
                    showClose();
                } else {
                    showDown();
                }
            }
        });

        mNestedScrollView.setOnScrollChangeListener(titleElevation);
    }

    private void showClose() {
        if (dismissState == DISMISS_CLOSE) return;
        dismissState = DISMISS_CLOSE;
        final AnimatedVectorDrawable downToClose = (AnimatedVectorDrawable)
                ContextCompat.getDrawable(this, R.drawable.avd_down_to_close);
        close.setImageDrawable(downToClose);
        downToClose.start();
    }

    private void showDown() {
        if (dismissState == DISMISS_DOWN) return;
        dismissState = DISMISS_DOWN;
        final AnimatedVectorDrawable closeToDown = (AnimatedVectorDrawable)
                ContextCompat.getDrawable(this, R.drawable.avd_close_to_down);
        close.setImageDrawable(closeToDown);
        closeToDown.start();
    }

    @OnClick({R.id.bottom_sheet, R.id.close})
    public void dismiss(View view) {
        if (view.getVisibility() != View.VISIBLE) return;
        mBottomSheet.dismiss();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            MODE_ARTIST_BIOGRAPHY
    })
    @interface ContentSheetMode {
    }


}
