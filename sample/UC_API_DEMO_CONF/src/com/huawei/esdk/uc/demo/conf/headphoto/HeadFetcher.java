//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.esdk.uc.demo.conf.headphoto;

import java.io.File;
import java.util.concurrent.RejectedExecutionException;

import com.huawei.common.library.asyncimage.ImageWorker;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.utils.img.BitmapUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public abstract class HeadFetcher extends ImageWorker
{
	protected final Bitmap outlineBitmap;
	protected File sysFile = null;

	protected HeadFetcher(Context context, int defaultRes)
	{
		super(context);
		sysFile = context.getFilesDir();
		Logger.debug("UCAPI", "" + sysFile);
		outlineBitmap = HeadPhotoUtil.getIns().getRoundCornerBgSmall();
		setDefaultHead(defaultRes);
		setImageFadeIn(false);
		setForHeadShow(true);
		setImageCache(HeadPhotoUtil.getIns().getHeadCache());
	}

	private void setDefaultHead(int res)
	{
		Bitmap bitmap = HeadPhotoUtil.getIns().getDefaultBitmap(String.valueOf(res));
		if (bitmap == null)
		{
			bitmap = BitmapFactory.decodeResource(mContext.getResources(), res);
			bitmap = BitmapUtil.getRoundCornerBitmap(bitmap, outlineBitmap);
			HeadPhotoUtil.getIns().setDefaultBitmap(String.valueOf(res), bitmap);
		}

		setLoadingImage(bitmap);
	}

	protected void execute(AsyncTask task, Object data)
	{
		try
		{
			task.execute(new Object[] { data });
		}
		catch (RejectedExecutionException var4)
		{
			Logger.warn("eSpaceService", var4);
		}

	}
}
