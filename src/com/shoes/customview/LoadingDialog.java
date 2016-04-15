package com.shoes.customview; 

import com.example.shoes.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class LoadingDialog extends Dialog {

	private String message;
	
	public LoadingDialog(Context context, String message) {
		// TODO Auto-generated constructor stub
		super(context, R.style.loading_dialog_style);
		this.message=message;
	}
	
	public LoadingDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_loading);
		
		TextView tvMessage=(TextView)findViewById(R.id.loading_dialog_tv);
		tvMessage.setText(message);
	}

}
