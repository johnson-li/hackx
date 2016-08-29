package com.huawei.esdk.uc.demo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.multidex.MultiDex;

import com.huawei.application.BaseApp;
import com.huawei.common.constant.UCResource;
import com.huawei.common.os.EventHandler;
import com.huawei.common.res.LocContext;
import com.huawei.config.param.SDKConfigParam;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.esdk.uc.demo.call.VoipFunction;
import com.huawei.esdk.uc.demo.call.VoipNotification;
import com.huawei.push.ExceptionHandler;
import com.huawei.service.EspaceService;
import com.huawei.service.ServiceProxy;
import com.huawei.utils.AndroidLogger;
import com.huawei.utils.io.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class UCAPIApp extends Application
{
	private static final String TAG = UCAPIApp.class.getSimpleName();

	public static UCAPIApp instance = null;

	public ServiceProxy serviceProxy = null;

	private boolean mServiceStarted = false;

	private final List<Message> mQueue = new ArrayList<Message>();

	public static void setApp(UCAPIApp app)
	{
		instance = app;
	}

	public static UCAPIApp getApp()
	{
		if (instance == null)
		{
			return instance = new UCAPIApp();
		}
		return instance;
	}
	@Override
	public void onCreate()
	{
		setApp(this);
		super.onCreate();
		doOnCreate(this);
	}
	public void doOnCreate(Application app)
	{
		BaseApp.initData(app);

		//启动Application时初始化，否则其他地方引用可能崩溃（未注册便在主线程调用引起的崩溃）
		EventHandler.getIns();

		if (BaseApp.isPushProcess(LocContext.getContext()))
		{
			Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
			return;
		}
		Logger.setLogger(new AndroidLogger());
		BaseApp.registerDefaultLocalReceiver();
	}

	public ServiceProxy getService()
	{
		return serviceProxy;
	}

	public void callWhenServiceConnected(Runnable callback)
	{
		Message msg = Message.obtain(new Handler(), callback);

		if (serviceConnected())
		{
			msg.sendToTarget();
		}
		else
		{
			startEspaceService();

			synchronized (mQueue)
			{
				mQueue.add(msg);
			}
		}
	}

	/**
	 * 启动eSpace服务
	 */
	private synchronized void startEspaceService()
	{

		Intent intent = new Intent(LocContext.getContext().getApplicationContext(), EspaceService.class);
		intent.putExtra(UCResource.EXTRA_CHECK_AUTO_LOGIN, false);
		intent.putExtra(UCResource.EXTRA_BROADCAST_RECEIVER_PERMISSION,
				"com.huawei.eSpaceMobileApp");
		intent.putExtra(UCResource.EXTRA_PROTOCOL_VERSION, 3);
		intent.putExtra(UCResource.EXTRA_CHAT_NOTIFICATION, false);
		intent.putExtra(UCResource.HTTP_LOG_PATH,
				FileUtil.getSdcardPath());

		/** configServiceParam方法弃用，再次添加by lwx302895 start */
		SDKConfigParam param = new SDKConfigParam();
		SelfDataHandler handler = SelfDataHandler.getIns();
		param.setVoipSupport(handler.getSelfData().isVoIPSwitchFlag());
		param.setVoipSupport(handler.getSelfData().isVoIPSwitchFlag());
		param.addAbility(SDKConfigParam.Ability.CODE_OPOUS);
		param.addAbility(SDKConfigParam.Ability.VOIP_2833);
		param.addAbility(SDKConfigParam.Ability.VOIP_VIDEO);
		param.setClientType(SDKConfigParam.ClientType.UC_MOBILE);
		param.setMegTypeVersion((short) 3);
		param.setHttpLogPath(FileUtil.getSdcardPath());
		intent.putExtra(UCResource.SDK_CONFIG, param);
		/** by lwx303895 end */

		LocContext.getContext().startService(intent);
		LocContext.getContext().bindService(intent, mImServiceConn, Context.BIND_AUTO_CREATE);

	}
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	private final ServiceConnection mImServiceConn = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			mServiceStarted = true;
			if (!(service instanceof EspaceService.ServiceBinder))
			{
				return;
			}

			// 获取代理服务 ServiceProxy
			serviceProxy = ((EspaceService.ServiceBinder) service).getService();

			//注册VOIP回调
			VoipFunction.getInstance().registerVoip(new VoipNotification());

			synchronized (mQueue)
			{
				for (Message message : mQueue)
				{
					message.sendToTarget();
				}
				mQueue.clear();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			serviceProxy = null;
			mServiceStarted = false;
		}

	};

	private boolean serviceConnected()
	{
		return null != serviceProxy;
	}

	public synchronized void stopEspaceService(boolean clearData)
	{
		if (mServiceStarted)
		{
			if (serviceProxy != null)
			{
				EspaceService.stopService(clearData);
				LocContext.getContext().getApplicationContext().unbindService(mImServiceConn);
				serviceProxy = null;
			}
			mServiceStarted = false;
		}

	}

}
