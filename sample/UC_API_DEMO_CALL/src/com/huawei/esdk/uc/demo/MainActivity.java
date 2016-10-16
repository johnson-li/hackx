package com.huawei.esdk.uc.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.contacts.ContactClientStatus;
import com.huawei.contacts.PersonalContact;
import com.huawei.contacts.SelfDataHandler;
import com.huawei.demo.uc.R;
import com.huawei.device.DeviceManager;
import com.huawei.esdk.uc.demo.call.VoipFunction;
import com.huawei.esdk.uc.demo.contacts.ContactAddView;
import com.huawei.esdk.uc.demo.contacts.MemberAddAdapter;
import com.huawei.esdk.uc.demo.contacts.MemberAddHandler;
import com.huawei.espace.framework.common.ThreadManager;
import com.huawei.service.ServiceProxy;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener, MemberAddHandler {
  /**
   * 初始状态
   */
  public static final int STATUS_INIT = 0;
  private static final String TAG = MainActivity.class.getSimpleName();
  /**
   * 联系人完全同步
   */
  private static final int SYNCMODE = 2;
  private Button btnCall;
  private Button btnVideo;
  private Button btnLogout;
  /**
   * 已添加成员的GridView
   */
  private GridView contactsToAddGv;
  /**
   * 已添加成员的适配器
   */
  private MemberAddAdapter addAdpater;
  private RelativeLayout rlContactListView;
  private ContactAddView contactAddView;
  private IntentFilter filter;
  private View mainView;
  private PopupWindow pop;
  private String callNumber = "";
  /**
   * Voip状态
   */
  private int voipStatus = STATUS_INIT;
  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      Log.d(TAG, " onReceive| action = " + action);
      if (CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE.equals(action)) {
        contactAddView.updateSearchContactList(intent);
      } else if (CustomBroadcastConst.UPDATE_CONTACT_VIEW.equals(action)) {
        contactAddView.updateSelfContactList();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // setContentView(R.layout.main_activity);
    mainView = getLayoutInflater().inflate(R.layout.main_activity, null);
    setContentView(mainView);
    initComponent();
    refreshGridView();

    loadContact(SYNCMODE);

    filter = new IntentFilter();
    filter.addAction(CustomBroadcastConst.ACTION_SEARCE_CONTACT_RESPONSE);
    filter.addAction(CustomBroadcastConst.UPDATE_CONTACT_VIEW);
    registerBroadcast();
  }

  private void initComponent() {
    btnCall = (Button) findViewById(R.id.btn_call);
    btnVideo = (Button) findViewById(R.id.btn_video);
    btnLogout = (Button) findViewById(R.id.btn_logout);
    btnCall.setOnClickListener(this);
    btnVideo.setOnClickListener(this);
    btnLogout.setOnClickListener(this);

    contactsToAddGv = (GridView) findViewById(R.id.contacts_added_gv);
    rlContactListView = (RelativeLayout) findViewById(R.id.contact_list_rl);

    contactAddView = new ContactAddView(this);
    rlContactListView.addView(contactAddView);
  }

  /**
   * 刷新已添加成员
   */
  public void refreshGridView() {
    int columWidth = getResources().getDimensionPixelSize(R.dimen.grid_item_wight);
    // 添加一个空白列
    int colums = getAddList().size() + 1;
    // 总宽度
    int width = colums * columWidth;
    // 宽度为总宽度，高度自适应
    LayoutParams params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
    contactsToAddGv.setLayoutParams(params);
    // 设定列宽
    contactsToAddGv.setColumnWidth(columWidth);
    // 每列间隔
    contactsToAddGv.setHorizontalSpacing(0);
    contactsToAddGv.setStretchMode(GridView.NO_STRETCH);
    // 设定列数
    contactsToAddGv.setNumColumns(colums);
    final List<PersonalContact> addList = getAddList();
    addAdpater = new MemberAddAdapter(this, addList);
    // 已添加成员点击事件
    addAdpater.setOnMemberClickListener(new MemberAddAdapter.OnMemberClickListener() {
      @Override
      public void onMemberClick(int position) {
        // 移除成员
        if (position < addList.size()) {
          PersonalContact contact = addList.get(position);
          contactAddView.removeContact(contact);
          // addAdpater.notifyDataSetChanged();
        }
      }
    });
    contactsToAddGv.setAdapter(addAdpater);
  }

  /**
   * 注册广播
   */
  private void registerBroadcast() {
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
  }

  /**
   * 注销广播
   */
  private void unRegisterBroadcast() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
  }

  // 获取已添加成员的列表，需要与添加的数据分开保存，否则会引起界面错误
  private List<PersonalContact> getAddList() {
    return contactAddView.getAddContacts();
  }

  public void ShowPrompt(String text) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_call:

        if (STATUS_INIT == voipStatus) {
          callNumber = getCallNum();
          if (callNumber != "") {
            VoipFunction.getInstance().makeCall(callNumber);
          }
          Log.d(TAG, "makeCall is init");
        } else {
          ShowPrompt("已有通话在进行");
        }
        break;

      case R.id.btn_video:
        if (STATUS_INIT == voipStatus) {
          callNumber = getCallNum();
          if (callNumber != "") {
            VoipFunction.getInstance().makeVideoCall(callNumber);
          }
          Log.d(TAG, "makeVideoCall is init");
        } else {
          ShowPrompt("已有通话在进行");
        }
        break;

      case R.id.btn_logout:
        logout();
        break;

      case R.id.btn_dialog_cancle:
        if (pop != null) {
          pop.dismiss();
        }
        break;

      case R.id.btn_dialog_logout:
        boolean isLogin = getSharedPreferences(UserData.UserData, Activity.MODE_PRIVATE)
            .getBoolean(UserData.IsLogin, false);
        if (isLogin) {
          // 保存当前用户下线状态
          SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.AWAY, true);
          UCAPIApp.getApp().getService().logout(true);
          UCAPIApp.getApp().stopEspaceService(true);
          MainActivity.this.finish();
          DeviceManager.killProcess();
        }
        getSharedPreferences(UserData.UserData, Activity.MODE_PRIVATE).edit().putBoolean(UserData.IsLogin, false)
            .commit();
        SelfDataHandler.getIns().getSelfData().setStatus(ContactClientStatus.AWAY, true);
        UCAPIApp.getApp().stopEspaceService(true);
        MainActivity.this.finish();
        DeviceManager.killProcess();

        if (pop != null) {
          pop.dismiss();
        }
        break;

      default:
        break;
    }
  }

  /**
   * 限制选择的人数，一次只能与一个人通话
   * 返回联系人服务号
   *
   * @return
   */
  private String getCallNum() {
    String num;
    List<PersonalContact> personalContacts = getAddList();
    if (personalContacts.size() == 1) {
      PersonalContact contact = personalContacts.get(0);
      num = contact.getBinderNumber();
      return num;
    } else if (personalContacts.size() == 0) {
      ShowPrompt("请选择联系人");
    } else {
      ShowPrompt("只能与一个联系人建立通话");
    }
    return "";
  }


  /**
   * 加载联系人
   *
   * @param syncMode 同步模式： <li>0：不同步 <li>1：增量同步 <li>2：完全同步
   */
  public void loadContact(final int syncMode) {
    ThreadManager.getInstance().addToFixedThreadPool(new Runnable() {
      @Override
      public void run() {
        ServiceProxy serviceProxy = UCAPIApp.getApp().getService();
        if (null == serviceProxy) {
          return;
        }
        serviceProxy.loadContacts(syncMode);
      }
    });
  }

  private void logout() {
    View contentView = LayoutInflater.from(this).inflate(R.layout.logout_popwindow, null);
    Button btn_cancel = (Button) contentView.findViewById(R.id.btn_dialog_cancle);
    Button btn_logout = (Button) contentView.findViewById(R.id.btn_dialog_logout);
    pop = new PopupWindow(contentView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);

    btn_cancel.setOnClickListener(this);
    btn_logout.setOnClickListener(this);
    pop.showAtLocation(mainView, Gravity.CENTER, 0, 0);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == event.KEYCODE_BACK) {
      logout();

      return false;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onMemberChanged() {
    this.refreshGridView();
  }

  @Override
  protected void onDestroy() {
    unRegisterBroadcast();
    super.onDestroy();
  }
}
