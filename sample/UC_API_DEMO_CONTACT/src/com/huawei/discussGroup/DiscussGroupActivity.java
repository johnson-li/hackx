package com.huawei.discussGroup;

import java.util.ArrayList;
import java.util.List;

import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.contacts.group.ConstGroupManager;
import com.huawei.data.ConstGroup;
import com.huawei.logindemo.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ListView;

public class DiscussGroupActivity extends Activity
{
	
	private ListView listview;
	
	private GroupAdapter adapter;
	
	private List<ConstGroup> constGroups = new ArrayList<ConstGroup>();
	
	private IntentFilter filter;
	
	private BroadcastReceiver receiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			updateGroups();
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discuss_show);
		
		updateGroups();
		listview = (ListView) findViewById(R.id.list_group);
		adapter = new GroupAdapter(constGroups, this);
		listview.setAdapter(adapter);
		
		filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE);
		filter.addAction(CustomBroadcastConst.ACTION_ADD_FRIEND_RESPONSE);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
	}
	
	
	
	private void updateGroups()
	{
		constGroups.clear();
		constGroups.addAll(getDiscussionGroups());
		if(adapter != null)
		{
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 获取讨论组列表
	 * @return
	 */
	public List<ConstGroup> getDiscussionGroups()
	{
		// 该方法获取到的包括固定群，需要根据groupType过滤处理下
		List<ConstGroup> groups = ConstGroupManager.ins().getConstGroups();
		List<ConstGroup> disGroups = new ArrayList<ConstGroup>();
		for (ConstGroup constGroup : groups)
		{
			//if (ConstGroup.DISCUSSION == constGroup.getGroupType()) /** 注释掉这句代码就将获取讨论组跟固定群组*/
			{
				disGroups.add(constGroup);
			}

		}
		return disGroups;
	}
}
