package com.huawei.esdk.uc.demo.conf.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.ResponseCodeHandler;
import com.huawei.common.constant.UCResource;
import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.MyOtherInfo;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.esdk.uc.demo.Constants;
import com.huawei.esdk.uc.demo.conf.R;
import com.huawei.esdk.uc.demo.conf.application.UCAPIApp;
import com.huawei.esdk.uc.demo.conf.function.ConfMainActivity;
import com.huawei.esdk.uc.demo.conf.function.LoginFunc;
import com.huawei.esdk.uc.demo.conf.function.data.ConferenceDataHandler;
import com.huawei.esdk.uc.demo.conf.utils.ToastUtil;
import com.huawei.espace.framework.common.ThreadManager;
import com.huawei.service.ServiceProxy;
import com.huawei.service.login.LoginErrorResp;

public class LoginActivity extends Activity
{

	private static final String TAG = Constants.GTAG
			+ LoginActivity.class.getSimpleName();
	private EditText edServerIP;
	private EditText edServerPort;
	private EditText edUsername;
	private EditText edPassword;
	private Button btnLogin;
	private IntentFilter filter;
	private String userName, password, serverIP, serverPort;
	/**
	 * 登录进度对话框
	 */
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initView();

		filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);
		filter.addAction(CustomBroadcastConst.ACTION_LOGIN_ERRORACK);
		registerRec();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unRegisterRec();
	}


	private void initView()
	{
		edServerIP = (EditText) findViewById(R.id.et_serverip);
		edServerPort = (EditText) findViewById(R.id.et_serverport);
		edUsername = (EditText) findViewById(R.id.et_username);
		edPassword = (EditText) findViewById(R.id.et_password);

		btnLogin = (Button) findViewById(R.id.btn_login);

		btnLogin.setOnClickListener(listener);

		SharedPreferences sharedInfo = getSharedPreferences("UserData", Activity.MODE_PRIVATE);

		serverIP = sharedInfo.getString("serverip", "172.22.8.61");
		serverPort = sharedInfo.getString("serverport", "7801");
		userName = sharedInfo.getString("username", "");
		password = sharedInfo.getString("password", "");
		edServerIP.setText(serverIP);
		edServerPort.setText(serverPort);
		edUsername.setText(userName);
		edPassword.setText(password);
	}

	private void registerRec()
	{
		LocalBroadcastManager.getInstance(this).registerReceiver(loginReceiver,
				filter);
	}

	private void unRegisterRec()
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				loginReceiver);
	}

	private void login()
	{
		UCAPIApp.getApp().callWhenServiceConnected(new Runnable()
		{
			@Override
			public void run()
			{
				ServiceProxy mService = UCAPIApp.getApp().getService();
				if (null == mService)
				{

					return;
				}
				ThreadManager.getInstance().addToFixedThreadPool(new Runnable()
				{
					@Override
					public void run()
					{
						SelfDataHandler.getIns().getSelfData().setServerUrl(serverIP);
						SelfDataHandler.getIns().getSelfData().setServerPort(serverPort);

						LoginFunc.getIns().login(userName, password);
					}
				});
			}
		});

	}

	private OnClickListener listener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.btn_login:

				serverIP = edServerIP.getText().toString();
				serverPort = edServerPort.getText().toString();
				userName = edUsername.getText().toString();
				password = edPassword.getText().toString();
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
				showProgressDialog();
				login();
				break;

			default:
				break;
			}
		}
	};

	public void ShowPrompt(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private BroadcastReceiver loginReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{

			String action = intent.getAction();
			Log.i(TAG, "onReceive action = " + action);

			if (CustomBroadcastConst.ACTION_CONNECT_TO_SERVER.equals(action))
			{
				closeDialog();
				boolean respData = intent.getBooleanExtra(UCResource.SERVICE_RESPONSE_DATA, false);
				if (respData)
				{
					ToastUtil.showToast(LoginActivity.this, "connect to server success");

					saveUserData();
					// 保存当前用户上线状态（系统自带方法）
					SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.ON_LINE, false);
					//初始化会议列表
					ConferenceDataHandler.getIns().initConfList();

					startActivity(new Intent(LoginActivity.this,
							ConfMainActivity.class));

					LoginActivity.this.finish();
				}
				else
				{
					ToastUtil.showToast(LoginActivity.this, "connect to server failed");
					SharedPreferences sharedInfo = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
					sharedInfo.edit().putBoolean("islogin", false).commit();

				}
			}
			else if (CustomBroadcastConst.ACTION_LOGIN_ERRORACK.equals(action))
			{
				closeDialog();
				LoginErrorResp errorData = (LoginErrorResp) intent
						.getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);

				if (ResponseCodeHandler.ResponseCode.FORCEUPDATE == errorData.getStatus())
				{

				}
				else if (ResponseCodeHandler.ResponseCode.OTHERLOGIN == errorData.getStatus())
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
     * 进度对话框
     */
    private void showProgressDialog()
    {
        if (null == progressDialog)
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                   
                }
            });
        }
        
            progressDialog.setMessage("登录中，请稍后..");
       
        if (!progressDialog.isShowing())
        {
            progressDialog.show();
        }
    }
    
    /**
     * 关闭进度框
     */
    private void closeDialog()
    {
        if (null != progressDialog && progressDialog.isShowing())
        {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

	/**
	 * 为了简便操作，登录成功后保存帐户信息，一切以实际情况
	 */
	private void saveUserData()
	{

		SharedPreferences sharedInfo = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedInfo.edit();

		editor.putString("username", edUsername.getText().toString().trim());
		editor.putString("password", edPassword.getText().toString());
		editor.putString("serverip", edServerIP.getText().toString());
		editor.putString("serverport", edServerPort.getText().toString().trim());
		// 用户上线状态保存在本地，登出时判断用
		editor.putBoolean("islogin", true);

		editor.commit();
	}

}
