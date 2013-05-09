/*
 * MyFileChooser.java
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

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

/** A JFileChooser that remembers the directory between instances, and
 *  can filter by a default extension */
class MyFileChooser extends JFileChooser {

    /** Default extension for selected files */
    private String ext;
    
    private static File defaultDir = null;
    
    public MyFileChooser() { 
	ext = null;
    }
    
    public MyFileChooser(String ext) {
	this.ext = ext;
	setFileFilter(new ExtFilter(ext));
    }
    
    public int showOpenDialog(Component parent) {
	if (defaultDir != null)
	    setCurrentDirectory(defaultDir);
	int status = super.showOpenDialog(parent);
	defaultDir = getCurrentDirectory();
	return status;
    }
    
    public int showSaveDialog(Component parent) {
	if (defaultDir != null)
	    setCurrentDirectory(defaultDir);
	int status = super.showSaveDialog(parent);
	defaultDir = getCurrentDirectory();
	return status;
    }
    
    public File getSelectedFile() {
	File file = super.getSelectedFile();
	if (file != null && ext != null) {
	    String name = file.getName();
	    if (name.indexOf('.') < 0)
		return new File(file.getParent(), name + ext);
	}
	return file;
    }
    
    /** Filter files with the given extension */
    private static class ExtFilter extends javax.swing.filechooser.FileFilter {
	private String ext;
	
	public ExtFilter(String ext) { this.ext = ext; }
	
	public boolean accept(File f) {
	    return f.isDirectory() || f.getName().endsWith(ext); 
	}
	
	public String getDescription() {
	    return ext + " files";
	}
    }
}