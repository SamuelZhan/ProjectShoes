package com.shoes.activity; 

import com.example.shoes.R;
import com.shoes.customview.MatrixImageView;
import com.shoes.customview.ScaleableImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PictureActivity extends Activity{

	private ScaleableImageView picture;
	private ImageView thumbnail;
	private RelativeLayout thumbnailLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
		thumbnailLayout=(RelativeLayout)findViewById(R.id.thumbnail_layout);
		
		thumbnail=(ImageView)findViewById(R.id.thumbnail_image);
		Picasso.with(this).load(getIntent().getStringExtra("thumbnailUrl")).into(thumbnail);
		
		picture=(ScaleableImageView)findViewById(R.id.picture_show);
		
		Picasso.with(this).load(getIntent().getStringExtra("imageUrl")).into(picture, new OnFinishLoadingListener());
		
	}

	private class OnFinishLoadingListener implements Callback{

		@Override
		public void onError() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess() {
			// TODO Auto-generated method stub
			thumbnailLayout.setVisibility(View.GONE);
		}
		
	}
	
}
