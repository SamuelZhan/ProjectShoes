package com.shoes.activity; 

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.example.shoes.R;
import com.shoes.customview.LoadingDialog;
import com.shoes.customview.RoundCornerImageView;
import com.shoes.server.HttpServer;
import com.squareup.picasso.Picasso;

public class SpecificUserCommunityInfosActivity extends Activity {

	private ImageButton btnBack;
	private TextView tvTitleUserName;
	private ListView lvInfos;
	private LinearLayout inputLayout;
	private EditText etInputComment;
	private Button btnSendComment;
	private MyBaseAdapter adapter;
	private ArrayList<HashMap<String, Object>> infos;
	private ExecutorService executorService;
	private InputMethodManager imm;
	private Handler handler;
	private String date=(String)DateFormat.format("yyyy-MM-dd", new Date());
	private String replyCommentId;
	private String replyInfoId;
	private int replyPosition;
	private String headImageUrl;
	private String userId;
	private String userName;
	private String description;
	private LoadingDialog loadingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_specific_user_community_infos);
		
		loadingDialog=new LoadingDialog(this, "");
		executorService=Executors.newFixedThreadPool(3);
		imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		handler=new Handler();
		
		userId=getIntent().getStringExtra("userId");
		userName=getIntent().getStringExtra("userName");
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		tvTitleUserName=(TextView)findViewById(R.id.tv_specific_user_name);
		tvTitleUserName.setText(userName);
		
		inputLayout=(LinearLayout)findViewById(R.id.send_comment_input);
		inputLayout.setVisibility(View.GONE);
		
		etInputComment=(EditText)findViewById(R.id.et_input_comment);		
		
		btnSendComment=(Button)findViewById(R.id.btn_send_comment);
		btnSendComment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				imm.hideSoftInputFromWindow(etInputComment.getWindowToken(), 0);
				if(etInputComment.getText().toString().trim().equals("")){
					Toast.makeText(SpecificUserCommunityInfosActivity.this, "内容为空", Toast.LENGTH_SHORT).show();
				}else{
					loadingDialog.show();
					executorService.submit(new Runnable() {
						@SuppressWarnings("unchecked")
						public void run() {
							HashMap<String, Object> comment=HttpServer.sendComment(LoginActivity.userId, replyInfoId, 
									etInputComment.getText().toString(), replyCommentId);
							if(comment!=null){
								((ArrayList<HashMap<String, Object>>)infos.get(replyPosition).get("comments")).add(comment);
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										adapter.notifyDataSetChanged();
										etInputComment.setText("");
										inputLayout.setVisibility(View.GONE);
									}
								});								
							}else{
								handler.post(new Runnable() {
									public void run() {
										Toast.makeText(SpecificUserCommunityInfosActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
									}	
								});
							}
							if(loadingDialog!=null && loadingDialog.isShowing()){
								loadingDialog.dismiss();
							}
						}
					});
				}
			}
		});
		
		infos=new ArrayList<HashMap<String, Object>>();
		
		HashMap<String, Object> info=new HashMap<String, Object>();
		infos.add(info);
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, Object> userInfo=HttpServer.getUserInfo(userId);
				if(userInfo!=null){
					headImageUrl=(String) userInfo.get("headImageUrl");
					description=(String) userInfo.get("description");
				}else{
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(SpecificUserCommunityInfosActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
						}
					});
				}
				
				ArrayList<HashMap<String, Object>> infoFromWeb=HttpServer.getSpecificUserCommunityMessage(userId);
				if(infoFromWeb==null){
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(SpecificUserCommunityInfosActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
						}
					});
				}else{
					infos.addAll(infoFromWeb);
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							adapter.notifyDataSetChanged();
						}
					});
					
				}
			}
		});
		
		lvInfos=(ListView)findViewById(R.id.user_community_infos);
		adapter=new MyBaseAdapter(SpecificUserCommunityInfosActivity.this, infos);
		lvInfos.setAdapter(adapter);
		lvInfos.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView v, int arg1) {
				// TODO Auto-generated method stub
				
				inputLayout.setVisibility(View.GONE);
				imm.hideSoftInputFromWindow(etInputComment.getWindowToken(), 0);
				
				if(lvInfos.getLastVisiblePosition()==infos.size()-1){
					executorService.submit(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							ArrayList<HashMap<String, Object>> infosFromWeb=HttpServer
									.getSpecificUserMoreCommunityMessage(userId, (String) infos.get(infos.size()-1).get("time"));
							if(infosFromWeb==null){
								
							}else{
								infos.addAll(infosFromWeb);
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										adapter.notifyDataSetChanged();
									}
								});
								
							}
						}
					});
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleView, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}		
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(inputLayout.getVisibility()==View.VISIBLE){
				inputLayout.setVisibility(View.GONE);
			}else{
				super.onKeyDown(keyCode, event);
			}
			
		}
		return true;
	}



	private class MyBaseAdapter extends BaseAdapter{
		
		private Context context;
		private ArrayList<HashMap<String, Object>> data;
		
		public MyBaseAdapter(Context context, ArrayList<HashMap<String, Object>> infos){
			this.context=context;
			data=infos;
		}
		
		private class ViewHolder{
			boolean isTheFirst;
			RoundCornerImageView ivHead;
			TextView tvNickname;
			TextView tvDate;
			TextView tvContent;
			ImageView ivContent;
			TextView tvPraise;
			TextView tvComment;
			EditText etComment;
			Button btnSend;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return data.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			//解决scrollview与listview不兼容问题所以，去掉scrollview层，保留listview，
			//第一个item与其他items布局不一样，要定制两个不一样的item布局，用position识别即可
			if(position==0){
				convertView=LayoutInflater.from(context).inflate(R.layout.list_item_user_community_infos_the_first, null);
				TextView tvName=(TextView)convertView.findViewById(R.id.tv_user_name);
				TextView tvId=(TextView)convertView.findViewById(R.id.tv_user_id);
				TextView tvDescription=(TextView)convertView.findViewById(R.id.tv_user_description);
				RoundCornerImageView ivHead=(RoundCornerImageView)convertView.findViewById(R.id.iv_head);
				Picasso.with(context).load(headImageUrl).into(ivHead);
				tvName.setText(userName);
				tvId.setText("会员号："+userId);
				tvDescription.setText(description);
				holder=new ViewHolder();
				holder.isTheFirst=true;
				convertView.setTag(holder);
			}else{				
				//通过isTheFirst判断是不是第一个item的holder，若不是则新建holder，用于除第一个item外的items
				//否则直接重用，达到优化性能的效果
				if(convertView!=null && !((ViewHolder) convertView.getTag()).isTheFirst){
					holder=(ViewHolder) convertView.getTag();	
				}else{
					convertView=LayoutInflater.from(context).inflate(R.layout.list_item_comments, null);
					holder=new ViewHolder();
					holder.ivHead=(RoundCornerImageView)convertView.findViewById(R.id.list_item_infos_head_image);
					holder.tvNickname=(TextView)convertView.findViewById(R.id.list_item_infos_nickname);
					holder.tvDate=(TextView)convertView.findViewById(R.id.list_item_infos_date);
					holder.tvContent=(TextView)convertView.findViewById(R.id.list_item_infos_content);
					holder.ivContent=(ImageView)convertView.findViewById(R.id.list_item_infos_content_image);
					holder.tvPraise=(TextView)convertView.findViewById(R.id.list_item_btn_praise);
					holder.tvComment=(TextView)convertView.findViewById(R.id.list_item_btn_comment);
					holder.etComment=(EditText)convertView.findViewById(R.id.list_item_community_comment);
					holder.btnSend=(Button)convertView.findViewById(R.id.list_item_community_comment_send);	
					convertView.setTag(holder);
				}
				
				String headImageUrl=(String)data.get(position).get("headImageUrl");
				if(headImageUrl!=null){	
					//使用开源图片加载项目，毕加索
					Picasso.with(context).load(headImageUrl).into(holder.ivHead);
				}else{
					holder.ivHead.setImageResource(R.drawable.head_image_default);
				}
				
				String nickname=(String) data.get(position).get("userName");
				if(nickname==null){
					holder.tvNickname.setText("");
				}else{
					holder.tvNickname.setText(nickname);
				}
				
				String time=(String)data.get(position).get("time");
				if(time==null){
					holder.tvDate.setText(DateFormat.format("yyyy-MM-dd", new Date()));
				}else{
					if(time.substring(0, 10).equals(date)){
						holder.tvDate.setText(time.substring(11, 16));
					}else{
						holder.tvDate.setText(time.substring(0, 10));
					}					
				}
				
				String content=(String) data.get(position).get("content");
				if(content==null){
					holder.tvContent.setVisibility(View.GONE);
				}else{
					holder.tvContent.setVisibility(View.VISIBLE);
					holder.tvContent.setText(content);
				}
				
				final String thumbnailUrl=(String)data.get(position).get("thumbnail");				
				if(thumbnailUrl!=null){	
					
					Picasso.with(context).load(thumbnailUrl).into(holder.ivContent);
					holder.ivContent.setVisibility(View.VISIBLE);
								
				}else{
					holder.ivContent.setVisibility(View.GONE);
				}
				final String imageUrl=(String)data.get(position).get("imageUrl");
				holder.ivContent.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(SpecificUserCommunityInfosActivity.this, PictureActivity.class);
						intent.putExtra("thumbnailUrl", thumbnailUrl);
						intent.putExtra("imageUrl", imageUrl);
						startActivity(intent);
					}
				});
				
				String praiseString=(String)data.get(position).get("praise");
				String[] userIds=null;
				boolean hasPraised=false;								
				if(praiseString.trim().equals("")){
					userIds=new String[0];
					Drawable drawable=getResources()
							.getDrawable(R.drawable.community_btn_unsupport);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					holder.tvPraise.setCompoundDrawables(drawable, null, null, null);
				}else{
					userIds=praiseString.split(",");
					for(int i=0; i<userIds.length; i++){
						if(userIds[i].equals(LoginActivity.userId)){
							Drawable drawable=getResources()
									.getDrawable(R.drawable.community_btn_support);
							drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
							holder.tvPraise.setCompoundDrawables(drawable, null, null, null);
							hasPraised=true;
							break;
						}
					}
					if(!hasPraised){
						Drawable drawable=getResources()
								.getDrawable(R.drawable.community_btn_unsupport);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
						holder.tvPraise.setCompoundDrawables(drawable, null, null, null);
					}
				}	
				holder.tvPraise.setText(""+userIds.length);
				holder.tvPraise.setOnClickListener(new BtnPraiseOnClickListener((String)data.get(position).get("infoId"),
						userIds, position));
				
				holder.tvComment.setOnClickListener(new CommentCountOnClickListener(holder.etComment));
								
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> comments
					=(ArrayList<HashMap<String, Object>>) data.get(position).get("comments");
				LinearLayout listItemLayout=(LinearLayout) convertView.findViewById(R.id.comments_layout);
				//先清除convertview中缓存的textview，再重新添加
				listItemLayout.removeAllViews();
				if(comments!=null){					
					holder.tvComment.setText(""+comments.size()); 
					for(int i=0; i<comments.size(); i++){
						TextView tvComment=new TextView(SpecificUserCommunityInfosActivity.this);
						tvComment.setTextColor(Color.WHITE);
						tvComment.setBackgroundResource(R.drawable.selector_comment_bg);
						SpannableString spStr1=new SpannableString((String)comments.get(i).get("userName"));
						spStr1.setSpan(new NoUnderLineClickableSpan(), 0, spStr1.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
						tvComment.append(spStr1);		
																							
						if(!((String)comments.get(i).get("userName2")).equals("")){
							tvComment.append(" 回复 ");
							SpannableString spStr2=new SpannableString((String)comments.get(i).get("userName2"));
							spStr2.setSpan(new NoUnderLineClickableSpan(), 0, spStr2.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
							tvComment.append(spStr2);
						}
						
						tvComment.setMovementMethod(LinkMovementMethod.getInstance());
						
						tvComment.append(" : "+(String)comments.get(i).get("commentContent"));
												
						tvComment.setOnClickListener(new CommentOnClickListener(position,
								(String)comments.get(i).get("userName"), (String)comments.get(i).get("commentId")));
						listItemLayout.addView(tvComment);
					}										
				}else{
					holder.tvComment.setText("0");
				}
				holder.btnSend.setOnClickListener(new BtnSendOnClickListener(holder.etComment, position));
							
			}
			return convertView;

		}
		
		private class BtnSendOnClickListener implements OnClickListener{
			
			private EditText etText;
			private int position;
			
			public BtnSendOnClickListener(EditText etText, int position){
				this.etText=etText;
				this.position=position;
			}

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				imm.hideSoftInputFromWindow(etText.getWindowToken(), 0);
				
				if(etText.getText().toString().trim().equals("")){
					Toast.makeText(SpecificUserCommunityInfosActivity.this, "内容为空", Toast.LENGTH_SHORT).show();
				}else{
					loadingDialog.show();
					executorService.submit(new Runnable() {
						
						@SuppressWarnings("unchecked")
						@Override
						public void run() {
							// TODO Auto-generated method stub
							
							//发表评论应该为自己id
							HashMap<String, Object> comment=HttpServer.sendComment(LoginActivity.userId, 
									(String)data.get(position).get("infoId"),
									etText.getText().toString(), null);
							if(comment!=null){
								((ArrayList<HashMap<String, Object>>) data.get(position).get("comments")).add(comment);
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										etText.setText("");
										adapter.notifyDataSetChanged();
									}
								});
							}else{
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(SpecificUserCommunityInfosActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
									}
								});
							}
							if(loadingDialog!=null && loadingDialog.isShowing()){
								loadingDialog.dismiss();
							}
						}
					});
				}
			
			}
			
		}
		
		private class BtnPraiseOnClickListener implements OnClickListener{
			private String infoId;
			private String[] userIds;
			private int position;

			public BtnPraiseOnClickListener(String infoId, String[] userIds, int position){
				this.infoId=infoId;
				this.userIds=userIds;
				this.position=position;
			}
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				loadingDialog.show();
				for(int i=0; i<userIds.length; i++){
					if(userIds[i].equals(LoginActivity.userId)){
						Toast.makeText(SpecificUserCommunityInfosActivity.this, "你已点过赞", Toast.LENGTH_SHORT).show();
						if(loadingDialog!=null && loadingDialog.isShowing()){
							loadingDialog.dismiss();
						}
						return;
					}
				}
				executorService.submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String result=HttpServer.praise(infoId, LoginActivity.userId);Log.d("zz", "result="+result);
						if(result.equals("1")){
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub																		
									data.get(position).put("praise", 
											data.get(position).get("praise")+LoginActivity.userId+",");
									adapter.notifyDataSetChanged();
								}
							});
							
						}
						if(loadingDialog!=null && loadingDialog.isShowing()){
							loadingDialog.dismiss();
						}
					}
				});
				
				
			}
			
		}
		
		private class CommentOnClickListener implements OnClickListener{

			private int position;
			private String commentId;
			private String userName;
			
			public CommentOnClickListener(int position, String userName, String commentId){
				this.position=position;
				this.userName=userName;
				this.commentId=commentId;
			}
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				inputLayout.setVisibility(View.VISIBLE);
				replyCommentId=commentId;
				replyPosition=position;
				replyInfoId=(String)data.get(position).get("infoId");
				etInputComment.setFocusable(true);
				etInputComment.requestFocus();
				etInputComment.setFocusableInTouchMode(true);
				etInputComment.setText("");
				etInputComment.setHint("回复 "+userName+" : ");
				imm.showSoftInput(etInputComment, 0);
			}
			
		}
		
		private class CommentCountOnClickListener implements OnClickListener{
			private EditText etComment;

			public CommentCountOnClickListener(EditText etComment){
				this.etComment=etComment;
			}
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				etComment.requestFocus();
				imm.showSoftInput(etComment, 0);
			}
			
		}
		
		private class NoUnderLineClickableSpan extends ClickableSpan{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				// TODO Auto-generated method stub
				ds.setColor(0xffFFDD11);
				ds.setUnderlineText(false);
			}
			
		}
		
		
	}
	
}
