/*
 Copyright 2011 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2011/12/03

 */
package net.sqs2.omr.ui;

import java.awt.CardLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.ui.swing.GroupLayoutUtil;
import net.sqs2.omr.ui.swing.ImageIconUtil;
import net.sqs2.omr.ui.util.Observer;

import org.apache.commons.lang.time.DurationFormatUtils;

class ExportControlPanel extends JPanel implements Observer<Float>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** export */
	
	JButton spreadSheetBrowseButton = new JButton(Messages.getString("ExportControlPanel.SpreadSheet")); //$NON-NLS-1$
	JButton openFolderButton = new JButton(Messages.getString("ExportControlPanel.Browse")); //$NON-NLS-1$
	
	CardLayout cardLayout = new CardLayout();
	JPanel exportButtonPanel = new JPanel();
	JPanel progressPanel = new JPanel();
	
	public static final String ELAPSED_LABEL = Messages.getString("ExportControlPanel.Elapsed"); //$NON-NLS-1$
	public static final String FINISHED_LABEL = Messages.getString("ExportControlPanel.Finished"); //$NON-NLS-1$
	public static final String REMAINS_LABEL = Messages.getString("ExportControlPanel.Reamins"); //$NON-NLS-1$
	
	JLabel elapsedLabel = new JLabel(ELAPSED_LABEL);
	JLabel remainsLabel = new JLabel(REMAINS_LABEL);
	JTextField elapsed = new JTextField();
	JTextField remains = new JTextField();
	
	public static final String[]  COMPONENT_NAMES = {Messages.getString("ExportControlPanel.ProgressPanel"), Messages.getString("ExportControlPanel.ExportButtonPanel")}; //$NON-NLS-1$ //$NON-NLS-2$
	MarkReaderSession session;
	
	ExportControlPanel(MarkReaderSession session){
		this.session = session;
		
		spreadSheetBrowseButton.setIcon(ImageIconUtil.createImageIcon(Messages.getString("ExportControlPanel.OpenSpreadSheetButtonIconPath"))); //$NON-NLS-1$
		openFolderButton.setIcon(ImageIconUtil.createImageIcon(Messages.getString("ExportControlPanel.OpenFolderButtonIconPath"))); //$NON-NLS-1$
				
		exportButtonPanel.setLayout(new GridLayout(1,2));
		exportButtonPanel.add(spreadSheetBrowseButton);
		exportButtonPanel.add(openFolderButton);
		
		spreadSheetBrowseButton.setEnabled(false);
		openFolderButton.setEnabled(false);

		elapsed.setEditable(false);
		remains.setEditable(false);
		layout(progressPanel, elapsedLabel, remainsLabel, elapsed, remains);
		
		setBorder(new CompoundBorder(new EmptyBorder(3,3,3,3), new CompoundBorder(new EtchedBorder(), new EmptyBorder(3,3,3,3))));
		setLayout(cardLayout);
		
		add(COMPONENT_NAMES[0], progressPanel);
		add(COMPONENT_NAMES[1], exportButtonPanel);

		cardLayout.show(this, COMPONENT_NAMES[0]);
		session.getProgressRate().bind(this);
	}
	
	private void layout(JPanel p1, JLabel label1, JLabel label2, JTextField tf1, JTextField tf2){
		label1.setHorizontalAlignment(JLabel.RIGHT);
		label2.setHorizontalAlignment(JLabel.RIGHT);
		GroupLayoutUtil.layout(p1, new JLabel[]{label1, label2}, new JComponent[]{tf1, tf2}, false);
	}
	
	public void setEnabled(boolean enabled){
		spreadSheetBrowseButton.setEnabled(enabled);
		openFolderButton.setEnabled(enabled);
	}
	
	public void update(final Float r){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				long duration = System.currentTimeMillis() - session.getTimeStarted();
				String elapsedText = DurationFormatUtils.formatDuration(duration, Messages.getString("ExportControlPanel.ElapsedTimeFormat")); //$NON-NLS-1$
				elapsed.setText(elapsedText);
				
				exportButtonPanel.setToolTipText(FINISHED_LABEL+elapsedText);
				spreadSheetBrowseButton.setToolTipText(FINISHED_LABEL+elapsedText);
				openFolderButton.setToolTipText(FINISHED_LABEL+elapsedText);

				float rate = r.floatValue();
				String remainsText = Messages.getString("ExportControlPanel.DefaultRemainsText"); //$NON-NLS-1$
				if(0.001 <= rate){
					long remainsMils = (long) (duration / rate - duration);
					if(0 < remainsMils){
						remainsText = DurationFormatUtils.formatDuration(remainsMils, Messages.getString("ExportControlPanel.RemainsTimeFormat")); //$NON-NLS-1$
						remains.setText(remainsText);
						return;
					}
				}
				remains.setText(Messages.getString("ExportControlPanel.DefaultRemainsText"));		 //$NON-NLS-1$
			}
		});
	}
}