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

  static {
    try {
      String path = MetaInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      String decodedPath = URLDecoder.decode(path, "UTF-8");
      JarFile myJar = new JarFile(decodedPath);
      Manifest manifest = myJar.getManifest();
      attributes = manifest.getMainAttributes();
    } catch (Exception e) {
      System.err.println("Error while fetching version info from the jar file.");
    }
  }

  // Returns the attribute value, or an empty string if it isn't found.
  public static String get(String attribute) {
    return attributes.getValue(attribute);
  }
}
