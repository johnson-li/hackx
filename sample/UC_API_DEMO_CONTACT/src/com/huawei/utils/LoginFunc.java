package com.huawei.utils;

import com.huawei.common.CommonVariables;
import com.huawei.common.constant.UCResource;
import com.huawei.contacts.SelfData;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.data.ExecuteResult;
import com.huawei.device.DeviceManager;
import com.huawei.esdk.uc.application.UCAPIApp;
import com.huawei.espace.sharedprefer.AccountShare;
import com.huawei.logindemo.R;
import com.huawei.service.ServiceProxy;
import com.huawei.service.login.LoginM;
import com.huawei.utils.net.ConnectInfo;

import android.util.Log;

public class LoginFunc {
	 private static LoginFunc instance;
	 private boolean isLogin = false;
	
	 private LoginFunc()
	 {
		 
	 }
	 public  static LoginFunc getIns()
	 {
		 if(null == instance)
		 {
			 instance = new LoginFunc();
		 }
		 return instance;
	 }
	 
	 public void setLogin(boolean isLogin)
	    {
	        this.isLogin = isLogin;
	    }

	    public boolean isLogin()
	    {
	        return isLogin;
	    }
	    
	    /**
	     * 登出
	     */
	    public boolean logout()
	    {
	        ServiceProxy service = UCAPIApp.getApp().getService();

	        if (null == service)
	        {
	            return false;
	        }

	        ExecuteResult result = service.logout(true);
	        if (result != null)
	        {
	            return result.isResult();
	        }
	        return false;
	    }
	 
	 /**
	     * 登录
	     * @param account
	     * @param pwd
	     */
	    public void login(String url,int port,String account, String pwd)
	    {
			AccountShare.getIns().setLoginUser(account);
			SelfData sedata = SelfDataHandler.getIns().getSelfData();
			sedata.setPowerMode(UCResource.POWER_MODE_STANDARD);

	        ServiceProxy service = UCAPIApp.getApp().getService();

	        if (null == service)
	        {
	            return;
	        }

	        String devicId = DeviceManager.getDeviceId();
	        String language = DeviceManager.getLocalLanguage();
	        String version = UCAPIApp.getApp().getString(
	                R.string.androidversion);
	        String timestamp = "00000000000000";
	        String configTimeStamp = "00000000000000";
	        
//	        SelfData data = SelfDataHandler.getIns().getSelfData();
	        ConnectInfo connectInfo = new ConnectInfo();
	        connectInfo.setServerAddress(url);
	        connectInfo.setServerProt(port);
	     
	        connectInfo.setSVNEnable(false);
	        connectInfo.setSvnServerAddress("");
	        connectInfo.setSvnServerPort(StringUtil.stringToInt(""));
	        connectInfo.setSvnAccount("");
	        connectInfo.setSvnPassword("");
	        
//	      //发登录请求前，读取VoIP开关配置，并设置到SDK配置项(不知道在干嘛)
//	        boolean voipSwitch = SelfDataHandler.getIns().getSelfData()
//	                .isVoIPSwitchFlag();
	        CommonVariables.getIns().setVoipSupport(true);
	        
	        LoginM loginM = new LoginM();
	        loginM.setConnectInfo(connectInfo);
	        loginM.setAccount(account);
	        loginM.setValue(pwd);
	        loginM.setCurrentVersion(version);   
	        loginM.setLanguage(language);
	        loginM.setTimestamp(timestamp);
	        loginM.setConfigTimestamp(configTimeStamp);
	        loginM.setDevicId(devicId);
	        loginM.setOs("Android");
	        loginM.setUmAbility(true);
	        loginM.setGroupAbility(true);
	        
	        service.login(loginM);
	        Log.d("LoginFunc", "is loginning over");
	    }

}
