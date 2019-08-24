package net.sqs2.omr.session.service;

import java.util.List;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.FormAreaResult;
import net.sqs2.omr.model.MarkAreaResult;
import net.sqs2.omr.model.PageAreaResult;

public abstract class PageAreaResultListProcessor{

	protected FormMaster formMaster;
	int pageIndex;
	protected float threshold;
	List<PageAreaResult> pageAreaResultList;
	
	public PageAreaResultListProcessor(FormMaster formMaster, int pageIndex, float threshold, List<PageAreaResult> pageAreaResultList){
		this.formMaster = formMaster;
		this.pageIndex = pageIndex;
		this.threshold = threshold;
		this.pageAreaResultList = pageAreaResultList;
	}

	public abstract void processSelectSingleQuestion(FormArea firstFormArea, FormArea lastFormArea, int numMarks);
	 
	public abstract void processSelectSingleQuestionItem(FormArea formArea, float density);
			
	public void run(){

		processSelectSingleQuestions();
		
	}

	private void processSelectSingleQuestions() {
		FormArea firstFormArea = null;
		FormArea prevFormArea = null;
		int numMarks = 0;
		
		for(FormArea formArea: formMaster.getFormAreaListByPageIndex(pageIndex)){
			
			if(prevFormArea == null){
				firstFormArea = formArea;
			}else{
				if(prevFormArea.getQuestionIndex() != formArea.getQuestionIndex()){
					FormArea lastFormArea = prevFormArea;
					if( prevFormArea.isSelectMultiple()&& numMarks != 1){
						
						processSelectSingleQuestion(firstFormArea, lastFormArea, numMarks);
						
					}
					numMarks = 0;
					firstFormArea = formArea;
				}
			}
			
			FormAreaResult formAreaResult = (FormAreaResult)pageAreaResultList.get(formArea.getAreaIndexInPage());
			if(formAreaResult instanceof MarkAreaResult){
				MarkAreaResult markAreaResult = (MarkAreaResult) formAreaResult;
				float density = markAreaResult.getDensity();
				if(density < threshold){
					numMarks++;
				}
				processSelectSingleQuestionItem(formArea, density);
			}
			
			prevFormArea = formArea;
		}

		if(prevFormArea != null && prevFormArea.isSelectSingle() && numMarks != 1){
			FormArea lastFormArea = prevFormArea;
			processSelectSingleQuestion(firstFormArea, lastFormArea, numMarks);
		}
	}
}