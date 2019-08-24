/**
 * 
 */
package net.sqs2.omr.app.deskew;

import java.awt.geom.Point2D;

import net.sqs2.omr.model.OMRProcessorSource;

public class Validator{
	OMRProcessorSource source;
	DeskewedImageSource deskewedImageSource;
	Point2D[] centerPoints;
	int[] areaSizes;

	
	Validator(OMRProcessorSource source, 
			DeskewedImageSource deskewedImageSource,
			Point2D[] centerPoints,
			int[] areaSizes){
		this.source = source;
		this.deskewedImageSource = deskewedImageSource;
		this.centerPoints = centerPoints;
		this.areaSizes = areaSizes;

	}
}