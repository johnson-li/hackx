package com.huawei.discussGroup;

import java.util.ArrayList;
import java.util.List;

import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.ResponseCodeHandler.ResponseCode;
import com.huawei.common.constant.UCResource;
import com.huawei.contacts.PersonalContact;
import com.huawei.data.ExecuteResult;
import com.huawei.data.ManageGroupResp;
import com.huawei.data.base.BaseResponseData;
import com.huawei.discussGroup.MemberAddAdapter.OnMemberClickListener;
import com.huawei.esdk.uc.application.UCAPIApp;
import com.huawei.logindemo.R;
import com.huawei.service.ServiceProxy;
import com.huawei.utils.StringUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GroupMemberAddActivity extends Activity implements OnClickListener{
	
	private Button btCreate;
	
	private GridView gvContacts;
	
	private MemberAddAdapter addAdapter;
	
	private RelativeLayout rlContactListView;
	
	private ContactAddView contactAddView;  
	
	private String groupId;
	
	private int groupType;
	
	private IntentFilter filter;
	
	private ProgressDialog progressDialog;
	
	private List<PersonalContact> groupContacts = new ArrayList<PersonalContact>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_groupmemberadd);
		
		initViews();
		
		refreshGridView();
		
		filter = new IntentFilter();
        filter.addAction(CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE);
        filter.addAction(CustomBroadcastConst.ACTION_CREATE_GROUP);
        filter.addAction(CustomBroadcastConst.ACTION_INVITETO_JOIN_GROUP);
       
        registerRec();
	}
	private void initViews() {
		btCreate = (Button) findViewById(R.id.bt_create);
		gvContacts = (GridView) findViewById(R.id.to_add_gv);
		
		rlContactListView = (RelativeLayout) findViewById(R.id.contact_list_rl);
		contactAddView = new ContactAddView(this);
		rlContactListView.addView(contactAddView);
		
		btCreate.setOnClickListener(this);
	}
	
	public void refreshGridView()
	{
		int columWidth = getResources().getDimensionPixelSize(R.dimen.grid_item_wight);
		// 添加一个空白列
        int colums = getAddList().size() + 1;
        // 总宽度
        int width = colums * columWidth;
        // 宽度为总宽度，高度自适应
        LayoutParams params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        gvContacts.setLayoutParams(params);
        //设定列宽
        gvContacts.setColumnWidth(columWidth);
        //每列间隔
        gvContacts.setHorizontalSpacing(0);
        gvContacts.setStretchMode(GridView.NO_STRETCH);
        //设定列数
        gvContacts.setNumColumns(colums);
        
        final List<PersonalContact> addList = getAddList();
        addAdapter = new MemberAddAdapter(this,addList);
        
        addAdapter.setOnMemberClickListener(new OnMemberClickListener() {
			
			@Override
			public void onMemberClick(int position) {
				
				//移除成员
				if(position < addList.size())
				{
					PersonalContact contact = addList.get(position);
					contactAddView.removeContact(contact);
				}
			}
		});
        gvContacts.setAdapter(addAdapter);
        
        //成员为空时隐藏创建button
        if(addList.isEmpty())
        {
        	btCreate.setVisibility(View.GONE);
        }
        else
        {
        	btCreate.setVisibility(View.VISIBLE);
        }
        	
	}
	
	private List<PersonalContact> getAddList()
	{
		return contactAddView.getAddContacts();
	}
	
	private void registerRec()
    {
        //registerReceiver(receiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        
    }
	
	private BroadcastReceiver receiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

            if (CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE
                    .equals(action))
            {
                contactAddView.updateSearchContactList(intent);
            }
            else if (CustomBroadcastConst.ACTION_CREATE_GROUP.equals(action))
            {
                closeDialog();

                int result = intent.getIntExtra(UCResource.SERVICE_RESPONSE_RESULT, 0);

                if (UCResource.REQUEST_OK == result)
                {
                    BaseResponseData data = (BaseResponseData) intent
                            .getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);
                    if (ResponseCode.REQUEST_SUCCESS != data.getStatus())
                    {
                        // 响应错误
                        Toast.makeText(
                                GroupMemberAddActivity.this,
                                "create discuss group filed | ResponseCode = "
                                        + data.getStatus() + "["
                                        + data.getDesc() + "]",
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        // 成功响应
                        ManageGroupResp groupRtn = (ManageGroupResp) data;
                        groupId = groupRtn.getGroupId();
                        groupType = groupRtn.getGroupType();

//                        gotoChat(true);
                        Toast.makeText(GroupMemberAddActivity.this,"创建成功！",Toast.LENGTH_SHORT).show();
                        GroupMemberAddActivity.this.finish();
                    }
                }
                else
                {
                    // 请求错误
                    Toast.makeText(GroupMemberAddActivity.this,
                            "create discuss group filed ", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            else if (CustomBroadcastConst.ACTION_INVITETO_JOIN_GROUP.equals(action))
            {
                closeDialog();

                int result = intent.getIntExtra(UCResource.SERVICE_RESPONSE_RESULT, 0);

                if (UCResource.REQUEST_OK == result)
                {
                    BaseResponseData data = (BaseResponseData) intent
                            .getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);
                    if (ResponseCode.REQUEST_SUCCESS != data.getStatus())
                    {
                        // 响应错误
                        Toast.makeText(
                                GroupMemberAddActivity.this,
                                "invite group members response filed | ResponseCode = "
                                        + data.getStatus() + "["
                                        + data.getDesc() + "]",
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        // 成功响应
                        GroupMemberAddActivity.this.finish();
                    }
                }
                else
                {
                    // 请求错误
                    Toast.makeText(GroupMemberAddActivity.this,
                            "invite group members request filed",
                            Toast.LENGTH_SHORT).show();
                }
            }
		}
		
	};
	
	 private void showProgressDlg()
	    {
	        if (null == progressDialog)
	        {
	            progressDialog = new ProgressDialog(this);
	            if (!TextUtils.isEmpty(groupId))
	            {
	                progressDialog.setMessage("正在发送邀请，请稍后······");
	            }
	            else
	            {
	                progressDialog.setMessage("正在创建，请稍后······");
	            }
	            progressDialog.setCanceledOnTouchOutside(false);
	        }
	        progressDialog.show();
	    }

	    private void closeDialog()
	    {
	        if (null != progressDialog && progressDialog.isShowing())
	        {
	            progressDialog.dismiss();
	        }
	    }
	    
	    //此demo仅仅创建
	    private void createGroup(List<PersonalContact> addList) {
			
	    	ExecuteResult result = null;
	    	result = createDiscussGroup(addList);
	    	if(result.isResult())
	    	{
	    		showProgressDlg();
	    	}
	    }
	    
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		
		case R.id.bt_create:
			createGroup(getAddList());
			break;

		default:
			break;
		}
	}
	/**
     * 创建讨论组
     * @param contactList
     * @return
     */
    public ExecuteResult createDiscussGroup(List<PersonalContact> contactList)
    {
        ServiceProxy service = UCAPIApp.getApp().getService();
        if (service == null)
        {
            return null;
        }
        return service.createDiscussGroup(contactList);
    }
}
