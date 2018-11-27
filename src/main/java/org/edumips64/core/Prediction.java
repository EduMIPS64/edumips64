package org.edumips64.core;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.edumips64.core.is.*;

public class Prediction {
  private String instruction;   // instruction number
  private boolean[] prediction; // an array of prediction of the instruction


  // Constructor
  public Prediction(InstructionInterface inst) {
    instruction = inst.getFullName();
    prediction = new boolean[4];
    for (int i=0; i<4; i++){
      prediction[i] = false;
    }
  }

  public Prediction(String instruction_name) {
    instruction = instruction_name;
    prediction = new boolean[4];
    for (int i=0; i<4; i++){
      prediction[i] = false;
    }
  }

  /**
   * Get properties
   **/
  public String getInstructionName() {
    return instruction;
  }

  /**
   * Get Predictions
   */
  public boolean getPrediction(int index) {
    return prediction[index];
  }

  public boolean getPrediction2bit(){
    if (prediction[0] == false && prediction[1] == false){ return false; }
    else if (prediction[0] == false && prediction[1] == true) { return false; }
    else if (prediction[0] == true && prediction[1] == false) { return true; }
    else { return true; }
  }

  public boolean getPredictionDynamic21(Boolean[] global_history){
    if (global_history[0] == false && global_history[1] == false){ return prediction[0]; }
    else if (global_history[0] == false && global_history[1] == true){ return prediction[1]; }
    else if (global_history[0] == true && global_history[1] == false){ return prediction[2]; }
    else {return prediction[3]; }
  }


  /**
   * Update Predictions
   **/
  public void UpdatePrediction2bit(boolean actual_outcome){
    // 00
    if (prediction[0] == false && prediction[1] == false){
      if (actual_outcome) { prediction[1] = true; } // 01
    }
    // 01
    else if (prediction[0] == false && prediction[1] == true) {
      if (actual_outcome) { prediction[0] = true; } // 11
      else { prediction[1] = false; } // 00
    }
    // 10
    else if (prediction[0] == true && prediction[1] == false) {
      if (actual_outcome) { prediction[1] = true; } // 11
      else { prediction[0] = false; } // 00
    }
    // 11
    else {
      if (!actual_outcome) { prediction[1] = false; } //10
    }
  }

  public void UpdatePrediction21(Boolean[] global_prediction, boolean actual_outcome){
    if (global_prediction[0] == false && global_prediction[1] == false){ prediction[0] = actual_outcome; }
    else if (global_prediction[0] == false && global_prediction[1] == true) { prediction[1] = actual_outcome; }
    else if (global_prediction[0] == true && global_prediction[1] == false) { prediction[2] = actual_outcome; }
    else { prediction[3] = actual_outcome; }
  }

  public String toString(){
    String str = "";
    String int2str;
    str += instruction;
    for (int i=0; i<4; i++){
      int2str = prediction[i]? "1" : "0";
      str += " " + int2str;
    }
    return str;
  }
}