package com.huawei.esdk.uc.demo.im.goim;

import java.util.ArrayList;

import com.huawei.common.CommonVariables;
import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.UCResource;
import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.PersonalContact;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.data.SearchContactsResp;
import com.huawei.data.base.BaseResponseData;
import com.huawei.device.DeviceManager;
import com.huawei.esdk.uc.demo.im.BaseActivity;
import com.huawei.esdk.uc.demo.im.ChatActivity;
import com.huawei.esdk.uc.demo.im.R;
import com.huawei.esdk.uc.demo.im.application.data.IntentData;
import com.huawei.esdk.uc.demo.im.application.data.UCAPIApp;
import com.huawei.esdk.uc.demo.im.function.ImFunc;
import com.huawei.esdk.uc.demo.im.login.LoginFunc;
import com.huawei.service.ServiceProxy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ToIMActivity extends BaseActivity
{

	private EditText espaceNumEt;
	private Button chatBtn;
	private PersonalContact currentContact = null;
	private Dialog exitDialog;
	private ProgressDialog progressDialog;
	private String name;
	private Button btnLogin;
	
	@Override
	public void initializeData()
	{
		setContentView(R.layout.activity_toim);
		init();

	}
	private void init()
	{
		espaceNumEt = (EditText) findViewById(R.id.et_espace_num);
		chatBtn = (Button) findViewById(R.id.btn_chat);

		chatBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{

				name = espaceNumEt.getText().toString();
				requestPersonalDetail(name);

				Handler handler = new Handler();
				handler.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						if (currentContact != null)
						{
							Intent intent = new Intent(ToIMActivity.this,
									ChatActivity.class);
							intent.putExtra(IntentData.ESPACENUMBER,
									currentContact);
							startActivity(intent);
							
						}
					}
				}, 1000);

			}
		});

		btnLogin = (Button) findViewById(R.id.logout_btn);
		btnLogin.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				showExitDialog();
			}
		});
	}

	private void requestPersonalDetail(String eSpaceNumber)
	{
		ServiceProxy serviceProxy = UCAPIApp.getApp().getService();
		if (null != serviceProxy)
		{
			serviceProxy.searchContact(eSpaceNumber, true, 1, 50, true);
		}
	}

	private void registerLocalBroadcast()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				broadcastReceiver, filter);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			if (CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE
					.equals(action))
			{

				handleSearchContactResponse(intent);

			}
		}
	};

	/**
	 * 查询联系人的具体操作
	 * 
	 * @param intent
	 */
	private void handleSearchContactResponse(Intent intent)
	{

		if (TextUtils.isEmpty(name))
		{
			currentContact = null;
			Toast.makeText(ToIMActivity.this, "输入账号不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (CommonVariables.getIns().getUserAccount()
				.equals(name))
		{
			currentContact = null;
			Toast.makeText(ToIMActivity.this, "不能与自己聊天", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		int result = intent.getIntExtra(UCResource.SERVICE_RESPONSE_RESULT, 0);
		if (UCResource.REQUEST_OK != result)
		{
			currentContact = null;
			Toast.makeText(ToIMActivity.this, "查询失败", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		BaseResponseData data = (BaseResponseData) intent
				.getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);
		if (data != null && data instanceof SearchContactsResp)
		{
			SearchContactsResp contactsResp = (SearchContactsResp) data;
			if (contactsResp.getContacts() != null)
			{
				ArrayList<PersonalContact> contacts = contactsResp
						.getContacts();
				if (null != contacts && !contacts.isEmpty())
				{

					if ((name).equals(contacts.get(
							0).getEspaceNumber()))
					{
						currentContact = contacts.get(0);
					} else
					{
						currentContact = null;
						Toast.makeText(ToIMActivity.this, "未查找到联系人",
								Toast.LENGTH_SHORT).show();
					}

				}

				else
				{
					currentContact = null;
					Toast.makeText(ToIMActivity.this, "未查找到联系人",
							Toast.LENGTH_SHORT).show();
				}
			} else
			{
				currentContact = null;
			}
		} else
		{
			currentContact = null;
		}

	};

	@Override
	protected void onResume()
	{

		registerLocalBroadcast();
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				broadcastReceiver);
		super.onPause();
	}


	@Override
	public void initializeComposition()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clearData()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			showExitDialog();
		}

		return super.onKeyDown(keyCode, event);
	}

	private void showExitDialog()
	{
		exitDialog = new Dialog(ToIMActivity.this,R.style.exit_dialog);
		exitDialog.setContentView(R.layout.dialog_common_d);

		((Button)exitDialog.findViewById(R.id.left_btn)).setOnClickListener(listener);
		((Button)exitDialog.findViewById(R.id.right_btn)).setOnClickListener(listener);

		exitDialog.show();
	}

	/**
	 * 等待对话框
	 */
	private void showProgerssDlg()
	{
		if (null == progressDialog)
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在登出，请稍后······");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeDialog()
	{
		if (null != progressDialog && progressDialog.isShowing())
		{
			progressDialog.dismiss();
		}
	}

	private OnClickListener listener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.left_btn:
					showProgerssDlg();
					if(LoginFunc.getIns().isLogin())
					{
						exitDialog.dismiss();
						new Handler().postDelayed(new Runnable()
						{

							@Override
							public void run()
							{
								closeDialog();
								LoginFunc.getIns().setLogin(false);
								SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.AWAY,false);
								UCAPIApp.getApp().getService().logout(true);
								UCAPIApp.getApp().stopEspaceService(true);
								UCAPIApp.getApp().popAllExcept(null);

								DeviceManager.killProcess();
							}
						}, 5*1000);
						if(LoginFunc.getIns().logout())
						{
							showProgerssDlg();
							return;
						}
					}
					LoginFunc.getIns().setLogin(false);
					SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.AWAY,false);
					UCAPIApp.getApp().stopEspaceService(true);
					UCAPIApp.getApp().popAllExcept(null);

					DeviceManager.killProcess();

					break;
				case R.id.right_btn:

					if(exitDialog.isShowing())
					{
						exitDialog.dismiss();
					}

					break;
				default:
					break;
			}
		}
	};
}
