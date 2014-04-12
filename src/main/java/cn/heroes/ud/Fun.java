package cn.heroes.ud;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

public class Fun {

	/**
	 * @param args
	 * @throws Exception
	 * @throws ImageReadException
	 */
	/**
	 * @param args
	 * @throws ImageReadException
	 * @throws Exception
	 */
	public static void main(String[] args) throws ImageReadException, Exception {
		File file = new File("image", "heart-elika.jpg");
		BufferedImage image = ImageIO.read(file);

		// BufferedImage image = Imaging.getBufferedImage(file);

		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage target = new BufferedImage(width, height, image.getType());

		// int[][] rgbs = new int[height][width];
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int rgb = image.getRGB(col, row);
				target.setRGB(width - 1 - col, row, rgb);
			}
		}
		File output = new File("image", "target.jpg");
		ImageIO.write(target, "JPEG", output);
//		Imaging.writeImage(target, output, ImageFormats.JPEG, null);
	}

	public static byte[] transform(byte[] bs) throws Exception {
		BufferedImage image = Imaging.getBufferedImage(bs);

		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage target = new BufferedImage(width, height, image.getType());

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int rgb = image.getRGB(col, row);
				target.setRGB(width - 1 - col, row, rgb);
			}
		}

		return Imaging.writeImageToBytes(target, ImageFormats.GIF, null);
	}
}
