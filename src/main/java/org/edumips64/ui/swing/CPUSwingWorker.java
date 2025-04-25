/* CPUSwingWorker.java
 *
 * Swing worker that deals with executing the program and updating the UI.
 *
 * (c) 2017 Andrea Spadaccini
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

import org.edumips64.core.*;
import org.edumips64.core.is.AddressErrorException;
import org.edumips64.core.is.BreakException;
import org.edumips64.core.is.HaltException;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.CycleBuilder;
import org.edumips64.utils.CycleElement;
import org.edumips64.utils.MetaInfo;

import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.swing.*;

import static java.lang.Thread.sleep;

/**
 * Swing worker that deals with executing the program and updating the UI.
 * */
public class CPUSwingWorker extends SwingWorker<Void, Void> {

  private int nStep;

  /**
   * Booleans for synchronous exception handling
   */
  private boolean masked;
  private boolean terminateOnSynchronousExceptions;

  /**
   * Boolean describing if the CPU is running
   */
  private boolean externalStop;

  /**
   * Verbose mode
   */
  private boolean verbose;

  /**
   * Sleep interval between cycles in verbose mode
   */
  private int sleep_interval;

  private CPU cpu;
  private GUIFrontend front;
  private JFrame mainFrame;
  private ConfigStore config;
  private GUIUpdateThread guiUpdateThread;
  private CycleBuilder builder;
  private Supplier<String> codeSupplier;
  private CacheSimulator cachesim;

  private static final Logger logger = Logger.getLogger(CPUSwingWorker.class.getName());

  /** Callbacks */
  private Runnable initCallback, haltCallback, finalizeCallback;

  public CPUSwingWorker(CPU cpu, CacheSimulator cacheSimulator, GUIFrontend front, JFrame mainFrame, ConfigStore config, CycleBuilder builder, Runnable initCallback, Runnable haltCallback, Runnable finalizeCallback, Supplier<String> codeSupplier) {
    externalStop = false;
    this.builder = builder;
    this.cpu = cpu;
    this.front = front;
    this.mainFrame = mainFrame;
    this.config = config;
    this.codeSupplier = codeSupplier;
    this.cachesim = cacheSimulator;
    updateConfigValues();

    this.haltCallback = haltCallback;
    this.initCallback = initCallback;
    this.finalizeCallback = finalizeCallback;

    guiUpdateThread = new GUIUpdateThread(front);
    guiUpdateThread.start();
  }

  /**
   * Used to refresh the internal configuration values. Takes the needed
   * configuration values from the configuration file.
   */
  public void updateConfigValues() {
    sleep_interval = config.getInt(ConfigKey.SLEEP_INTERVAL);
    verbose = config.getBoolean(ConfigKey.VERBOSE);
    masked = config.getBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED);
    terminateOnSynchronousExceptions = config.getBoolean(ConfigKey.SYNC_EXCEPTIONS_TERMINATE);
    logger.info("Terminate = " + terminateOnSynchronousExceptions + "; masked = " + masked);
  }

  /**
   * Allows external classes to stop the execution.
   */

  public synchronized void stopExecution() {
    externalStop = true;
  }

  /**
   * Sets the number of cpu cycles. Set a negative number if you want the CPU
   * to cycle endlessly.
   *
   * @param n an integer value
   */

  public synchronized void setSteps(int n) {
    nStep = n;
  }

  private synchronized void haltCPU() {
    logger.info("Halting the CPU.");
    front.updateComponents();
    cpu.setStatus(CPU.CPUStatus.HALTED);

    // Check if the transactions in the CycleBuilder are all valid.
    for (CycleElement el : builder.getElementsList()) {
      if (!el.isValid()) {
        logger.severe("Invalid CycleElement for instruction " + el.getName());
      }
    }
    haltCallback.run();
  }

  @Override
  protected Void doInBackground() throws Exception {
    long startTimeMs = System.currentTimeMillis();
    logger.info("running");

    initCallback.run();

    // If the nStep variable is set to a value < 0, then we must loop forever (an exception
    // will be the way to terminate execution); otherwise, we must be looping only
    // 'nStep' number of times.
    int steps = 0;

    while (true) {
      if (steps++ == nStep) {
        break;
      }

      if (verbose && (sleep_interval != 0)) {
        sleep(sleep_interval);
      }

      // TODO: implement
      if (externalStop) {
        logger.info("Stopping cycles because of external interaction.");
        break;
      }

      try {
        cpu.step();
      } catch (StoppedCPUException ex) {
        logger.info("CPUGUIThread: CPU was stopped");
        break;
      } catch (BreakException ex) {
        break;
      } catch (SynchronousException ex) {
        logger.info("Caught a synchronous exception.");
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(mainFrame, CurrentLocale.getString(ex.getCode() + ".Message"), "EduMIPS64 - " + CurrentLocale.getString("EXCEPTION"), JOptionPane.ERROR_MESSAGE);
          front.updateComponents();
          front.represent();
        });

        if (terminateOnSynchronousExceptions) {
          haltCPU();
          break;
        }
      } catch (HaltException ex) {
        haltCPU();
        logger.info("CPUGUIThread: CPU Halted because of HaltException.");
        break;
      } catch (NotAlignException ex) {
        logger.info("NotAlignException. " + ex);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE));
        haltCPU();
        break;
      } catch (AddressErrorException ex) {
        logger.info("AddressErrorException. " + ex);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE));
        haltCPU();
        break;
      } catch (MemoryElementNotFoundException ex) {
        logger.info("Attempt to read a non-existent cell (MemoryElementNotFoundException). " + ex);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, CurrentLocale.getString("ERROR_LABEL"), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE));
        haltCPU();
        break;
      } catch (Exception ex) {
        logger.severe("Exception in CPUSwingWorker: " + ex);
        SwingUtilities.invokeLater(() -> new ReportDialog(mainFrame, ex, CurrentLocale.getString("GUI_STEP_ERROR"), MetaInfo.VERSION, MetaInfo.BUILD_DATE, MetaInfo.FULL_BUILDSTRING, codeSupplier.get()));
        haltCPU();
        break;
      } finally {
        builder.step();
        front.updateComponents();
        if (verbose) {
          // SwingUtilities.invokeAndWait(() -> front.represent());
          guiUpdateThread.triggerUpdate();
        }
      }
    }

    guiUpdateThread.terminate();
    guiUpdateThread.join();

    SwingUtilities.invokeAndWait(finalizeCallback);

    long endTimeMs = System.currentTimeMillis();
    float cyclesPerSecond = steps / ((endTimeMs - startTimeMs) / 1000);
    logger.info("Executed " + steps + " steps in " + (endTimeMs - startTimeMs) + " ms. Speed: " + cyclesPerSecond + " cycles/sec");
    return null;
  }
}
