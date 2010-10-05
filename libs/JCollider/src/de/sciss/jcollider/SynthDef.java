/*
 *  SynthDef.java
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
 *		29-Jun-05	created
 *		15-Oct-05	fixed a bug with non-recognized controls (LagControl, TrigControl)
 *		24-Feb-05	fixed bug with missing control names as produced by Control.names([ \gaga ]).kr([ 1, 2 ])
 *					for example (first control output is named \gaga, second is named "?" now like sclang does)
 *		29-Jul-06	added load() and play()
 */

package de.sciss.jcollider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.sciss.net.OSCMessage;

/**
 *	This is the representation of a UGen graph, a prototype
 *	for a synth node. While it was created to mimic most of the
 *	behaviour of the SClang counterpart, a lot of the internals
 *	are slightly different, including the whole idea of how
 *	a UGen graph is represented.
 *	<p>
 *	While in SClang as an interpreter
 *	language the graph is represented as a function, this is
 *	not appropriate for java as a (semi)compiled language. Therefore,
 *	you do not pass a graph function to the constructor, but rather
 *	a collection of graph elements (UGens and Constants) which have
 *	already been put together.
 *	<p>
 *	This also implies that there is no
 *	function header which can be read by <code>SynthDef</code> to
 *	automatically construct <code>Control</code> UGens from the
 *	function's arguments. You therefore have to create <code>Control</code>
 *	UGens explicitly yourself. <code>SynthDef</code> will find them
 *	and construct the synth def binary object accordingly.
 *	<p>
 *	Note that this class includes the functionality found separately
 *	in SClang's SynthDesc class, that is methods for reading and formatting
 *	a synth def. Unlike SClang, when a synth def is read, UGens are
 *	created as instances of the <code>UGen</code> class. In an earlier
 *	version, <code>java.lang.reflect</code> was used to dynamically load
 *	UGen subclasses. This concept was dropped because it would imply
 *	<UL>
 *	<LI>that the JCollider java source is modified whenever new UGens
 *	are introduced to supercollider</LI>
 *	<LI>a tree of some hundred classes would have to be created with all
 *	the memory requirements and time consumption when the VM loads
 *	the JCollider package</LI>
 *	<LI>having to deal with all the smalltalk idiosyncratic stuff
 *	in the UGen representations which often are different from
 *	the actual UGens objects as known by the server</LI>
 *	</UL>
 *	<P>
 *	So instead, there is one clumsy <code>UGen</code> class which
 *	carries all the information about inlets and outlets. A synth def
 *	file is sufficient to recreate the graph tree using this class.
 *	On the other side, when you yourself create a UGen tree, another
 *	objects comes in, the <code>UGenInfo</code> which acts as a lookup
 *	table for installed UGen clases.
 *	<p>
 *	This class is somewhat more simple than the SClang counterpart.
 *	For example, input rate consistency is not checked.
 *	Tree optimization is still inferior because of the non-existing
 *	UGen subclasses (like <code>BinaryOpUGen</code>) that could handle
 *	context-sensitive optimization. Control-lags and Trigger-controls 
 *	are not supported. For the sake of cleanness, all the strange
 *	interpenetration of SynthDef and UGen in the building process as
 *	exhibited by SClang was dropped, where the def would go and write
 *	things into the UGen and vice versa, setting up temporary fields
 *	like the building-def and so on. So this implementation is more
 *	stripped down but way cleaner and less spaghetti.
 *	<p>
 *	There seemed to be a non-finished project in SClang's SynthDef
 *	called &quot;variants&quot;. i don't know what this was, it
 *	has was just been dropped.
 *	<p>
 *	Here is an example of building a SynthDef (comments are below):
 *	<pre>
 *
 *	GraphElem   f       = null;
 *	GraphElem   g, h;
 *	Control     c       = Control.kr( new String[] { "resinv" }, new float[] { 0.5f });
 *	UGenChannel reso    = c.getChannel( 0 );
 *	Synth       synth;
 *	Random      r       = new Random( System.currentTimeMillis() );
 *	String      defName = "JNoiseBusiness1b";
 *	OSCBundle   bndl;
 *	SynthDef    def;
 *	long        time;
 *	
 *	f = null;
 *	for( int i = 0; i < 4; i++ ) {
 *	    g = UGen.ar( "*", UGen.ar( "LFSaw", UGen.kr( "midicps", UGen.kr( "MulAdd",
 *	        UGen.kr( "LFPulse", UGen.ir( 0.06f ), UGen.ir( 0 ), UGen.ir( 0.5f )),
 *	            UGen.ir( 2 ), UGen.array( UGen.ir( 34 + r.nextFloat() * 0.2f ),
 *	                                      UGen.ir( 34 + r.nextFloat() * 0.2f ))))),
 *	          UGen.ir( 0.01f ));
 *	    f = (f == null) ? g : UGen.ar( "+", f, g );
 *	}
 *	h   = UGen.kr( "LinExp", UGen.kr( "SinOsc", UGen.ir( 0.07f )),
 *	          UGen.ir( -1 ), UGen.ir( 1 ), UGen.ir( 300 ), UGen.ir( 5000 ));
 *	f   = UGen.ar( "softclip", UGen.ar( "RLPF", f, h, reso ));
 *	f   = UGen.ar( "softclip", UGen.ar( "RLPF", f, h, reso ));
 *	def = new SynthDef( defName, UGen.ar( "Out", UGen.ir( 0 ), f ));
 *	
 *	synth = Synth.basicNew( defName, myServer );
 *	try {
 *	    def.send( myServer, synth.newMsg( myServer.asTarget(),
 *	        new String[] { "resinv" }, new float[] { 0.98f }));
 *	    time = System.currentTimeMillis();
 *	    for( int i = 500; i < 5000; i += 250 ) {
 *	        bndl = new OSCBundle( time + i );
 *	        bndl.addPacket( synth.setMsg( "resinv", r.nextFloat() * 0.8f + 0.015f ));
 *	        myServer.sendBundle( bndl );
 *	    }
 *	    bndl = new OSCBundle( time + 5500 );
 *	    bndl.addPacket( synth.freeMsg() );
 *	    myServer.sendBundle( bndl );
 *	}
 *	catch( IOException e1 ) {
 *	    System.err.println( e1 );
 *	}
 *	</pre>
 *
 *	Yes, it's true, the code is at least three times as big as
 *	would be the SClang counter part, but we're definitely focussing
 *	on an application different from jit developing synthesizers.
 *	<p>
 *	So some remarks on the example (the sound isn't particularly
 *	interesting though ;-) : generally it improves readability if
 *	me create piece of the graph in more than one line. While the
 *	loop body is difficult to read, the statement that adds clipping
 *	and resonance is easy. Since we have only one dead end for the
 *	graph (Out.ar), we simply pass the result of <code>UGen.ar( &quot;Out&quot; ... )</code>
 *	to the synth def constructor. to overwrite the resonance control default
 *	value of 0.5 (as specified in the <code>Control</code> constructor),
 *	we add control name and value parameters to the <code>synth.newMsg</code>
 *	call. the result of this call is an <code>OSCMessage</code> (whereas
 *	<code>new Synth( ... )</code> would have sent that message immediately),
 *	this is passed to the synth def constructor as the completion message.
 *	the rest shows you how to send bundles to set the resonance value
 *	of the synth at certain times.
 *	<p>
 *	You will probably want to use SClang to prototype the synthesizers
 *	and then just port them to JCollider which shouldn't be too
 *	difficult after some practising. See the JColliderDemo for
 *	more examples of UGen graphs.
 *
 *	@todo	for the same synth def, the graphs produced by SClang
 *			and JCollider can look slightly different regarding the
 *			ordering of the topology. this should be reviewed more
 *			thoroughly. It doesn't seem that JCollider is less
 *			efficient (from the CPU loads point of view), but it
 *			makes comparison and debugging a bit tricky
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.32, 25-Feb-08
 */
public class SynthDef
implements Constants
{
	/**
	 *	Default file suffix when writing defs to disk.
	 */
	public static final String		SUFFIX			= ".scsyndef";

	/**
	 *	Currently supported synth def file
	 *	version (1).
	 */
	public static final int			SCGF_VERSION	= 1;

	private static final int		SCGF_MAGIC		= 0x53436766; // 'SCgf'
	
	private			List			controlDescs	= new ArrayList();
	private			List			ugens			= new ArrayList();
	private			Set				ugenSet			= new HashSet();
	private			List			constants		= new ArrayList();
	private			Set				constantSet		= new HashSet();
	private	final	String			name;
	private			List			variants		= new ArrayList();
	
	private static final Object[]	RATES			= { kScalarRate, kControlRate, kAudioRate, kDemandRate };

	private static final Comparator synthIdxComp	= new SynthIndexComparator();

	private static final Set		ctrlUGensSet	= new HashSet();

	static {
		ctrlUGensSet.add( "Control" );
		ctrlUGensSet.add( "TrigControl" );
		ctrlUGensSet.add( "LagControl" );
	}

	private SynthDef( String name )
	{
		this.name	= name;
	}

	/**
	 *	Constructs a new SynthDef from the given
	 *	graph element.
	 *
	 *	@param	name	the name of the synth def
	 *					as would be used to instantiate a <code>Synth</code>
	 *	@param	graph	a graph element such as a <code>UGen</code> or
	 *					a collection of ugens. Basically anything that
	 *					comes out of one of the static contructor methods
	 *					of the <code>UGen</code> class. Note that when there
	 *					are several &quot;dead ends&quot; in the graph, those
	 *					dead ends should be collected in a <code>GraphElemArray</code>
	 *					which is then passed to <code>SynthDef</code>, otherwise the
	 *					synthdef may be incomplete. See the <code>JColliderDemo</code>
	 *					to see how to do it.
	 */
	public SynthDef( String name, GraphElem graph )
	{
		this( name );
		build( GraphElemArray.asArray( graph ));
	}

	private void build( GraphElemArray graphArray )
	{
		// save/restore controls in case of *wrap
//		var saveControlNames = controlNames;
		
//		prependArgs = prependArgs.asArray;
//		this.addControlsFromArgsOfFunc( func, rates, prependArgs.size );
//		result = func.valueArray( prependArgs ++ this.buildControls );
		
//		controlNames = saveControlNames

		// collectUGens)

		collectUGens( graphArray );
//		optimizeGraph();

		// collects only those in used UGens,
		// therefore we do not pass graphArray as an argument
		collectConstants();
		
		// XXX should do this conditionally
		// (using a static boolean)
//		checkInputs();
		
		// re-sort graph. reindex.
		topologicalSort();
//		indexUGens();
	}
	
//	private void addUGenInput( UGenInput ui )
//	{
//		if( ui instanceof UGenChannel ) {
//			addUGen( ((UGenChannel) ui).getUGen() );
//		} else if( ui instanceof Constant ) {
//			addConstant( (Constant) ui );
//		} else {
//			assert false : ui.getClass().getName();
//		}
//	}

	private void addControlDesc( ControlDesc desc )
	{
//System.err.print( "me add dem desc " );
//desc.printOn( System.err );
		controlDescs.add( desc );
	}

	// includes check for Controls !
	private void addUGen( UGen ugen )
	{
		if( ugenSet.add( ugen )) {
//		ugen.initSetSynthIndex( ugens.size() );
			ugens.add( ugen );
			if( ugen instanceof Control ) {
				final Control ctrl = (Control) ugen;
				ctrl.setSpecialIndex( controlDescs.size() );
				for( int i = 0; i < ctrl.getNumDescs(); i++ ) {
					addControlDesc( ctrl.getDesc( i ));
				}
			}
		}
	}

	private void addConstant( Constant value )
	{
		if( constantSet.add( value )) {
			constants.add( value );
		}
	}

	private void collectUGens( GraphElemArray graphArray )
	{
		UGen		ugen;
		GraphElem	g;
	
		for( int i = 0; i < graphArray.getNumElements(); i++ ) {
			g = graphArray.getElement( i );
			if( g instanceof UGen ) {
				ugen = (UGen) g;
				addUGen( ugen );
				collectUGens( ugen.getInputs() );
			} else if( g instanceof GraphElemArray ) {
				collectUGens( (GraphElemArray) g );		// recurse
			} else {
				collectUGens( g.asUGenInputs() );		// e.g. UGenChannel
			}
		}
	}

	private void collectUGens( UGenInput[] ins )
	{
		UGen ugen;
	
		for( int i = 0; i < ins.length; i++ ) {
			if( ins[ i ] instanceof UGenChannel ) {
				ugen = ((UGenChannel) ins[ i ]).getUGen();
				addUGen( ugen );
				collectUGens( ugen.getInputs() );
			}
		}
	}
	
	private void collectConstants()
	{
		UGen		ugen;
		UGenInput	ui;
	
		for( int i = 0; i < ugens.size(); i++ ) {
			ugen = (UGen) ugens.get( i );
			for( int j = 0; j < ugen.getNumInputs(); j++ ) {
				ui = ugen.getInput( j );
				if( ui instanceof Constant ) {
					addConstant( (Constant) ui );
				}
			}
		}
	}

	private void topologicalSort()
	{
		// initializes the inlet/outlet lists
		// and collects the ugens that do not
		// rely on other ugens
		final List available = initTopoSort();

		// now all ugens are collected as ugen-environments
		// and will be re-added according to the tree structure
		ugens.clear();

		UGenEnv env, env2;

		while( !available.isEmpty() ) {
			env = (UGenEnv) available.remove( available.size() - 1 );
			for( int i = env.collDe.size() - 1; i >= 0; i-- ) {
				env2 = (UGenEnv) env.collDe.get( i );
				env2.collAnte.remove( env );
				if( env2.collAnte.isEmpty() ) available.add( env2 ); // treated in next loop
			}
			ugens.add( env.ugen );
		}

//		cleanupTopoSort();
	}

	private List initTopoSort()
	{
		final int				numUGens	= ugens.size();
	
		final List				available	= new ArrayList();
		final Map				mapEnv		= new HashMap();
		final UGenEnv[]			envs		= new UGenEnv[ numUGens ];

		UGen					ugen;
		UGenEnv					env, env2;
		UGenInput				ui;

		for( int i = 0; i < numUGens; i++ ) {
			ugen		= (UGen) ugens.get( i );
			env			= new UGenEnv( ugen, i );
			mapEnv.put( ugen, env );
			envs[ i ]	= env;
		}
		
		for( int i = 0; i < numUGens; i++ ) {
			env			= envs[ i ];
			ugen		= env.ugen;
//			ugen.initTopoSort(); // this populates the descendants and antecedents
			for( int j = 0; j < ugen.getNumInputs(); j++ ) {
				ui		= ugen.getInput( j );
				if( ui instanceof UGenChannel ) {
					env2	= (UGenEnv) mapEnv.get( ((UGenChannel) ui).getUGen() );
					env.collAnte.add( env2 );
					env2.collDe.add( env );
				}
			}
		}
		for( int i = numUGens - 1; i >= 0; i-- ) {
			env		= envs[ i ];
//			ugen.descendants = ugen.descendants.asArray.sort(
//								{ arg a, b; a.synthIndex < b.synthIndex }
			Collections.sort( env.collDe, synthIdxComp );
//			ugen.makeAvailable(); // all ugens with no antecedents are made available
			if( env.collAnte.isEmpty() ) {
				available.add( env );
			}
		}
		
		return available;
	}

	private byte[] asBytes()
	throws IOException
	{
		final ByteArrayOutputStream	baos	= new ByteArrayOutputStream();
		final DataOutputStream		dos		= new DataOutputStream( baos );
	
		// don't ask me where these lines
		// are in sclang
		dos.writeInt( SCGF_MAGIC );
		dos.writeInt( SCGF_VERSION );
		dos.writeShort( 1 ); // number of defs in file.
		write( dos );
		dos.flush();
		dos.close();
		return baos.toByteArray();
	}
	
	/**
	 *	Sends the definition to
	 *	a server.
	 *
	 *	@param	server	to representation of the server
	 *					to send the def to
	 *
	 *	@throws	IOException	if a network error occured
	 */
	public void send( Server server )
	throws IOException
	{
		server.sendMsg( recvMsg() );
	}

	/**
	 *	Sends the definition to
	 *	a server. The server will
	 *	execute the optional completion message
	 *	when it has processed the definition.
	 *
	 *	@param	server			to representation of the server
	 *							to send the def to
	 *	@param	completionMsg	message to execute by the server
	 *							when the synth def has become available.
	 *							typically something like <code>Synth.newMsg( ... )</code>.
	 *							may be <code>null</code>
	 *
	 *	@throws	IOException	if a network error occured
	 */
	public void send( Server server, OSCMessage completionMsg )
	throws IOException
	{
		server.sendMsg( recvMsg( completionMsg ));
	}
	
	/**
	 *	Constructs a message to sends to
	 *	a server for providing the synth def. 
	 *
	 *	@return	message ready to send to a server
	 *
	 *	@throws	IOException	when synth def compilation
	 *			fails (? this should never happen?)
	 */
	public OSCMessage recvMsg()
	throws IOException
	{
		return recvMsg( null );
	}

	/**
	 *	Constructs a message to sends to
	 *	a server for providing the synth def. 
	 *	The optional completion message is
	 *	attached to the returned message and
	 *	will be executed by the server, when
	 *	the definition has become available.
	 *
	 *	@param	completionMsg	completion message, such as <code>/s_new</code>
	 *							or <code>null</code>
	 *
	 *	@return	message ready to send to a server
	 *
	 *	@throws	IOException	when synth def compilation
	 *			fails (? this should never happen?)
	 */
	public OSCMessage recvMsg( OSCMessage completionMsg )
	throws IOException
	{
		final Object[] args;
		
		if( completionMsg == null ) {
			args = new Object[] { this.asBytes() };
		} else {
			args = new Object[] { this.asBytes(), completionMsg };
		}
		return new OSCMessage( "/d_recv", args );
	}

	/**
	 *	Returns the name of the synth definition
	 */
	public String getName()
	{
		return name;
	}

	/**
	 *	Stores the def in a temp file and sends a
	 *	corresponding OSC <code>/d_load</code> message to the server.
	 *
	 *	@param	s	the server to send the def to
	 *
	 *	@throws	IOException	if the file could not be created or the message could not be sent
	 */
	public void load( Server s )
	throws IOException
	{
		load( s, null );
	}

	/**
	 *	Stores the def in a temp file and sends a
	 *	corresponding OSC <code>/d_load</code> message to the server.
	 *
	 *	@param	s				the server to send the def to
	 *	@param	completionMsg	an OSC message to be executed when the def was received (can be <code>null</code>)
	 *
	 *	@throws	IOException	if the file could not be created or the message could not be sent
	 */
	public void load( Server s, OSCMessage completionMsg )
	throws IOException
	{
		final File f = File.createTempFile( "tmp", SUFFIX );
		f.deleteOnExit();
		load( s, completionMsg, f );
	}

	/**
	 *	Stores the def in a file and sends a
	 *	corresponding OSC <code>/d_load</code> message to the server.
	 *
	 *	@param	s				the server to send the def to
	 *	@param	completionMsg	an OSC message to be executed when the def was received (can be <code>null</code>)
	 *	@param	path			path to a file. if a file by this name
	 *							already exists, the caller should delete it
	 *							before calling this method
	 *
	 *	@throws	IOException	if the file could not be created or the message could not be sent
	 *
	 *	@warning	unlike in SClang, the path denotes the file not the
	 *				parent folder of the file
	 */
	public void load( Server s, OSCMessage completionMsg, File path )
	throws IOException
	{
		final Object[] args;
		writeDefFile( path );
		if( completionMsg == null ) {
			args = new Object[] { path };
		} else {
			args = new Object[] { path, completionMsg };
		}
		s.sendMsg( new OSCMessage( "/d_load", args ));
	}
	
	/**
	 *	Sends the def to the server and creates a synth from this def.
	 *
	 *	@param	target	the group to whose head the node is added
	 *	@return	the newly created synth
	 *
	 *	@throws	IOException	if a network error occurs
	 */
	public Synth play( Group target )
	throws IOException
	{
		return play( target, null, null );
	}
	
	/**
	 *	Sends the def to the server and creates a synth from this def.
	 *
	 *	@param	target		the group to whose head the node is added
	 *	@param	argNames	the names of the controls to set. can be <code>null</code>
	 *	@param	argValues	the values of the controls. each array element corresponds to
	 *						the element in <code>argNames</code> with the same index. the sizes of <code>argValues</code>
	 *						and <code>argNames</code> must be equal. can be <code>null</code>
	 *	@return	the newly created synth
	 *
	 *	@throws	IOException	if a network error occurs
	 */
	public Synth play( Group target, String[] argNames, float[] argValues )
	throws IOException
	{
		return play( target, argNames, argValues, kAddToHead );
	}
	 
	/**
	 *	Sends the def to the server and creates a synth from this def.
	 *
	 *	@param	target		the node to which the new synth is added
	 *	@param	argNames	the names of the controls to set. can be <code>null</code>
	 *	@param	argValues	the values of the controls. each array element corresponds to
	 *						the element in <code>argNames</code> with the same index. the sizes of <code>argValues</code>
	 *						and <code>argNames</code> must be equal. can be <code>null</code>
	 *	@param	addAction	the add action re <code>target</code>
	 *	@return	the newly created synth
	 *
	 *	@throws	IOException	if a network error occurs
	 */
	public Synth play( Node target, String[] argNames, float[] argValues, int addAction )
	throws IOException
	{
		final Synth			synth;
		final OSCMessage	msg;
		
		synth	= Synth.basicNew( getName(), target.getServer() );
		msg		= synth.newMsg( target, argNames, argValues, addAction );
		
		send( target.getServer(), msg );

		return synth;
	}

	/**
	 *	Prints a textual representation
	 *	of the synth def to the given stream.
	 *	This will print a list of all ugens
	 *	and their wiring. Useful for debugging.
	 *
	 *	@param	out	the stream to print on, such as <code>System.out</code>
	 *
	 *	@todo	resolve alias names for specialIndex
	 *			synths such as BinaryOpUGen
	 *			(would require the use of <code>UGenInfo</code>
	 *			which in turn requires to read in all
	 *			UGen definitions, some overhead we may
	 *			not want ... ?)
	 *
	 *	@see	System#out
	 */
	public void printOn( PrintStream out )
	{
		UGen		ugen;
		UGenInput[]	inputs;
		UGenChannel	uch;
	
		out.println( "SynthDef(\"" + getName() + "\")" );

		if( ugens.size() > 0 ) out.println( "\n ugens:" );
		for( int i = 0; i < ugens.size(); i++ ) {
			out.print( "  #" + i + " : " );
			ugen	= (UGen) ugens.get( i );
			out.print( ugen.dumpName() + " @ " + ugen.getRate() );
			if( ugen.getNumOutputs() != 1 ) out.print( ", numOuts: " + ugen.getNumOutputs() );
			inputs	= ugen.getInputs();
			if( inputs.length > 0 ) {
				out.print( ", arg: [ " );
				for( int j = 0; j < inputs.length; j++ ) {
					if( inputs[ j ] instanceof UGenChannel ) {
						uch = (UGenChannel) inputs[ j ];
						out.print( "#" + ugens.indexOf( uch.getUGen() ) + '_' );
						if( uch.getUGen().getName().equals( "Control" )) {
							out.print( "Control(\"" + ((ControlDesc) controlDescs.get(
								uch.getUGen().getSpecialIndex() + uch.getChannel() )).getName() + "\")" );
						} else {
							out.print( uch.dumpName() );
						}
					} else {
						out.print( inputs[ j ].dumpName() );
					}
					if( j < inputs.length - 1 ) out.print( ", " );
				}
				out.print( " ]" );
			}
			out.println();
		}

		if( controlDescs.size() > 0 ) out.println( "\n controls:" );
		for( int i = 0; i < controlDescs.size(); i++ ) {
			out.print( "  #" + i + " : " );
			((ControlDesc) controlDescs.get( i )).printOn( out );
		}
	}
	
	/**
	 *	Return a list of all UGens in the graph
	 *	(in the depth-first sorted topological order).
	 *
	 *	@return	list whose elements are of class <code>UGen</code>
	 */
	public List getUGens()
	{
		return new ArrayList( ugens );
	}

	/**
	 *	Checks to see if a given file is a
	 *	synth definition file.
	 *
	 *	@param	path to the synth def file
	 *
	 *	@return	<code>true</code> if the file starts with
	 *			the synth definition magic cookie. does not
	 *			check for the synth def file version
	 *
	 *	@throws	IOException	if the file could not be read
	 */
	public static boolean isDefFile( File path )
	throws IOException
	{
		final DataInputStream dis = new DataInputStream( new FileInputStream( path ));
		final boolean result = (dis.available() >= 10) && (dis.readInt() == SCGF_MAGIC);
		dis.close();
		return result;
	}

	/**
	 *	Writes an array of definitions to a file.
	 *
	 *	@param	path	path to a file. if a file by this name
	 *					already exists, the caller should delete it
	 *					before calling this method
	 *	@param	defs	array of definitions which will be written
	 *					one after another
	 *
	 *	@throws	IOException	if the file cannot be opened, denotes a
	 *						directory, or if a write error occurs
	 *
	 *	@warning	unlike in SClang, the path denotes the file not the
	 *				parent folder of the file
	 */
	public static void writeDefFile( File path, SynthDef[] defs )
	throws IOException
	{
		final OutputStream		os	= new FileOutputStream( path );
		final DataOutputStream	dos	= new DataOutputStream( new BufferedOutputStream( os ));
		
		try {
			dos.writeInt( SCGF_MAGIC );
			dos.writeInt( SCGF_VERSION );
			dos.writeShort( defs.length ); // number of defs in file.
			for( int i = 0; i < defs.length; i++ ) {
				defs[ i ].write( dos );
			}
		}
		finally {
			dos.close();
		}
	}

	/**
	 *	Writes this def to a definition file. That it,
	 *	the resulting file will contain just one definition, that is us.
	 *
	 *	@param	path	path to a file. if a file by this name
	 *					already exists, the caller should delete it
	 *					before calling this method
	 *
	 *	@throws	IOException	if the file cannot be opened, denotes a
	 *						directory, or if a write error occurs
	 *
	 *	@warning	unlike in SClang, the path denotes the file not the
	 *				parent folder of the file
	 */
	public void writeDefFile( File path )
	throws IOException
	{
		SynthDef.writeDefFile( path, new SynthDef[] { this });
	}

	/**
	 *	Writes this def to an output stream (such as a file or
	 *	a memory buffer).
	 *
	 *	@param	os		stream to write to. the stream will be
	 *					buffered by this method, so you do not need
	 *					to do this
	 *
	 *	@throws	IOException	if a write error occurs
	 */
	public void write( OutputStream os )
	throws IOException
	{
		final DataOutputStream dos = new DataOutputStream( new BufferedOutputStream( os ));
	
		write( dos );
		dos.flush();
	}

	private void write( DataOutputStream dos )
	throws IOException
	{
		ControlDesc	desc;

		SynthDef.writePascalString( dos, name );
		
		writeConstants( dos );

		dos.writeShort( controlDescs.size() );
		for( int i = 0; i < controlDescs.size(); i++ ) {
			desc = (ControlDesc) controlDescs.get( i );
			dos.writeFloat( desc.getDefaultValue() );
		}
		
		dos.writeShort( controlDescs.size() );
		for( int i = 0; i < controlDescs.size(); i++ ) {
			desc = (ControlDesc) controlDescs.get( i );
			if( desc.getName() != null ) {
				SynthDef.writePascalString( dos, desc.getName() );
//				dos.writeShort( desc.getIndex() );
				dos.writeShort( i );
			} else {
				System.err.println( "Warning: unnamed control " + i + " dropped." );
			}
		}
	
		dos.writeShort( ugens.size() );
		for( int i = 0; i < ugens.size(); i++ ) {
			writeUGenSpec( dos, (UGen) ugens.get( i ));
		}
		
		dos.writeShort( variants.size() );
		if( !variants.isEmpty() ) {
			throw new IllegalStateException( "Variants : not supported!!" );
		}
	}

	private void writeConstants( DataOutputStream dos )
	throws IOException
	{
		dos.writeShort( constants.size() );
		for( int i = 0; i < constants.size(); i++ ) {
			dos.writeFloat( ((Constant) constants.get( i )).getValue() );
		}
	}

	private int getRateID( Object rate )
	{
		for( int i = 0; i < RATES.length; i++ ) {
			if( rate.equals( RATES[ i ])) return i;
		}
		return -1;
	}

	private void writeUGenSpec( DataOutputStream dos, UGen ugen )
	throws IOException
	{
		final UGenInput[]	inputs		= ugen.getInputs();
		final Object[]		outputRates	= ugen.getOutputRates();
	
		writePascalString( dos, ugen.getName() );

		dos.writeByte( getRateID( ugen.getRate() ));
		dos.writeShort( ugen.getNumInputs() );
		dos.writeShort( ugen.getNumOutputs() );
		dos.writeShort( ugen.getSpecialIndex() );

		for( int i = 0; i < inputs.length; i++ ) {
			writeInputSpec( dos, inputs[ i ]);
		}
		for( int i = 0; i < outputRates.length; i++ ) {
			dos.writeByte( getRateID( outputRates[ i ]));
		}
	}

	private void writeInputSpec( DataOutputStream dos, UGenInput inp )
	throws IOException
	{
		if( inp instanceof UGenChannel ) {
			final UGenChannel	uch			= (UGenChannel) inp;
			final int			synthIndex	= ugens.indexOf( uch.getUGen() );
			if( synthIndex == -1 ) throw new IOException( "UGen not listed in graph function : " + inp.dumpName() );
			dos.writeShort( synthIndex ); 
			dos.writeShort( uch.getChannel() );
		
		} else if( inp instanceof Constant ) {
			final int			constIndex	= constants.indexOf( inp );
			if( constIndex == -1 ) throw new IOException( "Constant not listed in synth def : " + inp.dumpName() );
			dos.writeShort( -1 );
			dos.writeShort( constIndex );
			
		} else {
			throw new IOException( "Illegal UGen input class " + inp.getClass().getName() );
		}
	}

	private static void writePascalString( DataOutputStream dos, String str )
	throws IOException
	{
		dos.writeByte( str.length() );
		dos.write( str.getBytes() );
	}

	/**
	 *	Reads definitions from a synth def file.
	 *
	 *	@param	path	the location of the synth def file
	 *					such as a local harddisk or remote server file
	 *	@return	an array of all definitions found in the file
	 *
	 *	@throws	IOException	if a read error occurs, if the
	 *						file has not a valid synth def
	 *						format or if the synth def file
	 *						version is unsupported (greater than <code>SCFG_VERSION</code>)
	 */
	public static SynthDef[] readDefFile( URL path )
	throws IOException
	{
		final InputStream is = path.openStream();
		
		try {
			return SynthDef.readDefFile( is ); 
		}
		finally {
			is.close();
		}
	}
	
	/**
	 *	Reads definitions from a synth def file.
	 *
	 *	@param	path		the location of the synth def file
	 *	@return	an array of all definitions found in the file
	 *
	 *	@throws	IOException	if a read error occurs, if the
	 *						file has not a valid synth def
	 *						format or if the synth def file
	 *						version is unsupported (greater than <code>SCFG_VERSION</code>)
	 */
	public static SynthDef[] readDefFile( File path ) 
	throws IOException
	{
		final InputStream is = new FileInputStream( path );
		
		try {
			return SynthDef.readDefFile( is );
		}
		finally {
			is.close();
		}
	}
	
	/**
	 *	Reads definitions from an input stream
	 *	(such as a harddisk file or memory buffer).
	 *
	 *	@param	is		the stream to read from
	 *	@return	an array of all definitions found in the stream
	 *
	 *	@throws	IOException	if a read error occurs, if the
	 *						stream has not a valid synth def
	 *						format or if the synth def file
	 *						version is unsupported (greater than <code>SCFG_VERSION</code>)
	 */
	public static SynthDef[] readDefFile( InputStream is )
	throws IOException
	{
		final DataInputStream	dis		= new DataInputStream( new BufferedInputStream( is ));
		final int				version;
		final int				numDefs;
		final SynthDef[]		defs;
		
		if( dis.readInt() != SCGF_MAGIC ) throw new IOException( "Not a SynthDef SCgf file" );
		version = dis.readInt();
		if( version > 1 ) throw new IOException( "Unknown SynthDef file format version : " + version );
		
		numDefs = dis.readShort();
		defs	= new SynthDef[ numDefs ];
		
		for( int i = 0; i < numDefs; i++ ) {
			defs[ i ] = read( dis );
		}
		
		return defs;
	}

	/**
	 *	Reads a single <code>SynthDef</code> from an input stream
	 *	(such as a harddisk file or memory buffer). Please refer
	 *	to the SuperCollider document <code>Synth-Definition-File-Format.rtf</code>
	 *	to read how a synth def is constructed
	 *	(read the paragraph &quot;a synth-definition is :&quot;).
	 *	This assumes synth def file format version 1 as used
	 *	by SuperCollider as of september 2005.
	 *
	 *	@param	is		the stream to read from with the current read
	 *					position placed at the start of a new synth def
	 *	@return	the decoded synth def
	 *
	 *	@throws	IOException	if a read error occurs
	 */
	public static SynthDef read( InputStream is )
	throws IOException
	{
		return read( new DataInputStream( new BufferedInputStream( is )));
	}
	
	private static SynthDef read( DataInputStream dis )
	throws IOException
	{
		final SynthDef		def				= new SynthDef( readPascalString( dis ));
		final int			numConstants;
		final int			numParams;
		final int			numParamNames;
		final int			numUGens;
		final Constant[]	constants;
		final ControlDesc[]	controlDescs;
		final String[]		paramName;
		final float[]		controlDefaults;
		UGen				ugen;
		String				str;
	
//		UGen.buildSynthDef	= def;
		
		numConstants		= dis.readShort();
		constants			= new Constant[ numConstants ];
		
//		inputs.clear();
//		outputs.clear();
		
		for( int i = 0; i < numConstants; i++ ) {
			constants[ i ]	= new Constant( dis.readFloat() );
		}
		
		numParams			= dis.readShort();
		controlDefaults		= new float[ numParams ];
		controlDescs		= new ControlDesc[ numParams ];
		
		for( int i = 0; i < numParams; i++ ) {
//			def.controls[ i ]		= dis.readFloat();
			controlDefaults[ i ]	= dis.readFloat();
			
//			this.controlDescs[ i ]	= new ControlDesc( null, i, UGen.UNKNOWN_RATE, def.controls[ i ]);	// XXX
		}
		
		numParamNames		= dis.readShort();
		paramName			= new String[ numParamNames ];
		for( int i = 0; i < numParamNames; i++ ) {
			str								= readPascalString( dis );
			paramName[ dis.readShort() ]	= str;
//			this.controlDescs[ dis.readShort() ].name = str;
//System.err.println( "name[ "+x+" ] == "+str );
		}
		
		numUGens			= dis.readShort();
		for( int i = 0; i < numUGens; i++ ) {
			ugen = readUGenSpec( dis, def, paramName, constants );
			// NOTE : the ugen is always of class UGen, even
			// if it's a control. therefore, we have to register
			// the controlDescs manually, while a user instantiated
			// Control will behave differently!
			def.addUGen( ugen );
	//		ugen.addToSynth();
	//		ugen.initFinished();
			
	//		if( ugen instanceof ControlUGen ) {
//			if( ugen.getName().equals( "Control" )) {
			if( ctrlUGensSet.contains( ugen.getName() )) {
//System.err.println( "special index "+ugen.getSpecialIndex()+"; numoutputs "+ugen.getNumOutputs()+"; controlDescs.length"+controlDescs.length+"; paramName.length "+paramName.length+"; controlDefaults.length "+controlDefaults.length );
				for( int k = 0, j = ugen.getSpecialIndex(); k < ugen.getNumOutputs(); k++, j++ ) {
//					controlDescs[ j ] = new ControlDesc( paramName[ j ], j, ugen.getRate(), def.controls[ j ]);
					controlDescs[ j ] = new ControlDesc( j < paramName.length ? paramName[ j ] : "?", ugen.getRate(), controlDefaults[ j ]);
				}
			}
		}

		for( int i = 0; i < controlDescs.length; i++ ) {
// this is a bug in sclang
//			if( controlDescs[ i ].getName() != null ) def.controlDescs.add( controlDescs[ i ]);
			if( controlDescs[ i ] != null ) {
				if( controlDescs[ i ].getName() != null ) {
					def.addControlDesc( controlDescs[ i ]);
				} else {
					System.err.println( "Warning: unnamed control " + i + " (" + paramName[i] + ") dropped." );
				}
			} else {
				System.err.println( "Warning: unreferenced control " + i + " (" + paramName[i] + ") dropped." );
			}
		}
		for( int i = 0; i < constants.length; i++ ) {
// this is a bug in sclang
//			def.constants.put( constants[ i ], new Integer( i ));
			def.addConstant( constants[ i ]);
		}

		// if( !keepDef ) {
		//		def			= null;
		//		constants	= null;
		// }
		// makeMsgFunc();
//		UGen.buildSynthDef = null;

		return def;
	}

	private static UGen readUGenSpec( DataInputStream dis, SynthDef def, String[] paramName, Constant[] constants )
	throws IOException
	{
		final String		name			= readPascalString( dis );
		final Object		rate			= RATES[ dis.readByte() ];
		final int			numInputs		= dis.readShort();
		final int			numOutputs		= dis.readShort();
		// specialIndex: this value is used by some unit generators for a special purpose. For example, UnaryOpUGen
		// and BinaryOpUGen use it to indicate which operator to perform. If not used it should be set to zero
		final int			specialIndex	= dis.readShort();
		final Object[]		outputRates		= new Object[ numOutputs ];
		final UGenInput[]	ugenInputs		= new UGenInput[ numInputs ];

		int					ugenIndex, outputIndex;
		final UGen			ugen;
		
		for( int i = 0; i < numInputs; i++ ) {
			ugenIndex	= dis.readShort();
			outputIndex	= dis.readShort();
			
			if( ugenIndex < 0 ) {	// constant input
				ugenInputs[ i ]	= constants[ outputIndex ];
			} else {				// input from another ugen's output
//				if( ugen instanceof MultiOutUGen ) {
//					ugenInputs[ i ]	= ((MultiOutUGen) ugen).channels[ outputIndex ];
//				} else {
//					ugenInputs[ i ]	= ugen;
//				}
				ugenInputs[ i ]	= new UGenChannel( (UGen) def.ugens.get( ugenIndex ), outputIndex );
			}
		}

		for( int i = 0; i < numOutputs; i++ ) {
			outputRates[ i ] = RATES[ dis.readByte() ];
		}

		ugen	= new UGen( name, rate, outputRates, ugenInputs, specialIndex );
		return ugen;
	}

	private static String readPascalString( DataInputStream dis )
	throws IOException
	{
		final byte		numChars	= dis.readByte();
		final byte[]	buf			= new byte[ numChars ];
		
		dis.readFully( buf );
		
		return new String( buf );
	}

// ---------------- internal classes ----------------

	private static class UGenEnv
	{
		protected final UGen			ugen;
		protected final List			collAnte;
		protected final List			collDe;
		protected int					synthIndex;
		
		protected UGenEnv( UGen ugen, int synthIndex )
		{
			this.ugen		= ugen;
			this.synthIndex	= synthIndex;
			collAnte		= new ArrayList( ugen.getNumInputs() );
			collDe			= new ArrayList();
		}
	}

	private static class SynthIndexComparator
	implements Comparator
	{
		protected SynthIndexComparator() { /* empty */ }
		
		public int compare( Object env1, Object env2 )
		{
			return( ((UGenEnv) env1).synthIndex - ((UGenEnv) env2).synthIndex );
		}
	}
}
