package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

import org.edumips64.core.CPU;
import org.edumips64.core.Parser;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigManager;

public class WebUi implements EntryPoint {
  private CPU cpu;
  private Parser parser;
  private ConfigStore config = ConfigManager.getTmpConfig();

  public void onModuleLoad() {
    Button b = new Button("Start", new ClickHandler() {
      public void onClick(ClickEvent event) {
        Window.alert("THE RESULTS OF THE COMPUTATION.");
      }
    });

    RootPanel.get().add(b);
  }
}
