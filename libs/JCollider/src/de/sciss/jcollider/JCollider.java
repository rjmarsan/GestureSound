/*
 *  JCollider.java
 *  JCollider
 *
 *  Copyright (c) 2004-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de , or visit http://www.sciss.de/jcollider
 *
 *
 *	JCollider is closely modelled after SuperCollider Language,
 *	often exhibiting a direct translation from Smalltalk to Java.
 *	SCLang is a software originally developed by James McCartney,
 *	which has become an Open Source project.
 *	See http://supercollider.sourceforge.net/ for details.
 *
 *
 *  Changelog:
 *		10-Sep-05	created
 *		11-Feb-08	added --bindefs option
 */

package de.sciss.jcollider;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *  This is a helper class containing utility static functions
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.36, 11-Oct-09
 */
public abstract class JCollider
{
	private static final double VERSION	= 0.36;
	private static final ResourceBundle resBundle = ResourceBundle.getBundle( "JColliderStrings" );
//	private static final Preferences prefs = Preferences.userNodeForPackage( JCollider.class );

	/**
	 *	<code>true</code> if we're running on Mac OS X.
	 *	This value can be used to check if certain UGens
	 *	are available, or to find the location of scsynth.
	 */
	public static final boolean	isMacOS		= System.getProperty( "os.name" ).indexOf( "Mac OS" ) >= 0;
	/**
	 *	<code>true</code> if we're running on a Windows system
	 */
	public static final boolean	isWindows	= System.getProperty( "os.name" ).indexOf( "Windows" ) >= 0;

	/**
	 *	This method gets called when one tries
	 *	to start the .jar file directly.
	 *	It prints copyright information and
	 *	quits, unless one of the test methods
	 *	is specified in the arguments
	 *
	 *	@param	args	shell arguments. there may be a single argument
	 *					&quot;--test1&quot; or &quot;--test2&quot; to
	 *					run the demos. &quot;--bindefs&quot; to create
	 *					a binary def file from the xml descriptions
	 */
    public static void main( String args[] )
	{
		final String demoClass;
	
		if( args.length == 1 ) {
			final String arg1 = args[ 0 ];
			if( arg1.equals( "--test1" )) {
				demoClass = "de.sciss.jcollider.test.Demo";
			} else if( arg1.equals( "--test2" )) {
				demoClass = "de.sciss.jcollider.test.MotoRevCtrl";
			} else if( arg1.equals( "--test3" )) {
				demoClass = "de.sciss.jcollider.test.BusTests";
			} else if( arg1.equals( "--bindefs" )) {
				try {
					UGenInfo.readDefinitions();
					UGenInfo.writeBinaryDefinitions( new File( "ugendefs.bin" ));
				}
				catch( IOException e1 ) {
					e1.printStackTrace();
				}
				demoClass = null;
				System.exit( 0 );
			} else {
				demoClass = null;
			}
		} else {
			demoClass = null;
		}
		
		if( demoClass != null ) {
			SwingUtilities.invokeLater( new Runnable() {
				public void run()
				{
					try {
						final Class c = Class.forName( demoClass );
						c.newInstance();
					}
					catch( Exception e1 ) {
						System.err.println( e1 );
						System.exit( 1 );
					}
				}
			});
			
		} else {
			System.err.println( "\nJCollider v" + VERSION + "\n" +
				getCopyrightString() + "\n\n" +
				getCreditsString() + "\n\n  " +
				getResourceString( "errIsALibrary" ));
			
			System.out.println( "\nThe following options are available:\n"+
			                    "--test1    SynthDef demo\n"+
			                    "--test2    MotoRev Control Demo\n"+
			                    "--test3    Bus Tests\n"+
			                    "--bindefs  Create Binary UGen Definitions\n");
			System.exit( 1 );
		}
    }

	/**
	 *	Returns the library's version.
	 *
	 *	@return	the current version of JCollider
	 */
	public static final double getVersion()
	{
		return VERSION;
	}

	/**
	 *	Returns a copyright information string
	 *	about the library
	 *
	 *	@return	text string which can be displayed
	 *			in an about box
	 */
	public static final String getCopyrightString()
	{
		return JCollider.getResourceString( "copyright" );
	}

	/**
	 *	Returns a license and website information string
	 *	about the library
	 *
	 *	@return	text string which can be displayed
	 *			in an about box
	 */
	public static final String getCreditsString()
	{
		return JCollider.getResourceString( "credits" );
	}

	public static final String getResourceString( String key )
	{
		try {
			return resBundle.getString( key );
		}
		catch( MissingResourceException e1 ) {
			return( "[Missing Resource: " + key + "]" );
		}
	}
   
	/**
	 *  Set a font for a container
	 *  and all children we can find
	 *  in this container (calling this
	 *  method recursively). This is
	 *  necessary because calling <code>setFont</code>
	 *  on a <code>JPanel</code> does not
	 *  cause the <code>Font</code> of the
	 *  gadgets contained in the panel to
	 *  change their fonts.
	 *
	 *  @param  c		the container to traverse
	 *					for children whose font is to be changed
	 *  @param  fnt		the new font to apply
	 *
	 *  @see	java.awt.Component#setFont( Font )
	 */
	public static void setDeepFont( Container c, Font fnt )
	{
		final Component[] comp = c.getComponents();
		
//		if( fnt == null ) {
//			final Application app = AbstractApplication.getApplication();
//			if( app == null ) return;
//			fnt = app.getWindowHandler().getDefaultFont();
//		}
		
//		if( c.getFont() != null ) return;
		c.setFont( fnt );
		for( int i = 0; i < comp.length; i++ ) {
			if( comp[ i ] instanceof Container ) {
				setDeepFont( (Container) comp[ i ], fnt );
			} else {
				comp[ i ].setFont( fnt );
			}
		}
	}

	/**
	 *  Displays an error message dialog by
	 *  examining a given <code>Exception</code>. Returns
	 *  after the dialog was closed by the user.
	 *
	 *  @param  component   the component in which to open the dialog.
	 *						<code>null</code> is allowed in which case
	 *						the dialog will appear centered on the screen.
	 *  @param  exception   the exception that was thrown. the message's
	 *						text is displayed using the <code>getLocalizedMessage</code>
	 *						method.
	 *  @param  title		name of the action in which the error occurred
	 *
	 *  @see	javax.swing.JOptionPane#showOptionDialog( Component, Object, String, int, int, Icon, Object[], Object )
	 *  @see	java.lang.Throwable#getLocalizedMessage()
	 */
	public static void displayError( Component component, Exception exception, String title )
	{
		String							message = exception.getLocalizedMessage();
		StringTokenizer					tok;
		final StringBuffer				strBuf  = new StringBuffer( getResourceString( "errException" ));
		int								lineLen = 0;
		String							word;
		String[]						options = { getResourceString( "buttonOk" ),
													getResourceString( "optionDlgStack" )};
	
		if( message == null ) message = exception.getClass().getName();
		tok = new StringTokenizer( message );
		strBuf.append( ":\n" );
		while( tok.hasMoreTokens() ) {
			word = tok.nextToken();
			if( lineLen > 0 && lineLen + word.length() > 40 ) {
				strBuf.append( "\n" );
				lineLen = 0;
			}
			strBuf.append( word );
			strBuf.append( ' ' );
			lineLen += word.length() + 1;
		}
		if( JOptionPane.showOptionDialog( component, strBuf.toString(), title, JOptionPane.YES_NO_OPTION,
									      JOptionPane.ERROR_MESSAGE, null, options, options[0] ) == 1 ) {
			exception.printStackTrace();
		}
	}
}