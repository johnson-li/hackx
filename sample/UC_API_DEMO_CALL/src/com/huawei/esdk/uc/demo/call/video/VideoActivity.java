package com.huawei.esdk.uc.demo.call.video;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.huawei.common.BaseData;
import com.huawei.common.BaseReceiver;
import com.huawei.demo.uc.R;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.esdk.uc.demo.call.VoipFunction;
import com.huawei.esdk.uc.demo.call.media.MediaActivity;
import com.huawei.voip.data.OrientChange;

public class VideoActivity extends Activity implements OnClickListener, BaseReceiver
{
	private Button btnSwitchCamera;
	private Button btnHangup;
	private Button btnRemoveVideo;
	private FrameLayout localView;
	private FrameLayout remoteView;
	private VideoFunction videoHolder;
	private static final String TAG = VideoActivity.class.getSimpleName();

	/** 从视频通信降为语音通话 */
	private static final int VIDEO_COLOSE = 1001;

	/** 关闭视频通信 */
	private static final int CLOSED = 1002;

	/** 视频通信建立成功 */
	private static final int VIDEOUPDATE = 1003;

	private static final int VIDEO_ORIENT_CHANGE = 1016;

	private String[] sipActions;

	private Handler handler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		// 保持屏幕常亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				+ WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		setContentView(R.layout.vedio_ui_activity);
		videoHolder = VideoFunction.getIns();
		initHandler();

		initSipReceiver();
		initCompenent();

	}

	/***
	 * 注册广播，过滤广播
	 */
	private void initSipReceiver()
	{
		sipActions = new String[] { VoipFunction.VIDEO_REMOVE, VoipFunction.CALL_CLOSED, VoipFunction.VIDEO_ADD_SUCESS,
				VoipFunction.REFRESH_LOCAL_VIEW, VoipFunction.REFRESH_REMOTE_VIEW, VoipFunction.VIDEO_CHANGE_ORIENT };
		VoipFunction.getInstance().registerBroadcast(this, sipActions);
	}

	/**
	 * UI线程更新
	 */
	private void initHandler()
	{
		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) 
			{
				Logger.debug(TAG, " | msg.what=" + msg.what);
				switch (msg.what) {
				case VIDEO_COLOSE:

					// 从视频通信降为语音通话
					skipMediaActivity();
					break;

				case VIDEOUPDATE:
					// 建立视频通信成功后，初始化界面，接收到的帧画面显示出来
					addSufaceView(true);
					break;

				case CLOSED:
					// 关闭视频通信
					finish();
					break;
				case VIDEO_ORIENT_CHANGE:
					// 屏幕横竖屏切换
					// changeScreenOrient(msg.arg1);
					break;
				default:
					break;
				}
			}
		};
	}

	private void initCompenent() 
	{
		setScreenOrient();
		btnSwitchCamera = (Button) findViewById(R.id.switch_camera);
		btnHangup = (Button) findViewById(R.id.hang_up);
		btnRemoveVideo = (Button) findViewById(R.id.remove_video);
		localView = (FrameLayout) findViewById(R.id.local_video);
		remoteView = (FrameLayout) findViewById(R.id.remote_video);
		btnSwitchCamera.setOnClickListener(this);
		btnHangup.setOnClickListener(this);
		btnRemoveVideo.setOnClickListener(this);
	}

	/**
	 * 接收VoipNotification中发出的广播，通知主线程，执行相应的处理
	 */
	@Override
	public void onReceive(String id, BaseData data) 
	{
		if (VoipFunction.VIDEO_REMOVE.equals(id))
		{
			handler.sendEmptyMessage(VIDEO_COLOSE);
		} 
		else if (VoipFunction.CALL_CLOSED.equals(id))
		{
			handler.sendEmptyMessage(CLOSED);
		}
		else if (VoipFunction.VIDEO_ADD_SUCESS.equals(id))
		{
			handler.sendEmptyMessageDelayed(VIDEOUPDATE, 5000l);
		} 
		else if (VoipFunction.REFRESH_LOCAL_VIEW.equals(id))
		{
			handler.sendEmptyMessage(VIDEOUPDATE);
		} 
		else if (VoipFunction.VIDEO_CHANGE_ORIENT.equals(id))
		{
			if (data instanceof OrientChange)
			{
				Message msg = new Message();
				msg.what = VIDEO_ORIENT_CHANGE;
				msg.arg1 = ((OrientChange) data).getOrient();
				handler.sendMessage(msg);
			}
		}
	}

	private void skipMediaActivity() 
	{
		Intent intent = new Intent(this, MediaActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onResume() 
	{

		// 默认本地摄像头捕获画面
		addSufaceView(false);
		super.onResume();
	}

	@Override
	protected void onPause() 
	{

		super.onPause();
	}

	@Override
	protected void onStop()
	{
		removeSurfaceView();
		super.onStop();
	}

	private void removeSurfaceView() 
	{
		localView.removeAllViews();
		remoteView.removeAllViews();
	}

	@Override
	protected void onDestroy()
	{

		super.onDestroy();
		VoipFunction.getInstance().unRegisterBroadcast(this, sipActions);
	}

	/**
	 * 画面显示
	 * 
	 * @param onlyLocal
	 */
	private void addSufaceView(boolean onlyLocal)
	{
		if (!onlyLocal)
		{
			addSufaceView(remoteView, videoHolder.getRemoteVideoView());
		}
		addSufaceView(localView, videoHolder.getLocalVideoView());
	}

	private void addSufaceView(ViewGroup container, SurfaceView child) 
	{
		if (child == null)
		{
			return;
		}
		if (child.getParent() != null) 
		{
			ViewGroup vGroup = (ViewGroup) child.getParent();
			vGroup.removeAllViews();
		}
		container.addView(child);
	}

	@Override
	public void onClick(View v) 
	{

		switch (v.getId())
		{
		case R.id.switch_camera:
			VideoFunction.getIns().switchCamera();
			break;
		case R.id.hang_up:
			VoipFunction.getInstance().hangup();
			finish();
			break;
		case R.id.remove_video:
			VoipFunction.getInstance().closeVideo();
			skipMediaActivity();
			finish();
			break;

		default:
			break;
		}
	}

	private void setScreenOrient()
	{
		int orient = videoHolder.getOrient();
//		final int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
		if (VideoFunction.LANDSCAPE == orient)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else if (VideoFunction.REVERSE_LANDSCAPE == orient)
		{
			//在2.2中可以设置屏幕的方向为反转横屏:setRequestedOrientation(8);
			//因为系统没有公开出这个参数的设置，
			//不过在源码里面已经定义了SCREEN_ORIENTATION_REVERSE_LANDSCAPE这个参数
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO)
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			}
			// 2.3或是以后可以直接设置
			else if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			}
		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@Override
	public void onBackPressed()
	{
//		super.onBackPressed();
		Toast.makeText(VideoActivity.this, "请点击挂断按键结束通话", Toast.LENGTH_LONG).show();
	}
}
