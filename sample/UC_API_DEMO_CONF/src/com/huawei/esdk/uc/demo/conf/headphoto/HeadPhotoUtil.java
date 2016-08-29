//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.esdk.uc.demo.conf.headphoto;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.huawei.common.library.asyncimage.ImageCache;
import com.huawei.common.res.LocContext;
import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.PersonalContact;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.esdk.uc.demo.conf.R.drawable;
import com.huawei.utils.StringUtil;
import com.huawei.utils.img.BitmapUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.ImageView;

public final class HeadPhotoUtil
{
	public static final String SUFFIX = ".jpg";
	public static final String SEPARATOR = "_";
	private static final int MAX_SIZE = 20;
	private static HeadPhotoUtil instance = new HeadPhotoUtil();
	private ImageCache imageCache;
	private ImageCache localCache;
	private ImageCache publicCache;
	private Bitmap circleBgBig = null;
	private Bitmap roundCornerBgBig = null;
	private Bitmap roundCornerBgSmall = null;
	private Map<String, String> accounts;
	private LruCache<String, Bitmap> bitmapMap;

	private HeadPhotoUtil()
	{
		init();
	}

	public static HeadPhotoUtil getIns()
	{
		return instance;
	}

	public static void loadBgHeadPhoto(PersonalContact pContact, ImageView imageView)
	{
		if (pContact == null)
		{
			imageView.setImageResource(drawable.default_head);
		}
		else
		{
			String eSpaceNumber = pContact.getEspaceNumber();
			if (TextUtils.isEmpty(eSpaceNumber))
			{
				imageView.setImageResource(drawable.default_head_local);
			}
			else
			{
				String headId = pContact.getHead();
				if (TextUtils.isEmpty(headId))
				{
					imageView.setImageResource(drawable.default_head);
				}
				else
				{
					loadBgHeadPhoto(imageView, eSpaceNumber, headId);
				}

			}
		}
	}

	private static void loadBgHeadPhoto(ImageView imageView, String eSpaceNumber, String headId)
	{
		Bitmap defaultHeadBitmap = getDefaultHeadImg(headId);
		if (defaultHeadBitmap == null)
		{
			imageView.setImageResource(drawable.default_head);
			String fileName = createHeadFileName(eSpaceNumber, headId);
			File file = new File(LocContext.getContext().getFilesDir(), fileName);
			if (!file.exists())
			{
				Logger.warn("UCAPI", fileName + "file not exit!");
			}

			int sideLength = ContactLogic.getIns().getMyOtherInfo().getPictureSideLength();
			imageView.setImageBitmap(BitmapUtil.decodeBitmapFromFile(file.getAbsolutePath(), sideLength, sideLength));
		}
		else
		{
			imageView.setImageBitmap(defaultHeadBitmap);
		}

	}

	public void setDefaultBitmap(String key, Bitmap defaultBitmap)
	{
		bitmapMap.put(key, defaultBitmap);
	}

	public Bitmap getDefaultBitmap(String key)
	{
		return (Bitmap) bitmapMap.get(key);
	}

	public Bitmap getCircleBgBig()
	{
		if (circleBgBig == null)
		{
			int resId = drawable.bg_call_head;
			Resources resources = LocContext.getContext().getResources();
			circleBgBig = BitmapFactory.decodeResource(resources, resId);
		}

		return circleBgBig;
	}

	public Bitmap getRoundCornerBgBig()
	{
		if (roundCornerBgBig == null)
		{
			int resId = drawable.head_bg_big;
			Resources resources = LocContext.getContext().getResources();
			roundCornerBgBig = BitmapFactory.decodeResource(resources, resId);
		}

		return roundCornerBgBig;
	}

	public Bitmap getRoundCornerBgSmall()
	{
		if (roundCornerBgSmall == null)
		{
			int resId = drawable.head_bg;
			Resources resources = LocContext.getContext().getResources();
			roundCornerBgSmall = BitmapFactory.decodeResource(resources, resId);
		}

		return roundCornerBgSmall;
	}

	public void addAccount(String account, String fileName)
	{
		if (account != null && fileName != null)
		{
			Map var3 = accounts;
			synchronized (accounts)
			{
				accounts.put(account, fileName);
			}
		}
	}

	public String getFileName(String account)
	{
		if (account == null)
		{
			return null;
		}
		else
		{
			Map var2 = accounts;
			synchronized (accounts)
			{
				return (String) accounts.get(account);
			}
		}
	}

	public void init()
	{
		Logger.debug("UCAPI", "init");
		accounts = new HashMap();
		imageCache = new ImageCache();
		localCache = new ImageCache();
		bitmapMap = new LruCache(20);
	}

	public void cleanPhotos()
	{
		imageCache.clearCaches();
		localCache.clearCaches();
		if (publicCache != null)
		{
			publicCache.clearCaches();
		}

		bitmapMap.evictAll();
		Map var1 = accounts;
		synchronized (accounts)
		{
			accounts.clear();
		}
	}

	public void deletePhotoDir()
	{
		deletePhoto(LocContext.getContext(), ".jpg");
	}

	public static void deletePhoto(Context context, String filter)
	{
		File sysFile = context.getFilesDir();
		File[] files = sysFile.listFiles(new ContactHeadFilter(filter));
		if (files != null && files.length > 0)
		{
			Logger.debug("UCAPI", "/length=" + files.length);
			File[] var7 = files;
			int var6 = files.length;

			for (int var5 = 0; var5 < var6; ++var5)
			{
				File sFile = var7[var5];
				deleteFile(sFile);
			}
		}

	}

	private static void deleteFile(File sFile)
	{
		if (sFile.isFile() && !sFile.delete())
		{
			Logger.debug("UCAPI", "Delete photo file fail, File is " + sFile.getPath());
		}

	}

	public static Bitmap getDefaultHeadImg(String headid)
	{
		int head = StringUtil.stringToInt(headid);
		int resource;
		Resources r;
		if (head == 0)
		{
			resource = drawable.default_head;
			r = LocContext.getContext().getResources();
			return BitmapFactory.decodeResource(r, resource);
		}
		else if (head > 0 && head <= 9)
		{
			resource = drawable.head0;
			resource += head;
			r = LocContext.getContext().getResources();
			return BitmapFactory.decodeResource(r, resource);
		}
		else
		{
			return null;
		}
	}

	public static String createHeadFileName(String account, String id)
	{
		return account + "_" + id + ".jpg";
	}

	public static String parseHeadId(String account, String fileName)
	{
		if (fileName != null && account != null)
		{
			String headId = fileName.replace(account + "_", "");
			return headId.replace(".jpg", "");
		}
		else
		{
			return "";
		}
	}

	public ImageCache getHeadCache()
	{
		return imageCache;
	}

	public ImageCache getPublicHeadCache()
	{
		if (publicCache == null)
		{
			publicCache = new ImageCache();
		}

		return publicCache;
	}

	public ImageCache getLocalHeadCache()
	{
		return localCache;
	}
}
