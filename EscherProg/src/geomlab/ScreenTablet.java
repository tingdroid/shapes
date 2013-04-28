/*
 * ScreenTablet.java
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Stack;

import plugins.Tablet;
import plugins.Tran2D;
import plugins.Vec2D;

/** Painting context for drawing on the screen */
public class ScreenTablet extends Tablet {
    private static final String svnid =
	"$Id: ScreenTablet.java 372 2008-10-06 22:49:30Z mike $";

    private Graphics2D gcxt;
    private Stack<Graphics2D> saved = new Stack<Graphics2D>();

    public ScreenTablet(Graphics2D g, float slider) {
	super(slider);
	this.gcxt = g;
    }
    
    // drawStroke and fillOutline use our careful rounding

    public void drawStroke(Vec2D[] stroke, Tran2D t) {
	int n = stroke.length;
	int xx[] = new int[n], yy[] = new int[n];
	for (int j = 0; j < n; j++) {
	    xx[j] = t.scaleX(stroke[j]);
	    yy[j] = t.scaleY(stroke[j]);
	}
	gcxt.setColor(Color.black);
	gcxt.drawPolyline(xx, yy, n);
	// Draw the endpoints separately, in case drawPolyline
	// misses them.  Is this still needed?
	gcxt.drawLine(xx[0], yy[0], xx[0], yy[0]);
	gcxt.drawLine(xx[n-1], yy[n-1], xx[n-1], yy[n-1]);
    }

    public void fillOutline(Vec2D[] outline, int color, Tran2D t) {
	int n = outline.length;
	int xx[] = new int[n], yy[] = new int[n];
	for (int j = 0; j < n; j++) {
	    xx[j] = t.scaleX(outline[j]);
	    yy[j] = t.scaleY(outline[j]); 
	}
	gcxt.setColor(new Color(color));
	gcxt.fillPolygon(xx, yy, n);
    }
    
    // drawLine and drawArc can use the rounding from Java2D

    public void drawLine(Vec2D from, Vec2D to, int color, Tran2D t) {
	Vec2D a = t.transform(from), b = t.transform(to);
	gcxt.setColor(new Color(color));
	gcxt.draw(new Line2D.Float(a.x, a.y, b.x, b.y));
    }

    public void drawArc(Vec2D centre, float xrad, float yrad,
	    float start, float extent, int color, Tran2D t) {
	// Java assumes a barbarian coordinate system, so we must negate
	// the angles here:
	Shape arc0 = 
	    new Arc2D.Float(centre.x-xrad, centre.y-yrad, 
		    2*xrad, 2*yrad, -start, -extent, Arc2D.OPEN);
	AffineTransform tt = t.asAffineTransform();
	GeneralPath arc = new GeneralPath();
	arc.append(arc0.getPathIterator(tt), false);
	gcxt.setColor(new Color(color));
	gcxt.draw(arc);
    }
    
    public void save() {
	Graphics2D g2 = (Graphics2D) gcxt.create();
	saved.push(gcxt);
	gcxt = g2;
    }

    public void restore() {
	gcxt.dispose();
	gcxt = saved.pop();
    }

    public void setStroke(float width) {
	gcxt.setStroke(new BasicStroke(width));
    }

    public void drawImage(BufferedImage image, Tran2D t) {
	/* We draw the photo slightly large, so that it overlaps the
	 * bounding box slightly on all sides.  This avoids unsightly
	 * white lines between adjacent photos. */
	int w = image.getWidth(), h = image.getHeight();
	float u = t.getXaxis().length(), v = t.getYaxis().length();
	final float m = 1;  // Overlap in pixels
	Tran2D t1 = t.scale(1/u, 1/v).translate(-m, v+m)
					.scale((u+2*m)/w, -(v+2*m)/h);
	gcxt.drawImage(image, t1.asAffineTransform(), null);
    }

    public boolean isTiny(Tran2D t) {
	return t.isTiny(2.0f);
    }
}