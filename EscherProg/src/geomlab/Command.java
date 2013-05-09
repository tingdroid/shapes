/*
 * Command.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** A command that may be appear on a menu.  Static methods of this class
 *  create the menus for the application */
public abstract class Command extends AbstractAction {
    
    private GeomLab app;
    
    public Command(String text, int mnemonic, GeomLab app) {
	super(text);
	putValue(MNEMONIC_KEY, new Integer(mnemonic));
	this.app = app;
    }
    
    public void actionPerformed(ActionEvent e) {
	try {
	    perform();
	}
	catch (CommandException ex) {
	    app.errorMessage(ex.getMessage(), ex.getErrtag());
	    app.prompt();
	}
    }
    
    public abstract void perform() throws CommandException;
    
    public static JMenuBar makeAppMenuBar(GeomLab app) {
	JMenuBar menuBar = new JMenuBar();
	menuBar.add(makeFileMenu(app));
	menuBar.add(makeToolsMenu(app));
	menuBar.add(makeOptionsMenu(app));
	menuBar.add(Box.createHorizontalGlue());
	menuBar.add(makeHelpMenu(app));
	return menuBar;
    }
    
    private static JMenu makeFileMenu(final GeomLab app) {
	JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(new Command("Load...", KeyEvent.VK_L, app) { 
            public void perform() throws CommandException {
        	JFileChooser loadDialog = new MyFileChooser();
		if (loadDialog.showOpenDialog(app.frame) 
			== JFileChooser.APPROVE_OPTION) {
		    File file = loadDialog.getSelectedFile();
		    app.loadFileCommand(file);
		}
            }
        });
        menu.addSeparator();
        menu.add(new Command("Load session ...", KeyEvent.VK_O, app) { 
            public void perform() throws CommandException {
        	JFileChooser loadDialog = new MyFileChooser(".gls");
		if (loadDialog.showOpenDialog(app.frame)
			== JFileChooser.APPROVE_OPTION) {
		    File file = loadDialog.getSelectedFile();
		    Session.loadSession(file);
		    app.logMessage("Loaded session from " + file.getName());
		    app.prompt();
		}
            }
        });
        menu.add(new Command("Save session ...", KeyEvent.VK_S, app) { 
            public void perform() throws CommandException {
        	JFileChooser saveDialog = new MyFileChooser(".gls");
		if (saveDialog.showSaveDialog(app.frame)
			== JFileChooser.APPROVE_OPTION) {
		    File file = saveDialog.getSelectedFile();
		    Session.saveSession(file);
		    app.logMessage("Saved session as " + file.getName());
		    app.prompt();
		}
            }
        }); 
        menu.addSeparator();
        menu.add(new Command("Print image ...", KeyEvent.VK_P, app) { 
            public void perform() throws CommandException {
        	app.arena.print();
            }
        });	
        menu.add(new Command("Image setup ...", KeyEvent.VK_I, app) { 
            public void perform() {
        	JTextField dimField = new JTextField(5);
		JPanel params = new JPanel();
		params.setLayout(new VariGridLayout(1, 2, 2));
		params.add(new JLabel("Mean dimension (pixels):"));
		params.add(dimField);
		dimField.setText(Integer.toString(app.arena.getImageMean()));
		
		int result = JOptionPane.showOptionDialog(
			app.frame, params, "Image parameters", 
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE, null, null, null);
		
		if (result == JOptionPane.OK_OPTION)
		    app.arena.setImageMean(
			    Integer.parseInt(dimField.getText()));
            }
        });
        menu.add(new Command("Save image ...", KeyEvent.VK_V, app) { 
            public void perform() throws CommandException {
        	if (! app.arena.isPicture())
		    throw new CommandException("No picture", "#nopicture");
		
		JFileChooser saveDialog = new MyFileChooser(".png");
		if (saveDialog.showSaveDialog(app.frame) 
			== JFileChooser.APPROVE_OPTION) {
		    File file = saveDialog.getSelectedFile();
		    try {
			app.arena.writePicture(file);
		    }
		    catch (IOException e) {
			throw new CommandException(
				"I/O failed while writing " + file.getName(),
				"#picio");
		    }
		}
            }
        }); 
        menu.addSeparator();
        menu.add(new Command("Exit", KeyEvent.VK_X, app) {
            public void perform() { app.exit(); }
        });
	return menu;
    }

    private static JMenu makeToolsMenu(final GeomLab app) {
	JMenu menu = new JMenu("Tools");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.add(new Command("Evaluate expression", KeyEvent.VK_E,  app) {
            public void perform() { app.frame.input.performAction(); }
        });
        menu.add(new Command("List defined names", KeyEvent.VK_L,  app) {
            public void perform() { app.listNames(); }
        });
        menu.add(new Command("Find definition", KeyEvent.VK_F,  app) {
            public void perform() { app.findDefinition(); }
        });
	return menu;
    }

    private static JMenu makeOptionsMenu(final GeomLab app) {
	JMenu menu = new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_O);
        menu.add(new Option("Match brackets", KeyEvent.VK_M) {
            public boolean isValue() { 
        	return app.frame.input.isShowMatches(); 
            }
            public void setValue(boolean value) {
        	app.frame.input.setShowMatches(value);
            }
        });
        menu.add(new Option("Use larger font", KeyEvent.VK_L) {
            public boolean isValue() {
        	return app.frame.isLargeFont();
            }
            public void setValue(boolean value) {
        	app.frame.setLargeFont(value);
            }
        });
        menu.add(new Option("Smoother screen display", KeyEvent.VK_S) {
            public boolean isValue() {
        	return app.isAntialiased();
            }
            public void setValue(boolean value) {
        	app.setAntialiased(value);
            }
        });
        menu.add(new Option("Count reduction steps", KeyEvent.VK_C) {
            public boolean isValue() {
        	return app.getStatsFlag();
            }
            public void setValue(boolean value) {
        	app.setStatsFlag(value);
            }
        });
//        menu.add(new Option("Show bytecode", KeyEvent.VK_B, app) {
//            public boolean isValue() {
//        	return funbase.Closure.Assembler.showCode;
//            }
//            public void setValue(boolean value) {
//        	funbase.Closure.Assembler.showCode = value;
//            }
//        });
        return menu;
    }

    private static JMenu makeHelpMenu(final GeomLab app) {
	JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.add(new Command("Contents ...", KeyEvent.VK_C, app) {
            public void perform() { HelpFrame.showContents(); }
        });
        menu.add(new Command("Help after an error ...", KeyEvent.VK_E, app) {
            public void perform() { HelpFrame.errorHelp(app.getErrtag()); }
        });
        menu.add(new Command("About GeomLab ...", KeyEvent.VK_A,  app) {
            public void perform() {
        	AboutBox.aboutBox(app);
            }
        });
	return menu;
    }

    public static class CommandException extends Exception {
	private String errtag;
	
	public CommandException(String message, String errtag) {
	    super(message);
	    this.errtag = errtag;
	}
	
	public String getErrtag() { return errtag; }
    }

    private static abstract class Option extends JCheckBoxMenuItem 
    implements ActionListener {
	public Option(String text, int mnemonic) {
	    super(text);
	    setMnemonic(mnemonic);
	    setSelected(isValue());
	    addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
	    setValue(isSelected());
	}
	
	public abstract boolean isValue();
	public abstract void setValue(boolean value);
    }
}