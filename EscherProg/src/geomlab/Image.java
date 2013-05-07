package geomlab;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import plugins.ImagePicture;

public class Image extends BufferedImage {

    public Image(BufferedImage bi) {
	super(bi.getColorModel(), bi.copyData(null), bi.getColorModel()
		.isAlphaPremultiplied(), null);
    }

    public Image(int w, int h, int typeIntRgb) {
	super(w, h, typeIntRgb);
    }

    public static Image fromResource(String name) throws IOException {
	return fromUrl(GeomBase.getResource(name));
    }

    public static Image fromUrl(URL url) throws IOException {
	if (url == null) return null;
	InputStream in = url.openStream();
	try {
	    BufferedImage bufferedImage = ImageIO.read(in);
	    return new Image(bufferedImage);
	} finally {
	    in.close();
	}
    }

}
