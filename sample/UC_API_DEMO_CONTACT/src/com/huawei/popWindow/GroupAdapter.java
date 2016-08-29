package com.huawei.popWindow;

import java.util.ArrayList;

import com.huawei.data.PersonalTeam;
import com.huawei.logindemo.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GroupAdapter extends BaseAdapter {
	
	private Context context;
	
	private LayoutInflater mInflater;
	
	private ArrayList<PersonalTeam> personalTeams =new ArrayList<PersonalTeam>();
	
	
	
	
	public GroupAdapter(Context context, ArrayList<PersonalTeam> personalTeams) 
	{
		super();
		
		this.context = context;
		this.mInflater = LayoutInflater.from(context);
		this.personalTeams = personalTeams;
	}

	@Override
	public int getCount() 
	{
		
		return personalTeams.size();
	}

	@Override
	public Object getItem(int position) 
	{
		
		return personalTeams.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		convertView = mInflater.inflate(R.layout.pop_adapter,null);
		
		TextView groupname = (TextView) convertView.findViewById(R.id.group_name);
		
		PersonalTeam team = personalTeams.get(position);
		
		if(team != null)
		{
			groupname.setText(team.getTeamName());
		}
		
		return convertView;
	}
	
}
