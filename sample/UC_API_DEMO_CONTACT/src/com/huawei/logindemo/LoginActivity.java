package com.huawei.logindemo;

import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.ResponseCodeHandler.ResponseCode;
import com.huawei.common.constant.UCResource;
import com.huawei.contacts.MyOtherInfo;
import com.huawei.esdk.uc.application.UCAPIApp;
import com.huawei.espace.framework.common.ThreadManager;
import com.huawei.service.ServiceProxy;
import com.huawei.service.login.LoginErrorResp;
import com.huawei.utils.LoginFunc;
import com.huawei.utils.TestData;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private EditText ip;
	private EditText port;
	private EditText name;
	private EditText pwd;
	private CheckBox rememberBox;
	private Button register;
	private IntentFilter filter;
	private Context mContent;
	private ProgressDialog progressDialog;
	 private Handler startServiceHandler = new Handler();
	private static final String TAG = LoginActivity.class.getSimpleName();

	private String serverUrl;
	private String serverPort;
	private String username;
	private String password;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContent = this;

		// 初始化控件
		initViews();

		// 注册广播
		filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);
		filter.addAction(CustomBroadcastConst.ACTION_LOGIN_ERRORACK);
		registerRec();
	}

	private void registerRec() {
		LocalBroadcastManager.getInstance(mContent).registerReceiver(loginReceiver, filter);
	}

	private void unRegisterRec() {
		LocalBroadcastManager.getInstance(mContent).unregisterReceiver(loginReceiver);
	}

	/**
	 * 等待对话框和停止等待对话框
	 */
	private void showDialg() {
		if (null == progressDialog) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("登录中，请稍后");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	private BroadcastReceiver loginReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "broadcast receiver --- action =" + action);
			// 如果广播是已连接服务器
			if (CustomBroadcastConst.ACTION_CONNECT_TO_SERVER.equals(action)) {
				boolean respData = intent.getBooleanExtra(UCResource.SERVICE_RESPONSE_DATA, false);
				Log.d(TAG, "broadcast receiver --- respData =" + respData);
				closeDialog();
				if (respData) {
					Log.d(TAG, "broadcast receiver --- respData2 =" + respData);

					// 设置登录成功
					LoginFunc.getIns().setLogin(true);
					startActivity(new Intent(LoginActivity.this, MainActivity.class));

					LoginActivity.this.finish();

					// 保存账号密码
					boolean isCheck = rememberBox.isChecked();
					if (isCheck) {
						saveAccount();
					} else {
						clearAccount();
					}
				} else {
					// 不设置状态，不停止服务，直接提示
					Toast.makeText(LoginActivity.this, "connect to server failed", Toast.LENGTH_LONG).show();
				}
			}

			// 如果广播是连接错误
			else if (CustomBroadcastConst.ACTION_LOGIN_ERRORACK.equals(action)) {
				closeDialog();

				// 根据 判断错误类型
				LoginErrorResp errorData = (LoginErrorResp) intent
						.getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);
				if (ResponseCode.FORCEUPDATE == errorData.getStatus()) {
					
					/**新的sdk对应的demo未做处理*/
//					UCAPIApp.getApp().getService().continueLogin();
				}

				// 其他的错误类型先不管了
				else if (ResponseCode.OTHERLOGIN == errorData.getStatus()) {
					MyOtherInfo.OtherLoginType otherLoginType = errorData.getOtherLoginType();
					if (otherLoginType != MyOtherInfo.OtherLoginType.NULL
							&& otherLoginType != MyOtherInfo.OtherLoginType.MOBILE)
					{
						onLoginOtherTerminal(otherLoginType);
					}
				} else {
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

		// 保存账号密码
		private void saveAccount() {
			SharedPreferences account = getSharedPreferences("account", MODE_PRIVATE);
			SharedPreferences.Editor editor = account.edit();
			editor.putString("ip",ip.getText().toString());
			editor.putString("port",port.getText().toString());
			editor.putString("username", name.getText().toString());
			editor.putString("pwd", pwd.getText().toString());
			editor.commit();
		}

		// 删除账号密码
		private void clearAccount() {
			SharedPreferences account = getSharedPreferences("account", MODE_PRIVATE);
			SharedPreferences.Editor editor = account.edit();
			editor.clear();
		}

	};

	/**
	 * 初始化控件，如果保存账号密码则赋值 如果没有保存则使用默认值
	 * 
	 */

	private void initViews()
	{
		ip = (EditText) findViewById(R.id.edit_ip);
		port = (EditText) findViewById(R.id.edit_port);
		name = (EditText) findViewById(R.id.edit_input);
		pwd = (EditText) findViewById(R.id.pwd);
		rememberBox = (CheckBox) findViewById(R.id.checkBox1);
		rememberBox.setChecked(true);
		register = (Button) findViewById(R.id.register);
		register.setOnClickListener(this);
		getAccount();
	}

	private void getAccount() {
		SharedPreferences sharedPreferences = getSharedPreferences("account", MODE_PRIVATE);
		String ipNum = sharedPreferences.getString("ip", TestData.URL);
		String portNum = sharedPreferences.getString("port", TestData.PORT);
		String username = sharedPreferences.getString("username", TestData.ACCOUNT_MYSELF);
		String password = sharedPreferences.getString("pwd", TestData.ACCOUNT_PSW);

		ip.setText(ipNum);
		port.setText(portNum);
		name.setText(username);
		pwd.setText(password);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register:
			login();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterRec();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void login()
	{
		serverUrl = ip.getText().toString();
		serverPort = port.getText().toString();
		username = name.getText().toString();
		password = pwd.getText().toString();
		if (TextUtils.isEmpty(serverUrl))
		{
			Toast.makeText(LoginActivity.this, "服务器地址不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(serverPort))
		{
			Toast.makeText(LoginActivity.this, "服务器端口号不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(username))
		{
			Toast.makeText(LoginActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(password))
		{
			Toast.makeText(LoginActivity.this, "用户密码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		showDialg();
		UCAPIApp.getApp().callWhenServiceConnected(new Runnable() {
			public void run() {

				ServiceProxy mService = UCAPIApp.getApp().getService();

				if (null == mService) {
					closeDialog();
					return;
				}


				ThreadManager.getInstance().addToFixedThreadPool(new Runnable() {

					@Override
					public void run() {
						int port = Integer.parseInt(serverPort);
						LoginFunc.getIns().login(serverUrl,port,username,password);
						Log.d(TAG, "is loginning");
					}
				});
			}

		});

	}
	

}
