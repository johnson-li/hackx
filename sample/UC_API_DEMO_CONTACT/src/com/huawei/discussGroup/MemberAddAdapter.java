package com.huawei.discussGroup;

import java.util.List;

import com.huawei.contacts.ContactTools;
import com.huawei.contacts.PersonalContact;
import com.huawei.logindemo.R;
import com.huawei.photoUtils.ContactHeadFetcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberAddAdapter extends BaseAdapter {
	
	private List<PersonalContact> memberList;
	
	private LayoutInflater inflater;
	
	private OnMemberClickListener onMemberClickListener;
	
	 private ContactHeadFetcher headFetcher;
	
	public  MemberAddAdapter(Context context,List<PersonalContact> contactList) {
		
		inflater = LayoutInflater.from(context);
		memberList = contactList;
		headFetcher = new ContactHeadFetcher(context);
	}
	
	@Override
	public int getCount() {
		
		return memberList.size()+1;
	}

	@Override
	public Object getItem(int position) {
		
		return memberList.get(position+1);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		MemberViewHolder viewHolder;
		
		if(null == convertView)
		{
			viewHolder = new MemberViewHolder();
			convertView = inflater.inflate(R.layout.adapter_memberadd,null);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_head);
			viewHolder.textView = (TextView) convertView.findViewById(R.id.name_member);
			viewHolder.lable = (TextView) convertView.findViewById(R.id.lable);
			
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (MemberViewHolder) convertView.getTag();
		}
		if(position < memberList.size())
		{
			PersonalContact contact = memberList.get(position);
			viewHolder.textView.setText(ContactTools.getDisplayName(contact,null,null));
			headFetcher.loadHead(contact, viewHolder.imageView, true);
			viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			viewHolder.lable.setVisibility(View.VISIBLE);
		}
		else 
		{
			viewHolder.imageView.setImageResource(R.drawable.group_add_enable);
			viewHolder.textView.setText(null);
		}
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onMemberClickListener != null)
				{
					onMemberClickListener.onMemberClick(position);
				}
			}
		});
		
		return convertView;
	}
	
	//设置adapter的点击事件
	public void setOnMemberClickListener(OnMemberClickListener lsn)
	{
		this.onMemberClickListener = lsn;
	}
	
	private class MemberViewHolder
	{
		public ImageView imageView;
		public TextView textView;
		public TextView lable;
	}
	
	public interface OnMemberClickListener
	{
		void onMemberClick(int position);
	}
}
