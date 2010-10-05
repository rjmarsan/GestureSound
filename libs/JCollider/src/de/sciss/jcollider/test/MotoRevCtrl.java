/*
 *  MotoRevCtrl
 *
 *  Created by Hanns Holger Rutz on 29.07.06.
 */
 
package de.sciss.jcollider.test;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import de.sciss.jcollider.*;
import de.sciss.jcollider.gui.*;
import de.sciss.net.*;

/**
 *	Provides a control enabled version of MotoRev with simple GUI.
 *	<p>
 *	To compile, be sure to add JCollider.jar to your classpath.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 25-Feb-08
 */
public class MotoRevCtrl
implements ServerListener
{
	protected Server	server	= null;
	protected Synth		synth	= null;
	
	private static final String[]		ctrlNames	= { "pulseModFreq", "pulseFreqMin", "pulseFreqMax", "pulseWidth", "lowPassFreq", "amp" };
	private static final float[]		ctrlLags	= { 0.1f,           0.1f,           0.1f,           0.1f,         0.1f,          0.1f };
	private static final ControlSpec[]	ctrlSpecs	= { new ControlSpec( 0.01,   100.0, Warp.exp, 0.0,   0.2, "Hz" ),	// pulseModFreq
														new ControlSpec( 0.1,   1000.0, Warp.exp, 0.0,  10.0, "Hz" ),	// pulseFreqMin
														new ControlSpec( 0.1,   1000.0, Warp.exp, 0.0,  30.0, "Hz" ),	// pulseFreqMax
														new ControlSpec( 0.01,    0.99, Warp.lin, 0.0,   0.5, null ),	// pulseWidth
														new ControlSpec( 20.0, 20000.0, Warp.exp, 0.0, 100.0, "Hz" ),	// lowPassFreq
														new ControlSpec(  0.0,     1.0, Warp.lin, 0.0,   0.4, null ),	// amp
	};
	private EZSlider[]					ezs;
	private boolean						defSent		= false;

//	public static void main( String args[] )
//	{
//		SwingUtilities.invokeLater( new Runnable() {
//			public void run()
//			{
//				new MotoRevCtrl();
//			}
//		});
//	}
	
	public MotoRevCtrl()
	{
		final String fs = File.separator;

		createControlWindow();
		try {
//			UGenInfo.readDefinitions();	// necessary if we build our own synth defs
			UGenInfo.readBinaryDefinitions();	// necessary if we build our own synth defs
			server	= new Server( "myServer" );
			File f = findFile( JCollider.isWindows ? "scsynth.exe" : "scsynth", new String[] {
				fs + "Applications" + fs + "SuperCollider_f",
				fs + "Applications" + fs + "SC3",
				fs + "usr" + fs + "local" + fs + "bin",
				fs + "usr" + fs + "bin",
				"C:\\Program Files\\SC3",
				"C:\\Program Files\\SuperCollider_f"
			});
			if( f != null ) Server.setProgram( f.getAbsolutePath() );
			server.addListener( this );
			try {
				// detect server that was started from outside (e.g. sclang)
				server.start();
				server.startAliveThread();
			}
			catch( IOException e1 ) { /* ignored */ }
		}
		catch( IOException e1 ) {
			e1.printStackTrace();
//			reportError( e1 );
			System.exit( 1 );
		}
		createServerWindow();
	}

	private static File findFile( String fileName, String[] folders )
	{
		File f;
	
		for( int i = 0; i < folders.length; i++ ) {
			f = new File( folders[ i ], fileName );
			if( f.exists() ) return f;
		}
		return null;
	}

	private void createServerWindow()
	{
		final JFrame spf = ServerPanel.makeWindow( server, ServerPanel.MIMIC | ServerPanel.CONSOLE | ServerPanel.DUMP );
		spf.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
	}
	
	private void createControlWindow()
	{
		final JFrame				win;
		final Container				cp;
		final JPanel				p;
		final GridBagLayout			lay		= new GridBagLayout();
		final GridBagConstraints	con		= new GridBagConstraints();
		final JToolBar				tb;
		final JToggleButton			ggPlay;
		
		win = new JFrame( "Moto Rev Ctrl" );
		win.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		win.addWindowListener( new WindowAdapter() {
			public void windowClosed( WindowEvent e )
			{
				if( server != null ) {
					try {
						if( server.didWeBootTheServer() ) server.quitAndWait();
						else if( synth != null ) synth.free();
					}
					catch( IOException e1 ) {
						System.err.println( e1 );
					}
					synth	= null;
				}
				System.exit( 0 );
			}
		});
		
		cp = win.getContentPane();

		p	= new JPanel( lay );
		p.setBorder( BorderFactory.createEmptyBorder( 4, 4, 4, 4 ));
		ezs	= new EZSlider[ ctrlNames.length ];
		con.gridwidth	= GridBagConstraints.REMAINDER;
		con.weightx		= 1.0;
		con.fill		= GridBagConstraints.HORIZONTAL;
		con.insets		= new Insets( 2, 2, 2, 2 );
		for( int i = 0; i < ctrlNames.length; i++ ) {
			ezs[ i ] = new EZSlider();
			ezs[ i ].setControlSpec( ctrlSpecs[ i ]);
			ezs[ i ].setLabel( ctrlNames[ i ]);
			ezs[ i ].setValue( ctrlSpecs[ i ].getDefaultVal() );
			lay.setConstraints( ezs[ i ], con );
			p.add( ezs[ i ]);
			new ControlListener( ezs[ i ]);
		}
		
		tb = new JToolBar();
		tb.setBorderPainted( false );
		tb.setFloatable( false );
		ggPlay = new JToggleButton( ">" );
		ggPlay.setFont( new Font( "SansSerif", Font.PLAIN, 36 ));
		tb.add( ggPlay );
		ggPlay.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				if( server != null ) {
					if( synth != null ) {
						try {
							synth.free();
						}
						catch( IOException e1 ) {
							reportError( e1 );
						}
						synth = null;
					}
					if( ggPlay.isSelected() ) {
						createSynth();
					}
				}
			}
		});
		
		cp.add( p, BorderLayout.CENTER );
		cp.add( tb, BorderLayout.WEST );
		
		EZSlider.align( ezs );
		
		win.pack();
		win.setLocationRelativeTo( null );	// center on screen
		win.setVisible( true );
	}
	
	protected void createSynth()
	{
		final Control		lagCtrl;
		final GraphElem		out, pulseModFreq, pulseFreqMin, pulseFreqMax, pulseWidth, lowPassFreq, amp;
		final GraphElem		pulseFreqMul, pulseFreqAdd;
		final GraphElem		pulseFreq, pulse, filter, clip, ugenGraph;
		final SynthDef		def;
		final String		defName;
		final OSCMessage	newMsg;
		final float[]		ctrlDefaults;
		final float[]		ctrlValues;
		
		try {
			defName			= "MotoRevCtrl";
			ctrlValues		= new float[ ezs.length ];
			for( int i = 0; i < ctrlValues.length; i++ ) {
				ctrlValues[ i ] = (float) ezs[ i ].getValue();
			}
			synth			= Synth.basicNew( defName, server );
			newMsg			= synth.newMsg( server.asTarget(), ctrlNames, ctrlValues );

			if( !defSent ) {
				ctrlDefaults	= new float[ ctrlSpecs.length ];
				for( int i = 0; i < ctrlDefaults.length; i++ ) {
					ctrlDefaults[ i ] = (float) ctrlSpecs[ i ].getDefaultVal();
				}
				out				= Control.kr( "out", 0f );
				lagCtrl			= LagControl.kr( ctrlNames, ctrlDefaults, ctrlLags );
				pulseModFreq	= lagCtrl.getOutput( 0 );
				pulseFreqMin	= UGen.kr( "min", lagCtrl.getOutput( 1 ), lagCtrl.getOutput( 2 ));
				pulseFreqMax	= UGen.kr( "max", lagCtrl.getOutput( 1 ), lagCtrl.getOutput( 2 ));
				pulseWidth		= lagCtrl.getOutput( 3 );
				lowPassFreq		= lagCtrl.getOutput( 4 );
				amp				= lagCtrl.getOutput( 5 );
				pulseFreqMul	= UGen.kr( "*", UGen.kr( "-", pulseFreqMax, pulseFreqMin ), UGen.ir( 0.5f ));
				pulseFreqAdd	= UGen.kr( "+", pulseFreqMin, pulseFreqMul );
				
				pulseFreq		= UGen.kr( "MulAdd", UGen.kr( "SinOsc", pulseModFreq ), pulseFreqMul, pulseFreqAdd );
				pulse			= UGen.ar( "LFPulse", pulseFreq, UGen.ir( 0.1f ), pulseWidth );
				filter			= UGen.ar( "RLPF", pulse, lowPassFreq, UGen.ir( 0.1f ));
				clip			= UGen.ar( "clip2", filter, amp );
				ugenGraph		= UGen.ar( "Out", out, clip );
				
				def				= new SynthDef( defName, ugenGraph );
				def.send( server, newMsg );
				defSent			= true;
				
			} else {
				server.sendMsg( newMsg );
			}
		}
		catch( IOException e1 ) {
			reportError( e1 );
		}
    }

	private void initServer()
	throws IOException
	{
		if( !server.didWeBootTheServer() ) {
			server.initTree();
		}
		defSent = false;
	}
	
	protected static void reportError( Exception e ) {
		System.err.println( e.getClass().getName() + " : " + e.getLocalizedMessage() );
	}

// ------------- ServerListener interface -------------

	public void serverAction( ServerEvent e )
	{
		switch( e.getID() ) {
		case ServerEvent.RUNNING:
			try {
				initServer();
// NodeWatcher nw = NodeWatcher.newFor( 
			}
			catch( IOException e1 ) {
				reportError( e1 );
			}
			break;
		
		case ServerEvent.STOPPED:
			synth = null;
			// have to call startAliveThread to keep watching for server starts
			final javax.swing.Timer t = new javax.swing.Timer( 1000, new ActionListener() {
				public void actionPerformed( ActionEvent e )
				{
					try {
						if( server != null ) server.startAliveThread();
					}
					catch( IOException e1 ) {
						reportError( e1 );
					}
				}
			});
			t.setRepeats( false );
			t.start();
			break;
		
		default:
			break;
		}
	}
	
// ------------- internal classes -------------
				 
	private class ControlListener
	implements ActionListener
	{
		private final EZSlider ez;
	
		protected ControlListener( EZSlider ez )
		{
			this.ez = ez;
			ez.addActionListener( this );
		}
		
		public void actionPerformed( ActionEvent e )
		{
			if( synth != null ) {
				try {
					synth.set( ez.getLabel(), (float) ez.getValue() );
				}
				catch( IOException e1 ) {
					reportError( e1 );
				}
			}
		}
	}
}