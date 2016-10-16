package com.huawei.esdk.uc.demo.call.video;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.opengl.GLException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.common.BaseData;
import com.huawei.common.BaseReceiver;
import com.huawei.demo.uc.R;
import com.huawei.ecs.mtk.log.Logger;
import com.huawei.esdk.uc.demo.call.VoipFunction;
import com.huawei.esdk.uc.demo.call.media.MediaActivity;
import com.huawei.esdk.uc.demo.model.Emotion;
import com.huawei.esdk.uc.demo.model.Scores;
import com.huawei.voip.data.OrientChange;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHeader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.opengles.GL10;

public class VideoActivity extends Activity implements OnClickListener, BaseReceiver {
  private static final String TAG = VideoActivity.class.getSimpleName();
  /**
   * 从视频通信降为语音通话
   */
  private static final int VIDEO_COLOSE = 1001;
  /**
   * 关闭视频通信
   */
  private static final int CLOSED = 1002;
  private static final int VIDEOUPDATE = 1003;
  private static final int VIDEO_ORIENT_CHANGE = 1016;
  /**
   * 视频通信建立成功
   */
  String path = "/sdcard/img.png";
  int imageHeight, imageWidth;
  ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
  HttpClient emotionHttpClient = AndroidHttpClient.newInstance("emotion");
  private Button btnSwitchCamera;
  private Button btnHangup;
  private Button btnRemoveVideo;
  private Button recognizeButton;
  private Button emotionButton;
  private ImageView sticker;
  private FrameLayout localView;
  private FrameLayout remoteView;
  private RelativeLayout stickers;
  private VideoFunction videoHolder;
  private String[] sipActions;
  private FaceServiceClient faceServiceClient =
      new FaceServiceRestClient("426205f50ddb4637850364e97c36b7d7");
  private Handler handler = null;
  private View anger, happiness, sadness, normal;

  private Scores.EmotionItem preStatus = Scores.EmotionItem.NEUTRAL;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    // 保持屏幕常亮
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    setContentView(R.layout.vedio_ui_activity);
    videoHolder = VideoFunction.getIns();
    initHandler();

    initSipReceiver();
    initCompenent();

    startRepeatEmotionCheck();
  }

  private void startRepeatEmotionCheck() {
    service.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        Bitmap bitmap = loadBitmapFromView(remoteView.getChildAt(0));
        HttpPost method = new HttpPost("https://api.projectoxford.ai/emotion/v1.0/recognize");
        method.addHeader(new BasicHeader("Ocp-Apim-Subscription-Key", "f287e15eb92543eea6e2e5655b976b4a"));
        method.setEntity(new FileEntity(new File(path), "application/octet-stream"));
        String responseStr = null;
        try {
          HttpResponse response = emotionHttpClient.execute(method);
          InputStream is = response.getEntity().getContent();
          InputStreamReader reader = new InputStreamReader(is);
          BufferedReader bufferedReader = new BufferedReader(reader);
          StringBuilder stringBuilder = new StringBuilder();
          String str;
          while ((str = bufferedReader.readLine()) != null) {
            stringBuilder.append(str);
          }
          response.getEntity().consumeContent();
          responseStr = stringBuilder.toString();
          System.out.println(responseStr);
          if (responseStr != null) {
            final Emotion[] emotions = new Gson().fromJson(responseStr, Emotion[].class);
            VideoActivity.this.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                if (emotions == null || emotions.length != 1) {
                  Toast.makeText(VideoActivity.this, "no face detected", Toast.LENGTH_SHORT).show();
                } else {
                  Emotion emotion = emotions[0];
                  Scores.EmotionItem emotionItem = emotion.getScores().getEmotionItem();
                  Toast.makeText(VideoActivity.this, emotionItem.toString(), Toast.LENGTH_SHORT).show();
                  switch (emotionItem) {
                    case SADNESS:
                      sticker.setImageResource(R.drawable.rain_icon);
                      sticker.startAnimation(AnimationUtils.loadAnimation(VideoActivity.this, R.anim.shake));
                      break;
                    case HAPPINESS:
                      sticker.setImageResource(R.drawable.sunny);
                      sticker.startAnimation(AnimationUtils.loadAnimation(VideoActivity.this, R.anim.rotation));
                      break;
                    case NEUTRAL:
                      if (preStatus != Scores.EmotionItem.NEUTRAL) {
                        sticker.clearAnimation();
                        Animation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                        fadeOut.setStartOffset(1000);
                        fadeOut.setDuration(1000);
                        sticker.startAnimation(fadeOut);
                      }
                      break;
                    default:
                      break;
                  }
                  preStatus = emotionItem;
                }
              }
            });
          }
        } catch (Exception e) {
          System.out.println(responseStr);
          e.printStackTrace();
        }
      }
    }, 1, 1, TimeUnit.SECONDS);
  }

  /***
   * 注册广播，过滤广播
   */
  private void initSipReceiver() {
    sipActions = new String[]{VoipFunction.VIDEO_REMOVE, VoipFunction.CALL_CLOSED, VoipFunction.VIDEO_ADD_SUCESS,
        VoipFunction.REFRESH_LOCAL_VIEW, VoipFunction.REFRESH_REMOTE_VIEW, VoipFunction.VIDEO_CHANGE_ORIENT};
    VoipFunction.getInstance().registerBroadcast(this, sipActions);
  }

  /**
   * UI线程更新
   */
  private void initHandler() {
    handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
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

  private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
      throws OutOfMemoryError {
    int bitmapBuffer[] = new int[w * h];
    int bitmapSource[] = new int[w * h];
    IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
    intBuffer.position(0);

    try {
      gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
      int offset1, offset2;
      for (int i = 0; i < h; i++) {
        offset1 = i * w;
        offset2 = (h - i - 1) * w;
        for (int j = 0; j < w; j++) {
          int texturePixel = bitmapBuffer[offset1 + j];
          int blue = (texturePixel >> 16) & 0xff;
          int red = (texturePixel << 16) & 0x00ff0000;
          int pixel = (texturePixel & 0xff00ff00) | red | blue;
          bitmapSource[offset2 + j] = pixel;
        }
      }
    } catch (GLException e) {
      return null;
    }

    return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
  }

  public Bitmap loadBitmapFromView(View v) {
//    if (v instanceof ViEAndroidGLES20) {
//      EGL10 egl = (EGL10) EGLContext.getEGL();
//      GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
//      ViEAndroidGLES20 view = (ViEAndroidGLES20) v;
//      return createBitmapFromGLSurface(0, 0, view.getWidth(), view.getHeight(), gl);
//    } else {
//      Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
//      Canvas c = new Canvas(b);
//      v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
//      v.draw(c);
//      return b;
//    }
    try {
      Process sh = Runtime.getRuntime().exec("su", null, null);
      OutputStream os = sh.getOutputStream();
      os.write(("/system/bin/screencap -p " + path).getBytes("ASCII"));
      os.flush();
      os.close();
      sh.waitFor();
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inPreferredConfig = Bitmap.Config.ARGB_8888;
      Bitmap bitmap = BitmapFactory.decodeFile(path, options);
      imageHeight = bitmap.getHeight();
      imageWidth = bitmap.getWidth();
      return bitmap;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private void initCompenent() {
    setScreenOrient();
    btnSwitchCamera = (Button) findViewById(R.id.switch_camera);
    btnHangup = (Button) findViewById(R.id.hang_up);
    btnRemoveVideo = (Button) findViewById(R.id.remove_video);
    localView = (FrameLayout) findViewById(R.id.local_video);
    remoteView = (FrameLayout) findViewById(R.id.remote_video);
    stickers = (RelativeLayout) findViewById(R.id.stickers);
    recognizeButton = (Button) findViewById(R.id.recognize);
    emotionButton = (Button) findViewById(R.id.emotion);
    sticker = (ImageView) findViewById(R.id.sticker);
    anger = findViewById(R.id.anger);
    happiness = findViewById(R.id.happiness);
    sadness = findViewById(R.id.sadness);
    normal = findViewById(R.id.normal);

    OnClickListener emotionListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (v.getId()) {
          case R.id.anger:
            break;
          case R.id.happiness:
            break;
          case R.id.normal:
            break;
          case R.id.sadness:
            break;
        }
      }
    };

    anger.setOnClickListener(emotionListener);
    sadness.setOnClickListener(emotionListener);
    happiness.setOnClickListener(emotionListener);
    normal.setOnClickListener(emotionListener);
    btnSwitchCamera.setOnClickListener(this);
    btnHangup.setOnClickListener(this);
    btnRemoveVideo.setOnClickListener(this);
    emotionButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Bitmap bitmap = loadBitmapFromView(remoteView.getChildAt(0));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        new AsyncTask<InputStream, String, String>() {
          @Override
          protected String doInBackground(InputStream... inputStreams) {
            HttpClient faceHttpClient = AndroidHttpClient.newInstance("face");
            HttpPost method = new HttpPost("https://api.projectoxford.ai/emotion/v1.0/recognize");
            method.addHeader(new BasicHeader("Ocp-Apim-Subscription-Key", "f287e15eb92543eea6e2e5655b976b4a"));
            method.setEntity(new FileEntity(new File(path), "application/octet-stream"));
            try {
              HttpResponse response = faceHttpClient.execute(method);
              InputStream is = response.getEntity().getContent();
              InputStreamReader reader = new InputStreamReader(is);
              BufferedReader bufferedReader = new BufferedReader(reader);
              StringBuilder stringBuilder = new StringBuilder();
              String str;
              while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
              }
              response.getEntity().consumeContent();
              return stringBuilder.toString();
            } catch (IOException e) {
              e.printStackTrace();
            }
            return null;
          }

          @Override
          protected void onPostExecute(String s) {
            if (s != null) {
              try {
                Emotion[] emotions = new Gson().fromJson(s, Emotion[].class);
                if (emotions == null || emotions.length < 1) {
                  System.out.println("not face detected");
                } else {
                  Emotion emotion = emotions[0];
                  Scores.EmotionItem emotionItem = emotion.getScores().getEmotionItem();
                  System.out.println(emotionItem);
                }
              } catch (Exception e) {
                e.printStackTrace();
                System.out.println(s);
              }

            }
          }
        }.execute(inputStream);

      }
    });
    recognizeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Bitmap bitmap = loadBitmapFromView((remoteView.getChildAt(0)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        System.out.println("hahaha");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
            new AsyncTask<InputStream, String, Face[]>() {

              @Override
              protected void onPostExecute(Face[] faces) {
                if (faces == null || faces.length == 0) {
                  System.out.println("no face detected");
                } else {
                  if (faces.length > 1) {
                    System.out.println("more that one face detected!");
                  }
                  Face face = faces[0];
                  int left = face.faceRectangle.left + face.faceRectangle.width / 2;
                  int top = stickers.getHeight() - (imageHeight - face.faceRectangle.top);
                  RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) sticker.getLayoutParams();
                  layoutParams.leftMargin = left;
                  layoutParams.topMargin = top;
                  stickers.requestLayout();
                }
              }

              @Override
              protected Face[] doInBackground(InputStream... inputStreams) {
                try {
                  publishProgress("Detecting...");
                  System.out.println("Detecting...");
                  Face[] result = faceServiceClient.detect(
                      inputStreams[0],
                      true,         // returnFaceId
                      false,        // returnFaceLandmarks
                      null           // returnFaceAttributes: a string like "age, gender"
                  );
                  if (result == null) {
                    publishProgress("Detection Finished. Nothing detected");
                    return null;
                  }
                  publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                  return result;
                } catch (Exception e) {
                  publishProgress("Detection failed");
                  return null;
                }
              }
            };
        detectTask.execute(inputStream);
      }
    });
  }

  /**
   * 接收VoipNotification中发出的广播，通知主线程，执行相应的处理
   */
  @Override
  public void onReceive(String id, BaseData data) {
    if (VoipFunction.VIDEO_REMOVE.equals(id)) {
      handler.sendEmptyMessage(VIDEO_COLOSE);
    } else if (VoipFunction.CALL_CLOSED.equals(id)) {
      handler.sendEmptyMessage(CLOSED);
    } else if (VoipFunction.VIDEO_ADD_SUCESS.equals(id)) {
      handler.sendEmptyMessageDelayed(VIDEOUPDATE, 5000l);
    } else if (VoipFunction.REFRESH_LOCAL_VIEW.equals(id)) {
      handler.sendEmptyMessage(VIDEOUPDATE);
    } else if (VoipFunction.VIDEO_CHANGE_ORIENT.equals(id)) {
      if (data instanceof OrientChange) {
        Message msg = new Message();
        msg.what = VIDEO_ORIENT_CHANGE;
        msg.arg1 = ((OrientChange) data).getOrient();
        handler.sendMessage(msg);
      }
    }
  }

  private void skipMediaActivity() {
    Intent intent = new Intent(this, MediaActivity.class);
    startActivity(intent);
    finish();
  }

  @Override
  protected void onResume() {

    // 默认本地摄像头捕获画面
    addSufaceView(false);
    super.onResume();
  }

  @Override
  protected void onPause() {

    super.onPause();
  }

  @Override
  protected void onStop() {
    removeSurfaceView();
    super.onStop();
  }

  private void removeSurfaceView() {
    localView.removeAllViews();
    remoteView.removeAllViews();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    VoipFunction.getInstance().unRegisterBroadcast(this, sipActions);
    service.shutdown();
  }

  /**
   * 画面显示
   *
   * @param onlyLocal
   */
  private void addSufaceView(boolean onlyLocal) {
    if (!onlyLocal) {
      addSufaceView(remoteView, videoHolder.getRemoteVideoView());
    }
    addSufaceView(localView, videoHolder.getLocalVideoView());
  }

  private void addSufaceView(ViewGroup container, SurfaceView child) {
    if (child == null) {
      return;
    }
    if (child.getParent() != null) {
      ViewGroup vGroup = (ViewGroup) child.getParent();
      vGroup.removeAllViews();
    }
    container.addView(child);
  }

  @Override
  public void onClick(View v) {

    switch (v.getId()) {
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

  private void setScreenOrient() {
    int orient = videoHolder.getOrient();
//		final int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
    if (VideoFunction.LANDSCAPE == orient) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    } else if (VideoFunction.REVERSE_LANDSCAPE == orient) {
      //在2.2中可以设置屏幕的方向为反转横屏:setRequestedOrientation(8);
      //因为系统没有公开出这个参数的设置，
      //不过在源码里面已经定义了SCREEN_ORIENTATION_REVERSE_LANDSCAPE这个参数
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
      }
      // 2.3或是以后可以直接设置
      else if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
      }
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
  }

  @Override
  public void onBackPressed() {
//		super.onBackPressed();
    Toast.makeText(VideoActivity.this, "请点击挂断按键结束通话", Toast.LENGTH_LONG).show();
  }
}
