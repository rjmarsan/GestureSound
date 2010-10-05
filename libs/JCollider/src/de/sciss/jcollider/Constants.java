/*
 *  Constants.java
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
 */

package de.sciss.jcollider;

/**
 *	A utility interface that
 *	different classes can implements to
 *	gain access to commonly used constants.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.28, 31-Jul-06
 */
public interface Constants
{
	// add actions
	public static final int		kAddToHead		= 0;
	public static final int		kAddToTail		= 1;
	public static final int		kAddBefore		= 2;
	public static final int		kAddAfter		= 3;
	public static final int		kAddReplace		= 4;
	
	// rate symbols
	public static final Object	kScalarRate		= "scalar";
	public static final Object	kControlRate	= "control";
	public static final Object	kAudioRate		= "audio";
	public static final Object	kDemandRate		= "demand";
	
	// dumpOSC
	public static final int		kDumpOff		= 0;
	public static final int		kDumpText		= 1;
	public static final int		kDumpHex		= 2;
	public static final int		kDumpBoth		= 3;
	
	// -------------- sound file write formats --------------

	/**
	 *	header format used for <code>/b_write</code>: AIFF
	 */
	public static final String	kHeaderAIFF		= "aiff";
	/**
	 *	header format used for <code>/b_write</code>: NeXT / AU
	 */
	public static final String	kHeaderNeXT		= "next";
	/**
	 *	header format used for <code>/b_write</code>: WAVE
	 */
	public static final String	kHeaderWAVE		= "wav";
	/**
	 *	header format used for <code>/b_write</code>: IRCAM
	 */
	public static final String	kHeaderIRCAM	= "ircam";
	/**
	 *	header format used for <code>/b_write</code>: Raw (headerless)
	 */
	public static final String	kHeaderRaw		= "raw";

	// -------------- sound file write sample encodings --------------

	/**
	 *	sample encoding used for <code>/b_write</code>: 8-bit integer
	 */
	public static final String	kSampleInt8		= "int8";
	/**
	 *	sample encoding used for <code>/b_write</code>: 16-bit integer
	 */
	public static final String	kSampleInt16	= "int16";
	/**
	 *	sample encoding used for <code>/b_write</code>: 24-bit integer
	 */
	public static final String	kSampleInt24	= "int24";
	/**
	 *	sample encoding used for <code>/b_write</code>: 32-bit integer
	 */
	public static final String	kSampleInt32	= "int32";
	/**
	 *	sample encoding used for <code>/b_write</code>: single precision floating point
	 */
	public static final String	kSampleFloat	= "float";
	/**
	 *	sample encoding used for <code>/b_write</code>: double precision floating point
	 */
	public static final String	kSampleDouble	= "double";
	/**
	 *	sample encoding used for <code>/b_write</code>: 8-bit mu-law noise
	 */
	public static final String	kSampleMuLaw	= "mulaw";
	/**
	 *	sample encoding used for <code>/b_write</code>: 8-bit a-law noise
	 */
	public static final String	kSampleALaw		= "alaw";
	
	// -------------- done actions --------------
	/**
	 *	done action: do nothing when the UGen is finished
	 */
	public static final int		kDoneNothing			= 0;
	/**
	 *	done action: pause the enclosing synth, but do not free it
	 */
	public static final int		kDonePause				= 1;
	/**
	 *	done action: free the enclosing synth
	 */
	public static final int		kDoneFree				= 2;
	/**
	 *	done action: free both this synth and the preceding node
	 */
	public static final int		kDoneFreePred			= 3;
	/**
	 *	done action: free both this synth and the following node
	 */
	public static final int		kDoneFreeSucc			= 4;
	/**
	 *	done action: free this synth; if the preceding node is a group then do g_freeAll on it, else free it
	 */
	public static final int		kDoneFreePredGroup		= 5;
	/**
	 *	done action: free this synth; if the following node is a group then do g_freeAll on it, else free it
	 */
	public static final int		kDoneFreeSuccGroup		= 6;
	/**
	 *	done action: free this synth and all preceding nodes in this group
	 */
	public static final int		kDoneFreeAllPred		= 7;
	/**
	 *	done action: free this synth and all following nodes in this group
	 */
	public static final int		kDoneFreeAllSucc		= 8;
	/**
	 *	done action: free this synth and pause the preceding node
	 */
	public static final int		kDoneFreePausePred		= 9;
	/**
	 *	done action: free this synth and pause the following node
	 */
	public static final int		kDoneFreePauseSucc		= 10;
	/**
	 *	done action: free this synth and if the preceding node is a group then do g_deepFree on it, else free it
	 */
	public static final int		kDoneFreePredGroupDeep	= 11;
	/**
	 *	done action: free this synth and if the following node is a group then do g_deepFree on it, else free it
	 */
	public static final int		kDoneFreeSuccGroupDeep	= 12;
	/**
	 *	done action: free this synth and all other nodes in this group (before and after)
	 */
	public static final int		kDoneFreeAll			= 13;
	/**
	 *	done action: free the enclosing group and all nodes within it (including this synth)
	 */
	public static final int		kDoneFreeGroup			= 14;
}