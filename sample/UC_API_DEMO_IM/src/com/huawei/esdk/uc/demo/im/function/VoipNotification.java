package com.huawei.esdk.uc.demo.im.function;

import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.PersonalContact;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.esdk.uc.demo.im.application.data.CommonUtil;
import com.huawei.esdk.uc.demo.im.self.SelfInfoUtil;
import com.huawei.utils.StringUtil;
import com.huawei.voip.CallSession;
import com.huawei.voip.IpCallNotification;
import com.huawei.voip.data.AudioMediaEvent;
import com.huawei.voip.data.EarpieceMode;
import com.huawei.voip.data.EventData;
import com.huawei.voip.data.OrientChange;
import com.huawei.voip.data.VoiceMailNotifyData;
import com.huawei.voip.data.VoiceQuality;

import android.text.TextUtils;
import android.util.Log;
import common.TupCallParam;
import tupsdk.TupCall;

public class VoipNotification implements IpCallNotification
{

    private static final String TAG = VoipNotification.class.getSimpleName();
    
    public static final int REFRESH_LOCAL_ADD = 1;
    public static final int REFRESH_LOCAL_DEL = 3;
    public static final int REFRESH_REMOTE_ADD = 2;
    public static final int REFRESH_REMOTE_DEL = 4;
    public static final int REFRESH_UNKNOWN = 0;
    
    private CallSession curCallSession;

    @Override
    public void onAudioPlayEnd(int arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onAudioPlayEnd ");
        VoipFunc.getIns().setAudioRoute(EarpieceMode.TYPE_AUTO);
        AudioMediaEvent param = new AudioMediaEvent(arg0);
        VoipFunc.getIns().postNotification(VoipFunc.AUDIOSTOPNOTIFY, param);

    }

    @Override
    public void onCallAddVideo(CallSession callSession)
    {
        Log.d(CommonUtil.APPTAG, TAG + " | onCallAddVideo ");

        VoipFunc.getIns().setCallSession(callSession);

        VoipFunc.getIns().setVideo(true);
        
        saveOrient(callSession.getOrientType());

        // 如果设置video发现无法设置成功(无权限),则直接拒接.
        if (!VoipFunc.getIns().isVideo())
        {
            VoipFunc.getIns().declineVideoInvite();
        }
        else
        {
            
            VoipFunc.getIns().postSipNotification(VoipFunc.VIDEO_INVITE);
        }
    }
    
    
    private void saveOrient(int orient)
    {
        orient = VideoFunc.transOrient(orient);
        VideoFunc.getIns().setOrient(orient);
    }

    @Override
    public void onCallBldTransferFailed(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onCallBldTransferFailed ");
    }

    @Override
    public void onCallBldTransferSuccess(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onCallBldTransferSuccess ");
    }

    @Override
    public void onCallComing(CallSession arg0)
    {
    	
        Log.d(CommonUtil.APPTAG, TAG + " | onCallComing ");

        VoipFunc.getIns().setCallSession(arg0);

        SelfInfoUtil.getIns().setStatus(ContactClientStatus.BUSY);

        VoipFunc.getIns().setCallMode(VoipFunc.CALL_COME);
        VoipFunc.getIns().setVoipStatus(VoipFunc.STATUS_CALLING);

        VoipFunc.getIns().saveCurrentCallPerson();

        TupCall tupCall;
        String number = null;
        String displayName = null;
        PersonalContact contact = null;
        if (arg0 != null)
        {

            arg0.alertingCall(); // 通知对端已振铃,更新sdk修改
            
            /**number 和 name 更新SDK后 修改获取的方法 tupCall也无法获取了*/
//            tupCall = arg0.getTupCall();
//            number = tupCall.getFromNumber();
//            displayName = tupCall.getFromDisplayName();
            
            
            if (!TextUtils.isEmpty(curCallSession.getTellNumber()))
            {
            	number = curCallSession.getTellNumber();
                displayName = curCallSession.getPaiDisplayName();
            }
            else if (!TextUtils.isEmpty(curCallSession.getPaiNumber()))
            {
            	number = curCallSession.getPaiNumber();
                displayName = curCallSession.getPaiDisplayName();
            }    
            else
            {
            	number = curCallSession.getCallerNumber();
                displayName = curCallSession.getCallerDisplayName();
            }
            
            contact = ContactFunc.getIns().getContactByNumber(number);

            boolean isConf = arg0.getIsFoces();//更新SDK后修改

            if (isConf)
            {
                boolean isCtc = ContactLogic.getIns().getAbility().isCtcFlag();

                if (isCtc)
                {
                    VoipFunc.getIns().setConfId(arg0.getServerConfID());
                    VoipFunc.getIns().setConf(true);
                }
            }

            /**更新SDK后 修改*/
//            if (TupCallParam.CALL_E_CALL_TYPE.TUP_CALLTYPE_VIDEO == tupCall
//                    .getCallType())
            if (arg0.isVideoCallType())
            {
                VoipFunc.getIns().setVideo(true);
            }
        }

        String callerNumber = "";

        if (!TextUtils.isEmpty(arg0.getTellNumber()))
        {
            callerNumber = arg0.getTellNumber();
        }
        else if (!TextUtils.isEmpty(arg0.getPaiNumber()))
        {
            callerNumber = arg0.getPaiNumber();
        }
        else
        {
            callerNumber = arg0.getCallerNumber();
        }

        if (callerNumber == null)
        {
            callerNumber = "";
        }

        // 判断callerNumber是否有; 有的话截断;后面的所有东西 判断是否是cpc=ordinary
        boolean isCpcOrdinary = false;
        String[] splitNumbers = callerNumber.split("\\;", 2);

        if (splitNumbers.length == 2 && splitNumbers[1] != null
                && splitNumbers[1].equals("cpc=ordinary"))
        {
            isCpcOrdinary = true;
            callerNumber = splitNumbers[0];
        }

        if (isCpcOrdinary)
        {
            splitNumbers = callerNumber.split("\\*", 2);

            if (!TextUtils.isEmpty(splitNumbers[0]))
            {
                callerNumber = splitNumbers[0];
            }
        }

        if (callerNumber.equals(SelfDataHandler.getIns().getSelfData()
                .getCallbackNmb()))
        {
            if (VoipFunc.getIns().getConfId() != null)
            {
                VoipFunc.getIns().answerConf();
                return;
            }
        }

        VoipFunc.getIns()
                .showMediaActivity(false, number, displayName, contact);
    }

    /**更新SDK 后修改  start*/
//    private void handleIncomingCall()
//    {
//        if (null == curCallSession || curCallSession.isVoiceMail())
//        {
//            return;
//        }
//
//        if (isVoipCalling(true))
//        {
//            curCallSession.hangUp(true);
//            curCallSession = null;
//            return;
//        }
//
//        stopSound();
//
//        curCallSession.alertingCall();
//
//        boolean isSnrFail = (getSnrStatus() == SNR_TRANSOUT_BYE);
//        clearSnrData();
//        setVoipStatus(STATUS_CALLING);
//        callType = CallFunc.CALL_COME;
//        callStatus = RecentCallContactDao.CALL_MISS;
//        setBeginCallTime(System.currentTimeMillis());
//        SelfInfoFunc.getIns().setToVoipStatus();
//        //顺序不能颠倒
//        parseCallSession(curCallSession);
//
//
//        if(curCallSession.isVideoCallType())
//        {
//            setVideo(true);
//        }
//
//        String callerNumber;
//        String backCallerNumber = null;
//      
//        if (!StringUtil.isStringEmpty(curCallSession.getTellNumber()))
//        {
//            callerNumber = curCallSession.getTellNumber();
//            backCallerNumber = curCallSession.getPaiNumber();
//            originName = curCallSession.getPaiDisplayName();
//        }
//        else if (!StringUtil.isStringEmpty(curCallSession.getPaiNumber()))
//        {
//            callerNumber = curCallSession.getPaiNumber();
//            originName = curCallSession.getPaiDisplayName();
//        }    
//        else
//        {
//            callerNumber = curCallSession.getCallerNumber();
//            originName = curCallSession.getCallerDisplayName();
//        }
//        
//        //DTS2014012306952 匿名名称处理
//        if(callerNumber != null && callerNumber.equals(ANONYMOUS))
//        {
//            String anonymousName = UCAPIClipApp.getApp().getString(R.string.unknown_call);
//            originName = anonymousName;
//        }
//     
//        if (callerNumber == null)
//        {
//            callerNumber = "";
//        }
//    
//        if (!StringUtil.isStringEmpty(curCallSession.getHistoryNumber()))
//        {
//            historyNumber = curCallSession.getHistoryNumber();
//            saveHistoryInfo(historyNumber);
//        }
//        
//        if (curCallSession.getCallControl() == CallControl.CALLER)
//        {
//            setUnderCtrlStatus(UNDERCTRL_CONTROLED);
//        }
//   
//        //判断callerNumber是否有; 有的话截断;后面的所有东西  判断是否是cpc=ordinary
//        boolean isCpcOrdinary = false;
//        String[] splitNumbers = callerNumber.split("\\;", 2);
//        
//        if (splitNumbers.length == 2 && "cpc=ordinary".equals(splitNumbers[1]))
//        {
//            isCpcOrdinary = true;
//            callerNumber = splitNumbers[0];
//        }
//   
//        ConferenceEntity sipConf = ConferenceDataHandler.getIns()
//                .getConference(confId);
//        ConferenceEntity conf = null;
//        
//        if (StringUtil.isStringEmpty(confId))
//        {
//            // 判断是否是MediaX会议
//            conf = ConferenceDataHandler.getIns().getConfByAccessCode(
//                    callerNumber);
//        }
//        else if (sipConf != null
//                && ConferenceDataHandler.getIns().isConfByAccessCode(
//                        callerNumber, sipConf))
//        {
//            conf = sipConf;
//        }
//     
//        if (conf != null && StringUtil.isStringEmpty(confId))
//        {
//            callMode = conf.getMediaType();
//            confId = conf.getConfId();
//        }
//        
//        if (isCpcOrdinary)
//        {
//            splitNumbers = callerNumber.split("\\*", 2);
//            if (!StringUtil.isStringEmpty(splitNumbers[0]))
//            {
//                callerNumber = splitNumbers[0];
//            }
//        }
//       
//        oppoNumber = callerNumber;
//        oppoBackNumber = backCallerNumber;
//        saveOppositeInfo(callerNumber, backCallerNumber);
//        
//        boolean isConference = (getConfId() != null);
//        long tempTime = System.currentTimeMillis() - getJoinInConferenceTime();
//        boolean isInTime = true;
//        
//        // 大于5秒
//        if (tempTime > 5 * 1000)
//        {
//            isInTime = false;
//            saveJoinInConferenceInfo(null, 0, 0);
//        }
//       
//        boolean isSelfBindNum = callerNumber.equals(ContactLogic.getIns().getMyContact().getBinderNumber());
//        
//        if (isSelfBindNum)
//        {
//            conf = null;
//            
//            if (StringUtil.isStringEmpty(confId))
//            {
//                // 此处使用邀请中的状态，如果会议中状态先推送过来的话就不会自动接听了
//                conf = ConferenceDataHandler.getIns().isSelfInInvite();
//            }
//            else if (sipConf != null && sipConf.isSelfInInvite())
//            {
//                conf = sipConf;
//            }
//            
//            if (conf != null)
//            {
//                // 为兼容HWUC里没有confId，但是又要标记会议来电
//                if (StringUtil.isStringEmpty(confId))
//                {
//                    callMode = conf.getMediaType();
//                    confId = conf.getConfId();
//                }
//            
//                answer(true);
//                return;
//            }
//        }
//       
//        if (isConference && getConfId().equals(getJoinInConferenceId())
//                && isInTime)
//        {
//            if (confId == null || callMode == VOIP_CALL)
//            {
//                confId = getJoinInConferenceId();
//                callMode = getJoinInConfMediaType();
//            }
//           
//            saveJoinInConferenceInfo(null, 0, 0);
//            answer(true);
//            
//            return;
//        }
//      
//        if (isSnrFail && ActivityStack.getIns().contain(MediaActivity.class))
//        {
//            answer(true);
//            isEverSnr = true;
//            return;
//        }
//        
//        //非匿名才查询
//        if(!oppoNumber.equals(ANONYMOUS))
//        {
//            checkOppoDetail();   
//        }
//        if(ActivityStack.getIns().contain(CalloutActivity.class))
//        {
//            ActivityStack.getIns().popup(CalloutActivity.class);
//        }
//        // 弹出来电界面
//        CallFunc.getIns().showMediaActivity(false, null, null, null);
//       
//        //C50需求 免打扰状态不响铃
//        if(!SelfInfoFunc.isUninterruptable())
//        {
//            MediaUtil.getInstance().playCallRing(false);
//            DeviceUtil.loopVibrator();
//        }       
//      
//        // 自动接听功能
//        boolean autoAnswer = SelfDataHandler.getIns().getSelfData()
//                .isAutoAnswer();
//        
//        if (!autoAnswer)
//        {
//            return;
//        }
//        
//        long autoAnswerTime = SelfDataHandler.getIns().getSelfData()
//                .getAutoAnswerTime();
//    
//        if (autoAnswerTime == 0)
//        {
//            answer();
//        }
//        else if (autoAnswerTime > 0)
//        {
//            autoAnswerTimer.callId = curCallSession.getCallID();
//            EventHandler.getIns().postDelayed(autoAnswerTimer,
//                    autoAnswerTime * 1000);
//        }
//    }
//
//    /**
//     * 检查当前是否在通话中
//     * @param isContainPstn 是否检查pstn通话
//     * @return
//     */
//    public boolean isVoipCalling(boolean isContainPstn)
//    {
//        // 如果考虑pstn通话的情况下，pstn通话中直接返回通话中
//        if (isContainPstn && !AndroidSystemUtil.isCallStateIdle())
//        {
//            return true;
//        }
//
//        return getVoipStatus() != STATUS_INIT;
//    }
//    
//    public boolean stopSound()
//    {
//        int result = -1;
//
//        if (callRingHandle != -1)
//        {
//            CallManager cm = getVoipManager();
//
//            if (cm != null)
//            {
//                result = cm.getTupHelper().stopPlay(callRingHandle);
//            }
//        }
//
//        boolean isSuccess = (result == 0);
//
//        if (isSuccess)
//        {
//            VoipFunc.getIns().setAudioRoute(EarpieceMode.TYPE_AUTO);
//            callRingHandle = -1;
//        }
//        
//        return isSuccess;
//    }
    
    /** end */
    @Override
    public void onCallConnect(CallSession call)
    {
  /* 会议相关 先不做
   *       Log.d(CommonUtil.APPTAG, TAG + " | onCallConnect ");

        VoipFunc.getIns().setCallSession(call);

        if (VoipFunc.getIns().getConfId() != null)
        {
            ConferenceFunc.getIns().reportTerminalType(
                    VoipFunc.getIns().getConfId());
        }

        VoipFunc.getIns().saveCurrentCallPerson();

        VoipFunc.getIns().setVoipStatus(VoipFunc.STATUS_TALKING);
        
    

        if (call.getTupCall().getCallType() == TupCallParam.CALL_E_CALL_TYPE.TUP_CALLTYPE_VIDEO)
        {
            VoipFunc.getIns().setVideo(true);
            saveOrient(call.getTupCall().getOrientType());
            VoipFunc.getIns().prepareVideoCall();
        }
        else
        {
            VoipFunc.getIns().setVideo(false);
        }

        if (VoipFunc.getIns().isConf())
        {
            EventData data = new EventData();
            data.setRawData(VoipFunc.getIns().getConfId());
            VoipFunc.getIns().sendBroadcast(VoipFunc.GOTO_CONF_VIEW, data);
        }
        else
        {
            VoipFunc.getIns().postSipNotification(VoipFunc.CALL_CONNECTED);
        }*/
    }

    @Override
    public void onCallHoldFailed(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onCallHoldFailed ");

        VoipFunc.getIns().setCallSession(arg0);

        VoipFunc.getIns().postSipNotification(VoipFunc.CALL_HOLD_FAILED);
    }

    @Override
    public void onCallHoldSuccess(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onCallHoldSuccess ");

        VoipFunc.getIns().setCallSession(arg0);

        VoipFunc.getIns().setHoldStatus(true);
        VoipFunc.getIns().postSipNotification(VoipFunc.CALL_HOLD);
    }

    @Override
    public void onCallRemoveVideo(CallSession arg0)
    {
        Log.d(CommonUtil.APPTAG, TAG + " | onCallRemoveVideo ");

        VoipFunc.getIns().setCallSession(arg0);

        VoipFunc.getIns().setVideo(false);

        VoipFunc.getIns().postSipNotification(VoipFunc.VIDEO_REMOVE);
    }

    @Override
    public void onCallToConf(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onCallToConf ");
    }

    @Override
    public void onCallUnHoldFailed(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onCallUnHoldFailed ");

        VoipFunc.getIns().setCallSession(arg0);

        VoipFunc.getIns().postSipNotification(VoipFunc.CALL_HOLD_FAILED);
    }

    @Override
    public void onCallUnHoldSuccess(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onCallUnHoldSuccess ");

        VoipFunc.getIns().setCallSession(arg0);

        VoipFunc.getIns().setHoldStatus(false);
        VoipFunc.getIns().postSipNotification(VoipFunc.CALL_HOLD);
    }

    @Override
    public void onCallVideoAddResult(CallSession session)
    {
        Log.d(CommonUtil.APPTAG, TAG + " | onCallVideoAddResult ");

        VoipFunc.getIns().setCallSession(session);
//        TupCall tupCall;
        if (session != null)
        {
//            tupCall = session.getTupCall();
            if (session.isVideoCall())
            {
                // VoipFunc.getIns().setVideo(true);
                saveOrient(session.getOrientType());
                VoipFunc.getIns().postSipNotification(VoipFunc.VIDEO_ADD_SUCESS);
                return;
            }
            else
            {
                if (VoipFunc.getIns().isVideo())
                {
                    VoipFunc.getIns().setVideo(false);
                    VoipFunc.getIns().postSipNotification(
                            VoipFunc.VIDEO_ADD_FAILED);
                }

            }
        }
    }

//    @Override
//    public void onCallend(CallSession callSession)
//    {
//        Log.d(CommonUtil.APPTAG, TAG + " | onCallend ");
//
//        VoipFunc.getIns().setCallSession(callSession);
//
//        VoipFunc.getIns().setVoipStatus(VoipFunc.STATUS_INIT);
//        VoipFunc.getIns().setHoldStatus(false);
//        VoipFunc.getIns().setMuteStatus(false);
//        // VoipFunc.getIns().setVideo(false);
//
//        SelfInfoUtil.getIns().setStatus(PersonalContact.ON_LINE);
//        VoipFunc.getIns().clear();
//
//        VoipFunc.getIns().postSipNotification(VoipFunc.CALL_CLOSED);
//    }

    @Override
    public void onFrowardNotify(String arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onFrowardNotify ");
    }

    @Override
    public void onNetLevelChange(VoiceQuality arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onNetLevelChange ");
    }

    @Override
    public void onRefreshView()
    {
        Log.d(CommonUtil.APPTAG, TAG + " | onRefreshView ");
//
//        // 1 添加视频；0 删除视频/添加视频失败
//        
//        if(REFRESH_LOCAL_ADD == eventType)
//        {
            VoipFunc.getIns().postSipNotification(VoipFunc.REFRESH_LOCAL_VIEW);
//        }
        
    }
    @Override
    public void onCallVideoRemoveResult(CallSession arg0) 
    {
    	// TODO Auto-generated method stub
    	Log.d(CommonUtil.APPTAG, TAG + " | onCallVideoRemoveResult ");
    }

    @Override
    public void onRegisterSuccess()
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onRegisterSuccess ");

        // VideoFunc.getIns().initView();
        // VideoFunc.getIns().setVideoCaps();
        // VideoFunc.getIns().deployGlobalVideoCaps();
        VideoFunc.getIns().handleVoipRegisterSuccess();
    }

    @Override
    public void onRingBack(CallSession arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onRingBack ");
    }

    @Override
    public void onRouteChange()
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onRouteChange ");
    }

    @Override
    public void onVoiceMailNotify(VoiceMailNotifyData arg0)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onVoiceMailNotify ");
    }

    @Override
    public void onRequestHangup(CallSession arg0, int arg1)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | onVoiceMailNotify ");
    }

    @Override
    public int reportNofitication(String arg0, int arg1, EventData arg2)
    {
        // TODO Auto-generated method stub
        Log.d(CommonUtil.APPTAG, TAG + " | reportNofitication " + arg0 + "  "
                + arg1);
        return 0;
    }

	@Override
	public void onCallBldTransferRecvSucRsp(CallSession arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOrientChange(int toOrient) {
		// TODO Auto-generated method stub
		
		/**更新SDK 后构造函数改变，修改之*/
        OrientChange orientChange = new OrientChange(VideoFunc.transOrient(toOrient));
//        orientChange.setOrient(VideoFunc.transOrient(toOrient));
        VoipFunc.getIns().sendBroadcast(VoipFunc.VIDEO_CHANGE_ORIENT, orientChange);
	}

	@Override
	public void onCallStartResult(CallSession arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCallEnd(CallSession callSession) 
	{
		 Log.d(CommonUtil.APPTAG, TAG + " | onCallend ");

	        VoipFunc.getIns().setCallSession(callSession);

	        VoipFunc.getIns().setVoipStatus(VoipFunc.STATUS_INIT);
	        VoipFunc.getIns().setHoldStatus(false);
	        VoipFunc.getIns().setMuteStatus(false);
	        // VoipFunc.getIns().setVideo(false);

	        SelfInfoUtil.getIns().setStatus(ContactClientStatus.ON_LINE);
	        VoipFunc.getIns().clear();

	        VoipFunc.getIns().postSipNotification(VoipFunc.CALL_CLOSED);
	}

	@Override
	public void onCallDestroy(CallSession paramCallSession) {
		// TODO Auto-generated method stub
		
	}



}
