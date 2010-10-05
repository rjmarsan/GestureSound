package de.sciss.jcollider.test;

import java.io.IOException;
import java.util.*;

import de.sciss.jcollider.*;
import de.sciss.net.OSCBundle;

/**
 *	Static example generator
 *	to compare with SC examples
 *	found in the from-SC2.rtf file
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.32, 24-Feb-08
 */
public abstract class DemoDefs
{
	public static java.util.List create()
	{
		final java.util.List	result	= new ArrayList();
		final Random			rnd		= new Random( System.currentTimeMillis() );
		SynthDef				def;
		GraphElem				f, g, h;

		// AnalogBubbles
		// NOTE : the graph tree shows a slightly different
		//   ordering of the top most LFSaw elements, have to check
		f = UGen.kr( "midicps", UGen.kr( "MulAdd", UGen.kr( "LFSaw", UGen.ir( 0.4f ), UGen.ir( 0 )),
				UGen.ir( 24 ), UGen.kr( "MulAdd", UGen.kr( "LFSaw", UGen.array( UGen.ir( 8 ), UGen.ir( 7.23f )), UGen.ir( 0 )),
					UGen.ir( 3 ), UGen.ir( 80 ))));
		g = UGen.ar( "CombN", UGen.ar( "*", UGen.ar( "SinOsc", f, UGen.ir( 0 )), UGen.ir( 0.04f )),
				UGen.ir( 0.2f ), UGen.ir( 0.2f ), UGen.ir( 4 ));
		def = new SynthDef( "JAnalogBubbles", UGen.ar( "Out", UGen.ir( 0 ), g ));
		result.add( def );
		
		// PulseModulation
		f	= UGen.ar( "CombL", UGen.ar( "RLPF", UGen.ar( "*", UGen.ar( "LFPulse",
			UGen.kr( "MulAdd", UGen.kr( "FSinOsc", UGen.ir( 0.05f ), UGen.ir( 0 )), UGen.ir( 80 ), UGen.ir( 160 )),
				UGen.ir( 0 ), UGen.ir( 0.4f )), UGen.ir( 0.05f )), UGen.kr( "MulAdd",
					UGen.kr( "FSinOsc", UGen.array( UGen.ir( 0.6f ), UGen.ir( 0.7f )), UGen.ir( 0 )), UGen.ir( 3600 ), UGen.ir( 4000 )),
						UGen.ir( 0.2f )), UGen.ir( 0.3f ), UGen.array( UGen.ir( 0.2f ), UGen.ir( 0.25f )), UGen.ir( 2 ));
		def = new SynthDef( "JPulseModulation", UGen.ar( "Out", UGen.ir( 0 ), f ));
		result.add( def );
		
		// MotoRev
		f	= UGen.ar( "clip2", UGen.ar( "RLPF", UGen.ar( "LFPulse", UGen.kr( "MulAdd",
				UGen.kr( "SinOsc", UGen.ir( 0.2f ), UGen.ir( 0 )), UGen.ir( 10 ), UGen.ir( 21 )),
					UGen.ir( 0.1f )), UGen.ir( 100 ), UGen.ir( 0.1f )), UGen.ir( 0.4f ));
		def = new SynthDef( "JMotoRev", UGen.ar( "Out", UGen.ir( 0 ), UGen.ar( "Pan2", f )));
		result.add( def );

		// Scratchy
		// IMPORTANT NOTE : bringing one instance of BrownNoise together with a mul array
		//		into MulAdd will expand the channels but not duplicate BrownNoise
		//		(as in the SCLang source). So for equality, we put BrownNoise in the array!
		f	= UGen.ar( "RHPF", UGen.ar( "*", UGen.ar( "max", UGen.ar( "MulAdd",
			UGen.array( UGen.ar( "BrownNoise" ), UGen.ar( "BrownNoise" )), UGen.ir( 0.5f ), UGen.ir( -0.49f )),
				UGen.ir( 0 )), UGen.ir( 20 )), UGen.ir( 5000 ), UGen.ir( 1 ));
		def = new SynthDef( "JScratchy", UGen.ar( "Out", UGen.ir( 0 ), f ));
		result.add( def );

		// Sprinkler
		f = UGen.ar( "BPZ2", UGen.ar( "*", UGen.ar( "WhiteNoise" ), UGen.kr( "*", UGen.kr( "LFPulse",
				UGen.kr( "MulAdd", UGen.kr( "LFPulse", UGen.ir( 0.09f ), UGen.ir( 0 ), UGen.ir( 0.16f )),
					UGen.ir( 10 ), UGen.ir( 7 )), UGen.ir( 0 ), UGen.ir( 0.25f )), UGen.ir( 0.1f ))));
		def = new SynthDef( "JSprinkler", UGen.ar( "Out", UGen.ir( 0 ), UGen.ar( "Pan2", f )));
		result.add( def );

		// HarmonicSwimming
	{
		float		freq		= 50;
		int			partials	= 20;
		GraphElem	z			= UGen.ir( 0.0f );
		GraphElem	offset		= UGen.kr( "Line", UGen.ir( 0 ), UGen.ir( -0.02f ), UGen.ir( 60 ));
		for( int i = 0; i < partials; i++ ) {
			f = UGen.kr( "max", UGen.ir( 0 ), UGen.kr( "MulAdd", UGen.kr( "LFNoise1", UGen.array(
				UGen.ir( 2 + rnd.nextFloat() * 8 ), UGen.ir( 2 + rnd.nextFloat() * 8 ))), UGen.ir( 0.02f ), offset ));
			z = UGen.ar( "MulAdd", UGen.ar( "FSinOsc", UGen.ir( freq * (i + 1)), UGen.ir( 0 )), f, z );
		}
		def = new SynthDef( "JHarmonicSwimming", UGen.ar( "Out", UGen.ir( 0 ), z ));
		result.add( def );
	}

		// HarmonicTumbling
	{
		float		freq		= 80;
		float		partials	= 10;
		GraphElem	z			= UGen.ir( 0.0f );
		GraphElem	trig		= UGen.kr( "XLine", UGen.array( UGen.ir( 10 ), UGen.ir( 10 )), UGen.ir( 0.1f ), UGen.ir( 60 ));

		for( int i = 0; i < partials; i++ ) {
			f	= UGen.kr( "Decay2", UGen.kr( "*", UGen.kr( "Dust", trig ), UGen.ir( 0.02f )), UGen.ir( 0.005f ),
					UGen.ir( rnd.nextFloat() * 0.5f ));
			z	= UGen.ar( "MulAdd", UGen.ar( "FSinOsc", UGen.ir( freq * (i + 1)), UGen.ir( 0 )), f, z );
		}
		def = new SynthDef( "JHarmonicTumbling", UGen.ar( "Out", UGen.ir( 0 ), z ));
		result.add( def );
	}
	
		// KlankReson
		// NOTE : while in SClang the Klank constructor takes
		//	the array as the first argument before calling the
		//	super constructor, we follow the actual UGen spec
		//	where the array is the last argument; since we haven't
		//	provided a keyword constructor yet, it means we
		//	have to provide the three missing default arguments
		// Also note that the concept of the the random panning 
		//	was probably meant to vary along the partials
		//	(which won't happend in neither language ; see also the orphan 'n')
	{
		int				partials	= 15;
		GraphElem[]		flop		= new GraphElem[ partials * 3 ];
		float			fl;
		
		for( int i = 0, j = 0; i < partials; i++ ) {
			fl			= rnd.nextFloat();
			flop[ j++ ]	= UGen.ir( 80 + fl*fl*10000 );				// freq
			flop[ j++ ]	= UGen.ir( rnd.nextFloat() * 2 - 1 );		// amp
			flop[ j++ ]	= UGen.ir( 0.2f + rnd.nextFloat() * 8 );	// reson
		}
		
		f	= UGen.ar( "Klank", UGen.ar( "*", UGen.ar( "Dust", UGen.ir( 0.7f )), UGen.ir( 0.04f )),
				UGen.ir( 1.0f ), UGen.ir( 0.0f ), UGen.ir( 1.0f ), new GraphElemArray( flop ));
		g	= UGen.ar( "Pan2", f, UGen.ir( rnd.nextFloat() * 2 - 1 ));
		def = new SynthDef( "JKlankReson", UGen.ar( "Out", UGen.ir( 0 ), g ));
		result.add( def );
	}

		// PoliceState
		// NOTE : don't ask me where Mix.arFill is contained
		//	in the sc sources, but it simply adds up all arguments
	{
		int	num	= 4;
		
		f	= null;
		for( int i = 0; i < num; i++ ) {
			g = UGen.ar( "Pan2", UGen.ar( "*", UGen.ar( "SinOsc", UGen.kr( "MulAdd",
				UGen.kr( "SinOsc", UGen.ir( rnd.nextFloat() * 0.1f + 0.02f ), UGen.ir( rnd.nextFloat() * 2 * (float) Math.PI )),
					UGen.ir( rnd.nextInt( 600 )), UGen.ir( rnd.nextInt( 600 ) + 700 )), UGen.ir( 0 )),
						UGen.ar( "*", UGen.ar( "LFNoise2", UGen.ir( 80 + rnd.nextFloat() * 40 )), UGen.ir( 0.1f ))),
							UGen.ir( rnd.nextFloat() * 2 - 1 ));
			f = f == null ? g : UGen.ar( "+", f, g );
		}
		g	= UGen.ar( "*", UGen.ar( "LFNoise2",
			UGen.kr( "MulAdd", UGen.kr( "LFNoise2", UGen.array( UGen.ir( 0.4f ), UGen.ir( 0.4f ))),
				UGen.ir( 90 ), UGen.ir( 620 ))),
			UGen.kr( "MulAdd", UGen.kr( "LFNoise2", UGen.array( UGen.ir( 0.3f ), UGen.ir( 0.3f ))),
				UGen.ir( 0.15f ), UGen.ir( 0.18f )));
		h	= UGen.ar( "CombL", UGen.ar( "+", f, g ), UGen.ir( 0.3f ), UGen.ir( 0.3f ), UGen.ir( 3 ));
		def = new SynthDef( "JPoliceState", UGen.ar( "Out", UGen.ir( 0 ), h ));
		result.add( def );
	}
	
		// SampleAndHoldLiquidities
	{
		GraphElem	clockRate	= UGen.kr( "MouseX", UGen.ir( 1 ), UGen.ir( 200 ), UGen.ir( 1 ));
		GraphElem	clockTime	= UGen.kr( "reciprocal", clockRate );
		GraphElem	clock		= UGen.kr( "Impulse", clockRate, UGen.ir( 0.4f ));
		GraphElem	centerFreq	= UGen.kr( "MouseY", UGen.ir( 100 ), UGen.ir( 8000 ), UGen.ir( 1 ));
		GraphElem	freq		= UGen.kr( "Latch", UGen.kr( "MulAdd", UGen.kr( "WhiteNoise" ),
									UGen.kr( "*", centerFreq, UGen.ir( 0.5f )), centerFreq ), clock );
		GraphElem	panPos		= UGen.kr( "Latch", UGen.kr( "WhiteNoise" ), clock );

		f	= UGen.ar( "*", UGen.ar( "SinOsc", freq ), UGen.kr( "Decay2", clock,
				UGen.kr( "*", UGen.ir( 0.1f ), clockTime ), UGen.kr( "*", UGen.ir( 0.9f ), clockTime )));
		g	= UGen.ar( "Pan2", f, panPos );
		h	= UGen.ar( "CombN", g, UGen.ir( 0.3f ), UGen.ir( 0.3f ), UGen.ir( 2 ));
		def = new SynthDef( "JSampleAndHoldLiquid", UGen.ar( "Out", UGen.ir( 0 ), h ));
		result.add( def );
	}

		// AleatoricQuartet
	{
		float		amp		= 0.07f;
		GraphElem	density	= UGen.kr( "MouseX", UGen.ir( 0.01f ), UGen.ir( 1 ));
		GraphElem	dmul	= UGen.kr( "*", UGen.kr( "reciprocal", density ), UGen.ir( 0.5f * amp ));
		GraphElem	dadd	= UGen.kr( "+", UGen.kr( "neg", dmul ), UGen.ir( amp ));
		float[]		fRange	= new float[] { 1, 0.5f, 0.25f };

		GraphElem	excitation, freq;

		f = null;

		for( int i = 0; i < 4; i++ ) {
			excitation = UGen.ar( "*", UGen.ar( "PinkNoise" ), UGen.kr( "max", UGen.ir( 0 ),
				UGen.kr( "MulAdd", UGen.kr( "LFNoise1", UGen.ir( 8 )), dmul, dadd )));
				
			freq = UGen.kr( "midicps", UGen.kr( "Lag", UGen.kr( "round", UGen.kr( "MulAdd",
				UGen.kr( "LFNoise0", UGen.ir( fRange[ rnd.nextInt( 3 )])), UGen.ir( 7 ), UGen.ir( 36 + rnd.nextInt( 60 ))),
					UGen.ir( 1 )), UGen.ir( 0.2f )));
				
			g = UGen.ar( "Pan2", UGen.ar( "CombL", excitation, UGen.ir( 0.02f ), UGen.kr( "reciprocal", freq ),
				UGen.ir( 3 )), UGen.ir( rnd.nextFloat() * 2 - 1 ));
			
			f = f == null ? g : UGen.ar( "+", f, g );
		}
		
		for( int i = 0; i < 5; i++ ) {
			f = UGen.ar( "AllpassN", f, UGen.ir( 0.05f ), UGen.array(
				UGen.ir( rnd.nextFloat() * 0.05f ), UGen.ir( rnd.nextFloat() * 0.05f )), UGen.ir( 1 ));
		}
		
		f = UGen.ar( "LeakDC", f, UGen.ir( 0.995f ));

		def = new SynthDef( "JAleatoricQuartet", UGen.ar( "Out", UGen.ir( 0 ), f ));
		result.add( def );
	}

		// NoiseBusiness1
	{
		f = null;
	
		for( int i = 0; i < 4; i++ ) {
			g	= UGen.ar( "*", UGen.ar( "LFSaw", UGen.kr( "midicps", UGen.kr( "MulAdd",
				UGen.kr( "LFPulse", UGen.ir( 0.06f ), UGen.ir( 0 ), UGen.ir( 0.5f )),
					UGen.ir( 2 ), UGen.array( UGen.ir( 34 + rand2( rnd, 0.1f )),
											  UGen.ir( 34 + rand2( rnd, 0.1f )))))),
						UGen.ir( 0.01f ));
			f	= f == null ? g : UGen.ar( "+", f, g );
		}
		g	= UGen.kr( "MouseY", UGen.ir( 0.1f ), UGen.ir( 0.7f ));
		h	= UGen.kr( "LinExp", UGen.kr( "SinOsc", UGen.ir( 0.07f )),
			UGen.ir( -1 ), UGen.ir( 1 ), UGen.ir( 300 ), UGen.ir( 5000 ));
		f	= UGen.ar( "softclip", UGen.ar( "RLPF", f, h, g ));
		f	= UGen.ar( "softclip", UGen.ar( "RLPF", f, h, g ));
		def = new SynthDef( "JNoiseBusiness1", UGen.ar( "Out", UGen.ir( 0 ), f ));
		result.add( def );
	}

		// NoiseBusiness2
	{
		float pi2 = (float) Math.PI * 2;
	
		f = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 ) * rrand( rnd, 0.7f, 1.3f )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		g = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		f = UGen.ar( "max", UGen.ir( 0 ), UGen.ar( "*", UGen.ir( 0.1f ), UGen.ar( "+", f, g )));

		h = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 6, 24 ) * rrand( rnd, 0.7f, 1.3f )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		g = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		h = UGen.ar( "abs", UGen.ar( "*", UGen.ir( 0.1f ), UGen.ar( "+", h, g )));
		
		f = UGen.ar( "*", UGen.ar( "SinOsc", UGen.ir( midicps( rrand( rnd, 24, 108 ))), UGen.ir( rnd.nextFloat() * pi2 )),
				UGen.ar( "*", f, h ));

		f = UGen.ar( "Pan2", f, UGen.ir( rand2( rnd, 1.0f )));
		def = new SynthDef( "JNoiseBusiness2", UGen.ar( "Out", UGen.ir( 0 ), f ));
		result.add( def );
	}
	
		// NoiseBusiness3
	{
		float pi2 = (float) Math.PI * 2;
	
		f = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 ) * rrand( rnd, 0.7f, 1.3f )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		g = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		f = UGen.ar( "max", UGen.ir( 0 ), UGen.ar( "*", UGen.ir( 0.04f ), UGen.ar( "+", f, g )));

		h = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 6, 24 ) * rrand( rnd, 0.7f, 1.3f )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		g = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		h = UGen.ar( "abs", UGen.ar( "+", h, g ));
		
		f = UGen.ar( "*", UGen.ar( "LFTri", UGen.ir( midicps( rrand( rnd, 24, 108 ))), UGen.ir( rnd.nextFloat() * pi2 )),
				UGen.ar( "*", f, h ));

		f = UGen.ar( "Pan2", UGen.ar( "HPZ1", f ), UGen.ir( rand2( rnd, 1.0f )));
		def = new SynthDef( "JNoiseBusiness3", UGen.ar( "Out", UGen.ir( 0 ), f ));
		result.add( def );
	}

		// NoiseBusiness4
	{
		float pi2 = (float) Math.PI * 2;

		f = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 ) * rrand( rnd, 0.7f, 1.3f )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		g = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		f = UGen.ar( "max", UGen.ir( 0 ), UGen.ar( "*", UGen.ir( 0.04f ), UGen.ar( "+", f, g )));

		h = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 6, 24 ) * rrand( rnd, 0.7f, 1.3f )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		g = UGen.ar( "SinOsc", UGen.ir( exprand( rnd, 0.3f, 8 )),
				UGen.ir( rnd.nextFloat() * pi2 ));
		h = UGen.ar( "abs", UGen.ar( "+", h, g ));
		
		f = UGen.ar( "*", UGen.ar( "LFPulse", UGen.ir( 80 * rrand( rnd, 1, 32 )), UGen.ir( rnd.nextFloat() * pi2 ), UGen.ir( 0.1f )),
				UGen.ar( "*", f, h ));

		f = UGen.ar( "Pan2", UGen.ar( "LPZ2", f ), UGen.ir( rand2( rnd, 1.0f )));
		def = new SynthDef( "JNoiseBusiness4", UGen.ar( "Out", UGen.ir( 0 ), f ));
		result.add( def );
	}

		// CombDist
		// NOTE : here you see how controls are generated. We imitate
		//	SClang's SynthDef behaviour of grouping all controls of the
		//	same rate together in one UGen, therefore you pass an
		//	array to the contructor and access the indidual controls
		//	as the output channels.
		// - Also note the explicit specifications of the number of channels
		//	for the In UGen, which is a seperate second int argument.
		// - Also note how we have to treat 'reciprocal' in two different
		//	ways (Constant and UGen) to avoid extra overhead
		// - Also note how we create a final GraphElemArray to include
		//	the two 'dead ends' of the graph (XOut and FreeSelfWhenDone).
		//	This is always necessary in cases where UGens do not have outlets
	{
		Control		kCtrl	= Control.kr( new String[] { "out", "freq", "decay", "wet", "gate" }, new float[] { 0, 400, 2, 1, 1 });
		GraphElem	zin		= UGen.ar( "In", 2, kCtrl.getChannel( "out" ));
		GraphElem	freq	= UGen.kr( "Lag3", kCtrl.getChannel( "freq" ), UGen.ir( 0.1f ));
		GraphElem	env		= UGen.kr( "Linen", kCtrl.getChannel( "gate" ), UGen.ir( 1 ), UGen.ir( 1 ), UGen.ir( 1 ));
		GraphElem	wet		= UGen.kr( "Ramp", kCtrl.getChannel( "wet" ), UGen.ir( 0.1f ));
		GraphElem	comb	= UGen.ar( "CombN", zin, UGen.ir( 1.0f / midicps( 24 )), UGen.kr( "reciprocal", freq ),
								kCtrl.getChannel( "decay" ));
//		GraphElem	zout	= UGen.ar( "reverse", UGen.ar( "distort", comb ));
		GraphElem	zout	= UGen.ar( "distort", comb );
		
		f	= UGen.ar( "XOut", kCtrl.getChannel( "out" ), UGen.kr( "*", wet, env ), zout );
		g	= UGen.kr( "FreeSelfWhenDone", env );
		
		def = new SynthDef( "JCombDist", new GraphElemArray( new GraphElem[] { f, g }));
		result.add( def );
	}
	
		// RingMod
	{
		Control		kCtrl	= Control.kr( new String[] { "out", "freq", "wet", "gate" }, new float[] { 0, 800, 1, 1 });
		GraphElem	freq	= UGen.kr( "Ramp", kCtrl.getChannel( "freq" ), UGen.ir( 0.1f ));
		GraphElem	env		= UGen.kr( "Linen", kCtrl.getChannel( "gate" ), UGen.ir( 1 ), UGen.ir( 1 ), UGen.ir( 1 ));
		GraphElem	wet		= UGen.kr( "Ramp", kCtrl.getChannel( "wet" ), UGen.ir( 0.1f ));
		GraphElem	zin		= UGen.ar( "In", 2, kCtrl.getChannel( "out" ));
		GraphElem	zout	= UGen.ar( "*", zin, UGen.ar( "SinOsc", freq, UGen.array( UGen.ir( 0 ), UGen.ir( (float) Math.PI/2 ))));
		
		f	= UGen.ar( "XOut", kCtrl.getChannel( "out" ), UGen.kr( "*", wet, env ), zout );
		g	= UGen.kr( "FreeSelfWhenDone", env );
		
		def = new SynthDef( "JRingMod", new GraphElemArray( new GraphElem[] { f, g }));
		result.add( def );
	}

		return result;
	}
	
	public static void synthDefApiExample( Server myServer )
	{
		GraphElem	f		= null;
		GraphElem	g, h;
		Control		c		= Control.kr( new String[] { "resinv" }, new float[] { 0.5f });
		UGenChannel	reso	= c.getChannel( 0 );
		Synth		synth;
		Random 		r 		= new Random( System.currentTimeMillis() );
		String		defName	= "JNoiseBusiness1b";
		OSCBundle	bndl;
		SynthDef	def;
		long		time;

		for( int i = 0; i < 4; i++ ) {
			g = UGen.ar( "*", UGen.ar( "LFSaw", UGen.kr( "midicps", UGen.kr( "MulAdd",
				UGen.kr( "LFPulse", UGen.ir( 0.06f ), UGen.ir( 0 ), UGen.ir( 0.5f )),
					UGen.ir( 2 ), UGen.array( UGen.ir( 34 + r.nextFloat() * 0.2f ),
											  UGen.ir( 34 + r.nextFloat() * 0.2f ))))),
						UGen.ir( 0.01f ));
			f = (f == null) ? g : UGen.ar( "+", f, g );
		}
		h	= UGen.kr( "LinExp", UGen.kr( "SinOsc", UGen.ir( 0.07f )),
				UGen.ir( -1 ), UGen.ir( 1 ), UGen.ir( 300 ), UGen.ir( 5000 ));
		f	= UGen.ar( "softclip", UGen.ar( "RLPF", f, h, reso ));
		f	= UGen.ar( "softclip", UGen.ar( "RLPF", f, h, reso ));
		def = new SynthDef( defName, UGen.ar( "Out", UGen.ir( 0 ), f ));
		
		synth = Synth.basicNew( defName, myServer );
		try {
			def.send( myServer, synth.newMsg( myServer.asTarget(),
				new String[] { "resinv" }, new float[] { 0.98f }));
			time = System.currentTimeMillis();
			for( int i = 500; i < 5000; i += 250 ) {
				bndl = new OSCBundle( time + i );
				bndl.addPacket( synth.setMsg( "resinv", r.nextFloat() * 0.8f + 0.015f ));
				myServer.sendBundle( bndl );
			}
			bndl = new OSCBundle( time + 5500 );
			bndl.addPacket( synth.freeMsg() );
			myServer.sendBundle( bndl );
		}
		catch( IOException e1 ) {
			System.err.println( e1 );
		}
	}
	
	private static float exprand( Random rnd, float lo, float hi )
	{
		final double dlog = Math.log( hi ) - Math.log( lo );
		
		return( (float) Math.exp( rnd.nextDouble() * dlog ) * lo );
	}
	
	private static float rrand( Random rnd, float lo, float hi )
	{
		return( rnd.nextFloat() * (hi - lo) + lo );
	}

	private static float rand2( Random rnd, float x )
	{
		return( rnd.nextFloat() * 2 * x - x );
	}
	
	private static float midicps( float midi )
	{
		return( (float) (Math.exp( (midi - 69) / 12 * Math.log( 2 )) * 440) );
	}
}
