//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.esdk.uc.demo.headphoto;

import com.huawei.contacts.ContactCache;
import com.huawei.contacts.PersonalContact;
import com.huawei.demo.uc.R.drawable;
import com.huawei.esdk.uc.demo.headphoto.ContactHeadDownloader.ServerPhotoLoadedListener;
import com.huawei.utils.StringUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.widget.ImageView;

public class ContactHeadFetcher extends HeadFetcher
{
	private final ContactHeadDownloader contactHeadRequester;
	private ContactHeadLocalLoader localLoader;
	private ServerPhotoLoadedListener listener;

	public void setListener(ServerPhotoLoadedListener listener)
	{
		this.listener = listener;
	}

	public ContactHeadFetcher(Context context)
	{
		super(context, drawable.default_head);
		contactHeadRequester = new ContactHeadDownloader(context, outlineBitmap);
		localLoader = new ContactHeadLocalLoader(context, outlineBitmap, sysFile);
	}

	protected BitmapDrawable processBitmap(Object data)
	{
		return loadBitmapFromServer((HeadPhoto) data, -1);
	}

	protected BitmapDrawable getBitmapFromDiskCache(Object data)
	{
		HeadPhoto headPhoto = (HeadPhoto) data;
		return localLoader.load(headPhoto);
	}

	public void loadImageFromCache(String key, ImageView iv)
	{
		if (TextUtils.isEmpty(key))
		{
			iv.setImageResource(drawable.default_head_local);
		}
		else
		{
			super.loadImageFromCache(key, iv);
		}
	}

	public void loadHead(String account, ImageView headImage, boolean supportDel)
	{
		if (TextUtils.isEmpty(account))
		{
			headImage.setImageResource(drawable.default_head_local);
		}
		else
		{
			PersonalContact pContact = ContactCache.getIns().getContactByAccount(account);
			if (pContact != null)
			{
				loadHead(pContact, headImage, supportDel);
			}
			else
			{
				HeadPhoto headPhoto = new HeadPhoto(account, "");
				loadImage(headPhoto, headImage);
			}

		}
	}

	public void loadHead(String account, ImageView headImage, boolean onlyFromCache, boolean supportDel)
	{
		if (onlyFromCache)
		{
			loadImageFromCache(account, headImage);
		}
		else
		{
			loadHead(account, headImage, supportDel);
		}

	}

	public void loadHead(PersonalContact pContact, ImageView headImage, boolean supportDel)
	{
		if (pContact == null)
		{
			headImage.setImageResource(drawable.default_head);
		}
		else
		{
			String account = pContact.getEspaceNumber();
			if (TextUtils.isEmpty(account))
			{
				headImage.setImageResource(drawable.default_head_local);
			}
			else if (isHeadChangeOrDelete(pContact, supportDel))
			{
				deletePhoto(account);
				headImage.setImageBitmap(getDefaultBitmap((Object) null));
			}
			else
			{
				HeadPhoto headPhoto = new HeadPhoto(account, pContact.getHead());
				loadImage(headPhoto, headImage);
			}
		}
	}

	protected boolean forceRequestFromServer(Object data)
	{
		HeadPhoto headPhoto = (HeadPhoto) data;
		String account = headPhoto.getAccount();
		String fileName = HeadPhotoUtil.getIns().getFileName(account);
		if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(headPhoto.getId()))
		{
			String headId = HeadPhotoUtil.parseHeadId(account, fileName);
			return !headId.equals(headPhoto.getId());
		}
		else
		{
			return false;
		}
	}

	private void deletePhoto(String account)
	{
		HeadPhotoUtil.deletePhoto(mContext, account);
		getImageCache().removeBitmapFromCache(account);
	}

	private boolean isHeadChangeOrDelete(PersonalContact pContact, boolean supportDel)
	{
		return (pContact.isSelf() || supportDel || pContact.isFriend()) && TextUtils.isEmpty(pContact.getHead());
	}

	public void loadHead(String espacenumber, ImageView bigHead)
	{
		loadHead(espacenumber, bigHead, false);
	}

	public BitmapDrawable loadBitmapFromServer(HeadPhoto photo, int sideLength)
	{
		return contactHeadRequester.loadBitmapFromServer(photo, sideLength, listener);
	}
}
