package com.startek.biota.app.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Z215 on 2016/06/03.
 *
 * reference:
 * https://github.com/chiuki/android-square-view
 * http://stackoverflow.com/questions/8981029/simple-way-to-do-dynamic-but-square-layout
 */
public class SquareRelativeLayout extends RelativeLayout
{
    public SquareRelativeLayout(Context context) {
        super(context);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // (1)
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        //super.onMeasure(heightMeasureSpec, heightMeasureSpec);

        // (2)
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        int size = width > height ? height : width;
//        setMeasuredDimension(size, size);

        // (3)
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        int size;
//        if(widthMode == MeasureSpec.EXACTLY && widthSize > 0){
//            size = widthSize;
//        }
//        else if(heightMode == MeasureSpec.EXACTLY && heightSize > 0){
//            size = heightSize;
//        }
//        else{
//            size = widthSize < heightSize ? widthSize : heightSize;
//        }
//
//        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
//        super.onMeasure(finalMeasureSpec, finalMeasureSpec);

        // (4)
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = getMeasuredWidth();
//        int height = getMeasuredHeight();
//        int squareLen = width;
//        if (height > width) {
//            squareLen = height;
//        }
//        super.onMeasure(MeasureSpec.makeMeasureSpec(squareLen, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(squareLen, MeasureSpec.EXACTLY));

        // (5)
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        int widthDesc = MeasureSpec.getMode(widthMeasureSpec);
//        int heightDesc = MeasureSpec.getMode(heightMeasureSpec);
//        int size = 0;
//        if (widthDesc == MeasureSpec.UNSPECIFIED
//                && heightDesc == MeasureSpec.UNSPECIFIED) {
//            size = DP(defaultSize); // Use your own default size, in our case
//            // it's 125dp
//        } else if ((widthDesc == MeasureSpec.UNSPECIFIED || heightDesc == MeasureSpec.UNSPECIFIED)
//                && !(widthDesc == MeasureSpec.UNSPECIFIED && heightDesc == MeasureSpec.UNSPECIFIED)) {
//            //Only one of the dimensions has been specified so we choose the dimension that has a value (in the case of unspecified, the value assigned is 0)
//            size = width > height ? width : height;
//        } else {
//            //In all other cases both dimensions have been specified so we choose the smaller of the two
//            size = width > height ? height : width;
//        }
//        setMeasuredDimension(size, size);
    }
}
