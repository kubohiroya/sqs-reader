package net.sqs2.omr.result.export.spreadsheet;

import org.apache.commons.collections15.multimap.MultiHashMap;

import net.sqs2.omr.model.Answer;
import net.sqs2.omr.model.Config;
import net.sqs2.omr.model.MarkAreaAnswer;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.omr.result.export.MarkAreaAnswerValueUtil;
import net.sqs2.omr.session.traverse.MasterEvent;
import net.sqs2.omr.session.traverse.QuestionEvent;
import net.sqs2.omr.session.traverse.SessionSourceEvent;
import net.sqs2.omr.session.traverse.SourceDirectoryEvent;
import net.sqs2.omr.session.traverse.SpreadSheetTraverseEventListener;

/**
 * Abstract base class for spreadsheet based
 * export modules.
 * 
 *
 */
public abstract class AbstractExportModule implements SpreadSheetTraverseEventListener
{
    protected float densityThreshold;
    protected float doubleMarkSuppressionThreshold;
    protected float noMarkSuppressionThreshold;;
    

    /**
     * Subclasses only constructor.
     */
    protected AbstractExportModule()
    {
        super();
    }
    @Override
    public final void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
        SourceDirectoryConfiguration sourceDirectoryConfiguration = sourceDirectoryEvent.getSourceDirectory().getConfiguration();
        Config config = sourceDirectoryConfiguration.getConfig();
        SourceConfig sourceConfig = (SourceConfig)config.getPrimarySourceConfig();
        this.densityThreshold = sourceConfig.getMarkRecognitionConfig().getMarkRecognitionDensityThreshold();
        this.doubleMarkSuppressionThreshold = sourceConfig.getMarkRecognitionConfig().getDoubleMarkErrorSuppressionThreshold();
        this.noMarkSuppressionThreshold = sourceConfig.getMarkRecognitionConfig().getNoMarkErrorSuppressionThreshold();
        onStartSourceDirectory(sourceDirectoryEvent);
    }
    /**
     * Empty template method for subclasses of {@link AbstractExportModule}
     * that must extend this method instead of {@link #startSourceDirectory(SourceDirectoryEvent)}
     * @param sourceDirectoryEvent A {@link SourceDirectoryEvent}, never <code>null</code>.
     */
    protected void onStartSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent)
    {
        
    }
    @Override
    public void startSessionSource(SessionSourceEvent sessionSourceEvent) {
    }

    @Override
    public void startMaster(MasterEvent masterEvent) {
    }

    @Override
    public void endSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
        // do nothing
    }

    @Override
    public void endMaster(MasterEvent masterEvent) {
        // do nothing
    }

    @Override
    public void endSessionSource(SessionSourceEvent sessionSourceEvent) {
        // do nothing
    }

    /**
     * Implementations helper
     * @param taskErrorModelMap
     * @param answer
     * @return
     */
    protected final boolean checkWriteAnswerPrecondition(MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap,
        Answer answer)
    {
        if (answer == null || (taskErrorModelMap != null && !taskErrorModelMap.isEmpty())) {
            return false;
        }
        return true;
    }
    /**
     * Implementation helper
     * @param questionEvent
     * @param answer
     * @return
     */
    protected final String getSingleValue(QuestionEvent questionEvent, MarkAreaAnswer answer)
    {
        return   MarkAreaAnswerValueUtil.createSelectSingleMarkAreaAnswerValueString(
            this.densityThreshold, this.doubleMarkSuppressionThreshold, this.noMarkSuppressionThreshold, answer,
            questionEvent.getFormAreaList(), ',');
    }




}