/*
 * RunScript.java
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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;

import funbase.BootLoader;
import geomlab.Command.CommandException;


/** RunScript allows expressions to be evaluated from the command line, and
 * that is convenient for preparing images to be included in documents.
 * It is also capable of bootstrapping the compiler from a text file
 * containing a dump of the object code. */
public class RunScript extends GeomBase {

    public void evalString(String exp) {
	StringReader reader = new StringReader(exp);
	eval_loop(reader, true, null);
    }

    public static void main(String args[]) {
	final RunScript app = new RunScript();
	GeomBase.registerApp(app);
	app.setLog(new PrintWriter(System.out));
	
	int i = 0;

	try {
	    if (i+1 < args.length && args[i].equals("-b")) {
		Session.installPlugin(GeomBase.class);
		Session.installPlugin(plugins.BasicPrims.class);
		Session.installPlugin(funbase.Assembler.class);
		Session.installPlugin(plugins.Cell.class);
		BootLoader.bootstrap(new File(args[i+1]));
		i += 2;
	    }
	    else if (i+1 < args.length && args[i].equals("-s")) {
		Session.loadSession(new File(args[i+1]));
		i += 2;
	    }
	    else
		Session.loadResource("geomlab.gls");
	}
	catch (CommandException e) {
	    System.err.println(e);
	    System.exit(2);
	}

	while (i < args.length) {
	    if (args[i].equals("-e")) {
		if (i+1 < args.length)
		    app.evalString(args[++i]);
	    }
	    else if (args[i].equals("-")) {
		app.loadFromStream(System.in);
	    }
	    else {
		app.loadFromFile(new File(args[i]), false);
	    }
	    i++;
	}

	System.exit(app.getStatus());
    }
}
