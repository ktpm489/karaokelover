package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import vn.com.frankle.karaokelover.fragments.KFragmentZingArtist;

/**
 * Created by duclm on 9/21/2016.
 */

public class KFragmentAdapterArtists extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 3;
    private final String PAGE_TITLES[] = {"V-Pop", "US-UK", "K-Pop"};

    private Context mContext;

    public KFragmentAdapterArtists(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;

    }

    @Override
    public Fragment getItem(int position) {
        int artistType;
        switch (position) {
            case 0:
                artistType = KFragmentZingArtist.ARTIST_TYPE.VPOP;
                break;
            case 1:
                artistType = KFragmentZingArtist.ARTIST_TYPE.US_UK;
                break;
            case 2:
                artistType = KFragmentZingArtist.ARTIST_TYPE.K_POP;
                break;
            default:
                artistType = KFragmentZingArtist.ARTIST_TYPE.VPOP;
                break;
        }

        return KFragmentZingArtist.newInstance(artistType);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PAGE_TITLES[position];
    }
}
