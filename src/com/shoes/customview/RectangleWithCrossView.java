package com.shoes.customview; 

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class RectangleWithCrossView extends View {
	
	private Paint paint;

	public RectangleWithCrossView(Context context) {		
		// TODO Auto-generated constructor stub
		this(context, null);
	}

	public RectangleWithCrossView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		paint=new Paint();
	}

	public RectangleWithCrossView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		paint.setColor(Color.GRAY);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Style.STROKE);
		PathEffect effect=new DashPathEffect(new float[]{dp2px(4), dp2px(6)}, dp2px(1));
		paint.setPathEffect(effect);
		paint.setStrokeWidth(dp2px(2));
		canvas.drawRect(dp2px(4), dp2px(4), getWidth()-dp2px(4), getHeight()-dp2px(4), paint);
		
		paint.setPathEffect(null);		
		canvas.drawLine(getWidth()/2, getHeight()/5, getWidth()/2, getHeight()/5*4, paint);
		canvas.drawLine(getWidth()/5, getHeight()/2, getWidth()/5*4, getHeight()/2, paint);
	}
	
	private float dp2px(float value){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}
	
	

}
