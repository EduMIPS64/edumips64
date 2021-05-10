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

package org.edumips64.ui.swing;

import org.edumips64.core.Dinero;
import org.edumips64.utils.ConfigKey;
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
  private static JTextField path, params;
  private static JButton execute, configure, create;	//configure and create buttons added to create panels and cache parameters
  private static JTextArea result;
  private static JLabel  cacheLevelLabel, cacheTypeLabel;	//labels for cache level and cache type added
  private static int argLevel;	//static declaration of array Level argument
  private static char argType;	//static declaration of array type argument
  private static Box cachePanel;	//static container globally declared for flexibility
  private static JScrollPane scrollPane;	//static delclaration of scrollbar
  public static JPanel panelL1, panelL2, panelL3, panelL4, panelL5;	//static panels to add configuration components
  private static JComboBox<String> cacheLevel, cacheType;		//static delcaration of cache combo boxes

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

  public DineroFrontend(Frame owner, Dinero dinero, ConfigStore config) {
    super(owner);
    setTitle("Dinero frontend");
    Container cp = rootPane.getContentPane();
    cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

    Dimension hSpace = new Dimension(5, 0);
    Dimension vSpace = new Dimension(0, 5);

    JLabel pathLabel = new JLabel("DineroIV executable path:");
    JLabel paramsLabel = new JLabel("Command line parameters:");

    path = new JTextField(config.getString(ConfigKey.DINERO));
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

    final String[] level = {"1", "2", "3", "4", "5"};
	final String[] type = {"data", "instruction", "unified/mixed"};

	cacheLevelLabel	= new JLabel("Set Cache Level (N)");	//Label for cacheLevel
	cacheTypeLabel  = new JLabel("Set Cache Type (T)");	//Label for cacheType

	cacheLevelLabel.setPreferredSize(new Dimension(110, 26));
	cacheLevelLabel.setMaximumSize(new Dimension(120, 26));
	cacheLevelLabel.setMinimumSize(new Dimension(90, 26));

	cacheTypeLabel.setPreferredSize(new Dimension(110, 26));
	cacheTypeLabel.setMaximumSize(new Dimension(120, 26));
	cacheTypeLabel.setMinimumSize(new Dimension(80, 26));

	//combo box defined for cache level
	cacheLevel = new JComboBox<String>(level);
	cacheLevel.setPreferredSize(new Dimension(80, 26));
	cacheLevel.setMaximumSize(new Dimension(100, 26));
	cacheLevel.setMinimumSize(new Dimension(60, 26));
	
	//combo box defined for cache type
	cacheType = new JComboBox<String>(type);
	cacheType.setPreferredSize(new Dimension(110, 26));
	cacheType.setMaximumSize(new Dimension(130, 26));
	cacheType.setMinimumSize(new Dimension(100, 26));

	//combo box action of cache level - passing combo option to argument for cache panel
	cacheLevel.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    String levelMsg = String.valueOf(cacheLevel.getSelectedItem());
	    argLevel = Integer.parseInt(levelMsg);
	  }
	});
	cacheLevel.setSelectedIndex(0);

	//combo box action of cache type - passing combo option to argument for cache panel
	cacheType.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e){
		argType = (String.valueOf(cacheType.getSelectedItem())).charAt(0);
	  }
	});
	cacheType.setSelectedIndex(2);

	//button for creating cache defined
	create = new JButton("Create Cache");
	create.setAlignmentX(Component.RIGHT_ALIGNMENT);
	
	//button for configuring cache defined
	configure = new JButton("Configure Cache");
	configure.setAlignmentX(Component.CENTER_ALIGNMENT);

	JButton browse = new JButton("Browse...");
    browse.setAlignmentX(Component.RIGHT_ALIGNMENT);
	execute = new JButton("Execute");
	execute.setAlignmentX(Component.CENTER_ALIGNMENT);

	//action for cache create button - creates panel to add cache parameters, based on cache level and type
	create.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){

		cachePanel.removeAll();

		//Panels for different cache type and their configuration options
		cachePanel.add(panelL1 = new DineroSingleCachePanel(argType, 1));

		if (argLevel > 1){
		  cachePanel.add(Box.createRigidArea(vSpace));
		  cachePanel.add(panelL2 = new DineroSingleCachePanel(argType, 2));
		}
		
		if (argLevel > 2){
		  cachePanel.add(Box.createRigidArea(vSpace));
		  cachePanel.add(panelL3 = new DineroSingleCachePanel(argType, 3));
		}

		if (argLevel > 3){
		  cachePanel.add(Box.createRigidArea(vSpace));
		  cachePanel.add(panelL4 = new DineroSingleCachePanel(argType, 4));
		}

		if (argLevel > 4){
		  cachePanel.add(Box.createRigidArea(vSpace));
		  cachePanel.add(panelL5 = new DineroSingleCachePanel(argType, 5));
		}

		//For cache panel container refresh
		cachePanel.revalidate();
		cachePanel.repaint();
	  }
	});

	//Action for cache configure button - to accumulate cache parameters
	configure.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
		String parameter;
		//cache parameter only taken for defined cache levels
		parameter = panelL1.toString();
		if (argLevel > 1){parameter += panelL2.toString();}
		if (argLevel > 2){parameter += panelL3.toString();}
		if (argLevel > 3){parameter += panelL4.toString();}
		if (argLevel > 4){parameter += panelL5.toString();}	
		params.setText(parameter);
	  }
	});

    browse.addActionListener(e -> {
      JFileChooser jfc = new JFileChooser();
      int val = jfc.showOpenDialog(null);

      if (val == JFileChooser.APPROVE_OPTION) {
        config.putString(ConfigKey.DINERO, jfc.getSelectedFile().getPath());
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
	
	//Box for adding cache configuration ui
	Box cacheCreate = Box.createHorizontalBox();
	cacheCreate.add(Box.createHorizontalGlue());
	cacheCreate.add(cacheLevelLabel);
	cacheCreate.add(Box.createRigidArea(hSpace));
	cacheCreate.add(cacheLevel);
	cacheCreate.add(Box.createRigidArea(hSpace));
	cacheCreate.add(cacheTypeLabel);
	cacheCreate.add(Box.createRigidArea(hSpace));
	cacheCreate.add(cacheType);
	cacheCreate.add(Box.createRigidArea(hSpace));
	cacheCreate.add(create);
	cp.add(cacheCreate);
	cp.add(Box.createRigidArea(vSpace));

    //Box created for adding Cache Panel components
	cachePanel = Box.createVerticalBox();
	cachePanel.add(panelL1 = new DineroSingleCachePanel(argType, 1));
	cp.add(cachePanel);
	cp.add(Box.createRigidArea(vSpace));
    
    result = new JTextArea();
    result.setBorder(BorderFactory.createTitledBorder("Messages"));
    result.setEditable(false);
    result.setFont(new Font("Monospaced", Font.PLAIN, config.getInt(ConfigKey.UI_FONT_SIZE)));

    cp.add(configure);
		cp.add(Box.createRigidArea(vSpace));
    cp.add(execute);
    cp.add(Box.createRigidArea(vSpace));
    cp.add(new JScrollPane(result));

    //Vertical and horizontal scroll added to the Frame container
	scrollPane = new JScrollPane(cp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	setContentPane(scrollPane);

	//Resized the frame container to have better UX with the new panels
	setSize(900, 800);
  }
}

/** Panel with all the necessary controls to modify the options of a Cache.
 */
class DineroSingleCachePanel extends JPanel {
	private DineroCacheOptions dco;
	private JComboBox<String> size, sizeUnit, bsize, bsizeUnit;
	private JTextField assoc;
	private JCheckBox ccc;
	private JLabel cacheSizeLabel, cacheSizeUnitLabel, blockSizeLabel, bsizeUnitLabel, assocLabel, cccLabel;

	public DineroSingleCachePanel(char type, int level) {
	  dco = new DineroCacheOptions(type, level);

	  final String[] sizes = {"1", "2", "4", "8", "16", "32", "64", "128", "256", "512"};
	  final String[] units = {" ", "k", "M", "G"};

	  size = new JComboBox<String>(sizes);
	  bsize = new JComboBox<String>(sizes);

	  sizeUnit = new JComboBox<String>(units);
	  bsizeUnit = new JComboBox<String>(units);

	  assoc = new JTextField();
	  ccc = new JCheckBox();

	  cacheSizeLabel = new JLabel("Cache size");
	  cacheSizeUnitLabel = new JLabel("Unit (Byte)");
	  blockSizeLabel = new JLabel("Block size");
	  bsizeUnitLabel = new JLabel("Unit (Byte)");
	  assocLabel = new JLabel("N way set associative");
	  cccLabel = new JLabel("CCC Enable");

	  size.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
		  //dco.size = String.valueOf(sizeUnit.getSelectedItem());
		  sizeUnit.setSelectedIndex(0);
		}
	  });
	  size.setSelectedIndex(0);

	  bsize.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e){
		  //dco.bsize = String.valueOf(bsize.getSelectedItem());
		  bsizeUnit.setSelectedIndex(0);
	    }
	  });
	  bsize.setSelectedIndex(0);
	
	  sizeUnit.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
		  String sizeMsg = String.valueOf(size.getSelectedItem());
		  dco.size = sizeMsg + String.valueOf(sizeUnit.getSelectedItem());
	    }
	  });
	  sizeUnit.setSelectedIndex(0);

	  bsizeUnit.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e){
		  String bsizeMsg = String.valueOf(bsize.getSelectedItem());
		  dco.bsize = bsizeMsg + String.valueOf(bsizeUnit.getSelectedItem());
	    }
	  });
	  bsizeUnit.setSelectedIndex(0);

	  //adding components to panel layout
	  setBorder(BorderFactory.createTitledBorder("Level " + level + " cache (" + type + ")"));
	  setLayout(new GridLayout(2, 6, 1, 1));
	  add(cacheSizeLabel);
	  add(cacheSizeUnitLabel);
	  add(blockSizeLabel);
	  add(bsizeUnitLabel);
	  add(assocLabel);
	  add(cccLabel);
	  add(size);
	  add(sizeUnit);
	  add(bsize);
	  add(bsizeUnit);
	  add(assoc);
	  add(ccc);

	  //Default dimension set for panel
	  setPreferredSize(new Dimension(850, 80));
	  setMaximumSize(new Dimension(850, 90));
	  setMinimumSize(new Dimension(850, 50));
	  }

	  //Passes Dinero command parameters with proper syntax
	  public String toString(){
		ccc.addItemListener(new ItemListener(){
		  public void itemStateChanged(ItemEvent e) {
			Boolean cccFlag =  ccc.isSelected();
			dco.ccc = cccFlag;
		  }
		});

		//Implements try and catch method to only allow numerical value only
		try{
		  dco.assoc = Integer.parseInt(assoc.getText());
		}
		catch(NumberFormatException exception){
		  dco.assoc = 0;
		}
		return dco.toString();
	}
}

/** Class holding the config options for a Cache.
 *  Its attributes are public because this class has package visibility, and so 
 *  it's used only by the DineroFrontend and the DineroCachePanel classes.
 */
class DineroCacheOptions {
  public String size, bsize;
  public int assoc = 0;
  public boolean ccc = false;
	
  private char type;
  private int level;
	
  public DineroCacheOptions(char type, int level) {
	this.type = type;
	this.level = level;
  }

  public String toString() {
	// creates empty builder, capacity 16
	StringBuilder cacheConfig = new StringBuilder();
	// adds 9 character string at beginning
	try {
	  String prefix = "-l" + level + "-" + type;
	  cacheConfig.append("prefix");

	  String cmdline = prefix + "size" + " " + size + " ";
	  cacheConfig.append("cmdline");

	  cmdline += prefix + "bsize" + " " + bsize + " ";

	  if(assoc > 0)
	    cmdline += prefix + "assoc" + " " + assoc + " ";
		if(ccc)
		  cmdline += prefix + "ccc" + " ";
		
		cacheConfig.append("cmdline");
		return cmdline;
	}

	catch (Exception e) {
		e.printStackTrace();
		System.out.println("- ERROR: in building dinero command line: " + e.toString());
		return("");
	}
	}
}
