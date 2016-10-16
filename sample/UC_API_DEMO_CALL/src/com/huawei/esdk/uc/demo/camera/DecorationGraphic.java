package com.huawei.esdk.uc.demo.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.gms.vision.face.Face;
import com.huawei.demo.uc.R;

/**
 * Created by Johnson on 2016/10/16.
 */

public class DecorationGraphic extends GraphicOverlay.Graphic {
  private static final float FACE_POSITION_RADIUS = 10.0f;
  private static final float ID_TEXT_SIZE = 40.0f;
  private static final float ID_Y_OFFSET = 50.0f;
  private static final float ID_X_OFFSET = -50.0f;
  private static final float BOX_STROKE_WIDTH = 5.0f;

  private volatile Face mFace;
  private Context context;
  private int mFaceId;
  private float mFaceHappiness;

  public DecorationGraphic(GraphicOverlay overlay, Context context) {
    super(overlay);
    this.context = context;
  }

  void setId(int id) {
    mFaceId = id;
  }

  void updateFace(Face face) {
    mFace = face;
    postInvalidate();
  }

  @Override
  public void draw(Canvas canvas) {
    Face face = mFace;
    if (face == null) {
      return;
    }
    float centerX = translateX(face.getPosition().x + face.getWidth() / 2);
    float centerY = translateY(face.getPosition().y);
    Drawable drawable = context.getResources().getDrawable(R.drawable.pirate_hat);
    int width = (int) face.getWidth();
    int height = (int) (drawable.getIntrinsicHeight() * face.getWidth() / drawable.getIntrinsicWidth());
    drawable.setBounds((int) centerX - width, (int) centerY - height, (int) centerX + width, (int) centerY + height);
    drawable.draw(canvas);
  }
}
