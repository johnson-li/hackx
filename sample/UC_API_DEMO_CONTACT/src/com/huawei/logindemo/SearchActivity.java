package com.huawei.logindemo;

import java.util.ArrayList;
import java.util.List;

import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.ResponseCodeHandler.ResponseCode;
import com.huawei.common.constant.UCResource;
import com.huawei.common.res.LocContext;
import com.huawei.contact.ContactAdapter;
import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.PersonalContact;
import com.huawei.data.AddFriendResp;
import com.huawei.data.ExecuteResult;
import com.huawei.data.PersonalTeam;
import com.huawei.data.SearchContactsResp;
import com.huawei.data.base.BaseResponseData;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.esdk.uc.application.UCAPIApp;
import com.huawei.popWindow.GroupAdapter;
import com.huawei.service.ServiceProxy;
import com.huawei.utils.StringUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnClickListener{
	
	private EditText edCondition;
	
	private Button btnSearch;
	
	private ListView lvContact;
	
	private ArrayList<PersonalContact>personalContacts = new ArrayList<PersonalContact>();
	
	private ContactAdapter contactAdapter;
	
	private int requestId;
	
	private IntentFilter filter;
	
	private static final int ADD_FRIEND_SUCCESS = 0x0;
	
	public static Context mContext;
	
	private List<PersonalTeam> serviceTeams;
	
	private ServiceProxy mService = UCAPIApp.getApp().getService();
	
	private PopupWindow popupWindow;
	
	private ArrayList<PersonalTeam> teams;
	
	private int index;
	/**
	 * 更新UI？
	 * */
	private Handler myHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			
			case ADD_FRIEND_SUCCESS:
				
				handleAddFriendSuccess((AddFriendResp)msg.obj);
				break;

			default:
				break;
			}
		}
		
	};
	
	/**
	 * toast之前在作甚？
	 * */
	private void handleAddFriendSuccess(AddFriendResp data)
	{
		if(data == null)
		{
			Logger.debug("UCAPIDEMO","handleAddFriendSuccess receiveData is null");
			return;
		}
		Toast.makeText(this,"add friend response:"+data.getAnswer(), Toast.LENGTH_SHORT).show();
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		
		mContext = this;
		serviceTeams = ContactLogic.getIns().getTeams();
		
		edCondition = (EditText) findViewById(R.id.condition);
		btnSearch = (Button) findViewById(R.id.btn_search);
		lvContact = (ListView) findViewById(R.id.list_contact);
		
		contactAdapter = new ContactAdapter(this, personalContacts);
		contactAdapter.setIsFromSearch(true);
		lvContact.setAdapter(contactAdapter);
//		lvContact.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//				PersonalContact contact = personalContacts.get(position);
//				initPopuptWindow(view, contact);
//				contactAdapter.notifyDataSetChanged();
//			}
//		});
		
		btnSearch.setOnClickListener(this);
		edCondition.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) 
			{
				if(s.length() == 0)
				{
					personalContacts.clear();
					contactAdapter.notifyDataSetChanged();
				}
			}
		});
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
        return serviceProxy.searchContact(condition, true, 1, 50, true);
    }
	
    /**
	 * 添加联系人
	 * */
	public void addFriend(PersonalContact contact)
	{
		if(contact == null)
		{
			return;
		}

		if(serviceTeams.size() == 0)
		{
			return;
		}
		ExecuteResult r = null;
		
		if(mService != null)
		{
			r = mService.addFriend(contact.getEspaceNumber(), null, teams.get(index).getTeamId(),false,false);
		} 
	}
    
    
	/**
	 * 将SearchContactView中onDetachedFromWindow（）放在onResume（）
	 * 因为彼是一个View而此处是一个Activity
	 * 
	 * */
	@Override
	protected void onResume() {
		super.onResume();
		filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE);
		filter.addAction(CustomBroadcastConst.ACTION_ADD_FRIEND_RESPONSE);

		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

	}
	
	/**
	 * 像是更新搜索列表
	 * */
	private void updateSearchContactList(Intent intent) 
	{
		int result = intent.getIntExtra(UCResource.SERVICE_RESPONSE_RESULT, 0);
		if(UCResource.REQUEST_OK != result)
		{
			return;
		}
		
		BaseResponseData data = (BaseResponseData) intent.getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);
	
		int id = data.getBaseId();
		
		if(data != null && data.getBaseId() == requestId
				&& data instanceof SearchContactsResp)
		{
			personalContacts.clear();
			
			SearchContactsResp contactsResp = (SearchContactsResp) data;
			
			if(contactsResp.getContacts() != null)
			{
				personalContacts.addAll(contactsResp.getContacts());
				contactAdapter.notifyDataSetChanged();
				if (contactsResp.getContacts().size() <= 0)
				{
					Toast.makeText(LocContext.getContext(), "用户不存在", Toast.LENGTH_SHORT).show();
				}
			}
			else
			{
				Toast.makeText(SearchActivity.this,"无结果！",Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver()
			{

				@Override
				public void onReceive(Context context, Intent intent) {
					
					String action = intent.getAction();
					if(CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE
		                    .equals(action))
					{
						updateSearchContactList(intent);
						Log.i("updateSearchContactList", "updateSearchContactList(intent)-----");
					}
					else if(CustomBroadcastConst.ACTION_ADD_FRIEND_RESPONSE
		                    .equals(action))
					{
						Log.i("CustomBroadcastConst", "CustomBroadcastConst.ACTION_ADD_FRIEND_RESPONSE-----");
						int result = intent.getIntExtra(UCResource.SERVICE_RESPONSE_RESULT, 0);
						if(UCResource.REQUEST_OK != result)
						{
							return;
						}
						BaseResponseData data = (BaseResponseData) intent.getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);
					
						if(data != null && data instanceof AddFriendResp)
						{
							AddFriendResp afr = (AddFriendResp) data;
							if(ResponseCode.REQUEST_SUCCESS.equals(afr.getStatus()))
							{
								sendMessage(ADD_FRIEND_SUCCESS,afr);
							}
						}
					}
				}

		private void sendMessage(int what, Object object) 
		{
			if(object != null)
			{
				Message msg = new Message();
				msg.what = what;
				msg.obj = object;
				myHandler.sendMessage(msg);
			}
			else
			{
				myHandler.sendEmptyMessage(what);
			}
		}

	};

	 /** 
     * 创建PopupWindow 
     */  
    public void initPopuptWindow(View v,final PersonalContact contact) {  
    	
        View popupWindow_view = LayoutInflater.from(SearchActivity.mContext).inflate(R.layout.activity_popupwindow_left, null,  
                false); 
        
        ListView list_pop = (ListView) popupWindow_view.findViewById(R.id.list_pop);
        teams =new ArrayList<PersonalTeam>();
        teams.clear();
        teams.addAll(serviceTeams);
        
        GroupAdapter groupAdapter = new GroupAdapter(SearchActivity.mContext,teams);
        list_pop.setAdapter(groupAdapter);
        
        // 创建PopupWindow实例
        popupWindow = new PopupWindow(popupWindow_view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);  
        
        // 设置动画效果  
        popupWindow.setBackgroundDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.bg_dialog));

        //给popwindow中item设置点击事件，取出其中“分组”在其
        list_pop.setOnItemClickListener(new AdapterView.OnItemClickListener() 
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				index = position;
				contactAdapter.addFriend(contact,teams,index);
				contactAdapter.notifyDataSetChanged();
//				addFriend(contact);
				popupWindow.dismiss();
			}
			
		});
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
    }  
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_search:
			String condition = edCondition.getText().toString();
			if (TextUtils.isEmpty(condition)) {
				break;
			}
			
			//应该是查找联系人
			ExecuteResult result = searchContact(condition);
			
			if(null != result && result.isResult())
			{
				requestId = result.getId();
			}
			break;
		
		default:
			break;
		}
	}
	
}
