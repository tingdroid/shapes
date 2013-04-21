/*
 * AboutBox.java
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

import javax.swing.*;

/** A dialog box showing the version number, credits and licence information */
public class AboutBox {
    private static final String svnid =
	"$Id: AboutBox.java 331 2008-05-06 16:05:51Z mike $";

    /** Command -- show the about box */
    public static void aboutBox(GeomLab app) {
        String version =
            "GeomLab version 2.0\n"
            + "Copyright (c) 2005-2008 J. M. Spivey\n"
	    + "Font data Copyright (c) 2003 Bitstream, Inc.";
        String licence =
            "Redistribution and use in source and binary forms, with or\n"
            + "without modification, are permitted provided that the\n"
            + "following conditions are met:\n\n"
            + "1. Redistributions of source code must retain the above\n"
            + "   copyright notice, this list of conditions and the\n"
            + "   following disclaimer.\n"
            + "2. Redistributions in binary form must reproduce the above\n"
            + "   copyright notice, this list of conditions and the\n"
            + "   following disclaimer in the documentation and/or other\n"
            + "   materials provided with the distribution.\n"
            + "3. The name of the author may not be used to endorse or\n"
            + "   promote products derived from this software without\n"
            + "   specific prior written permission.\n\n"
            + "THIS SOFTWARE IS PROVIDED BY THE AUTHOR \"AS IS\" AND\n"
            + "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT \n"
            + "LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY\n"
            + "AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"
            + "IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,\n"
            + "INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n"
            + "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF\n"
            + "SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR\n"
            + "PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND\n"
            + "ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT\n"
            + "LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\n"
            + "ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN\n"
            + "IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
        JTextArea licenceArea = new JTextArea(licence);
        licenceArea.setEditable(false);
        String javaVersion = System.getProperty("java.version");
        String pingReply = app.phoneHome.getResult();
        Object contents[] = new Object[] { version, licenceArea, 
        	"Java version " + javaVersion, pingReply };
        JOptionPane.showMessageDialog(app.frame, contents, "About GeomLab",
        	JOptionPane.INFORMATION_MESSAGE);
    }
}
