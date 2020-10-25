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

import org.edumips64.utils.CurrentLocale;

/**
 * This class controls the Edumips64 user guide.
 */
public class GUIHelp {
  private final static Logger log = Logger.getLogger(GUIHelp.class.getName());
  private static Path manTmpDir;

  /**
   * Opens the EduMIPS64 HTML manual with the default application for HTML files.
   * HTML files are decompressed to a temporary folder.
   *
   * In case of error, logs a SEVERE error statement and returns without opening
   * the manual.
   */
  public static void showHelp() {
    try {
      if (Objects.isNull(manTmpDir))
        decompressManual();

      URI indexUrl = manTmpDir.resolve("index.html").toUri();
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(indexUrl);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not open the manual.", e);
    }
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
