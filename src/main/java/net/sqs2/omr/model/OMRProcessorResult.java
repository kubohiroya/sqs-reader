/*

 PageTaskResult.java

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

 Created on 2007/01/11

 */
package net.sqs2.omr.model;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import net.sqs2.geom.ProjectionTranslator;
import net.sqs2.util.FileResourceID;

public class OMRProcessorResult implements Serializable {
	public static final long serialVersionUID = 4L;

	protected FileResourceID masterFileResourceID;
	protected Point2D[] deskewGuideCenterPoints;
	protected List<PageAreaResult> pageAreaResultList = new LinkedList<PageAreaResult>();
	protected Polygon leftArea;
	protected Polygon rightArea;
	protected int leftAreaValue;
	protected int rightAreaValue;
	protected ProjectionTranslator projectionTranslator;
	 
	public OMRProcessorResult() {
	}

	public OMRProcessorResult(Point2D[] deskewGuideCenterPoints, Polygon leftArea, Polygon rightArea, int leftAreaValue, int rightAreaValue, ProjectionTranslator projectionTranslator) {
		this.deskewGuideCenterPoints = deskewGuideCenterPoints;
		this.leftArea = leftArea;
		this.rightArea = rightArea;
		this.leftAreaValue = leftAreaValue;
		this.rightAreaValue = rightAreaValue;
		this.projectionTranslator = projectionTranslator;
	}

	@Override
	public String toString() {
		return "deskewGuides=" + this.deskewGuideCenterPoints[0] + "," + this.deskewGuideCenterPoints[3] + " cmd=" + this.pageAreaResultList;
	}
	
	public ProjectionTranslator getProjectionTranslator(){
		return projectionTranslator;
	}

	public Point2D[] getDeskewGuideCenterPoints() {
		return this.deskewGuideCenterPoints;
	}

	public List<PageAreaResult> getPageAreaResultList() {
		return this.pageAreaResultList;
	}

	public void addPageAreaResult(PageAreaResult pageAreaResult) {
		this.pageAreaResultList.add(pageAreaResult);
	}
	
	public FileResourceID getMasterFileResourceID(){
		return this.masterFileResourceID;
	}

	public void setMasterFileResourceID(FileResourceID masterFileResourceID){
		this.masterFileResourceID = masterFileResourceID;
	}
	
	public Polygon getLeftArea(){
		return leftArea;
	}
	
	public Polygon getRightArea(){
		return rightArea;
	}
	
	public int getLeftAreaValue(){
		return leftAreaValue;
	}

	public int getRightAreaValue(){
		return rightAreaValue;
	}
	
}
