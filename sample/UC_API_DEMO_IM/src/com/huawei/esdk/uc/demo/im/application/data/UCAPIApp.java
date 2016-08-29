package com.huawei.esdk.uc.demo.im.application.data;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.huawei.application.BaseApp;
import com.huawei.common.LocalBroadcast;
import com.huawei.common.constant.UCResource;
import com.huawei.common.os.EventHandler;
import com.huawei.common.res.LocContext;
import com.huawei.config.param.SDKConfigParam;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.esdk.uc.control.data.proc.DataProc;
import com.huawei.esdk.uc.demo.im.UCAPIService;
import com.huawei.esdk.uc.demo.im.function.VoipFunc;
import com.huawei.esdk.uc.demo.im.function.VoipNotification;
import com.huawei.espace.framework.common.ThreadManager;
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

		LocalBroadcast.getIns().registerBroadcastProc(DataProc.getIns().getProc());

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
            startUCAPIService();
            Log.d(CommonUtil.APPTAG, TAG + " | startESpaceService");

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
    
    private void startUCAPIService()
    {
        Intent service = new Intent(LocContext.getContext().getApplicationContext(), UCAPIService.class);
        LocContext.getContext().getApplicationContext().startService(service);
    }
    
    private void stopUCAPIService()
    {
        Intent service = new Intent(LocContext.getContext().getApplicationContext(), UCAPIService.class);
        LocContext.getContext().getApplicationContext().stopService(service);
    }
    public synchronized void stopEspaceService(boolean clearData)
    {
        if (mServiceStarted)
        {
            stopUCAPIService();
            Log.d(CommonUtil.APPTAG, TAG
                    + " | stop ImService because there's no active connections");

            if (serviceConnected())
            {
                Log.d(CommonUtil.APPTAG, TAG + " | unbindService ImService ");
               
                /**更换SDK 后修改*/
//                serviceProxy.stopService(clearData);
                EspaceService.stopService(clearData);
                unbindService(mImServiceConn);
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

            VoipFunc.getIns().registerVoip(new VoipNotification());
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
    
    /**
     * 本地广播管理器：
     * 1、只在本应用范围内传播，防止隐私数据泄露；
     * 2、只在本应用范围内传播，防止其他应用伪造数据；
     * 3、比全局广播更高效；
     */
    private LocalBroadcastManager localBroadcastManager;
    private GlobalBroadCastReceiver receiver;
    
    /**
     * 注册全局广播接受类
     */
    private void registerGlobalBroadcastReceiver()
    {
        receiver = new GlobalBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        // 不需要null检查，一定为非null类型，findbugs
        for (String action : LocalBroadcast.getIns().getAllBroadcast())
        {
            filter.addAction(action);
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        if (localBroadcastManager != null)
        {
            localBroadcastManager.registerReceiver(receiver, filter);
        }
    }

	/**
     * 全局的接受广播类
     */
    private static class GlobalBroadCastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            ThreadManager.getInstance().addToBroadcastThread(new MyRunnable(intent));
        }

        private static class MyRunnable implements Runnable
        {
            private Intent intent;

            public MyRunnable(Intent intent)
            {
                this.intent = intent;
            }

            @Override
            public void run()
            {
                LocalBroadcast.getIns().onBroadcastReceive(intent);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}