package org.edumips64.core;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.edumips64.core.is.*;

public class BranchPredictor {
  public static final int MAX_PREDICTION_ENTRY1 = 4000;
  public static final int MAX_PREDICTION_ENTRY2 = 2000;
  /*******************Prediction Strategy*******************/
  public int prediction_mode; // 1:2-bit predictor
                              // 2:(2,1) predictor
                              // 3:predicting Untaken
                              // 4:predicting Taken
  /*********************************************************/

  private HashMap<Integer, Prediction> prediction_map;
  private Boolean[] global_history;

  protected static final Logger logger = Logger.getLogger(BranchPredictor.class.getName());

  // Constructor
  public BranchPredictor(){
    prediction_map = new HashMap<>();
    global_history = new Boolean[2];
    setPredictionMode(3); // predicting untaken as default

    //initialize global_history
    //insert two untaken branches
    global_history[0] = false;
    global_history[1] = false;
    logger.info("Insert three untaken branches to global branch history.");
    logger.info(String.valueOf(global_history.length) + " elements in Queue global_history");
  }

  public void setPredictionMode(int mode){
    prediction_mode = mode;
  }

  /**getPrediction
   * return prediction outcome if record exists
   * add new instruction into prediction buffer if there was not record
   * @param address
   * @param instruction_name
   * @return boolean prediction outcome
   */
  public boolean getPrediction(int address, String instruction_name){
    /****Static Prediction****/
    if (prediction_mode == 3) { return false; }
    else if(prediction_mode == 4) {return true; }

    /****Dynamic Prediction****/
    boolean has_record = prediction_map.containsKey(address);

    // insert a new instruction into prediction_map
    if (!has_record) {
      // prediction_map is full => remove the first inserted element
      if (prediction_mode == 1) {
        if (prediction_map.size() > MAX_PREDICTION_ENTRY1) {
          int first_key = prediction_map.entrySet().iterator().next().getKey();
          prediction_map.remove(first_key);
        }
      }
      else if (prediction_mode == 2){
        if (prediction_map.size() > MAX_PREDICTION_ENTRY2) {
          int first_key = prediction_map.entrySet().iterator().next().getKey();
          prediction_map.remove(first_key);
        }
      }
      Prediction new_prediction;
      new_prediction = new Prediction(instruction_name);
      prediction_map.put(address, new_prediction);

      //debug
      ShowPredictionMap();

      // predicted untaken for new instructions
      return false;
    }
    // has record
    else {
      Prediction pred;
      if (prediction_mode == 1) {
        pred = prediction_map.get(address);
        return pred.getPrediction2bit();
      }
      else if (prediction_mode == 2){
        pred = prediction_map.get(address);
        String pred_debug = pred.getPredictionDynamic21(global_history)? "true":"false";
        ShowGlobalHistory();
        logger.info("now the prediction_table is:");
        ShowPredictionMap();
        logger.info("got prediction = " + pred_debug);
        return pred.getPredictionDynamic21(global_history);
      }
    }
    return false;
  }

  public void UpdatePrediction(int address, boolean actual_outcome){
    Prediction pred;
    pred = prediction_map.get(address);
    if (prediction_mode == 1){
      logger.info("UpdatePrediction");
      pred.UpdatePrediction2bit(actual_outcome);
    }
    else if (prediction_mode == 2){
      logger.info("UpdatePrediction");
      pred.UpdatePrediction21(global_history, actual_outcome);
      UpdateGlobalHistory(actual_outcome);
      ShowGlobalHistory();
      ShowPredictionMap();
    }
  }

  public void UpdateGlobalHistory(boolean actual_outcome){
    global_history[0] = global_history[1];
    global_history[1] = actual_outcome;
  }

  public void reset(){
    prediction_map.clear();
    global_history[0] = false;
    global_history[1] = false;
  }

  public void ShowGlobalHistory(){
    String gh1 = global_history[0]? "1" : "0";
    String gh2 = global_history[1]? "1" : "0";
    logger.info("Global History: " + gh1 + " " + gh2);
  }

  public void ShowPredictionMap(){
    for (Integer key : prediction_map.keySet()) {
      logger.info(prediction_map.get(key).toString());
    }
  }
}
