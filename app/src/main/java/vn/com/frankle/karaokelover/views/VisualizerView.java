package vn.com.frankle.karaokelover.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duclm on 23-Nov-16.
 */

public class VisualizerView extends View {
    private static final int LINE_WIDTH = 1; // Visualizer line width
    private static final int LINE_SCALE = 75; // Scale visualizer line
    private List<Float> amplitudes; // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private Paint linePaint; // specifies line drawing characteristics

    public VisualizerView(Context context) {
        super(context);
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setAlpha(85);
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        amplitudes = new ArrayList<Float>(width / LINE_WIDTH);
    }

    // clear all amplitudes to prepare for a new visualization
    public void clear() {
        amplitudes.clear();
    }

    // add the given amplitude to the amplitudes ArrayList
    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * LINE_WIDTH >= width) {
            amplitudes.remove(0); // remove oldest power value
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int middle = height / 2; // get the middle of the View
        float curX = 0; // start curX at zero

        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {
            float scaledHeight = power / LINE_SCALE; // scale the power
            curX += LINE_WIDTH; // increase X by LINE_WIDTH

            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                    - scaledHeight / 2, linePaint);
        }
    }
}
