package com.huawei.esdk.uc.demo.im.function;

import com.huawei.common.CommonVariables;
import com.huawei.common.res.LocContext;
import com.huawei.contacts.SelfData;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.data.ExecuteResult;
import com.huawei.device.DeviceManager;
import com.huawei.esdk.uc.demo.im.R;
import com.huawei.esdk.uc.demo.im.application.data.UCAPIApp;
import com.huawei.esdk.uc.demo.im.self.SelfInfoUtil;
import com.huawei.module.anyoffice.SVNUtil;
import com.huawei.service.EspaceService;
import com.huawei.service.ServiceProxy;
import com.huawei.service.login.LoginM;
import com.huawei.utils.StringUtil;
import com.huawei.utils.net.ConnectInfo;

public class LoginFunc
{
    private static LoginFunc instance;

    private boolean isLogin = false;

    private LoginFunc()
    {

    }

    public static LoginFunc getIns()
    {
        if (null == instance)
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
     * 登录
     * @param account
     * @param pwd
     */
    public void login(String account, String pwd)
    {
        ServiceProxy service = UCAPIApp.getApp().getService();

        if (null == service)
        {
            return;
        }

        String devicId = DeviceManager.getDeviceId();
        String language = DeviceManager.getLocalLanguage();
        String version = LocContext.getContext().getString(
                R.string.androidversion);
        String timestamp = "00000000000000";
        String configTimeStamp = "00000000000000";
        
        SelfData data = SelfDataHandler.getIns().getSelfData();
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setServerAddress(data.getServerUrl());
        connectInfo.setServerProt(StringUtil.stringToInt(data
                .getServerPort()));
        connectInfo.setSvnServerAddress(data.getSvnIp());
        connectInfo.setSvnServerPort(StringUtil.stringToInt(""));
        connectInfo.setSVNEnable(data.isSvnFlag());
        connectInfo.setSvnAccount("");
        connectInfo.setSvnPassword("");
        
      //发登录请求前，读取VoIP开关配置，并设置到SDK配置项
        boolean voipSwitch = SelfDataHandler.getIns().getSelfData()
                .isVoIPSwitchFlag();
        CommonVariables.getIns().setVoipSupport(voipSwitch);
        
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

    public void backToLoginView()
    {
        // 清除在线状态
        SelfInfoUtil.getIns().setToLogoutStatus();
        if (null != EspaceService.getService())
        {
            UCAPIApp.getApp().stopEspaceService(true);
        }
        
        
        
        

    }

}
