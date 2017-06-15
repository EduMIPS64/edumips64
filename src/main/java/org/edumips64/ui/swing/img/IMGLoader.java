package org.edumips64.ui.swing.img;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
public class IMGLoader {
  public static Image getImage(String imagename) throws IOException {
    try {
      return ImageIO.read(IMGLoader.class.getResource(imagename));
    } catch (IllegalArgumentException e) {
      // Hacky work-around to run EduMIPS64 in a debugger, out of a JAR.
      return ImageIO.read(new File("../src/org/edumips64/img/" + imagename));
    }
  }
}
