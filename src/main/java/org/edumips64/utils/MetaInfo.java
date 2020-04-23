/* MetaInfo.java
 *
 * This class encapsulates information about the running simulator.
 * (c) Andrea Spadaccini, 2013
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
package org.edumips64.utils;

import java.net.URLDecoder;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

// The meta information is currently stored in the Manifest.
public class MetaInfo {
  static Attributes attributes;

  public static String VERSION;
  public static String CODENAME;
  public static String BUILD_DATE;
  public static String GIT_REVISION;
  public static String FULL_BUILDSTRING;

  static {
    try {
      JarFile myJar = null;
      try {
        String path = MetaInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        myJar = new JarFile(decodedPath);
        Manifest manifest = myJar.getManifest();
        if (manifest != null) {
          attributes = manifest.getMainAttributes();
          VERSION = attributes.getValue("Signature-Version");
          CODENAME = attributes.getValue("Codename");
          BUILD_DATE = attributes.getValue("Build-Date");
          GIT_REVISION = attributes.getValue("Git-Revision");
          FULL_BUILDSTRING = attributes.getValue("Full-BuildString");

          // Build-Qualifier is set during CI, augment the version number in that case.
          if (!attributes.getValue("Build-Qualifier").isEmpty()) {
            VERSION += "-" + attributes.getValue("Build-Qualifier") + "-" + GIT_REVISION;
          }
        } else
          System.err.println("Error while getting the manifest from the JAR file.");
      } finally {
        if (myJar != null)
          myJar.close();
      }
    } catch (Exception e) {
      System.err.println("Error while fetching version info from the jar file.");
    }
  }
}
