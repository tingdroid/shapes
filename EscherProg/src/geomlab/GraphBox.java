/*
 * GraphBox.java
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
import java.awt.image.*;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import geomlab.Command.CommandException;
import javax.swing.*;
import javax.swing.event.*;

/** A Frame for displaying a Picture object */
public class GraphBox extends JFrame implements Printable {
    private static final String svnid =
	"$Id: GraphBox.java 319 2008-04-03 10:05:25Z mike $";
    
    protected Drawable picture = null;
    protected boolean antialiased = false;
    private int imageMean = 144;

    protected final JComponent canvas = new JComponent() {
	public void paintComponent(Graphics g) {
	    if (picture == null) return;
	    
	    Graphics2D g2 = (Graphics2D) g;
	    if (antialiased)
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    Dimension dim = getSize();
	    int w = dim.width-1, h = dim.height-1;
	    int ww = w, hh = h;
	    float aspect = picture.getAspect();
	    
	    if (aspect == 0.0) return;
	    
	    /* Determine hh <= h and ww <= w so that hh = h or ww = w
	     * and ww/hh ~= aspect */ 
	    if (h * aspect >= w)
		hh = (int) (w / aspect + 0.5f);
	    else
		ww = (int) (h * aspect + 0.5f);
	    
	    g2.translate((w - ww)/2, (h - hh)/2);
	    picture.draw(g2, ww, hh, Color.white, sliderValue());
	}
    };
    
    private JSlider slider = new JSlider();

    public GraphBox(String title, JFrame parent) {
	super(title);
	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	
	Container contents = getContentPane();
	canvas.setBackground(Color.lightGray);
	canvas.setPreferredSize(new Dimension(400, 400));
	contents.add(canvas, "Center");
	contents.add(slider, "South");
	slider.setVisible(false);
	pack();
	
	slider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		canvas.repaint();
	    }
	});
	
	addWindowListener(new WindowAdapter() {
	    public void windowOpened(WindowEvent e) {
		setPreferredLocation();
	    }
	});
    }
    
    protected void setPreferredLocation() {
	Toolkit toolkit = getToolkit();
	Dimension screenSize = toolkit.getScreenSize();

	/* Fancy arithmetic with the location of the parent falls foul of
	 * an apparent inconsistency between Windows and X about the meaning
	 * of setLocation w.r.t. decorations on the frame.  So this simplistic
	 * approach will do for now. */
	
	if (screenSize.width >= 1024)
	    setLocation(600, 50);
	else
	    setLocation(screenSize.width - 450, 100);
    }
    
    public void setPicture(Drawable picture) {
	this.picture = picture;
	slider.setVisible(picture != null && picture.isInteractive());
	pack();
	if (isVisible()) canvas.repaint();
    }
    
    public Drawable getPicture() {
	return picture;
    }
    
    /** A scalable picture with fixed aspect ratio */
    public interface Drawable {
	public float getAspect();
	public boolean isInteractive();
	public void draw(Graphics2D g, int ww, int hh, 
		Color background, float slider);
    }

    public boolean isAntialiased() { return antialiased; }
    
    public void setAntialiased(boolean antialiased) {
        this.antialiased = antialiased;
	repaint();
    }
    
    public float sliderValue() { return slider.getValue() / 100.0f; }

    /** Command -- print the current picture */
    public void print() throws CommandException {
        if (getPicture() == null) 
            throw new CommandException("No picture", "#nopicture");
        
        PrinterJob job = PrinterJob.getPrinterJob();
        if (job == null)
            throw new CommandException("No print job", "#noprint");
        
        job.setPrintable(this);
        if (! job.printDialog()) return;
        
        try {
            job.print();
        }
        catch (PrinterException e) {
            throw new CommandException("Printing failed", "#printfail");
        }
    }

    public int print(Graphics g, PageFormat fmt, int n) {
	if (n > 0) return Printable.NO_SUCH_PAGE;
	double width = fmt.getImageableWidth();
	double height = fmt.getImageableHeight();
	double x = fmt.getImageableX(), y = fmt.getImageableY();
	float aspect = picture.getAspect();
	
	if (height * aspect >= width)
	    height = width / aspect;
	else
	    width = height * aspect;
	
	Graphics2D g2 = (Graphics2D) g;
	g2.translate(x, y);
	picture.draw(g2, (int) width, (int) height, Color.white,
		sliderValue());
	
	return Printable.PAGE_EXISTS;
    }

    public void setImageMean(int imageMean) {
	this.imageMean = imageMean;
    }

    public int getImageMean() {
	return imageMean;
    }
    
    public boolean isPicture() { return picture != null; }

    public void writePicture(File file) throws IOException {
	writePicture(picture, imageMean, sliderValue(), Color.white, file);
    }
    
    public static void writePicture(Drawable pic, int meanSize, float slider,
	    Color background, File file) throws IOException {
    	/* The dimensions of the image are chosen to give
    	 * approximately the right aspect ratio, and so that the
    	 * geometric mean of width and height is approx. meanSize */
    	float aspect = pic.getAspect();
    	float sqrtAspect = (float) Math.sqrt(aspect);
    	int width = Math.round(meanSize * sqrtAspect);
    	int height = Math.round(meanSize / sqrtAspect);
    	BufferedImage image = 
    	    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	Graphics g = image.getGraphics();
    	Graphics2D g2 = (Graphics2D) g;
    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    		RenderingHints.VALUE_ANTIALIAS_ON);
    	pic.draw(g2, width, height, background, slider);
        javax.imageio.ImageIO.write(image, "png", file);
    }
}   
