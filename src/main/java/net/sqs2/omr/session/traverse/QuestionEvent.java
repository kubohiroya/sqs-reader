package net.sqs2.omr.session.traverse;

import java.util.List;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.Answer;
import net.sqs2.omr.model.Row;

public class QuestionEvent extends TraverseEvent {
	RowEvent rowEvent;
	FormMaster master;
	int questionIndex;
	List<FormArea> formAreaList;

	public QuestionEvent(RowEvent rowEvent, FormMaster master) {
		this.rowEvent = rowEvent;
		this.master = master;
		this.numEvents = master.getNumQuestions();
	}
	
	public String toString(){
		return "QuestionEvent{master:"+master.getRelativePath()+", questionIdex"+questionIndex+",rowIndex:"+rowEvent.getRowIndex();
	}

	public FormMaster getFormMaster() {
		return this.master;
	}

	public RowEvent getRowEvent() {
		return this.rowEvent;
	}

	public void setQuestionIndex(int questionIndex) {
		this.questionIndex = questionIndex;
	}

	public void setFormAreaList(List<FormArea> formAreaList) {
		this.formAreaList = formAreaList;
	}

	public FormArea getPrimaryFormArea() {
		return this.formAreaList.get(0);
	}

	public List<FormArea> getFormAreaList() {
		return this.formAreaList;
	}

	public int getQuestionIndex() {
		return this.questionIndex;
	}

	public Answer getAnswer() {
		Row row = this.rowEvent.getRow();
		if(row == null){
			throw new RuntimeException("row is null:"+rowEvent);
		}
		return row.getAnswer(this.questionIndex);
	}
}
