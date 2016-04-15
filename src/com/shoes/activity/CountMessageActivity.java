 
package com.shoes.activity; 

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.RelativeLayout;
import com.example.shoes.R;

public class CountMessageActivity extends Activity {

	private ImageButton btnBack;
	private RelativeLayout btnAge, btnHeight, btnWeight, btnStepLength;
	private TextView tvAge, tvHeight, tvWeight, tvStepLength;
	private MyOnClickListener listener;
	
	private int age, height, weight, stepLength;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_count_message);
		
		SharedPreferences preferences=getSharedPreferences("sport_data", Context.MODE_PRIVATE);
		age=preferences.getInt("age", 24);
		height=preferences.getInt("height", 169);
		weight=preferences.getInt("weight", 65);
		stepLength=preferences.getInt("stepLength", 50);
		
		listener=new MyOnClickListener();
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(listener);

		btnAge=(RelativeLayout)findViewById(R.id.btn_age);
		btnAge.setOnClickListener(listener);
		
		btnHeight=(RelativeLayout)findViewById(R.id.btn_height);
		btnHeight.setOnClickListener(listener);
		
		btnWeight=(RelativeLayout)findViewById(R.id.btn_weight);
		btnWeight.setOnClickListener(listener);
		
		btnStepLength=(RelativeLayout)findViewById(R.id.btn_step_length);
		btnStepLength.setOnClickListener(listener);

		tvAge=(TextView)findViewById(R.id.tv_age);
		tvAge.setText(age+"岁");
		
		tvHeight=(TextView)findViewById(R.id.tv_height);
		tvHeight.setText(height+"cm");
		
		tvWeight=(TextView)findViewById(R.id.tv_weight);
		tvWeight.setText(weight+"kg");
		
		tvStepLength=(TextView)findViewById(R.id.tv_step_length);
		tvStepLength.setText(stepLength+"cm");
		
	}

	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			
			case R.id.btn_age:
				LinearLayout dialog2=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_age, null);
				final NumberPicker np1=(NumberPicker)dialog2.findViewById(R.id.dialog_age_number_picker);
				np1.setMaxValue(100);
				np1.setMinValue(0);
				np1.setValue(age);
				new AlertDialog.Builder(CountMessageActivity.this)
				.setView(dialog2)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						age=np1.getValue();
						getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putInt("age", age).commit();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
				break;
			case R.id.btn_height:
				LinearLayout dialog3=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_height, null);
				final NumberPicker np2=(NumberPicker)dialog3.findViewById(R.id.dialog_height_number_picker);
				np2.setMaxValue(250);
				np2.setMinValue(0);
				np2.setValue(height);
				new AlertDialog.Builder(CountMessageActivity.this)
				.setView(dialog3)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						height=np2.getValue();
						getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putInt("height", height).commit();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
				break;
			case R.id.btn_weight:
				LinearLayout dialog4=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_weight, null);
				final NumberPicker np3=(NumberPicker)dialog4.findViewById(R.id.dialog_weight_number_picker);
				np3.setMaxValue(100);
				np3.setMinValue(0);
				np3.setValue(weight);
				new AlertDialog.Builder(CountMessageActivity.this)
				.setView(dialog4)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						weight=np3.getValue();
						getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putInt("weight", weight).commit();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
				break;
			case R.id.btn_step_length:
				LinearLayout dialog5=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_step_length, null);
				final NumberPicker np4=(NumberPicker)dialog5.findViewById(R.id.dialog_step_length_number_picker);
				np4.setMaxValue(120);
				np4.setMinValue(0);
				np4.setValue(stepLength);
				new AlertDialog.Builder(CountMessageActivity.this)
				.setView(dialog5)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						stepLength=np4.getValue();
						getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putInt("stepLength", stepLength).commit();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
				break;
			}
		}
		
	}
	
}
