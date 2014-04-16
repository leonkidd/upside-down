package cn.heroes.ud;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.IOUtils;

public class Fun {

	public static void main(String[] args) throws ImageReadException, Exception {
		File file = new File("image", "heart-elika.jpg");
		byte[] bs = Fun.transform(IOUtils
				.toByteArray(new FileInputStream(file)));
		FileOutputStream fos = new FileOutputStream("image/_" + file.getName());
		IOUtils.write(bs, fos);
	}

	public static byte[] transform(byte[] bs) throws Exception {
		ImageInfo info = Imaging.getImageInfo(bs);
		ImageFormat format = info.getFormat();
		try {

			BufferedImage image = Imaging.getBufferedImage(bs);

			int width = image.getWidth();
			int height = image.getHeight();

			BufferedImage target = new BufferedImage(width, height,
					image.getType());

			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					int rgb = image.getRGB(col, row);
					target.setRGB(width - 1 - col, row, rgb);
				}
			}
			return Imaging.writeImageToBytes(target, format, null);
		} catch (ImageWriteException e) {
			return transformJava(bs, format.toString());
		}
	}

	public static byte[] transformJava(byte[] bs, String format)
			throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		BufferedImage image = ImageIO.read(bais);

		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage target = new BufferedImage(width, height, image.getType());

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int rgb = image.getRGB(col, row);
				target.setRGB(width - 1 - col, row, rgb);
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(target, format, baos);
		return baos.toByteArray();
	}

	// public static String getFormatName(InputStream is) throws Exception {
	// ImageInputStream iis = ImageIO.createImageInputStream(is);
	// Iterator<ImageReader> iterator = ImageIO.getImageReaders(iis);
	// while (iterator.hasNext()) {
	// ImageReader reader = (ImageReader) iterator.next();
	// return reader.getFormatName();
	// }
	// }

	public static String getFormat(byte[] bs) {
		String type = "";
		byte b0 = bs[0];
		byte b1 = bs[1];
		byte b2 = bs[2];
		byte b3 = bs[3];
		byte b6 = bs[6];
		byte b7 = bs[7];
		byte b8 = bs[8];
		byte b9 = bs[9];
		// GIF
		if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F')
			type = "GIF";
		// PNG
		else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G')
			type = "PNG";
		// JPG
		else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I'
				&& b9 == (byte) 'F')
			type = "JPG";
		else
			type = "Unknown";
		return type;
	}
}
