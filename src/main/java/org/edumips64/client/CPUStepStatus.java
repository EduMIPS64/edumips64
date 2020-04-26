package org.edumips64.client;

import com.google.gwt.json.client.JSONObject;

// Simple holder for a single step's status.
// Used to avoid JSON encoding/decoding within the WebUi logic.
public class CPUStepStatus {
  public Result result;
  public boolean terminated = false;

  public CPUStepStatus(Result r) {
    this.result = r;
  }
  public CPUStepStatus(Result r, boolean terminated) {
    this.result = r;
    this.terminated = terminated;
  }

  public JSONObject toJsonObject() {
    return new FluentJsonObject()
      .put("terminated", terminated)
      .put("success", result.success)
      .put("errorMessage", result.errorMessage)
      .toJsonObject();
  }
}
