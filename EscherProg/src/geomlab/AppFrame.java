/*
 * AppFrame.java
 * 
 * This file is part of GeomLab
 * Copyright (c) 2005 J. M. Spivey
 * All rights reserved
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.      
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products 
 *    derived from this software without specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package geomlab;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

/** The main GUI frame for the GeomLab application */
public class AppFrame extends JFrame {
    private static final String svnid =
	"$Id: AppFrame.java 319 2008-04-03 10:05:25Z mike $";

    private final LogArea results = new LogArea(20, 40);
    protected final HistoryInput input = new HistoryInput(3, 30);
    
    private final JButton prevButton = makeButton("<", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    input.previous();
	}
    });
    
    private final JButton nextButton = makeButton(">", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    input.next();
	}
    });
    
    private final JButton clearButton = 
	makeButton("Clear", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		input.clear();
	    }
    });
    
    private final JButton goButton = makeButton("Go", new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    input.performAction();
	}
    });
    
    private boolean largeFont = false;

    public AppFrame() {
	super("GeomLab");
	setLocation(50, 50);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	Container content = getContentPane();
	JPanel controls = new JPanel(new BorderLayout());
	JPanel buttons = new JPanel(new VariGridLayout(3,1));
	JPanel prevnext = new JPanel(new VariGridLayout(1,2));
	Border myborder = BorderFactory.createCompoundBorder
	    (BorderFactory.createBevelBorder(BevelBorder.LOWERED),
	     BorderFactory.createEmptyBorder(2, 2, 2, 2));
	
	results.setBorder(myborder);
	content.add(new JScrollPane(results,
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
	    "Center");
	
	input.setLineWrap(true);
	input.setBorder(myborder);
	controls.add(input, "Center");
	
	buttons.add(prevnext);
	prevnext.add(prevButton);
	prevnext.add(nextButton);
	buttons.add(clearButton);
	buttons.add(goButton);
	controls.add(buttons, "East");
	content.add(controls, "South");
	pack();
	loadFontResource();
    }
    
    public void addActionListener(ActionListener listener) {
	input.addActionListener(listener);
    }
    
    public void setFocus() {
	input.requestFocus();
    }
    
    private static JButton makeButton(String text, ActionListener action) {
	JButton button = new JButton(text);
	button.addActionListener(action);
	return button;
    }
    
    private static Font fontResource = null;

    private void loadFontResource() {
        String name = "VeraMono.ttf";
        int size = (largeFont ? 16 : 12);
        
        if (fontResource == null) {
            ClassLoader loader = AppFrame.class.getClassLoader();
            InputStream stream = loader.getResourceAsStream(name);
        
            if (stream != null) {
        	try {
        	    fontResource = Font.createFont(Font.TRUETYPE_FONT, stream);
        	}
        	catch (IOException e) { } 
        	catch (FontFormatException e) { }
        	finally { 
        	    try { stream.close(); } catch (IOException e) { }
        	}
            }
        }
            	
    	if (fontResource != null)
    	    setFont(fontResource.deriveFont((float) size));
    	else
    	    setFont(new Font("Default", Font.PLAIN, size));
    }
    
    public void setFont(Font font) {
	super.setFont(font);
        results.setFont(font);
        input.setFont(font);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        input.setEnabled(enabled);
        prevButton.setEnabled(enabled);
        nextButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        goButton.setEnabled(enabled);
        
        JMenuBar menubar = getJMenuBar();
        if (menubar != null) menubar.setEnabled(enabled);
    }
    
    public void setAntialiased(boolean antialiased) {
	input.setAntialiased(antialiased);
	results.setAntialiased(antialiased);
    }
    
    public boolean isLargeFont() { return largeFont; }
    
    public void setLargeFont(boolean largeFont) {
	this.largeFont = largeFont;
	loadFontResource();
    }
    
    public void showError(int start, int end) {
	input.setEnabled(true);
	input.setSelectionStart(start);
	input.setSelectionEnd(end);
    }
    
    public PrintWriter getLogWriter() { return results.getPrintWriter(); }
}
