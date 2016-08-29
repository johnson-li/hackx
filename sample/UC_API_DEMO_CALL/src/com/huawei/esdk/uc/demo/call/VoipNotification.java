package com.huawei.esdk.uc.demo.call;

import com.huawei.contacts.ContactCache;
import com.huawei.contacts.PersonalContact;
import com.huawei.esdk.uc.demo.call.video.VideoFunction;
import com.huawei.voip.CallSession;
import com.huawei.voip.IpCallNotification;
import com.huawei.voip.data.EventData;
import com.huawei.voip.data.VoiceMailNotifyData;
import com.huawei.voip.data.VoiceQuality;

import android.util.Log;
import common.TupCallParam;
import tupsdk.TupCall;

public class VoipNotification implements IpCallNotification 
{
	private static final String TAG = VoipNotification.class.getSimpleName();
	public static final int REFRESH_LOCAL_ADD = 1;

	@Override
	public void onAudioPlayEnd(int arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onAudioPlayEnd");
	}

	/**
	 *
	 * 语音升级为视频
	 * @param session
	 */
	@Override
	public void onCallAddVideo(CallSession session)
	{
		Log.d(TAG, "Executive-->onCallAddVideo");
		VoipFunction.getInstance().setCallSession(session);
		VoipFunction.getInstance().setVideo(true);

		saveOrient(session.getOrientType());
		if (!VoipFunction.getInstance().isVideo())
		{
			VoipFunction.getInstance().declineVideoInvite();
		}
		else
		{
			VoipFunction.getInstance().postSipNotification(VoipFunction.VIDEO_INVITE);
		}

	}

	private void saveOrient(int orient) 
	{
		orient = VideoFunction.transOrient(orient);
		VideoFunction.getIns().setOrient(orient);
	}

	@Override
	public void onCallBldTransferFailed(CallSession arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallBldTransferFailed");

	}

	@Override
	public void onCallBldTransferRecvSucRsp(CallSession arg0) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallBldTransferRecvSucRsp");

	}

	@Override
	public void onCallBldTransferSuccess(CallSession arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallBldTransferSuccess");

	}

	/**
	 * 语音和视频通话来电时执行onCallComing方法
	 */
	@Override
	public void onCallComing(CallSession session) 
	{
		Log.d(TAG, "Executive-->onCallComing");
		VoipFunction.getInstance().setCallSession(session);
		VoipFunction.getInstance().setCallMode(VoipFunction.CALL_COME);
		VoipFunction.getInstance().setVoipStatus(VoipFunction.STATUS_CALLING);
		TupCall tupCall;
		String number = null;
		String displayName = null;
		PersonalContact contact = null;
		if (session != null) 
		{
//			tupCall = session.getTupCall();
			session.alertingCall();
			number = session.getCallerDisplayName();/*.getFromDisplayName();*/
			contact = ContactCache.getIns().getContactByNumber(number);
			
			//判断接收到的是否为视频呼叫
			if (session.isVideoCallType())
			{
				VoipFunction.getInstance().setVideo(true);
			}
		}
		VoipFunction.getInstance().showMediaActivity(false, number, displayName, contact);
	}

	/**
	 * 接通电话时执行onCallConnect方法
	 */
	@Override
	public void onCallConnect(CallSession session)
	{
		Log.d(TAG, "Executive-->onCallConnect");

		VoipFunction.getInstance().setCallSession(session);
		
		VoipFunction.getInstance().setVoipStatus(VoipFunction.STATUS_TALKING);

		if (session.isVideoCallType())
		{
			VoipFunction.getInstance().setVideo(true);
			saveOrient(session.getOrientType());
			VoipFunction.getInstance().prepareVideoCall();
		} 
		else
		{
			VoipFunction.getInstance().setVideo(false);
		}

		VoipFunction.getInstance().postSipNotification(VoipFunction.CALL_CONNECTED);

	}

	@Override
	public void onCallHoldFailed(CallSession arg0) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallHoldFailed");
	}

	@Override
	public void onCallHoldSuccess(CallSession arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallHoldSuccess");

	}

	/**
	 * 关闭视频聊天
	 */
	@Override
	public void onCallRemoveVideo(CallSession arg0)
	{
		VoipFunction.getInstance().setCallSession(arg0);
		VoipFunction.getInstance().setVideo(false);
		VoipFunction.getInstance().postSipNotification(VoipFunction.VIDEO_REMOVE);
		Log.d(TAG, "Executive-->onCallRemoveVideo");
	}

	@Override
	public void onCallVideoRemoveResult(CallSession callSession)
	{
		Log.d(TAG, "Executive-->onCallVideoRemoveResult");
	}

	@Override
	public void onCallStartResult(CallSession arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallStartResult");

	}

	@Override
	public void onCallToConf(CallSession arg0) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallToConf");
	}

	@Override
	public void onCallUnHoldFailed(CallSession arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallUnHoldFailed");
	}

	@Override
	public void onCallUnHoldSuccess(CallSession arg0) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallUnHoldSuccess");
	}

	/**
	 * 来电通知时，判断通讯方式是否为视频通信
	 */
	@Override
	public void onCallVideoAddResult(CallSession session)
	{
		Log.d(TAG, "Executive-->onCallVideoAddResult");
//		VoipFunction.getInstance().setCallSession(session);
//		TupCall tupCall;
//		if (session != null) 
//		{
//			tupCall = session.getTupCall();
//			if (1 == tupCall.getIsviedo()) 
//			{
//				saveOrient(tupCall.getOrientType());
//				VoipFunction.getInstance().postSipNotification(VoipFunction.VIDEO_ADD_SUCESS);
//				Log.d(TAG, "VoipFunction.VIDEO_ADD_SUCESS");
//				return;
//			} 
//			else
//			{
//				if (VoipFunction.getInstance().isVideo()) 
//				{
//					VoipFunction.getInstance().setVideo(false);
//					VoipFunction.getInstance().postSipNotification(VoipFunction.VIDEO_ADD_FAILED);
//				}
//			}
//		}
		/** 以上方法注释掉，用新sdk by302895 2016.1.11*/
		if(session.isVideoCall())
		{
			saveOrient(session.getOrientType());
			VoipFunction.getInstance().setVoipStatus(VoipFunction.STATUS_TALKING);
			VoipFunction.getInstance().postSipNotification(VoipFunction.VIDEO_ADD_SUCESS);
		}
		else
		{
			VoipFunction.getInstance().setVideo(false);
			VoipFunction.getInstance().postSipNotification(VoipFunction.VIDEO_ADD_FAILED);
		}

	}

	@Override
	public void onCallEnd(CallSession arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onCallend");
		VoipFunction.getInstance().getCallSession();
		VoipFunction.getInstance().setVoipStatus(VoipFunction.STATUS_INIT);
		VoipFunction.getInstance().setVideo(false);
		VoipFunction.getInstance().postSipNotification(VoipFunction.CALL_CLOSED);
	}

	@Override
	public void onFrowardNotify(String arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onFrowardNotify");
	}

	@Override
	public void onNetLevelChange(VoiceQuality arg0) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onNetLevelChange");

	}

	@Override
	public void onOrientChange(int arg0) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onOrientChange");
	}

	@Override
	public void onRefreshView()
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onRefreshView");

//		if (REFRESH_LOCAL_ADD == eventType)
//		{
			VoipFunction.getInstance().postSipNotification(VoipFunction.REFRESH_LOCAL_VIEW);
//		}

	}

	@Override
	public void onRegisterSuccess()
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onRegisterSuccess");
		VideoFunction.getIns().handleVoipRegisterSuccess();
	}

	@Override
	public void onRequestHangup(CallSession arg0, int arg1) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onRequestHangup");
	}

	@Override
	public void onRingBack(CallSession arg0) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onRingBack");
	}

	@Override
	public void onRouteChange()
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onRouteChange");
	}

	@Override
	public void onVoiceMailNotify(VoiceMailNotifyData arg0)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->onVoiceMailNotify");
	}

	@Override
	public int reportNofitication(String arg0, int arg1, EventData arg2)
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Executive-->reportNofitication");
		return 0;
	}

	@Override
	public void onCallDestroy(CallSession arg0) 
	{
		// TODO Auto-generated method stub
		
	}

}
