package com.huawei.esdk.uc.demo.conf.utils;

import com.huawei.common.library.asyncimage.ImageWorker;
import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.MyOtherInfo;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.fetcher.Fetchers;
import com.huawei.utils.io.EncryptUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by cWX198123 on 2014/7/15.
 * 
 * 移植自espace
 */
public class SDCardPhotoFetcher extends ImageWorker
{
	private static final String TAG = SDCardPhotoFetcher.class.getSimpleName();

	boolean isVideo = false;

	public SDCardPhotoFetcher(Context context)
	{
		super(context);
		setImageFadeIn(false);
	}

	@Override
	protected BitmapDrawable processBitmap(Object data)
	{
		return null;
	}

	@Override
	protected BitmapDrawable getBitmapFromDiskCache(Object data)
	{
		String fileName = (String) data;
		if (fileName == null)
		{
			Logger.error(TAG, "fileName null.");
			return null;
		}

		 BitmapDrawable drawable = Fetchers.decodePicture(mContext, fileName,
	                isVideo, -1, MyOtherInfo.PICTURE_DEFAULT_WIDTH);
	     boolean encrypt = ContactLogic.getIns().getMyOtherInfo().isImageEncrypt();
	     if (drawable == null && encrypt && !isVideo)
	     {
	         fileName = EncryptUtil.getMdmPath(fileName);
	         drawable = Fetchers.decodePicture(mContext, fileName,
	                    isVideo, -1, MyOtherInfo.PICTURE_DEFAULT_WIDTH);
	     }

	     return drawable;
	}

	public void setVideo(boolean isVideo)
	{
		this.isVideo = isVideo;
	}

}
