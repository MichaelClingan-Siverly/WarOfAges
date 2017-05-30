package com.example.bakes.login_menu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.ViewGroup;

/* Adapted from SceLus's comment on StackOverflow
 * https://stackoverflow.com/a/22987264
 *
 * modifications made:
 *      Class now extends AppCompatImageView. Because Android Studio wanted me to
 *          I won't be using tint or anything, so it doesn't really matter either way.
 *      Changed calculatePath so that it now creates flat top hexagons
 *      Changed onMeasure to better fit the size of the view to the size of the hexagon
 *      If no layoutParams are set, it defaults to 100 height (will end up being a bit less)
 *          and 100 width
 *      Removed setRadius and setBorderColor
 *
 */

//I extended it from this because it didn't like me extending ImageView. I don't think I'll be using
//tints or anything, but
public class HexagonMaskView extends android.support.v7.widget.AppCompatImageView {
    private Path hexagonPath;
    private Path hexagonBorderPath;
    private Paint mBorderPaint;

    public HexagonMaskView(Context context) {
        super(context);
        this.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        init();
    }

    public HexagonMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HexagonMaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.CENTER_CROP);
        this.hexagonPath = new Path();
        this.hexagonBorderPath = new Path();

        this.mBorderPaint = new Paint();
        this.mBorderPaint.setColor(Color.BLACK);
        this.mBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mBorderPaint.setStrokeWidth(1f);
        this.mBorderPaint.setStyle(Paint.Style.STROKE);
    }

    //Changed it from pointy to flat top hexagons
    private void calculatePath(float radius) {
        float halfRadius = radius / 2f;
        //added height offsets a bit in sort of a hackish attempt to get horizontal lines to show
        float triangleHeight = this.getMeasuredHeight()/2f;
        float centerX = getMeasuredWidth() / 2f;
        float centerY = getMeasuredHeight() / 2f;
        float offset = 1f;

        //draw hexagon which will be filled with the image
        hexagonPath.reset();
        hexagonPath.moveTo(centerX - halfRadius, centerY - triangleHeight + offset);
        hexagonPath.lineTo(centerX + halfRadius, centerY - triangleHeight + offset);
        hexagonPath.lineTo(centerX + radius, centerY);
        hexagonPath.lineTo(centerX + halfRadius, centerY + triangleHeight - offset);
        hexagonPath.lineTo(centerX - halfRadius, centerY + triangleHeight - offset);
        hexagonPath.lineTo(centerX - radius, centerY);
        hexagonPath.close();

        float radiusBorder = radius + .5f;
        float halfRadiusBorder = radiusBorder / 2f;
        float borderOffset = offset/2;

        //adds a small border to the edges of the hexagon
        hexagonBorderPath.reset();
        hexagonBorderPath.moveTo(centerX - halfRadiusBorder, centerY - triangleHeight + borderOffset);
        hexagonBorderPath.lineTo(centerX + halfRadiusBorder, centerY - triangleHeight + borderOffset);
        hexagonBorderPath.lineTo(centerX + radiusBorder, centerY);
        hexagonBorderPath.lineTo(centerX + halfRadiusBorder, centerY + triangleHeight - borderOffset);
        hexagonBorderPath.lineTo(centerX - halfRadiusBorder, centerY + triangleHeight - borderOffset);
        hexagonBorderPath.lineTo(centerX - radiusBorder, centerY);
        hexagonBorderPath.close();
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        c.drawPath(hexagonBorderPath, mBorderPaint);
        c.clipPath(hexagonPath);
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        super.onDraw(c);
    }

    //Since I know the hexagons are vertically aligned (flat-topped), I know r will be related to width
    //additionally, I want to change the height measurement, since I don't want empty space between hexes
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        setMeasuredDimension(width, height);
//        calculatePath(Math.min(width / 2f, height / 2f) - 10f);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //flat topped hexagons are wider than they are taller.
        height = Math.round((float)Math.sqrt(3.0)/2 * height);
        int minHeight = getSuggestedMinimumHeight();
        int minWidth = getSuggestedMinimumWidth();
        //I try to keep the same ratio between height/width
        if(height < minHeight || width < minWidth){
            int y = minHeight / height;
            int x = minWidth / width;
            width = width * Math.max(x,y);
            height = height * Math.max(x,y);
        }
        setMeasuredDimension(width, height);
        calculatePath(width/2f);
    }
}