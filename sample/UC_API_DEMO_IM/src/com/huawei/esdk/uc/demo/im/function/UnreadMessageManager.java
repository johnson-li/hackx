package com.huawei.esdk.uc.demo.im.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.huawei.common.CommonVariables;
import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.dao.impl.InstantMessageDao;
import com.huawei.dao.impl.PublicAccountMsgDao;
import com.huawei.data.entity.RecentChatContact;

public class UnreadMessageManager 
{
	private Map<String, Integer> unreadNumberMap = new HashMap<String, Integer>();

    public synchronized boolean saveUnreadNumber(String account, int number)
    {
        unreadNumberMap.put(account, Integer.valueOf(number));

        return true;
    }

    public boolean addUnreadNumber(String account)
    {
        if (!ContactLogic.getIns().getAbility().isHistoryMsgMerger())
        {
            return false;
        }

        int oldNumber = getUnreadNumber(account);

        return saveUnreadNumber(account, ++oldNumber);
    }

    public synchronized int clearUnreadNumber(String account)
    {
        Integer number = unreadNumberMap.remove(account);

        if (number == null)
        {
            return 0;
        }

        return number;
    }

    public int getUnreadNumber(String account, int msgType)
    {
        // 离线不显示未读条数
        if (!SelfDataHandler.getIns().getSelfData().isConnect())
        {
            return 0;
        }
        // 显示在一级目录的订阅号记录刷新未读条数
        if (RecentChatContact.PUBLIC_RECENT == msgType)
        {
            return PublicAccountMsgDao.queryUnreadCount(account);
        }
        // 公众号最近记录入口的未读显示根据配置缓存显示
        if (RecentChatContact.PUBLIC_ENTRANCE == msgType)
        {
            boolean notify = CommonVariables.getIns().isEntranceNotify();
            return notify ? 1 : 0;
        }
        // 非订阅号记录获取未读条数
        if (ContactLogic.getIns().getAbility().isHistoryMsgMerger())
        {
            return getUnreadNumber(account);
        }
        else
        {
            return InstantMessageDao.getUnReadMsgCount(account, msgType);
        }
    }

    private synchronized int getUnreadNumber(String account)
    {
        Integer number = unreadNumberMap.get(account);

        if (number == null)
        {
            return 0;
        }

        return number/*.intValue()*/;
    }

    public int getAllImUnreadNumber()
    {
        //离线不显示未读条数
        if (!SelfDataHandler.getIns().getSelfData().isConnect())
        {
            return 0;
        }

        if (ContactLogic.getIns().getAbility().isHistoryMsgMerger())
        {
            return getAllImUnreadNumberInMap();
        }
        else
        {
            return InstantMessageDao.getUnreadCount();
        }
    }

    private synchronized int getAllImUnreadNumberInMap()
    {
        Collection<Integer> numberList = unreadNumberMap.values();

        int total = 0;

        for (Integer number : numberList)
        {
            if (number != null)
            {
                total += number.intValue();
            }
        }

        return total;
    }

    public synchronized void cleanUp()
    {
        unreadNumberMap.clear();
    }
}
