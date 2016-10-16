package com.huawei.esdk.uc.demo.model;

import com.microsoft.projectoxford.face.contract.FaceRectangle;

/**
 * Created by Johnson on 2016/10/16.
 */

public class Emotion {
  FaceRectangle faceRectangle;
  Scores scores;

  public FaceRectangle getFaceRectangle() {
    return faceRectangle;
  }

  public Scores getScores() {
    return scores;
  }
}
