package net.sqs2.omr.ui.swing;

import java.awt.BasicStroke;

public class DottedLineStrokeManager {
	public static final BasicStroke DOTTED_LINE_STROKE[] ={ 
		new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, new float[]{5.0f, 3.0f}, 0f),
		new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, new float[]{5.0f, 3.0f}, 2.0f),
		new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, new float[]{5.0f, 3.0f}, 4.0f),
		new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, new float[]{5.0f, 3.0f}, 6.0f),
	};

	public static BasicStroke get(int index){
		return DOTTED_LINE_STROKE[index % DOTTED_LINE_STROKE.length];
	}
}
