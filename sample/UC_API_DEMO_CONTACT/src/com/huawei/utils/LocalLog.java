package com.huawei.utils;

import com.huawei.common.res.LocContext;
import com.huawei.esdk.uc.application.UCAPIApp;

public class LocalLog 
{
	  public static final String APPTAG = "UC_API_DEMO";
	    
	    public static final String FRAMEWORKTAG = "FrameWork";
	   
	    private static final String LOGFILE = "/ucApiDemoLog.txt";
	    
	    public static final String LOG_PATH = LocContext.getContext().getFilesDir() + "/ucApiDemoLog";
	    
	    public static final String LOG_PATH_RELATIVE = "/eSpaceAppLog";
	    
	    private static final int LOGMAXSIZE = 10485760;

}
