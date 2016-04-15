
package com.shoes.constants; 

import com.example.shoes.R;

public class VirtualCallContants {
	
	public static final int CALL_ON_TIME_30_SECONDS=1;
	public static final int CALL_ON_TIME_1_MINUTE=2;
	public static final int CALL_ON_TIME_5_MINUTES=3;
	public static final int CALL_ON_TIME_10_MINUTES=4;
	public static final int CALL_ON_TIME_CUSTOM=5;
	private static final String callOnTime1="30秒后来电";
	private static final String callOnTime2="1分钟后来电";
	private static final String callOnTime3="5分钟后来电";
	private static final String callOnTime4="10分钟来电";
	private static final String callOnTime5="自定义";
	
	public static String getCallOnTimeString(int which){
		switch (which) {
		case 1:
			return callOnTime1;
		case 2:
			return callOnTime2;			
		case 3:
			return callOnTime3;			
		case 4:
			return callOnTime4;
		case 5:
			return callOnTime5;
		}
		return callOnTime1;
	}
			

	public static final int NOTIFY_STYLE_VOICE=1;
	public static final int NOTIFY_STYLE_VIBRATION=2;
	public static final int NOTIFY_STYLE_VOICE_AND_VIBRATION=3;
	private static final String notifyVoice="仅铃声";
	private static final String notifyVibration="仅震动";
	private static final String notifyVoiceAndVIbration="铃声+震动";
	
	public static String getNotifyString(int which){
		switch (which) {
		case 1:
			return notifyVoice;
		case 2:
			return notifyVibration;
		case 3:
			return notifyVoiceAndVIbration;
		}
		return notifyVoice;
	}
	
	public static final int CONTACTER_RANDOM=1;
	public static final int CONTACTER_APPOINT=2;
	public static final int CONTACTER_CUSTOM=3;
	private static final String contacterRandom="通讯录随机";
	private static final String contacterChoose="通讯录选择";
	private static final String contacterCustom="自定义联系人";
	
	public static String getContacterString(int which){
		switch (which) {
		case 1:
			return contacterRandom;
		case 2:
			return contacterChoose;
		case 3:
			return contacterCustom;
		}
		return contacterRandom;
	}
	
	public static final int WALLPAPER_ANDROID=R.drawable.wallpaper_default1;
	public static final int WALLPAPER_IOS=R.drawable.wallpaper_default2;
	public static final int WALLPAPER_CUSTOM=3;
	private static final String wallpaperAndroid="Android";
	private static final String wallpaperIOS="IOS";
	private static final String wallpaperCustom="自定义壁纸";
	public static String getWallpaperString(int which){
		switch (which) {
		case R.drawable.wallpaper_default1:
			return wallpaperAndroid;
			
		case R.drawable.wallpaper_default2:
			return wallpaperIOS;
		case 3:
			return wallpaperCustom;		
		}
		return wallpaperIOS;
	}
	
	
	public static final int VOICE_MAN=1;
	public static final int VOICE_WOMAN=2;
	private static final String voiceMan="恶搞男声";
	private static final String voiceWoman="恶搞女声";
	
	public static String getVoiceString(int which){
		switch (which) {
		case 1:
			return voiceMan;
		case 2:
			return voiceWoman;
		
		}
		return voiceMan;
	}
	
}
