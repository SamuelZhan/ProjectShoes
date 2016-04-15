package com.shoes.fragment; 

import com.example.shoes.R;
import com.shoes.activity.LoginActivity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NoLoginFragment extends Fragment {
	
	private TextView tvNoLogin;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView=inflater.inflate(R.layout.fragment_no_login, null);
		
		tvNoLogin=(TextView)rootView.findViewById(R.id.tv_no_login);
		SpannableString spStr=new SpannableString("抱歉，您还未登陆");
		spStr.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout dialog=(LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.dialog_logout, null);
				TextView tvMessage=(TextView) dialog.findViewById(R.id.tv_dialog_message);
				tvMessage.setText("是否跳转到登陆页面");
				new AlertDialog.Builder(getActivity())
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						startActivity(new Intent(getActivity(), LoginActivity.class));
						getActivity().finish();
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
			}
		}, 0, spStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		tvNoLogin.append(spStr);
		tvNoLogin.setMovementMethod(LinkMovementMethod.getInstance());
		
		LinearLayout dialog=(LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.dialog_logout, null);
		TextView tvMessage=(TextView) dialog.findViewById(R.id.tv_dialog_message);
		tvMessage.setText("您还没登陆，是否要登陆");
		new AlertDialog.Builder(getActivity())
		.setView(dialog)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getActivity(), LoginActivity.class));
				getActivity().finish();
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
		
		return rootView;
	}

}
