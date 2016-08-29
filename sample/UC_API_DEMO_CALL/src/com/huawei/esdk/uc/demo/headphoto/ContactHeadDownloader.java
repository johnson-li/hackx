//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.esdk.uc.demo.headphoto;

import java.util.ArrayList;
import java.util.List;

import com.huawei.data.ViewHeadPhotoData;
import com.huawei.data.ViewHeadPhotoParam;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.msghandler.maabusiness.GetHeadImageRequest;
import com.huawei.utils.StringUtil;
import com.huawei.utils.img.BitmapUtil;
import com.huawei.utils.io.FileUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

public class ContactHeadDownloader
{
	private final Context mContext;
	private final Bitmap outlineBitmap;

	public ContactHeadDownloader(Context context, Bitmap outlineBitmap)
	{
		this.mContext = context;
		this.outlineBitmap = outlineBitmap;
	}

	protected BitmapDrawable loadBitmapFromServer(HeadPhoto headPhoto, int sideLength,
			ContactHeadDownloader.ServerPhotoLoadedListener listener)
	{
		String account = headPhoto.getAccount();
		String id = headPhoto.getId();
		if (isInValidParam(account, id))
		{
			return null;
		}
		else
		{
			Bitmap bitmap = requestBitmap(account, id, sideLength);
			if (bitmap == null)
			{
				return null;
			}
			else
			{
				if (listener != null)
				{
					listener.onLoadSuccess();
				}

				return new BitmapDrawable(mContext.getResources(),
						BitmapUtil.getRoundCornerBitmap(bitmap, outlineBitmap));
			}
		}
	}

	private boolean isInValidParam(String account, String id)
	{
		return TextUtils.isEmpty(account) || TextUtils.isEmpty(id);
	}

	protected Bitmap doRequest(List<ViewHeadPhotoParam> list)
	{
		GetHeadImageRequest request = new GetHeadImageRequest();
		request.setWaitTime(15000);
		List dataList = request.requestPhoto(list);
		return dataList == null ? null : saveHeadPhoto(dataList, list);
	}

	private Bitmap requestBitmap(String account, String headId, int sideLength)
	{
		Logger.debug("ContactHeadDownloader", "account=" + account + "/id=" + headId);
		List list = getParam(account, headId, sideLength);
		return doRequest(list);
	}

	private List<ViewHeadPhotoParam> getParam(String account, String id, int sideLength)
	{
		ArrayList list = new ArrayList();
		ViewHeadPhotoParam param = new ViewHeadPhotoParam();
		param.setJid(account);
		param.setHeadId(id);
		param.setH(getSideLength(sideLength));
		param.setW(getSideLength(sideLength));
		list.add(param);
		return list;
	}

	private String getSideLength(int sideLength)
	{
		return String.valueOf(sideLength < 0 ? 100 : sideLength);
	}

	public Bitmap saveHeadPhoto(List<ViewHeadPhotoData> photoDatas, List<ViewHeadPhotoParam> headPhoto)
	{
		if (!isInValidParam(photoDatas, headPhoto))
		{
			return null;
		}
		else
		{
			ViewHeadPhotoData photoData = (ViewHeadPhotoData) photoDatas.get(0);
			ViewHeadPhotoParam mHeadPhoto = (ViewHeadPhotoParam) headPhoto.get(0);
			return saveBytes(photoData, mHeadPhoto);
		}
	}

	private boolean isInValidParam(List<ViewHeadPhotoData> photoDatas, List<ViewHeadPhotoParam> headPhoto)
	{
		return headPhoto != null && headPhoto.size() == 1 && photoDatas.size() == 1;
	}

	private Bitmap saveBytes(ViewHeadPhotoData photoData, ViewHeadPhotoParam mHeadPhoto)
	{
		String account = photoData.getEspaceNumber();
		if (TextUtils.isEmpty(account))
		{
			Logger.debug("UCAPI", "eSpaceNumber = null or \"\"");
			return null;
		}
		else
		{
			HeadPhotoUtil.deletePhoto(mContext, account);
			byte[] data = photoData.getData();
			String fileName = save(account, mHeadPhoto.getHeadId(), data);
			HeadPhotoUtil.getIns().addAccount(account, fileName);
			return BitmapUtil.decodeByteArray(data, 100);
		}
	}

	private String save(String account, String headId, byte[] data)
	{
		if (data == null)
		{
			Logger.debug("UCAPI", "headId = null");
			return null;
		}
		else
		{
			String fileName = HeadPhotoUtil.createHeadFileName(account, headId);
			FileUtil.saveBytes(mContext, fileName, data, true);
			return fileName;
		}
	}

	public interface ServerPhotoLoadedListener
	{
		void onLoadSuccess();
	}
}
