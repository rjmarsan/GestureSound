/*
 *  UGen.java
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
 *		29-Jun-05	created
 */

package de.sciss.jcollider;

import java.util.ArrayList;

// Arg count statistics :
//   19 UGens with 0 arguments.
//   54 UGens with 1 arguments.
//   80 UGens with 2 arguments.
//   67 UGens with 3 arguments.
//   45 UGens with 4 arguments.
//   20 UGens with 5 arguments.
//	 22 UGens with 6+ arguments.
// so for ease of use we create separate
// methods for up to 5 args (= 93% of all ugens)

/**
 *	The client side represenation of a unit generator.
 *	Unlike SClang, there are no subclasses for all the different
 *	ugens (few exceptions, such as <code>Control</code>), therefore
 *	to construct a ugen graph requires you to make successive calls
 *	to the static constructor methods provided by this class. See
 *	<code>SynthDef</code> and <code>JColliderDemo</code> for examples
 *	of how to construct a graph.
 *	<p>
 *	When you use the static constructor methods of <code>UGen</code>,
 *	the class relies on the UGen database to be loaded into <code>UGenInfo</code>.
 *	Please see the <code>UGenInfo</code> doc for details about this database.
 *	<p>
 *	While some methods like <code>array( ... )</code> would better
 *	fit into the <code>GraphElemArray</code> class, they have been included
 *	here, simply because it's shorter to write <code>UGen.array( ... )</code>
 *	and it gives your code a more homogeneous and pathological look.
 *	<p>
 *	To access a single output of a multi-output UGen, use either the
 *	<code>getOutput</code> or <code>getChannel</code> method. To create
 *	controls, use the separate <code>Control</code> class.
 *
 *	@warning	no rate checking is performed along the inputs
 *
 *	@todo		a keyword constructor variant has been removed because it
 *				wasn't too convincing (well, keywords is just a concept
 *				totally unknown to java). however to make constructor calls
 *				shorter and more readable, some sort of keyword constructor
 *				should be re-invented.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.35, 19-Sep-09
 */
public class UGen
implements Constants, GraphElem
{
	private final String		name;
	private final Object		rate;
	private final Object[]		outputRates;
	private final UGenInput[]	inputs;
	private int					specialIndex;

	private static final GraphElem[]	NO_ARGS	= new GraphElem[0];

	/**
	 *	This constructor is used by <code>SynthDef</code>
	 *	when building the UGen from a def file. Do not
	 *	use this method yourself.
	 */
	protected UGen( String name, Object rate, Object[] outputRates, UGenInput[] inputs, int specialIndex )
	{
		this.name			= name;
		this.rate			= rate;
		this.outputRates	= outputRates;
		this.inputs			= inputs;
		this.specialIndex	= specialIndex;
	}

	/**
	 *	Returns the rate at which this UGen is running
	 *
	 *	@return	either of <code>kAudioRate</code>, <code>kControlRate</code>,
	 *			<code>kDemandRate</code> or <code>kScalarRate</code>
	 */
	public Object getRate()
	{
		return rate;
	}
	
	/**
	 *	Returns an array of the rates at which the individual
	 *	output channels run. I know of no UGen any of whose
	 *	outputs have a rate different from the UGen rate.
	 */
	public Object[] getOutputRates()
	{
		return outputRates;
	}

	/**
	 *	Returns the rate of one of the UGens output channels.
	 *	I know of no UGen any of whose
	 *	outputs have a rate different from the UGen rate.
	 */
	public Object getOutputRate( int channel )
	{
		return outputRates[ channel ];
	}
	
	/**
	 *	Returns an array of the UGen's inputs.
	 */
	public UGenInput[] getInputs()
	{
		return inputs;
	}

	/**
	 *	Returns the UGen's input at the given index.
	 *	Note that for UGens constructed with a variable
	 *	length array argument, this argument is expanded
	 *	to separate individual arguments. For example
	 *	for <code>Out</code>, each element of the
	 *	<code>channelsArray</code> argument is a separate
	 *	input with a separate index (as you would expect
	 *	from the binary compiled synth def format).
	 */
	public UGenInput getInput( int index )
	{
		return inputs[ index ];
	}
	
	/**
	 *	Returns the so-called special index of the UGen.
	 *	This is kind of an extra internal static parameter.
	 *	Special indices are used by <code>BinaryOpUGen</code>
	 *	and <code>UnaryOpUGen</code> to specify the mathematical operator,
	 *	and by <code>Control</code> to specify the index in
	 *	the control name table. Note that this index is invalid
	 *	for <code>Control</code> until the graph has been constructed
	 *	by <code>SynthDef</code>.
	 */
	public int getSpecialIndex()
	{
		return specialIndex;
	}

	/**
	 *	This is called by <code>SynthDef</code> when assembling
	 *	controls. Do not call this method directly.
	 *	To create unary and binary operator ugens, simply
	 *	construct them with their operator name and the
	 *	ugen factory will set the right index itself. So to
	 *	create a multiplying binary ugen, you will call:
	 *	<pre>
	 *	UGen.ar( "*", firstUGen, secondUGen );
	 *	</pre>
	 *	and not
	 *	<pre>
	 *	UGen myBin = UGen.ar( "BinaryOpUGen", firstUGen, secondUGen );
	 *	myBin.setSpecialIndex(	2 );
	 *	</pre>
	 */
	protected void setSpecialIndex( int idx )
	{
		specialIndex = idx;
	}

	/**
	 *	Returns the number of input arguments.
	 *	Arrays are expanded, so an <code>Out</code>
	 *	UGen with three input channels will report
	 *	<code>4</code> (three inputs plus bus parameter).
	 */
	public int getNumInputs()
	{
		return inputs.length;
	}
	
// --------- GraphElem interface ---------
	
	public int getNumOutputs()
	{
		return outputRates.length;
	}
	
	public GraphElem getOutput( int idx )
	{
		return getChannel( idx );
	}

	public UGenInput[] asUGenInputs()
	{
		final UGenInput[] result = new UGenInput[ getNumOutputs() ];
		for( int i = 0; i < result.length; i++ ) {
			result[ i ] = getChannel( i );
		}
		return result;
	}
	
	public UGenChannel getChannel( int ch )
	{
		return new UGenChannel( this, ch );
	}

// XXX could be re-implemented using UGenInfo
//	protected String argNameForInputAt( int argIndex )
//	{
//	}

	/**
	 *	Returns the <strong>class name</strong> of the UGen.
	 *	In the case of unary and binary op ugens, this
	 *	will report &quot;UnaryOpUGen&quot; and &quot;BinaryOpUGen&quot;
	 *	and not their operators. To get the operator names
	 *	look up the <code>UGen</code> in the <code>UGenInfo</code> dictionary.
	 *
	 *	@see	UGenInfo#infos
	 */
	public String getName()
	{
		return name;
//		String className = this.getClass().getName();
//		return className.substring( className.lastIndexOf( '.' ) + 1 );
	}
	
// BinaryOpUGen could do this XXX
//	protected void optimizeGraph() {}

	/**
	 *	Returns a string representation of the UGen.
	 *
	 *	@todo	should report the operator name or unary/binary op
	 */
	public String dumpName()
	{
//		return( getSynthIndex() + "_" + getName() );
		if( specialIndex == 0 ) {
			return getName();
		} else {
			return( getName() + '?' + specialIndex );
		}
	}

	/**
	 *	Constructs a new audio rate UGen
	 *	with zero arguments and a required explict
	 *	output channel number. This method should only
	 *	be used for UGens that require the explicit
	 *	setting of the number of output channels, such
	 *	as <code>LocalIn.ar</code>.
	 *	<p>
	 *	This method can also be used for UGens with more
	 *	than zero arguments, if the info dictionary contains
	 *	default values for the missing arguments. An example
	 *	is <code>BufRd</code> which actually has four arguments
	 *	for bufNum, phase, loop and interolation, but specifies
	 *	defaults for all of them (bufNum 0, phase zero, loop 1, interpolation 2).
	 *
	 *	@param	name			either ugen class name or
	 *							alias name (for unary/binary op)
	 *	@param	numChannels		number of output channels to create
	 *
	 *	@throws	IllegalArgumentException	if the UGen cannot be found in the info dictionary,
	 *										if the rate is not allowed for the ugen
	 *	@throws	IllegalStateException		if the UGens requires arguments
	 *										which have not been provided and have no default values
	 */
	public static GraphElem ar( String name, int numChannels )
	{
		return UGen.construct( name, kAudioRate, numChannels, NO_ARGS );
	}

	/**
	 *	Constructs a new audio rate UGen
	 *	with zero arguments.
	 *	<p>
	 *	This method can also be used for UGens with more
	 *	than zero arguments, if the info dictionary contains
	 *	default values for the missing arguments. An example
	 *	is <code>SinOsc</code> which actually has two arguments
	 *	for frequency and phase, but both of them have default
	 *	values (440 hertz, zero degrees phase).
	 *
	 *	@param	name			either ugen class name or
	 *							alias name (for unary/binary op)
	 *
	 *	@throws	IllegalArgumentException	if the UGen cannot be found in the info dictionary,
	 *										if the rate is not allowed for the ugen,
	 *										or if the UGen requires the explicit specification of the
	 *										number of output channels
	 *	@throws	IllegalStateException		if the UGens requires arguments
	 *										which have not been provided and have no default values
	 */
	public static GraphElem ar( String name )
	{
		return UGen.construct( name, kAudioRate, -1, NO_ARGS );
	}

	/**
	 *	Constructs a new audio rate UGen
	 *	with one given argument and an explicit
	 *	number of output channels. This method should only
	 *	be used for UGens that require the explicit
	 *	setting of the number of output channels, such
	 *	as <code>PanAz.ar</code>.
	 *	<p>
	 *	This method can also be used for UGens with more
	 *	than one argument (e.g. <code>PanAz</code>), if the info
	 *	dictionary contains
	 *	default values for the missing arguments. For example,
	 *	<code>PanAz</code> has five arguments, the first one
	 *	being the input signal which needs to specified, but
	 *	the other ones having default values (pos 0.0, level 1.0, width 2.0, orientation 0.5).
	 *
	 *	@param	name			either ugen class name or
	 *							alias name (for unary/binary op)
	 *	@param	numChannels		number of output channels to create
	 *	@param	in1				first input argument
	 *
	 *	@throws	IllegalArgumentException	if the UGen cannot be found in the info dictionary,
	 *										if the rate is not allowed for the ugen
	 *	@throws	IllegalStateException		if the UGens requires arguments
	 *										which have not been provided and have no default values
	 */
	public static GraphElem ar( String name, int numChannels, GraphElem in1 )
	{
		return UGen.construct( name, kAudioRate, numChannels, new GraphElem[] { in1 });
	}

	/**
	 *	Constructs a new audio rate UGen
	 *	with one given argument
	 *	<p>
	 *	This method can also be used for UGens with more
	 *	than one argument (e.g. <code>Osc</code>), if the info
	 *	dictionary contains
	 *	default values for the missing arguments.
	 *
	 *	@param	name			either ugen class name or
	 *							alias name (for unary/binary op)
	 *	@param	in1				first input argument
	 *
	 *	@throws	IllegalArgumentException	if the UGen cannot be found in the info dictionary,
	 *										if the rate is not allowed for the ugen,
	 *										or if the UGen requires the explicit specification of the
	 *										number of output channels
	 *	@throws	IllegalStateException		if the UGens requires arguments
	 *										which have not been provided and have no default values
	 */
	public static GraphElem ar( String name, GraphElem in1 )
	{
		return UGen.construct( name, kAudioRate, -1, new GraphElem[] { in1 });
	}

	public static GraphElem ar( String name, int numChannels, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kAudioRate, numChannels, new GraphElem[] { in1, in2 });
	}

	public static GraphElem ar( String name, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kAudioRate, -1, new GraphElem[] { in1, in2 });
	}

	public static GraphElem ar( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kAudioRate, numChannels, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem ar( String name, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kAudioRate, -1, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem ar( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kAudioRate, numChannels, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem ar( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kAudioRate, -1, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem ar( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3,
								GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kAudioRate, numChannels, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

	public static GraphElem ar( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kAudioRate, -1, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

	public static GraphElem ar( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4, GraphElem in5, GraphElem in6 )
	{
		return UGen.construct( name, kAudioRate, -1, new GraphElem[] { in1, in2, in3, in4, in5, in6 });
	}
	
//	public static GraphElem ar( String name, Object[] inputs )
//	{
//		return UGen.construct( name, kAudioRate, -1, inputs );
//	}
	
	public static GraphElem kr( String name, int numChannels )
	{
		return UGen.construct( name, kControlRate, numChannels, NO_ARGS );
	}

	public static GraphElem kr( String name )
	{
		return UGen.construct( name, kControlRate, -1, NO_ARGS );
	}

	public static GraphElem kr( String name, int numChannels, GraphElem in1 )
	{
		return UGen.construct( name, kControlRate, numChannels, new GraphElem[] { in1 });
	}

	public static GraphElem kr( String name, GraphElem in1 )
	{
		return UGen.construct( name, kControlRate, -1, new GraphElem[] { in1 });
	}

	public static GraphElem kr( String name, int numChannels, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kControlRate, numChannels, new GraphElem[] { in1, in2 });
	}

	public static GraphElem kr( String name, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kControlRate, -1, new GraphElem[] { in1, in2 });
	}

	public static GraphElem kr( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kControlRate, numChannels, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem kr( String name, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kControlRate, -1, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem kr( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kControlRate, numChannels, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem kr( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kControlRate, -1, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem kr( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3,
								GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kControlRate, numChannels, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

	public static GraphElem kr( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kControlRate, -1, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

	public static GraphElem kr( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4, GraphElem in5, GraphElem in6 )
	{
		return UGen.construct( name, kControlRate, -1, new GraphElem[] { in1, in2, in3, in4, in5, in6 });
	}

//	public static GraphElem kr( String name, Object[] inputs )
//	{
//		return UGen.construct( name, kControlRate, -1, inputs );
//	}
	
	/**
	 *	&quot;dr&quot; stands for demand-rate
	 */
	public static GraphElem dr( String name, int numChannels )
	{
		return UGen.construct( name, kDemandRate, numChannels, NO_ARGS );
	}

	public static GraphElem dr( String name )
	{
		return UGen.construct( name, kDemandRate, -1, NO_ARGS );
	}

	public static GraphElem dr( String name, int numChannels, GraphElem in1 )
	{
		return UGen.construct( name, kDemandRate, numChannels, new GraphElem[] { in1 });
	}

	public static GraphElem dr( String name, GraphElem in1 )
	{
		return UGen.construct( name, kDemandRate, -1, new GraphElem[] { in1 });
	}

	public static GraphElem dr( String name, int numChannels, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kDemandRate, numChannels, new GraphElem[] { in1, in2 });
	}

	public static GraphElem dr( String name, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kDemandRate, -1, new GraphElem[] { in1, in2 });
	}

	public static GraphElem dr( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kDemandRate, numChannels, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem dr( String name, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kDemandRate, -1, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem dr( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kDemandRate, numChannels, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem dr( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kDemandRate, -1, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem dr( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3,
								GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kDemandRate, numChannels, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

	public static GraphElem dr( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kDemandRate, -1, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

//	public static GraphElem dr( String name, Object[] inputs )
//	{
//		return UGen.construct( name, kDemandRate, -1, inputs );
//	}

	/**
	 *	A shorthand method for creating a constant
	 *	in the ugen graph. This is equivalent
	 *	to <code>new Constant( value )</code>.
	 */
	public static GraphElem ir( float value )
	{
		return new Constant( value );
	}

	public static GraphElem ir( String name, int numChannels )
	{
		return UGen.construct( name, kScalarRate, numChannels, NO_ARGS );
	}

	public static GraphElem ir( String name )
	{
		return UGen.construct( name, kScalarRate, -1, NO_ARGS );
	}

	public static GraphElem ir( String name, int numChannels, GraphElem in1 )
	{
		return UGen.construct( name, kScalarRate, numChannels, new GraphElem[] { in1 });
	}

	public static GraphElem ir( String name, GraphElem in1 )
	{
		return UGen.construct( name, kScalarRate, -1, new GraphElem[] { in1 });
	}

	public static GraphElem ir( String name, int numChannels, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kScalarRate, numChannels, new GraphElem[] { in1, in2 });
	}

	public static GraphElem ir( String name, GraphElem in1, GraphElem in2 )
	{
		return UGen.construct( name, kScalarRate, -1, new GraphElem[] { in1, in2 });
	}

	public static GraphElem ir( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kScalarRate, numChannels, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem ir( String name, GraphElem in1, GraphElem in2, GraphElem in3 )
	{
		return UGen.construct( name, kScalarRate, -1, new GraphElem[] { in1, in2, in3 });
	}

	public static GraphElem ir( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kScalarRate, numChannels, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem ir( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4 )
	{
		return UGen.construct( name, kScalarRate, -1, new GraphElem[] { in1, in2, in3, in4 });
	}

	public static GraphElem ir( String name, int numChannels, GraphElem in1, GraphElem in2, GraphElem in3,
								GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kScalarRate, numChannels, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

	public static GraphElem ir( String name, GraphElem in1, GraphElem in2, GraphElem in3, GraphElem in4, GraphElem in5 )
	{
		return UGen.construct( name, kScalarRate, -1, new GraphElem[] { in1, in2, in3, in4, in5 });
	}

//	public static GraphElem ir( String name, Object[] inputs )
//	{
//		return UGen.construct( name, kScalarRate, -1, inputs );
//	}
	
	private static GraphElem construct( String name, Object rate, int numChannels, GraphElem[] inputs )
	{
		final UGenInfo			ui = (UGenInfo) UGenInfo.infos.get( name );
		final GraphElem[]		ugens;
		final UGenInput[][]		ugenIns;
		final java.util.List	args;
		final boolean			hasArray;
		final int				outChan;
		final Object[]			outRates;
		
		if( ui == null ) {
			throw new IllegalArgumentException( "Unknown UGen class " + name );
		}
		if( !ui.rates.contains( rate )) {
			throw new IllegalArgumentException( rate.toString() + " : illegal rate for UGen " + name );
		}

		UGenInfo.Arg			argInfo;
		UGenInput[]				ins;
		int						chanExp		= 1;	// channel expansion : output numchan = max( each input's numchan)
		int						numArgs		= ui.args.length;
		int						numIns		= inputs.length;
		int						specialIndex= 0;
		int						i, j;
		GraphElem				graph;
		Object					o;
		
		// last arg may be an array
		if( (numArgs > 0) && (ui.args[ numArgs - 1 ].isArray) ) {
			hasArray	= true;
			numArgs--;
			numIns--;
			args		= new ArrayList( numArgs + inputs[ numIns ].getNumOutputs() );
		} else {
			hasArray	= false;
			args		= new ArrayList( numArgs );
		}
		
		// fill in the non-array args
		for( i = 0; (i < numArgs) && (i < numIns); i++ ) {
			ins		= inputs[ i ].asUGenInputs();
			chanExp = Math.max( chanExp, ins.length );	// examine the channel expansion
			args.add( ins );
		}
		// fill in omitted args which have a default value
		for( ; i < numArgs; i++ ) {
			argInfo = ui.args[ i ];
			if( Float.isNaN( argInfo.def )) {
				throw new IllegalStateException( "Missing argument " + argInfo.name + " for UGen " + name );
			}
			ins = new Constant( argInfo.def ).asUGenInputs();
			args.add( ins );
		}
		// fill in array elements
		if( hasArray ) {
			graph	= inputs[ numIns ]; // note: numIns has been decreased by one
			for( i = 0; i < graph.getNumOutputs(); i++ ) {
				ins		= graph.getOutput( i ).asUGenInputs();
				chanExp = Math.max( chanExp, ins.length );
				args.add( ins );
			}
		}
		
		if( ui.args.length < inputs.length ) {
			System.err.println( name + ": Warning, illegal additional arguments (" + (inputs.length - numArgs) + ")" );
		}

		numArgs	= args.size();
		outChan = ui.getNumOutputs( numArgs, numChannels );
		
		if( outChan == -1 ) {
			throw new IllegalArgumentException( name + " : numChannels need to be specified" );
		}

		outRates	= new Object[ outChan ];
		for( i = 0; i < outChan; i++ ) {
			outRates[ i ] = rate;
		}
		
		ugens		= new GraphElem[ chanExp ];
		ugenIns		= new UGenInput[ chanExp ][ numArgs ];
		
		// fill in and expand the ugen inputs
		for( i = 0; i < numArgs; i++ ) {
			ins	= (UGenInput[]) args.get( i );
			for( j = 0; j < chanExp; j++ ) {
				ugenIns[ j ][ i ] = ins[ j % ins.length ];
			}
		}
		
		// now instantiate the ugens		
		if( ui.specials != null ) {
			o	= ui.specials.get( name );
			if( o != null ) {
				specialIndex = ((Integer) o).intValue();
			}
		}
		
		for( i = 0; i < chanExp; i++ ) {
			ugens[ i ] = new UGen( ui.className, rate, outRates, ugenIns[ i ], specialIndex );
			// XXX here is potential place for in place optimization,
			// like exchanging a ugen for a constant
		}
		
		return( chanExp == 1 ? ugens[ 0 ] : new GraphElemArray( ugens ));
	}

//	public static GraphElem mulAdd( GraphElem in, float mul, float add )
//	{
//		return mulAdd( in, ir( mul ), ir( add ));
//	}
//
//	public static GraphElem mulAdd( GraphElem in, GraphElem mul, GraphElem add )
//	{
//		UGenInput[]	uins		= in.asUGenInputs()
//		Object		highestRate	= kScalarRate;
//		Object		rate;
//
//		for( int i = 0; i < uins.length; i++ ) {
//			if( uins[ i ] instanceof UGenChannel ) {
//				rate = ((UGenChannel) uins[ i ]).getRate();
//				if( rate == kAudioRate ) {
//					highestRate = kAudioRate;
//					break;
//				} else if( rate == kControlRate ) {
//					highestRate = kControlRate;
//				} else if( rate == kDemandRate ) {
//					throw new IllegalArgumentException( "MulAdd : illegal rate " + rate );
//				}
//			}
//		}
//		
//		return construct( "MulAdd", highestRate, -1, in, mul, add );
//	}
	
	/**
	 *	Assembles two graph elements (such as UGens,
	 *	or Constants) into one array which in turn
	 *	can be used for multichannel expansion when passed
	 *	as an argument to one of the UGen constructor
	 *	methods
	 *
	 *	@param	g1	first graph element such as a <code>UGen</code>, <code>Constant</code>,
	 *				<code>UGenChannel</code> or <code>GraphElemArray</code>
	 *	@param	g2	second graph element
	 *	@return	array'ed elements
	 */
	public static GraphElem array( GraphElem g1, GraphElem g2 )
	{
		return new GraphElemArray( new GraphElem[] { g1, g2 });
	}

	/**
	 *	Assembles three graph elements (such as UGens,
	 *	or Constants) into one array which in turn
	 *	can be used for multichannel expansion when passed
	 *	as an argument to one of the UGen constructor
	 *	methods
	 *
	 *	@param	g1	first graph element
	 *	@param	g2	second graph element
	 *	@param	g3	third graph element
	 *	@return	array'ed elements
	 */
	public static GraphElem array( GraphElem g1, GraphElem g2, GraphElem g3 )
	{
		return new GraphElemArray( new GraphElem[] { g1, g2, g3 });
	}

	/**
	 *	Assembles four graph elements (such as UGens,
	 *	or Constants) into one array which in turn
	 *	can be used for multichannel expansion when passed
	 *	as an argument to one of the UGen constructor
	 *	methods
	 *
	 *	@param	g1	first graph element
	 *	@param	g2	second graph element
	 *	@param	g3	third graph element
	 *	@param	g4	fourth graph element
	 *	@return	array'ed elements
	 */
	public static GraphElem array( GraphElem g1, GraphElem g2, GraphElem g3, GraphElem g4 )
	{
		return new GraphElemArray( new GraphElem[] { g1, g2, g3, g4 });
	}

	/**
	 *	Assembles five graph elements (such as UGens,
	 *	or Constants) into one array which in turn
	 *	can be used for multichannel expansion when passed
	 *	as an argument to one of the UGen constructor
	 *	methods
	 *
	 *	@param	g1	first graph element
	 *	@param	g2	second graph element
	 *	@param	g3	third graph element
	 *	@param	g4	fourth graph element
	 *	@param	g5	fifth graph element
	 *	@return	array'ed elements
	 */
	public static GraphElem array( GraphElem g1, GraphElem g2, GraphElem g3, GraphElem g4, GraphElem g5 )
	{
		return new GraphElemArray( new GraphElem[] { g1, g2, g3, g4, g5 });
	}
}