/* CPUGUIThread.java
 *
 * This class handles the multi-threaded CPU object, and acts as a proxy between
 * the Main class and the CPU class.
 * (c) 2006 Antonella Scandura, Andrea Spadaccini
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

package edumips64.ui;

import edumips64.*;
import edumips64.core.*;
import edumips64.core.is.*;
import edumips64.ui.*;
import edumips64.utils.*;


import javax.swing.*;

/** This class handles the multi-threaded CPU object, and acts as a proxy between
 * the Main class and the CPU class.
 * @author Antonella Scandura
 * @author Andrea Spadaccini
 * */
public class CPUGUIThread extends Thread{

	/** Needed for multithreading. */
	private int nStep;

	/** Booleans for synchronous exception handling */
	private boolean masked;
	private boolean terminate;

	/** Boolean describing if the CPU is running */
	private boolean externalStop;

	/** Verbose mode */
	private boolean verbose;

	/** Sleep interval between cycles in verbose mode */
	private int sleep_interval;
	
	private CPU cpu;
	private GUIFrontend front;
	private JFrame f;
	
	public CPUGUIThread(){
		externalStop = false;
		cpu = CPU.getInstance();
		front = Main.getGUIFrontend();
		f = Main.getMainFrame();
		updateConfigValues();
	}

	/** Used to refresh the internal configuration values. Takes the needed
	 * configuration values from the configuration file.
	 */
	public void updateConfigValues() {
		sleep_interval = (Integer)Config.get("sleep_interval");
		verbose = (Boolean)Config.get("verbose");
		masked = (Boolean)Config.get("syncexc-masked");
		terminate = (Boolean)Config.get("syncexc-terminate");
		edumips64.Main.logger.debug("Terminate = " + terminate + "; masked = " + masked);
	}
	
	/** Allows external classes to stop the execution. */
	public synchronized void stopExecution(){
		externalStop = true;
	}

	/** Sets the number of cpu cycles. Set a negative number if you want the CPU
	 * to cycle endlessly.
	 * @param n an integer value*/
	public synchronized void setSteps(int n){
		nStep = n;
	}

	private synchronized void haltCPU() {
		front.updateComponents();
		cpu.setStatus(CPU.CPUStatus.HALTED);
		Main.changeShownMenuItems(CPU.CPUStatus.HALTED);
	}
	
	/** Run method: waits for an external thread that sends the notify. When the
	 * notify arrives, the method will execute nStep CPU cycles if nStep is
	 * positive or equal to zero, or it will cycle indefinitely if nStep is
	 * negative.
	 */
	public void run() {
		try {
			while(true){
				synchronized(this) {
					wait();
				}

				// Let's disable the running menu items and enable the stop menu
				// item
				Main.setRunningMenuItemsStatus(false);
				Main.setStopStatus(true);
				
				// Progress bar
				Main.startPB();

				if(nStep < 0){
					while(true){
						if(verbose && (sleep_interval != 0)) {
							Main.logger.debug("Waiting for " + sleep_interval + " milliseconds...");
							sleep(sleep_interval);
						}
						synchronized(this) {
							if(externalStop == true) {
								externalStop = false;
								break;
							}
						}
						try {
							cpu.step();						
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
						}
                        catch(StoppedCPUException ex) {
							edumips64.Main.logger.debug("CPUGUIThread: CPU was stopped");
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
                            break;
                        }
						catch(BreakException ex) {
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
							break;
						}
						catch(SynchronousException ex) {
							JOptionPane.showMessageDialog(f, CurrentLocale.getString(ex.getCode() + ".Message"), "EduMIPS64 - " + CurrentLocale.getString("EXCEPTION"), JOptionPane.ERROR_MESSAGE);
							if(terminate) {
								haltCPU();
								break;
							}
							continue;
						}
						catch(HaltException ex) {
							haltCPU();
							edumips64.Main.logger.debug("CPUGUIThread: CPU Halted");
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
							break;
						}
						catch(Exception ex) {
                            Main.logger.debug("Exception in CPUGUIThread");
							haltCPU();
							new ReportDialog(f,ex,CurrentLocale.getString("GUI_STEP_ERROR"));
							break;
						}	
					}
				}
				else{
					for(int i=0 ; i<nStep; i++){
						if(verbose && (sleep_interval != 0) && nStep > 1) {
							Main.logger.debug("Waiting for " + sleep_interval + " milliseconds...");
							sleep(sleep_interval);
						}
						synchronized(this) {
							if(externalStop == true) {
								externalStop = false;
								edumips64.Main.logger.debug("Stopping the CPU.");
								break;
							}
						}
						try {
							cpu.step();
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
						}
                        catch(StoppedCPUException ex) {
							edumips64.Main.logger.debug("CPUGUIThread: CPU was stopped");
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
                            break;
                        }
						catch(BreakException ex) {
							edumips64.Main.logger.debug("Caught a BreakException.");
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
							break;
						}
						catch(SynchronousException ex) {
							JOptionPane.showMessageDialog(f, CurrentLocale.getString(ex.getCode() + ".Message"), "EduMIPS64 - " + CurrentLocale.getString("EXCEPTION"), JOptionPane.ERROR_MESSAGE);
							if(terminate) {
								haltCPU();
								break;
							}
							continue;
						}
						catch(HaltException ex) {
							haltCPU();
							edumips64.Main.logger.debug("CPUGUIThread: CPU Halted");
							front.updateComponents();
							if(verbose) {
								front.represent();
							}
							break;
						}
						catch(Exception ex) {
							new ReportDialog(f,ex,CurrentLocale.getString("GUI_STEP_ERROR"));
							haltCPU();
							break;
						}	
					}
				}
				if(!verbose) {
					front.represent();
				}
				if(cpu.getStatus() != CPU.CPUStatus.HALTED)
					Main.setRunningMenuItemsStatus(true);
				Main.setStopStatus(false);
				Main.stopPB();
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}


