package com.huawei.esdk.uc.control.data.proc;

import java.util.HashMap;
import java.util.Map;

import com.huawei.common.LocalBroadcast.LocalBroadcastProc;
import com.huawei.common.LocalBroadcast.ReceiveData;
import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.UCResource;
import com.huawei.data.base.BaseResponseData;

import android.content.Intent;

public class DataProc 
{
	 private final static DataProc INS = new DataProc();
	 
	    private static final Map<String, LocalBroadcastProc> PROCESSOR = 
	            new HashMap<String, LocalBroadcastProc> ();
	    
	    static
	    {
//	        LoginProc      loginProc      = new LoginProc();
//	        ContactProc    contactProc    = new ContactProc();
//	        SelfInfoProc   selfInfoProc   = new SelfInfoProc();
//	        CommonProc     commonProc     = new CommonProc();
//	        ConferenceProc conferenceProc = new ConferenceProc();
//	        
//	        PROCESSOR.put(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER, loginProc);
//	        PROCESSOR.put(CustomBroadcastConst.ACTION_LOGIN_ERRORACK, loginProc);
//	        PROCESSOR.put(CustomBroadcastConst.BACK_TO_LOGIN_VIEW, loginProc);
//	        PROCESSOR.put(CustomBroadcastConst.UPDATE_CONTACT_VIEW, contactProc);
//	        PROCESSOR.put(CustomBroadcastConst.ACTION_SET_STATUS_RESPONSE, selfInfoProc);
//	        PROCESSOR.put(CustomBroadcastConst.CTC_UPDATE_MEMBERSTATUS_PUSH, conferenceProc);
//	        PROCESSOR.put(CustomBroadcastConst.KICKED_BY_SVN_GATEWAY, commonProc);
	    }

	    private final LocalBroadcastProc proc = new LocalBroadcastProc()
	    {
	        @Override
	        public boolean onProc(Intent intent, ReceiveData rd)
	        {
	            LocalBroadcastProc proc = PROCESSOR.get(intent.getAction());
	           
	            if (proc != null)
	            {
	                return proc.onProc(intent, rd);
	            }
	            else
	            {
	                return onCommonProc(intent, rd);
	            }
	        }
	    }; 
	    
	    private DataProc()
	    {
	    }
	    
	    public static DataProc getIns()
	    {
	        return INS;
	    }
	    
	    public LocalBroadcastProc getProc()
	    {
	        return proc;
	    }
	    
	    private boolean onCommonProc(Intent intent, ReceiveData rd)
	    {
	        rd.action = intent.getAction();
	        String name = UCResource.SERVICE_RESPONSE_RESULT;
	        rd.result = intent.getIntExtra(name, UCResource.REQUEST_OK);
	        name = UCResource.SERVICE_RESPONSE_DATA;
	        Object obj = intent.getSerializableExtra(name);
	        if (obj instanceof BaseResponseData)
	        {
	            rd.data = (BaseResponseData) obj;
	        }
	        return true;
	    }
}
