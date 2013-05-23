// -*- mode: Java;  tab-width: 2; c-basic-offset: 2; -*-
//
// $Id: TextPanel.java 7033 2008-07-15 01:05:25Z tar $
//
//  Copyright (C) 2007 University of Southern California.
//  All Rights Reserved.
//

package edu.isi.bmkeg.lapdf.bin;

import java.util.Date;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

/** A text panel, consisting of a scrolling text area and buttons
 *  to save and clear the text.
 *
 *  @author  University of Southern California
 *  @version $Revision: 7033 $
 */
public class TextPanel extends JPanel {

  final static String DEFAULT_CLEAR_BUTTON_NAME = "Clear";
  final static String DEFAULT_SAVE_BUTTON_NAME  = "Save";
  final static int DEFAULT_HEIGHT = 20;
  final static int DEFAULT_WIDTH  = 80;
  JTextArea textOutput;

  /** Construct a default sized text panel.
   */
  public TextPanel () {
    this(DEFAULT_HEIGHT, DEFAULT_WIDTH, DEFAULT_SAVE_BUTTON_NAME, DEFAULT_CLEAR_BUTTON_NAME);
  }

  /** Construct a TextPanel with given button names.
	 *  If no name is given, then the specified button will not appear.
   *
   * @param saveText  Text string for labeling the save button.
   * @param clearText Text string for labeling the clear button.
   */
  public TextPanel (String saveText, String clearText) {
    this(DEFAULT_HEIGHT, DEFAULT_WIDTH, saveText, clearText);
  }


  /** Construct a TextPanel of the given size.
   *
   * @param height The height of the text panel's text area.
   * @param width  The width of the text panel's text area.
   */
  public TextPanel (int height, int width) {
		this(height, width, DEFAULT_SAVE_BUTTON_NAME, DEFAULT_CLEAR_BUTTON_NAME);
	}

	protected String getSaveDialogTitle() {
		return "Save Text Output";
	}

  /** Construct a TextPanel with given size and button names.
	 *  If no name is given, then the specified button will not appear.
   *
   * @param height The height of the text panel's text area.
   * @param width  The width of the text panel's text area.
   * @param saveText  Text string for labeling the save button.
   * @param clearText Text string for labeling the clear button.
   */
	public TextPanel (int height, int width, String saveText, String clearText) {
    super();
    setLayout(new BorderLayout());
    textOutput = new JTextArea(height, width);
    textOutput.setEditable(false);
    textOutput.setLineWrap(false);
    JScrollPane outputScroller = new JScrollPane(textOutput);
    add(outputScroller, BorderLayout.CENTER);

		if (saveText != null || clearText != null) {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.add(Box.createHorizontalGlue());

			if (saveText != null) {
				JButton saveButton = new JButton(saveText);
		    saveButton.addActionListener(new ActionListener () {
						public void actionPerformed(ActionEvent event) {
							JFileChooser chooser = new JFileChooser();
							chooser.setDialogTitle(getSaveDialogTitle());
							if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
								File file = chooser.getSelectedFile();
								if (file != null) {
									PrintWriter writer = null;
									try {
										writer = new PrintWriter(new FileWriter(file));
										writer.print(textOutput.getText());
									} catch (IOException ioe) {
										JOptionPane.showMessageDialog(null,  ioe.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
									} finally {
										if (writer != null) writer.close();
									}
								}
							}
						}
					});
				buttonPanel.add(Box.createHorizontalStrut(5));
				buttonPanel.add(saveButton);
			}
			if (clearText != null) {
				JButton clearButton = new JButton(clearText);
				clearButton.addActionListener(new ActionListener () {
						public void actionPerformed(ActionEvent event) {
							textOutput.setText("");
						}
					});
				buttonPanel.add(Box.createHorizontalStrut(5));
				buttonPanel.add(clearButton);
			}

			buttonPanel.add(Box.createHorizontalStrut(5));
			add(buttonPanel, BorderLayout.SOUTH);
		}
  }

  /** Sets the text entry.
	 * 
	 * @param text The new text value.
   */
  public void setText (String text) {
    textOutput.setText(text);
  }

  /** Gets the text entry.
	 * 
	 * @return The contents of the text area.
   */
  public String getText () {
    return textOutput.getText();
  }


  /** Append to  text entries.
	 *
	 * @param text The  text value to append.
   */
  public void append (String text) {
    textOutput.append(text);
  }


  /** Clears text entries.
   */
  public void clear () {
    textOutput.setText("");
  }


}
