package com.huawei.esdk.uc.demo.conf.function;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.device.DeviceManager;
import com.huawei.esdk.uc.demo.conf.R;
import com.huawei.esdk.uc.demo.BaseActivity;
import com.huawei.esdk.uc.demo.conf.application.UCAPIApp;
import com.huawei.esdk.uc.demo.conf.widget.SlippingViewGroup;

public class ConfMainActivity extends BaseActivity implements OnClickListener
{

	private SlippingViewGroup viewGroup;
	private ImageView ivCreate;
	private PopupWindow createPopupWindow;
	private ConfListView confListView;
	private PopupWindow pop;
	private View mainView;
	private Button btnLogout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_confmainactivity);
		mainView = getLayoutInflater().inflate(R.layout.activity_confmainactivity , null);
		setContentView(mainView);
		initView();

	}

	private void initView()
	{

		ivCreate = (ImageView) findViewById(R.id.create_img);
		// 滑动视图
		viewGroup = (SlippingViewGroup) findViewById(R.id.slippingGroup);
		btnLogout = (Button)findViewById(R.id.logout_btn);

		// 会议的View
		confListView = new ConfListView(this);
		viewGroup.addView(confListView);

		ivCreate.setOnClickListener(this);
		btnLogout.setOnClickListener(this);
	}

	@Override
	public void initializeData()
	{
		// TODO Auto-generated method stub

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
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.logout_btn:
			logout();
			break;
		case R.id.create_img:
			showCreatePopup(v);
			break;
			case R.id.btn_dialog_cancle:
				if (pop != null)
				{
					pop.dismiss();
				}

				break;
			case R.id.btn_dialog_logout:
				boolean isLogin = getSharedPreferences("UserData", Activity.MODE_PRIVATE)
						.getBoolean("islogin", false);
				if (isLogin)
				{
					// 保存当前用户下线状态
					SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.AWAY, true);
					UCAPIApp.getApp().getService().logout(true);
//					UCAPIApp.getApp().stopEspaceService(true);
					UCAPIApp.getApp().stopEspaceService(true);
					DeviceManager.killProcess();
				}
				getSharedPreferences("UserData", Activity.MODE_PRIVATE).edit().putBoolean("islogin", false)
						.commit();
				SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.AWAY, true);
//				UCAPIClipApp.getApp().stopEspaceService(true);
				UCAPIApp.getApp().stopEspaceService(true);
				DeviceManager.killProcess();

				if (pop != null)
				{
					pop.dismiss();
				}
				break;

		default:
			break;
		}

	}

	private void showCreatePopup(View anchor)
	{
		if (null == createPopupWindow)
		{
			View view = getLayoutInflater()
					.inflate(R.layout.create_popup, null);
			TextView createConf = (TextView) view
					.findViewById(R.id.create_conf);

			createConf.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					toCreateConf();
					createPopupWindow.dismiss();
				}
			});

			createPopupWindow = new PopupWindow(view,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			createPopupWindow.setOutsideTouchable(true);
		}

		if (!createPopupWindow.isShowing())
		{
			createPopupWindow.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bg_dialog));
			createPopupWindow.showAsDropDown(anchor);
		}
	}

	private void toCreateConf()
	{
		Intent intent = new Intent(ConfMainActivity.this,
				ConfCreateActivity.class);

		startActivity(intent);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		ConferenceFunc.getIns().requestConferenceList();

		UCAPIApp.getApp().popAllExcept(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == event.KEYCODE_BACK)
		{
			logout();
			return  false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void logout()
	{
		View contentView = LayoutInflater.from(this).inflate(R.layout.logout_popwindow, null);
		Button btn_cancle = (Button) contentView.findViewById(R.id.btn_dialog_cancle);
		Button btn_logout = (Button) contentView.findViewById(R.id.btn_dialog_logout);
		pop = new PopupWindow(contentView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);

		btn_cancle.setOnClickListener(this);
		btn_logout.setOnClickListener(this);
		pop.showAtLocation(mainView, Gravity.CENTER, 0, 0);
	}
}
