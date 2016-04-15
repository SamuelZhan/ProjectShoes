
package com.shoes.server; 

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.R.integer;
import android.content.Context;
import android.util.Log;

public class HttpServer {

	public static final String REGISTER = "http://112.74.199.133:8080/shoe/addUserAction.action";// 注册
	public static final String LOGIN = "http://112.74.199.133:8080/shoe/loginAction.action";// 登录
	public static final String RESET_PASSWORD = "http://112.74.199.133:8080/shoe/updatePasswordAction.action";// 修改密码
	public static final String GET_USER_INFO = "http://112.74.199.133:8080/shoe/user_getUserInfo.action";// 获取用户信息
	public static final String UPDATE_USER_INFO = "http://112.74.199.133:8080/shoe/user_updateInfo.action";// 修改用户信息
	public static final String GET_COMMUNITY_MESSAGE="http://112.74.199.133:8080/shoe/thought_getThoughts.action";//获取社区最新动态
	public static final String GET_MORE_COMMUNITY_MESSAGE="http://112.74.199.133:8080/shoe/thought_getFollowings.action";//获取更多动态
	public static final String GET_SPECIFIC_USER_COMMUNITY_MESSAGE="http://112.74.199.133:8080/shoe/thought_getUserThoughts.action";//获取指定用户的动态
	public static final String GET_SPECIFIC_USER_MORE_COMMUNITY_MESSAGE="http://112.74.199.133:8080/shoe/thought_getFollowing.action";//获取指定用户更多社区动态
	public static final String SEND_INFO="http://112.74.199.133:8080/shoe/thought_addThought.action";//发表动态
	public static final String SEND_COMMENT="http://112.74.199.133:8080/shoe/thought_addComment.action";//发表评论
	public static final String PRAISE="http://112.74.199.133:8080/shoe/thought_praise.action";//点赞
	public static final String DELETE="http://112.74.199.133:8080/shoe/thought_delThought.action";//删除
	public static final String GET_RANKING="http://112.74.199.133:8080/shoe/user_getRanklist.action";//获取排行榜
	public static final String GET_RECOMMENDED_COMMODITIES="http://112.74.199.133:8080/shoe/product_getRecommends.action";//获取推荐商品
	public static final String GET_DISPLAY_COMMODITIES="http://112.74.199.133:8080/shoe/product_getExhibition.action";//获取展示商品
	public static final String LOGIN_WITH_OTHER_PLATFORM="http://112.74.199.133:8080/shoe/user_thirdPartyRegister.action";//三方登陆
	public static final String UPDATE_USER_STEPS="http://112.74.199.133:8080/shoe/user_updateDailyStep.action";//上传步数
	public static final String GET_ADVERTISEMENT="http://112.74.199.133:8080/shoe/product_getAd.action";//获取主页广告
	public static final String SEND_FEEDBACK="http://112.74.199.133:8080/shoe/addSuggestAction";//发送反馈
	public static final String GET_FEEDBACK="http://112.74.199.133:8080/shoe/suggestion_getMySuggest.action";//获取反馈
	public static final String GET_OPEN_ID="https://api.weixin.qq.com/sns/oauth2/access_token";//获取微信openid
	
	public static String register(String phoneNum, String password){
		
		try {
			HttpPost request=new HttpPost(REGISTER);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			List<NameValuePair> params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userMobile", phoneNum));
			params.add(new BasicNameValuePair("userPassword", password));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse httpResponse=new DefaultHttpClient().execute(request);
			JSONTokener jsonParser=new JSONTokener(EntityUtils.toString(httpResponse.getEntity()));
			JSONObject user=(JSONObject)jsonParser.nextValue();
			String s=user.getString("result");
			if(s.equals("0")){
				return "注册成功";
			}else if(s.equals("1")){
				return "手机号码已注册";
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("zz", e.toString());
		}		
		return "网络异常，请检查你的网络";		
	}
	
	public static String login(String phoneNum, String password){
		try{
			HttpPost request=new HttpPost(LOGIN);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			List<NameValuePair> params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userMobile", phoneNum));
			params.add(new BasicNameValuePair("userPassword", password));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject jsonObject=new JSONObject(EntityUtils.toString(response.getEntity()));
			
			String result=jsonObject.getString("result");
			if(result.equals("0")){
				return jsonObject.getString("userId");
			}else{
				return null;
			}
		}catch(Exception e){
			Log.d("zz", e.toString());
		}
		return null;
	}
	
	public static String resetPassword(String phoneNum, String password){
		
		try{
			HttpPost request=new HttpPost(RESET_PASSWORD);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			List<NameValuePair> params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userMobile", phoneNum));
			params.add(new BasicNameValuePair("userPassword", password));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse httpResponse=new DefaultHttpClient().execute(request);
			JSONTokener jsonParser=new JSONTokener(EntityUtils.toString(httpResponse.getEntity()));
			JSONObject user=(JSONObject)jsonParser.nextValue();
			String s=user.getString("result");
			
			if(s.equals("0")){
				return "密码修改成功";
			}else if(s.equals("1")){
				return "用户名不存在";
			}
		}catch(Exception e){
			Log.d("zz", e.toString());
		}
		return "网络异常，请检查你的网络";
	}
	
	//获取用户信息
	public static HashMap<String, Object> getUserInfo(String userId){
		
		try {
			HttpPost request=new HttpPost(GET_USER_INFO);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			
			List<NameValuePair> params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("users.userId", userId));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject infosJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			HashMap<String, Object> userInfo=new HashMap<String, Object>();
			userInfo.put("userId", infosJson.get("userId"));
			userInfo.put("userName", infosJson.get("userName"));
			userInfo.put("headImageUrl", "http://112.74.199.133:8080/shoe/"+infosJson.get("headImage"));
			userInfo.put("sex", infosJson.get("sex"));
			userInfo.put("description",infosJson.get("individualDescription"));
			userInfo.put("userArea", infosJson.get("userArea"));
			userInfo.put("height", infosJson.get("height"));
			userInfo.put("weight", infosJson.get("weight"));
			userInfo.put("age", infosJson.get("age"));
			userInfo.put("stepNumber", infosJson.get("stepNumber"));
			userInfo.put("stepLength", infosJson.get("stepLength"));
			userInfo.put("consume", infosJson.get("consume"));
			userInfo.put("distance", infosJson.get("journey"));
			
			return userInfo;
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("zz", e.toString());
		}		
		return null;		
	}
	
	//修改我的信息
	public static String updateUserInfo(Map<String, String> texts, Map<String, String> files ){
		String BOUNDARY="-----------------------132821742118716";
		HttpURLConnection connection=null;
		String result=null;
		try{
			URL url=new URL(UPDATE_USER_INFO);
			connection=(HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(300000);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0(Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+BOUNDARY);
			OutputStream os=connection.getOutputStream();
			
			//发送文本信息
			if(texts!=null){
				StringBuffer sb=new StringBuffer();
				for(Map.Entry<String, String> entry : texts.entrySet()){
					sb.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					sb.append("Content-Disposition:form-data;name=\""+entry.getKey()+"\"\r\n\r\n");
					sb.append(entry.getValue());
				}
				os.write(sb.toString().getBytes("utf-8"));
			}
			//发送参数和图片
			if(files!=null){				
				for(Map.Entry<String, String> entry : files.entrySet()){					
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					if(value==null){
						continue;
					}
					
					File file = new File(URI.create(value));
					String filename = file.getName();
					String contentType = new MimetypesFileTypeMap().getContentType(file);
					if(contentType==null || contentType.equals("")){
						contentType = "application/octet-stream";
					}
					StringBuffer sb = new StringBuffer();
					sb.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					sb.append("Content-Disposition:form-data;name=\""+key+"\";filename=\""+filename+"\"\r\n");
					sb.append("Content-Type:"+contentType+"\r\n\r\n");
					os.write(sb.toString().getBytes());
					DataInputStream in = new DataInputStream(new FileInputStream(file));
					int len = 0;
					byte[] buffer = new byte[1024];
					while((len=in.read(buffer))!=-1){
						os.write(buffer, 0, len);
					}
					in.close();
				}
			}
			byte[] endData = ("\r\n--"+BOUNDARY+"--\r\n").getBytes();
			os.write(endData);
			os.flush();
			os.close();
			
			//读取服务器返回信息
			StringBuffer sb = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while((line=reader.readLine())!=null){
				sb.append(line).append("\n");
			}
			reader.close();
			reader = null;
			JSONObject resultJson=new JSONObject(sb.toString());
			result=resultJson.getString("result");
			Log.d("zz", "result:"+result);
		}catch(Exception e){
			result="1";
		}finally{
			if(connection!=null){
				connection.disconnect();
				connection=null;
			}
		}
		
		return result;

	}
	
	//获取社区动态
	public static ArrayList<HashMap<String, Object>> getCommunityMessage(){
		ArrayList<HashMap<String, Object>> infos = new ArrayList<HashMap<String, Object>>();
		try{			
			HttpPost request = new HttpPost(GET_COMMUNITY_MESSAGE);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject infosJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			
			JSONArray infosJsonArray=infosJson.getJSONArray("info");
			for(int i=0; i<infosJsonArray.length(); i++){
				JSONObject infoJson=(JSONObject)infosJsonArray.opt(i);
				
				HashMap<String, Object> info=new HashMap<String, Object>();
				info.put("infoId", infoJson.get("tId"));
				info.put("userId", infoJson.get("userId"));
				info.put("userName", infoJson.get("userName"));
				info.put("headImageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("headImage"));
				info.put("time", infoJson.get("p_time"));
				info.put("content", infoJson.get("content"));
				if(!infoJson.get("image").equals("")){
					info.put("imageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("image"));
				}
				if(!infoJson.get("thumbnail").equals("")){
					info.put("thumbnail", "http://112.74.199.133:8080/shoe/"+infoJson.get("thumbnail"));
				}
				info.put("praise", infoJson.get("praise"));
				
				ArrayList<HashMap<String, Object>> comments=new ArrayList<HashMap<String, Object>>();
				JSONArray commentsJsonArray=infoJson.getJSONArray("comments");
				for(int j=0; j<commentsJsonArray.length(); j++){
					JSONObject commentJson=(JSONObject)commentsJsonArray.opt(j); 
					HashMap<String, Object> comment=new HashMap<String, Object>();
					comment.put("commentId", commentJson.get("c_id"));
					comment.put("userId", commentJson.get("userId"));
					comment.put("userName", commentJson.get("userName"));
					comment.put("commentContent", commentJson.get("CConent"));
					comment.put("reply", commentJson.get("reply"));
					comment.put("userId2", commentJson.get("userId2"));
					comment.put("userName2", commentJson.get("userName2"));
					comments.add(comment);
				}
				info.put("comments", comments);
				infos.add(info);
			}
			
		}catch(Exception e){
			Log.d("zz", e.toString());
			return null;
		}
		
		
		return infos;
		
	}
	
	//获取更多社区动态
	public static ArrayList<HashMap<String, Object>> getMoreCommunityMessage(String lastTime){
		ArrayList<HashMap<String, Object>> infos=new ArrayList<HashMap<String,Object>>();
		try{
			HttpPost request = new HttpPost(GET_MORE_COMMUNITY_MESSAGE);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("lastTime", lastTime));
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject infosJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			
			Log.d("zz", "返回:"+infosJson.getString("result"));
			JSONArray infosJsonArray=infosJson.getJSONArray("info");
			for(int i=0; i<infosJsonArray.length(); i++){
				JSONObject infoJson=(JSONObject)infosJsonArray.opt(i);
				
				HashMap<String, Object> info=new HashMap<String, Object>();
				info.put("infoId", infoJson.get("tId"));
				info.put("userId", infoJson.get("userId"));
				info.put("userName", infoJson.get("userName"));
				info.put("headImageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("headImage"));
				info.put("time", infoJson.get("p_time"));
				info.put("content", infoJson.get("content"));
				if(!infoJson.get("image").equals("")){
					info.put("imageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("image"));
				}
				if(!infoJson.get("thumbnail").equals("")){
					info.put("thumbnail", "http://112.74.199.133:8080/shoe/"+infoJson.get("thumbnail"));
				}
				info.put("praise", infoJson.get("praise"));
				
				ArrayList<HashMap<String, Object>> comments=new ArrayList<HashMap<String, Object>>();
				JSONArray commentsJsonArray=infoJson.getJSONArray("comments");
				for(int j=0; j<commentsJsonArray.length(); j++){
					JSONObject commentJson=(JSONObject)commentsJsonArray.opt(j);
					HashMap<String, Object> comment=new HashMap<String, Object>();
					comment.put("commentId", commentJson.get("c_id"));
					comment.put("userId", commentJson.get("userId"));
					comment.put("userName", commentJson.get("userName"));
					comment.put("commentContent", commentJson.get("CConent"));
					comment.put("reply", commentJson.get("reply"));
					comment.put("userId2", commentJson.get("userId2"));
					comment.put("userName2", commentJson.get("userName2"));
				}
				info.put("comments", comments);
				infos.add(info);
			}
		}catch(Exception e){
			
		}
		return infos;
	}
	
	//发表动态
	public static HashMap<String, Object> sendMessage(Context context, Map<String, String> texts, Map<String, String> files){
		String BOUNDARY="-----------------------132821742118716";
		HttpURLConnection connection=null;
		try{
			URL url=new URL(SEND_INFO);
			connection=(HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(300000);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0(Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+BOUNDARY);
			OutputStream os=connection.getOutputStream();
			
			//发送文本信息
			if(texts!=null){
				StringBuffer sb=new StringBuffer();
				for(Map.Entry<String, String> entry : texts.entrySet()){
					sb.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					sb.append("Content-Disposition:form-data;name=\""+entry.getKey()+"\"\r\n\r\n");
					sb.append(entry.getValue());
				}
				os.write(sb.toString().getBytes("utf-8"));
			}
			//发送参数和图片
			if(files!=null){				
				for(Map.Entry<String, String> entry : files.entrySet()){					
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					if(value==null){
						continue;
					}
					
					File file = new File(value);
					String filename = file.getName();
					String contentType = new MimetypesFileTypeMap().getContentType(file);
					if(contentType==null || contentType.equals("")){
						contentType = "application/octet-stream";
					}
					StringBuffer sb = new StringBuffer();
					sb.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					sb.append("Content-Disposition:form-data;name=\""+key+"\";filename=\""+filename+"\"\r\n");
					sb.append("Content-Type:"+contentType+"\r\n\r\n");
					os.write(sb.toString().getBytes());
					DataInputStream in = new DataInputStream(context.openFileInput(value));
					int len = 0;
					byte[] buffer = new byte[1024];
					while((len=in.read(buffer))!=-1){
						os.write(buffer, 0, len);
					}
					in.close();
				}
			}
			byte[] endData = ("\r\n--"+BOUNDARY+"--\r\n").getBytes();
			os.write(endData);
			os.flush();
			os.close();
			
			//读取服务器返回信息
			StringBuffer sb = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while((line=reader.readLine())!=null){
				sb.append(line).append("\n");
			}
			reader.close();
			reader = null;
			JSONObject resultJson=new JSONObject(sb.toString());
			Log.d("zz", "返回内容："+sb.toString());
			String result=resultJson.getString("result");
			if(result.equals("1")){
				JSONObject jsonObject=resultJson.getJSONObject("info");
				HashMap<String, Object> info=new HashMap<String, Object>();
				info.put("infoId", jsonObject.get("tId"));
				info.put("userId", jsonObject.get("userId"));
				info.put("userName", jsonObject.get("userName"));
				info.put("headImageUrl", "http://112.74.199.133:8080/shoe/"+jsonObject.get("headImage"));
				info.put("time", jsonObject.get("p_time"));
				info.put("content", jsonObject.get("content"));
				if(!jsonObject.get("image").equals("")){
					info.put("imageUrl", "http://112.74.199.133:8080/shoe/"+jsonObject.get("image"));
				}
				if(!jsonObject.get("thumbnail").equals("")){
					info.put("thumbnail", "http://112.74.199.133:8080/shoe/"+jsonObject.get("thumbnail"));
				}
				info.put("praise", "");
				ArrayList<HashMap<String, Object>> comments=new ArrayList<HashMap<String, Object>>();
				info.put("comments", comments);
				return info;
			}else{
				return null;
			}
			
		}catch(Exception e){
			Log.d("zz", "上传异常："+e.toString());
		}finally{
			if(connection!=null){
				connection.disconnect();
				connection=null;
			}
		}
		
		return null;
	}
	
	//发送评论
	public static HashMap<String, Object> sendComment(String userId, String infoId, String commentContent, String replyCommentId){
		try {
			HttpPost request = new HttpPost(SEND_COMMENT);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("comment.userId", userId));
			params.add(new BasicNameValuePair("comment.tId", infoId));
			params.add(new BasicNameValuePair("comment.CContent", commentContent));
			params.add(new BasicNameValuePair("comment.replyComm", replyCommentId));
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject resultJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			String result=resultJson.getString("result");
			if(result.equals("0")){
				return null;
			}else{
				HashMap<String, Object> comment=new HashMap<String, Object>();
				JSONObject jsonObject=resultJson.getJSONObject("info");
				comment.put("commentId", jsonObject.get("c_id"));
				comment.put("userId", jsonObject.get("userId"));
				comment.put("userName", jsonObject.get("userName"));
				comment.put("commentContent", jsonObject.get("CConent"));
				comment.put("reply", jsonObject.get("reply"));
				comment.put("userId2", jsonObject.get("userId2"));
				comment.put("userName2", jsonObject.get("userName2"));
				return comment;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	//点赞
	public static String praise(String messageId, String userId){
		String result=null;
		try {
			HttpPost request = new HttpPost(PRAISE);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("thought.tId", messageId));
			params.add(new BasicNameValuePair("thought.userId", userId));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject resultJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			result=resultJson.getString("result");
		} catch (Exception e) {
			// TODO: handle exception
		}	
		return result;
		
	}
	
	//删除动态
	public static String delete(String messageId, String userId){
		String result=null;
		try {
			HttpPost request = new HttpPost(DELETE);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("thought.tId", messageId));
			params.add(new BasicNameValuePair("thought.userId", userId));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject resultJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			result=resultJson.getString("result");
		} catch (Exception e) {
			// TODO: handle exception
		}	
		return result;
	}
	
	//获取指定用户的最新动态
	public static ArrayList<HashMap<String, Object>> getSpecificUserCommunityMessage(String userId){
		ArrayList<HashMap<String, Object>> infos = new ArrayList<HashMap<String, Object>>();
		try{			
			HttpPost request = new HttpPost(GET_SPECIFIC_USER_COMMUNITY_MESSAGE);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("thought.userId", userId));
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject infosJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			
			JSONArray infosJsonArray=infosJson.getJSONArray("info");
			for(int i=0; i<infosJsonArray.length(); i++){
				JSONObject infoJson=(JSONObject)infosJsonArray.opt(i);
				
				HashMap<String, Object> info=new HashMap<String, Object>();
				info.put("infoId", infoJson.get("tId"));
				info.put("userId", infoJson.get("userId"));
				info.put("userName", infoJson.get("userName"));
				info.put("headImageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("headImage"));
				info.put("time", infoJson.get("p_time"));
				info.put("content", infoJson.get("content"));
				if(!infoJson.get("image").equals("")){
					info.put("imageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("image"));
				}
				if(!infoJson.get("thumbnail").equals("")){
					info.put("thumbnail", "http://112.74.199.133:8080/shoe/"+infoJson.get("thumbnail"));
				}
				info.put("praise", infoJson.get("praise"));
				
				ArrayList<HashMap<String, Object>> comments=new ArrayList<HashMap<String, Object>>();
				JSONArray commentsJsonArray=infoJson.getJSONArray("comments");
				for(int j=0; j<commentsJsonArray.length(); j++){
					JSONObject commentJson=(JSONObject)commentsJsonArray.opt(j); 
					HashMap<String, Object> comment=new HashMap<String, Object>();
					comment.put("commentId", commentJson.get("c_id"));
					comment.put("userId", commentJson.get("userId"));
					comment.put("userName", commentJson.get("userName"));
					comment.put("commentContent", commentJson.get("CConent"));
					comment.put("reply", commentJson.get("reply"));
					comment.put("userId2", commentJson.get("userId2"));
					comment.put("userName2", commentJson.get("userName2"));
					comments.add(comment);
				}
				info.put("comments", comments);
				infos.add(info);
			}
			
		}catch(Exception e){
			Log.d("zz", e.toString());
			return null;
		}
		
		
		return infos;
		
	} 
	
	//获取指定用户更多社区动态
	public static ArrayList<HashMap<String, Object>> getSpecificUserMoreCommunityMessage(String userId, String lastTime){
		ArrayList<HashMap<String, Object>> infos=new ArrayList<HashMap<String,Object>>();
		try{
			HttpPost request = new HttpPost(GET_SPECIFIC_USER_MORE_COMMUNITY_MESSAGE);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("thought.userId", userId));
			params.add(new BasicNameValuePair("lastTime", lastTime));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject infosJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			
			JSONArray infosJsonArray=infosJson.getJSONArray("info");
			for(int i=0; i<infosJsonArray.length(); i++){
				JSONObject infoJson=(JSONObject)infosJsonArray.opt(i);
				
				HashMap<String, Object> info=new HashMap<String, Object>();
				info.put("infoId", infoJson.get("tId"));
				info.put("userId", infoJson.get("userId"));
				info.put("userName", infoJson.get("userName"));
				info.put("headImageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("headImage"));
				info.put("time", infoJson.get("p_time"));
				info.put("content", infoJson.get("content"));
				if(!infoJson.get("image").equals("")){
					info.put("imageUrl", "http://112.74.199.133:8080/shoe/"+infoJson.get("image"));
				}
				if(!infoJson.get("thumbnail").equals("")){
					info.put("thumbnail", "http://112.74.199.133:8080/shoe/"+infoJson.get("thumbnail"));
				}
				info.put("praise", infoJson.get("praise"));
				
				ArrayList<HashMap<String, Object>> comments=new ArrayList<HashMap<String, Object>>();
				JSONArray commentsJsonArray=infoJson.getJSONArray("comments");
				for(int j=0; j<commentsJsonArray.length(); j++){
					JSONObject commentJson=(JSONObject)commentsJsonArray.opt(j);
					HashMap<String, Object> comment=new HashMap<String, Object>();
					comment.put("commentId", commentJson.get("c_id"));
					comment.put("userId", commentJson.get("userId"));
					comment.put("userName", commentJson.get("userName"));
					comment.put("commentContent", commentJson.get("CConent"));
					comment.put("reply", commentJson.get("reply"));
					comment.put("userId2", commentJson.get("userId2"));
					comment.put("UserName2", commentJson.get("userName2"));
				}
				info.put("comments", comments);
				infos.add(info);
			}
		}catch(Exception e){
			
		}
		return infos;
	}
	
	//获取排行榜
	public static ArrayList<HashMap<String, Object>> getRankings(){		
		try{
			HttpPost request = new HttpPost(GET_RANKING);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject rankingJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			JSONArray jsonArray=rankingJson.getJSONArray("rank");
			ArrayList<HashMap<String, Object>> participants=new ArrayList<HashMap<String,Object>>();
			for(int i=0; i<jsonArray.length(); i++){
				JSONObject participantJson=jsonArray.optJSONObject(i);
				HashMap<String, Object> participant=new HashMap<String, Object>();
				participant.put("No.", participantJson.get("No."));
				participant.put("userId", participantJson.get("userId"));
				participant.put("userName", participantJson.get("userName"));
				participant.put("headImageUrl", "http://112.74.199.133:8080/shoe/"+participantJson.get("headImage"));
				participant.put("dailySteps", participantJson.get("dailyStep"));
				participants.add(participant);				
			}
			return participants;
		}catch(Exception e){
			Log.d("zz", e.toString());
		}
		return null;
	}
	
	//获取推荐商品
	public static ArrayList<HashMap<String, Object>> getRecommendedCommodities(){
		try{
			HttpPost request = new HttpPost(GET_RECOMMENDED_COMMODITIES);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject jsonObject=new JSONObject(EntityUtils.toString(response.getEntity()));
			JSONArray jsonArray=jsonObject.getJSONArray("recommend");
			ArrayList<HashMap<String, Object>> commodities=new ArrayList<HashMap<String,Object>>();
			for(int i=0; i<jsonArray.length(); i++){
				JSONObject commodityJson=jsonArray.getJSONObject(i);
				HashMap<String, Object> commodity=new HashMap<String, Object>();
				commodity.put("pictureUrl", "http://112.74.199.133:8080/shoe/"+commodityJson.get("productPic_url"));
				commodity.put("link", commodityJson.get("product_link"));
				commodities.add(commodity);
			}
			return commodities;
		}catch(Exception e){
			Log.d("zz", e.toString());
		}
		return null;
	}
	
	//获取展示商品
	public static ArrayList<HashMap<String, Object>> getDisplayCommodities(){
		try{
			HttpPost request = new HttpPost(GET_DISPLAY_COMMODITIES);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject jsonObject=new JSONObject(EntityUtils.toString(response.getEntity()));
			JSONArray jsonArray=jsonObject.getJSONArray("exhibition");
			ArrayList<HashMap<String, Object>> commodities=new ArrayList<HashMap<String,Object>>();
			for(int i=0; i<jsonArray.length(); i++){
				JSONObject commodityJson=jsonArray.getJSONObject(i);
				HashMap<String, Object> commodity=new HashMap<String, Object>();
				commodity.put("name", commodityJson.get("product_name"));
				commodity.put("price", commodityJson.get("price"));
				commodity.put("pictureUrl", "http://112.74.199.133:8080/shoe/"+commodityJson.get("productPic_url"));
				commodity.put("link", commodityJson.get("product_link"));
				commodities.add(commodity);
			}
			return commodities;
		}catch(Exception e){
			Log.d("zz", e.toString());
		}
		return null;
	}
	
	//上传步数
	public static void updateSteps(String userId, String userSteps){
		try {
			HttpPost request = new HttpPost(UPDATE_USER_STEPS);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("users.userId", userId));
			params.add(new BasicNameValuePair("users.dailyStep", userSteps));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			
			JSONObject resultJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			String result=resultJson.getString("result");
			if(result.equals("0")){
				Log.d("zz", "chenggong");
			}			
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("zz", e.toString());
		}
	}
	
	//三方登陆
	public static String loginWithOtherPlatform(String platformType, String openid){
		String userId=null;
		try {
			HttpPost request = new HttpPost(LOGIN_WITH_OTHER_PLATFORM);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("users.thirdParty", platformType));
			params.add(new BasicNameValuePair("users.accountNo", openid));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject resultJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			String result=resultJson.getString("result");
			if(result.equals("0")){
				userId=resultJson.getString("userId");
				return userId;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}	
		return userId;
		
	}
	
	//首页商品展示
	public static HashMap<String, Object> getAdvertisement(){
		try {
			HttpPost request = new HttpPost(GET_ADVERTISEMENT);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);			

			HttpResponse response=new DefaultHttpClient().execute(request);

			JSONObject resultJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			HashMap<String, Object> advertisement=new HashMap<String, Object>();
			advertisement.put("pictureUrl", "http://112.74.199.133:8080/shoe/"+resultJson.get("ad_pic"));
			advertisement.put("announcement", resultJson.get("slogan"));
			return advertisement;
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("zz", e.toString());
		}
		return null;
	}
	
	//发送反馈
	public static boolean sendFeedback(String userId, String feedback){
		try {
			HttpPost request = new HttpPost(SEND_FEEDBACK);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("suggest.userId", userId));
			params.add(new BasicNameValuePair("suggest.suggestContent", feedback));
			
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject resultJson=new JSONObject(EntityUtils.toString(response.getEntity()));
			String result=resultJson.getString("result");
			if(result.equals("1")){
				return true;
			}
		}catch (Exception e) {
				// TODO: handle exception
			Log.d("zz", e.toString());
		}
		return false;
		
	}
	
	//获取反馈
	public static ArrayList<HashMap<String, Object>> getFeedback(String userId){
		try{
			HttpPost request = new HttpPost(GET_FEEDBACK);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("suggest.userId", userId));
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject jsonObject=new JSONObject(EntityUtils.toString(response.getEntity()));
			String result=jsonObject.getString("result");
			if(result.equals("1")){
				JSONArray jsonArray=jsonObject.getJSONArray("info");
				ArrayList<HashMap<String, Object>> chat=new ArrayList<HashMap<String,Object>>();
				for(int i=0; i<jsonArray.length(); i++){
					JSONObject chatJson=jsonArray.getJSONObject(i);
					HashMap<String, Object> oneChat=new HashMap<String, Object>();
					oneChat.put("who", chatJson.get("speaker"));
					oneChat.put("content", chatJson.get("conent"));					
					chat.add(oneChat);
				}
				return chat;
			}				
		}catch(Exception e){
			Log.d("zz", e.toString());
		}
		return null;
		
	}
	
	//获取微信授权用户openid
	public static String getOpenId(String code){
		try{
			HttpPost request = new HttpPost(GET_OPEN_ID);
			request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			
			// 先封装一个 JSON 对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("appid", "wx1d4cc222dde1750c"));
			params.add(new BasicNameValuePair("secret", "ff6f9b7b1241a62e4de1e6a39c9f82ac"));
			params.add(new BasicNameValuePair("code", code));
			params.add(new BasicNameValuePair("grant_type", "authorization_code"));
			request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			
			HttpResponse response=new DefaultHttpClient().execute(request);
			JSONObject jsonObject=new JSONObject(EntityUtils.toString(response.getEntity()));
			String openid=jsonObject.getString("openid");Log.d("zz", "111"+openid);
			return openid;				
		}catch(Exception e){
			Log.d("zz", e.toString());
		}
		return null;
		
	}
	
}
