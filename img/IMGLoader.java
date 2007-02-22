package edumips64.img;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
public class IMGLoader{
	public static Image getImage(String imagename)throws IOException{
		return ImageIO.read(IMGLoader.class.getResource(imagename));
	}
}
