package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;

import jsinterop.annotations.JsType;

import org.edumips64.core.CPU;
import org.edumips64.core.Parser;
import org.edumips64.core.is.HaltException;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;

import java.util.logging.Logger;

@JsType(namespace = "jsedumips64")
public class WebUi implements EntryPoint {
  private CPU cpu;
  private Parser parser;
  private ConfigStore config;
  private FileUtils fu;

  private final String defaultCode = ".data\n.word64 10\n.code\nlw r1, 0(r0)\nSYSCALL 0";

  public String runAndGetRegisters() {
    try {
      cpu.reset();
      parser.doParsing(defaultCode);
      cpu.setStatus(CPU.CPUStatus.RUNNING);
      while (true) {
        cpu.step();
      }
    } catch (HaltException e) {
      return cpu.gprString();
    } catch (Exception e) {
      return "ERROR";
    }
  }

  private native void runSimulatorJS() /*-{
    $wnd.run();
  }-*/;

  @Override
  public void onModuleLoad() {
    runSimulatorJS();
  }

  public void init() {
    // Simulator initialization.
    Logger logger = Logger.getLogger("NameOfYourLogger");
    config = ConfigManager.getTmpConfig();
    ConfigManager.setConfig(config);
    fu = new NullFileUtils();
    Parser.createInstance(fu);
    parser = Parser.getInstance();
    cpu = CPU.getInstance();
  }
}
