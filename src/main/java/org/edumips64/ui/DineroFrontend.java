/* DineroFrontend.java
 *
 * Graphical frontend for DineroIV
 * (c) 2006 Andrea Spadaccini
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.edumips64.ui;

import org.edumips64.core.Dinero;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.io.LocalWriterAdapter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.swing.*;

/** Graphical frontend for DineroIV
 *  @author Andrea Spadaccini
 */

public class DineroFrontend extends JDialog {
  // Attributes are static in order to make them accessible from
  // the nested anonymous classes. They can be static, because at most
  // one instance of DineroFrame will be created in EduMIPS64
  private static final Logger logger = Logger.getLogger(DineroFrontend.class.getName());
  private static JLabel pathLabel, paramsLabel;
  private static JTextField path, params;
  private static JButton browse, execute;
  private static JTextArea result;
  private static Container cp;
  private Dinero dinero;

  private class StreamReader extends Thread {
    private InputStream stream;
    private String name;
    private LinkedList<String> contents;
    private boolean finished = false;
    StreamReader(InputStream stream, String name) {
      this.stream = stream;
      this.name = name;
      this.contents = new LinkedList<>();
      this.finished = false;
    }

    public void run() {
      logger.info("Starting the " + name + " StreamReader");
      BufferedReader br = new BufferedReader(new InputStreamReader(stream));
      String line;

      try {
        while ((line = br.readLine()) != null) {
          contents.add(line);
        }

        logger.info("Finished reading from the " + name + " StreamReader");
      } catch (IOException e) {
        logger.severe("Exception while reading from the " + name + " StreamReader: " + e);
      }

      finished = true;
    }

    public LinkedList<String> getContents() {
      return contents;
    }

    // Will always be called after join()
    boolean isFinished() {
      return finished;
    }
  }

  private LinkedList<String> extractSimulationResults(LinkedList<String> stdout) {
    LinkedList<String> result = new LinkedList<>();
    boolean found = false;

    for (String line : stdout) {
      if (line.equals("---Simulation complete.")) {
        found = true;
      }

      if (found) {
        result.add(line + "\n");
      }
    }

    return result;
  }

  public DineroFrontend(Frame owner, Dinero dinero) {
    super(owner);
    this.dinero = dinero;
    setTitle("Dinero frontend");
    cp = rootPane.getContentPane();
    cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

    final ConfigStore config = ConfigManager.getConfig();

    Dimension hSpace = new Dimension(5, 0);
    Dimension vSpace = new Dimension(0, 5);

    pathLabel = new JLabel("DineroIV executable path:");
    paramsLabel = new JLabel("Command line parameters:");

    path = new JTextField(config.getString("dineroIV"));
    params = new JTextField("-l1-usize 512 -l1-ubsize 64");

    path.setPreferredSize(new Dimension(400, 26));
    path.setMaximumSize(new Dimension(1000, 26));
    path.setMinimumSize(new Dimension(50, 25));

    params.setPreferredSize(new Dimension(400, 26));
    params.setMaximumSize(new Dimension(1000, 26));
    params.setMinimumSize(new Dimension(50, 26));

    params.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          execute.doClick();
        }
      }
    });

    browse = new JButton("Browse...");
    browse.setAlignmentX(Component.RIGHT_ALIGNMENT);
    execute = new JButton("Execute");
    execute.setAlignmentX(Component.CENTER_ALIGNMENT);

    browse.addActionListener(e -> {
      JFileChooser jfc = new JFileChooser();
      int val = jfc.showOpenDialog(null);

      if (val == JFileChooser.APPROVE_OPTION) {
        config.putString("dineroIV", jfc.getSelectedFile().getPath());
        path.setText(jfc.getSelectedFile().getPath());
      }
    });

    execute.addActionListener(e -> {
      try {
        String dineroPath = path.getText();
        String paramString = params.getText();

        LinkedList<String> paramsList = new LinkedList<>();
        paramsList.add(dineroPath);

        Collections.addAll(paramsList, paramString.split(" "));

        // Clean up the JTextArea
        result.setText("");

        logger.info("Starting the Dinero process.");
        Process process = Runtime.getRuntime().exec(paramsList.toArray(new String[0]));

        logger.info("Creating and starting reader threads for stdout and stderr");
        StreamReader stdoutReader = new StreamReader(process.getInputStream(), "stdout");
        StreamReader stderrReader = new StreamReader(process.getErrorStream(), "stderr");
        stdoutReader.start();
        stderrReader.start();

        logger.info("Sending the tracefile to Dinero via stdin");
        // Let's send the tracefile to Dinero
        PrintWriter dineroIn = new PrintWriter(process.getOutputStream());
        dinero.writeTraceData(new LocalWriterAdapter(dineroIn));
        dineroIn.flush();
        dineroIn.close();

        // Well, wait for Dinero to terminate
        logger.info("Data sent. Waiting for Dinero to terminate.");
        process.waitFor();
        logger.info("Dinero terminated.");
        stdoutReader.join(10000);
        stderrReader.join(10000);
        logger.info("Reader threads have been joined. Results: " + stdoutReader.isFinished() + ", " + stderrReader.isFinished());

        // Debug info
        logger.info("STDOUT: " + stdoutReader.getContents());
        logger.info("STDERR: " + stderrReader.getContents());

        logger.info("Writing data to the JTextArea..");
        LinkedList<String> simulationResults = extractSimulationResults(stdoutReader.getContents());

        if (simulationResults.isEmpty()) {
          result.append(">> Errors while retrieving the simulation results.");
          result.append(">> STDOUT: " + stdoutReader.getContents());
          result.append(">> STDERR: " + stderrReader.getContents());
        } else {
          result.append(">> Dinero path: " + dineroPath + "\n");
          result.append(">> Dinero parameters: " + paramString + "\n");
          result.append(">> Simulation results:\n");

          for (String line : simulationResults) {
            result.append(line);
          }
        }

        logger.info("DineroFrontend: all done.");
      } catch (InterruptedException ie) {
        result.append(">> ERROR: " + ie);
        logger.severe("InterruptedException: " + ie);
      } catch (IOException ioe) {
        result.append(">> ERROR: " + ioe);
        logger.severe("IOException: " + ioe);
      } catch (Exception ex) {
        result.append(">> ERROR: " + ex);
        logger.severe("Exception: " + ex);
      }
    });

    Box dineroEx = Box.createHorizontalBox();
    dineroEx.add(Box.createHorizontalGlue());
    dineroEx.add(pathLabel);
    dineroEx.add(Box.createRigidArea(hSpace));
    dineroEx.add(path);
    dineroEx.add(Box.createRigidArea(hSpace));
    dineroEx.add(browse);
    cp.add(dineroEx);

    cp.add(Box.createRigidArea(vSpace));

    Box cmdLine = Box.createHorizontalBox();
    cmdLine.add(Box.createHorizontalGlue());
    cmdLine.add(paramsLabel);
    cmdLine.add(Box.createRigidArea(hSpace));
    cmdLine.add(params);
    cmdLine.add(Box.createRigidArea(hSpace));
    cp.add(cmdLine);
    cp.add(Box.createRigidArea(vSpace));

    result = new JTextArea();
    result.setBorder(BorderFactory.createTitledBorder("Messages"));
    result.setEditable(false);
    result.setFont(new Font("Monospaced", Font.PLAIN, 12));

    cp.add(execute);
    cp.add(Box.createRigidArea(vSpace));
    cp.add(new JScrollPane(result));

    setSize(850, 500);
  }
}
