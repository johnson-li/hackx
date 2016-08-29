package com.huawei.esdk.uc.demo.im;

import com.huawei.application.BaseApp;
import com.huawei.common.res.LocContext;
import com.huawei.data.unifiedmessage.AudioUniMessage;
import com.huawei.data.unifiedmessage.FileUniMessage;
import com.huawei.data.unifiedmessage.ImgUniMessage;
import com.huawei.data.unifiedmessage.MediaResource;
import com.huawei.data.unifiedmessage.TxtUniMessage;
import com.huawei.data.unifiedmessage.VideoUniMessage;

public class UmImConstant 
{
	public static final int MAXPICSIZE = 540;
	public static final int DOWNLOAD_PROMPT_SIZE = 1048576;
	public static final int NOT_DOWNLOAD = -1;
	private static final int PREPARE_DOWNLOAD = 0;
	public static final String LOG_PATH = LocContext.getContext().getFilesDir() + "/eSpaceAppLog";

	public static final String VOIP_LOG_FOLDER_NAME = "/voipLog/";
	 
	public static final String URL_DIVIDER = "/";
	public static final String DOT = ".";
	public static final String MP4 = "mp4";
	public static final String JPG = "jpg";
	public static final String WAV = "wav";
	public static final String PNG = "png";
	public static final String AMR = "amr";
	private static final int MAX_COUNT = 5000;
	private static final String LOGIN_UM = "login_um";
	
	public static final String UPLOADFILEFINISH = "local_um_upload_file_finish";
	   
	public static final String DOWNLOADFILEFINISH = "local_um_download_file_finish";
	public static final String UPLOADPROCESSUPDATE = "local_um_upload_process_update";
	public static final String DOWNLOADPROCESSUPDATE = "local_um_download_process_update";
	public static final String CONVERSATIONUPDATE = "local_um_conversationupdate";
	public static final String SHOWTOAST = "local_um_showtoast";
	private static final String[] PROCESSACTION = { "local_um_upload_process_update",
			"local_um_download_process_update" };

	private static final String[] FINISHACTION = { "local_um_upload_file_finish", "local_um_download_file_finish" };

	public static MediaResource generateMediaResource(String path, int resType, int mediaType, int size, int duration, String name, String extraParam)
	{
	      MediaResource result = null;
	      
	      switch (mediaType)
	      {
	      case 4: 
	        result = new FileUniMessage(path, resType);
	        
	        break;
	      case 1: 
	        result = new AudioUniMessage(path, resType);
	        
	        break;
	      case 2: 
	        result = new VideoUniMessage(path, resType);
	        
	        break;
	      case 3: 
	        result = new ImgUniMessage(path, resType);
	        
	        break;
	      case 99: 
	        result = new TxtUniMessage(path);
	        break;
	      }
	      
	      return result;
	}
}
