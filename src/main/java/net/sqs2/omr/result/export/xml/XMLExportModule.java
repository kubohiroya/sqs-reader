/**
 * XMLExportModule.java

 Copyright 2009 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Author hiroya, Michele Vivoda
 */

package net.sqs2.omr.result.export.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.model.Answer;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.MarkAreaAnswer;
import net.sqs2.omr.model.MarkAreaAnswerItem;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.TextAreaAnswer;
import net.sqs2.omr.result.export.spreadsheet.AbstractExportModule;
import net.sqs2.omr.session.traverse.MasterEvent;
import net.sqs2.omr.session.traverse.PageEvent;
import net.sqs2.omr.session.traverse.QuestionEvent;
import net.sqs2.omr.session.traverse.QuestionItemEvent;
import net.sqs2.omr.session.traverse.RowEvent;
import net.sqs2.omr.session.traverse.RowGroupEvent;
import net.sqs2.omr.session.traverse.SessionSourceEvent;
import net.sqs2.omr.session.traverse.SourceDirectoryEvent;
import net.sqs2.omr.session.traverse.SpreadSheetEvent;
import net.sqs2.omr.session.traverse.SpreadSheetTraverseEventListener;
import net.sqs2.util.HTMLUtil;

import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.io.IOUtils;

/**
 * A {@link SpreadSheetTraverseEventListener} implementation
 * to export results as an xml format.
 * 
 *
 */
public class XMLExportModule extends AbstractExportModule {

    private final ExtraModel extraModel = new ExtraModel();
    private boolean writePage = false;
	private PrintWriter out;


	@Override
	public void startSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
        System.out.println("Start sheet" + spreadSheetEvent);
        File resultDirectory = new File(spreadSheetEvent.getSpreadSheet().getSourceDirectory().getDirectory().getAbsoluteFile(), AppConstants.RESULT_DIRNAME);
	    File xml = new File(resultDirectory, "sqs-results.xml");
	    FileOutputStream xmlOut = null;
	    Writer writer = null;
        try
	    { 
	        xmlOut = new FileOutputStream(xml);
    	    writer = new OutputStreamWriter(xmlOut,"UTF-8");
    	    writer = new BufferedWriter(writer);
            this.out = new PrintWriter(writer);
            this.out.write("<result xmlns='http://sqs2.net/2012/result'>");
        }
	    catch (IOException e) {
            
            IOUtils.closeQuietly(xmlOut);
            IOUtils.closeQuietly(writer);
	        handleException("Could not create xml export file " + xml, e);
        }
	    
	    
    }

	private void handleException(String msg, IOException e)
    {
        throw new RuntimeException(msg, e);
    }

	
    @Override
	public void startRowGroup(RowGroupEvent rowGroupEvent) {
        extraModel.clear();
        this.out.write("<schema.physical>");
        ArrayList<FormArea> list = rowGroupEvent.getFormMaster().getFormAreaList();
        for (FormArea fa : list)
        {
            //int questionIndex = questionEvent.getIndex();
            //final FormArea fa = questionEvent.getPrimaryFormArea();
            final String faLabel = fa.getLabel();
            final String faHint= fa.getHint();
            final String faType = fa.getType();
            
            startOpenTag("pqdef");
            writeAttr("index", fa.getQuestionIndex());
            writeAttr("areaIndex", fa.getAreaIndexInPage());
            
            writeAttr("id", fa.getId());
            writeAttr("qid", fa.getQID());
            writeAttr("itemLabel", fa.getItemLabel());
            writeAttr("itemIndex", fa.getItemIndex());
            writeAttr("kind", faType);
            writeAttr("label", faLabel);
            writeAttr("hint", faHint);
            closeOpenTag();

        }
        this.out.write("</schema.physical>");
        this.out.write("<schema.logical>");
        for(String qid  : rowGroupEvent.getFormMaster().getQIDSet())
        {
            ArrayList<FormArea> falist = rowGroupEvent.getFormMaster().getFormAreaList(qid);
            for (int i=0,len=falist.size();i<len;i++)
            {
                FormArea fa = falist.get(i);
               
                if (i == 0) {
                    extraModel.put(fa);
                    final String faLabel = fa.getLabel();
                    final String faHint= fa.getHint();
                    final String faType = fa.getType();
                    startOpenTag("qdef");
                    writeAttr("kind", faType);
                    writeAttr("index", fa.getQuestionIndex());
                    writeAttr("id", fa.getQID());
                    writeAttr("label", faLabel);
                    writeAttr("hint", faHint);
                    endOpenTag();
                } 
                
                if (fa.isTextArea()) {
                } else if (fa.isSelectSingle() || fa.isSelectMultiple()) {
                    startOpenTag("option");
                    writeAttr("value", fa.getItemValue());
                    writeAttr("label", fa.getItemLabel());
                    closeOpenTag();
                    if (fa.isSelectMultiple()) {
                        extraModel.addSelectManyOption(fa.getQID(), fa.getItemValue(), fa.getItemLabel());
                    }
                } else throw new RuntimeException("ASSERT unknown type:" + fa.getType());
                
                
                //writeAttr("itemLabel", fa.getItemLabel());
                if (i == len - 1) closeElement("qdef");
                
            }
            

        }
        this.out.write("</schema.logical>");
        this.out.write("<rows>");
        
	}
    
    @Override
	public void startRow(RowEvent rowEvent) {
	    //rowEvent.getRow().
	    final int index = rowEvent.getRowIndex();
	    //final String id = rowEvent.getRow().getID();
		this.out.write("\n<row");
		writeAttr("index", index);
		//writeAttr("id", id);
        out.write(">\n");
	}

	@Override
	public void startPage(PageEvent rowEvent) {
	    if (writePage ) this.out.write("\n<page>");
	}

	@Override
	public void startQuestion(QuestionEvent questionEvent) {
        
	    startOpenTag("q");
	    //questionEvent.
        final int questionIndex = questionEvent.getQuestionIndex();
        final String id =extraModel.getQuestionIdFromIndex(questionIndex);
        //writeAttr("index", questionIndex);
        writeAttr("def", id);
        //final String faLabel = questionEvent.getLabel();
        //final String faHint= questionEvent.getHint();
        //final String faType = questionEvent.getType();
        //writeAttr("kind", faType);
        //writeAttr("label", faLabel);
        //writeAttr("hint", faHint);
        endOpenTag();

	    MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap = questionEvent.getRowEvent().getTaskErrorModelMultiHashMap();
		//int numPageIDsWithTaskErrors = taskErrorModelMap.size();
		FormArea primaryFormArea = questionEvent.getPrimaryFormArea();
		questionEvent.getPrimaryFormArea();
	
		Answer answer = questionEvent.getAnswer();
		if (primaryFormArea.isSelectSingle()) {
		    writeSelectSingleAnswer(questionEvent, taskErrorModelMap, answer);
		} else if (primaryFormArea.isSelectMultiple()) {
			writeSelectMultipleAnswer(questionEvent, taskErrorModelMap, answer);
		} else if (primaryFormArea.isTextArea()) {
			writeTextAreaAnswer(taskErrorModelMap, answer);
		}
	}
	
	private void writeSelectSingleAnswer(QuestionEvent questionEvent, MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap, Answer answer) {
        if (!checkWriteAnswerPrecondition(taskErrorModelMap, answer)) return;
        final String value = getSingleValue(questionEvent, ((MarkAreaAnswer) answer));
        if (value != null) {
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreElements())
            {
                String s = st.nextToken().trim();
                if (s.length()!=0) {
                    writeValueElement(s);
                }
            }
        }
        
    }

    
    private void writeTextAreaAnswer(MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap, Answer answer) {
        if (!checkWriteAnswerPrecondition(taskErrorModelMap, answer)) return;
        final String value = ((TextAreaAnswer) answer).getValue();
        if (value != null) {
            writeValueElement(value);
            
        }
    }

    private void writeSelectMultipleAnswer(final QuestionEvent questionEvent, final MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap, final Answer answer) {
        if (!checkWriteAnswerPrecondition(taskErrorModelMap, answer)) return;
        
        final int questionIndex = questionEvent.getIndex();
        final String qid = extraModel.getQuestionIdFromIndex(questionIndex);
        int size = questionEvent.getFormAreaList().size();
        for (int itemIndex = 0; itemIndex < size; itemIndex++) {
            
            MarkAreaAnswerItem answerItem = ((MarkAreaAnswer) answer).getMarkAreaAnswerItem(itemIndex);
            
            final String outValue = extraModel.getOptionValue(qid, itemIndex);
            //+ " .... " +  qid + " - " + String.valueOf(questionIndex) + " - " + itemIndex;
            if (answerItem.isManualMode()) {
                if (answerItem.isManualSelected()) {
                    
                    writeValueElement(outValue);
                        
                } else {
                    //this.csvWriter.print("0");
                }
            } else {
                if (answerItem.getDensity() < this.densityThreshold) {
                    writeValueElement(outValue);
                } else {
                    //this.csvWriter.print("0");
                }
            }
        }
    }

    static class ExtraModel
    {
        private HashMap<Integer, String> questionIndex2Id = new HashMap<Integer, String>();
        private final HashMap<String,ArrayList<CodeLabel>> questionId2Options = new HashMap<String, ArrayList<CodeLabel>>();
        
        static class CodeLabel
        {
            final String code;
            final String label;
            public CodeLabel(String code,
                String label)
            {
                super();
                this.code = code;
                this.label = label;
            }
            
        }
        /**
         * 
         * @param questionIndex
         * @return A {@link String}, never <code>null</code>.
         */
        public String getQuestionIdFromIndex(int questionIndex)
        {
            final String id = questionIndex2Id.get(new Integer(questionIndex));
            if (id == null) throw new IllegalStateException("Assert - no id for " + questionIndex);
            return id;
        }

        public String getOptionValue(String qid, int itemIndex)
        {
            final ArrayList<CodeLabel> list = questionId2Options.get(qid);
            if (list==null) throw new IllegalArgumentException("unknown qid:" + qid);
            return list.get(itemIndex).code;
        }

        
        public void addSelectManyOption(String qid, String itemId, String label)
        {
            final String key = qid;
            ArrayList<CodeLabel> list = questionId2Options.get(key);
            if (list==null) {
                list = new ArrayList<CodeLabel>();
                questionId2Options.put(key, list);
            }
            list.add(new CodeLabel(itemId, label));
            
        }   

        public void put(FormArea fa)
        {
            questionIndex2Id.put(getKey(fa), fa.getQID());
        }

        
        private Integer getKey(FormArea fa)
        {
            return new Integer(fa.getQuestionIndex());
        }

        public void clear()
        {
            questionIndex2Id.clear();
            questionId2Options.clear();
            
        }
    }
    private void closeElement(String tag)
    {
        out.write("</" + tag + ">");
    }

    /**
     * Writes <code>/&gt;</code>
     */
    private void closeOpenTag()
    {
        out.write("/>");
    }

    
    /**
     * Writes <code>&gt;</code>
     */
    private void endOpenTag()
    {
	    out.write(">");        
    }

    /**
     * Writes <code>&lt;</code> + "tag"
     */
        private void startOpenTag(String tag)
    {
	    this.out.write("\n<" + tag);
        
    }

    private void writeAttr(String name, int value)
    {
        writeAttr(name, String.valueOf(value));
    }

    private void writeAttr(String name, String value)
    {
        if (value==null) return;
        
        this.out.write(" " + name + "='" + escape(value) + "'");
    }

    private String escape(String content)
    {
	    return HTMLUtil.escapeHTML(content);
    }

    @Override
	public void startQuestionItem(QuestionItemEvent questionItemEvent) {
//	    int index = questionItemEvent.getItemIndex();
//        final FormArea fa = questionItemEvent.getFormArea();
//        final String faLabel = fa.getItemLabel();
//        final String faValue = fa.getItemValue();
//        this.out.write("\n\t<item ");
//        writeAttr("value",faValue);
//        writeAttr("label",faLabel);
//        out.write(">");
	   
	}

	@Override
	public void endQuestionItem(QuestionItemEvent questionItemEvent) {
        //this.out.write("</item>");
	}

	@Override
	public void endQuestion(QuestionEvent questionEvent) {
//	    Answer answer = questionEvent.getAnswer();
//	    if(answer!=null) {
//	        if (answer instanceof MarkAreaAnswer) {
//	            MarkAreaAnswer maa = ((MarkAreaAnswer)answer);
//	            MarkAreaAnswerItem[] arr = maa.getMarkAreaAnswerItemArray();
//	            for (MarkAreaAnswerItem markAreaAnswerItem : arr)
//                {
//                    //if (markAreaAnswerItem..isManualSelected()) {
//                        writeValueElement(String.valueOf(markAreaAnswerItem.getItemIndex()));
//                        
//                    //}
//                }
//	        } else if (answer instanceof TextAreaAnswer) {
//	            TextAreaAnswer taa = (TextAreaAnswer) answer;
//	            final String path = taa.getAreaImageFilePath();
//	            writeValueElement(path);
//	        }
//	    }
        this.out.write("\n</q>");
	}

	private void writeValueElement(String s)
    {
	    startOpenTag("v");
        endOpenTag();
        out.write(escape(s));
        closeElement("v");
    }

    @Override
	public void endPage(PageEvent pageEvent) {
        if (writePage) this.out.write("\n</page>");
	}

	@Override
	public void endRow(RowEvent rowEvent) {
        this.out.write("\n</row>\n");
	}

	@Override
	public void endRowGroup(RowGroupEvent rowGroupEvent) {
        //v this.out.write("\n</rowgroup>");
	    out.write("\n</rows>");
        
	}

	@Override
	public void endSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
	    this.out.write("\n</result>");
	    this.out.flush();
        this.out.close();
	}

	

	
}
