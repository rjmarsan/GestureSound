/*
 *  NumberField.java
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
 *	contact@sciss.de
 *
 *
 *  Changelog:
 *		29-Jul-06	copied from de.sciss.gui.NumberField ; and TimeFormat is stripped off
 */

//package de.sciss.gui;
package de.sciss.jcollider.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;

/**
 *  A NumberField is basically a <code>JPanel</code>
 *  holding a <code>JTextField</code> whose content
 *  is limited to decimal numbers. The
 *  idea is somewhat similar to FScape's
 *  <code>ParamField</code>, but we try to avoid the
 *  conceptual drawbacks made there.
 *  <p>
 *  Number formatting is accomplished by using
 *  a <code>NumberFormat</code> object whose configuration
 *  is determinated by a <code>NumberSpace</code> given
 *  to the constructor.
 *  <p>
 *  Clients can listen to user edits by registering
 *  a <code>NumberListener</code>.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 19-Mar-08
 */
public class NumberField
extends JFormattedTextField
implements EventManager.Processor //, PropertyChangeListener
{
	private static final double LN10	= Math.log( 10 );

	/**
	 *  Constructor flag : Format the values
	 *  as minutes:seconds
	 */
	public static final int HHMMSS			=	0x20000;		// display as HH:MM:SS.xxx

//	/**
//	 *  Constructor flag : Pressing enter
//	 *	unfocusses the gadget
//	 */
//	public static final int ENTER_UNFOCUS	=	0x00001;

	private NumberSpace						space;
	protected Number						value;
	private NumberFormat					numberFormat;
// JJJ
//	private TimeFormat						timeFormat;
	private int								flags;

	private EventManager					em				= null;	// lazy creation

	private final DefaultFormatterFactory	factory			= new DefaultFormatterFactory();
	private final NumberFormatter			numberFormatter	= new NumberFormatter();
// JJJ
//	private final TimeFormatter				timeFormatter	= new TimeFormatter();
	
	protected final NumberField				enc_this		= this;
	private final AbstractAction			actionLooseFocus;

	protected static final DataFlavor		numberFlavor	= new DataFlavor( Number.class, Number.class.getName() );
	protected static final DataFlavor[]		supportedFlavors= { numberFlavor, DataFlavor.stringFlavor };

	/**
	 *  Create a new <code>NumberField</code> for
	 *  a given space. the initial value of the 
	 *  <code>NumberField</code> is taken
	 *  from <code>space.reset</code>. The number of
	 *  displayed integers and decimals is calculated 
	 *  by evaluating the space's <code>min</code>,
	 *  <code>max</code> and <code>quant</code>
	 *  fields. If the <code>quant</code> field is integer, no
	 *  decimals are displayed. User adjustments of
	 *  the number are automatically trimmed to the
	 *  space's <code>min</code> and <code>max</code> and
	 *  quantisized to its <code>quant</code> field
	 */
	public NumberField()
	{
		super();

		final ActionMap amap	= getActionMap();
		final InputMap	imap	= getInputMap();
		final int		msh		= Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		Action			action;
		Object			key;

		setFormatterFactory( factory );
		setHorizontalAlignment( RIGHT );

		this.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				final Number newVal = (Number) getValue();
				if( newVal.equals( value )) {
					value = newVal;
					fireNumberChanged();
				}
			}
		});

		this.addPropertyChangeListener( "value", new PropertyChangeListener() {
			public void propertyChange( PropertyChangeEvent e )
			{
				final Number newVal = (Number) getValue();
				if( !newVal.equals( value )) {
					value = newVal;
					fireNumberChanged();
				}
			}
		});
		
		actionLooseFocus	= new ActionLooseFocus();
		key					= "lost";
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), key );
		amap.put( key, actionLooseFocus );
		
// we don't need cut
//		action				= TransferHandler.getCutAction();
//		key					= action.getValue( Action.NAME );
//		// action.getValue( Action.ACCELERATOR_KEY ) returns null unfortunately
//		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_X, msh ), key );
//		amap.put( key, action );

		action				= TransferHandler.getCopyAction();
		key					= action.getValue( Action.NAME );
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_C, msh ), key );
		amap.put( key, action );

		action				= TransferHandler.getPasteAction();
		key					= action.getValue( Action.NAME );
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_V, msh ), key );
		amap.put( key, action );

		setTransferHandler( new NumberTransferHandler() );
	}
	
	
	public NumberField( NumberSpace space )
	{
		this();
		setSpace( space );
	}
	
	public void setSpace( NumberSpace space )
	{
		if( !space.equals( this.space )) {
			this.space	= space;
			updateFormatter();
		}
	}
	
	public void setFlags( int newFlags )
	{
//System.err.println(" old : "+this.flags+"; new : "+newFlags+" ; XOR "+(this.flags ^ newFlags) );
		final int change = this.flags ^ newFlags;
	
		this.flags = newFlags;
	
		if( (change & HHMMSS) != 0 ) {
			updateFormatter();
		}
//		if( (change & ENTER_UNFOCUS) != 0 ) {
//			final InputMap	imap	= getInputMap();
//			
//			imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ),
//				(newFlags & ENTER_UNFOCUS) == 0 ? null : "lost" );
//		}
	}
	
	public int getFlags()
	{
		return flags;
	}
	
	private void updateFormatter()
	{
		double  d;
		int		i;

		if( space.isInteger() ) {
			numberFormat	= NumberFormat.getIntegerInstance( Locale.US );
		} else {
			numberFormat	= NumberFormat.getInstance( Locale.US );
			i				= Math.min( 8, space.maxFracDigits );
			numberFormat.setMinimumFractionDigits( space.minFracDigits );
			numberFormat.setMaximumFractionDigits( i );
		}
// JJJ
//		if( (flags & HHMMSS) != 0 ) {
//			timeFormat  = new TimeFormat( 0, null, null, numberFormat.getMaximumFractionDigits(), Locale.US );
//			numberFormat.setMinimumIntegerDigits( 2 );
//			numberFormat.setMaximumIntegerDigits( 2 );
//			numberFormat.setMinimumFractionDigits( numberFormat.getMaximumFractionDigits() );
//			i			= 5;
//		} else {
//			timeFormat  = null;
			if( Double.isInfinite( space.min ) || Double.isInfinite( space.max )) {
				numberFormat.setMaximumIntegerDigits( 8 );
			} else {
				d   = Math.max( 1.0, Math.floor( Math.abs( space.min )) + 0.1 );	// deal with rounding errors
				i	= (int) (Math.log( d ) / LN10) + 1;
				d   = Math.max( 1.0, Math.floor( Math.abs( space.max )) + 0.1 );
				i	= Math.max( i, (int) (Math.log( d ) / LN10) + 1 );
				numberFormat.setMaximumIntegerDigits( i );
			}
			i = 1;
// JJJ
//		}
		i += Math.min( 4, numberFormat.getMaximumFractionDigits() ) + numberFormat.getMaximumIntegerDigits();

		setColumns( i );

		if( value == null ) {
			value	= space.isInteger() ? (Number) new Long( (long) (space.reset + 0.5) ) :
										  (Number) new Double( space.reset );
		}
		numberFormat.setGroupingUsed( false );

// JJJ
//		if( (flags & HHMMSS) == 0 ) {
			numberFormatter.setFormat( numberFormat );
			if( space.isInteger() ) {
				numberFormatter.setValueClass( Long.class );
				if( !Double.isInfinite( space.min )) {
					numberFormatter.setMinimum( new Long( (long) space.min ));
				}
				if( !Double.isInfinite( space.max )) {
					numberFormatter.setMaximum( new Long( (long) space.max ));
				}
			} else {
				numberFormatter.setValueClass( Double.class );
				if( !Double.isInfinite( space.min )) {
					numberFormatter.setMinimum( new Double( space.min ));
				}
				if( !Double.isInfinite( space.max )) {
					numberFormatter.setMaximum( new Double( space.max ));
				}
			}

			factory.setDefaultFormatter( numberFormatter );

// JJJ
//		} else {
//			// XXX fehlt setMin/Max
//			timeFormatter.setFormat( timeFormat );
//			factory.setDefaultFormatter( timeFormatter );
//		}
		
		setValue( value );
	}

	/**
	 *  Return the number currently displayed
	 *
	 *  @return		the number displayed in the gadget.
	 *				if the <code>NumberSpace</space> is
	 *				integer, a <code>Long</code> is returned,
	 *				otherwise a <code>Double</code>.
	 *
	 *  @see		de.sciss.jcollider.gui.NumberSpace#isInteger()
	 *  @warning	if the number was set using the <code>setNumber</code>
	 *				method, it is possible that the returned object
	 *				is neither <code>Long</code> nor <code>Double</code>.
	 *				Thus never cast it to a subclass of Number, but rather
	 *				use an appropriate translation like <code>intValue()</code>
	 *				or <code>doubleValue()</code>!
	 */
	public Number getNumber()
	{
		return value;
	}

	/**
	 *  Set the gadget contents
	 *  to a new number. No event
	 *  is fired. Though the number is formatted
	 *  according to the space's settings
	 *  (e.g. number of decimals), its value
	 *  is not altered, even if it exceeds the
	 *  space's bounds.
	 *
	 *  @param  value   the new number to display
	 */
	public void setNumber( Number value )
	{
		this.value  = value;
		setValue( value );
	}

	/**
	 *  Returns the used number space.
	 *
	 *  @return		the <code>NumberSpace</code> that was used
	 *				to construct the <code>NumberField</code>.
	 */
	public NumberSpace getSpace()
	{
		return space;
	}

	// --- listener registration ---
	
	/**
	 *  Register a <code>NumberListener</code>
	 *  which will be informed about changes of
	 *  the gadgets content.
	 *
	 *  @param  listener	the <code>NumberListener</code> to register
	 */
	public void addListener( NumberListener listener )
	{
		synchronized( this ) {
			if( em == null ) {
				em = new EventManager( this );
			}
			em.addListener( listener );
		}
	}

	/**
	 *  Unregister a <code>NumberListener</code>
	 *  from receiving number change events.
	 *
	 *  @param  listener	the <code>NumberListener</code> to unregister
	 */
	public void removeListener( NumberListener listener )
	{
		if( em != null ) em.removeListener( listener );
	}

	public void processEvent( BasicEvent e )
	{
		NumberListener listener;
		
		for( int i = 0; i < em.countListeners(); i++ ) {
			listener = (NumberListener) em.getListener( i );
			switch( e.getID() ) {
			case NumberEvent.CHANGED:
				listener.numberChanged( (NumberEvent) e );
				break;
			default:
				assert false : e.getID();
			}
		} // for( i = 0; i < em.countListeners(); i++ )
	}

	protected void fireNumberChanged()
	{
		if( em != null ) {
			em.dispatchEvent( new NumberEvent( this, NumberEvent.CHANGED,
				System.currentTimeMillis(), value, false ));
		}
	}

// ----------- internal action classes -----------
	
	private class ActionLooseFocus
	extends AbstractAction
	{
		protected ActionLooseFocus() { /* empty */ }
		
		public void actionPerformed( ActionEvent e )
		{
			final JRootPane rp = SwingUtilities.getRootPane( enc_this );
			if( rp != null ) rp.requestFocus();
		}
	}

// ----------- internal TransferHandler -----------

	private class NumberTransferHandler
	extends TransferHandler
	{
		protected NumberTransferHandler() { /* empty */ }

		/**
		 * Overridden to import a Number or String if it is available.
		 */
		public boolean importData( JComponent c, Transferable t )
		{
			Number	n	= null;
		
			try {
				if( t.isDataFlavorSupported( numberFlavor )) {
					n = (Number) t.getTransferData( numberFlavor );
				} else if( t.isDataFlavorSupported( DataFlavor.stringFlavor )) {
					n = (Number) getFormatter().stringToValue( (String)
						t.getTransferData( DataFlavor.stringFlavor ));
				}
				if( n != null ) {
					setNumber( n );
					fireNumberChanged();
					return true;
				}
			}
			catch( UnsupportedFlavorException e1 ) { /* ignored */ }
			catch( IOException e2 ) { /* ignored */ }
			catch( ParseException e3 ) { /* ignored */ }

			return false;
		}
		
		public int getSourceActions( JComponent c )
		{
			return COPY;
		}
		
		protected Transferable createTransferable( JComponent c )
		{
			return new NumberTransferable( getNumber(), getFormatter() );
		}
		
//		protected void exportDone( JComponent source, Transferable data, int action )
//		{
//		}

		public boolean canImport( JComponent c, DataFlavor[] flavors )
		{
			for( int i = 0; i < flavors.length; i++ ) {
				for( int j = 0; j < supportedFlavors.length; j++ ) {
					if( flavors[i].equals( supportedFlavors[j] )) return true;
				}
			}
			return false;
		}
	} // class NumberTransferHandler

	private static class NumberTransferable
	implements Transferable
	{
		private final Number n;
		private final JFormattedTextField.AbstractFormatter f;
		
		protected NumberTransferable( Number n, JFormattedTextField.AbstractFormatter f )
		{
			this.n	= n;
			this.f	= f;
		}
		
		public DataFlavor[] getTransferDataFlavors()
		{
			return supportedFlavors;
		}
		
		public boolean isDataFlavorSupported( DataFlavor flavor )
		{
			for( int i = 0; i < supportedFlavors.length; i++ ) {
				if( supportedFlavors[ i ].equals( flavor )) return true;
			}
			return false;
		}
		
		public Object getTransferData( DataFlavor flavor )
		throws UnsupportedFlavorException, IOException
		{
			if( n == null ) throw new IOException();
			if( flavor.equals( numberFlavor )) {
				return n;
			} else if( flavor.equals( DataFlavor.stringFlavor )) {
				try {
					return f.valueToString( n );
				}
				catch( ParseException e1 ) {
					throw new IOException( e1.getLocalizedMessage() );
				}
			} else {
				throw new UnsupportedFlavorException( flavor );
			}
		}
	} // class NumberTransferable
} // class NumberField