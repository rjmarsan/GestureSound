/*
 *  ServerOptions.java
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
 *		03-Aug-05	created
 *		24-Jul-06	added variable block allocator class
 *		08-Oct-07	added environment variable support (i.e. for Jack); REMOVED AGAIN
 *		11-Feb-08	added options for rendezvous and verbosity
 */

package de.sciss.jcollider;

import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;

/**
 *	A class full of getter/setter methods
 *	to describe the options to boot a local scsynth server.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 11-Feb-08
 */
public class ServerOptions
{
	/**
	 *	Protocol type : universal datagram protocol
	 */
	public static final String		UDP								= "udp";
	/**
	 *	Protocol type : transport control protocol
	 */
	public static final String		TCP								= "tcp";
	
	private static final int		DEFAULT_NUMAUDIOBUSCHANNELS		= 128;
	private static final int		DEFAULT_NUMCONTROLBUSCHANNELS	= 4096;
	private static final int		DEFAULT_NUMINPUTBUSCHANNELS		= 8;
	private static final int		DEFAULT_NUMOUTPUTBUSCHANNELS	= 8;
	private static final int		DEFAULT_NUMBUFFERS				= 1024;
	private static final int		DEFAULT_MAXNODES				= 1024;
	private static final int		DEFAULT_MAXSYNTHDEFS			= 1024;
	private static final String		DEFAULT_PROTOCOL				= UDP;
	private static final int		DEFAULT_BLOCKSIZE				= 64;
	private static final int		DEFAULT_HARDWAREBUFFERSIZE		= 0;
	private static final int		DEFAULT_MEMSIZE					= 8192;
	private static final int		DEFAULT_NUMRGENS				= 64;
	private static final int		DEFAULT_NUMWIREBUFS				= 64;
	private static final double		DEFAULT_SAMPLERATE				= 0.0;
	private static final boolean	DEFAULT_LOADDEFS				= true;
	private static final String		DEFAULT_INPUTSTREAMSENABLED		= null;
	private static final String		DEFAULT_OUTPUTSTREAMSENABLED	= null;
	private static final String		DEFAULT_DEVICE					= null;
	private static final int		DEFAULT_VERBOSITY				= 0;
	private static final boolean	DEFAULT_RENDEZVOUS				= true;

	private int						numAudioBusChannels				= DEFAULT_NUMAUDIOBUSCHANNELS;
	private int						numControlBusChannels			= DEFAULT_NUMCONTROLBUSCHANNELS;
	private int						numInputBusChannels				= DEFAULT_NUMINPUTBUSCHANNELS;
	private int						numOutputBusChannels			= DEFAULT_NUMOUTPUTBUSCHANNELS;
	private int						numBuffers						= DEFAULT_NUMBUFFERS;
	private int						maxNodes						= DEFAULT_MAXNODES;
	private int						maxSynthDefs					= DEFAULT_MAXSYNTHDEFS;
	private String					protocol						= DEFAULT_PROTOCOL;
	private int						blockSize						= DEFAULT_BLOCKSIZE;
	private int						hardwareBufferSize				= DEFAULT_HARDWAREBUFFERSIZE;
	private int						memSize							= DEFAULT_MEMSIZE;
	private int						numRGens						= DEFAULT_NUMRGENS;
	private int						numWireBufs						= DEFAULT_NUMWIREBUFS;
	private double					sampleRate						= DEFAULT_SAMPLERATE;
	private boolean					loadDefs						= DEFAULT_LOADDEFS;
	private String					inputStreamsEnabled				= DEFAULT_INPUTSTREAMSENABLED;
	private String					outputStreamsEnabled			= DEFAULT_OUTPUTSTREAMSENABLED;
	private String					device							= DEFAULT_DEVICE;
	private BlockAllocator.Factory	blockAlloc						= new PowerOfTwoAllocator.Factory();
//	private Map						envVars							= new HashMap();
	private int						verbosity						= DEFAULT_VERBOSITY;
	private boolean					rendezvous						= DEFAULT_RENDEZVOUS;

	/**
	 *	Creates a list of all the server options,
	 *	omitting those whose value equals the default value.
	 *
	 *	@param	port	the port number to use for the <code>-u</code> option
	 *
	 *	@return	a list containing <code>String</code>
	 *			elements of the server options
	 */
	public java.util.List toOptionList( int port )
	{
		return toOptionList( port, false );
	}

	/**
	 *	Creates a list of all the server options.
	 *
	 *	@param	port	the port number to use for the <code>-u</code> option
	 *	@param	verbose	if <code>true</code>, elements for all parameters are
	 *					generated, otherwise only elements for parameters that
	 *					are different from scsynth's defaults are generated
	 *
	 *	@return	a list containing <code>String</code>
	 *			elements of the server options
	 */
	public List toOptionList( int port, boolean verbose )
	{
		final List coll = new ArrayList();

		if( protocol.equals( UDP )) {
			coll.add( "-u" );
		} else if( protocol.equals( TCP )) {
			coll.add( "-t" );
		} else {
			assert false : protocol;
		}
		coll.add( String.valueOf( port ));
		
		if( verbose || (numAudioBusChannels != DEFAULT_NUMAUDIOBUSCHANNELS) ) {
			coll.add( "-a" );
			coll.add( String.valueOf( numAudioBusChannels ));
		}
		if( verbose || (numControlBusChannels != DEFAULT_NUMCONTROLBUSCHANNELS) ) {
			coll.add( "-c" );
			coll.add( String.valueOf( numControlBusChannels ));
		}
		if( verbose || (numInputBusChannels != DEFAULT_NUMINPUTBUSCHANNELS) ) {
			coll.add( "-i" );
			coll.add( String.valueOf( numInputBusChannels ));
		}
		if( verbose || (numOutputBusChannels != DEFAULT_NUMOUTPUTBUSCHANNELS) ) { 
			coll.add( "-o" );
			coll.add( String.valueOf( numOutputBusChannels ));
		}
		if( verbose || (numBuffers != DEFAULT_NUMBUFFERS) ) { 
			coll.add( "-b" );
			coll.add( String.valueOf( numBuffers ));
		}
		if( verbose || (maxNodes != DEFAULT_MAXNODES) ) {
			coll.add( "-n" );
			coll.add( String.valueOf( maxNodes ));
		}
		if( verbose || (maxSynthDefs != DEFAULT_MAXSYNTHDEFS) ) { 
			coll.add( "-d" );
			coll.add( String.valueOf( maxSynthDefs ));
		}
		if( verbose || (blockSize != DEFAULT_BLOCKSIZE) ) {
			coll.add( "-z" );
			coll.add( String.valueOf( blockSize ));
		}
		if( verbose || (hardwareBufferSize != DEFAULT_HARDWAREBUFFERSIZE) ) {
			coll.add( "-Z" );
			coll.add( String.valueOf( hardwareBufferSize ));
		}
		if( verbose || (memSize != DEFAULT_MEMSIZE) ) {
			coll.add( "-m" );
			coll.add( String.valueOf( memSize ));
		}
		if( verbose || (numRGens != DEFAULT_NUMRGENS) ) {
			coll.add( "-r" );
			coll.add( String.valueOf( numRGens ));
		}
		if( verbose || (numWireBufs != DEFAULT_NUMWIREBUFS) ) {
			coll.add( "-w" );
			coll.add( String.valueOf( numWireBufs ));
		}
		if( verbose || (sampleRate != DEFAULT_SAMPLERATE) ) {
			coll.add( "-S" );
			coll.add( String.valueOf( sampleRate ));
		}
		if( verbose || (loadDefs != DEFAULT_LOADDEFS) ) {
			coll.add( "-D" );
			coll.add( String.valueOf( loadDefs ? 1 : 0 ));
		}
		if( inputStreamsEnabled != DEFAULT_INPUTSTREAMSENABLED ) {
			coll.add( "-I" );
			coll.add( inputStreamsEnabled );
		}
		if( outputStreamsEnabled != DEFAULT_OUTPUTSTREAMSENABLED ) {
			coll.add( "-O" );
			coll.add( outputStreamsEnabled );
		}
		if( device != DEFAULT_DEVICE ) {
			coll.add( "-H" );
			coll.add( device );
		}
		if( verbose || (verbosity != DEFAULT_VERBOSITY) ) {
			coll.add( "-v" );
			coll.add( String.valueOf( verbosity ));
		}
		if( verbose || (rendezvous != DEFAULT_RENDEZVOUS) ) {
			coll.add( "-R" );
			coll.add( String.valueOf( rendezvous ? 1 : 0 ));
		}
		
		return coll;
	}
	
	/**
	 *	Creates a new instance with default options
	 */
	public ServerOptions()
	{
		// nothing too interesting here
	}

	public int getNumAudioBusChannels()
	{
		return numAudioBusChannels;
	}

	public void setNumAudioBusChannels( int numAudioBusChannels )
	{
		this.numAudioBusChannels = numAudioBusChannels;
	}

	public int getNumControlBusChannels()
	{
		return numControlBusChannels;
	}

	public void setNumControlBusChannels( int numControlBusChannels )
	{
		this.numControlBusChannels = numControlBusChannels;
	}

	public int getNumInputBusChannels()
	{
		return numInputBusChannels;
	}

	public void setNumInputBusChannels( int numInputBusChannels )
	{
		this.numInputBusChannels = numInputBusChannels;
	}

	public int getNumOutputBusChannels()
	{
		return numOutputBusChannels;
	}

	public void setNumOutputBusChannels( int numOutputBusChannels )
	{
		this.numOutputBusChannels = numOutputBusChannels;
	}

	public int getNumBuffers()
	{
		return numBuffers;
	}

	public void setNumBuffers( int numBuffers )
	{
		this.numBuffers = numBuffers;
	}

	public int getMaxNodes()
	{
		return maxNodes;
	}

	public void setMaxNodes( int maxNodes )
	{
		this.maxNodes = maxNodes;
	}

	public int getMaxSynthDefs()
	{
		return maxSynthDefs;
	}

	public void setMaxSynthDefs( int maxSynthDefs )
	{
		this.maxSynthDefs = maxSynthDefs;
	}

	public String getProtocol()
	{
		return protocol;
	}

	/**
	 *	@throws	IllegalArgumentException	if <code>protocol</code> is not
	 *										<code>UDP</code>
	 */
	public void setProtocol( String protocol )
	{
		if( !protocol.equals( UDP ) && !protocol.equals( TCP )) {
			throw new IllegalArgumentException( protocol );
		}
	
		this.protocol = protocol;
	}

	public int getBlockSize()
	{
		return blockSize;
	}

	public void setBlockSize( int blockSize )
	{
		this.blockSize = blockSize;
	}

	public int getHardwareBufferSize()
	{
		return hardwareBufferSize;
	}

	public void setHardwareBufferSize( int hardwareBufferSize )
	{
		this.hardwareBufferSize = hardwareBufferSize;
	}

	public int getMemSize()
	{
		return memSize;
	}

	public void setMemSize( int memSize )
	{
		this.memSize = memSize;
	}

	public int getNumRGens()
	{
		return numRGens;
	}

	public void setNumRGens( int numRGens )
	{
		this.numRGens = numRGens;
	}

	public int getNumWireBufs()
	{
		return numWireBufs;
	}

	public void setNumWireBufs( int numWireBufs )
	{
		this.numWireBufs = numWireBufs;
	}

	public double getSampleRate()
	{
		return sampleRate;
	}

	public void setSampleRate( double sampleRate )
	{
		this.sampleRate = sampleRate;
	}

	public boolean getLoadDefs()
	{
		return loadDefs;
	}

	public void setLoadDefs( boolean loadDefs )
	{
		this.loadDefs = loadDefs;
	}

	public String getInputStreamsEnabled()
	{
		return inputStreamsEnabled;
	}

	public void setInputStreamsEnabled( String inputStreamsEnabled )
	{
		this.inputStreamsEnabled = inputStreamsEnabled;
	}

	public String getOutputStreamsEnabled()
	{
		return outputStreamsEnabled;
	}

	public void setOutputStreamsEnabled( String outputStreamsEnabled )
	{
		this.outputStreamsEnabled = outputStreamsEnabled;
	}

	public String getDevice()
	{
		return device;
	}
	
	public void setDevice( String device )
	{
		this.device = device;
	}

	public BlockAllocator.Factory getBlockAllocFactory()
	{
		return blockAlloc;
	}
	
	public void setBlockAllocFactory( BlockAllocator.Factory baf )
	{
		blockAlloc = baf;
	}
	
	public int getVerbosity()
	{
		return verbosity;
	}
	
	public void setVerbosity( int verbosity )
	{
		this.verbosity = verbosity;
	}

	public boolean getRendezvous()
	{
		return rendezvous;
	}
	
	public void setRendezvous( boolean rendezvous )
	{
		this.rendezvous = rendezvous;
	}

//	public void setEnv( String key, String value )
//	{
//		envVars.put( key, value );
//	}
//	
//	public String getEnv( String key )
//	{
//		return envVars.get( key ).toString();
//	}
//	
//	public Map getEnvMap()
//	{
//		return Collections.unmodifiableMap( envVars );
//	}

	/**
	 *	Utility method to return the audio bus offset
	 *	for the first bus which is not connected to
	 *	audio hardware interface channels. This value
	 *	is only valid, if the server was booted with
	 *	this options
	 *
	 *	@return	the index of the first audio bus not
	 *			connected to audio hardware
	 */
	public int getFirstPrivateBus()
	{
		return( getNumOutputBusChannels() + getNumInputBusChannels() );
	}

	/**
	 *	Converts a list of string elements to one
	 *	an array of strings
	 *
	 *	@param	list	a list whose elements are <code>String</code>s
	 *	@return	array of those elements in the same order as they appeared
	 *			in the list
	 */
	public static String[] optionListToStringArray( List list )
	{
		final String[] array	= new String[ list.size() ];
	
		for( int i = 0; i < list.size(); i++ ) {
			array[ i ]	= list.get( i ).toString();
		}
		
		return array;
	}
	
	/**
	 *	Converts an array of strings to a string
	 *	which can be used in a Un*x console. This
	 *	assembles all elements with space characters between
	 *	them. If an element contains a space character, it
	 *	will be put in quotes.
	 *
	 *	@param	array	parameters to a un*x process
	 *	@return	concatenated command string
	 */
	public static String stringArrayToConsoleString( String[] array )
	{
		final StringBuffer	strBuf	= new StringBuffer();
		boolean				b;
	
		for( int i = 0; i < array.length; i++ ) {
			b	= array[ i ].indexOf( ' ' ) != -1;
			if( b ) strBuf.append( '"' );
			strBuf.append( array[ i ]);
			if( b ) strBuf.append( '"' );
			strBuf.append( ' ' );
		}
		
		return strBuf.toString();
	}
}