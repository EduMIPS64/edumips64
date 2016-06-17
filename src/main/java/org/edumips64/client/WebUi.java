package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

import com.google.gwt.user.client.ui.TextArea;
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

  private TextArea code, registers, memory;
  private Button b;

  public void onModuleLoad() {
    // Simulator initialization.
    config = ConfigManager.getTmpConfig();
    ConfigManager.setConfig(config);
    fu = new NullFileUtils();
    Parser.createInstance(fu);
    parser = Parser.getInstance();
    cpu = CPU.getInstance();

    // Web UI initialization.
    code = new TextArea();
    code.setCharacterWidth(100);
    code.setVisibleLines(20);
    code.setText(".data\n.word64 10\n.code\nlw r1, 0(r0)\nSYSCALL 0");

    registers = new TextArea();
    registers.setCharacterWidth(100);
    registers.setVisibleLines(20);

    memory = new TextArea();
    memory.setCharacterWidth(100);
    memory.setVisibleLines(20);

    b = new Button("Run", new ClickHandler() {
      public void onClick(ClickEvent event) {
        try {
          cpu.reset();
          parser.doParsing(code.getText());
          cpu.setStatus(CPU.CPUStatus.RUNNING);
          while (true) {
            cpu.step();
          }
        } catch (HaltException e) {
          registers.setText(cpu.gprString());
          memory.setText(cpu.getMemory().toString());
        } catch (Exception e) {
          Window.alert("PROBLEM: " + e.toString());
        }
      }
    });

    RootPanel.get().add(code);
    RootPanel.get().add(b);
    RootPanel.get().add(registers);
    RootPanel.get().add(memory);
  }
}
