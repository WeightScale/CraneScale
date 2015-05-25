package com.kostya.cranescale;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/*
 * Created by Kostya on 16.11.2014.
 */
public class BatteryProgressBar extends ProgressBar {
    private String text = "";
    private int textColor = Color.BLACK;
    private float textSize = getResources().getDimension(R.dimen.text_nano);
    private final Paint textPaint;
    private final Rect bounds;

    public BatteryProgressBar(Context context) {
        super(context);
        textPaint = new Paint();
        bounds = new Rect();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
    }

    public BatteryProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint = new Paint();
        bounds = new Rect();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public synchronized void updateProgress(int progress) {

        setProgress(0);

        if (progress > 15) {
            if (progress < 50) {
                setTextColor(Color.BLACK);
            } else {
                setTextColor(Color.WHITE);
            }
            setProgressDrawable(getResources().getDrawable(R.drawable.battery));
        } else if(progress > 0) {
            setTextColor(Color.BLACK);
            setProgressDrawable(getResources().getDrawable(R.drawable.battery_discharged));
        } else {
            setTextColor(getResources().getColor(R.color.transparent));
        }
        setText(String.valueOf(progress));
        setProgress(progress);
        drawableStateChanged();
    }

    private void setText(String text) {
        this.text = text + '%';
        postInvalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int x = getWidth() / 2 - bounds.centerX();
        int y = getHeight() / 2 - bounds.centerY();
        canvas.drawText(text, x, y, textPaint);
    }

    public synchronized void setTextColor(int textColor) {
        textPaint.setColor(textColor);
        postInvalidate();
    }

    public synchronized void setTextSize(float textSize) {
        this.textSize = textSize;
        postInvalidate();
    }

}
