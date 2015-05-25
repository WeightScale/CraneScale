package com.kostya.cranescale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/*
 * Created by Kostya on 16.11.2014.
 */
public class TemperatureProgressBar extends ProgressBar {
    private String text = "";
    private int textColor = Color.WHITE;
    private float textSize = getResources().getDimension(R.dimen.text_nano);
    private final Paint textPaint;
    private final Rect bounds;
    private int mMinus;

    public TemperatureProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint = new Paint();
        if (!isInEditMode()) {
            setAttrs(attrs);
        }
        bounds = new Rect();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public synchronized void updateProgress(int temperature) {

        setProgress(0);
        if(temperature > -40)
            setText(String.valueOf(temperature));
        else
            setText("-");
        setProgress(temperature + mMinus);
        drawableStateChanged();
    }

    private void setText(String text) {
        this.text = text + '°' + 'C';
        postInvalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int x = getWidth() / 2 - bounds.centerX();
        int y = 0;
        y -= bounds.top;
        canvas.drawText(text, x, y, textPaint);
    }

    synchronized void setTextColor(int textColor) {
        textPaint.setColor(textColor);
        postInvalidate();
    }

    private void setAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextProgressBar, 0, 0);
            setText(a.getString(R.styleable.TextProgressBar_text));
            setTextColor(a.getColor(R.styleable.TextProgressBar_textColor, Color.WHITE));
            setTextSize(a.getDimension(R.styleable.TextProgressBar_textSize, getResources().getDimension(R.dimen.text_nano)));
            mMinus = a.getInteger(R.styleable.TextProgressBar_offSet, 40);
            a.recycle();
        }
    }

    synchronized void setTextSize(float textSize) {
        this.textSize = textSize;
        postInvalidate();
    }

    void setMinusOffset(int offset) {
        mMinus = offset;
    }

}
