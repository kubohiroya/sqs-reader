/*

 PageSequenceInvalidExceptionCore.java

 Copyright 2007 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on Apr 7, 2007

 */
package net.sqs2.omr.app.deskew;

import java.awt.Polygon;
import java.io.Serializable;


public class PageSequenceInvalidErrorModel extends PageImageErrorModel implements Serializable {
	final static private long serialVersionUID = 0L;

	Polygon leftFooterArea, rightFooterArea;
	int leftValue, rightValue;

	public PageSequenceInvalidErrorModel(){}
			
	public PageSequenceInvalidErrorModel(
			Polygon footerAreaLeft, int leftValue, Polygon footerAreaRight, int rightValue) {
		this.leftFooterArea = footerAreaLeft;
		this.rightFooterArea = footerAreaRight;
		this.leftValue = leftValue;
		this.rightValue = rightValue;
	}

	public PageSequenceInvalidErrorModel(int leftValue, int rightValue) {
		this.leftValue = leftValue;
		this.rightValue = rightValue;
	}

	public Polygon getLeftFooterArea() {
		return this.leftFooterArea;
	}

	public Polygon getRightFooterArea() {
		return this.rightFooterArea;
	}

	public int getLeftValue() {
		return this.leftValue;
	}

	public int getRightValue() {
		return this.rightValue;
	}

	@Override
	public String toString() {
		return "{left:" + this.leftValue + ',' + "right:" + this.rightValue+"}";
	}
}
