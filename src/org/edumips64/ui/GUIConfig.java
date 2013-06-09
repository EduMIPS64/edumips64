/* GUIConfig.java
 *
 * This class provides a window for configuration options.
 * (c) 2006 EduMIPS64 project - Rizzo Vanni G,  Trubia Massimo (FPU modifications)
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

import org.edumips64.core.CPU;
import org.edumips64.core.is.Instruction;
import org.edumips64.Main;
import org.edumips64.utils.ConfigBuilder;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigStoreTypeException;
import org.edumips64.utils.CurrentLocale;

import java.util.*;
import java.util.logging.Logger;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * This class provides a window for configuration options.
*/
public class GUIConfig extends JDialog {

  private static final Logger logger = Logger.getLogger(GUIConfig.class.getName());
  private static ConfigStore config = ConfigBuilder.getConfig();
  String MAIN;
  String APPEARANCE;
  String FPUEXCEPTIONS;
  String FPUROUNDING;
  String BEHAVIOR;

  JTabbedPane tabPanel;
  JButton okButton;
  int width = 700, height = 300;

  // Local cache of the configuration values that will need to be applied to
  // the configuration backend.
  Map<String, Object> cache;

  public GUIConfig(final JFrame owner) {
    super(owner, CurrentLocale.getString("Config.ITEM"), true);
    logger.info("Building a new GUIConfig instance.");
    MAIN = CurrentLocale.getString("Config.MAIN");
    APPEARANCE = CurrentLocale.getString("Config.APPEARANCE");
    BEHAVIOR = CurrentLocale.getString("Config.BEHAVIOR");
    FPUEXCEPTIONS = CurrentLocale.getString("Config.FPUEXCEPTIONS");
    FPUROUNDING = CurrentLocale.getString("Config.FPUROUNDING");

    cache = new HashMap<String, Object>();

    tabPanel = new JTabbedPane();
    tabPanel.addTab(MAIN, makeMainPanel());
    tabPanel.addTab(BEHAVIOR, makeBehaviorPanel());
    tabPanel.addTab(FPUEXCEPTIONS, makeExceptionsPanel());
    tabPanel.addTab(FPUROUNDING, makeRoundingPanel());
    tabPanel.addTab(APPEARANCE, makeAppearancePanel());

    final JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    addButtons(buttonPanel);

    getRootPane().setDefaultButton(okButton);
    Container content = getContentPane();

    content.add(tabPanel, BorderLayout.CENTER);
    content.add(buttonPanel, BorderLayout.PAGE_END);

    //pack();
    setSize(width, height);
    setLocation((getScreenWidth() - getWidth()) / 2, (getScreenHeight() - getHeight()) / 2);
    setVisible(true);
  }

  GridBagLayout gbl;
  GridBagConstraints gbc;

  private JPanel makeMainPanel() {
    gbl = new GridBagLayout();
    gbc = new GridBagConstraints();

    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 10);

    JPanel panel = new JPanel();

    panel.setLayout(gbl);
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    int row = 2;

    addRow(panel, row++, "forwarding", new JCheckBox());
    addRow(panel, row++, "n_step", new JNumberField());

    // fill remaining vertical space
    grid_add(panel, new JPanel(), gbl, gbc, 0, 1, 0, row, GridBagConstraints.REMAINDER, 1);
    return panel;
  }

  private JPanel makeBehaviorPanel() {
    gbl = new GridBagLayout();
    gbc = new GridBagConstraints();

    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 10);

    JPanel panel = new JPanel();

    panel.setLayout(gbl);
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    int row = 2;

    addRow(panel, row++, "warnings", new JCheckBox());
    addRow(panel, row++, "verbose", new JCheckBox());
    addRow(panel, row++, "sleep_interval", new JNumberField());
    addRow(panel, row++, "syncexc-masked", new JCheckBox());
    addRow(panel, row++, "syncexc-terminate", new JCheckBox());

    // fill remaining vertical space
    grid_add(panel, new JPanel(), gbl, gbc, 0, 1, 0, row, GridBagConstraints.REMAINDER, 1);

    return panel;
  }

  private JPanel makeExceptionsPanel() {
    gbl = new GridBagLayout();
    gbc = new GridBagConstraints();

    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 10);

    JPanel panel = new JPanel();

    panel.setLayout(gbl);
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    int row = 2;

    addRow(panel, row++, "INVALID_OPERATION", new JCheckBox());
    addRow(panel, row++, "OVERFLOW", new JCheckBox());
    addRow(panel, row++, "UNDERFLOW", new JCheckBox());
    addRow(panel, row++, "DIVIDE_BY_ZERO", new JCheckBox());

    // fill remaining vertical space
    grid_add(panel, new JPanel(), gbl, gbc, 0, 1, 0, row, GridBagConstraints.REMAINDER, 1);

    return panel;
  }

  private JPanel makeRoundingPanel() {
    ButtonGroup bg = new ButtonGroup();
    JRadioButton rdoNearest = new JRadioButton();
    JRadioButton rdoTowardZero = new JRadioButton();
    JRadioButton rdoTowardsPlusInfinity = new JRadioButton();
    JRadioButton rdoTowardsMinusInfinity = new JRadioButton();
    bg.add(rdoNearest);
    bg.add(rdoTowardZero);
    bg.add(rdoTowardsPlusInfinity);
    bg.add(rdoTowardsMinusInfinity);

    gbl = new GridBagLayout();
    gbc = new GridBagConstraints();


    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 10);

    JPanel panel = new JPanel();

    panel.setLayout(gbl);
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    int row = 2;

    addRow(panel, row++, "NEAREST", rdoNearest);
    addRow(panel, row++, "TOWARDZERO", rdoTowardZero);
    addRow(panel, row++, "TOWARDS_PLUS_INFINITY", rdoTowardsPlusInfinity);
    addRow(panel, row++, "TOWARDS_MINUS_INFINITY", rdoTowardsMinusInfinity);

    // fill remaining vertical space
    grid_add(panel, new JPanel(), gbl, gbc, 0, 1, 0, row, GridBagConstraints.REMAINDER, 1);

    return panel;
  }

  private JPanel makeAppearancePanel() {
    gbl = new GridBagLayout();
    gbc = new GridBagConstraints();

    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 10);

    JPanel panel = new JPanel();

    panel.setLayout(gbl);
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    int row = 2;

    addRow(panel, row++, "IFColor", new JButton());
    addRow(panel, row++, "IDColor", new JButton());
    addRow(panel, row++, "EXColor", new JButton());
    addRow(panel, row++, "MEMColor", new JButton());
    addRow(panel, row++, "WBColor", new JButton());
    addRow(panel, row++, "FPAdderColor", new JButton());
    addRow(panel, row++, "FPMultiplierColor", new JButton());
    addRow(panel, row++, "FPDividerColor", new JButton());
    addRow(panel, row++, "LONGDOUBLEVIEW", new JCheckBox());
    addRow(panel, row++, "show_aliases", new JCheckBox());

    // fill remaining vertical space
    grid_add(panel, new JPanel(), gbl, gbc, 0, 1, 0, row, GridBagConstraints.REMAINDER, 1);
    return panel;
  }

  // Monster function that adds a given row (label + control) to a given
  // JPanel, and sets its behaviour according to the type of control.
  public void addRow(JPanel panel, final int row, final String key, final JComponent comp) {
    String title = CurrentLocale.getString("Config." + key.toUpperCase());
    String tip = CurrentLocale.getString("Config." + key.toUpperCase() + ".tip");
    //Setting title
    JLabel label = new JLabel(title);
    label.setHorizontalAlignment(JLabel.RIGHT);
    label.setToolTipText(tip);
    grid_add(panel, label, gbl, gbc, .1, 0, 0, row, 1, 1);



    if (comp instanceof JCheckBox) {
      final JCheckBox cbox = (JCheckBox) comp;
      //Setting Component
      cbox.setHorizontalAlignment(SwingConstants.LEFT);
      cbox.setVerticalAlignment(SwingConstants.CENTER);
      cbox.setSelected(config.getBoolean(key));

      cbox.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          logger.info("Changing " + key + " to " + cbox.getModel().isSelected());
          cache.put(key, cbox.getModel().isSelected());
        }
      });
    } else if (comp instanceof JRadioButton) {
      final JRadioButton rbut = (JRadioButton) comp;
      rbut.setHorizontalAlignment(SwingConstants.LEFT);
      rbut.setVerticalAlignment(SwingConstants.CENTER);
      rbut.setSelected(config.getBoolean(key));

      // When a radio button is clicked, the other buttons must be deselected.
      // TODO: more generic handling of radio buttons: currently we have only
      // one and this code is tailored for it.
      rbut.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          LinkedList<String> keys = new LinkedList<String>();
          keys.add("NEAREST");
          keys.add("TOWARDZERO");
          keys.add("TOWARDS_PLUS_INFINITY");
          keys.add("TOWARDS_MINUS_INFINITY");

          cache.put(key, true);
          keys.remove(key);

          for (String k : keys) {
            cache.put(k, false);
          }
        }
      });
    } else if (comp instanceof JNumberField) {
      final JNumberField number = (JNumberField) comp;
      number.setNumber(config.getInt(key));

      number.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          if (number.isNumber()) {
            cache.put(key, number.getNumber());
          } else {
            logger.info("Error, the specified value is not a number.");
            JOptionPane.showMessageDialog(GUIConfig.this, CurrentLocale.getString("INT_FORMAT_EXCEPTION"), CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
          }
        }
      });
      number.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          if (number.isNumber()) {
            cache.put(key, number.getNumber());
          } else {
            logger.info("Error, the specified value is not a number.");
            JOptionPane.showMessageDialog(GUIConfig.this, CurrentLocale.getString("INT_FORMAT_EXCEPTION"), CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
          }
        }
      });
    } else if (comp instanceof JTextField) {
      final JTextField text = (JTextField) comp;
      text.setText(config.getString(key));

      text.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          logger.info("focus");
          cache.put(key, text.getText());
        }
      });
      text.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          logger.info("abstract");
          cache.put(key, text.getText());
        }
      });
    } else if (comp instanceof JButton) {
      final JButton button = (JButton) comp;
      button.setBounds(0, 0, 50, 10);
      button.setBackground(config.getColor(key));
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Color color = JColorChooser.showDialog(
                          GUIConfig.this,
                          CurrentLocale.getString("Config." + key.toUpperCase()),
                          button.getBackground());

          if (color != null) {
            button.setBackground(color);
            cache.put(key, button.getBackground());
          }
        }
      });
    }

    grid_add(panel, comp, gbl, gbc, .2, 0, 1, row, 1, 1);
    panel.setMinimumSize(new java.awt.Dimension(10, 10));
  }

  private static void grid_add(
    JComponent jc_, //pannello contenitore
    Component c_, //Componente da inserire
    GridBagLayout gbl_, //Layout da usare
    GridBagConstraints gbc_, //Costanti
    double weightx_, double weighty_,
    int x_, int y_,
    int w_, int h_
  ) {
    gbc_.weightx = weightx_;
    gbc_.weighty = weighty_;
    gbc_.gridx = x_;
    gbc_.gridy = y_;
    gbc_.gridwidth = w_;
    gbc_.gridheight = h_;
    gbl_.setConstraints(c_, gbc_);
    jc_.add(c_);
  }


  public void addButtons(JPanel buttonPanel) {

    final JButton okButton = new JButton("OK");
    final JButton cancelButton = new JButton("Cancel");

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    //Setting Action for each buttons
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        setVisible(false);
      }
    });
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {

        try {
          // Flush the cache to the actual configuration.
          config.mergeFromGenericMap(cache);
          // Might be needed if show_alias is changed.
          org.edumips64.Main.getGUIFrontend().updateComponents();

          if (Instruction.getEnableForwarding() != config.getBoolean("forwarding")) {
            CPU cpu = CPU.getInstance();
            Instruction.setEnableForwarding(config.getBoolean("forwarding"));

            // Let's verify that we have to reset the CPU
            if (cpu.getStatus() == CPU.CPUStatus.RUNNING) {
              logger.info("Reset");
              org.edumips64.Main.resetSimulator(true);
            }
          }

          org.edumips64.Main.updateCGT();
        } catch (ConfigStoreTypeException ex) {
          logger.severe("Unknown type encountered while storing the configuration.");
        }

        setVisible(false);
      }
    });
  }
  public static int getScreenWidth() {
    return (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
  }

  public static int getScreenHeight() {
    return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
  }

}
