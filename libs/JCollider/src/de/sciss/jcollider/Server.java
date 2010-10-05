/*
 *  Server.java
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
 *		04-Aug-05	created
 *		11-Aug-05	getClientAddr() added
 *		28-Aug-05	correctly determines isLocal
 *		04-Sep-05	uses OSCTransmitter
 *		24-Sep-05	boot-completion actions are executed before
 *					any server event notification is fired
 *		07-Oct-05	sendMsgSync recognized /fail replies ; added sendBundleSync()
 *		24-Jul-06	uses updated server options with variable block allocator class ; alive thread robust against unresponsive server
 *					; added extended sendMsgSync and sendBundleSync methods ; responders recreated after quit
 *		01-Oct-06	uses new NetUtil and allows for TCP mode
 *		25-Aug-08	OSC buffer size increased to 64K
 */

package de.sciss.jcollider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.net.OSCBundle;
import de.sciss.net.OSCChannel;
import de.sciss.net.OSCClient;
import de.sciss.net.OSCMessage;

/**
 *	Closely following SClang's server class,
 *	this is the client side <strong>representation</strong>
 *	of a supercollider server.
 *	<P>
 *	<B>As of v0.29</B>, the server must be started explicitly by calling <code>start()</code>
 *	unless the server is booted. The call to <code>start()</code> tells the <code>OSCClient</code> to
 *	start listening for incoming messages. For TCP connections, this will try to connect to the server.
 *
 *	@todo		the behaviour should be changed to the server
 *				representation automatically sending dumpOSC and
 *				notification status and initializing the tree,
 *				whenever the running status becomes true!
 *
 *	@warning	don't rely on the default group's node ID. it is
 *				planned to use a client specific ID in the next
 *				version, so JCollider and SClang can peacefully
 *				coexist on the same server without killing each
 *				other's groups (which happens at the moment if
 *				you press cmd+period in sclang)
 *
 *	@synchronization	unless specified, all methods should be
 *						regarded thread safe
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 25-Aug-08
 */
public class Server
implements Constants, EventManager.Processor
{
	/**
	 *	We just use the same default scsynth
	 *	server port (57110) here as assumed by sclang
	 */
	public static final int					DEFAULT_PORT		= 57110;

	private static final Set				setServers			= Collections.synchronizedSet( new HashSet() );
	private static final Map				mapServerNames		= Collections.synchronizedMap( new HashMap() );

	private final String					name;
	private final InetSocketAddress			addr;
	private final ServerOptions				options;
	private final int						clientID;
	
	private final boolean					isLocal;
	private volatile boolean				serverRunning		= false;
	protected volatile boolean				serverBooting		= false;
	private boolean							notified			= true;
	
	protected Buffer[]						bufferArray;
	protected OSCResponderNode				bufInfoResponder;
	protected boolean						waitingForBufInfo;
	protected int							waitingBufs;
	
	private NodeIDAllocator					nodeAllocator;
	private BlockAllocator					controlBusAllocator, audioBusAllocator, bufferAllocator;

	private static String					program				= "scsynth";
	private static boolean					inform				= true;
	protected static volatile PrintStream	printStream			= System.err;
	
//	protected static final Timer			appClock			= new Timer();	// nice work-around, eh?
	private StatusWatcher					aliveThread			= null;
	
	// OSC communication
	protected final OSCClient				c;
	private final OSCMultiResponder			multi;
//	private final OSCTransmitter			trns;
//	private final DatagramChannel			dch;
	private int								dumpMode			= kDumpOff;

	private final Group						defaultGroup;

	// status watcher
	protected final Status					status				= new Status();

	// messaging
	private final EventManager				em					= new EventManager( this );
	private final List						collBootCompletion	= new ArrayList();
	
	protected BootThread					bootThread			= null;

	private static final OSCMessage			statusMsg			= new OSCMessage( "/status" );

	protected final Server					enc_this			= this;
	
	protected final Object					syncBootThread		= new Object();

	/**
	 *	Creates a new <code>Server</code> representation
	 *	object. Note that this will neither &quot;create&quot;
	 *	a server, nor boot one, nor contact one. It simply
	 *	establishes means of communication to a server
	 *	at the given address. The caller is responsible for
	 *	contacting the server and, before starting to create
	 *	nodes and such, to initialize the default tree
	 *	using <code>initTree</code>, and to ping the server
	 *	using <code>startAliveThread</code> if one wishes
	 *	to be informed about server boots and quits.
	 *
	 *	@param	name		the name of the server. this arbitrary but
	 *						must be unique since a set of created servers
	 *						is internally maintained. currently no check
	 *						on this uniqueness is performed. this is also
	 *						the name of the OSC thread and the one shown
	 *						in a server panel
	 *	@param	addr		the address at which scsynth responds. as
	 *						of this writing, the communication will use
	 *						the UDP protocol
	 *	@param	options		an instance of <code>ServerOptions</code>
	 *						which needn't reflect the actual server settings
	 *						when contacting a remote server, but which are
	 *						used when booting a local server.
	 *	@param	clientID	this is a concept taken from sclang, something about
	 *						which the server knows nothing. it merely conditions
	 *						the node ID allocator to occupy different value
	 *						ranges for up to 31 different clients, so they won't
	 *						conflict. beware, that as of this writing, there is
	 *						no mechanism to coordinate the use of buffers and
	 *						busses between different concurrent clients.
	 *						the ID can be something between <code>0</code> (default) and
	 *						<code>NodeIDAllocator.getUserMax()</code>
	 *
	 *	@throws	IOException	if a networking error occurs
	 *
	 *	@see	#startAliveThread()
	 *	@see	#boot()
	 *	@see	#initTree()
	 */
	public Server( String name, InetSocketAddress addr, ServerOptions options, int clientID )
	throws IOException
	{
		this.name		= name;
		this.addr		= addr;
		this.options	= options;
		this.clientID	= clientID;
		
// this doesn't work XXX
//		isLocal			= addr.getAddress().isAnyLocalAddress();
//		isLocal			= true;
		final InetAddress host = addr.getAddress();
		if( host == null ) throw new IOException( "Server.new : unresolved network address " + addr );
		isLocal			= host.isLoopbackAddress() || host.equals( InetAddress.getLocalHost() );

		Server.mapServerNames.put( name, this );
		Server.setServers.add( this );

//		defaultGroup	= Group.basicNew( this, 1 );	// XXX should be changed
		defaultGroup	= Group.basicNew( this, 0 );

		try {
			// its crucial to create a multi responder with our server's
			// address here, because we need to use it's channel for
			// sendMsg/sendBundle in order for responders to get the replies!!
			
			c			= OSCClient.newUsing( options.getProtocol(), 0, host.isLoopbackAddress() );
//			c.start();
			c.setBufferSize( 0x10000 );
			multi		= new OSCMultiResponder( c );
//			dch			= (DatagramChannel) multi.getChannel(); // XXX
//			trns		= new OSCTransmitter( dch, addr );
//			trns		= OSCTransmitter.newUsing( dch );
			c.setTarget( addr );
			
			createNewAllocators();
			resetBufferAutoInfo();
		}
		catch( IOException e1 ) {
			Server.mapServerNames.remove( name );
			Server.setServers.remove( this );
			throw e1;
		}
			
		// ---- listeners and processors ----
		
// XXX there is no class level listener registration
// at the moment, also it's not too useful anyway
//		Server.changed(\serverAdded, this);
	}
	
//	// when SC quits, the DatagramChannel is closed
//	// ; if we continue to use it, we might end up with
//	// java.nio.channels.ClosedChannelException s.
//	// hence, we re-create the responders
//	// ; NO WE DON'T XXX
//	private void initCommunication()
//	throws IOException
//	{
////System.err.println( "initCommunication" );
//		if( multi != null ) {
////System.err.println( "  dipose "+multi.hashCode() );
//			multi.dispose();
////			multi	= null;
//		}
////System.err.println( "  recreate multi" );
//		multi		= new OSCMultiResponder( addr );
//		dch			= multi.getChannel();
////System.err.println( "  recreate trns; multi = "+multi.hashCode() );
//		trns		= new OSCTransmitter( dch, addr );
////System.err.println( "  recreate done" );
//		
//		createNewAllocators();
//		resetBufferAutoInfo();
//	}

	/**
	 *	Creates a server representation
	 *	for the default client (<code>0</code>).
	 */
	public Server( String name, InetSocketAddress addr, ServerOptions options )
	throws IOException
	{
		this( name, addr, options, 0 );
	}

	/**
	 *	Creates a server representation
	 *	for the default client (<code>0</code>),
	 *	using default options.
	 *
	 *	@see	ServerOptions#ServerOptions()
	 */
	public Server( String name, InetSocketAddress addr )
	throws IOException
	{
		this( name, addr, new ServerOptions() );
	}

	/**
	 *	Creates a representation for a
	 *	server which listens at the loopback
	 *	address (<code>127.0.0.1:57110</code>).
	 */
	public Server( String name )
	throws IOException
	{
		this( name, new InetSocketAddress( "127.0.0.1", DEFAULT_PORT ));
	}
	
	/**
	 *	Returns the address which is
	 *	assumed to be the one at which
	 *	the server listens
	 *
	 *	@return	the address (IP plus port)
	 *			which was used to instantiate
	 *			the server representation
	 */
	public InetSocketAddress getAddr()
	{
		return addr;
	}
	
	protected OSCMultiResponder getMultiResponder()
	{
		return multi;
	}
	
	public void start()
	throws IOException
	{
		c.start();
	}
	
	/**
	 *	Returns the socket address that the client
	 *	(that's us) is using to send messages to the server
	 *
	 *	@return	the socket address which is used
	 *			to send messages to the server
	 *			(i.e. the sender of <code>sendMsg</code> and <code>sendBundle</code>)
	 *
	 *	@see	AbstractOSCCommunicator#getChannel()
	 */
//	public InetSocketAddress getClientAddr()
//	{
//		final DatagramSocket ds = ((DatagramChannel) multi.getChannel()).socket();	// XXX
//	
//		return new InetSocketAddress( ds.getLocalAddress(), ds.getLocalPort() );
//	}

	/**
	 *	Queries the server representation's name
	 *
	 *	@return	the name which was used to
	 *			instantiate the <code>Server</code> class
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 *	Queries the options for booting the server.
	 *	The returned object is not a copy, and it's
	 *	mutable, so any changes made to the returned
	 *	objects will become effective when calling
	 *	the <code>boot</code> method.
	 *
	 *	@return	the server options describing the
	 *			options to pass to scsynth when booting it
	 */
	public ServerOptions getOptions()
	{
		return options;
	}
	
	/**
	 *	Queries the client ID used
	 *	for allocating node IDs.
	 *
	 *	@return	the ID used in the <code>Server</code>
	 *			constructor
	 */
	public int getClientID()
	{
		return clientID;
	}
	
	/**
	 *	Queries the application path
	 *	to scsynth.
	 *
	 *	@return	the pathname stored for
	 *			the location of scsynth. this is the 
	 *			path which will be used when booting
	 *			the server. the default value being
	 *			the cwd will probably not work for your
	 *			application, therefore it's advised to set
	 *			the path using <code>setProgram</code> before
	 *			trying to boot
	 */
	public static String getProgram()
	{
		return program;
	}

	/**
	 *	Changes the path at which scsynth is supposed
	 *	to located on the local harddisk. This is the
	 *	path used to boot a local server. Note that this
	 *	path is stored globally (for all server instances).
	 *	For simplicity, this is a string and not a <code>File</code>
	 *	object. To convert from a file, use
	 *	<code>File.getAbsolutePath()</code>.
	 *
	 *	@param	program	full pathname to scsynth(.exe)
	 *
	 *	@see	File#getAbsolutePath()
	 */
	public static void setProgram( String program )
	{
		Server.program	= program;
	}

	/**
	 *	Turns on or off verbose messaging
	 *	of the server representation. This has
	 *	little effect at the moment, but when turned
	 *	on, a few more messages will be printed to the
	 *	console.
	 *
	 *	@param	onOff	<code>true</code> means, be
	 *					a bit more verbose
	 */
	public static void setInform( boolean onOff )
	{
		Server.inform = onOff;
	}

	/**
	 *	Changes the stream to which messages are
	 *	printed. By default, <code>Server</code> will
	 *	print on <code>System.err</code>. This is
	 *	a global method and affects all server instances.
	 *	This method is particularly important because
	 *	it's value will be read by the <code>boot</code>
	 *	method to determine the print stream to which
	 *	a locally booted server will print. Changing
	 *	the stream after the server was booted, will not
	 *	affect it's print out at all, so that needs to
	 *	be done in advance.
	 *
	 *	@param	printStream	the new print stream to use
	 */
	public static void setPrintStream( PrintStream printStream )
	{
		Server.printStream = printStream;
	}
	
	/**
	 *	Queries the currently used print stream for
	 *	outputting messages
	 *
	 *	@return	the print stream used for message printout
	 *			and for the locally booted server
	 */
	public static PrintStream getPrintStream()
	{
		return printStream;
	}
	
	/**
	 *	Queries the booting state
	 *
	 *	@return	<code>true</code> if the local server is being booted
	 *			at the moment, <code>false</code> otherwise.
	 *
	 *	@see	#isRunning()
	 */
	public boolean isBooting()
	{
		return serverBooting;
	}

	protected void setBooting( boolean serverBooting )
	{
		this.serverBooting = serverBooting;
	}

	/**
	 *	Queries the running state. To become automatically
	 *	informed about changes of this state, register a
	 *	listener using the <code>addListener</code> method.
	 *
	 *	@return	<code>true</code> if a connection is established
	 *			to a supercollider server. <code>false</code> if the
	 *			server is not running or communication broke down.
	 *			note that this value will only be valid, when the
	 *			ping-thread is running, which is either started
	 *			automatically when booting the server (<code>boot</code>)
	 *			or manually by invoking <code>startAliveThread()</code>.
	 *
	 *	@todo	a future version may incorporate automatic service
	 *			discovery.
	 *
	 *	@see	#addListener( ServerListener )
	 *	@see	#boot()
	 *	@see	#startAliveThread()
	 */
	public boolean isRunning()
	{
		return serverRunning;
	}

	protected void setRunning( boolean serverRunning )
	{
//System.err.println( "ici : "+serverRunning );
		synchronized( syncBootThread ) {
			if( this.serverRunning != serverRunning ) {
				this.serverRunning = serverRunning;
				if( !serverRunning ) {
	//				recordNode = nil;
					changed( ServerEvent.STOPPED );
					if( bootThread != null ) {
						try {
							bootThread.keepScRunning = false;
							syncBootThread.wait( 4000 );
						}
						catch( InterruptedException e1 ) { /* empty */ }
					}
				} else {
	//System.err.println( "ici" );
					while( !collBootCompletion.isEmpty() ) {
						((CompletionAction) collBootCompletion.remove( 0 )).completion( this );
					}
					changed( ServerEvent.RUNNING );
				}
			}
		}
	}

	private void createNewAllocators()
	{
		nodeAllocator		= new NodeIDAllocator( getClientID() );
		controlBusAllocator = options.getBlockAllocFactory().create( options.getNumControlBusChannels() );
		audioBusAllocator	= options.getBlockAllocFactory().create( options.getNumAudioBusChannels(), options.getFirstPrivateBus() );
		bufferAllocator		= options.getBlockAllocFactory().create( options.getNumBuffers() );
	}
	
	/**
	 *	Automatic buffer ID allocator
	 *	for package internal use only.
	 */
	protected BlockAllocator getBufferAllocator()
	{
		return bufferAllocator;
	}

	/**
	 *	Automatic audio bus allocator
	 *	for package internal use only.
	 */
	protected BlockAllocator getAudioBusAllocator()
	{
		return audioBusAllocator;
	}
	
	/**
	 *	Automatic control bus allocator
	 *	for package internal use only.
	 */
	protected BlockAllocator getControlBusAllocator()
	{
		return controlBusAllocator;
	}
	
	/**
	 *	Queries the server's (audio) sampling rate.
	 *	This is the <strong>nominal</strong> rate
	 *	as returned by the <code>status.reply</code>
	 *	message from the server. This is only valid
	 *	when the server is running and the ping-thread
	 *	was started. To be automatically informed about
	 *	status changes, you can register a listener
	 *	using <code>addListener</code>.
	 *
	 *	@return	the nominal (i.e. constant) sampling rate
	 *			of the server
	 *
	 *	@see	#addListener( ServerListener )
	 *	@see	#startAliveThread()
	 *	@see	#getStatus()
	 */
	public double getSampleRate()
	{
		return status.sampleRate;
	}

	/**
	 *	Queries the latest reported server status.
	 *	The returned object is a snapshot of what
	 *	the server last send using a <code>status.reply</code>
	 *	message. It's only valid when the server is
	 *	running and the ping-thread is running.
	 *
	 *	@return	the <code>Status</code> with fields
	 *			for number of UGens, Nodes etc.
	 */
	public Status getStatus()
	{
		return Status.copyFrom( status );
	}
	
	/**
	 *	Queries the current OSC dumping mode.
	 *	Note that the value reflects just what
	 *	<strong>we</strong> know about the status.
	 *	When the server is booted, a dump message
	 *	is automatically send, so the reported
	 *	value should be considered correct, until
	 *	a different client sends a dumpOSC message
	 *	or the server is terminated and restarted.
	 *
	 *	@return	the mode at which the server
	 *			dumps OSC messages it receives. this
	 *			can be either of <code>kDumpOff</code>,
	 *			<code>kDumpText</code>, <code>kDumpHex</code>
	 *			or <code>kDumpBoth</code>.
	 *
	 *	@see	Constants#kDumpText
	 */
	public int getDumpMode()
	{
		return dumpMode;
	}
	
	/**
	 *	Returns the &quot;default&quot; group
	 *	of the server. This is the one used as 
	 *	target when no explicit target is specified
	 *	for node creation, such as in
	 *	<code>new Synth( defName, argNames, argValues, null )</code>.
	 *
	 *	@return		the default group. the value is
	 *				only valid (i.e. representing a group
	 *				that really exists), when the server representation
	 *				has initialized it's node tree, which is done
	 *				automatically after booting, or by calling
	 *				<code>initTree</code> explicitly.
	 *
	 *	@see		#initTree()
	 *
	 *	@warning	do not rely on a particular
	 *				group returned here, since it is
	 *				likely that it will change in a future version
	 */
	public Group getDefaultGroup()
	{
		return defaultGroup;
	}

	/**
	 *	Returns a group representation of the
	 *	server, which at the moment is a synonym for
	 *	<code>getDefaultGroup</code>. This will change in
	 *	one of the next version to return the root node
	 *	(ID 0) however!!
	 */
	public Group asTarget()
	{
		return defaultGroup;
	}

	protected static void inform( String txt )
	{
		if( inform ) printStream.println( txt );
	}

	/**
	 *	Starts booting a local server
	 *	and establishes the ping-thread when done
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void boot()
	throws IOException
	{
		boot( true );
	}

	/**
	 *	Starts booting a local server. A local
	 *	server can only be booted, when the server's address
	 *	is local. Be sure to set the appropriate application
	 *	path using <code>setProgram</code> before calling this method.
	 *	<p>
	 *	This method does nothing, when
	 *	the booting process is assumed to be ongoing already,
	 *	or when the server has already been successfully booted.
	 *	<p>
	 *	When booting is finished, the ping-thread is established
	 *	depending on the <code>startAliveThread</code> flag,
	 *	the node tree is initialized (<code>initTree</code>),
	 *	and all actions registered through <code>addDoWhenBooted</code>
	 *	are executed. Independantly, when the ping-thread receives
	 *	the first reply, the running status is updated, hence
	 *	informing all listeners registered using <code>addListener</code>.
	 *	
	 *	@param	startAliveThread	<code>true</code> to start a ping-thread
	 *								once the server was booted. this is required
	 *								if you wish to read the running status using
	 *								<code>isRunning()</code> or using listeners
	 *
	 *	@throws	IOException				if an error occurs when starting server,
	 *									particularly if the path to the server application
	 *									is wrong.
	 *	@throws	IllegalStateException	if you try to boot a remote server
	 *
	 *	@see	#setProgram( String )
	 *	@see	#isLocal()
	 *	@see	#isBooting()
	 *	@see	#isRunning()
	 *	@see	Server#addDoWhenBooted( Server.CompletionAction )
	 *	@see	#startAliveThread()
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void boot( boolean startAliveThread )
	throws IOException
	{
		if( isRunning() ) {
			printStream.println( "server already running" );
			return;
		}
		if( isBooting() ) {
			printStream.println( "server already booting" );
			return;
		}
		
		if( !isLocal ) throw new IllegalStateException( "Server.boot() : only allowed for local servers!" );

		final CompletionAction whenBooted = new CompletionAction() {
			public void completion( Server s )
			{
				try {
					s.setBooting( false );
					if( s.getDumpMode() != kDumpOff ) {
						s.dumpOSC( s.getDumpMode() );
					}
					if( s.isNotified() ) {
						Server.inform( "notification is on" );
						s.notify( true );
					} else {
						Server.inform( "notification is off" );
					}
					s.initTree();	// XXX inefficient since it re-created the node allocator
				}
				catch( IOException e1 ) {
					printError( "Server.boot", e1 );
				}
			}
		};

		setBooting( true );
		try {
//			if( startAliveThread ) startAliveThread();
			
			// XXX inefficient since it was created already in constructor, should use reset instead
			// (serverOptions is immutable here)
			createNewAllocators();
			resetBufferAutoInfo();
			
			addDoWhenBooted( whenBooted );
			bootServerApp( startAliveThread );
		}
		catch( IOException e1 ) {
			removeDoWhenBooted( whenBooted );
			try {
				stopAliveThread();
			}
			catch( IOException e2 ) {
				printError( "Server.boot", e2 );
			}
			setBooting( false );
			throw e1;
		}
	}
	
	/**
	 *	Sends a message to the server
	 *	requesting OSC dump (text format).
	 *
	 *	@throws	IOException	if the message failed to be sent
	 */
	public void dumpOSC()
	throws IOException
	{
		dumpOSC( kDumpText );
	}

	/**
	 *	Sends a message to the server
	 *	requesting OSC dumping being turned on or off
	 *
	 *	@param	dumpMode	either of <code>kDumpOff</code> (do not dump),
	 *						<code>kDumpText</code> (dump text format),
	 *						<code>kDumpHex</code> (hexadecimal printout),
	 *						<code>kDumpBoth</code> (both text + hex)
	 *
	 *	@see	Constants#kDumpText
	 *
	 *	@throws	IOException	if the message failed to be sent
	 */
	public void dumpOSC( int dumpMode )
	throws IOException
	{
		sendMsg( dumpOSCMsg( dumpMode ));
	}
	
	/**
	 *	Creates the OSC message which will ask the
	 *	server to turn on or off OSC dumping
	 *
	 *	@param	dumpMode	see <code>dumpOSC( int )</code> for details
	 *
	 *	@see	#dumpOSC( int )
	 */
	public OSCMessage dumpOSCMsg( int dumpMode )
	{
		this.dumpMode = dumpMode;
		return new OSCMessage( "/dumpOSC", new Object[] { new Integer( dumpMode )});
	}

	/**
	 *	Changes the way incoming messages are dumped
	 *	to the console. By default incoming messages are not
	 *	dumped. Incoming messages are those received
	 *	by the client from the server, before they
	 *	get delivered to registered <code>OSCResponderNode</code>s.
	 *
	 *	@param	dumpMode	see <code>dumpOSC( int )</code> for details
	 *
	 *	@see	#dumpOSC( int )
	 */
	public void dumpIncomingOSC( int dumpMode )
	{
//		multi.dumpOSC( dumpMode, printStream );
		c.dumpIncomingOSC( dumpMode, printStream );
	}

	/**
	 *	Changes the way outgoing messages are dumped
	 *	to the console. By default outgoing messages are not
	 *	dumped. Outgoing messages are those send via
	 *	<code>sendMsg</code> or <code>sendBundle</code>.
	 *
	 *	@param	dumpMode	see <code>dumpOSC( int )</code> for details
	 *
	 *	@see	#dumpOSC( int )
	 */
	public void dumpOutgoingOSC( int dumpMode )
	{
//		trns.dumpOSC( dumpMode, printStream );
		c.dumpOutgoingOSC( dumpMode, printStream );
	}
	
	/**
	 *	Sends a message to the server
	 *	requesting to be notified about server actions
	 *	such as nodes being added and deleted. This
	 *	is also required to receive <code>/tr</code>
	 *	messages from a <code>sendTrig</code> UGen.
	 *	The status of the notification flag is saved
	 *	internally, so when a local server is booted
	 *	and notification was turned on, a new
	 *	<code>/notify</code> message is sent.
	 *	<p>
	 *	By default (when creating a new instance of
	 *	<code>Server</code>), the notification flag
	 *	is <code>true</code>.
	 *
	 *	@param	notified	<code>true</code> to turn notification on,
	 *						<code>false</code> to turn it off
	 *
	 *	@warning	if you boot a local server, the flag
	 *				is sent to the server. However,
	 *				if you wish to contact a server manually, you
	 *				will have to call this method explicitly. In
	 *				a future version this behaviour will change,
	 *				so the server representation automatically
	 *				sends dumpOSC and notification flag as well
	 *				as tree initialization, whenever the server
	 *				becomes available.
	 *
	 *	@throws	IOException	if the message failed to be sent
	 */
	public void notify( boolean notified )
	throws IOException
	{
		this.notified = notified;
		sendMsg( new OSCMessage( "/notify", new Object[] { new Integer( notified ? 1 : 0 )}));
	}
	
	/**
	 *	Queries the server notification status.
	 *	By default, notification is turned on.
	 *	However, the flag represents the true
	 *	notification state only, when it was send
	 *	to the server, which happens after a local
	 *	boot but not when simply starting the ping-thread!
	 *	this will change in a future version.
	 *
	 *	@return	<code>true</code> if notification is turned on.
	 */
	public boolean isNotified()
	{
		return notified;
	}
	
	/**
	 *	After the server has been contacted,
	 *	calling this method will create the default group.
	 *	This is automatically called after booting,
	 *	but not automatically when starting the ping-thread
	 *	manually. 
	 *	this behaviour will change in a future version.
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void initTree()
	throws IOException
	{
		nodeAllocator = new NodeIDAllocator( getClientID() );	
//		sendMsg( new OSCMessage( "/g_new", new Object[] { new Integer( 1 )}));
	}
	
	/**
	 *	Registers an action to be executed
	 *	after the boot process is complete.
	 *	Often it may be more convenient to simply
	 *	add a server listener.
	 *
	 *	@param	action	action to be executed,
	 *					when the server running status
	 *					becomes true after the boot process
	 */
	public void addDoWhenBooted( CompletionAction action )
	{
		collBootCompletion.add( action );
	}

	/**
	 *	Unregisters an action from being executed
	 *	after the boot process. Note that the
	 *	action is automatically removed after it has
	 *	been executed, so you have to call this method
	 *	only if you wish to <strong>cancel</strong>
	 *	the action.
	 *
	 *	@param	action	action to be removed
	 */
	public void removeDoWhenBooted( CompletionAction action )
	{
		collBootCompletion.remove( action );
	}
	
	/**
	 *	Registers a listener to be informed about
	 *	server status changes. These changes include
	 *	the server starting to run (or more precisely
	 *	being successfully contact), the server being
	 *	stopped (or more precisely having lost contact),
	 *	and status changes, which are updated regularly
	 *	when the ping-thread was started.
	 *
	 *	@param	l		listener to be added
	 */
	public void addListener( ServerListener l )
	{
		em.addListener( l );
	}

	/**
	 *	Unregisters a listener from being informed about
	 *	server status changes.
	 *
	 *	@param	l		listener to be removed
	 */
	public void removeListener( ServerListener l )
	{
		em.removeListener( l );
	}
	
	protected void changed( int id )
	{
		em.dispatchEvent( new ServerEvent( this, id, System.currentTimeMillis(), this ));
	}

	private void bootServerApp( boolean startAliveThread )
	{
		final int				port		= getAddr().getPort();
		final List				cmdList		= getOptions().toOptionList( port );
		cmdList.add( 0, Server.program );
		final String[]			cmdArray	= ServerOptions.optionListToStringArray( cmdList );
	
		Server.inform( "Booting SuperCollider server at " + getOptions().getProtocol().toUpperCase() + " port " + port + " ..." );
//		bootThread							= new BootThread( this, cmdArray, getOptions().getEnvMap(), startAliveThread );
		synchronized( syncBootThread ) {
			bootThread						= new BootThread( this, cmdArray, startAliveThread );
		}
	}
	
	/**
	 *	Returns <code>true</code> if the
	 *	server has an address on the same
	 *	machine as the client.
	 */
	public boolean isLocal()
	{
		return isLocal;
	}

	/**
	 *	Returns <code>true</code> if the
	 *	server was locally booted by this client.
	 *
	 *	@return	<code>true</code> if we booted
	 *			the server ourselves. this suggests
	 *			that we are also responsible for shutting
	 *			it down when quitting the client
	 */
	public boolean didWeBootTheServer()
	{
		return( bootThread != null );
	}

	/**
	 *	Begins to ping the server in regular intervals
	 *	to detect a newly established connection, the
	 *	loss of the connection and the current server status.
	 *	By default, this thread is started after booting
	 *	the local server. In other cases, for the running
	 *	status to become valid and for the listeners to
	 *	be informed about server starts and stops, this
	 *	method needs to be called manually.
	 *	<p>
	 *	If the ping-thread is already running, this method does nothing.
	 *
	 *	@throws	IOException	if a networking error occurs
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void startAliveThread()
	throws IOException
	{
		startAliveThread( 2.0f, 0.7f, 4 );
	}
	
	/**
	 *	Starts the ping-thread with specified
	 *	initial delay and ping period.
	 *	<p>
	 *	If the ping-thread is already running, this method does nothing.
	 *
	 *	@param	delay	delay in seconds after which
	 *					the first ping is send (defaults to 2 seconds)
	 *	@param	period	period in seconds at which pings
	 *					are send (defaults to 0.7 seconds).
	 *					note that the behaviour of lost pings might
	 *					change in a future version. As of now, a lost
	 *					ping will result in server running status becoming
	 *					false and firing a corresponding server event to
	 *					registered listeners. The can be inappropriate
	 *					if the listener starts to dispose internals while
	 *					actually the server was just too slow and keeps
	 *					playing orphaned synths.
	 *
	 *	@throws	IOException	if a networking error occurs
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void startAliveThread( float delay, float period, int deathBounces )
	throws IOException
	{
		synchronized( syncBootThread ) {
			if( aliveThread == null ) {
				aliveThread = new StatusWatcher( delay, period, deathBounces );
				aliveThread.start();
			}
		}
	}
	
	/**
	 *	Stops the ping-thread.
	 *	Note that the thread is a demon. Therefore
	 *	it will not block quitting the java VM and will
	 *	automatically terminate <code>System.exit</code> is called.
	 *
	 *	@throws	IOException	if a networking error occurs
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void stopAliveThread()
	throws IOException
	{
		synchronized( syncBootThread ) {
			if( aliveThread != null ) {
				aliveThread.stop();
				aliveThread = null;
			}
		}
	}

//	private static void resumeThreads()
//	throws IOException
//	{
//		Server server;
//	
//		for( Iterator iter = setServers.iterator(); iter.hasNext(); ) {
//			server = (Server) iter.next();
//			server.stopAliveThread();
//			server.startAliveThread( 0.7f, 0.7f);
//		}
//	}

	protected void status()
	throws IOException
	{
		sendMsg( statusMsg );
	}

	/**
	 *	Sends an OSC message to the server
	 *
	 *	@param	msg	the message to send
	 *
	 *	@throws	IOException	if sending the message fails.
	 *						this can happen because of a network error,
	 *						because of a malformed message or because of
	 *						a buffer overflow (message exceeding 8K)
	 */
	public void sendMsg( OSCMessage msg )
	throws IOException
	{
//		trns.send( msg );
		c.send( msg );
	}

	/**
	 *	Sends an OSC bundle for scheduling to the server
	 *
	 *	@param	bndl	the bundle to send
	 *
	 *	@throws	IOException	if sending the bundle fails.
	 *						this can happen because of a network error,
	 *						because of a malformed bundle or its contained messages
	 *						or because of a buffer overflow (bundle exceeding 8K)
	 */
	public void sendBundle( OSCBundle bndl )
	throws IOException
	{
//		trns.send( bndl );
		c.send( bndl );
	}

	/**
	 *	Sends a message and waits for a corresponding <code>/done</code>
	 *	reply from the server.
	 *
	 *	@param	msg			the message to send
	 *	@param	timeout		the maximum amount of time in seconds to wait
	 *	@return				<code>true</code> if the successfull reply was
	 *						receivied within the timeout; <code>false</code> if
	 *						no reply was received in time or a <code>/fail</code>
	 *						message was received.
	 *	
	 *	@throws	IOException	if sending the message or receiving the reply fails
	 */
	public boolean sendMsgSync( OSCMessage msg, float timeout )
	throws IOException
	{
		final OSCMessage result = sendMsgSync( msg, "/done", "/fail", 0, msg.getName(), timeout );
		return( (result != null) && result.getName().equals( "/done" ));
	}

	/**
	 *	Sends a message and waits for a corresponding reply or failure message
	 *	from the server.
	 *
	 *	@param	msg			the message to send
	 *	@param	doneCmd		the OSC command with which the server replies upon success
	 *	@param	failCmd		the OSC command with which the server replies upon failure (can be <code>null</code>)
	 *	@param	doneArgIdx		the OSC reply message argument index to match
	 *	@param	doneArgMatch	the OSC reply message argument value to match
	 *	@param	timeout		the maximum amount of time in seconds to wait
	 *	@return				the reply message or <code>null</code> if
	 *						no reply was received in time
	 *	
	 *	@throws	IOException	if sending the message or receiving the reply fails
	 */
	public OSCMessage sendMsgSync( OSCMessage msg, String doneCmd, String failCmd, int doneArgIdx, Object doneArgMatch, float timeout )
	throws IOException
	{
		return sendMsgSync( msg, doneCmd, failCmd,
		                    new int[] { doneArgIdx }, new Object[] { doneArgMatch },
		                    new int[] { 0 }, new Object[] { msg.getName() },
		                    timeout );
	}
	
	/**
	 *	Sends a message and waits for a corresponding reply or failure message
	 *	from the server.
	 *
	 *	@param	msg				the message to send
	 *	@param	doneCmd			the OSC command with which the server replies upon success
	 *	@param	failCmd			the OSC command with which the server replies upon failure (can be <code>null</code>)
	 *	@param	doneArgIndices	the OSC reply message argument indices to match for success
	 *	@param	doneArgMatches	the OSC reply message argument values to match for success
	 *	@param	failArgIndices	the OSC reply message argument indices to match for failure
	 *	@param	failArgMatches	the OSC reply message argument values to match for failure
	 *	@param	timeout			the maximum amount of time in seconds to wait
	 *	@return					the reply message or <code>null</code> if
	 *							no reply was received in time
	 *	
	 *	@throws	IOException	if sending the message or receiving the reply fails
	 */
	public OSCMessage sendMsgSync( OSCMessage msg, String doneCmd, String failCmd,
								   int[] doneArgIndices, Object[] doneArgMatches,
								   int[] failArgIndices, Object[] failArgMatches,
								   float timeout )
	throws IOException
	{
		final SyncResponder	resp = new SyncResponder( doneCmd, failCmd, doneArgIndices, doneArgMatches, failArgIndices, failArgMatches );
		
		try {
			synchronized( resp ) {
				resp.add();
				sendMsg( msg );
				resp.wait( (long) (timeout * 1000) );
			}
		}
		catch( InterruptedException e1 ) { /* ignored */ }
		finally {
			resp.remove();
		}
		return resp.replyMsg;
	}

	/**
	 *	Sends a bundle and waits for a <code>/done</code>
	 *	reply for a given command name from the server.
	 *
	 *	@param	bndl		the bundle to send
	 *	@param	cmdName		to name of the message command to be replied to
	 *	@param	timeout		the maximum amount of time in seconds to wait
	 *	@return				<code>true</code> if the successfull reply was
	 *						receivied within the timeout; <code>false</code> if
	 *						no reply was received in time or a <code>/fail</code>
	 *						message was received.
	 *	
	 *	@throws	IOException	if sending the bundle or receiving the reply fails
	 */
	public boolean sendBundleSync( OSCBundle bndl, String cmdName, float timeout )
	throws IOException
	{
		final OSCMessage result = sendBundleSync( bndl, "/done", "/fail", 0, cmdName, timeout );
		return( (result != null) && result.getName().equals( "/done" ) );
	}
	
	/**
	 *	Sends a bundle and waits for a corresponding reply or failure message
	 *	from the server.
	 *
	 *	@param	bndl		the bundle to send
	 *	@param	doneCmd		the OSC command with which the server replies upon success
	 *	@param	failCmd		the OSC command with which the server replies upon failure (can be <code>null</code>)
	 *	@param	argIdx		the OSC reply message argument index to match
	 *	@param	argMatch	the OSC reply message argument value to match
	 *	@param	timeout		the maximum amount of time in seconds to wait
	 *	@return				the reply message or <code>null</code> if
	 *						no reply was received in time
	 *	
	 *	@throws	IOException	if sending the bundle or receiving the reply fails
	 */
	public OSCMessage sendBundleSync( OSCBundle bndl, String doneCmd, String failCmd, int argIdx, Object argMatch, float timeout )
	throws IOException
	{
		return sendBundleSync( bndl, doneCmd, failCmd, new int[] { argIdx }, new Object[] { argMatch },
		                       new int[ 0 ], new Object[ 0 ], timeout );
	}

	/**
	 *	Sends a bundle and waits for a corresponding reply or failure message
	 *	from the server.
	 *
	 *	@param	bndl		the bundle to send
	 *	@param	doneCmd		the OSC command with which the server replies upon success
	 *	@param	failCmd		the OSC command with which the server replies upon failure (can be <code>null</code>)
	 *	@param	doneArgIndices	the OSC reply message argument indices to match
	 *	@param	doneArgMatches	the OSC reply message argument values to match
	 *	@param	timeout		the maximum amount of time in seconds to wait
	 *	@return				the reply message or <code>null</code> if
	 *						no reply was received in time
	 *	
	 *	@throws	IOException	if sending the bundle or receiving the reply fails
	 */
	public OSCMessage sendBundleSync( OSCBundle bndl, String doneCmd, String failCmd,
									  int[] doneArgIndices, Object[] doneArgMatches,
									  int[] failArgIndices, Object[] failArgMatches,
									  float timeout )
	throws IOException
	{
		final SyncResponder	resp = new SyncResponder( doneCmd, failCmd, doneArgIndices, doneArgMatches, failArgIndices, failArgMatches );
		
		try {
			synchronized( resp ) {
				resp.add();
				sendBundle( bndl );
				resp.wait( (long) (timeout * 1000) );
			}
		}
		catch( InterruptedException e1 ) { /* ignored */ }
		finally {
			resp.remove();
		}
		return resp.replyMsg;
	}

	/**
	 *	Sends a <code>/sync</code> message to the server and waits for a
	 *	corresponding <code>/synced</code> reply.
	 *
	 *	@param	timeout		the maximum amount of time in seconds to wait
	 *	@return				<code>true</code> if the successfull reply was
	 *						receivied within the timeout.
	 *
	 *	@throws	IOException	if sending the message or receiving the reply fails
	 */
	public boolean sync( float timeout )
	throws IOException
	{
		return sync( null, timeout );
	}

	/**
	 *	Attaches a <code>/sync</code> message to the list of messages in a
	 *	bundle and sends the bundle to server, waiting for a
	 *	corresponding <code>/synced</code> reply.
	 *
	 *	@param	bndl		the bundle to send. a <code>/sync</code> message
	 *						is appended to this bundle. <code>bndl</code> may
	 *						be <code>null</code>, in this case the <code>/sync</code>
	 *						is send alone.
	 *	@param	timeout		the maximum amount of time in seconds to wait
	 *	@return				<code>true</code> if the successfull reply was
	 *						receivied within the timeout.
	 *
	 *	@throws	IOException	if sending the message or receiving the reply fails
	 */
	public boolean sync( OSCBundle bndl, float timeout )
	throws IOException
	{
		final Integer id = new Integer( UniqueID.next() );
	
		if( bndl == null ) bndl	= new OSCBundle();
		bndl.addPacket( new OSCMessage( "/sync", new Object[] { id }));

		return( sendBundleSync( bndl, "/synced", null, 0, id, timeout ) != null );
	}

	/**
	 *	Allocates a new free node ID for a group or synth.
	 *
	 *	@return	the new node ID.
	 *
	 *	@todo	should be accompanied by a nextPermanentNodeID method
	 *	@todo	should throw a RuntimeException if the allocator
	 *			reaches its limit?
	 */
	public int nextNodeID()
	{
		return nodeAllocator.alloc();
	}

	/**
	 *	For internal use by <code>Buffer</code> objects.
	 *	Do not use yourself.
	 */
	protected void addBuf( Buffer buf )
	{
		// Buffer objects are cached in an Array for easy
		// auto buffer info updating
		bufferArray[ buf.getBufNum() ] = buf;
	}

	/**
	 *	For internal use by <code>Buffer</code> objects.
	 *	Do not use yourself.
	 */
	protected void freeBuf( int idx )
	{
		bufferArray[ idx ] = null;
	}
	
	/**
	 *	For internal use by <code>Buffer</code> objects.
	 *	Do not use yourself.
	 */
	protected void waitForBufInfo()
	throws IOException
	{
		// /b_info on the way
		// keeps a reference count of waiting Buffers so that only one responder is needed
		if( !waitingForBufInfo ) {
			bufInfoResponder	= new OSCResponderNode( this, "/b_info", new OSCResponderNode.Action() {
				public void respond( OSCResponderNode r, OSCMessage msg, long time )
				{
					if( msg.getArgCount() < 4 ) return;
				
					try {
						final Buffer buf = bufferArray[ ((Number) msg.getArg( 0 )).intValue() ];
						if( buf != null ) {
							buf.setNumFrames(   ((Number) msg.getArg( 1 )).intValue() );
							buf.setNumChannels( ((Number) msg.getArg( 2 )).intValue() );
							buf.setSampleRate(  ((Number) msg.getArg( 3 )).doubleValue() );
							buf.queryDone();
							
							if( --waitingBufs == 0 ) {
								waitingForBufInfo = false;
								r.remove();
							}
						}
					}
					catch( ClassCastException e2 ) {
						printError( "Server.waitForBufInfo", e2 );
					}
				}
			}).add();
		
			waitingForBufInfo = true;
		}
		waitingBufs++;
	}
	
	private void resetBufferAutoInfo()
	throws IOException
	{
		bufferArray			= new Buffer[ options.getNumBuffers() ];
		waitingBufs			= 0;
		waitingForBufInfo	= false;

		if( bufInfoResponder != null ) {
			bufInfoResponder.remove();
		}
	}

	/**
	 *	Prints a textual representation of this
	 *	object onto the given stream
	 *
	 *	@param	stream	the stream to print on
	 */
	public void printOn( PrintStream stream )
	{
		stream.print( "Server(" + getName() + "," + getAddr() + "," + getOptions() + "," + getClientID() + ")" );
	}

	/**
	 *	Sends an asynchronous <code>/quit</code>
	 *	to the server and cleans up the client's resources
	 *
	 *	@throws	IOException	if the message failed to be sent
	 *						or if cleanup failed
	 *
	 *	@warning	unlike in SClang, this stops the alive thread,
	 *				so in case you want to recognize remote server starts,
	 *				call <code>startAliveThread</code> afterwards.
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void quit()
	throws IOException
	{
		sendMsg( quitMsg() );
		Server.inform( "/quit sent" );
		cleanUpAfterQuit();
	}
	
	/**
	 *	Constructs a quit message for the server
	 *
	 *	@return	OSCMessage requesting the server to quit
	 */
	public OSCMessage quitMsg()
	{
		return new OSCMessage( "/quit", OSCMessage.NO_ARGS );
	}

	private void cleanUpAfterQuit()
	{
		try {
			stopAliveThread();
	//		alive			= false;
			dumpMode		= 0;
			setBooting( false );
			setRunning( false );
	//		if(scopeWindow.notNil) { scopeWindow.quit };
	//		new RootNode( this ).freeAll();
//
//			sendMsg( new OSCMessage( "/g_freeAll", new Object[] { new Integer( 0 )}));
//try {
//	Thread.sleep( 1000 );
//}
//catch( InterruptedException e1 ) {}

//			initCommunication();
createNewAllocators();
resetBufferAutoInfo();
		}
		catch( IOException e1 ) {
			printError( "Server.cleanUpAfterQuit", e1 );
		}
	}
	
	/**
	 *	Disposes any resources
	 *	allocated by this representation.
	 *	This shuts down OSC communication
	 *	and server event dispatching.
	 *	Do not use this object any more
	 *	after calling this method.
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void dispose()
	{
		multi.dispose();
		setServers.remove( this );
		mapServerNames.remove( getName() );
		em.dispose();
	}

	/**
	 *	Synchronously quit the server. If the server is not
	 *	booting and not running according to the running status,
	 *	this method returns immediately. Otherwise sends a <code>/quit</code>
	 *	message and waits for the reply. If no reply is received,
	 *	and the server was locally booted, terminates the server process.
	 *
	 *	@return	<code>true</code> if the server has successfully quit
	 *
	 *	@throws	IOException	if the message failed to be sent
	 *						or if cleanup failed
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public boolean quitAndWait()
	throws IOException
	{
		try {
			if( !isBooting() && !isRunning() ) return true;
		
			final OSCMessage msg = quitMsg();
		
			for( int i = 0; i < 16; i++ ) {
				if( sendMsgSync( msg, 0.5f )) {
					cleanUpAfterQuit();
					return true;
				}
			}
			// ok, now last chance : if local, kill the process
			if( isLocal ) {
				try {
					synchronized( syncBootThread ) {
						if( bootThread != null ) {
							bootThread.keepScRunning = false;
							syncBootThread.wait( 4000 );
						}
					}
				}
				catch( InterruptedException e1 ) { /* ignored */ }
			}

			if( !isBooting() && !isRunning() ) return true;
			
			printOn( printStream );
			printStream.println( " : failed to quit!" );
			return false;
		}
		finally {
			cleanUpAfterQuit();
		}
	}

	/**
	 *	Synchronously quits all known servers.
	 *	That is all servers for which a <code>Server</code>
	 *	representation instance has been created.
	 *	Cathes all thrown exceptions.
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public static void quitAll()
	{
		Server s;
	
		for( Iterator iter = setServers.iterator(); iter.hasNext(); ) {
			s = (Server) iter.next();
			if( s.isLocal ) {
				try {
					s.quitAndWait();
				}
				catch( IOException e1 ) {
					printError( "Server.quitAll", e1 );
				}
			}
			s.dispose();
		}
	}

	protected static void printError( String name, Throwable t )
	{
//		printStream.println( name + " : " + t.getClass().getName() + " : " + t.getLocalizedMessage() );
		printStream.print( name + " : " );
		t.printStackTrace( printStream );
	}

// ----------- EventManager.Processor interface -----------

	/**
	 *	This is used to dispatch
	 *	server events. Do not call this method.
	 */
	public void processEvent( BasicEvent e )
	{
		ServerListener		listener;
		final ServerEvent	sce			= (ServerEvent) e;
		
		for( int i = 0; i < em.countListeners(); i++ ) {
			listener = (ServerListener) em.getListener( i );
			listener.serverAction( sce );
		}
	}

// ----------- internal clases and interfaces -----------

	/**
	 *	This interface is used to describe an action
	 *	to be executed after some process such as
	 *	booting the server has completed.
	 *
	 *	@see	Server#addDoWhenBooted( Server.CompletionAction )
	 */
	public static interface CompletionAction
	{
		/**
		 *	Requests the implementing class to
		 *	perform any action it wishes. This is
		 *	called after the corresponding process
		 *	to which this action was attached, has completed.
		 *
		 *	@param	server	the server representation
		 *					for which the process completed
		 */
		public void completion( Server server );
	}

	private class BootThread
	extends Thread
	{
		private final String[]			cmdArray;
//		private final String[]			envArray;
		protected volatile boolean		keepScRunning	= true;
		protected final Server			server;
		private final boolean			startAliveThread;
	
//		private BootThread( Server server, String[] cmdArray, Map envMap, boolean startAliveThread )
		protected BootThread( Server server, String[] cmdArray, boolean startAliveThread )
		{
			super( server.getName() );
		
			this.cmdArray			= cmdArray;
			this.server				= server;
			this.startAliveThread	= startAliveThread;
			
//			if( envMap != null ) {
//				Map.Entry me;
//				final Iterator iter	= envMap.entrySet().iterator();
//				envArray			= new String[ envMap.size() ];
//				for( int i = 0; iter.hasNext(); i++ ) {
//					me				= (Map.Entry) iter.next();
//					envArray[ i ]	= me.getKey().toString() + "=" + me.getValue().toString();
//				}
//			} else {
//				envArray			= null;
//			}
			
			setDaemon( true );
			start();
		}
		
		public void run()
		{
			Process				p			= null;
			int					resultCode  = -1;
			boolean				pRunning	= true;
			boolean				cStarted	= false;
			InputStream			inStream, errStream;

			final byte[]		inBuf		= new byte[128];
			final byte[]		errBuf		= new byte[128];
			final File			cwd			= new File( cmdArray[0] ).getParentFile();

			try {
// NOTE: using envArray will make some scsynth versions (e.g. PPC G3) fail to boot
// because they seem to rely on some environment variables that get lost this way
// (providing a null argument preserves the current environment variables)
//				p			= Runtime.getRuntime().exec( cmdArray, envArray, cwd );
				p			= Runtime.getRuntime().exec( cmdArray, null, cwd );
				// "Implementation note: It is a good idea for the input stream to be buffered."
				inStream	= new BufferedInputStream( p.getInputStream() );
				errStream	= new BufferedInputStream( p.getErrorStream() );

				while( keepScRunning && pRunning ) {
					if( !cStarted ) {
						try {
//System.err.println( "...try" );
							server.start();
//System.err.println( "...succeeded" );
							cStarted = true;
							if( startAliveThread ) server.startAliveThread( 2.0f, 0.7f, 8 );	// allow really long unresponsiveness as a real server quit is recognized instantly
						}
						// thrown when in TCP mode and socket not yet available
						catch( ConnectException e1 ) {
//e1.printStackTrace();
						}
					}
					try {
						Thread.sleep( 500 );   // a kind of cheesy way to wait for the program to end
					}
					catch( InterruptedException e5 ) { /* ignored */ }

					handleConsole( inStream, inBuf );
					handleConsole( errStream, errBuf );
					try {
						resultCode	= p.exitValue();
						pRunning	= false;
						p			= null;
						printStream.println( "scsynth terminated (" + resultCode +")" );
					}
					// gets thrown if we call exitValue() while sc still running
					catch( IllegalThreadStateException e1 ) { /* ignored */ }
				} // while( keepScRunning && pRunning )
			}
			catch( IOException e3 ) {
				printError( "BootThread.run", e3 );
			}
			finally {
				if( p != null ) {
//					printStream.println( "scsynth didn't quit. we're killing it!" );
					p.destroy();				
				}
				synchronized( syncBootThread ) {
					try {
						server.stopAliveThread();
					}
					catch( IOException e1 ) {
						printError( "Server.stopAliveThread", e1 );
					}
					server.bootThread = null; // ! must be before setRunning !
					server.setBooting( false );
					server.setRunning( false );
					syncBootThread.notifyAll();
				}
			}
		}

		// redirect console
		private void handleConsole( InputStream stream, byte[] buf )
		{
			int i;

			try {
				while( stream.available() > 0 ) {
					i = Math.min( buf.length, stream.available() );
					stream.read( buf, 0, i );
					printStream.write( buf, 0, i );
				}
			}
			catch( IOException e1 ) { /* ignored XXX */ }
		}
	}

	/**
	 *	A static field only class
	 *	describing the snapshot of the server status
	 *	as delivered by <code>getStatus</code>
	 *
	 *	@see	#getStatus()
	 */	
	public static class Status
	{
		public int				numUGens;
		public int				numSynths;
		public int				numGroups;
		public int				numSynthDefs;
		public float			avgCPU;
		public float			peakCPU;
		public volatile double	sampleRate;
		public volatile double	actualSampleRate;
		
		protected static Status copyFrom( Status s )
		{
			final Status result			= new Status();
			synchronized( s ) {
				result.numUGens			= s.numUGens;
				result.numSynths		= s.numSynths;
				result.numGroups		= s.numGroups;
				result.numSynthDefs		= s.numSynthDefs;
				result.avgCPU			= s.avgCPU;
				result.peakCPU			= s.peakCPU;
				result.sampleRate		= s.sampleRate;
				result.actualSampleRate	= s.actualSampleRate;
			}
			return result;
		}
	}
	
	private class StatusWatcher
//	extends TimerTask
	implements OSCResponderNode.Action, ActionListener
	{
		private int							alive			= 0;
		private final	int					delayMillis;
		private final	int					periodMillis;
		private final	OSCResponderNode	resp;
		private final	int					deathBounces;
		private final	Timer				timer;

//		private StatusWatcher( float delay, float period )
//		{
//			this( delay, period, 4 );
//		}
//
		protected StatusWatcher( float delay, float period, int deathBounces )
		{
			delayMillis			= (int) (delay * 1000);
			periodMillis		= (int) (period * 1000);
			resp				= new OSCResponderNode( enc_this, "status.reply", this );
			this.deathBounces	= deathBounces;
			timer				= new Timer( periodMillis, this );
			timer.setInitialDelay( delayMillis );
		}
		
		protected void start()
		throws IOException
		{
//System.err.println( "start" );
//new Throwable().printStackTrace();
			resp.add();
			timer.restart();
//			appClock.schedule( this, delayMillis, periodMillis );
		}

		protected void stop()
		throws IOException
		{
//System.err.println( "stop" );
//new Throwable().printStackTrace();
//			this.cancel();
			timer.stop();
			resp.remove();
		}
		
		public void actionPerformed( ActionEvent e )
		{
//System.err.println( "setRunning( "+alive+" )" );
			if( alive > 0 ) {
				setRunning( true );
				alive--;
			} else {
				setRunning( false );
			}
			if( serverBooting && getOptions().getProtocol().equals( OSCChannel.TCP ) && !c.isConnected() ) {
				try {
//					c.connect();
					c.start();
				}
				catch( IOException e1 ) {
					printError( "Server.status", e1 );
				}
			} else {
				try {
					status();
				}
				catch( IOException e1 ) {
					printError( "Server.status", e1 );
				}
			}
		}
		
		// XXX create specific osc message decoder
		public void respond( OSCResponderNode r, OSCMessage msg, long time )
		{
			if( msg.getArgCount() < 9 ) return;
			
			alive = deathBounces;
			
			try {
				// msg.at( 0 ) == 1
				synchronized( status ) {
					status.numUGens			= ((Number) msg.getArg( 1 )).intValue();
					status.numSynths		= ((Number) msg.getArg( 2 )).intValue();
					status.numGroups		= ((Number) msg.getArg( 3 )).intValue();
					status.numSynthDefs		= ((Number) msg.getArg( 4 )).intValue();
					status.avgCPU			= ((Number) msg.getArg( 5 )).floatValue();
					status.peakCPU			= ((Number) msg.getArg( 6 )).floatValue();
					status.sampleRate		= ((Number) msg.getArg( 7 )).doubleValue();
					status.actualSampleRate	= ((Number) msg.getArg( 8 )).doubleValue();
				}
//				setRunning( true );	// should be thread safe ?
				changed( ServerEvent.COUNTS );
			}
			catch( ClassCastException e1 ) {
				printError( "StatusWatcher.messageReceived", e1 );
			}
		}
	}

	/*
	 *	A helper OSC responder
	 *	for asynchronous communication.
	 */
	private class SyncResponder
	implements OSCResponderNode.Action
	{
		protected volatile OSCMessage	replyMsg	= null;
		private final OSCResponderNode	doneResp;
		private final OSCResponderNode	failResp;
		private final String			doneCmdName;
		private final int[]				doneArgIndices;
		private final Object[]			doneArgMatches;
		private final int				doneMinArgNum;
		private final String			failCmdName;
		private final int[]				failArgIndices;
		private final Object[]			failArgMatches;
		private final int				failMinArgNum;
		
//		protected SyncResponder( String doneCmdName, String failCmdName, int argIdx, Object argMatch )
//		throws IOException
//		{
//			this( doneCmdName, failCmdName, new int[] { argIdx }, new Object[] { argMatch });
//		}

		protected SyncResponder( String doneCmdName, String failCmdName,
								 int[] doneArgIndices, Object[] doneArgMatches,
								 int[] failArgIndices, Object[] failArgMatches )
		throws IOException
		{
			this.doneCmdName	= doneCmdName;
			this.doneArgIndices	= doneArgIndices;
			this.doneArgMatches	= doneArgMatches;
			this.failCmdName	= failCmdName;
			this.failArgIndices	= failArgIndices;
			this.failArgMatches	= failArgMatches;
			
			int i = 0;
			for( int j = 0; j < doneArgIndices.length; j++ ) i = Math.max( i, doneArgIndices[ j ]);
			doneMinArgNum = i;
			doneResp			= new OSCResponderNode( enc_this, doneCmdName, this );
			
			if( failCmdName != null ) {
				i = 0;
				for( int j = 0; j < failArgIndices.length; j++ ) i = Math.max( i, failArgIndices[ j ]);
				failMinArgNum	= i;
				failResp		= new OSCResponderNode( enc_this, failCmdName, this );
			} else {
				failMinArgNum	= 0;
				failResp		= null;
			}
		}
		
		protected void add()
		throws IOException
		{
			doneResp.add();
			if( failResp != null ) failResp.add();
		}

		protected void remove()
		{
			doneResp.remove();
			if( failResp != null ) failResp.remove();
		}
		
		public void respond( OSCResponderNode r, OSCMessage msg, long time )
		{
			if( msg.getName().equals( doneCmdName )) {
				doneMessageReceived( msg );
			} else if( msg.getName().equals( failCmdName )) {
				failMessageReceived( msg );
			} else {
				assert false : msg.getName();
			}
		}
		
		private void doneMessageReceived( OSCMessage msg )
		{
			if( msg.getArgCount() < doneMinArgNum ) return;

			for( int i = 0; i < doneArgIndices.length; i++ ) {
				if( !msg.getArg( doneArgIndices[ i ]).equals( doneArgMatches[ i ])) return;
			}
			replyMsg	= msg;
			remove();
			synchronized( this ) {
				this.notifyAll();
			}
		}

		private void failMessageReceived( OSCMessage msg )
		{
			if( msg.getArgCount() < failMinArgNum ) return;

			for( int i = 0; i < failArgIndices.length; i++ ) {
				if( !msg.getArg( failArgIndices[ i ]).equals( failArgMatches[ i ])) return;
			}
			replyMsg = msg;
			remove();
			synchronized( this ) {
				this.notifyAll();
			}
		}
	}
}