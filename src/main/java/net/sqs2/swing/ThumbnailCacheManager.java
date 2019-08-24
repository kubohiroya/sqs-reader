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
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sqs2.image.ImageFactory;
import net.sqs2.lang.GroupThreadFactory;

import org.apache.commons.collections15.map.LRUMap;

public class ThumbnailCacheManager {

	static ExecutorService service = Executors.newFixedThreadPool(16, new GroupThreadFactory(
			"ThumbnailCache", Thread.MIN_PRIORITY, true));
	static LRUMap<File, ThumbnailEntry> map = new LRUMap<File, ThumbnailEntry>(32);

	public static ThumbnailEntry loadImage(final File file, final int index) throws IOException{
		try {
			return service.submit(new Callable<ThumbnailEntry>() {
				public ThumbnailEntry call() throws IOException{
					ThumbnailEntry entry = get(file);
					if (entry != null) {
						return entry;
					}

					BufferedImage image = ImageFactory.createImage(file, index);
					int sWidth = image.getWidth();
					int sHeight = image.getHeight();
					int tHeight = 64;
					int tWidth = tHeight * sWidth / sHeight;
					ImageFilter filter = new AreaAveragingScaleFilter(tWidth, tHeight);
					ImageProducer im = new FilteredImageSource(image.getSource(), filter);
					Image thumbnail = Toolkit.getDefaultToolkit().createImage(im);
					entry = new ThumbnailEntry(thumbnail, sWidth, sHeight);
					put(file, entry);
					image.flush();
					return entry;
				}
			}).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		throw new IOException("Error: File:"+file.getAbsolutePath()+"\tindex="+index);
	}

	private static synchronized ThumbnailEntry get(File file) {
		return map.get(file);
	}

	private static synchronized void put(File file, ThumbnailEntry entryValue) {
		map.put(file, entryValue);
	}
}
