/*
  Copyright 2011 KUBO Hiroya (hiroya@cuc.ac.jp).
  
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2011/12/03

 */
package net.sqs2.swing;

import java.awt.Image;

public class ThumbnailEntry {
	Image image;
	int originalImageWidth;
	int originalImageHight;

	public ThumbnailEntry(Image image, int width, int height) {
		this.image = image;
		this.originalImageWidth = width;
		this.originalImageHight = height;
	}

	public Image getImage() {
		return this.image;
	}

	public int getOriginalImageWidth() {
		return this.originalImageWidth;
	}

	public int getOriginalImageHeight() {
		return this.originalImageHight;
	}

}