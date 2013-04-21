/*
 * HistoryInput.java
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

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.TextAction;
import javax.swing.text.View;

/** A TextArea that can register ActionListeners that are notified
 * when the user presses Enter.  The commands that have been executed
 * are also recorded as a history. */
public class HistoryInput extends MyTextArea {
    private static final String svnid =
	"$Id: HistoryInput.java 306 2007-11-14 00:27:45Z mike $";
    
    protected java.util.List<String> history = new ArrayList<String>();
    protected int index;
    private ActionListener actionListener = null;
    private MyHighlightPainter lowlightPainter = new MyHighlightPainter();
    private Object lowlight = null;
    protected boolean showMatches = false;
    
    public HistoryInput(int rows, int columns) {
	super(rows, columns);
	installKeymap();
	
	// Remove the highlight whenever the insertion point moves
	addCaretListener(new CaretListener() {
	    public void caretUpdate(CaretEvent e) {
		removeLowlight();
	    }
	});
    }
    
    private void installKeymap() {
	Keymap keymap = JTextComponent.addKeymap(null, getKeymap());
	
	for (int i = 0; i < closers.length(); i++) {
	    final char ch = closers.charAt(i);
	    keymap.addActionForKeyStroke(
		    KeyStroke.getKeyStroke(ch),
		    new TextAction("insert-closer") {
			public void actionPerformed(ActionEvent e) {
			    replaceSelection(String.valueOf(ch));
			    if (showMatches)
				showMatch(getCaretPosition()-1);
			}
		    });
	}
	
	// Ctrl-Enter etc. -- submit the command
	TextAction submitAction =
	    new TextAction("history-go") {
		public void actionPerformed(ActionEvent e) {
		    performAction();
		}
	    };
	keymap.addActionForKeyStroke(
		KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_MASK),
		submitAction);
	keymap.addActionForKeyStroke(
		KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK),
		submitAction);
	
	// Ctrl-Up, Ctrl-P -- move to previous history item
	TextAction prevAction =
	    new TextAction("history-prev") {
	    public void actionPerformed(ActionEvent e) {
		previous();
	    }
	};
	keymap.addActionForKeyStroke(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK),
		prevAction);
	keymap.addActionForKeyStroke(
		KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_MASK),
		prevAction);
	
	// Ctrl-Down, Ctrl-N -- move to next history item
	TextAction nextAction =
	    new TextAction("history-prev") {
	    public void actionPerformed(ActionEvent e) {
		next();
	    }
	};
	keymap.addActionForKeyStroke(
		KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK),
		nextAction);
	keymap.addActionForKeyStroke(
		KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_MASK),
		nextAction);
	
	setKeymap(keymap);
    }
    
    
    public void setText(String s) {
	removeLowlight();
	super.setText(s);
    }
    
    public void clear() {
	setText("");
    }
    
    public void setShowMatches(boolean showMatches) {
	this.showMatches = showMatches;
    }
    
    public boolean isShowMatches() {
	return showMatches;
    }
    
    private void insertHistory() {
	String s = history.get(index);
	setText(s);
	setCaretPosition(s.length());
    }
    
    public void previous() {
	int n = history.size();
	if (n > 0) {
	    index--;
	    if (index < 0) index = n-1;
	    insertHistory();
	}
    }
    
    public void next() {
	int n = history.size();
	if (n > 0) {
	    index++;
	    if (index >= n) index = 0;
	    insertHistory();
	}
    }
    
    private final String openers = "([{", closers = ")]}";
    
    public void showMatch(int j) {
	String text = this.getText();
	int depth = 1, i = j;
	
	while (i > 0 && depth > 0) {
	    i--;
	    char c = text.charAt(i);
	    if (openers.indexOf(c) >= 0)
		depth--;
	    else if (closers.indexOf(c) >= 0)
		depth++;
	}
	
	boolean ok = (depth == 0 && openers.indexOf(text.charAt(i)) 
		== closers.indexOf(text.charAt(j)));
	
	addLowLight(i, j+1, !ok);
    }
    
    public void addLowLight(int i1, int i2, boolean bad) {
	Highlighter h = getHighlighter();
	try {
	    lowlightPainter.setColor(bad ? Color.red : getSelectionColor());
	    lowlight = h.addHighlight(i1, i2, lowlightPainter);
	}
	catch (BadLocationException e) { }
    }
    
    public void removeLowlight() {
	Highlighter h = getHighlighter();
	if (lowlight != null) h.removeHighlight(lowlight);
	lowlight = null;
    }
    
    public void performAction() {
	removeLowlight();
	
	String command = this.getText();
	
	if (! command.equals("") 
		&& (history.size() == 0 
			|| ! command.equals(history.get(history.size()-1))))
	    history.add(command);
	
	if (actionListener != null) {
	    actionListener.actionPerformed(
		    new ActionEvent(HistoryInput.this, 
			    ActionEvent.ACTION_PERFORMED, 
			    command));
	}
	
	index = history.size();
    }
    
    public void addActionListener(ActionListener l) {
	actionListener = AWTEventMulticaster.add(actionListener, l);
    }
    
    public void removeActionListener(ActionListener l) {
	actionListener = AWTEventMulticaster.remove(actionListener, l);
    }
    
    private static class MyHighlightPainter 
    				extends LayeredHighlighter.LayerPainter {
	Color color = null;
	
	public MyHighlightPainter() { }
	
	public void setColor(Color color) {
	    this.color = color;
	}
	
	public void paint(Graphics g, int p0, int p1, 
		Shape bounds, JTextComponent c) {
	    // Never used
	}
	
	public Shape paintLayer(Graphics g, int offs0, int offs1,
		Shape bounds, JTextComponent c, View view) {
	    // This returns the rectangle with the text, not the actual
	    // shape of the painted highlight.  Doesn't seem to matter.
	    
	    final int thick = 2;
	    
	    try {
		Shape shape;
		
		if (offs0 == view.getStartOffset() 
			&& offs1 == view.getEndOffset())
		    shape = bounds;
		else
		    shape = view.modelToView(offs0, Position.Bias.Forward,
			    offs1, Position.Bias.Backward,
			    bounds);
		
		Rectangle r = (shape instanceof Rectangle 
			? (Rectangle) shape 
				: shape.getBounds());
		g.setColor(color);
		g.fillRect(r.x, r.y + r.height - thick, r.width, thick);
		return r;
	    } 
	    catch (BadLocationException e) {
		return null;
	    }
	}
    }
}
