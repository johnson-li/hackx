package com.huawei.contact;

import java.util.ArrayList;
import java.util.List;

import com.huawei.contacts.ContactLogic;
import com.huawei.contacts.PersonalContact;
import com.huawei.data.PersonalTeam;
import com.huawei.espace.framework.common.ThreadManager;
import com.huawei.logindemo.MainActivity;
import com.huawei.logindemo.R;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContactView extends LinearLayout implements OnClickListener
{
	private Context context;
	private boolean flag = false;
	private RelativeLayout titleBar;
	private TextView tvTeamTitle;
	private ImageView ivContactTriangle;
	private GridView  gvTeams;
	private TeamsAdapter teamsAdapter;
	
	private ListView lvContact;
	private ContactAdapter contactAdapter;
	private List<PersonalTeam> personalTeams = new ArrayList<PersonalTeam>();
	private ArrayList<PersonalContact> personalContacts = new ArrayList<PersonalContact>();
	private int index;//这个index很诡异，觉得会出问题
	
	private Dialog dialogDeletFriend;
	//不知此构造函数作甚
	public ContactView(Context context)
	{
		this(context,null);
	}
	
	public  ContactView(Context context,AttributeSet attrs) 
	{
		super(context,attrs);
		
		this.context = context;
		
		View view = LayoutInflater.from(context).inflate(R.layout.view_contact,null);
		
		addView(view);
		
		getContactsTeams();
		
		tvTeamTitle = (TextView) findViewById(R.id.title);
		ivContactTriangle = (ImageView) findViewById(R.id.contact_triangle_image);
		
		gvTeams = (GridView) findViewById(R.id.teams);
		teamsAdapter = new TeamsAdapter(context, personalTeams);
		gvTeams.setAdapter(teamsAdapter);
		
		//此处改写，点击事件不在发生在小三角而发生在整个条栏
		titleBar = (RelativeLayout) findViewById(R.id.titleBar);
		titleBar.setOnClickListener(this);
		
		//写入联系人信息
		lvContact = (ListView) view.findViewById(R.id.list_contact);
		contactAdapter = new ContactAdapter(context, personalContacts);
		contactAdapter.setIsFromShow(true);
		
		lvContact.setAdapter(contactAdapter);
		
		//似乎没什么用
		 tvTeamTitle.setText(R.string.all_contacts);
	     gvTeams.setVisibility(View.GONE);
	     
	     //item点击事件
//	     setOnItemClickListener();
	     
	     //更新列表
	     ThreadManager.getInstance().addToSingleThread(new Runnable() {
			
			@Override
			public void run() {
				updateContactsList();
			}

		});
	}
	private void getContactsTeams()
	{
		personalTeams.clear();
		
		//获取联系人分组列表
		personalTeams.addAll(ContactLogic.getIns().getTeams());
		Log.i("personalTeams=====", personalTeams.size()+"");
		
		
		//获取所有联系人分组信息
		PersonalTeam allContactsTeam = ContactLogic.getIns().getAllContactsTeam();
		
		allContactsTeam.setTeamName(context.getString(R.string.all_contacts));
		
		//加入personalTeams对象
		personalTeams.add(0,allContactsTeam);
		Log.i("personalTeams+", personalTeams.size()+"");
	}
	
	private void setOnItemClickListener()
    {
//
////		/**点击删除联系人，李越*/
//		lvContact.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//				final PersonalContact contact = personalContacts.get(position);
//						contactAdapter.deleteFriend(contact);
//						contactAdapter.notifyDataSetChanged();
//			}
//		});
		
        gvTeams.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3)
            {
                if (index != arg2)
                {
                    index = arg2;
                    updateContactsList();
                }
            }
        });
    }
	public void updateContactsList() {
		
		//获取分组列表，返回List<PersonalTeam>
		getContactsTeams();
		
		if(null != personalTeams && personalTeams.size() > 0)
		{
			 PersonalTeam team = personalTeams.get(index);
			 
	            tvTeamTitle.setText(team.getTeamName());
	            personalContacts.clear();
	            personalContacts.addAll(team.getContactList());
	            Log.i("personalContacts=",personalContacts.size()+"");

	            if (contactAdapter != null)
	            {
	                contactAdapter.notifyDataSetChanged();
	            }
	            if (teamsAdapter != null)
	            {
	                teamsAdapter.notifyDataSetChanged();
	            }
		}
	}
	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.titleBar:
			if(flag)
			{
				gvTeams.setVisibility(View.GONE);
			}
			else 
			{
				gvTeams.setVisibility(View.VISIBLE);
			}
			flag = !flag;
			
			break;

		default:
			break;
		}
		
	}
}
