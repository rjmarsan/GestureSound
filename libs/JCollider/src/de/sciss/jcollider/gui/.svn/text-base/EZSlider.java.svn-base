/*
 *  EZSlider.java
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
 *		29-Jul-06	created
 */

package de.sciss.jcollider.gui;

import java.awt.AWTEventMulticaster;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *	Something similar to SClang's EZSlider: a combo of
 *	horizontal slider, number box and label.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 25-Feb-08
 */
public class EZSlider
extends JComponent
{
	private final Object		sync			= new Object();
	private ActionListener		actionListener	= null;
	protected ControlSpec		spec;
	
	protected double			value			= 0.0;
	
	private final JLabel		ggLabel;
	protected final JSlider		ggSlider;
	protected final NumberField	ggNumber;
	private final JLabel		ggUnits;
	 
	public EZSlider()
	{
		super();
		
		final GridBagLayout			lay		= new GridBagLayout();
		final GridBagConstraints	con		= new GridBagConstraints();

		setLayout( lay );
//		con.anchor		= GridBagConstraints.WEST;
		con.fill		= GridBagConstraints.HORIZONTAL;
//		con.ipadx		= 2;
		con.insets		= new Insets( 0, 2, 0, 2 );
		
		setLayout( lay );
		ggLabel			= new JLabel( (String) null, SwingConstants.RIGHT );
		con.weightx		= 0.0;
		lay.setConstraints( ggLabel, con );
		add( ggLabel );
		ggSlider	= new JSlider( 0, 0x10000 );
		ggSlider.setMaximumSize( new Dimension( 0x10000, ggSlider.getMaximumSize().height ));
		con.weightx		= 1.0;
		lay.setConstraints( ggSlider, con );
		add( ggSlider );
		ggNumber	= new NumberField();
		con.weightx		= 0.0;
		lay.setConstraints( ggNumber, con );
		add( ggNumber );
		ggUnits		= new JLabel();
//		con.weightx		= 0.0;
		lay.setConstraints( ggUnits, con );
		add( ggUnits );

//setBackground( java.awt.Color.red );
//setBorder( javax.swing.BorderFactory.createMatteBorder( 2, 2, 2, 2, java.awt.Color.blue ));
		
		setControlSpec( ControlSpec.defaultSpec );

//System.err.println( "kieka" );

		ggNumber.addListener( new NumberListener() {
			public void numberChanged( NumberEvent e )
			{
				value	= e.getNumber().doubleValue();
				ggSlider.setValue( (int) (spec.unmap( value ) * 0x10000) );
				dispatchEvent();
			}
		});
		
		ggSlider.addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent e )
			{
				value	= spec.map( (double) ggSlider.getValue() / 0x10000 );
				ggNumber.setNumber( new Double( value ));
				dispatchEvent();
			}
		});
	}
	
	public static void align( EZSlider[] ez )
	{
		int maxLabelWidth	= 0;
		int maxNumberWidth	= 0;
		int maxUnitsWidth	= 0;
		for( int i = 0; i < ez.length; i++ ) {
			maxLabelWidth	= Math.max( maxLabelWidth, ez[ i ].ggLabel.getPreferredSize().width );
			maxNumberWidth	= Math.max( maxNumberWidth, ez[ i ].ggNumber.getPreferredSize().width );
			maxUnitsWidth	= Math.max( maxUnitsWidth, ez[ i ].ggUnits.getPreferredSize().width );
		}
		for( int i = 0; i < ez.length; i++ ) {
			ez[ i ].setLabelWidth( maxLabelWidth );
			ez[ i ].setNumberWidth( maxNumberWidth );
			ez[ i ].setUnitsWidth( maxUnitsWidth );
//			System.err.println( "Aligning ... "+maxLabelWidth+"; "+maxNumberWidth+"; "+maxUnitsWidth );
		}
	}
	
	public void setLabelWidth( int width )
	{
		setComponentWidth( ggLabel, width );
	}
	
	public void setSliderWidth( int width )
	{
		setComponentWidth( ggSlider, width );
	}
	
	public void setNumberWidth( int width )
	{
		setComponentWidth( ggNumber, width );
		ggNumber.setColumns( 0 );
	}
	
	public void setUnitsWidth( int width )
	{
		setComponentWidth( ggUnits, width );
	}
	
	private void setComponentWidth( JComponent c, int width )
	{
		c.setSize( width, c.getHeight() );
		c.setPreferredSize( new Dimension( width, c.getPreferredSize().height ));
		c.setMinimumSize( new Dimension( width, c.getMinimumSize().height ));
		c.setMaximumSize( new Dimension( width, c.getMaximumSize().height ));
	}
	
	public void setLabel( String label )
	{
		ggLabel.setText( label );
	}
	
	public String getLabel()
	{
		return ggLabel.getText();
	}
	
	public void setControlSpec( ControlSpec spec )
	{
		this.spec	= spec;
		ggNumber.setSpace( new NumberSpace( spec.getMinVal(), spec.getMaxVal(), spec.getStep() ));
		ggUnits.setText( spec.getUnits() );
	}

	public void addActionListener( ActionListener l )
	{
		synchronized( sync ) {
			actionListener = AWTEventMulticaster.add( actionListener, l );
		}
	}
	
	public void removeActionListener( ActionListener l )
	{
		synchronized( sync ) {
			actionListener = AWTEventMulticaster.remove( actionListener, l );
		}
     }
	
	protected void dispatchEvent()
	{
		final ActionListener l = actionListener;
		if( l != null ) {
			l.actionPerformed( new ActionEvent( this, ActionEvent.ACTION_PERFORMED, null ));
		}
	}
	
	public double getValue()
	{
		return value;
	}
	
	public double getUnmappedValue()
	{
		return spec.unmap( value );
	}
	
	public void setValue( double value )
	{
		this.value = spec.constrain( value );
		ggSlider.setValue( (int) (spec.unmap( value ) * 0x10000) );
		ggNumber.setNumber( new Double( value ));
	}
	
	public void setUnmappedValue( double value )
	{
		setValue( spec.map( value ));
	}
	
	public void setValueAction( double value )
	{
		setValue( value );
		dispatchEvent();
	}
}