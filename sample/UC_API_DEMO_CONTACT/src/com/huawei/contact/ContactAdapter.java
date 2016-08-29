package com.huawei.contact;

import java.util.ArrayList;
import java.util.List;

import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.ContactTools;
import com.huawei.contacts.PersonalContact;
import com.huawei.data.ExecuteResult;
import com.huawei.data.PersonalTeam;
import com.huawei.data.entity.People;
import com.huawei.esdk.uc.application.UCAPIApp;
import com.huawei.logindemo.MainActivity;
import com.huawei.logindemo.R;
import com.huawei.logindemo.SearchActivity;
import com.huawei.photoUtils.ContactHeadFetcher;
import com.huawei.popWindow.GroupAdapter;
import com.huawei.service.ServiceProxy;
import com.huawei.utils.StringUtil;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ContactAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	
	private ServiceProxy mService = UCAPIApp.getApp().getService();
	
	private Context context;
	
	private ArrayList<PersonalContact> personalContacts=new ArrayList<PersonalContact>();
	
	private boolean isFromConf = false;
	
	private boolean isFromShow = false;
	
	private boolean isFromSearch = false;
	
	private PopupWindow popupWindow;
	
	private ListView list_pop;
	
	private List<PersonalTeam> serviceTeams;
	
	private ArrayList<PersonalTeam> teams =new ArrayList<PersonalTeam>();
	
	private int index;
	
	private ContactHeadFetcher headFetcher;
	
	private Dialog dialogDeletFriend;
	
	public ContactAdapter(Context context,ArrayList<PersonalContact> personalContacts)
	 {
		 this.context = context;
		 
		 this.personalContacts = personalContacts;
		 
		 mInflater = LayoutInflater.from(context);
		 
		 headFetcher = new ContactHeadFetcher(context);
		 
	 }
	
	public void setIsFromShow(boolean isFromShow)
	{
		this.isFromShow = isFromShow;
	}
	
	//会议功能调用此Adapter
	 public void setIsFromConf(boolean isFromConf)
	 {
		 this.isFromConf = isFromConf;
		 
		 notifyDataSetChanged();
	 }
	 
	 //因为联系人列表和查找列表公用一个ContactAdapter,如果是查找就显示出加好友图标，如果
	 //显示联系人就不显示
	 public void setIsFromSearch(boolean isSearch)
	 {
		 this.isFromSearch = isSearch;
	 }
	@Override
	public int getCount() 
	{
		
		return personalContacts.size();
	}

	@Override
	public Object getItem(int position) 
	{
		
		return personalContacts.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		convertView = mInflater.inflate(R.layout.contact_adapter,null);
		serviceTeams = ContactLogic.getIns().getTeams();
		
		ImageView ivHead = (ImageView) convertView.findViewById(R.id.image);
		TextView  tvName = (TextView) convertView.findViewById(R.id.name);
		TextView ivAdd =  (TextView) convertView.findViewById(R.id.add);
		TextView ivStatus = (TextView) convertView.findViewById(R.id.status);
		TextView ivDelete = (TextView) convertView.findViewById(R.id.delete);
		RelativeLayout rlContactDetail = (RelativeLayout)convertView.findViewById(R.id.contact_detail);
		
		ivAdd.setVisibility(View.GONE);
		
		final PersonalContact contact = personalContacts.get(position);

		rlContactDetail.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				showPersonDetail(contact);
			}
		});
		
		//显示状态
		int status = contact.getStatus(false);
		setUserStatus(ivStatus,status);
		
		//设置添加的点击事件
		if (isFromSearch) 
		{
			ivAdd.setVisibility(View.VISIBLE);
			
			//设置点击事件
			ivAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					//弹出popWindow
					 if (null != popupWindow && popupWindow.isShowing())
					 {
						 popupWindow.dismiss();
						 popupWindow = null;
						 return;
					 }
					 else
					 {
						 initPopuptWindow(v,contact);
						 // 设置popupWindow位置
						 popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
					 }
				}
			});
		}
		else 
		{
			ivAdd.setVisibility(View.GONE);
		}
		
//		ivHead.setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				showPersonDetail(contact);
//			}
//		});
		
		//设置删除的点击事件
		if(isFromShow)
		{
			ivDelete.setVisibility(View.VISIBLE);
			ivDelete.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					deleteFriend(contact);
				}
			});
		}
		
		//加载名字，加载头像
		String name = ContactTools.getDisplayName(contact, null, null);
		tvName.setText(name);
//		HeadPhotoUtils.getInstance().loadHeadPhoto(contact, ivHead,false);
		headFetcher.loadHead(contact, ivHead,false);
		Log.i("getDisplayName", ContactTools.getDisplayName(contact, null, null));
		return convertView;
	}
		
	
	private void setUserStatus(TextView ivStatus, int status) {
		switch (status) {
		
		case ContactClientStatus.ON_LINE :
			ivStatus.setText(R.string.online);
			break;
		
		case ContactClientStatus.BUSY:
			ivStatus.setText(R.string.busy);
			break;
			
		case ContactClientStatus.AWAY:
			ivStatus.setText(R.string.away);
			break;
		
		case ContactClientStatus.XA:
			ivStatus.setText(R.string.recent_away);
			break;

		default:
			ivStatus.setText(R.string.busy);
			break;
		}
	}
	
    

	/**
	 * 添加联系人
	 * */
	public void addFriend(PersonalContact contact,ArrayList<PersonalTeam> teams,int index)
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
	 * 删除联系人
	 * */
	public void deleteFriend(final PersonalContact contact)
	{
		if(contact == null)
		{
			return;
		}
		
		List<PersonalTeam> serviceTeams = ContactLogic.getIns().getTeams();
		if(serviceTeams.size() == 0)
		{
			return;
		}
		
		dialogDeletFriend = new Dialog(MainActivity.mContext,R.style.exit_dialog);
		dialogDeletFriend.setContentView(R.layout.dialog_deletefriend);
		dialogDeletFriend.show();
		
		Button ensure = (Button) dialogDeletFriend.findViewById(R.id.left_btn);
		ensure.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				dialogDeletFriend.dismiss();
				
				ExecuteResult ert = null;
				
				if(mService != null)
				{
					ert = mService.deleteFriend(contact.getEspaceNumber(), contact.getTeamId(),contact.getContactId());
					if(ert.isResult())
					{
						Toast.makeText(MainActivity.mContext,"删除成功！",Toast.LENGTH_LONG).show();
					}
					else {
						Toast.makeText(MainActivity.mContext,"删除失败",Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		
		Button cancel = (Button) dialogDeletFriend.findViewById(R.id.right_btn);
		cancel.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				if(dialogDeletFriend != null)
				{
					dialogDeletFriend.dismiss();
				}
			}
		});
		
	}

	/**
	 * 创建PopupWindow
	 */
	public void initPopuptWindow(View v,final PersonalContact contact) {

		View popupWindow_view = LayoutInflater.from(context).inflate(R.layout.activity_popupwindow_left, null,
				false);

		ListView list_pop = (ListView) popupWindow_view.findViewById(R.id.list_pop);
		teams =new ArrayList<PersonalTeam>();
		teams.clear();
		teams.addAll(serviceTeams);

		GroupAdapter groupAdapter = new GroupAdapter(context,teams);
		list_pop.setAdapter(groupAdapter);

		// 创建PopupWindow实例
		popupWindow = new PopupWindow(popupWindow_view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);

		// 设置动画效果
		popupWindow.setBackgroundDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.bg_dialog));

		//给popwindow中item设置点击事件，取出其中“分组”在其
		list_pop.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				index = position;
				addFriend(contact, teams, index);
				notifyDataSetChanged();
//				addFriend(contact);
				popupWindow.dismiss();
				popupWindow = null;
			}

		});
		popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
		popupWindow.setOutsideTouchable(true);
//        popupWindow.setTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setFocusable(true);
	}

	/**
	 * 显示联系人详情
	 *
	 * @param person 被显示的联系人
	 */
	protected void showPersonDetail(PersonalContact person)
	{
		People people = new People();

		if (TextUtils.isEmpty(person.getEspaceNumber()))
		{
			people.setType(People.SELFNUMBER);
			people.setKey(person.getContactId());
		}
		else
		{
			people.setType(People.ESPACENUMBER);
			people.setKey(person.getEspaceNumber());
		}

		Intent intent = new Intent(context, ContactDetailActivity.class);
		intent.putExtra("CONTACT_DETAIL", people);
		context.startActivity(intent);
	}
}
