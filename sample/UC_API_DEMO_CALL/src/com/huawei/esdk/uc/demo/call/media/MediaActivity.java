package com.huawei.esdk.uc.demo.call.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.common.BaseData;
import com.huawei.common.BaseReceiver;
import com.huawei.contacts.ContactLogic;
import com.huawei.demo.uc.R;
import com.huawei.esdk.uc.demo.call.VoipFunction;
import com.huawei.esdk.uc.demo.call.video.VideoActivity;
import com.huawei.esdk.uc.demo.call.video.VideoFunction;
import com.huawei.voip.data.EventData;

public class MediaActivity extends Activity implements OnClickListener, BaseReceiver {
  private static final int UPDATE_VIEW = 1;
  private static final int GOTO_CONF_VIEW = 4;
  private static final int GOTO_VIDEO_VIEW = 6;
  private static final int VIDEOINVITE = 11;
  private static final int UPDATE_VIDEO_FAILED = 13;
  private static final int DECLINE_VIDEO = 14;
  private static final int TIME_OUT = 30 * 1000;
  /**
   * 过滤广播 和IntentFilter功能类似
   */
  private final String[] sipBroadcast = new String[]{VoipFunction.CALL_CONNECTED, VoipFunction.CALL_HOLD,
      VoipFunction.CALL_HOLD_FAILED, VoipFunction.GOTO_CONF_VIEW, VoipFunction.VIDEO_INVITE,
      VoipFunction.VIDEO_ADD_SUCESS, VoipFunction.VIDEO_ADD_FAILED, VoipFunction.CALL_CLOSED};
  private String callNumber;
  private String displayName;
  private Button btnCancel;
  private Button btnAccept;
  private Button btnRefuse;
  private Button btnToVideo;
  private TextView indication;
  private AlertDialog videoInviteDialog;
  private Handler handler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case UPDATE_VIEW:
          updateView();
          break;
        case GOTO_VIDEO_VIEW:
          updateView();
          skipVideoActivity();
          break;
        case UPDATE_VIDEO_FAILED:

          btnToVideo.setEnabled(true);
          break;
        case VIDEOINVITE:
          showSkipVideoDialog();
          break;
        case GOTO_CONF_VIEW:
          // skipActivity((String) msg.obj);
          break;
        case DECLINE_VIDEO:
          closeVideoInviteDialog();
          VideoFunction.getIns().declineVideoInvite();
          VoipFunction.getInstance().setVideo(false);
          break;

        default:
          break;
      }
    }

    ;
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.media_ui_activity);

    registerSipCallback();

    initCompenent();
    updateView();
  }

  private void initCompenent() {
    btnRefuse = (Button) findViewById(R.id.call_refuse);
    btnAccept = (Button) findViewById(R.id.call_accept);
    btnCancel = (Button) findViewById(R.id.call_cancle);
    btnToVideo = (Button) findViewById(R.id.media_to_video);
    indication = (TextView) findViewById(R.id.indication);
    btnAccept.setOnClickListener(this);
    btnRefuse.setOnClickListener(this);
    btnCancel.setOnClickListener(this);
    btnToVideo.setOnClickListener(this);

    // 是否要在这里拨打电话
    boolean needMakeCall = getIntent().getBooleanExtra("needMakeCall", false);
    callNumber = getIntent().getStringExtra("callNumber");
    displayName = getIntent().getStringExtra("displayName");

    boolean isVideo = getIntent().getBooleanExtra("isVideo", false);
    if (needMakeCall) {
      boolean isVoipCallSuccess = VoipFunction.getInstance().doVoipCall(callNumber, isVideo);

      if (!isVoipCallSuccess) {
        Toast.makeText(this, "拨打电话失败", Toast.LENGTH_SHORT).show();

      }
    }
    if (ContactLogic.getIns().getAbility().isVideoCallAbility()) {
      btnToVideo.setVisibility(View.VISIBLE);
    } else {
      btnToVideo.setVisibility(View.GONE);
    }

  }

  private void registerSipCallback() {
    VoipFunction.getInstance().registerBroadcast(this, sipBroadcast);
  }

  private void unRegisterSipCallback() {
    VoipFunction.getInstance().unRegisterBroadcast(this, sipBroadcast);
  }

  private void showSkipVideoDialog() {
    if (videoInviteDialog == null) {
      videoInviteDialog = new AlertDialog.Builder(this)
          .setTitle(R.string.video_invite)
          .setNegativeButton(R.string.call_refuse, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              closeVideoInviteDialog();
              VideoFunction.getIns().declineVideoInvite();
              VoipFunction.getInstance().setVideo(false);
            }
          })
          .setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              VideoFunction.getIns().agreeVideoUpgradte();
              skipVideoActivity();
              closeVideoInviteDialog();
            }
          })
          .setCancelable(false).show();
    }
    // 延时30秒,时间不到关闭对话框.
    handler.sendEmptyMessageDelayed(DECLINE_VIDEO, TIME_OUT);
  }

  private void skipVideoActivity() {
    Intent intent = new Intent(this, VideoActivity.class);
    startActivity(intent);
    finish();
  }

  protected void closeVideoInviteDialog() {
    if (videoInviteDialog != null) {
      videoInviteDialog.dismiss();
      videoInviteDialog = null;
    }
  }

  @Override
  public void onReceive(String action, BaseData data) {
    if (VoipFunction.CALL_CONNECTED.equals(action)) {
      // 接通
      if (VoipFunction.getInstance().isVideo()) {
        sendMessage(GOTO_VIDEO_VIEW, null);
      } else {
        sendMessage(UPDATE_VIEW, null);
      }
    } else if (VoipFunction.CALL_HOLD.equals(action)) {
      sendMessage(UPDATE_VIEW, null);
    } else if (VoipFunction.CALL_HOLD_FAILED.equals(action)) {
      sendMessage(UPDATE_VIEW, null);
    } else if (VoipFunction.VIDEO_INVITE.equals(action)) {
      sendMessage(VIDEOINVITE, null);
    } else if (VoipFunction.GOTO_CONF_VIEW.equals(action)) {
      sendMessage(GOTO_CONF_VIEW, ((EventData) data).getRawData());
    } else if (VoipFunction.VIDEO_ADD_SUCESS.equals(action)) {
      sendMessage(GOTO_VIDEO_VIEW, null);
    } else if (VoipFunction.VIDEO_ADD_FAILED.equals(action)) {
      sendMessage(UPDATE_VIDEO_FAILED, null);
    } else if (VoipFunction.CALL_CLOSED.equals(action)) {
      finish();
    }
  }

  private void sendMessage(int what, Object obj) {
    Message msg = handler.obtainMessage(what, obj);
    handler.sendMessage(msg);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.call_cancle:
        VoipFunction.getInstance().hangup();
        VoipFunction.getInstance().setVoipStatus(VoipFunction.STATUS_INIT);
        MediaActivity.this.finish();
        break;
      case R.id.call_accept:
        VoipFunction.getInstance().answer(VoipFunction.getInstance().isVideo());
        break;
      case R.id.call_refuse:
        VoipFunction.getInstance().hangup();
        VoipFunction.getInstance().setVoipStatus(VoipFunction.STATUS_INIT);
        MediaActivity.this.finish();
        break;
      case R.id.media_to_video:
        if (VideoFunction.getIns().openVideo()) {
          btnToVideo.setEnabled(false);
        }
        break;

      default:
        break;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    unRegisterSipCallback();
  }

  /**
   * 更新通话界面按钮
   */
  private void updateView() {
    switch (VoipFunction.getInstance().getVoipStatus()) {
      case VoipFunction.STATUS_CALLING:
        btnToVideo.setEnabled(false);
        if (VoipFunction.CALL_OUT == VoipFunction.getInstance().getCallMode()) {
          btnAccept.setVisibility(View.GONE);
          btnCancel.setVisibility(View.VISIBLE);
          btnRefuse.setVisibility(View.GONE);
          indication.setVisibility(View.VISIBLE);
        } else if (VoipFunction.CALL_COME == VoipFunction.getInstance().getCallMode()) {
          btnToVideo.setVisibility(View.GONE);
          btnCancel.setVisibility(View.GONE);
          btnAccept.setVisibility(View.VISIBLE);
          btnRefuse.setVisibility(View.VISIBLE);
          indication.setVisibility(View.GONE);
        }
        break;
      case VoipFunction.STATUS_TALKING:
        btnToVideo.setEnabled(true);
        if (VoipFunction.CTD != VoipFunction.getInstance().getCallType()) {
          btnToVideo.setEnabled(!VoipFunction.getInstance().isVideo());
        }
        if (VoipFunction.CALL_COME == VoipFunction.getInstance().getCallMode()) {
          btnToVideo.setVisibility(View.GONE);
        }
        btnCancel.setVisibility(View.VISIBLE);
        btnAccept.setVisibility(View.GONE);
        btnRefuse.setVisibility(View.GONE);
        break;
      default:
        break;
    }
  }

  @Override
  public void onBackPressed() {
//		super.onBackPressed();
    Toast.makeText(MediaActivity.this, "请点击取消按键结束通话", Toast.LENGTH_LONG).show();
  }
}
