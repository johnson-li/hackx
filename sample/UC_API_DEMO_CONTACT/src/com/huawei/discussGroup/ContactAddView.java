package com.huawei.discussGroup;

import java.util.ArrayList;
import java.util.List;

import com.huawei.common.CommonVariables;
import com.huawei.common.constant.UCResource;
import com.huawei.common.res.LocContext;
import com.huawei.contacts.ContactCache;
import com.huawei.contacts.PersonalContact;
import com.huawei.data.ExecuteResult;
import com.huawei.data.SearchContactsResp;
import com.huawei.data.base.BaseResponseData;
import com.huawei.esdk.uc.application.UCAPIApp;
import com.huawei.logindemo.R;
import com.huawei.service.ServiceProxy;
import com.huawei.utils.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ContactAddView extends LinearLayout implements OnClickListener{

	private EditText edCondition;
	
	private Button btnSearch;
	
	private ListView lvContact;
	
	private ListView lvSearchContact;
	
	private List<PersonalContact> personalContacts = new ArrayList<PersonalContact>();
	
	private List<PersonalContact> searchContacts = new ArrayList<PersonalContact>();
	
	private List<PersonalContact> addContacts = new ArrayList<PersonalContact>();
	
	private List<PersonalContact> memberContacts = new ArrayList<PersonalContact>();
	
	private ContactAddAdapter contactAddAdapter;
	
	private ContactAddAdapter searchAdapter;
	
	private GroupMemberAddActivity memberActivity;
	
	private int requestId;
	
	public ContactAddView(Context context)
	{
		this(context,null);
	}
	
	public ContactAddView(Context context,AttributeSet attrs)
	{
		super(context,attrs);
		View view = LayoutInflater.from(context).inflate(R.layout.activity_contactaddview,null);
		addView(view);
		
		//初始化
		memberActivity = (GroupMemberAddActivity) context;
		edCondition = (EditText) findViewById(R.id.edcondition);
		btnSearch = (Button) findViewById(R.id.btn_search);
		lvContact = (ListView) findViewById(R.id.list_contact);
		lvSearchContact = (ListView) findViewById(R.id.list_search_contact);
		
		//adapter
		contactAddAdapter = new ContactAddAdapter(context);
		contactAddAdapter.setContactList(personalContacts, memberContacts, addContacts);
		lvContact.setAdapter(contactAddAdapter);
		
		searchAdapter = new ContactAddAdapter(context);
		searchAdapter.setContactList(searchContacts, memberContacts,addContacts);
		lvSearchContact.setAdapter(searchAdapter);
		
		//设置点击事件
		btnSearch.setOnClickListener(this);
		
		setItemClickListener();
		
		getLocalContacts();
	}


	private void setItemClickListener() {
		lvContact.setOnItemClickListener(new OnItemClickListener() 
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PersonalContact contact = personalContacts.get(position);
				if(isCanAdd(contact))
				{
					addContacts.add(contact);
					contactAddAdapter.notifyDataSetChanged();
					searchAdapter.notifyDataSetChanged();
					memberActivity.refreshGridView();
				}
			}
			
		});
		
		lvSearchContact.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PersonalContact contact = searchContacts.get(position);
				if(isCanAdd(contact))
				{
					addContacts.add(contact);
					contactAddAdapter.notifyDataSetChanged();
					searchAdapter.notifyDataSetChanged();
					memberActivity.refreshGridView();
				}
			}
			
		});
		
		edCondition.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() == 0)
				{
					searchContacts.clear();
					searchAdapter.notifyDataSetChanged();
					lvSearchContact.setVisibility(View.GONE);
					lvContact.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private void getLocalContacts() {
		personalContacts.addAll(ContactCache.getIns().getFriends().getAllContacts());
		contactAddAdapter.notifyDataSetInvalidated();
	}
	
	private boolean isCanAdd(PersonalContact item)
    {
        if (item.getEspaceNumber().equals(
                CommonVariables.getIns().getUserAccount()))
        {
            return false;
        }
        if (isAdd(item))
        {
            return false;
        }
        if (isGroupMember(item))
        {
            return false;
        }
        return true;
    }

    private boolean isGroupMember(PersonalContact item)
    {
        for (PersonalContact contact : memberContacts)
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
        for (PersonalContact contact : addContacts)
        {
            if (contact.getEspaceNumber().equals(item.getEspaceNumber()))
            {
                return true;
            }
        }
        return false;
    }
    
    public List<PersonalContact> getAddContacts()
    {
        return addContacts;
    }
    
    public void removeContact(PersonalContact contact)
    {
    	for(int i = addContacts.size() -1 ; i>=0; i--)
    	{
    		if(contact.getEspaceNumber().equals(addContacts.get(i).getEspaceNumber()))
    		{
    			 addContacts.remove(i);
    			 break;
    		}
    	}
    	contactAddAdapter.notifyDataSetChanged();
    	searchAdapter.notifyDataSetChanged();
    	memberActivity.refreshGridView();
    }
    /**
     * 搜索企业通讯录
     * @param condition
     * @return
     */
    public ExecuteResult searchContact(String condition)
    {
        ServiceProxy serviceProxy = UCAPIApp.getApp().getService();
        if (null == serviceProxy)
        {
            return null;
        }
        // 这里只搜索前50条记录，分页处理暂时不做了
        return UCAPIApp.getApp().getService()
                .searchContact(condition, true, 1, 50, true);
    }
    
    /**更新联系人列表*/
    public void updateSearchContactList(Intent intent)
    {
        int result = intent.getIntExtra(UCResource.SERVICE_RESPONSE_RESULT, 0);
        if (UCResource.REQUEST_OK != result)
        {
            return;
        }
        BaseResponseData data = (BaseResponseData) intent
                .getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);

        if (data != null && data.getBaseId() == requestId
                && data instanceof SearchContactsResp)
        {
            searchContacts.clear();

            SearchContactsResp contactsResp = (SearchContactsResp) data;

            if (contactsResp.getContacts() != null)
            {
                searchContacts.addAll(contactsResp.getContacts());
                searchAdapter.notifyDataSetInvalidated();
				if (contactsResp.getContacts().size() <= 0)
				{
					Toast.makeText(LocContext.getContext(), "用户不存在", Toast.LENGTH_SHORT).show();
				}
            }
        }
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_search:
			String condition = edCondition.getText().toString();
			if(!TextUtils.isEmpty(condition))
			{
				ExecuteResult result = searchContact(condition);
				if(null != result && result.isResult())
				{
					requestId = result.getId();
					lvContact.setVisibility(View.GONE);
					lvSearchContact.setVisibility(View.VISIBLE);
				}
			}
			break;

		default:
			break;
		}
	}
}
