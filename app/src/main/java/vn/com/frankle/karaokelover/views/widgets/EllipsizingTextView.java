package vn.com.frankle.karaokelover.views.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by duclm on 8/2/2016.
 */

public class EllipsizingTextView extends TextView {
    private static final String Ellipsis = "...";

//    public event EllipsizeEvent EllipsizeStateChanged;

    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private String fullText;
    private int maxLines = -1;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding;

    public EllipsizingTextView(Context context) {
        super(context);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isEllipsized() {
        return this.isEllipsized;
    }

    @Override
    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        this.maxLines = maxlines;
        isStale = true;
    }

    @Override
    public int getMaxLines() {
        return this.maxLines;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        lineAdditionalVerticalPadding = add;
        lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (!programmaticChange) {
            fullText = text.toString();
            isStale = true;
        }
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        if (isStale) {
//            this.Ellipsize = null;
//            ResetText();
//        }
//        super.onDraw(canvas);
//    }
//
//    private void ResetText() {
//        int maxLines = getMaxLines();
//        String workingText = fullText;
//        boolean ellipsized = false;
//        if (maxLines != -1) {
//            Layout layout = CreateWorkingLayout(workingText);
//            if (layout.LineCount > maxLines) {
//                workingText = fullText.Substring(0, layout.GetLineEnd(maxLines - 1)).Trim();
//                while (CreateWorkingLayout(workingText + Ellipsis).LineCount > maxLines) {
//                    int lastSpace = workingText.LastIndexOf(' ');
//                    if (lastSpace == -1) {
//                        break;
//                    }
//                    workingText = workingText.Substring(0, lastSpace);
//                }
//                workingText = workingText + Ellipsis;
//                ellipsized = true;
//            }
//        }
//        if (workingText != Text) {
//            programmaticChange = true;
//            try {
//                Text = workingText;
//            } finally {
//                programmaticChange = false;
//            }
//        }
//        isStale = false;
//        if (ellipsized != isEllipsized) {
//            isEllipsized = ellipsized;
//            if (EllipsizeStateChanged != null)
//                EllipsizeStateChanged(ellipsized);
//        }
//    }
//
//    private Layout CreateWorkingLayout(String workingText)
//    {
//        return new StaticLayout(workingText, Paint,  - PaddingLeft - PaddingRight, Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
//    }

}
