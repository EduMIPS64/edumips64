/* GUIHelp.java
 *
 * EduMIPS64 Help
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

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;


import org.edumips64.utils.CurrentLocale;

/**
 * This class controls the Edumips64 user guide.
 */
public class GUIHelp {
  private final static String HELPSET = "EduMIPS64.hs";
  private final static Logger log = Logger.getLogger(GUIHelp.class.getName());
  private static Path manTmpDir;

   /**
   * Shows the EduMIPS64 help window.
   *
   * In case of error, logs a SEVERE error statement and returns without showing the help window.
   *
   * @param parent the window that owns this help dialog
   * @param helpSetUrl the URL to the directory of the help set.
   */
  public static void showHelp(Window parent, URL helpSetUrl, ConfigStore cfg) {
    // Clean up the URL from spaces.
    log.info("Got helpSetUrl: <"+helpSetUrl+">");
    String clean = helpSetUrl.getProtocol() + ":" + helpSetUrl.getPath().replace("%20", " ");
    URL cleanUrl;

    try {
      cleanUrl = new URL(clean);
    } catch (MalformedURLException e) {
      log.log(Level.SEVERE, "Could not parse Help URL_" + clean, e);
      return;
    }
    log.info("Cleaned: <" + cleanUrl + ">");


    URLClassLoader urlclassloader = new URLClassLoader(new URL[]{cleanUrl});
    URL url = HelpSet.findHelpSet(urlclassloader, HELPSET);
    log.info("Final Helpset Url: <" + url + ">");

    HelpSet helpset;
    try {
      helpset = new HelpSet(urlclassloader, url);
    } catch (HelpSetException e) {
      log.log(Level.SEVERE, "Could not load helpset " + url, e);
      return;
    }

    int desiredFontSize = cfg.getInt(ConfigKey.UI_FONT_SIZE);
    float windowScalingFactor = desiredFontSize / 12.0f;
    HelpBroker helpBroker = helpset.createHelpBroker();
    helpBroker.initPresentation();
    helpBroker.setSize(new Dimension((int) (800 * windowScalingFactor), (int) (600 * windowScalingFactor)));

    // Update the font.
    helpBroker.setSize(helpBroker.getSize());
    Font newFont = helpBroker.getFont().deriveFont((float)desiredFontSize);
    ((DefaultHelpBroker) helpBroker).setActivationWindow(parent);
    helpBroker.initPresentation();
    helpBroker.setFont(newFont);
    helpBroker.setDisplayed(true);
  }

  private static void decompressManual() throws IOException, URISyntaxException {
      manTmpDir = Files.createTempDirectory("edumips64-");
      var helpDir = CurrentLocale.getString("HELPDIR");
      copyFromJar(helpDir + File.separator + "html", manTmpDir);
  }

  private static void copyFromJar(String source, final Path target) throws URISyntaxException, IOException {
    var resourceUri = GUIHelp.class.getResource(source).toURI();
    final Path sourcePath;
    if (resourceUri.toString().startsWith("file:")) {  // executing from IDE
      sourcePath = Path.of(resourceUri.toString().replace("file:", ""));
    } else {                                        // executing from jar
      var fileSystem = FileSystems.newFileSystem(
              resourceUri,
              Collections.<String, String>emptyMap()
      );
      sourcePath = fileSystem.getPath(source);
    }

    Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
        private Path currentTarget;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            currentTarget = target.resolve(sourcePath.relativize(dir).toString());
            Files.createDirectories(currentTarget);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, target.resolve(sourcePath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    });
  }
}
