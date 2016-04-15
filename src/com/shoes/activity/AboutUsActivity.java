 
package com.shoes.activity; 

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.shoes.R;

public class AboutUsActivity extends Activity {

	private ImageButton btnBack;
	private Button btnVersion;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about_us);
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnVersion=(Button)findViewById(R.id.about_btn_version);
		btnVersion.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout dialog=(LinearLayout) getLayoutInflater().inflate(R.layout.dialog_version, null);
				new AlertDialog.Builder(AboutUsActivity.this)
				.setView(dialog)
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				
				.create()
				.show();
			}
		});
	}

}
