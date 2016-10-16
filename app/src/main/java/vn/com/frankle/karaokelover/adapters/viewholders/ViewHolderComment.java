package vn.com.frankle.karaokelover.adapters.viewholders;

import android.content.Context;
import android.text.Html;
import android.text.Layout;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.joda.time.DateTime;

import at.blogc.android.views.ExpandableTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.services.responses.youtube.commentthread.Comment;
import vn.com.frankle.karaokelover.services.responses.youtube.commentthread.CommentThread;
import vn.com.frankle.karaokelover.views.widgets.CircularImageView;

/**
 * Created by duclm on 10/11/2016.
 */

public class ViewHolderComment extends ViewHolderBase<CommentThread> {

    @BindView(R.id.item_comment_author)
    TextView author;
    @BindView(R.id.item_comment_text)
    ExpandableTextView comment;
    @BindView(R.id.item_comment_author_avatar)
    CircularImageView avatar;
    @BindView(R.id.item_comment_published_at)
    TextView publishedAt;
    @BindView(R.id.item_comment_like)
    TextView commentLike;
    @BindView(R.id.item_comment_btn_readmore)
    TextView readmore;
    @BindView(R.id.item_comment_layout_like)
    LinearLayout layoutLike;

    public ViewHolderComment(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Context context, CommentThread dataItem) {
        Comment userComment = dataItem.getThread().getTopLevelComment().getComment();

        author.setText(userComment.getAuthorDisplayName());

        comment.setText(Html.fromHtml(userComment.getTextDisplay()));
        comment.setMovementMethod(LinkMovementMethod.getInstance());
        Layout layout = comment.getLayout();
        if (layout != null) {
            // The TextView has already been laid out
            // We can check whether it's ellipsized immediately
            if (comment.getLineCount() > 5 && !comment.isExpanded()) {
                // Text is ellipsized in re-used view, show 'Expand' button
                readmore.setVisibility(View.VISIBLE);
                readmore.setOnClickListener(v -> comment.toggle());
                comment.setOnExpandListener(new ExpandableTextView.OnExpandListener() {
                    @Override
                    public void onExpand(ExpandableTextView view) {
                        readmore.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCollapse(ExpandableTextView view) {

                    }
                });
                comment.setInterpolator(new DecelerateInterpolator());
            } else {
                readmore.setVisibility(View.GONE);
            }
        } else {
            // The TextView hasn't been laid out, so we need to set an observer
            // The observer fires once layout's done, when we can check the ellipsizing
            ViewTreeObserver vto = comment.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (comment.getLineCount() > 5 && !comment.isExpanded()) {
                        // Text is ellipsized in re-used view, show 'Expand' button
                        readmore.setVisibility(View.VISIBLE);
                        readmore.setOnClickListener(v -> comment.toggle());
                        comment.setOnExpandListener(new ExpandableTextView.OnExpandListener() {
                            @Override
                            public void onExpand(ExpandableTextView view) {
                                readmore.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCollapse(ExpandableTextView view) {

                            }
                        });
                        comment.setInterpolator(new DecelerateInterpolator());
                    } else {
                        readmore.setVisibility(View.GONE);
                    }
                    // Remove the now unnecessary observer
                    // It wouldn't fire again for reused views anyways
                    ViewTreeObserver obs = comment.getViewTreeObserver();
                    obs.removeOnGlobalLayoutListener(this);
                }
            });
        }

        publishedAt.setText(DateUtils.getRelativeTimeSpanString(new DateTime(userComment.getPublishedAt()).toDate().getTime(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString().toUpperCase());
        if (userComment.getLikeCount() <= 0) {
            layoutLike.setVisibility(View.GONE);
        } else {
            layoutLike.setVisibility(View.VISIBLE);
            commentLike.setText(String.valueOf(userComment.getLikeCount()));
        }

        Glide.with(context).load(userComment.getAuthorProfileImageUrl())
                .placeholder(R.drawable.drawable_avatar_placeholder).into(avatar);
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE.DATA_ITEM;
    }
}
