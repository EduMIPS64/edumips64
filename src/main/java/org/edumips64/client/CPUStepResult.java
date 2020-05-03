package org.edumips64.client;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

// Simple holder for a single step's status.
// Used to avoid JSON encoding/decoding within the WebUi logic.
// This extends Result so the JS code can check the known fields in Result.
@JsType(namespace="jsedumips64")
public class CPUStepResult extends Result {
  public boolean terminated = false;

  @JsIgnore
  public CPUStepResult(Result r) {
    super(r);
  }

  @JsIgnore
  public CPUStepResult(Result r, boolean terminated) {
    super(r);
    this.terminated = terminated;
  }

  public String toString() {
    return "CpuStepResult {success: " + success + ", errorMessage: " + errorMessage + ", terminated: " + terminated + "}";
  }
}
