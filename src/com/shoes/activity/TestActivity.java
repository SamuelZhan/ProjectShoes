package com.shoes.activity; 

import com.example.shoes.R;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshListView;
import com.lee.pullrefresh.ui.PullToRefreshScrollView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		PullToRefreshScrollView pullToRefreshScrollView=(PullToRefreshScrollView) findViewById(R.id.test);
		ScrollView scrollView=pullToRefreshScrollView.getRefreshableView();
		
		View v=getLayoutInflater().inflate(R.layout.commodities_layout, null);
		scrollView.addView(v);
		
	}

}
