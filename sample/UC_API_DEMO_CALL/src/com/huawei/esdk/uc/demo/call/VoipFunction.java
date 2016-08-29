package com.huawei.esdk.uc.demo.call;

import java.util.LinkedList;

import com.huawei.common.BaseBroadcast;
import com.huawei.common.BaseReceiver;
import com.huawei.common.res.LocContext;
import com.huawei.contacts.ContactCache;
import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.ContactTools;
import com.huawei.contacts.PersonalContact;
import com.huawei.esdk.uc.demo.call.media.MediaActivity;
import com.huawei.esdk.uc.demo.call.video.VideoFunction;
import com.huawei.service.EspaceService;
import com.huawei.voip.CallManager;
import com.huawei.voip.CallSession;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

public class VoipFunction extends BaseBroadcast
{

	private static final String TAG = VoipFunction.class.getSimpleName();
	private CallSession callSession;
	private CallManager callManager;

	/** 初始状态 */
	public static final int STATUS_INIT = 0;

	/** 呼叫状态 **/
	public static final int STATUS_CALLING = 1;
	
	 /** 通话状态 **/
    public static final int STATUS_TALKING = 2;

	/** Voip状态 */
	private int voipStatus = STATUS_INIT;
	
	/**CTD呼叫方式*/
	public static final int CTD = 1;

	/** 呼叫类型为VOIP */
	public static final int VOIP = 2;
	/** 呼叫类型 */
	private int callType = VOIP;

	/** 呼叫连接成功 **/
	public static final String CALL_CONNECTED = "call_connected";

	/** 呼叫保持 **/
	public static final String CALL_HOLD = "call_hold";

	public static final String CALL_HOLD_FAILED = "call_hold_failed";

	/** 呼叫连接成功后的操作 **/
	public static final String CALL_CLOSED = "call_closed";

	public static final String VIDEO_INVITE = "call_video_invite";

	public static final String VIDEO_ADD_SUCESS = "call_video_add_sucess";

	public static final String VIDEO_ADD_FAILED = "call_video_add_failed";

	public static final String VIDEO_REMOVE = "call_video_remove";

	public static final String GOTO_CONF_VIEW = "call_transto_conference";

	public static final String REFRESH_LOCAL_VIEW = "call_refresh_local_view";

	public static final String REFRESH_REMOTE_VIEW = "call_refresh_remote_view";

	public static final String VIDEO_CHANGE_ORIENT = "call_video_change_orient";
	/** 是否是视频通话 **/
	private boolean isVideo = false;
	// //////////////////////// 呼叫类型 //////////////////////////////////////
	/** 来电 **/
	public static final int CALL_COME = 1;

	/** 呼出 **/
	public static final int CALL_OUT = 2;

	/** 呼叫模式 **/
	private int callMode = VoipFunction.CALL_OUT;

	private static VoipFunction instance = null;

	public static VoipFunction getInstance()
	{

		if (null == instance) 
		{
			instance = new VoipFunction();
		}
		return instance;
	}

	public int getCallType() 
	{
		return callType;
	}

	public void setCallType(int callType) 
	{
		this.callType = callType;
	}

	public CallSession getCallSession()
	{
		return callSession;
	}

	public void setCallSession(CallSession callSession)
	{
		this.callSession = callSession;
	}

	public boolean isVideo()
	{
		return isVideo;
	}

	public void setVideo(boolean isVideo) 
	{

		if (!ContactLogic.getIns().getAbility().isVideoCallAbility())
		{
			this.isVideo = false;
			return;
		}

		if (isVideo && callSession != null) 
		{
			VideoFunction.getIns().setCallId(callSession.getSessionId());
		}

		if (isVideo == this.isVideo) 
		{
			return;
		}

		if (isVideo)
		{
			prepareVideoCall();
		} 
		else 
		{
			clearAfterVideoCallEnd();
		}
		this.isVideo = isVideo;
	}

	private void clearAfterVideoCallEnd() 
	{
		VideoFunction.getIns().clearSurfaceView();
	}

	public int getVoipStatus() 
	{
		return voipStatus;
	}

	public void setVoipStatus(int voipStatus)
	{
		this.voipStatus = voipStatus;
	}

	public int getCallMode()
	{
		return callMode;
	}

	public void setCallMode(int callMode) 
	{
		this.callMode = callMode;
	}

	private VoipFunction() 
	{
		initBroadcasts();
	}

	/**
	 * 注册广播
	 */
	private void initBroadcasts() 
	{
		broadcasts.put(CALL_CONNECTED, new LinkedList<BaseReceiver>());
		broadcasts.put(CALL_HOLD, new LinkedList<BaseReceiver>());
		broadcasts.put(CALL_CLOSED, new LinkedList<BaseReceiver>());
	}

	/**
	 * 初始化视频界面面的view
	 * 
	 */
	public void prepareVideoCall() 
	{
		// 必须放到ui线程来执行.
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				if (callSession != null) {
					VideoFunction.getIns().deploySessionVideoCaps();
				}
			}
		});
	}

	/**
	 * 用户在发起视频呼叫时初始化视频部件
	 * 
	 * @param isVideo
	 */
	public void initVideo(boolean isVideo) 
	{
		if (!ContactLogic.getIns().getAbility().isVideoCallAbility())
		{
			this.isVideo = false;
			return;
		}

		if (isVideo == this.isVideo) 
		{
			return;
		}

		if (isVideo) 
		{
			VideoFunction.getIns().deploySessionVideoCaps();
		} 
		else 
		{
			clearAfterVideoCallEnd();
		}

		this.isVideo = isVideo;
	}

	/**
	 * (VoipNotification方法)根据id发送广播
	 * 
	 * @param id
	 */
	public void postSipNotification(String id) 
	{
		sendBroadcast(id, null);
	}

	public CallManager getCallManager() 
	{
		return EspaceService.getCallManager();
	}

	/**
	 * 语音呼叫
	 * 
	 * @param calledNumber
	 */
	public void makeCall(String calledNumber)
	{
		PersonalContact contact = getContactByNumber(calledNumber);
		String displayName = calledNumber;
		if (contact != null)
		{
			displayName = getDisplayName(contact);
		}
		showMediaActivity(true, calledNumber, displayName, contact);
	}

	/**
	 * 视频呼叫
	 * 
	 * @param calledNumber
	 */
	public void makeVideoCall(String calledNumber) 
	{
		PersonalContact contact = getContactByNumber(calledNumber);
		String displayName = calledNumber;
		if (contact != null)
		{
			displayName = getDisplayName(contact);
		}
		VoipFunction.getInstance().initVideo(true);
		VoipFunction.getInstance().showMediaActivity(true, calledNumber, displayName, contact);
	}

	/**
	 * 通过服务号码获取到联系人信息
	 * 
	 * @param number
	 * @return personalContact
	 */
	public PersonalContact getContactByNumber(String number)
	{
		if (TextUtils.isEmpty(number))
		{
			return null;
		}

		PersonalContact contact = ContactCache.getIns().getContactByNumber(number);
		if (contact == null)
		{
			contact = ContactLogic.getIns().getContactByPhoneNumber(number);
		}
		return contact;
	}

	/**
	 * 获取联系人显示的名称
	 * 
	 * @param pc
	 * @return
	 */
	public String getDisplayName(PersonalContact pc)
	{
		return ContactTools.getDisplayName(pc, null, null);
	}

	/**
	 * 通讯
	 * 
	 * @param needMakeCall
	 * @param callNum
	 * @param displayName
	 * @param contact
	 */
	public void showMediaActivity(boolean needMakeCall, String callNum, String displayName, PersonalContact contact)
	{
		Intent intent = new Intent(LocContext.getContext(), MediaActivity.class);
		intent.putExtra("needMakeCall", needMakeCall);
		intent.putExtra("callNumber", callNum);
		intent.putExtra("displayName", displayName);
		intent.putExtra("contact", contact);
		intent.putExtra("isVideo", isVideo);

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		LocContext.getContext().startActivity(intent);
	}

	/**
	 * @param calledNum 通信对方的软终端号码
	 * @param isVideo 发起类型是否为视频通话 true：是   false：不是
	 * @return
	 */
	public boolean doVoipCall(String calledNum, boolean isVideo)
	{
		// 获取CallManager对象
		CallManager cm = getCallManager(); 
		PersonalContact pc = ContactCache.getIns().getContactByNumber(calledNum);
		String domain = (pc == null) ? null : pc.getDomain();

		CallSession callSession = null;
		if (cm != null)
		{
			callSession = cm.makeCall(calledNum, domain, isVideo);
		}

		setCallSession(callSession);
		if (isVideo && callSession != null) 
		{
			VideoFunction.getIns().setCallId(callSession.getSessionId());
			VideoFunction.getIns().deployGlobalVideoCaps();
		}

		if (callSession != null) 
		{
			// SelfInfoUtil.getIns().setStatus(PersonalContact.BUSY);
			// 电话呼出状态
			VoipFunction.getInstance().setCallMode(CALL_OUT);
			VoipFunction.getInstance().setVoipStatus(VoipFunction.STATUS_CALLING);
			return true;
		}

		return false;
	}

	/**
	 * 接听
	 */
	public boolean answer(boolean isVideo)
	{
		if (callSession != null) 
		{
			return callSession.answer(isVideo);
		}
		return false;
	}

	/**
	 * 挂断
	 */
	public void hangup() 
	{
		if (callSession != null)
		{
			callSession.hangUp(false);
		}
	}

	/**
	 * 视频通话关闭
	 * 
	 * @return
	 */
	public boolean closeVideo()
	{
		boolean ret = false;
		if (callSession != null)
		{
			ret = callSession.removeVideo();
			VoipFunction.getInstance().setVideo(false);
		}
		return ret;
	}

	/**
	 * 语音通话升级为视频通话
	 * 
	 * @return
	 */
	public boolean upgradeVideo() 
	{
		boolean ret = false;
		if (callSession != null)
		{
			ret = callSession.addVideo();
		}

		if (ret) 
		{
			setVideo(true);
		}
		return ret;
	}

	/**
	 * 不同意升级为视频通话
	 */
	public void declineVideoInvite() 
	{
		if (callSession != null)
		{
			callSession.disagreeVideoUpdate();
		}
	}

	/**
	 * 同意升级视频通话 agreeVideoUpdate
	 */
	public void agreeVideoUpgradte() 
	{
		if (callSession != null) 
		{
			callSession.agreeVideoUpdate();
		}
	}

	/**
	 * 注册VOIP 等待响应
	 * 
	 * @param ipVoipNotification
	 */
	public void registerVoip(VoipNotification ipVoipNotification) 
	{
		// 获取CallManager对象
		CallManager cm = getCallManager(); 

		if (cm != null && ipVoipNotification != null)
		{
			// 注册回调消息
			cm.registerNofitication(ipVoipNotification); 
		}
	}

	/**
	 * 注销VOIP
	 * 
	 * @param needWaitResult
	 *            需要等待注销的响应回来才反初始化组件
	 */
	public void unRegisterVoip(boolean needWaitResult) 
	{
		// 获取CallManager对象
		CallManager cm = getCallManager(); 

		if (cm != null) 
		{
			cm.unRegister();
		}

		// unRegNofitication();
	}

}
