/*
Copyright (C) 2003 Morten O. Alver, Nizar N. Batada

All programs in this directory and
subdirectories are published under the GNU General Public License as
described below.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA

Further information about the GNU GPL is available at:
http://www.gnu.org/copyleft/gpl.ja.html

*/
package net.sf.jabref;
import java.io.File;

public class JabRef {

    public static void main(String[] args) {

	Globals.turnOffLogging();

		JabRefPreferences prefs = new JabRefPreferences();
		BibtexEntryType.loadCustomEntryTypes(prefs);
		Globals.setLanguage(prefs.get("language"), "");
		JabRefFrame jrf = new JabRefFrame();

		if(args.length > 0){
			System.out.println("Opening: " + args[0]);
			jrf.output("Opening: " + args[0]);
			//verify the file
			File f = new File (args[0]);
			
			if( f.exists() && f.canRead() && f.isFile()) {
				jrf.fileToOpen=f;
				jrf.openDatabaseAction.openIt(true);
			}else{
				System.err.println("Error" + args[0] + " is not a valid file or is not readable");
				//JOptionPane...
			}
			
			
		}else{//no arguments (this will be for later and other command line switches)
			// ignore..
		}
		

	}

}
