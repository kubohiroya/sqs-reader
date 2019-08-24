/*

DeskewGuideAreaBitmap.java


 Copyright 2009 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2009/01/11

 */
package net.sqs2.omr.app.deskew;

import java.awt.image.BufferedImage;

import net.sqs2.image.ImageUtil;
import net.sqs2.omr.model.DeskewGuideAreaConfig;

public class DeskewGuideAreaBitmap{

	public static final int HEADER_AREA_TYPE = 0;
	public static final int FOOTER_AREA_TYPE = 1;
	
	int headerOrFooter;
	int bitmapWidth;
	int bitmapHeight;
	
	DeskewGuideAreaConfig config;

	int imageWidth;
	int imageHeight;

	int horizontalMargin;
	int deskewGuideAreaHeight;
	int headerVerticalMargin;
	int footerVerticalMargin;

	double scaleX, scaleY;
	public boolean[] bitmap;
		
	public DeskewGuideAreaBitmap(DeskewGuideAreaConfig config, BufferedImage image,
				int bitmapWidth, int bitmapHeight, int headerOrFooter){
		this.config = config;
		this.headerOrFooter = headerOrFooter;
		this.bitmapWidth = bitmapWidth;
		this.bitmapHeight = bitmapHeight;
		
		this.imageWidth = image.getWidth();
		this.imageHeight = image.getHeight();

		this.horizontalMargin = (int) (this.imageWidth * config.getHorizontalMargin());
		this.deskewGuideAreaHeight = (int) (this.imageHeight * config.getHeight());
		this.headerVerticalMargin = (int) (this.imageHeight * config.getHeaderVerticalMargin());
		this.footerVerticalMargin = (int) (this.imageHeight * config.getFooterVerticalMargin());
		
		this.scaleX = 1.0 * this.bitmapWidth / (imageWidth - horizontalMargin * 2);
		this.scaleY = 1.0 * this.bitmapHeight / deskewGuideAreaHeight;
		
		this.bitmap = createBitmap(image, config);
	}
	
	public boolean[] getBitmap(){
		return this.bitmap;
	}

	public int getBitmapWidth() {
		return bitmapWidth;
	}

	public int getBitmapHeight() {
		return bitmapHeight;
	}

	public int getHorizontalMargin() {
		return horizontalMargin;
	}

	public int getDeskewGuideAreaHeight() {
		return deskewGuideAreaHeight;
	}

	public int getHeaderVerticalMargin() {
		return headerVerticalMargin;
	}

	public int getFooterVerticalMargin() {
		return footerVerticalMargin;
	}

	public double getScaleX() {
		return this.scaleX;
	}

	public double getScaleY() {
		return this.scaleY;
	}
	
	float translateX(float x) {
		return this.horizontalMargin + (x) * (this.imageWidth - this.horizontalMargin * 2) / this.bitmapWidth;
	}

	float translateY(float y) {
		switch(this.headerOrFooter){
		case HEADER_AREA_TYPE:
			return translateHeaderY(y);
		case FOOTER_AREA_TYPE:
			return translateFooterY(y);
		default:
			throw new IllegalArgumentException("type:"+this.headerOrFooter);
		}
	}
	
	private float translateHeaderY(float y) {
		return this.headerVerticalMargin + (y) * this.deskewGuideAreaHeight / this.bitmapHeight;
	}

	private float translateFooterY(float y) {
		return this.imageHeight
				- (this.footerVerticalMargin + (y) * this.deskewGuideAreaHeight / this.bitmapHeight);
	}
	
	private boolean[] createBitmap(BufferedImage image, DeskewGuideAreaConfig config) {
		int areaSize = this.bitmapWidth * this.bitmapHeight;
		boolean[] booleanValueBitmap = new boolean[areaSize];

		int[] grayscaleValueBitmap = new int[areaSize];
		int[] numPixelsInOriginalImage = new int[areaSize];

		int verticalMargin = (this.headerOrFooter == HEADER_AREA_TYPE) ? this.headerVerticalMargin : this.footerVerticalMargin;
		
		int[][] paramArray = new int[][]{{verticalMargin, 1},{this.imageHeight - verticalMargin - 1, -1}};
		int[] param = paramArray[this.headerOrFooter];
		
		for (int y = 0; y < this.deskewGuideAreaHeight; y++) {
			int bitmapY = y * this.bitmapHeight / this.deskewGuideAreaHeight;
			for (int x = this.horizontalMargin; x < this.imageWidth - this.horizontalMargin; x++) {
				int bitmapX = (x - this.horizontalMargin) * this.bitmapWidth / (this.imageWidth - 2 * this.horizontalMargin);
				int pixelIndex = bitmapX + bitmapY * this.bitmapWidth;
				int color = 0x00ffffff;
				try{
					color = image.getRGB(x, param[0] + param[1] * y);
				}catch(Exception ignore){}
				grayscaleValueBitmap[pixelIndex] += ImageUtil.rgb2gray(color);
				numPixelsInOriginalImage[pixelIndex]++;
			}
		}
		
		int[] grayscaleValueBitmapByType = grayscaleValueBitmap;
		int[] numPixelsInOriginalImageByType = numPixelsInOriginalImage;
		boolean[] booleanValueBitmapByType = booleanValueBitmap;
		float threshold = config.getDensity() * 255;
		for (int y = 0; y < this.bitmapHeight; y++) {
			for (int x = 0; x < this.bitmapWidth; x++) {
				int index = x + y * this.bitmapWidth;
				if (numPixelsInOriginalImage[index] != 0
						&& grayscaleValueBitmapByType[index] / numPixelsInOriginalImageByType[index] < threshold) {
					booleanValueBitmapByType[index] = true;
				}
			}
		}
		return booleanValueBitmap;
	}
}
