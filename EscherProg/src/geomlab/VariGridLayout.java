/*
 * VariGridLayout.java
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

/** A layout manager that maintains a grid, but with variable
 * column widths and row heights.  Components must be added by
 * rows in left-to-right order.
 * 
 * Implemented as a specialized version of GridBagLayout,
 * with (thankfully) a simple interface.
 */
public class VariGridLayout extends GridBagLayout {
    private static final String svnid =
	"$Id: VariGridLayout.java 174 2006-04-07 15:06:23Z mike $";
    
    private int ncols;
    private int row = 0, col = 0;
    private GridBagConstraints gbc = new GridBagConstraints();
    private Insets insets;
    
    /** Construct a layout that is nrows x ncols */
    public VariGridLayout(int nrows, int ncols) {
	this(nrows, ncols, 0);
    }
    
    /** Construct a layout that is nrows x ncols, 
     * with same inset all round. */
    public VariGridLayout(int nrows, int ncols, int inset) {
	this.ncols = ncols;
	
	insets = new Insets(inset, inset, inset, inset);
	gbc.fill = GridBagConstraints.BOTH;
	gbc.insets = insets;
    }

    /** Add another component in row-by-row order */
    public void addLayoutComponent(Component c, Object cns) {
	gbc.gridx = col;
	gbc.gridy = row;
	super.addLayoutComponent(c, gbc);
	col++;
	if (col == ncols) {
	    col = 0; row++;
	}
    }
}
