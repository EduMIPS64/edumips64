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
package org.edumips64.ui.swing;

import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigStoreTypeException;
import org.edumips64.utils.CurrentLocale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a window for configuration options.
*/
public class GUIConfig extends JDialog {
  private static final long serialVersionUID = -7037139989636376788L;

  private static final Logger logger = Logger.getLogger(GUIConfig.class.getName());

  // Local cache of the configuration values. Includes both modified and non-modified values.
  // Note that existing values are stored here in order to execute a validation of configuration values before
  // committing the changes to the ConfigStore.
  private Map<ConfigKey, Object> cache;
  private ConfigStore config;

  // Callback to call after updates.
  private Consumer<Boolean> updateCallback;

  // Font scaling factor.
  private float scalingFactor;

  private void scaleFont(Component component) {
    Font oldFont = component.getFont();
    component.setFont(oldFont.deriveFont(oldFont.getSize() * scalingFactor));
  }

  public GUIConfig(final JFrame owner, ConfigStore config, Consumer<Boolean> updateCallback) {
    super(owner, CurrentLocale.getString("Config.ITEM"), true);
    scalingFactor = config.getInt(ConfigKey.UI_FONT_SIZE) / (float)getFont().getSize();

    scaleFont(this);

    this.config = config;
    this.updateCallback = updateCallback;
    logger.info("Building a new GUIConfig instance.");
    String MAIN = CurrentLocale.getString("Config.MAIN");
    String APPEARANCE = CurrentLocale.getString("Config.APPEARANCE");
    String BEHAVIOR = CurrentLocale.getString("Config.BEHAVIOR");
    String FPUEXCEPTIONS = CurrentLocale.getString("Config.FPUEXCEPTIONS");
    String FPUROUNDING = CurrentLocale.getString("Config.FPUROUNDING");
    String CACHE = CurrentLocale.getString("Config.CACHE");

    cache = new HashMap<>();

    JTabbedPane tabPanel = new JTabbedPane();
    scaleFont(tabPanel);
    tabPanel.addTab(MAIN, makeMainPanel());
    tabPanel.addTab(APPEARANCE, makeAppearancePanel());
    tabPanel.addTab(BEHAVIOR, makeBehaviorPanel());
    tabPanel.addTab(FPUEXCEPTIONS, makeExceptionsPanel());
    tabPanel.addTab(FPUROUNDING, makeRoundingPanel());
    tabPanel.addTab(CACHE, makeCachePanel());

    logger.log(Level.INFO, "Current values: " + cache.toString());

    final JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    addButtons(buttonPanel);

    Container content = getContentPane();

    content.add(tabPanel, BorderLayout.CENTER);
    content.add(buttonPanel, BorderLayout.PAGE_END);

    //pack();
    int width = (int)(700 * scalingFactor);
    int height = (int)(350 * scalingFactor);
    setSize(width, height);
    setVisible(true);
  }

  private GridBagLayout gbl;
  private GridBagConstraints gbc;

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

    addRow(panel, row++, ConfigKey.FORWARDING, new JCheckBox());
    addRow(panel, row++, ConfigKey.N_STEPS, new JNumberField());

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

    addRow(panel, row++, ConfigKey.WARNINGS, new JCheckBox());
    addRow(panel, row++, ConfigKey.VERBOSE, new JCheckBox());
    addRow(panel, row++, ConfigKey.SLEEP_INTERVAL, new JNumberField());
    addRow(panel, row++, ConfigKey.SYNC_EXCEPTIONS_MASKED, new JCheckBox());
    addRow(panel, row++, ConfigKey.SYNC_EXCEPTIONS_TERMINATE, new JCheckBox());

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

    addRow(panel, row++, ConfigKey.FP_INVALID_OPERATION, new JCheckBox());
    addRow(panel, row++, ConfigKey.FP_OVERFLOW, new JCheckBox());
    addRow(panel, row++, ConfigKey.FP_UNDERFLOW, new JCheckBox());
    addRow(panel, row++, ConfigKey.FP_DIVIDE_BY_ZERO, new JCheckBox());

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

    addRow(panel, row++, ConfigKey.FP_NEAREST, rdoNearest);
    addRow(panel, row++, ConfigKey.FP_TOWARDS_ZERO, rdoTowardZero);
    addRow(panel, row++, ConfigKey.FP_TOWARDS_PLUS_INFINITY, rdoTowardsPlusInfinity);
    addRow(panel, row++, ConfigKey.FP_TOWARDS_MINUS_INFINITY, rdoTowardsMinusInfinity);

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

    addRow(panel, row++, ConfigKey.FP_LONG_DOUBLE_VIEW, new JCheckBox());
    addRow(panel, row++, ConfigKey.UI_FONT_SIZE, new JNumberField());
    addRow(panel, row++, ConfigKey.UI_DARK_THEME, new JCheckBox());
    addRow(panel, row++, ConfigKey.IF_COLOR, new JButton());
    addRow(panel, row++, ConfigKey.ID_COLOR, new JButton());
    addRow(panel, row++, ConfigKey.EX_COLOR, new JButton());
    addRow(panel, row++, ConfigKey.MEM_COLOR, new JButton());
    addRow(panel, row++, ConfigKey.WB_COLOR, new JButton());
    addRow(panel, row++, ConfigKey.FP_ADDER_COLOR, new JButton());
    addRow(panel, row++, ConfigKey.FP_MULTIPLIER_COLOR, new JButton());
    addRow(panel, row++, ConfigKey.FP_DIVIDER_COLOR, new JButton());

    // fill remaining vertical space
    grid_add(panel, new JPanel(), gbl, gbc, 0, 1, 0, row, GridBagConstraints.REMAINDER, 1);
    return panel;
  }

  // Monster function that adds a given row (label + control) to a given
  // JPanel, and sets its behaviour according to the type of control.
  private void addRow(JPanel panel, final int row, final ConfigKey key, final JComponent comp) {
    String title = CurrentLocale.getString("Config." + String.valueOf(key).toUpperCase());
    String tip = CurrentLocale.getString("Config." + String.valueOf(key).toUpperCase() + ".tip");
    //Setting title
    JLabel label = new JLabel(title);
    label.setFont(getFont());
    label.setHorizontalAlignment(JLabel.RIGHT);
    label.setToolTipText(tip);

    scaleFont(comp);

    grid_add(panel, label, gbl, gbc, .1, 0, 0, row, 1, 1);



    if (comp instanceof JCheckBox) {
      final JCheckBox cbox = (JCheckBox) comp;
      //Setting Component
      cbox.setHorizontalAlignment(SwingConstants.LEFT);
      cbox.setVerticalAlignment(SwingConstants.CENTER);
      cbox.setSelected(config.getBoolean(key));

      cache.put(key, config.getBoolean(key));

      cbox.setAction(new AbstractAction() {
        private static final long serialVersionUID = -6988834483997845598L;

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

      cache.put(key, config.getBoolean(key));

      // When a radio button is clicked, the other buttons must be deselected.
      // TODO: more generic handling of radio buttons: currently we have only
      // one and this code is tailored for it.
      rbut.setAction(new AbstractAction() {
        private static final long serialVersionUID = 5799131562641421466L;

        public void actionPerformed(ActionEvent e) {
          LinkedList<ConfigKey> keys = new LinkedList<>();
          keys.add(ConfigKey.FP_NEAREST);
          keys.add(ConfigKey.FP_TOWARDS_ZERO);
          keys.add(ConfigKey.FP_TOWARDS_PLUS_INFINITY);
          keys.add(ConfigKey.FP_TOWARDS_MINUS_INFINITY);

          cache.put(key, true);
          keys.remove(key);

          for (ConfigKey k : keys) {
            cache.put(k, false);
          }
        }
      });
    } else if (comp instanceof JNumberField) {
      final JNumberField number = (JNumberField) comp;
      number.setNumber(config.getInt(key));

      cache.put(key, config.getInt(key));

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
        private static final long serialVersionUID = 4922527101744774710L;

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

      cache.put(key, config.getString(key));

      text.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          logger.info("focus");
          cache.put(key, text.getText());
        }
      });
      text.setAction(new AbstractAction() {
		private static final long serialVersionUID = -56222320121905876L;

		public void actionPerformed(ActionEvent e) {
          logger.info("abstract");
          cache.put(key, text.getText());
        }
      });
    } else if (comp instanceof JButton) {
      final JButton button = (JButton) comp;
      button.setBounds(0, 0, 50, 40);
      Color currentColor = new Color(config.getInt(key));
      button.setBackground(currentColor);
      button.setText("("+currentColor.getRed() + ", "+ currentColor.getGreen() + ", "+ currentColor.getBlue() + ")");
      cache.put(key, config.getInt(key));

      button.addActionListener(e -> {
        Color color = JColorChooser.showDialog(
                        GUIConfig.this,
                        CurrentLocale.getString("Config." + String.valueOf(key).toUpperCase()),
                        button.getBackground());

        if (color != null) {
          button.setBackground(color);
          button.setText("("+color.getRed() + ", "+ color.getGreen() + ", "+ color.getBlue() + ")");
          cache.put(key, button.getBackground().getRGB());
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


  private void addButtons(JPanel buttonPanel) {

    final JButton okButton = new JButton("OK");
    okButton.setFont(getFont());
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.setFont(getFont());

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    //Setting Action for each buttons
    cancelButton.addActionListener(e -> setVisible(false));
    okButton.addActionListener(e -> {
      logger.log(Level.INFO, "Pushing values: " + cache.toString());

      try {
        if (!ConfigStore.isValid(cache)) {
          JOptionPane.showMessageDialog(null,
                  CurrentLocale.getString("NO_MASK_AND_TERMINATE"), CurrentLocale.getString("GUI_WARNING"),
                  JOptionPane.WARNING_MESSAGE);
          return;
        }

        boolean fontChanged = config.getInt(ConfigKey.UI_FONT_SIZE) != (Integer) cache.get(ConfigKey.UI_FONT_SIZE);
        boolean themeChanged = config.getBoolean(ConfigKey.UI_DARK_THEME) != (Boolean) cache.get(ConfigKey.UI_DARK_THEME);
        boolean fwdChanged = config.getBoolean(ConfigKey.FORWARDING) != (Boolean) cache.get(ConfigKey.FORWARDING);

        // Alert the user that the simulation will be restarted if the forwarding setting is changed.
        if (fwdChanged) {
            boolean shouldReset = JOptionPane.showConfirmDialog(null,
                    CurrentLocale.getString("FWD_RESET_WARNING"), CurrentLocale.getString("GUI_WARNING"),
                    JOptionPane.YES_NO_OPTION) == 0;
            if (!shouldReset) {
              return;
            }
        }

        // Flush the cache to the actual configuration.
        config.mergeFromGenericMap(cache);

        // Run the update callback.
        updateCallback.accept(fwdChanged);

        // Warn the user that they need to restart the simulator if they changed the font size.
        if (fontChanged || themeChanged) {
          JOptionPane.showMessageDialog(null,
              CurrentLocale.getString("RESTART_APPEARANCE"), CurrentLocale.getString("GUI_WARNING"),
              JOptionPane.WARNING_MESSAGE);
        }

      } catch (ConfigStoreTypeException ex) {
        logger.severe("Unknown type encountered while storing the configuration.");
      }

      setVisible(false);
    });
  }

  private JPanel makeCachePanel() {
    gbl = new GridBagLayout();
    gbc = new GridBagConstraints();

    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 10);

    JPanel panel = new JPanel();

    panel.setLayout(gbl);
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    int row = 2;

    addRow(panel, row++, ConfigKey.L1D_SIZE, new JNumberField());
    addRow(panel, row++, ConfigKey.L1I_SIZE, new JNumberField());
    addRow(panel, row++, ConfigKey.L1D_BLOCK_SIZE, new JNumberField());
    addRow(panel, row++, ConfigKey.L1I_BLOCK_SIZE, new JNumberField());
    addRow(panel, row++, ConfigKey.L1D_ASSOCIATIVITY, new JNumberField());
    addRow(panel, row++, ConfigKey.L1I_ASSOCIATIVITY, new JNumberField());
    addRow(panel, row++, ConfigKey.L1D_PENALTY, new JNumberField());
    addRow(panel, row++, ConfigKey.L1I_PENALTY, new JNumberField());

    grid_add(panel, new JPanel(), gbl, gbc, 0, 1, 0, row, GridBagConstraints.REMAINDER, 1);
    return panel;
  }
}
