package com.huawei.esdk.uc.demo.contacts;

import java.util.ArrayList;
import java.util.List;

import com.huawei.common.CommonVariables;
import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.ContactTools;
import com.huawei.contacts.PersonalContact;
import com.huawei.demo.uc.R;
import com.huawei.esdk.uc.demo.headphoto.ContactHeadFetcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAddAdapter extends BaseAdapter
{

    private LayoutInflater mInflater;
    
    private ContactHeadFetcher headFetcher;

    /** 可选的成员列表 **/
    private List<PersonalContact> allContacts = new ArrayList<PersonalContact>();

    /** 已选的成员列表 **/
    private List<PersonalContact> selectedContacts = new ArrayList<PersonalContact>();
    
    /** 新选的成员列表 **/
    private List<PersonalContact> toAddContacts = new ArrayList<PersonalContact>();

    public ContactAddAdapter(Context context )
    {
        mInflater = LayoutInflater.from(context);
        headFetcher = new ContactHeadFetcher(context);
    }

    
    public void setContactList(List<PersonalContact> personalContacts,
            List<PersonalContact> selectContacts, List<PersonalContact> groupContacts)
    {
        this.allContacts = personalContacts;
        
        this.selectedContacts = selectContacts;
        
        this.toAddContacts = groupContacts;
        
        this.notifyDataSetChanged();

    }
    
    @Override
    public int getCount()
    {
        return allContacts.size();
    }

    @Override
    public Object getItem(int position)
    {
        return allContacts.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        convertView = mInflater.inflate(R.layout.contact_add_list_item, null);

        ImageView ivRadio = (ImageView) convertView.findViewById(R.id.radio);
        ImageView ivHead = (ImageView) convertView.findViewById(R.id.head);
        ImageView ivStatus = (ImageView) convertView
                .findViewById(R.id.status_image);
        TextView tvName = (TextView) convertView.findViewById(R.id.name);

        final PersonalContact contact = allContacts.get(position);

        tvName.setText(ContactTools.getDisplayName(contact, null, null));
        setUserStatus(ivStatus, contact.getStatus(false));

        if (isAdd(contact))
        {
            ivRadio.setImageResource(R.drawable.btn_square_selected);
        }
        else
        {
            ivRadio.setImageResource(R.drawable.btn_square_unselected);
        }
        if (contact.getEspaceNumber().equals(
                CommonVariables.getIns().getUserAccount())
                || isMember(contact))
        {
            ivRadio.setImageResource(R.drawable.btn_square_disable);
        }
//        HeadPhotoUtil.getInstance().loadHeadPhoto(contact, ivHead, false);
        //新SDK头像加载
        headFetcher.loadHead(contact, ivHead, false);

        return convertView;
    }

    private boolean isMember(PersonalContact item)
    {
        for (PersonalContact contact : selectedContacts)
        {
            if (contact.getEspaceNumber().equals(item.getEspaceNumber()))
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean isAdd(PersonalContact item)
    {
        for (PersonalContact contact : toAddContacts)
        {
            if (contact.getEspaceNumber().equals(item.getEspaceNumber()))
            {
                return true;
            }
        }
        return false;
    }

    private void setUserStatus(ImageView ivStatus, int status)
    {
        switch (status)
        {
            case ContactClientStatus.ON_LINE:

                ivStatus.setImageResource(R.drawable.recent_online_small);
                break;

            case ContactClientStatus.BUSY:

                ivStatus.setImageResource(R.drawable.recent_busy_small);
                break;

            case ContactClientStatus.XA:

                ivStatus.setImageResource(R.drawable.recent_away_small);
                break;

            case ContactClientStatus.AWAY:

                ivStatus.setImageResource(R.drawable.recent_offline_small);
                break;

            default:

                ivStatus.setImageResource(R.drawable.recent_offline_small);
                break;
        }
    }

}
