package com.huawei.esdk.uc.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.common.CommonVariables;
import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.ResponseCodeHandler.ResponseCode;
import com.huawei.common.constant.UCResource;
import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.MyOtherInfo;
import com.huawei.contacts.SelfData;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.demo.uc.R;
import com.huawei.device.DeviceManager;
import com.huawei.espace.framework.common.ThreadManager;
import com.huawei.espace.sharedprefer.AccountShare;
import com.huawei.service.ServiceProxy;
import com.huawei.service.login.LoginErrorResp;
import com.huawei.service.login.LoginM;
import com.huawei.utils.StringUtil;
import com.huawei.utils.net.ConnectInfo;

public class LoginActivity extends Activity implements OnClickListener
{
	private static final String TAG = LoginActivity.class.getSimpleName();
	private EditText etUsername;
	private EditText etPassword;
	private EditText etServerIP;
	private EditText etServerPort;
	private Button btnLogin;
	private String userName, password, serverIP, serverPort;
	private Context mContext;
	private IntentFilter filter;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		initCompenent();
		mContext = this;

		filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);
		filter.addAction(CustomBroadcastConst.ACTION_LOGIN_ERRORACK);

		// 注册广播
		registerRec();
	}

	protected void initCompenent()
	{
		etServerIP = (EditText) findViewById(R.id.serverip);
		etServerPort = (EditText) findViewById(R.id.serverport);
		etUsername = (EditText) findViewById(R.id.username);
		etPassword = (EditText) findViewById(R.id.password);
		btnLogin = (Button) findViewById(R.id.btn_login);

		btnLogin.setOnClickListener(this);

		SharedPreferences sharedInfo = getSharedPreferences("UserData", Activity.MODE_PRIVATE);

		serverIP = sharedInfo.getString("serverip", "172.22.8.61");
		serverPort = sharedInfo.getString("serverport", "7801");
		userName = sharedInfo.getString("username", "");
		password = sharedInfo.getString("password", "");
		etServerIP.setText(serverIP);
		etServerPort.setText(serverPort);
		etUsername.setText(userName);
		etPassword.setText(password);

	}

	/**
	 * 注册广播
	 */
	private void registerRec()
	{
		LocalBroadcastManager.getInstance(mContext).registerReceiver(loginReceiver, filter);
	}

	/**
	 * 注销广播
	 */
	private void unRegisterRec()
	{
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(loginReceiver);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{

		case R.id.btn_login:
			serverIP = etServerIP.getText().toString();
			serverPort = etServerPort.getText().toString();
			userName = etUsername.getText().toString();
			password = etPassword.getText().toString();
			if (TextUtils.isEmpty(serverIP))
			{
				ShowPrompt("服务器ip地址不能为空");
				return;
			}
			if (TextUtils.isEmpty(serverPort))
			{
				ShowPrompt("服务器端口号不能为空");
				return;
			}
			if (TextUtils.isEmpty(userName))
			{
				ShowPrompt("用户名不能为空");
				return;
			}
			if (TextUtils.isEmpty(password))
			{
				ShowPrompt("密码不能为空");
				return;
			}
			showProDialog();
			// 登录操作，连接eSpace服务器，执行登录操作
			UCAPIApp.getApp().callWhenServiceConnected(new Runnable()
			{

				@Override
				public void run()
				{
					ServiceProxy service = UCAPIApp.getApp().getService();
					if (null == service)
					{
						return;
					}
					ThreadManager.getInstance().addToFixedThreadPool(new Runnable()
					{

						@Override
						public void run()
						{
							// 登录
							login(serverIP, serverPort, userName, password);
						}
					});
				}
			});

			break;

		default:
			break;
		}
	}

	public void ShowPrompt(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private void login(String ip, String port, String account, String password)
	{
		//设置为标准模式，新的SDK功能；如果设置为省电模式，联系人状态等无法出来
		AccountShare.getIns().setLoginUser(account);
		SelfData data = SelfDataHandler.getIns().getSelfData();
		data.setPowerMode(UCResource.POWER_MODE_STANDARD);

		ServiceProxy service = UCAPIApp.getApp().getService();
		if (null == service)
		{
			return;
		}

		String deviceId = DeviceManager.getDeviceId();
		String timeStamp = UserData.TimeStamp;
		String configTimestamp = UserData.ConfigTimestamp;
		String language = DeviceManager.getLocalLanguage();
		String version = UserData.Version;

		ConnectInfo connectInfo = new ConnectInfo();
		connectInfo.setServerAddress(ip);
		connectInfo.setServerProt(Integer.parseInt(port));

		connectInfo.setSVNEnable(false);
		connectInfo.setSvnServerAddress("");
		connectInfo.setSvnServerPort(StringUtil.stringToInt(""));
		connectInfo.setSvnAccount("");
		connectInfo.setSvnPassword("");

		CommonVariables.getIns().setVoipSupport(true);

		LoginM loginM = new LoginM();
		loginM.setConnectInfo(connectInfo);
		loginM.setAccount(account);
		loginM.setValue(password);
		loginM.setCurrentVersion(version);
		loginM.setLanguage(language);
		loginM.setDevicId(deviceId);
		loginM.setTimestamp(timeStamp);
		loginM.setConfigTimestamp(configTimestamp);
		loginM.setOs("Android");
		loginM.setUmAbility(true);
		loginM.setGroupAbility(true);

		service.login(loginM);

	}

	/**
	 * 广播接收器
	 */
	private BroadcastReceiver loginReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			closeProDialog();
			String action = intent.getAction();
			if (CustomBroadcastConst.ACTION_CONNECT_TO_SERVER.equals(action))
			{
				boolean respData = intent.getBooleanExtra(UCResource.SERVICE_RESPONSE_DATA, false);
				Log.d(TAG, "ACTION_CONNECT_TO_SERVER | response " + String.valueOf(respData));
				if (respData)
				{
					ShowPrompt("connect to server success");
					saveUserData();

					// 保存当前用户上线状态（系统自带方法）
					SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.ON_LINE, false);

					Intent intent2 = new Intent();
					intent2.setClass(mContext, MainActivity.class);
					startActivity(intent2);
					LoginActivity.this.finish();
				}
				else
				{
					ShowPrompt("connect to server failed");

					SharedPreferences sharedInfo = getSharedPreferences(UserData.UserData, Activity.MODE_PRIVATE);
					sharedInfo.edit().putBoolean(UserData.IsLogin, false).commit();

				}
			}
			else if (CustomBroadcastConst.ACTION_LOGIN_ERRORACK.equals(action))
			{
//				ShowPrompt("failed login");
                LoginErrorResp errorData = (LoginErrorResp) intent
                        .getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);

                if (ResponseCode.FORCEUPDATE == errorData.getStatus())
                {

                }
                else if (ResponseCode.OTHERLOGIN == errorData.getStatus())
                {
                    MyOtherInfo.OtherLoginType otherLoginType = errorData.getOtherLoginType();
                    if (otherLoginType != MyOtherInfo.OtherLoginType.NULL
                            && otherLoginType != MyOtherInfo.OtherLoginType.MOBILE)
                    {
                        onLoginOtherTerminal(otherLoginType);
                    }
                }
                else
                {
                    // TODO 异常场景 -1等
                    Toast.makeText(
                            LoginActivity.this,
                            "login filed | ResponseCode = "
                                    + errorData.getStatus() + "["
                                    + errorData.getDesc() + "]",
                            Toast.LENGTH_SHORT).show();
                }

			}

		}
	};

    private void onLoginOtherTerminal(MyOtherInfo.OtherLoginType loginType)
    {
        String desc = null;

        // 是否根据服务器返回错误码给相应提示语
        switch (loginType)
        {
            case PC:
                desc = getString(R.string.login_on_pc_desc);
                break;
            case MOBILE:
                desc = getString(R.string.login_on_mobile_desc);
                break;
            case WEB:
                desc = getString(R.string.login_on_web_desc);
                break;
            case PAD:
                desc = getString(R.string.login_on_pad_desc);
                break;
            case IPPHONE:
                desc = getString(R.string.login_on_ipphone_desc);
                break;
            default:
                break;
        }
        if (desc == null)
        {
            return;
        }
        // 弹框提示已在其他终端登录
        Toast.makeText(LoginActivity.this,
                "login filed | ResponseCode = " + desc, Toast.LENGTH_SHORT)
                .show();
    }

	/**
	 * 为了简便操作，登录成功后保存帐户信息，一切以实际情况
	 */
	private void saveUserData()
	{

		SharedPreferences sharedInfo = getSharedPreferences(UserData.UserData, Activity.MODE_PRIVATE);
		Editor editor = sharedInfo.edit();

		editor.putString("username", etUsername.getText().toString().trim());
		editor.putString("password", etPassword.getText().toString());
		editor.putString("serverip", etServerIP.getText().toString());
		editor.putString("serverport", etServerPort.getText().toString().trim());
		// 用户上线状态保存在本地，登出时判断用
		editor.putBoolean(UserData.IsLogin, true);

		editor.commit();
	}

	/**
	 * 注销广播操作
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unRegisterRec();

	}

	private void showProDialog()
	{
		if (null == progressDialog)
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在登录,请稍后。。。");
			progressDialog.setCanceledOnTouchOutside(false);
			if (!progressDialog.isShowing())
			{
				progressDialog.show();
			}
		}
	}

	private void closeProDialog()
	{
		if (null != progressDialog && progressDialog.isShowing())
		{
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

}
