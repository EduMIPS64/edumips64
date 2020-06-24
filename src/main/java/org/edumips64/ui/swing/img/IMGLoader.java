package org.edumips64.ui.swing.img;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class IMGLoader {
  public static Image getImage(String imagename) throws IOException {
    try {
      URL path = IMGLoader.class.getResource("/images/" + imagename);
      return ImageIO.read(path);
    } catch (IllegalArgumentException e) {
      // Hacky work-around to run EduMIPS64 in a debugger, out of a JAR.
      return ImageIO.read(new File("../src/org/edumips64/img/" + imagename));
    }
  }
}
