package net.sqs2.omr.app.deskew;

import net.sqs2.image.ImageSilhouetteExtract;

public class DeskewGuideAreaPrintable{
	
	final static String LABEL = ".0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	final static int LABEL_LENGTH = LABEL.length();

	DeskewGuideAreaBitmap bitmap;
	
	DeskewGuideAreaPrintable(DeskewGuideAreaBitmap bitmap){
		this.bitmap = bitmap;
	}

	public String toString2() {
		StringBuilder builder = new StringBuilder(1024);
		int pixelIndex = 0;
		ImageSilhouetteExtract ise = new ImageSilhouetteExtract(this.bitmap.bitmap, this.bitmap.getBitmapWidth(), this.bitmap.getBitmapHeight());
		int[] a = ise.getAreaArray();
		builder.append("################################################################\n");
		for (int y = 0; y < bitmap.bitmapHeight; y++) {
			for (int x = 0; x < bitmap.bitmapWidth; x++) {
				builder.append(getLabelChar(a[pixelIndex++]));
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	private static char getLabelChar(int p) {
		char c;
		if (p < LABEL_LENGTH) {
			c = LABEL.charAt(p);
		} else {
			c = '*';
		}
		return c;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder(1024);
		int step = 1;
		ImageSilhouetteExtract ise = new ImageSilhouetteExtract(this.bitmap.bitmap, this.bitmap.getBitmapWidth(), this.bitmap.getBitmapHeight());
		boolean[] b = bitmap.bitmap;
		int[] s = ise.getSilhouetteIndexArray();
		for (int y = 0; y < bitmap.bitmapHeight; y+=step) {
			for (int x = 0; x < bitmap.bitmapWidth; x+=step) {
				int pixelIndex = x + y * bitmap.bitmapWidth;
				int silhouetteIndex = s[pixelIndex];
				if (0 < silhouetteIndex) {
					if (0 < ise.getAreaArray()[silhouetteIndex]) {
						builder.append("@"); // valid silhouette
					} else {
						builder.append("#"); // ignorable silhouette
					}
				} else if (b[pixelIndex]) {
					builder.append("+"); // 
				} else {
					builder.append("-");
				}
			}
			builder.append("\n");
		}		
		return builder.toString();
	}
	
}