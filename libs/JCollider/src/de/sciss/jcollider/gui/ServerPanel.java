/*
 *  ServerWindow.java
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
 *	See http://www.audiosynth.com/ for details.
 *
 *
 *  Changelog:
 *		10-Sep-05	created
 */

package de.sciss.jcollider.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import de.sciss.jcollider.Constants;
import de.sciss.jcollider.JCollider;
import de.sciss.jcollider.Server;
import de.sciss.jcollider.ServerEvent;
import de.sciss.jcollider.ServerListener;

/**
 *	A quick hack to provide a GUI
 *	element looking similar to SCLang's
 *	server window. Additionally, you
 *	can opt to create a text console
 *	for scsynth text output when booting
 *	the local server.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 25-Feb-08
 */
public class ServerPanel
extends JPanel
implements ServerListener, Constants
{
	/**
	 *	<code>Font</code> used for the text console.
	 *	You can set this field prior to instantiating
	 *	the <code>ServerPanel</code> to use a
	 *	different font.
	 */
	public static Font	fntConsole;
	/**
	 *	<code>Font</code> used for the GUI elements (buttons).
	 *	You can set this field prior to instantiating
	 *	the <code>ServerPanel</code> to use a
	 *	different font.
	 */
	public static Font	fntGUI			= new Font( "Helvetica", Font.PLAIN, 12 );
	/**
	 *	<code>Font</code> used for server name box.
	 *	You can set this field prior to instantiating
	 *	the <code>ServerPanel</code> to use a
	 *	different font.
	 */
	public static Font	fntBigName		= new Font( "Helvetica", Font.BOLD, 16 );

	/**
	 *	Constructor flag: create a console panel.
	 */
	public static final int CONSOLE		=	0x01;
	/**
	 *	Constructor flag: create a boot/quit button
	 */
	public static final int BOOTQUIT	=	0x02;
//	public static final int KILL		=	0x04;
	/**
	 *	Constructor flag: create a server name box
	 */
	public static final int BIGNAME		=	0x08;
//	public static final int MAKEDEFAULT	=	0x10;
//	public static final int RECORD		=	0x20;
	/**
	 *	Constructor flag: create a status panel
	 *	for server CPU usage and number of nodes.
	 */
	public static final int COUNTS		=	0x40;
	/**
	 *	Constructor flag: create a button to
	 *	turning OSC dumping on/off.
	 */
	public static final int DUMP		=	0x80;

	/**
	 *	Constructor flag: shortcut for creating
	 *	those GUI elements that SClang provides.
	 */
	public static final int 	MIMIC		=	0x7E;

	private static final int 	BUTTONS		=	0xBE;

	protected final Server		server;
	protected final ServerPanel	enc_this	= this;
	
	private JTextField			lbBigName	= null;
	private ActionBoot			actionBoot	= null;
	
	private JFrame				ourFrame;
	private JScrollPane			ggScroll;
	private boolean				showHide;
//	private javax.swing.Timer	showHideTimer;
	
	private JLabel lbCntAvgCPU, lbCntPeakCPU, lbCntUGens, lbCntSynths, lbCntGroups, lbCntSynthDefs;
	
	private static final String COUNT_NA	= "?";
	private static final MessageFormat msgCntPercent = new MessageFormat( "{0,number,#.#} %", Locale.US );

	private static final Color	colrRunning	= new Color( 0x58, 0xB0, 0x8D );
	private static final Color	colrBooting	= new Color( 0xCC, 0xFF, 0x33 );
	private static final Color	colrStopped	= new Color( 0x60, 0x60, 0x60 );

	static {
		fntConsole	= JCollider.isMacOS ? new Font( "Monaco", Font.PLAIN, 10 ) :
										  new Font( "Monospaced", Font.PLAIN, 11 );
	}
	
	/**
	 *	Creates a new <code>ServerPanel</code> for
	 *	the given server and with GUI elements specified
	 *	by the flags.
	 *
	 *	@param	server	the server to which this panel is
	 *					connected
	 *	@param	flags	a mask of flags that define which
	 *					GUI elements should be created,
	 *					e.g. <code>CONSOLE</code>, <code>BOOTQUIT</code> etc.
	 */
	public ServerPanel( Server server, int flags )
	{
		super();

		this.server	= server;
		
		final BoxLayout	lay = new BoxLayout( this, BoxLayout.Y_AXIS );

		setLayout( lay );
		
		if( (flags & CONSOLE) != 0 ) {
			add( createConsole() );
		}
		if( (flags & BUTTONS) != 0 ) {
			add( createButtons( flags ));
		}
		if( (flags & COUNTS) != 0 ) {
			add( createCountsPanel() );
		}
		
//		JCollider.setDeepFont( this, fntGUI );
		
		server.addListener( this );
	}
	
	/**
	 *	Call this method if the server
	 *	isn't used any more or the panel's
	 *	parent window is disposed. This
	 *	will free any resources occupied by
	 *	the panel and remove listeners.
	 */
	public void dispose()
	{
		server.removeListener( this );
	}
	
	private JComponent createConsole()
	{
		final LogTextArea	lta		= new LogTextArea( 12, 40, false, null );
		final InputMap		imap	= lta.getInputMap();
		final ActionMap		amap	= lta.getActionMap();

		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), "clear" );
		amap.put( "clear", lta.getClearAction() );
		lta.setFont( fntConsole );

		Server.setPrintStream( lta.getLogStream() );

		ggScroll = lta.placeMeInAPane();

//		showHideTimer = new javax.swing.Timer( 100, new ActionListener() {
//			public void actionPerformed( ActionEvent e )
//			{
//				final boolean	isVisible		= ggScroll.isVisible();
//				final int		currentHeight	= isVisible ? ggScroll.getHeight() : 0; 
//				final int		frameWidth		= ourFrame.getWidth();
//				final int		frameHeight		= ourFrame.getHeight();
//			
//				if( showHide ) {
//					if( !isVisible ) {
//						ourFrame.setSize( frameWidth, frameHeight + 32 );
//						ggScroll.setVisible( true );
//					} else {
//						if( currentHeight < 256 ) {
//							ourFrame.setSize( frameWidth, frameHeight + 32 );
//						} else {
//							showHideTimer.stop();
//						}
//					}
//				} else {
//					if( currentHeight > 32 ) {
//						ourFrame.setSize( frameWidth, frameHeight - 32 );
//					} else {
//						ggScroll.setVisible( false );
//						showHideTimer.stop();
//					}
//				}
//			}
//		});
		
		return ggScroll;
	}

	private JComponent createCountsPanel()
	{
		final JPanel p	= new JPanel( new GridLayout( 3, 4, 0, 4 ));
		
		lbCntAvgCPU		= new JLabel();
		lbCntPeakCPU	= new JLabel();
		lbCntUGens		= new JLabel();
		lbCntSynths		= new JLabel();
		lbCntGroups		= new JLabel();
		lbCntSynthDefs	= new JLabel();
		
		p.add( new JLabel( JCollider.getResourceString( "countsAvgCPU" ) + " : ", SwingConstants.RIGHT ));
		p.add( lbCntAvgCPU );
		p.add( new JLabel( JCollider.getResourceString( "countsPeakCPU" ) + " : ", SwingConstants.RIGHT ));
		p.add( lbCntPeakCPU );
		p.add( new JLabel( JCollider.getResourceString( "countsUGens" ) + " : ", SwingConstants.RIGHT ));
		p.add( lbCntUGens );
		p.add( new JLabel( JCollider.getResourceString( "countsSynths" ) + " : ", SwingConstants.RIGHT ));
		p.add( lbCntSynths );
		p.add( new JLabel( JCollider.getResourceString( "countsGroups" ) + " : ", SwingConstants.RIGHT ));
		p.add( lbCntGroups );
		p.add( new JLabel( JCollider.getResourceString( "countsSynthDefs" ) + " : ", SwingConstants.RIGHT ));
		p.add( lbCntSynthDefs );
		
		p.setBorder( BorderFactory.createEmptyBorder( 2, 8, 8, 8 ));
		JCollider.setDeepFont( p, fntGUI );
		
		p.setMaximumSize( p.getPreferredSize() );
		updateCounts();
		return p;
	}
	
	private JComponent createButtons( int flags )
	{
//		final Box		b	= Box.createHorizontalBox();
		final JToolBar	tb	= new JToolBar();
		AbstractButton	but;
Insets insets;
	
		tb.setBorderPainted( false );
		tb.setFloatable( false );
	
		if( (flags & BOOTQUIT) != 0 ) {
			actionBoot	= new ActionBoot();
			but			= new JButton( actionBoot );
			but.setFont( fntGUI );
//			b.add( but );
insets = but.getMargin();
but.setMargin( new Insets( insets.top + 2, insets.left + 4, insets.bottom + 2, insets.right + 4 ));
			tb.add( but );
		}
		if( (flags & BIGNAME) != 0 ) {
			lbBigName	= new JTextField( 8 );
			lbBigName.setFont( fntBigName );
			lbBigName.setMaximumSize( lbBigName.getPreferredSize() );
			lbBigName.setText( server.getName() );
			lbBigName.setEditable( false );
			lbBigName.setBorder( BorderFactory.createEmptyBorder( 2, 4, 2, 4 ));
			lbBigName.setBackground( Color.black );
			lbBigName.setHorizontalAlignment( SwingConstants.CENTER );
			updateBigName();
//			b.add( lbBigName );
			tb.add( lbBigName );
		}
//		if( (flags & MAKEDEFAULT) != 0 ) {
//			b.add( new JButton( new actionMakeDefaultClass() ));
//		}
		if( (flags & DUMP) != 0 ) {
			but = new JToggleButton( new ActionDump() );
			but.setFont( fntGUI );
insets = but.getMargin();
but.setMargin( new Insets( insets.top + 2, insets.left + 4, insets.bottom + 2, insets.right + 4 ));
//			b.add( but );
			tb.add( but );
		}
//		b.add( Box.createHorizontalGlue() );
//		tb.add( Box.createHorizontalGlue() );
	
//		return b;
		return tb;
	}
	
	protected void updateBigName()
	{
		if( lbBigName == null ) return;
	
		if( server.isRunning() ) {
			lbBigName.setForeground( colrRunning );
		} else if( server.isBooting() ) {
			lbBigName.setForeground( colrBooting );
		} else {
			lbBigName.setForeground( colrStopped );
		}
	}

	private void updateCounts()
	{
		if( lbCntAvgCPU == null ) return;

		if( server.isRunning() ) {
			final Server.Status status = server.getStatus();
			final Float[] cntArgs	= new Float[ 1 ];
			cntArgs[ 0 ] = new Float( status.avgCPU );
			lbCntAvgCPU.setText( msgCntPercent.format( cntArgs ));
			cntArgs[ 0 ] = new Float( status.peakCPU );
			lbCntPeakCPU.setText( msgCntPercent.format( cntArgs ));
			lbCntUGens.setText( String.valueOf( status.numUGens ));
			lbCntSynths.setText( String.valueOf( status.numSynths ));
			lbCntGroups.setText( String.valueOf( status.numGroups ));
			lbCntSynthDefs.setText( String.valueOf( status.numSynthDefs ));
		} else {
			lbCntAvgCPU.setText( COUNT_NA );
			lbCntPeakCPU.setText( COUNT_NA );
			lbCntUGens.setText( COUNT_NA );
			lbCntSynths.setText( COUNT_NA );
			lbCntGroups.setText( COUNT_NA );
			lbCntSynthDefs.setText( COUNT_NA );
		}
	}
	
	protected void showHideConsole( boolean show )
	{
//		if( (ourFrame == null) || (showHideTimer == null) || (this.showHide == show) ) return;
		if( (ourFrame == null) || (this.showHide == show) ) return;
		
		this.showHide = show;
		ggScroll.setVisible( show );
		ourFrame.pack();
		
//		showHideTimer.start();
	}
	
	/**
	 *	Creates a window containing
	 *	a <code>ServerPanel</code> for the given
	 *	server. Uses default flags (<code>MIMIC</code>).
	 *
	 *	@param	server	the server to which the
	 *					panel shall be connected
	 *
	 *	@return			a frame containing the panel. This
	 *					frame is already made visible. The default
	 *					close operation is <code>DO_NOTHING_ON_CLOSE</code>,
	 *					so you will want to attach a <code>WindowListener</code>
	 *					which deals with closing and cleanup.
	 */
	public static JFrame makeWindow( Server server )
	{
		return makeWindow( server, MIMIC );
	}

	/**
	 *	Creates a window with custom flags.
	 *	See the one argument method for details.
	 *
	 *	@param	server	the server to which the
	 *					panel shall be connected
	 *	@param	flags	a mask of flags that define which
	 *					GUI elements should be created,
	 *					e.g. <code>CONSOLE</code>, <code>BOOTQUIT</code> etc.
	 *	
	 *	@see	#makeWindow( Server )
	 */
	public static JFrame makeWindow( Server server, int flags )
	{
		final ServerPanel	sp	= new ServerPanel( server, flags );
		final JFrame		f	= new JFrame( server.getName() + " server" );
		final Container		cp	= f.getContentPane();
		
		sp.ourFrame = f;
		if( sp.ggScroll != null ) sp.ggScroll.setVisible( false );
		
		f.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		cp.setLayout( new BorderLayout() );
		cp.add( sp, BorderLayout.CENTER );
		f.pack();
		f.setVisible( true );
		f.toFront();
		
		return f;
	}
	
// -------------- ServerListener interface --------------

	/**
	 *	This class implements the <code>ServerListener</code>
	 *	interface to be notified about the server
	 *	booting and quitting, and for tracking
	 *	the status.
	 */
	public void serverAction( ServerEvent e )
	{
		switch( e.getID() ) {
		case ServerEvent.RUNNING:
			if( actionBoot != null ) {
				actionBoot.booted();
			}
			updateBigName();
			break;

		case ServerEvent.STOPPED:
			if( actionBoot != null ) {
				actionBoot.terminated();
			}
			updateBigName();
			updateCounts();
			break;

		case ServerEvent.COUNTS:
			updateCounts();
			break;
		
		default:
			break;
		}
	}

// -------------- internal classes --------------

	private class ActionBoot
	extends AbstractAction
	{
		private boolean booted;
	
		protected ActionBoot()
		{
			super( JCollider.getResourceString( "buttonBoot" ));
			
			booted = server.isRunning() || server.isBooting();
		}
		
		public void actionPerformed( ActionEvent e )
		{
			try {
				if( booted ) {
					server.quit();
				} else {
					server.boot();
					updateBigName();
					showHideConsole( true );
				}
			}
			catch( IOException e1 ) {
				JCollider.displayError( enc_this, e1, getValue( NAME ).toString() );
			}
		}
		
		protected void terminated()
		{
			booted = false;
			putValue( NAME, JCollider.getResourceString( "buttonBoot" ));
			showHideConsole( false );
		}

		protected void booted()
		{
			booted = true;
			putValue( NAME, JCollider.getResourceString( "buttonQuit" ));
		}
	} // class actionBootClass

	private class ActionDump
	extends AbstractAction
	{
		private boolean dumping;
	
		protected ActionDump()
		{
			super( JCollider.getResourceString( "buttonDumpOSC" ));
			
			dumping = server.getDumpMode() != kDumpOff;
		}
		
		public void actionPerformed( ActionEvent e )
		{
			final AbstractButton b = (AbstractButton) e.getSource();
		
			dumping = b.isSelected();
			try {
				server.dumpOSC( dumping ? kDumpText : kDumpOff );
			}
			catch( IOException e1 ) {
				JCollider.displayError( enc_this, e1, getValue( NAME ).toString() );
			}
		}
	} // class actionDumpClass
}