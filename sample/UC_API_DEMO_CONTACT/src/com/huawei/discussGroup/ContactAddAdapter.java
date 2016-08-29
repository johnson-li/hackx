package com.huawei.discussGroup;

import java.util.ArrayList;
import java.util.List;

import com.huawei.common.CommonVariables;
import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.ContactTools;
import com.huawei.contacts.PersonalContact;
import com.huawei.logindemo.R;
import com.huawei.photoUtils.ContactHeadFetcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAddAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	
	/** 可选的成员列表 **/
    private List<PersonalContact> allContacts = new ArrayList<PersonalContact>();

    /** 已选的成员列表 **/
    private List<PersonalContact> selectedContacts = new ArrayList<PersonalContact>();
    
    /** 新选的成员列表 **/
    private List<PersonalContact> toAddContacts = new ArrayList<PersonalContact>();
	
    private ContactHeadFetcher headFetcher;
    
    public ContactAddAdapter(Context context) {
    	mInflater = LayoutInflater.from(context);
    	 headFetcher = new ContactHeadFetcher(context);
	}
    
    
	public void setContactList(List<PersonalContact> personalContacts, List<PersonalContact> selectContacts,
			List<PersonalContact> groupContacts) {
		this.allContacts = personalContacts;

		this.selectedContacts = selectContacts;

		this.toAddContacts = groupContacts;

		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		
		return allContacts.size();
	}

	@Override
	public Object getItem(int position) {
		
		return allContacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		convertView = mInflater.inflate(R.layout.contactaddadapter,null);
		//初始化控件
//		CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
		ImageView ivRadio = (ImageView) convertView.findViewById(R.id.radio);
		ImageView ivHead = (ImageView) convertView.findViewById(R.id.image_add);
		TextView tvName = (TextView) convertView.findViewById(R.id.name_add);
		TextView tvStatus = (TextView) convertView.findViewById(R.id.status_add);
		
		PersonalContact contact = allContacts.get(position);
		//设置名字和状态
		tvName.setText(ContactTools.getDisplayName(contact, null, null));
		setUserStatus(tvStatus,contact.getStatus(false));
		
		//设置复选框
		 if (isAdd(contact))
	        {
	            ivRadio.setImageResource(R.drawable.btn_square_selected);
	        }
	        else
	        {
	            ivRadio.setImageResource(R.drawable.btn_square_unselected);
	        }
	        if (contact.getEspaceNumber().equals(
	                CommonVariables.getIns().getUserAccount()))
	        {
	            ivRadio.setImageResource(R.drawable.btn_square_disable);
	        }
//	        HeadPhotoUtils.getInstance().loadHeadPhoto(contact,ivHead,false);
	        headFetcher.loadHead(contact,ivHead,false);
		return convertView;
	}
	private void setUserStatus(TextView tvStatus,int status)
	{
		switch (status)
        {
            case ContactClientStatus.ON_LINE:
            	tvStatus.setText(R.string.online);
                break;

            case ContactClientStatus.BUSY:

            	tvStatus.setText(R.string.busy);
                break;

            case ContactClientStatus.XA:

            	tvStatus.setText(R.string.recent_away);
                break;

            case ContactClientStatus.AWAY:

            	tvStatus.setText(R.string.offline);
                break;

            default:

            	tvStatus.setText(R.string.offline);
                break;
        }
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
	
//	 private boolean isMember(PersonalContact item)
//	    {
//	        for (PersonalContact contact : selectedContacts)
//	        {
//	            if (contact.getEspaceNumber().equals(item.getEspaceNumber()))
//	            {
//	                return true;
//	            }
//	        }
//	        return false;
//	    }
}
