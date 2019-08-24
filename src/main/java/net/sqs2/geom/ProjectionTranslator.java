package net.sqs2.geom;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface ProjectionTranslator {
	public abstract Point2D getPoint(final int x, final int y);
	public abstract Point2D getPoint(final int x, final int y, Point2D p);
	public abstract Point2D getPoint(final int x, final int y, Point p);

	public Polygon createRectPolygon(Rectangle2D rect);
	public Polygon createRectPolygon(double _x, double _y, double _w, double _h);

}