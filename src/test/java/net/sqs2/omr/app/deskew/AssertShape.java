package net.sqs2.omr.app.deskew;

import static org.testng.Assert.assertEquals;

import java.awt.geom.Point2D;

public class AssertShape {
	public static void assertEqualsPoint2D(Point2D a, Point2D b, double horizontalDelta, double verticalDelta){
		assertEquals(a.getX(), b.getX(), horizontalDelta);
		assertEquals(a.getY(), b.getY(), verticalDelta);
	}

}
