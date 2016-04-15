/**  
* @Project: Shoes
* @Title: ContactersActivity.java
* @Package com.shoes.activity
* @Description: TODO
* @author lzj
* @date 2015-10-20 上午10:03:45
* @version V1.0  
*/ 
package com.shoes.activity; 

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.shoes.R;

public class ContactersActivity extends Activity {

	private ListView listView;
	private ImageButton btnBack;
	private TextView tvNoContacter;
	private ArrayList<HashMap<String, Object>> contacters=new ArrayList<HashMap<String, Object>>();
	private SimpleAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacters);
		
		ContentResolver cr=getContentResolver();
		Cursor contacterCursor=cr.query(Phone.CONTENT_URI, new String[]{Phone.DISPLAY_NAME, Phone.NUMBER },
				null, null, null);
		
		if(contacterCursor!=null){
			while(contacterCursor.moveToNext()){
				String phone=contacterCursor.getString(1);
				if(TextUtils.isEmpty(phone))
					continue;
				String name=contacterCursor.getString(0);
				HashMap<String, Object> contacter=new HashMap<String, Object>();
				contacter.put("name", name);
				contacter.put("phone", phone);
				contacters.add(contacter);
			}
		}
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		tvNoContacter=(TextView)findViewById(R.id.tip_no_contacter);		
		if(contacters.size()==0){
			tvNoContacter.setVisibility(View.VISIBLE);
		}
		
		
		listView=(ListView)findViewById(R.id.list_contacters);
		adapter=new SimpleAdapter(this, contacters, R.layout.list_item_contacters,
						new String[]{"name", "phone"},
						new int[]{R.id.list_item_contacters_name, R.id.list_item_contacters_phone});
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent=getIntent();
				intent.putExtra("contacter", contacters.get(position));
				setResult(1, intent);
				finish();
			}
		});
						
	}

	
	public static HashMap<String, Object> getRandomContacter(Context c){
		ContentResolver cr=c.getContentResolver();
		HashMap<String, Object> randomContacter=new HashMap<String, Object>();
		Cursor contacterCursor=cr.query(Phone.CONTENT_URI, new String[]{Phone.DISPLAY_NAME, Phone.NUMBER },
				null, null, null);
		int i=(int)(Math.random()*contacterCursor.getCount());
		if(contacterCursor!=null){
			if(contacterCursor.moveToPosition(i)){
				randomContacter.put("name", contacterCursor.getString(0));
				randomContacter.put("phone", contacterCursor.getString(1));
			}
		}		
		return randomContacter;
	}
	
}
