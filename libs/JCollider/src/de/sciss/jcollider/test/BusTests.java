package de.sciss.jcollider.test;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.sciss.jcollider.Bus;
import de.sciss.jcollider.Constants;
import de.sciss.jcollider.Server;

/**
 * 	@version	0.36, 08-Oct-09
 * 	@author		Hanns Holger Rutz
 */
public class BusTests
{
	private Server s = null;
	private final Random rnd = new Random();
	
	public BusTests()
	{
		System.out.println( "Testing bus behaviour.\nAssuming scsynth is" +
		                    "running on udp port 57110...\n" );
				
		try {
			s = new Server( "Test" );
			s.start();
			s.dumpIncomingOSC( Constants.kDumpText );
			s.dumpOutgoingOSC( Constants.kDumpText );
			iter( 0 );
		}
		catch( IOException e1 ) {
			e1.printStackTrace();
		}
	}
	
	protected void test( boolean success, String name )
	{
		if( !success ) throw new AssertionError( name );
	}
	
	protected void test( float val1, float val2, String name )
	{
		if( val1 != val2 ) throw new AssertionError( name + " (" + val1 + " != " + val2 + ")" );
	}
	
	protected void test( int val1, int val2, String name )
	{
		if( val1 != val2 ) throw new AssertionError( name + " (" + val1 + " != " + val2 + ")" );
	}
	
	protected void nextIter( Bus lastBus, final int iter )
	{
		if( iter % 100 == 0 ) {
			System.out.println( "(Not freeing bus)" );
		} else {
			lastBus.free();
		}

		if( iter == 1000 ) {
			System.out.println( "\nAll tests successful!" );
			System.exit( 0 );
		}
		EventQueue.invokeLater( new Runnable() {
			public void run()
			{
				try {
					iter( iter );
				} catch( IOException e1 ) {
					e1.printStackTrace();
				}
			}
		});
	}
	protected void iter( final int iter )
	throws IOException
	{
		final int numCh = rnd.nextInt( 32 ) + 1;
		final Bus b = Bus.control( s, numCh );
		System.out.print( "#" + iter + " test " + b );
		if( iter < 333 ) {
			System.out.println( " : single set / get" );
			final float val1 = rnd.nextFloat();
			b.set( val1 );
			b.get( new Bus.GetCompletionAction() {
				public void completion( Bus bus, float[] values )
				{
					test( values.length, 1, "numChannels" );
					for( int i = 0; i < values.length; i++ ) {
						test( values[ i ], val1, "value at " + i );
					}
					nextIter( b, iter + 1 );
				}
			});
			
		} else if( iter < 667 ) {
			System.out.println( " : multi set / get" );
			final int numVals = rnd.nextInt( b.getNumChannels() ) + 1;
			final float[] vals = new float[ numVals ];
			for( int i = 0; i < numVals; i++ ) {
				vals[ i ] = rnd.nextFloat();
			}
			final List collOff = new ArrayList( b.getNumChannels() );
			for( int i = 0; i < b.getNumChannels(); i++ ) {
				collOff.add( new Integer( i ));
			}
			final int offsets[] = new int[ numVals ];
			for( int i = 0; i < numVals; i++ ) {
				offsets[ i ] = ((Integer) collOff.remove( rnd.nextInt( collOff.size() ))).intValue();
			}
			b.set( offsets, vals );
			b.get( offsets, new Bus.GetCompletionAction() {
				public void completion( Bus bus, float[] values )
				{
					test( values.length, numVals, "numChannels" );
					for( int i = 0; i < values.length; i++ ) {
						test( values[ i ], vals[ i ], "value at " + i );
					}
					nextIter( b, iter + 1 );
				}
			});
			
		} else {
			System.out.println( " : multi setn / getn" );
			final int numIntervals = Math.min( 4, rnd.nextInt( b.getNumChannels() ) + 1 );
			final int numChans[] = new int[ numIntervals ];
			final int offsets[] = new int[ numIntervals ];
			final float[][] vals = new float[ numIntervals ][];
			for( int i = 0, off = 0; i < numIntervals; i++ ) {
//System.out.println( "i = " + i + "; off " + off + "; numInt = " + numIntervals + "; numCh = " + b.getNumChannels() );
				final int skip = Math.max( 1, rnd.nextInt( (b.getNumChannels() - off) - (numIntervals - i) + 1 ));
				offsets[ i ] = off;
				numChans[ i ] = rnd.nextInt( skip ) + 1;
				vals[ i ] = new float[ numChans[ i ]];
				for( int j = 0; j < numChans[ i ]; j++ ) {
					vals[ i ][ j ] = rnd.nextFloat();
				}
				off += skip;
			}
			b.setn( offsets, vals );
			b.getn( offsets, numChans, new Bus.GetCompletionAction() {
				public void completion( Bus bus, float[] values )
				{
					for( int i = 0, j = 0; i < numIntervals; i++ ) {
						for( int k = 0; k < numChans[ i ]; k++, j++ ) {
							test( values[ j ], vals[ i ][ k ], "value at " + j );
						}
					}
					nextIter( b, iter + 1 );
				}
			});
		}
	}
}
