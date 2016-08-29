//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.esdk.uc.demo.headphoto;

import java.io.File;
import java.io.FilenameFilter;

public class ContactHeadFilter implements FilenameFilter
{
	String filter;

	ContactHeadFilter(String filter)
	{
		this.filter = filter;
	}

	public boolean accept(File dir, String filename)
	{
		return ".jpg".equalsIgnoreCase(filter) ? filename.endsWith(".jpg")
				: matchEspaceNumber(filename, filter);
	}

	private boolean matchEspaceNumber(String fileName, String eSpaceNum)
	{
		int index = fileName.indexOf("_");
		if (-1 != index)
		{
			String str = fileName.substring(0, index);
			return str.equalsIgnoreCase(eSpaceNum);
		}
		else
		{
			return false;
		}
	}
}
