/*
 *  SynthDefDiagram.java
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
 *		30-Jun-05	created
 */

package de.sciss.jcollider.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.sciss.jcollider.Constant;
import de.sciss.jcollider.Constants;
import de.sciss.jcollider.SynthDef;
import de.sciss.jcollider.UGen;
import de.sciss.jcollider.UGenChannel;
import de.sciss.jcollider.UGenInfo;
import de.sciss.jcollider.UGenInput;

/**
 *	An experimental view of a SynthDef's graph.
 *	This still lacks intelligent code to
 *	bring a form into the wires, but for
 *	small ugens and for debugging it's quite useful.
 *	<p>
 *	Colours are as follows:
 *	<ul>
 *	<li>red : audio rate</li>
 *	<li>blue : control rate</li>
 *	<li>green : demand rate</li>
 *	<li>grey : scalars</li>
 *	</ul>
 *	<p>
 *	The view can be magnified and boxes can
 *	be dragged around. Double clicking on a
 *	UGen will reveal the names of its inlets.
 *
 *	@version	0.32, 25-Feb-08
 *	@author		Hanns Holger Rutz
 */
public class SynthDefDiagram
extends JFrame
{
	private final Font		fntGUI	= new Font( "SansSerif", Font.PLAIN, 10 );

	private static final String[]	ZOOMS	= {
		"800%", "400%", "200%", "150%", "125%", "100%", "75%", "50%", "25%", "12.5%"
	};
	
	protected static final NumberFormat frmtZoom	= NumberFormat.getPercentInstance( Locale.US );

	/**
	 *	Creates a new frame displaying
	 *	the diagram of the provided SynthDef.
	 *	The frame is automatically made visible.
	 *
	 *	@param	def	the synth def to illustrate
	 */
	public SynthDefDiagram( SynthDef def )
	{
		super( "SynthDef(\"" + def.getName() + "\")" );

		final Container		cp				= getContentPane();
//		frame.getRootPane().setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ));
		final SynthDefView	synthDefView	= new SynthDefView( def );
		final JScrollPane	scroll			= new JScrollPane( synthDefView );
		final Box			box				= Box.createHorizontalBox();
		final JComboBox		ggZoom			= new JComboBox();

		for( int i = 0; i < ZOOMS.length; i++ ) {
			ggZoom.addItem( ZOOMS[ i ]);
		}
		ggZoom.setSelectedIndex( 5 );
		ggZoom.setEditable( true );
		ggZoom.setFont( fntGUI );
		ggZoom.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				final String	text	= ggZoom.getSelectedItem().toString();
				Number			num		= null;
				try {
					num = frmtZoom.parse( text );
				}
				catch( ParseException e1 ) { /* ignored */ }

				if( num != null ) {
					synthDefView.setZoom( num.doubleValue() );
				} else {
					ggZoom.setSelectedItem( frmtZoom.format( synthDefView.getZoom() ));
				}
			}
		});

		box.add( ggZoom );
		box.add( Box.createHorizontalGlue() );

		cp.setLayout( new BorderLayout() );
		cp.add( scroll, BorderLayout.CENTER );
		cp.add( box, BorderLayout.SOUTH );

		setSize( 320, 320 );
		setVisible( true );
		toFront();
	}
	
	protected static String formatConst( float value )
	{
		if( value == Math.round( value )) return String.valueOf( Math.round( value ));
		return frmtConst.format( new Float( value ));
	}

	protected static final Font fntUGen			= new Font( "Lucida Grande", Font.PLAIN, 10 );	// XXX bad for non-macos
	protected static final Font fntToolTip		= new Font( "Gill Sans", Font.ITALIC, 12 );	// XXX bad for non-macos
	private static final NumberFormat frmtConst = NumberFormat.getInstance( Locale.US );

	static {
		frmtConst.setGroupingUsed( false );
		frmtConst.setMaximumFractionDigits( 4 );
	}

	private class SynthDefView
//	extends JComponent
	extends JPanel
	implements MouseListener, MouseMotionListener
	{
		private static final double	HPAD		= 12.0;
		private static final double	VPAD		= 12.0;
		private static final int	MAX_WIDTH	= 640;	// linebreak after exceeding this width
			
		private final List				collUGenViews		= new ArrayList();
		private final List				collSelectedViews	= new ArrayList();
		private final List				collWires			= new ArrayList();
		private final Map				mapUGensToViews		= new HashMap();
		
		private boolean recalc = true;
		
		private final SynthDef	def;
		
		private Point	dragStartPtScreen	= null;
		private Point2D	dragStartPt			= null;
		private Point2D	dragCurrentPt		= null;
		private boolean	dragStarted			= false;
		
		private double zoom					= 1.0;
		
		private Rectangle2D	boundingBox		= new Rectangle2D.Double();
		
		protected SynthDefView( SynthDef def )
		{
			super();
		
			this.def	= def;
			
			this.setFont( fntUGen );
//			this.setBackground( null );
//			this.setOpaque( false );
			this.addMouseListener( this );
			this.addMouseMotionListener( this );
			this.setFocusable( true );
		}
		
		private void createBoxes( Graphics2D g2, FontMetrics fm, FontMetrics fm2 )
		{
			final List	children	= def.getUGens();
			final List	verbaut		= new ArrayList();
			final List	neuVerbaut	= new ArrayList();
			final List	constRects	= new ArrayList();
			final List	ugenRects	= new ArrayList();

			final double	h	= fm.getHeight() + VPAD;
			
			double		y = 0;
			double		x, w, hpadMax, constW, incY;
			int			cons;
			Rectangle2D	rect, rect2;
			Point2D		pt;
			UGen		ugen;
			UGenView	uv, uv2;
			UGenInput	inp;
			UGenInfo	ui;
			Wire		wire;
			String		name;
		
			collUGenViews.clear();
			mapUGensToViews.clear();
			collWires.clear();
		
//			ugenRects.add( new Rectangle( 0, -10, 65536, 10 ));	// simulates top window border
		
			do {
				x		= 0;
childLp:		for( int i = 0; i < children.size(); i++ ) {
					ugen	= (UGen) children.get( i );
					ui		= UGenInfo.infos == null ? null : (UGenInfo) UGenInfo.infos.get( ugen.getName() );
					hpadMax	= HPAD;
					for( int j = 0; j < ugen.getNumInputs(); j++ ) {
						inp = ugen.getInput( j );
						if( inp instanceof UGenChannel ) {
							if( !verbaut.contains( ((UGenChannel) inp).getUGen() )) continue childLp;
						} else if( inp instanceof Constant ) {
							constW	= 2 + fm.getStringBounds( formatConst( ((Constant) inp).getValue() ), g2 ).getWidth();
							hpadMax = Math.max( hpadMax, constW );
						}
					}
					// all right, all inputs ready
					children.remove( i-- );
					name	= ui == null ? ugen.getName() : ui.getDisplayName( ugen );
					rect	= fm.getStringBounds( name, g2 );
					cons	= Math.max( ugen.getNumInputs(), ugen.getNumOutputs() );
					w		= Math.max( cons * (2 + hpadMax), rect.getWidth() + HPAD );
					uv		= new UGenView( ugen, ui, name, new Rectangle2D.Double( x, y, w, h ), fm, fm2 );

					collUGenViews.add( uv );
					mapUGensToViews.put( ugen, uv );
					neuVerbaut.add( ugen );
					constRects.addAll( uv.getConstBounds() );
					
					for( int j = 0; j < ugen.getNumInputs(); j++ ) {
						inp = ugen.getInput( j );
						if( inp instanceof UGenChannel ) {
							uv2		= (UGenView) mapUGensToViews.get( ((UGenChannel) inp).getUGen() );
							wire	= new Wire( uv2, ((UGenChannel) inp).getChannel(), uv, j );
							collWires.add( wire );							
						}
					}
					
					x += w + HPAD;
					if( x > MAX_WIDTH ) break childLp;
				}

				// shift line downwards if there are vertical overlappings
				incY = 0.0;
				for( int i = 0; i < constRects.size(); i++ ) {
					rect	= (Rectangle2D) constRects.get( i );
					for( int j = 0; j < ugenRects.size(); j++ ) {
						rect2	= (Rectangle2D) ugenRects.get( j );
//System.err.println( "const "+rect.getX()+","+rect.getY()+","+rect.getWidth()+","+rect.getHeight()+"; ugen "+
//							 rect2.getX()+","+rect2.getY()+","+rect2.getWidth()+","+rect2.getHeight() );
						if( rect.intersects( rect2 )) {
//System.err.println( "intersects. dy = "+ (rect.getY() - (rect2.getY() + rect2.getHeight()) + 4) );
							incY	= Math.max( incY, (rect2.getY() + rect2.getHeight()) - rect.getY() + 6 );
						}
					}
				}

				ugenRects.clear();
				constRects.clear();
				for( int i = 0; i < neuVerbaut.size(); i++ ) {
					uv	= (UGenView) mapUGensToViews.get( neuVerbaut.get( i ));
					if( incY > 0.0 ) {
						pt	= uv.getLocation();
						uv.setLocation( new Point2D.Double( pt.getX(), pt.getY() + incY ));
					}
					ugenRects.add( uv.getContainer() );
				}
				
				verbaut.addAll( neuVerbaut );
				for( int i = 0; i < neuVerbaut.size(); i++ ) {
					uv	= (UGenView) mapUGensToViews.get( neuVerbaut.get( i ));
					ugenRects.add( uv.getContainer() );
				}
				neuVerbaut.clear();

				y += h + VPAD + incY;

			} while( !children.isEmpty() );
			
			recalcBoundingBox();
			recalc = false;
		}
		
		protected double getZoom()
		{
			return zoom;
		}

		protected void setZoom( double newZoom )
		{
			zoom = newZoom;

//			final Dimension oldSize	= getPreferredSize();
			final Dimension newSize	= new Dimension( (int) (boundingBox.getMaxX() * zoom + 4),
													 (int) (boundingBox.getMaxY() * zoom + 4) );

//			if( !oldSize.equals( newSize )) {
				setPreferredSize( newSize );
				revalidate();
//			}
//			repaint();
		}
		
		private void recalcBoundingBox()
		{
			if( collUGenViews.isEmpty() ) return;

			UGenView uv		= (UGenView) collUGenViews.get( 0 );
			boundingBox		= uv.getBoundingBox();
		
			for( int i = 1; i < collUGenViews.size(); i++ ) {
				uv = (UGenView) collUGenViews.get( i );
				Rectangle2D.union( boundingBox, uv.getBoundingBox(), boundingBox );
			}
			
			final Dimension oldSize	= getPreferredSize();
			final Dimension newSize	= new Dimension( (int) (boundingBox.getMaxX() * zoom + 4),
													 (int) (boundingBox.getMaxY() * zoom + 4) );

//System.err.println( "bounding box: "+box.getX()+","+box.getY()+","+box.getWidth()+","+box.getHeight() );
			
			if( (boundingBox.getX() < 4) || (boundingBox.getY() < 4) ) {
				final int	dx = Math.max( 0, (int) (5 - boundingBox.getX()) );
				final int	dy = Math.max( 0, (int) (5 - boundingBox.getY()) );
				Point2D		pt;
				for( int i = 0; i < collUGenViews.size(); i++ ) {
					uv	= (UGenView) collUGenViews.get( i );
					pt	= uv.getLocation();
					uv.setLocation( new Point2D.Double( pt.getX() + dx, pt.getY() + dy ));
				}
//				final Point	viewPt	= viewport.getViewPosition();
//				viewport.setViewPosition( new Point( viewPt.x - dx, viewPt.y - dy ));
			}
			
			if( !oldSize.equals( newSize )) {
//System.err.println( "setting new size "+newSize.getWidth()+","+newSize.getHeight() );
				setPreferredSize( newSize );
				revalidate();
			}
		}
	
		public void paintComponent( Graphics g )
		{
			super.paintComponent( g );
		
			final Graphics2D		g2		= (Graphics2D) g;
			final FontMetrics		fm		= g2.getFontMetrics();
			final FontMetrics		fm2		= g2.getFontMetrics( fntToolTip );
			final AffineTransform	atOrig	= g2.getTransform();

			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g2.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );

			g2.scale( zoom, zoom );
			
			if( recalc ) {
				createBoxes( g2, fm, fm2 );
			}
			
			UGenView	uv;
			Wire		wire;
			
			for( int i = 0; i < collUGenViews.size(); i++ ) {
				uv = (UGenView) collUGenViews.get( i );
				uv.paint( g2, fm, false );
			}

			for( int i = 0; i < collWires.size(); i++ ) {
				wire = (Wire) collWires.get( i );
				wire.paint( g2 );
			}
			
			g2.setFont( fntToolTip );
			for( int i = 0; i < collUGenViews.size(); i++ ) {
				uv = (UGenView) collUGenViews.get( i );
				if( uv.isShowingToolTips() ) uv.paintToolTips( g2, fm2 );
			}
			g2.setFont( fntUGen );

			if( dragStarted ) {
				g2.translate( dragCurrentPt.getX() - dragStartPt.getX(), dragCurrentPt.getY() - dragStartPt.getY() );
				for( int i = 0; i < collSelectedViews.size(); i++ ) {
					uv = (UGenView) collSelectedViews.get( i );
					uv.paint( g2, fm, true );
				}
			}

			g2.setTransform( atOrig );
		}
		
		private Point2D getVirtualPoint( Point screenPt )
		{
			return new Point2D.Double( screenPt.x / zoom, screenPt.y / zoom );
		}
		
		public void mousePressed( MouseEvent e )
		{
			requestFocus();
		
			final Point2D	mousePt		= getVirtualPoint( e.getPoint() );
		
			UGenView	hitView	= null;
			boolean		repaint	= false;
			UGenView	uv;
		
			for( int i = 0; i < collUGenViews.size(); i++ ) {
				uv = (UGenView) collUGenViews.get( i );
				if( uv.contains( mousePt )) {
					hitView = uv;
					break;
				}
			}
			
			if( (!e.isShiftDown() && !collSelectedViews.isEmpty()) &&
				((hitView == null) || !hitView.isSelected()) ) {		// deselect all
				
				for( int i = 0; i < collSelectedViews.size(); i++ ) {
					uv = (UGenView) collSelectedViews.get( i );
					uv.setSelected( false );
				}
				collSelectedViews.clear();
				repaint	= true;
			}
			if( hitView != null ) {
				if( e.getClickCount() == 2 ) {
					hitView.showToolTips( !hitView.isShowingToolTips() );
					repaint = true;
				}
				if( e.isShiftDown() ) {
					if( hitView.isSelected() ) {
						hitView.setSelected( false );
						collSelectedViews.remove( hitView );
					} else {
						hitView.setSelected( true );
						collSelectedViews.add( hitView );
					}
					repaint = true;
				} else {
					if( !hitView.isSelected() ) {
						hitView.setSelected( true );
						collSelectedViews.add( hitView );
						repaint = true;
					}
				}

			} else {
				if( e.getClickCount() == 2 ) {
					for( int i = 0; i < collUGenViews.size(); i++ ) {
						uv = (UGenView) collUGenViews.get( i );
						uv.showToolTips( false );
					}
					repaint	= true;
				}
			}
			
			if( !collSelectedViews.isEmpty() ) {
				dragStartPtScreen	= e.getPoint();
				dragStartPt			= mousePt;
				dragStarted			= false;
			}

			if( repaint ) repaint();
		}

		public void mouseReleased( MouseEvent e )
		{
			UGenView		uv;
			Point2D			pt;
			final double	dx, dy;

			if( dragStarted ) {
				dragCurrentPt	= getVirtualPoint( e.getPoint() );
				dx				= dragCurrentPt.getX() - dragStartPt.getX();
				dy				= dragCurrentPt.getY() - dragStartPt.getY();
				for( int i = 0; i < collSelectedViews.size(); i++ ) {
					uv	= (UGenView) collSelectedViews.get( i );
					pt	= uv.getLocation();
					uv.setLocation( new Point2D.Double( pt.getX() + dx, pt.getY() + dy ));
				}
				recalcBoundingBox();
				dragStarted	= false;
				repaint();
			}
			dragStartPt	= null;
		}

		public void mouseDragged( MouseEvent e )
		{
			if( dragStartPt != null ) {
				if( !dragStarted ) {
					if( (Math.abs( e.getX() - dragStartPtScreen.getX() ) > 2) ||
						(Math.abs( e.getY() - dragStartPtScreen.getY() ) > 2) ) {
					
						dragStarted = true;
					} else return;
				}
				dragCurrentPt	= getVirtualPoint( e.getPoint() );
				repaint();
			}
		}

		public void mouseClicked( MouseEvent e ) { /* ignored */ }
		public void mouseMoved( MouseEvent e ) { /* ignored */ }
		public void mouseEntered( MouseEvent e ) { /* ignored */ }
		public void mouseExited( MouseEvent e ) { /* ignored */ }
	}
	
	private static class Wire
	implements Constants
	{
		private static final Paint	pntScalarWire	= new Color( 0x60, 0x60, 0x60 );
		private static final Paint	pntControlWire	= new Color( 0x60, 0x60, 0xC0 );
		private static final Paint	pntAudioWire	= new Color( 0xC0, 0x60, 0x60 );
		private static final Paint	pntDemandWire	= new Color( 0x40, 0x80, 0x40 );

		private final Paint		pntWire;
		private Shape			shpWire;
		private final Stroke	strkWire	= new BasicStroke( 2.0f );
		
		private final UGenView	outputUGen, inputUGen;
		private final int		outputIndex, inputIndex;
	
		protected Wire( UGenView outputUGen, int outputIndex, UGenView inputUGen, int inputIndex )
		{
			this.outputUGen		= outputUGen;
			this.inputUGen		= inputUGen;
			this.outputIndex	= outputIndex;
			this.inputIndex		= inputIndex;
		
			final Object rate = outputUGen.getUGen().getOutputRate( outputIndex );

			if( rate == kScalarRate ) {
				pntWire	= pntScalarWire;
			} else if( rate == kControlRate ) {
				pntWire	= pntControlWire;
			} else if( rate == kAudioRate ) {
				pntWire	= pntAudioWire;
			} else if( rate == kDemandRate ) {
				pntWire	= pntDemandWire;
			} else {
				System.err.println( "Illegal rate : " + rate );
				pntWire	= pntScalarWire;
			}
			
			outputUGen.addOutletWire( this );
			inputUGen.addInletWire( this );
						
			recalcPositions();
		}
		
		protected void recalcPositions()
		{
			shpWire	= new Line2D.Double( outputUGen.getOutletLocation( outputIndex ),
										 inputUGen.getInletLocation( inputIndex ));
		}

		protected void paint( Graphics2D g2 )
		{
			final Stroke			strkOrig	= g2.getStroke();
//			final AffineTransform	atOrig		= g2.getTransform();
//			
//			g2.translate( -1, -1 );	// account for symmetric stroke width
			
			g2.setPaint( pntWire );
			g2.setStroke( strkWire );
			g2.draw( shpWire );
			
			g2.setStroke( strkOrig );
//			g2.setTransform( atOrig );
		}
	}

	private static class UGenView
	implements Constants
	{
		private final UGen				ugen;
//		private final UGenInfo			ui;
//		private final String			name;
		private final Shape				shpContainer;
		private final Shape				shpCons;
		private final Shape				shpConsts;
		private final Shape				shpToolTips;
		private final List				collConsts		= new ArrayList();	// elements = PositionedString
		private final List				collConstBounds	= new ArrayList();	// elements = Rectangle2D
		private final List				collToolTips	= new ArrayList();	// elements = PositionedString
		private final Rectangle2D		bounds;
		
		private static final Stroke	strkCons	= new BasicStroke( 2.0f );
		private static final Paint	pntConst	= new Color( 0x00, 0x00, 0x00, 0xC0 );
		private static final Paint	pntConstD	= new Color( 0x00, 0x00, 0x00, 0x60 );
//		private static final Paint	pntToolTips	= new GradientPaint( 0, 0, new Color( 0xFF, 0xFF, 0x00, 0xA0 ),
//																	 0, 20, new Color( 0xFF, 0xFF, 0x00, 0x00 ));
		private static final Paint	pntToolTips	= new Color( 0xFF, 0xFF, 0x00, 0xB0 );
		private static final Paint	pntToolTipTxt = new Color( 0x00, 0x00, 0x00, 0xD8 );
	
		private final Paint	pntBackground;
		private final Paint	pntBorder;
		private final Paint	pntLabel;
		
		private final Paint	pntBackgroundS;
		private final Paint	pntBorderS;
		private final Paint	pntLabelS;

		private final Paint	pntBackgroundD;
		private final Paint	pntBorderD;
		private final Paint	pntLabelD;
		
		private final PositionedString	label;
		
		private final Point2D[]	inletLocations, outletLocations;
		
		private final List	inletWires	= new ArrayList(); // elements = Wire
		private final List	outletWires	= new ArrayList(); // elements = Wire
		
		private boolean selected = false;
		private boolean toolTips = false;
		
		private static final double minus90 = -Math.PI / 2;

		protected UGenView( UGen ugen, UGenInfo ui, String name,
						   	Rectangle2D bounds, FontMetrics fm, FontMetrics fm2 )
		{
			this.ugen		= ugen;
//			this.ui			= ui;
//			this.name		= name;
			this.bounds		= bounds;
			
			inletLocations	= new Point2D[ ugen.getNumInputs() ];
			outletLocations	= new Point2D[ ugen.getNumOutputs() ];

			final GeneralPath	gp	= new GeneralPath();
			final GeneralPath	gp2 = new GeneralPath();
			final Area			a	= new Area();
			double				x, x2, dx, y, y2, h;
			int					r, g, b;
			UGenInput			inp;
			String				str;
			
			if( ugen.getRate() == kScalarRate ) {
				r	= 0xC0;
				g	= 0xC0;
				b	= 0xC0;
			} else if( ugen.getRate() == kControlRate ) {
				r	= 0x80;
				g	= 0x80;
				b	= 0xC0;
			} else if( ugen.getRate() == kAudioRate ) {
				r	= 0xC0;
				g	= 0x80;
				b	= 0x80;
			} else if( ugen.getRate() == kDemandRate ) {
				r	= 0x80;
				g	= 0xC0;
				b	= 0x80;
			} else {
				System.err.println( "Illegal rate : " + ugen.getRate() );
				r	= 0xC0;
				g	= 0xC0;
				b	= 0xC0;
			}

			pntBackground	= new Color( r, g, b );
			pntBorder		= new Color( r >> 1, g >> 1, b >> 1 );
			pntLabel		= new Color( r >> 2, g >> 2, b >> 2 );

			pntBackgroundS	= new Color( r >> 1, g >> 1, b >> 1 );
			pntBorderS		= new Color( r >> 2, g >> 2, b >> 2 );
			pntLabelS		= Color.white;

			pntBackgroundD	= new Color( r, g, b, 0x80 );
			pntBorderD		= new Color( r >> 1, g >> 1, b >> 1, 0x80 );
			pntLabelD		= new Color( r >> 2, g >> 2, b >> 2, 0x80 );
				
			// inlets
//			x	= SynthDefView.HPAD / 2 + 1 + bounds.getX();
			x	= SynthDefView.HPAD / 2 + 1;
			dx	= (bounds.getWidth() - 2 - SynthDefView.HPAD) / (ugen.getNumInputs() - 1);
//			y	= bounds.getY() + 1.0;
			y	= 1.0;
			h	= SynthDefView.VPAD / 4;
			
			for( int i = 0; i < ugen.getNumInputs(); i++ ) {
				inp = ugen.getInput( i );
				y2	= y;
				if( inp instanceof Constant ) {
					y2 -= 2 + fm.getHeight();
 					str = SynthDefDiagram.formatConst( ((Constant) inp).getValue() );
					collConsts.add( new PositionedString( str, x + 4, y2 + fm.getAscent() ));
					x2	= x + fm.stringWidth( str );
					gp2.moveTo( (float) x2, (float) y2 );
					gp2.lineTo( (float) x, (float) y2 );
					gp2.lineTo( (float) x, (float) y );
					collConstBounds.add( new Rectangle2D.Double( x, y2, x2 + 4 - x, y - y2 ));
				}
				gp.append( new Line2D.Double( x, y, x, y + h ), false );
				inletLocations[ i ] = new Point2D.Double( x, y );
				
				if( ui != null ) {
					str = ui.getArgNameForInput( ugen, i );
					if( str != null ) {
						collToolTips.add( new PositionedString( str, x + fm2.getAscent() - 4, y2 - 4 ));
						x2	= fm2.stringWidth( str ) + 8;
//						a.add( new Area( new Rectangle2D.Double( x - 2, y2 - x2 - 2, fm2.getHeight(), x2 )));
						a.add( new Area( new RoundRectangle2D.Double( x - 2.5, y2 - x2 - 2, fm2.getHeight(), x2, 6, 6 )));
					}
				}
				
				x += dx;
			}

			// outlets
//			x	= SynthDefView.HPAD / 2 + 1 + bounds.getX();
			x	= SynthDefView.HPAD / 2 + 1;
			dx	= (bounds.getWidth() - 2 - SynthDefView.HPAD) / (ugen.getNumOutputs() - 1);
//			y	= bounds.getY() + bounds.getHeight() - 1;
			y	= bounds.getHeight() - 1;
			
			for( int i = 0; i < ugen.getNumOutputs(); i++ ) {
				gp.append( new Line2D.Double( x, y - h, x, y ), false );
				outletLocations[ i ] = new Point2D.Double( x, y );
				x += dx;
			}

			shpCons		= gp;
			shpConsts	= gp2;
			shpToolTips	= a;
//			label		= new PositionedString( ugen.getName(), SynthDefView.HPAD / 2 + bounds.getX(),
//																SynthDefView.VPAD / 2 + bounds.getY() + fm.getAscent() );
			label		= new PositionedString( name, SynthDefView.HPAD / 2,
													  SynthDefView.VPAD / 2 + fm.getAscent() );
			
			shpContainer = new Rectangle2D.Double( 0.0, 0.0, bounds.getWidth(), bounds.getHeight() );
			
//			System.err.println( "# consts : " +collConsts.size() );
		}
		
		protected void setSelected( boolean selected )
		{
			this.selected = selected;
		}

		protected boolean isSelected()
		{
			return selected;
		}
		
		protected void showToolTips( boolean yesNo )
		{
			this.toolTips	= yesNo;
		}

		protected boolean isShowingToolTips()
		{
			return toolTips;
		}
		
		protected Point2D getLocation()
		{
			return new Point2D.Double( bounds.getX(), bounds.getY() );
		}
		
		protected Rectangle2D getContainer()
		{
			return bounds.getBounds2D();	// a copy (?)
		}

		protected boolean contains( Point2D pt )
		{
			return bounds.contains( pt );
		}
		
		protected void setLocation( Point2D topLeft )
		{
			bounds.setFrame( topLeft.getX(), topLeft.getY(), bounds.getWidth(), bounds.getHeight() );
			for( int i = 0; i < inletWires.size(); i++ ) {
				((Wire) inletWires.get( i )).recalcPositions();
			}
			for( int i = 0; i < outletWires.size(); i++ ) {
				((Wire) outletWires.get( i )).recalcPositions();
			}
		}
		
		protected List getConstBounds()
		{
			final List result = new ArrayList( collConstBounds.size() );
			
			Rectangle2D rect;
			
			for( int i = 0; i < collConstBounds.size(); i++ ) {
				rect = (Rectangle2D) collConstBounds.get( i );
				result.add( new Rectangle2D.Double( rect.getX() + bounds.getX(), rect.getY() + bounds.getY(),
													rect.getWidth(), rect.getHeight() ));
			}
		
			return result;
		}
		
		protected Rectangle2D getBoundingBox()
		{
			final Rectangle2D		result		= getContainer();
			final List				constBounds = getConstBounds();

			for( int i = 0; i < constBounds.size(); i++ ) {
				Rectangle2D.union( result, (Rectangle2D) constBounds.get( i ), result );
			}

			return result;
		}
		
		protected void addInletWire( Wire wire )
		{
			inletWires.add( wire );
		}

		protected void addOutletWire( Wire wire )
		{
			outletWires.add( wire );
		}
		
		protected UGen getUGen()
		{
			return ugen;
		}

		protected Point2D getInletLocation( int index )
		{
			return new Point2D.Double( inletLocations[ index ].getX() + bounds.getX(),
									   inletLocations[ index ].getY() + bounds.getY() );
		}

		protected Point2D getOutletLocation( int index )
		{
			return new Point2D.Double( outletLocations[ index ].getX() + bounds.getX(),
									   outletLocations[ index ].getY() + bounds.getY() );
		}
		
		protected void paint( Graphics2D g2, FontMetrics fm, boolean dragging )
		{
			final Stroke			strkOrig	= g2.getStroke();
			final AffineTransform	atOrig		= g2.getTransform();
			PositionedString		pStr;
		
			g2.translate( bounds.getX(), bounds.getY() );
		
			g2.setPaint( dragging ? pntBackgroundD : (selected ? pntBackgroundS : pntBackground) );
			g2.fill( shpContainer );
			g2.setPaint( dragging ? pntBorderD : (selected ? pntBorderS : pntBorder) );
			g2.draw( shpContainer );
			g2.setPaint( dragging ? pntLabelD : (selected ? pntLabelS : pntLabel) );
			g2.drawString( label.str, (float) label.x, (float) label.y );
			
			g2.setPaint( dragging ? pntConstD : pntConst );
			for( int i = 0; i < collConsts.size(); i++ ) {
				pStr	= (PositionedString) collConsts.get( i );
				g2.drawString( pStr.str, (float) pStr.x, (float) pStr.y );
			}
			g2.draw( shpConsts );
			
			g2.setStroke( strkCons );
			g2.draw( shpCons );
			
			g2.setStroke( strkOrig );
			g2.setTransform( atOrig );
		}

		protected void paintToolTips( Graphics2D g2, FontMetrics fm )
		{
			final Stroke			strkOrig	= g2.getStroke();
			final AffineTransform	atOrig		= g2.getTransform();
			PositionedString		pStr;
			final AffineTransform	atRecent;
			
			g2.translate( bounds.getX(), bounds.getY() );
			atRecent = g2.getTransform();

			g2.setPaint( pntToolTips );
			g2.fill( shpToolTips );
			g2.setPaint( pntToolTipTxt );
			for( int i = 0; i < collToolTips.size(); i++ ) {
				pStr	= (PositionedString) collToolTips.get( i );
				g2.translate( pStr.x, pStr.y );
				g2.rotate( minus90 );
				g2.drawString( pStr.str, 0, 0 );
				g2.setTransform( atRecent );
			}

			g2.setStroke( strkOrig );
			g2.setTransform( atOrig );
		}
	}
	
	private static class PositionedString
	{
		protected final String	str;
		protected final double	x, y;
		
		protected PositionedString( String str, double x, double y )
		{
			this.str	= str;
			this.x		= x;
			this.y		= y;
		}
	}
}