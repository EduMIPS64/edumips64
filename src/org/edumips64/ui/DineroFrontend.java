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

import org.edumips64.utils.Config;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.*;
import javax.swing.border.*;

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
  private class ReadStdOut extends Thread {
    public boolean finish = false;
    private BufferedReader stdOut;
    private BufferedReader stdErr;
    public ReadStdOut(BufferedReader stdOut, BufferedReader stdErr, JTextArea result) {
      this.stdOut = stdOut;
      this.stdErr = stdErr;
    }
    public void run() {
      logger.info("Stdout reader is starting now.");
      boolean found = false;
      String s;

      try {
        while (!finish) {
          logger.info("Waiting for stdout..");

          if (stdOut.ready()) {
            logger.info("There's data to read.");

            while ((s = stdOut.readLine()) != null) {
              if (s.equals("---Simulation complete.")) {
                found = true;
              }

              if (found) {
                result.append(s + "\n");
              }
            }
          }

          logger.info("Waiting for stderr..");

          if (stdErr.ready()) {
            logger.info("There's data to read.");

            while ((s = stdErr.readLine()) != null) {
              result.append(">> Dinero error: " + s + "\n");
            }
          }
        }
      } catch (java.io.IOException ioe) {
        logger.info("IOException while reading stdout: " + ioe);
        result.append(">> ERROR: " + ioe);
      }
    }
  }

  public DineroFrontend(Frame owner) {
    super(owner);
    setTitle("Dinero frontend");
    cp = rootPane.getContentPane();
    cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

    Dimension hSpace = new Dimension(5, 0);
    Dimension vSpace = new Dimension(0, 5);

    pathLabel = new JLabel("DineroIV executable path:");
    paramsLabel = new JLabel("Command line parameters:");

    path = new JTextField(Config.getString("dineroIV"));
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

    browse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();
        int val = jfc.showOpenDialog(null);

        if (val == JFileChooser.APPROVE_OPTION) {
          Config.putString("dineroIV", jfc.getSelectedFile().getPath());
          path.setText(jfc.getSelectedFile().getPath());
        }
      }
    });

    execute.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          // Process representing Dinero
          String dineroPath = path.getText();
          String paramString = params.getText();

          // Cleaning the JTextArea
          result.setText("");
          result.append(">> Dinero path: " + dineroPath + "\n");
          result.append(">> Dinero parameters: " + paramString + "\n");

          LinkedList<String> paramsList = new LinkedList<String>();
          paramsList.add(dineroPath);

          for (String p : paramString.split(" ")) {
            paramsList.add(p);
          }

          logger.info("Starting the Dinero process.");
          Process dinero = Runtime.getRuntime().exec(paramsList.toArray(new String[0]));
          result.append(">> Simulation results:\n");
          // Readers associated with Dinero output streams
          BufferedReader stdErr = new BufferedReader(new InputStreamReader(dinero.getErrorStream()));
          BufferedReader stdOut = new BufferedReader(new InputStreamReader(dinero.getInputStream()));
          ReadStdOut th = null;

          if (org.edumips64.Main.isWindows()) {
            logger.info("Under Windows, starting the external stdout/stderr reader.");
            th = new ReadStdOut(stdOut, stdErr, result);
            th.start();
          }

          logger.info("Sending the tracefile to Dinero via stdin..");
          // Writer associated with Dinero input streams
          PrintWriter dineroIn = new PrintWriter(dinero.getOutputStream());
          String s = new String();

          // Let's send the tracefile to Dinero
          org.edumips64.core.Dinero.getInstance().writeTraceData(dineroIn);
          dineroIn.flush();
          dineroIn.close();

          try {
            // Well, wait for Dinero to terminate
            logger.info("Data sent. Waiting for Dinero to terminate.");
            dinero.waitFor();
            logger.info("Dinero terminated.");
          } catch (InterruptedException ie) {
            logger.severe("InterruptedException: " + ie);
          }

          if (org.edumips64.Main.isWindows()) {
            logger.severe("Signaling to the thread that Dinero has terminated.");
            th.finish = true;
          } else {
            boolean found = false;

            // Let's get the results
            logger.info("Waiting for stdout..");

            if (stdOut.ready()) {
              logger.info("There's data to read.");

              while ((s = stdOut.readLine()) != null) {
                if (s.equals("---Simulation complete.")) {
                  found = true;
                }

                if (found) {
                  result.append(s + "\n");
                }
              }
            }

            logger.info("Waiting for stderr..");

            if (stdErr.ready()) {
              logger.info("There's data to read.");

              while ((s = stdErr.readLine()) != null) {
                result.append(">> Dinero error: " + s + "\n");
              }
            }
          }

        } catch (java.io.IOException ioe) {
          logger.severe("IOException: " + ioe);
          result.append(">> ERROR: " + ioe);
        }
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
