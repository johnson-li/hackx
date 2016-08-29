package com.huawei.contact;

import android.text.TextUtils;

import com.huawei.contacts.ContactCache;
import com.huawei.contacts.PersonalContact;
import com.huawei.data.entity.People;
import com.huawei.utils.StringUtil;

/**
 * Created by lWX302895 on 2016/6/12.
 */
public final class ContactDetailLogic
{
    /**
     * 通过参数获取PersonalContact详情
     * @param pc 通过intent,服务器返回的pc,信息不一定全;
     */
    public PersonalContact getContact(PersonalContact pc)
    {
        PersonalContact contact = null;

        if (!TextUtils.isEmpty(pc.getEspaceNumber()))
        {
            contact = ContactCache.getIns().getContactByAccount(pc.getEspaceNumber());
        }
        else if (!TextUtils.isEmpty(pc.getContactId()))
        {
            contact = ContactCache.getIns().getContactById(pc.getContactId());
        }

        if (contact == null)
        {
            contact = pc;

            //陌生人放缓存
            ContactCache.getIns().addStranger(contact);
        }

        return contact;
    }

    public PersonalContact getContact(People contactPeople)
    {
        PersonalContact contact = null;

        if (contactPeople.getType() == People.ESPACENUMBER)
        {
            contact = ContactCache.getIns().getContactByAccount(contactPeople.getKey());
        }
        else if (contactPeople.getType() == People.SELFNUMBER)
        {
            contact = ContactCache.getIns().getContactById(contactPeople.getKey());
        }

        if (contact == null)
        {
            contact = new PersonalContact();
            contact.setEspaceNumber(contactPeople.getKey());
            contact.setContactId(contactPeople.getKey());

            //陌生人放缓存
            ContactCache.getIns().addStranger(contact);
        }

        return contact;
    }

    public PersonalContact getContact(Object object)
    {
        PersonalContact contact = null;

        if (object instanceof PersonalContact)
        {
            contact = getContact((PersonalContact)object);
        }
        else if (object instanceof People)
        {
            contact = getContact((People)object);
        }

        return contact;
    }
}
