package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import vn.com.frankle.karaokelover.Utils;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;
import vn.com.frankle.karaokelover.views.FragmentHotKaraoke;

/**
 * Created by duclm on 7/16/2016.
 */

public class KPagerAdapterHotKaraokeSong extends FragmentPagerAdapter {
    private static final int MAX_HOT_KARAOKE = 5;

    private Context mContext;
    private List<ResponseSnippetStatistics> mListHotTrendKaraokes;

    public KPagerAdapterHotKaraokeSong(FragmentManager fragmentManager, List<ResponseSnippetStatistics> listHotTrendKaraokes) {
        super(fragmentManager);
        this.mListHotTrendKaraokes = listHotTrendKaraokes;
    }


    @Override
    public Fragment getItem(int position) {
        ResponseSnippetStatistics karaoke = mListHotTrendKaraokes.get(position);
        Bundle args = new Bundle();
        args.putString("title", karaoke.getItems().get(0).getSnippet().getTitle());
        args.putString("play_count", karaoke.getItems().get(0).getStatistics().getViewCount());
        args.putString("like_count", karaoke.getItems().get(0).getStatistics().getLikeCount());
        args.putString("thumbnail", Utils.getThumbnailURL(karaoke.getItems().get(0).getSnippet().getThumbnails()));
        FragmentHotKaraoke fragmentItem = new FragmentHotKaraoke();
        fragmentItem.setArguments(args);
        return fragmentItem;
    }

    @Override
    public int getCount() {
        return MAX_HOT_KARAOKE;
    }
}
