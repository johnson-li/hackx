package com.huawei.discussGroup;

import java.util.ArrayList;
import java.util.List;

import com.huawei.data.ConstGroup;
import com.huawei.logindemo.R;

import android.R.mipmap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GroupAdapter extends BaseAdapter 
{
	private Context context;
	
	private List<ConstGroup> constGroups = new ArrayList<ConstGroup>();
	
	private LayoutInflater mInflater ;
	
	private ViewHolder viewHolder;
	
	private class ViewHolder
	{
		public TextView name;
	}
	
	public  GroupAdapter(List<ConstGroup> constGroups,Context context) 
	{
		this.constGroups = constGroups;
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() 
	{
		return constGroups.size();
	}

	@Override
	public Object getItem(int position) 
	{
		return constGroups.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.group_list_adapter,null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(viewHolder);
		}else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ConstGroup group = constGroups.get(position);
		viewHolder.name.setText(group.getName());
		
		return convertView;
	}

}
