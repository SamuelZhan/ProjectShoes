package com.shoes.customview; 

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ScaleableImageView extends ImageView {
	
	private PointF startPoint;
	private PointF middlePoint;
	private Matrix matrix;
	private Matrix currentMatrix;
	
	private int mode=0;
	private static final int DRAG=1;
	private static final int ZOOM=2;
	
	private float startDistance=0;

	public ScaleableImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public ScaleableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public ScaleableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init(){
		startPoint=new PointF();
		matrix=new Matrix();
		currentMatrix=new Matrix();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mode=DRAG;
			currentMatrix.set(this.getImageMatrix());
			startPoint.set(event.getX(), event.getY());
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			mode=ZOOM;
			startDistance=getDistance(event);
			if(startDistance>10f){
				middlePoint=getMiddlePoint(event);
				currentMatrix.set(this.getImageMatrix());
			}
			
			break;

		case MotionEvent.ACTION_MOVE:
			
			if(mode==DRAG){
				float dx=event.getX()-startPoint.x;
				float dy=event.getY()-startPoint.y;
				matrix.set(currentMatrix);
				setScaleType(ScaleType.MATRIX);
				matrix.postTranslate(dx, dy);
			}else if(mode==ZOOM){
				float endDistance=getDistance(event);
				if(endDistance>10f){
					float scale=endDistance/startDistance;
					matrix.set(currentMatrix);
					setScaleType(ScaleType.MATRIX);
					matrix.postScale(scale, scale, middlePoint.x, middlePoint.y);
				}
			}
			break;
			
		case MotionEvent.ACTION_POINTER_UP:
			mode=0;
			break;
			
		case MotionEvent.ACTION_UP:
			mode=0;
			break;
		}
		this.setImageMatrix(matrix);
		
		return true;
	}
	
	private float getDistance(MotionEvent event){
		float dx=event.getX(1)-event.getX(0);
		float dy=event.getY(1)-event.getY(0);
		return (float) Math.sqrt(dx*dx+dy*dy);
	}
	
	private PointF getMiddlePoint(MotionEvent event){
		float midx=event.getX(1)+event.getX(0);
		float midy=event.getY(1)+event.getY(0);
		return new PointF(midx/2, midy/2);
		
	}

}
