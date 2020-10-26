package org.edumips64.ui.swing;

import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;

public class HelpViewerFlyingSaucer extends JFrame {

  public HelpViewerFlyingSaucer(String helpPath, String title) {
    XHTMLPanel panel = new XHTMLPanel();
    try {
      panel.setDocument(new File(helpPath));
    } catch (Exception e) {
      System.out.println(e);
    }
    FSScrollPane pane = new FSScrollPane(panel);
    getContentPane().add(pane);
    setMinimumSize(new Dimension(800, 600));
    setTitle(title);
  }
}