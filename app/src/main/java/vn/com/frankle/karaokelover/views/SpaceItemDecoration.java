package vn.com.frankle.karaokelover.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by duclm on 7/18/2016.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int space;
    private int direction;

    public SpaceItemDecoration(int space, int direction) {
        this.space = space;
        this.direction = direction;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        switch (direction) {
            case HORIZONTAL:
                outRect.right = space;
                break;
            case VERTICAL:
                outRect.bottom = space;
                break;
            default:
                break;
        }

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0) {
            switch (direction) {
                case HORIZONTAL:
                    outRect.left = space;
                    break;
                case VERTICAL:
                    outRect.top = space;
                    break;
                default:
                    break;
            }
        }
    }
}
