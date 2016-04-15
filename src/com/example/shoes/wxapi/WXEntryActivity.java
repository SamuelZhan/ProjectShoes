package com.example.shoes.wxapi;

import com.shoes.server.HttpServer;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

	private IWXAPI wxApi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		wxApi=WXAPIFactory.createWXAPI(this, "wx1d4cc222dde1750c", false);
		wxApi.registerApp("wx1d4cc222dde1750c");
		wxApi.handleIntent(getIntent(), this);
	}

	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResp(BaseResp result) {
		// TODO Auto-generated method stub
		int loginResult = ((SendAuth.Resp) result).errCode;
		String code=null;
		if(loginResult==0){
			code= ((SendAuth.Resp) result).code;
		}
		Intent intent=new Intent();
		intent.putExtra("result", loginResult);
		intent.putExtra("code", code);
		intent.setAction("weixin_login_successfully");
		sendBroadcast(intent);
		finish();
	}

}
