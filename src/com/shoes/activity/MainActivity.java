
package com.shoes.activity; 

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.example.shoes.R;
import com.shoes.fragment.CommunityFragment;
import com.shoes.fragment.HomeFragment;
import com.shoes.fragment.MineFragment;
import com.shoes.fragment.NoLoginFragment;
import com.shoes.fragment.ShopFragment;
import com.shoes.service.BleService;

public class MainActivity extends Activity {

	private RadioGroup tabMenu;
	private HomeFragment homeFragment;
	private CommunityFragment communityFragment;
	private ShopFragment shopFragment;
	private MineFragment mineFragment;
	private NoLoginFragment noLoginFragment;
	private long backFirstTime;
	private Fragment from;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		Intent intent=new Intent(this, BleService.class);
		startService(intent);
		
		homeFragment=new HomeFragment();
		communityFragment=new CommunityFragment();
		shopFragment=new ShopFragment();
		mineFragment=new MineFragment();	
		noLoginFragment=new NoLoginFragment();
		from=homeFragment;
		
		getFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment, "home").commit();
		
		tabMenu=(RadioGroup)findViewById(R.id.tab_menu);
		tabMenu.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			
			@SuppressLint("NewApi")
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.btn_home:
					changeFragment(homeFragment, "home");
					break;

				case R.id.btn_community:
					if(LoginActivity.isLogin){
						changeFragment(communityFragment, "community");
					}else{
						changeFragment(noLoginFragment, "noLogin");
					}					
					break;
					
				case R.id.btn_shop:
					changeFragment(shopFragment, "shop");
					break;
					
				case R.id.btn_mine:
					changeFragment(mineFragment, "mine");
					break;
				}
			}
		});
	}
	
	//切换fragment，有则show,没则add， 对于内存不足可能杀死fragment情况不清楚
	public void changeFragment(Fragment to, String tag){
		if(from!=to){
			FragmentTransaction transaction=getFragmentManager().beginTransaction();
			if(to.isAdded()){
				transaction.hide(from).show(to).commit();
			}else{
				transaction.hide(from).add(R.id.fragment_container, to, tag).commit();
			}
			from=to;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		Intent intent=new Intent(this, BleService.class);
		stopService(intent);

	}
			
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
						
		if(keyCode==KeyEvent.KEYCODE_BACK){
			Fragment fragment=getFragmentManager().findFragmentByTag("community");
			if(fragment!=null){
				View v=fragment.getView().findViewById(R.id.send_comment_input);
				if(v.getVisibility()==View.VISIBLE){
					v.setVisibility(View.GONE);
					return true;
				}
			}			
			long backSecondTime=System.currentTimeMillis();
			if(backSecondTime-backFirstTime>800){
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				backFirstTime=backSecondTime;
				return true;
			}else{
				finish();
			}
		}
		return true;
	}

	

}
