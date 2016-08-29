//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.esdk.uc.demo.headphoto;

import java.io.File;

import com.huawei.utils.StringUtil;
import com.huawei.utils.img.BitmapUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

public class ContactHeadLocalLoader
{
	private Context mContext;
	private Bitmap outlineBitmap;
	private File sysFile;

	public ContactHeadLocalLoader(Context context, Bitmap outlineBitmap, File sysFile)
	{
		mContext = context;
		this.outlineBitmap = outlineBitmap;
		this.sysFile = sysFile;
	}

	public BitmapDrawable load(HeadPhoto headPhoto)
	{
		String account = headPhoto.getAccount();
		if (TextUtils.isEmpty(account))
		{
			return null;
		}
		else
		{
			Bitmap bitmap = getBitmap(account, headPhoto.getId());
			return bitmap == null ? null
					: new BitmapDrawable(mContext.getResources(),
							BitmapUtil.getRoundCornerBitmap(bitmap, outlineBitmap));
		}
	}

	private Bitmap getBitmapFromFile(String account, String headid)
	{
		File file = getPhotoFile(account, headid);
		if (file.exists())
		{
			HeadPhotoUtil.getIns().addAccount(account, file.getName());
			return BitmapUtil.decodeBitmapFromFile(file.getAbsolutePath(), 100, 100);
		}
		else
		{
			return null;
		}
	}

	private Bitmap getBitmap(String account, String headid)
	{
		if (TextUtils.isEmpty(headid))
		{
			return readUnknownHeadPhoto(account);
		}
		else
		{
			Bitmap bitmap = HeadPhotoUtil.getDefaultHeadImg(headid);
			if (bitmap != null)
			{
				HeadPhotoUtil.getIns().addAccount(account, headid);
				return bitmap;
			}
			else
			{
				return getBitmapFromFile(account, headid);
			}
		}
	}

	private File getPhotoFile(String account, String headId)
	{
		String fileName = HeadPhotoUtil.createHeadFileName(account, headId);
		return new File(sysFile, fileName);
	}

	protected Bitmap readUnknownHeadPhoto(String eSpaceNum)
	{
		Bitmap bitmap = null;
		File[] files = sysFile.listFiles(new ContactHeadFilter(eSpaceNum));
		if (files != null && files.length > 0)
		{
			File file = files[0];
			HeadPhotoUtil.getIns().addAccount(eSpaceNum, file.getName());
			bitmap = BitmapUtil.decodeBitmapFromFile(file.getAbsolutePath(), 100, 100);
		}

		return bitmap;
	}
}
