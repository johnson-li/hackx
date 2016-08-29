package com.huawei.esdk.uc.application;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.huawei.application.BaseApp;
import com.huawei.common.LocalBroadcast;
import com.huawei.common.constant.UCResource;
import com.huawei.common.os.EventHandler;
import com.huawei.common.res.LocContext;
import com.huawei.config.param.SDKConfigParam;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.esdk.uc.control.data.proc.CommonProc;
import com.huawei.photoUtils.CommonUtil;
import com.huawei.push.ExceptionHandler;
import com.huawei.service.EspaceService;
import com.huawei.service.ServiceProxy;
import com.huawei.utils.AndroidLogger;
import com.huawei.utils.io.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UCAPIApp extends Application
{

    private static final String TAG = UCAPIApp.class.getSimpleName();

    private static UCAPIApp instance = null;

    private ServiceProxy serviceProxy = null;

    private boolean mServiceStarted = false;

    private Stack<Activity> activityStack = new Stack<Activity>();

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

		LocalBroadcast.getIns().registerBroadcastProc(new CommonProc());

		Logger.setLogger(new AndroidLogger());
		BaseApp.registerDefaultLocalReceiver();
	}

    public ServiceProxy getService()
    {
        return serviceProxy;
    }

    private synchronized void startEspaceService()
    {

        if (!mServiceStarted)
        {
//            startUCAPIService();
            Log.d(CommonUtil.APPTAG, TAG + " | startESpaceService");

            Intent intent = new Intent(LocContext.getContext().getApplicationContext(), EspaceService.class);
    		intent.putExtra(UCResource.EXTRA_CHECK_AUTO_LOGIN, false);
    		intent.putExtra(UCResource.EXTRA_BROADCAST_RECEIVER_PERMISSION,
    				"com.huawei.eSpaceMobileApp");
    		intent.putExtra(UCResource.EXTRA_PROTOCOL_VERSION, 3);
    		intent.putExtra(UCResource.EXTRA_CHAT_NOTIFICATION, false);
    		intent.putExtra(UCResource.HTTP_LOG_PATH, FileUtil.getSdcardPath());

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
    		LocContext.getContext().getApplicationContext().startService(intent);
    		LocContext.getContext().getApplicationContext().bindService(intent, mImServiceConn, Context.BIND_AUTO_CREATE);
        }
        else
        {
            synchronized (mQueue)
            {
                for (Message msg : mQueue)
                {
                    msg.sendToTarget();
                }
                mQueue.clear();
            }
        }
    }
    
    public void stopEspaceService()
    {
    	stopEspaceService(true);
    }
    
    public synchronized void stopEspaceService(boolean clearData)
    {
		if (mServiceStarted)
		{
//			stopUCAPIService();
			Log.d(CommonUtil.APPTAG, TAG
					+ " | stop ImService because there's no active connections");

			if (serviceConnected())
			{
				Log.d(CommonUtil.APPTAG, TAG + " | unbindService ImService ");
				EspaceService.stopService(clearData);
				LocContext.getContext().getApplicationContext().unbindService(mImServiceConn);
				serviceProxy = null;
			}

			mServiceStarted = false;
		}

    }

    /**
     * 内部类建立与SDK服务的连接
     **/
    private final ServiceConnection mImServiceConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.d(CommonUtil.APPTAG, TAG + " | onServiceConnected");

            mServiceStarted = true;

            if (!(service instanceof EspaceService.ServiceBinder))
            {
                return;
            }
            // 获取代理服务
            serviceProxy = ((EspaceService.ServiceBinder) service).getService();

            synchronized (mQueue)
            {
                for (Message msg : mQueue)
                {
                    msg.sendToTarget();
                }
                mQueue.clear();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d(CommonUtil.APPTAG, TAG + " | onServiceDisconnected");
            serviceProxy = null;
            mServiceStarted = false;
        }
    };

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

    private boolean serviceConnected()
    {
        return null != serviceProxy;
    }

    public void addActivity(Activity activity)
    {
        Log.d(CommonUtil.APPTAG, TAG + " | addActivity | activity = " + activity);
        activityStack.push(activity);
    }

    public void popWithoutFinish(Activity activity)
    {
        Log.d(CommonUtil.APPTAG, TAG + " | popWithoutFinish | activity = "
                + activity);
        Activity temp = activityStack.pop();
        if (temp != activity)
        {
            activityStack.push(temp);
        }
    }

    public void popActivity(Activity activity)
    {
        Log.d(CommonUtil.APPTAG, TAG + " | popActivity | activity = " + activity);
        if (activity != null)
        {
            activityStack.removeElement(activity);
            activity.finish();
        }
    }

    public Activity getCurActivity()
    {
        Log.d(CommonUtil.APPTAG, TAG + " | getCurActivity");
        if (!activityStack.isEmpty())
        {

            Activity currentActivity = activityStack.lastElement();
            if (null == currentActivity)
            {
                activityStack.pop();
                currentActivity = getCurActivity();
            }
            return currentActivity;
        }
        return null;
    }

    public void popAllExcept(Activity activity)
    {
        Log.d(CommonUtil.APPTAG, TAG + " | popAllExcept | activity = " + activity);
        int size = activityStack.size();
        Activity temp;
        for (int i = 0; i < size; i++)
        {
            temp = activityStack.pop();
            if (null != temp && temp != activity)
            {
                temp.finish();
            }
        }
        if (null != activity)
        {
            activityStack.push(activity);
        }
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}