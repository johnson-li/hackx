package com.huawei.esdk.uc.demo.im.function;

import java.util.HashSet;
import java.util.Set;

public class AtMessageManager 
{
	 private Set<String> atMessageManager = new HashSet<String>();

	    public synchronized boolean saveMsgNotify(String account)
	    {
	        atMessageManager.add(account);
	        return true;
	    }

	    public synchronized boolean isMsgNotify(String account)
	    {
	        return atMessageManager.contains(account);
	    }

	    public synchronized boolean clearMsgNotify(String account)
	    {
	        return atMessageManager.remove(account);
	    }

	    public synchronized void cleanUp()
	    {
	        atMessageManager.clear();
	    }
}
