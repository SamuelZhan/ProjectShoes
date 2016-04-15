
package com.shoes.fragment; 

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.shoes.R;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshListView;
import com.shoes.activity.LoginActivity;
import com.shoes.activity.PictureActivity;
import com.shoes.activity.RankingsActivity;
import com.shoes.activity.SendInfoActivity;
import com.shoes.activity.SpecificUserCommunityInfosActivity;
import com.shoes.customview.LoadingDialog;
import com.shoes.customview.RoundCornerImageView;
import com.shoes.server.HttpServer;
import com.squareup.picasso.Picasso;

public class CommunityFragment extends Fragment{

	private Button btnRankings;
	private ListView lvInfos;
	private PullToRefreshListView pullToRefreshListView;
	private LinearLayout inputLayout;
	private EditText etInputComment;
	private Button btnSendComment;
	private ArrayList<HashMap<String, Object>> infos;
	private MyBaseAdapter adapter;
	private InputMethodManager imm;
	private ExecutorService executorService;	
	private String replyCommentId;
	private String replyInfoId;
	private int replyPosition;
	private String date=(String)DateFormat.format("yyyy-MM-dd", new Date());//今天的日期，用于判断是否在动态里显示24小时制时间
	private LoadingDialog loadingDialog;
	private Handler handler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		handler=new Handler();
		executorService=Executors.newFixedThreadPool(3);
		loadingDialog=new LoadingDialog(getActivity(), "");
		
		imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);		
		
		View rootView=inflater.inflate(R.layout.fragment_community, null);

		btnRankings=(Button)rootView.findViewById(R.id.community_btn_rankings);
		btnRankings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getActivity(), RankingsActivity.class));				
			}
		});

		//初始化社区信息
		infos=new ArrayList<HashMap<String,Object>>();		
		HashMap<String, Object> info=new HashMap<String, Object>();
		info.put("userId", LoginActivity.userId);
		info.put("userName", LoginActivity.userName);
		info.put("headImageUrl", LoginActivity.headImageUrl);
		info.put("individualDescription", LoginActivity.description);
		infos.add(info);		
		
		inputLayout=(LinearLayout)rootView.findViewById(R.id.send_comment_input);
		inputLayout.setVisibility(View.GONE);
		
		etInputComment=(EditText)rootView.findViewById(R.id.et_input_comment);		
		
		btnSendComment=(Button)rootView.findViewById(R.id.btn_send_comment);
		btnSendComment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				imm.hideSoftInputFromWindow(etInputComment.getWindowToken(), 0);
				if(etInputComment.getText().toString().trim().equals("")){
					Toast.makeText(getActivity(), "内容为空", Toast.LENGTH_SHORT).show();
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
										Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
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
		
		//下拉刷新容器
		pullToRefreshListView=(PullToRefreshListView)rootView.findViewById(R.id.pull_to_refresh_listview);
		pullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				executorService.submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						ArrayList<HashMap<String, Object>> infoFromWeb=HttpServer.getCommunityMessage();
						if(infoFromWeb==null){
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
									pullToRefreshListView.onPullDownRefreshComplete();
								}
							});
						}else{
							if(infos.size()>1){
								int position=0;
								String timeOfTheFirst=(String)infos.get(1).get("time");
								//遍历刷新获取到的最新十条社区数据，找到最新需要添加到旧数据中的个数
								for(int i=0; i<infoFromWeb.size(); i++){
									HashMap<String, Object> info=infoFromWeb.get(i);
									if(info.get("time").equals(timeOfTheFirst)){
										position=i;
										break;
									}
								}
								for(int i=position-1; i>=0; i--){
									infos.add(1, infoFromWeb.get(i));
								}
								
							}else{
								infos.addAll(infoFromWeb);
							}
							
							handler.post(new Runnable() {
															
								@Override
								public void run() {
									// TODO Auto-generated method stub
									adapter.notifyDataSetChanged();
									pullToRefreshListView.onPullDownRefreshComplete();
									SimpleDateFormat df=new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
									String timeString=df.format(new Date(System.currentTimeMillis()));
							        pullToRefreshListView.setLastUpdatedLabel(timeString);
								}
							});
							
						}
					}
				});
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				
			}
		});
		
		lvInfos=pullToRefreshListView.getRefreshableView();
		lvInfos.setCacheColorHint(0x00000000);
		lvInfos.setDividerHeight(0);				
		adapter=new MyBaseAdapter(getActivity(), infos);
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
									.getMoreCommunityMessage((String) infos.get(infos.size()-1).get("time"));
							if(infosFromWeb==null){
								
							}else{
								infos.addAll(infosFromWeb);
								getActivity().runOnUiThread(new Runnable() {
									
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
		
		lvInfos.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				imm.hideSoftInputFromWindow(etInputComment.getWindowToken(), 0);
				inputLayout.setVisibility(View.GONE);
			}
		});
		
		lvInfos.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				imm.hideSoftInputFromWindow(etInputComment.getWindowToken(), 0);
				inputLayout.setVisibility(View.GONE);
				return true;
			}
		});
		
		pullToRefreshListView.doPullRefreshing(true, 0);
		
		return rootView;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		inputLayout.setVisibility(View.GONE);
		if(requestCode==1 && resultCode==1){
			HashMap<String, Object> info=(HashMap<String, Object>)data.getSerializableExtra("info");			
			infos.add(1, info);
			adapter.notifyDataSetChanged();			
			imm.hideSoftInputFromWindow(etInputComment.getWindowToken(), 0);			
		}
	}
	
	//若在个人信息里修改了信息，那就调用该函数改变所有项
	public void changeInfos(){
		infos.clear();
		HashMap<String, Object> info=new HashMap<String, Object>();
		info.put("userId", LoginActivity.userId);
		info.put("userName", LoginActivity.userName);
		info.put("headImageUrl", LoginActivity.headImageUrl);
		info.put("individualDescription", LoginActivity.description);
		infos.add(info);
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ArrayList<HashMap<String, Object>> infoFromWeb=HttpServer.getCommunityMessage();
				if(infoFromWeb==null){
					
				}else{
					infos.addAll(infoFromWeb);
					adapter.notifyDataSetChanged();
					
				}
			}
		});
	}

	private class MyBaseAdapter extends BaseAdapter{
		
		private Context context;
		private ArrayList<HashMap<String, Object>> data;
		
		public MyBaseAdapter(Context c, ArrayList<HashMap<String, Object>> infos){
			context=c;
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
			TextView btnDelete;
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
		public View getView(final int position, View convertView, ViewGroup group) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			//解决scrollview与listview不兼容问题所以，去掉scrollview层，保留listview，
			//第一个item与其他items布局不一样，要定制两个不一样的item布局，用position识别即可
			if(position==0){
				
				convertView=LayoutInflater.from(context).inflate(R.layout.list_item_community_the_first, null);
				
				RoundCornerImageView ivHead=(RoundCornerImageView)convertView.findViewById(R.id.community_head_image);
				ivHead.setImageResource(R.drawable.head_image_default);
				Picasso.with(context).load((String) data.get(position).get("headImageUrl")).error(R.drawable.head_image_default).into(ivHead);
				ivHead.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(getActivity(), SpecificUserCommunityInfosActivity.class);
						intent.putExtra("userId", LoginActivity.userId);
						intent.putExtra("userName", LoginActivity.userName);
						startActivity(intent);
					}
				});
				
				TextView tvName=(TextView)convertView.findViewById(R.id.community_nickname);
				tvName.setText((String) data.get(position).get("userName"));
				
				TextView tvDescriptionTextView=(TextView)convertView.findViewById(R.id.community_my_comment);
				if(data.get(position).get("individualDescription")!=null){
					tvDescriptionTextView.setText((String)data.get(position).get("individualDescription"));
				}else{
					tvDescriptionTextView.setText("");
				}
				
				LinearLayout btnSendInfo=(LinearLayout)convertView.findViewById(R.id.btn_send_info);				
				btnSendInfo.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						startActivityForResult(new Intent(getActivity(), SendInfoActivity.class), 1);
					}
				});

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
					holder.btnDelete=(TextView)convertView.findViewById(R.id.list_item_btn_delete);
					convertView.setTag(holder);
				}
				
				String headImageUrl=(String)data.get(position).get("headImageUrl");
				holder.ivHead.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(getActivity(), SpecificUserCommunityInfosActivity.class);
						intent.putExtra("userId", (String)data.get(position).get("userId"));
						intent.putExtra("userName", (String)data.get(position).get("userName"));
						startActivity(intent);
					}
				});
				if(headImageUrl!=null){	
					//使用开源图片加载项目，毕加索
					Picasso.with(context).load(headImageUrl).error(R.drawable.head_image_default).into(holder.ivHead);								
				}else{
					holder.ivHead.setImageResource(R.drawable.head_image_default);
				}
				
				String nickname=(String) data.get(position).get("userName");
				holder.tvNickname.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(getActivity(), SpecificUserCommunityInfosActivity.class);
						intent.putExtra("userId", (String)data.get(position).get("userId"));
						intent.putExtra("userName", (String)data.get(position).get("userName"));
						startActivity(intent);
					}
				});
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
				if(content==null || content.trim().equals("")){
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
						Intent intent=new Intent(getActivity(), PictureActivity.class);
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
				
				if(data.get(position).get("userId").equals(LoginActivity.userId)){
					holder.btnDelete.setVisibility(View.VISIBLE);
					holder.btnDelete.setOnClickListener(new BtnDeleteOnClickListener((String)data.get(position).get("infoId"), position));
				}else{
					holder.btnDelete.setVisibility(View.GONE);
				}
				
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
						TextView tvComment=new TextView(getActivity());
						tvComment.setTextColor(Color.WHITE);
						tvComment.setBackgroundResource(R.drawable.selector_comment_bg);
						SpannableString spStr1=new SpannableString((String)comments.get(i).get("userName"));
						spStr1.setSpan(new NoUnderLineClickableSpan((String)comments.get(i).get("userId"), (String)comments.get(i).get("userName")),
								0, spStr1.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
						tvComment.append(spStr1);		
																							
						if(!((String)comments.get(i).get("userName2")).equals("")){
							tvComment.append(" 回复 ");
							//这里需要被回复人的ID
							SpannableString spStr2=new SpannableString((String)comments.get(i).get("userName2"));
							spStr2.setSpan(new NoUnderLineClickableSpan((String)comments.get(i).get("userId2"), (String)comments.get(i).get("userName2")),
									0, spStr2.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
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
					Toast.makeText(getActivity(), "内容为空", Toast.LENGTH_SHORT).show();
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
										Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
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
						Toast.makeText(getActivity(), "你已点过赞", Toast.LENGTH_SHORT).show();
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
						String result=HttpServer.praise(infoId, LoginActivity.userId);
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
		
		private class BtnDeleteOnClickListener implements OnClickListener{
			private String messageId;
			private int position;

			public BtnDeleteOnClickListener(String messageId, int position){
				this.messageId=messageId;
				this.position=position;
			}
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				loadingDialog.show();				
				executorService.submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String result=HttpServer.delete(messageId, LoginActivity.userId);
						if(result.equals("1")){
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub																		
									data.remove(position);
									adapter.notifyDataSetChanged();
								}
							});
							
						}else if(result.equals("0")){
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub																		
									Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT).show();
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
		
		private class NoUnderLineClickableSpan extends ClickableSpan{

			private String userId;
			private String userName;
			
			public NoUnderLineClickableSpan(String userId, String userName){
				this.userId=userId;
				this.userName=userName;
			}
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(getActivity(), SpecificUserCommunityInfosActivity.class);
				intent.putExtra("userId", userId);
				intent.putExtra("userName", userName);
				startActivity(intent);
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
