/**
 * 
 */
package net.sqs2.omr.result.export.model;


import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.model.MarkAreaAnswer;
import net.sqs2.omr.model.MarkAreaAnswerItemSet;
import net.sqs2.omr.model.MarkRecognitionConfig;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.model.SpreadSheet;
import net.sqs2.omr.session.traverse.MarkReadStatusRetrieveModule;
import net.sqs2.omr.session.traverse.QuestionEvent;
import net.sqs2.omr.session.traverse.RowGroupEvent;
import net.sqs2.omr.session.traverse.SessionSourceEvent;
import net.sqs2.omr.session.traverse.SourceDirectoryEvent;

public class MarkAreaErrorModel extends MarkReadStatusRetrieveModule {

	private MarkAreasTableModel noAnswerMarkAreasTableModel;
	private MarkAreasTableModel multipleAnswersMarkAreasTableModel;
	private int noAnswerMarkAreasSerial = 0;
	private int multipleAnswersMarkAreasSerial = 0;

	private float densityThreshold, doubleMarkMarginThreshold, noMarkRecoveryThreshold;

	public MarkAreaErrorModel() {
		super();
		this.noAnswerMarkAreasTableModel = new MarkAreasTableModel();
		this.multipleAnswersMarkAreasTableModel = new MarkAreasTableModel();
	}
	
	public MarkAreasTableModel getNoAnswerMarkAreasTableModel(){
		return noAnswerMarkAreasTableModel;
	}
	
	public MarkAreasTableModel getMultipleAnswersMarkAreasTableModel(){
		return multipleAnswersMarkAreasTableModel;
	}
	
	void reset() {
		this.noAnswerMarkAreasTableModel.clear();
		this.multipleAnswersMarkAreasTableModel.clear();
		this.noAnswerMarkAreasSerial = 0;
		this.multipleAnswersMarkAreasSerial = 0;
	}

	@Override
	public void startSessionSource(SessionSourceEvent sessionSourceEvent) {
		super.startSessionSource(sessionSourceEvent);
		reset();
	}

	@Override
	public void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
		super.startSourceDirectory(sourceDirectoryEvent);
		SourceDirectory sourceDirectory = sourceDirectoryEvent.getSourceDirectory();
		MarkRecognitionConfig config = ((SourceConfig)sourceDirectory.getConfiguration().getConfig().getPrimarySourceConfig())
				.getMarkRecognitionConfig();
		this.densityThreshold = config.getMarkRecognitionDensityThreshold();
		this.doubleMarkMarginThreshold = config.getDoubleMarkErrorSuppressionThreshold();
		this.noMarkRecoveryThreshold = config.getNoMarkErrorSuppressionThreshold();
	}

	@Override
	public void endQuestion(QuestionEvent questionEvent) {

		SourceDirectoryEvent sourceDirectoryEvent = questionEvent.getRowEvent().getRowGroupEvent().getSpreadSheetEvent().getSourceDirectoryEvent();
		int tableIndex = sourceDirectoryEvent.getIndex();
		
		super.endQuestion(questionEvent); // do not remove

		FormArea primaryFormArea = questionEvent.getPrimaryFormArea();
		if (primaryFormArea.isMarkArea()) {

			MarkAreaAnswer answer = (MarkAreaAnswer) questionEvent.getAnswer();
			MarkAreaAnswerItemSet answerItemSet = answer.createMarkAreaAnswerItemSet();
			int columnIndex = questionEvent.getPrimaryFormArea().getQuestionIndex();
			int pageStart = questionEvent.getPrimaryFormArea().getPage();

			if (primaryFormArea.isSelectSingle()) {

				processSelect1Errors(questionEvent, tableIndex, answerItemSet, columnIndex, pageStart);

			} else if (primaryFormArea.isSelectMultiple()) {

			} else {
				throw new RuntimeException("invalid formArea type");
			}

		} else if (primaryFormArea.isTextArea()) {
			// do nothing
		} else {
			throw new RuntimeException("invalid formArea type");
		}
	}

	private void processSelect1Errors(QuestionEvent questionEvent, int tableIndex, MarkAreaAnswerItemSet answerItemSet, int columnIndex, int pageStart) {
		boolean[] isMarked = answerItemSet.getIsSelectedBooleanArray(this.densityThreshold, this.doubleMarkMarginThreshold, this.noMarkRecoveryThreshold);
		int pageEnd = questionEvent.getFormAreaList().get(isMarked.length - 1).getPage();

		int numSelected = answerItemSet.getNumSelected(this.densityThreshold, this.doubleMarkMarginThreshold, this.noMarkRecoveryThreshold);
		if (numSelected == 1) {
			return;
		}
		int rowIndex = questionEvent.getRowEvent().getIndex();
		RowGroupEvent rowGroupEvent = questionEvent.getRowEvent().getRowGroupEvent();
		int rowGroupRowIndex = rowGroupEvent.getIndex();
		SpreadSheet spreadSheet = rowGroupEvent.getSpreadSheetEvent().getSpreadSheet();
		SourceDirectory rowGroupSourceDirectory = rowGroupEvent.getSourceDirectory();

		if (numSelected == 0) {
			this.noAnswerMarkAreasTableModel.addRow(++this.noAnswerMarkAreasSerial, 
					spreadSheet,
					rowGroupSourceDirectory, rowGroupRowIndex, pageStart, pageEnd, 
					tableIndex,
					rowIndex,
					columnIndex);
		} else {
			this.multipleAnswersMarkAreasTableModel.addRow(++this.multipleAnswersMarkAreasSerial,
					spreadSheet, rowGroupSourceDirectory, rowGroupRowIndex, pageStart, pageEnd,
					tableIndex,
					rowIndex, 
					columnIndex);
		}
	}
}
