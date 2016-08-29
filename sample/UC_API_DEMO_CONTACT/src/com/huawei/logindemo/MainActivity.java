package com.huawei.logindemo;

import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.contact.ContactView;
import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.device.DeviceManager;
import com.huawei.discussGroup.DiscussGroupActivity;
import com.huawei.discussGroup.GroupMemberAddActivity;
import com.huawei.esdk.uc.application.UCAPIApp;
import com.huawei.service.ServiceProxy;
import com.huawei.utils.LoginFunc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	
	public static Context mContext;
	
	private ContactView contactView;
	
	private LinearLayout viewGroup;
	
	private IntentFilter filter;
	
	private ImageView ivSearch;
	
	private ImageView ivHead;
	
	private ImageView ivCreate;
	
	private PopupWindow popupWindow;
	
	private Dialog exitDialog;
	
	private ProgressDialog progressDialog;
	 
	public static int SYNCMODE_ALL = 2;

	private Button btnLogout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_second_new);
		mContext = this;
		
		initView();
		
		
		//广播
		filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_LOGINOUT_SUCCESS);
		filter.addAction(CustomBroadcastConst.ACTION_SET_STATUS_RESPONSE);
		filter.addAction(CustomBroadcastConst.UPDATE_CONTACT_VIEW);
		// filter.addAction(CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE);
		filter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);

		registerRec();

		//同步好友
		loadContact(SYNCMODE_ALL);
	}

	private void initView() 
	{
		ivSearch = (ImageView) findViewById(R.id.search_img);
		ivHead = (ImageView) findViewById(R.id.head);
		ivCreate = (ImageView) findViewById(R.id.create_img);
		btnLogout = (Button) findViewById(R.id.logout_btn);
		
		// 没有工具栏，不加入滑动视图.仅仅拉去联系人的姓名，头像先不要,动画点击事件都先不加
		viewGroup = (LinearLayout) findViewById(R.id.viewGroup);

		// 联系人
		contactView = new ContactView(this);
		viewGroup.addView(contactView);
		
		ivCreate.setOnClickListener(this);
		ivSearch.setOnClickListener(this);
		btnLogout.setOnClickListener(this);
	}

	/**
	 * 加载联系人
	 * */
	private void loadContact(int syncMode) {
		
		ServiceProxy serviceProxy = UCAPIApp.getApp().getService();
		
		if (null == serviceProxy)
        {
            return;
		}
		serviceProxy.loadContacts(syncMode);
	}
	/**
	 * 创建popupwindow
	 * */
	private void getPopupWindow(View v) {
		
		if(null != null)
		{
			popupWindow.dismiss();
			return;
		}
		else 
		{
			initPopwindow(v);
		}
	}
	
	/**
	 * 初始化popupwindow
	 * */
	private void initPopwindow(View v) {
		if (null == popupWindow) {
			View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.create_popup, null);
			TextView createDiscuss = (TextView) view.findViewById(R.id.create_discuss);
			createDiscuss.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(new Intent(MainActivity.this,GroupMemberAddActivity.class));
					popupWindow.dismiss();
				}
			});
			
			TextView showDiscuss = (TextView) view.findViewById(R.id.show_discuss);
			showDiscuss.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					startActivity(new Intent(MainActivity.this,DiscussGroupActivity.class));
					popupWindow.dismiss();
				}
			});
			
			popupWindow = new PopupWindow(view,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			popupWindow.setOutsideTouchable(true);
		}
		if(!popupWindow.isShowing())
		{
			popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_dialog));
			popupWindow.showAsDropDown(v);
		}
	}
	
	private void registerRec() {
		LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);
	}

	// 接收所有广播，取出action
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			
			String action = intent.getAction();
			
			if(CustomBroadcastConst.UPDATE_CONTACT_VIEW.equals(action))
			{
				Log.i("UPDATE_CONTACT_VIEW", "UPDATE_CONTACT_VIEW*****************");
				
				//不加载讨论组和聊天记录
				contactView.updateContactsList();
				
			}
			
		}
		
	};
	
	 private void unRegisterRec()
	    {
//	        unregisterReceiver(receiver);
	        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
	    }
	 
	 

	@Override
	protected void onResume() {
		super.onResume();
		if(contactView != null)
		{
			contactView.updateContactsList();
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
			showExitDialog();
		}
		
		return super.onKeyDown(keyCode, event);
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
	
	private void showExitDialog() 
	{
		exitDialog = new Dialog(MainActivity.this,R.style.exit_dialog);
		exitDialog.setContentView(R.layout.dialog_common);
		
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
	
	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.search_img:
			
			startActivity(new Intent(MainActivity.this,SearchActivity.class));
			break;
			
		case R.id.create_img:
			getPopupWindow(v);
			break;
		case R.id.logout_btn:
			showExitDialog();
			break;
		default:
			break;
		}
	}

	
	
}
