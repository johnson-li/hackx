//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.esdk.uc.demo.conf.headphoto;

import android.text.TextUtils;

import com.huawei.utils.StringUtil;

public class HeadPhoto
{
	private String account = "";
	private String id = "";

	public HeadPhoto(String eSpaceNumber, String headId)
	{
		if (!TextUtils.isEmpty(eSpaceNumber))
		{
			account = eSpaceNumber;
		}

		if (!TextUtils.isEmpty(headId))
		{
			id = headId;
		}

	}

	public String getAccount()
	{
		return account;
	}

	public String getId()
	{
		return id;
	}

	public boolean equals(Object o)
	{
		if (o instanceof HeadPhoto)
		{
			HeadPhoto mPhoto = (HeadPhoto) o;
			return account.equals(mPhoto.getAccount());
		}
		else
		{
			return false;
		}
	}

	public int hashCode()
	{
		return account.length();
	}

	public String toString()
	{
		return account;
	}
}
