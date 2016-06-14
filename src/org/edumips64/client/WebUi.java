package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

import org.edumips64.core.CPU;
import org.edumips64.core.Parser;
import org.edumips64.core.is.HaltException;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;

public class WebUi implements EntryPoint {
  private CPU cpu;
  private Parser parser;
  private ConfigStore config;
  private FileUtils fu;

  private final String code = ".code\ndaddi r1, r0, 42\nsyscall 0";

  public void onModuleLoad() {
    config = ConfigManager.getTmpConfig();
    ConfigManager.setConfig(config);

    fu = new NullFileUtils();
    Parser.createInstance(fu);
    parser = Parser.getInstance();

    cpu = CPU.getInstance();

    Button b = new Button("Start", new ClickHandler() {
      public void onClick(ClickEvent event) {
        try {
          parser.doParsing(code);
          cpu.setStatus(CPU.CPUStatus.RUNNING);
          while (true) {
            cpu.step();
          }
        } catch (HaltException e) {
          Window.alert("ALL DONE. R1 = " + cpu.getRegister(1).getValue());
        } catch (Exception e) {
          Window.alert("PROBLEM: " + e.toString());
        }
      }
    });

    RootPanel.get().add(b);
  }
}
