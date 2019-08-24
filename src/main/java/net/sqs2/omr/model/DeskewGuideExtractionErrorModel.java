package net.sqs2.omr.model;

import java.io.Serializable;

public class DeskewGuideExtractionErrorModel extends OMRProcessorErrorModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected OMRProcessorErrorModel headerErrorModel;
	protected OMRProcessorErrorModel footerErrorModel;

	public DeskewGuideExtractionErrorModel(OMRProcessorErrorModel headerErrorModel, OMRProcessorErrorModel footerErrorModel) {
		this.headerErrorModel = headerErrorModel;
		this.footerErrorModel = footerErrorModel;
	}

	public OMRProcessorErrorModel getHeaderErrorModel() {
		return this.headerErrorModel;
	}

	public OMRProcessorErrorModel getFooterErrorModel() {
		return this.footerErrorModel;
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getSimpleName()+"(");
		if(headerErrorModel != null){
			b.append("header=");
			b.append(headerErrorModel.getClass().getSimpleName());
		}
		if(footerErrorModel != null){
			if(headerErrorModel != null){
				b.append(" ,");
			}
			b.append("footer=");
			b.append(footerErrorModel.getClass().getSimpleName());
		}
		return b.append(")").toString();
	}
}
