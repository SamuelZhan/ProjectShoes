package com.shoes.customview;

import com.example.shoes.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;

public class RoundCornerListView extends ListView {

	public RoundCornerListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public RoundCornerListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public RoundCornerListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent motionEvent){
		switch (motionEvent.getAction()){
		case MotionEvent.ACTION_DOWN:
			int x=(int)motionEvent.getX();
			int y=(int)motionEvent.getY();
			int itemNo=pointToPosition(x, y);
			if(itemNo==AdapterView.INVALID_POSITION){
				break;
			}else{
				if(itemNo==0){
					if(itemNo==(getAdapter().getCount()-1)){
						setSelector(R.drawable.shape_list_top_bottom);
					}else{
						setSelector(R.drawable.shape_list_top);
					}
				}else if(itemNo==(getAdapter().getCount()-1)){
					setSelector(R.drawable.shape_list_bottom);
				}else{
					setSelector(R.drawable.shape_list_middle);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			break;
		}		
		return super.onInterceptTouchEvent(motionEvent);
		
	}
	
	
	
}
