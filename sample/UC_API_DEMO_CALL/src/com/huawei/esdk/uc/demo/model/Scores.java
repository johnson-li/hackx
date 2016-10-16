package com.huawei.esdk.uc.demo.model;

/**
 * Created by Johnson on 2016/10/16.
 */

public class Scores {
  float anger;
  float contempt;
  float disgust;
  float fear;
  float happiness;
  float neutral;
  float sadness;
  float surprise;

  public EmotionItem getEmotionItem() {
    if (anger >= anger && anger >= contempt && anger >= disgust && anger >= fear && anger >= happiness && anger >= neutral && anger >= sadness && anger >= surprise) {
      return EmotionItem.ANGER;
    }
    if (contempt >= anger && contempt >= contempt && contempt >= disgust && contempt >= fear && contempt >= happiness && contempt >= neutral && contempt >= sadness && contempt >= surprise) {
      return EmotionItem.CONTEMPT;
    }
    if (disgust >= anger && disgust >= contempt && disgust >= disgust && disgust >= fear && disgust >= happiness && disgust >= neutral && disgust >= sadness && disgust >= surprise) {
      return EmotionItem.DISGUST;
    }
    if (fear >= anger && fear >= contempt && fear >= disgust && fear >= fear && fear >= happiness && fear >= neutral && fear >= sadness && fear >= surprise) {
      return EmotionItem.FEAR;
    }
    if (happiness >= anger && happiness >= contempt && happiness >= disgust && happiness >= fear && happiness >= happiness && happiness >= neutral && happiness >= sadness && happiness >= surprise) {
      return EmotionItem.HAPPINESS;
    }
    if (neutral >= anger && neutral >= contempt && neutral >= disgust && neutral >= fear && neutral >= happiness && neutral >= neutral && neutral >= sadness && neutral >= surprise) {
      return EmotionItem.NEUTRAL;
    }
    if (sadness >= anger && sadness >= contempt && sadness >= disgust && sadness >= fear && sadness >= happiness && sadness >= neutral && sadness >= sadness && sadness >= surprise) {
      return EmotionItem.SADNESS;
    }
    if (surprise >= anger && surprise >= contempt && surprise >= disgust && surprise >= fear && surprise >= happiness && surprise >= neutral && surprise >= sadness && surprise >= surprise) {
      return EmotionItem.SURPRISE;
    }
    return null;
  }

  public enum EmotionItem {
    ANGER, CONTEMPT, DISGUST, FEAR, HAPPINESS, NEUTRAL, SADNESS, SURPRISE
  }
}
