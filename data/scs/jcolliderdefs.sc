// simple electro kick drum
SynthDef(\kik, { |basefreq = 50, envratio = 3, freqdecay = 0.02, ampdecay = 0.5, outbus = 0|
   var   fenv = EnvGen.kr(Env([envratio, 1], [freqdecay], \exp), 1) * basefreq,
      aenv = EnvGen.kr(Env.perc(0.005, ampdecay), 1, doneAction:2);
      fenv=Pan2.ar(fenv,0.0);
   Out.ar(outbus, SinOsc.ar(fenv, 0.5pi, aenv));
}).send(s);
Synth(\kik);


SynthDef(\granny, {arg trigRate, rate=1, centerPos, dur=0.05, pan=0.2, amp = 0.4, buffer=0, cntrPosRandWidth=0.1, cntrPosRandFreq=10, durRandWidth=0.1,  durRandFreq=10, revVol=0.1, delayTime=4, decayTime=6, aDelTime=1, aDecTime=1, rateRandWidth=0.01, rateRandFreq=10;
var fc, granny, outSignal, revSignal;
granny = TGrains.ar(2,Impulse.ar(trigRate),buffer,rate + TRand.kr(-1*rateRandWidth, rateRandWidth, Impulse.kr(rateRandFreq)), centerPos + TRand.kr(-1*cntrPosRandWidth, cntrPosRandWidth, Impulse.kr(cntrPosRandFreq)), dur + TRand.kr(-1*durRandWidth, durRandWidth, Impulse.kr(durRandFreq)),WhiteNoise.kr(pan),amp,2);
revSignal = Mix.ar(granny) * revVol;
revSignal = Mix.ar(CombL.ar(revSignal, 0.1, {0.04.rand2 + 0.05}.dup(4) * delayTime,decayTime));
4.do({ revSignal = AllpassN.ar(revSignal, 0.150, [0.050.rand,0.051.rand] * aDelTime, aDecTime)});
Out.ar([0,1], granny + LeakDC.ar(revSignal));
}).send(s);

SynthDef(\grannyyy, {arg trigRate, rate=1, centerPos, dur=0.05, pan=0.2, amp = 0.4, buffer=0;
Out.ar([0,1], TGrains.ar(2,Impulse.ar(trigRate),buffer,rate,centerPos,dur,pan,amp,2));
}).store;



b = Buffer.read(s, "/Users/rj/Documents/workspace/MT4j/data/sounds/amiu.aif");
pwd

g = Synth(\grannyyy, [\trigRate, 1, \buffer, 1]);
g.set(\trigRate, 10);
g.set(\centerPos, 2.3);

Synth(\StereoPlaceholder).set(\inbus,20);

s=Synth(\stereosaw,[\out,20]);
s.set(\freq,555);

//808 kick. sexyyyyyy
SynthDef("kickDrum", { arg outbus=0, gate=1;
	var daNoise,daOsc,env1,env2,env3;
	//noise filter cutoff envelope
	//controlls cutoff pitch...0 to 80 Hz
	env1=Env.perc(0.001,1,80,-20); 
	//mix-amp envelope
	//controlls overall amplitude...0 to 1
	env2=Env.perc(0.001,1,1,-8); 
	//osc-pitch envelope
	//controlls pitch of the oscillator...0 to 80 Hz
	env3=Env.perc(0.001,1,80,-8); 
	//Attack noise portion of the sound
	//filter cutoff controlled by env1
	//+20 to move it into the audible
	//spectrum
	daNoise=LPF.ar(WhiteNoise.ar(1),EnvGen.kr(env1,gate)+20);
    //VCO portion of the sound
    //Osc pitch controlled by env3
    //+20 to move the pitch into the
    //audible spectrum
	daOsc=LPF.ar(SinOsc.ar(EnvGen.kr(env3,gate)+20),200); 
	//output
	Out.ar(outbus,Pan2.ar(
				Mix.ar([daNoise,daOsc]),
				0, //position
				//level controlled by env2
				EnvGen.kr(env2,gate,doneAction: 2) 
			);
		  );
}).store;


	SynthDef
	(
		"snare",
		{ 
			arg outbus=0, amp=1, pan=0;
			
			var eg1, eg2, snare, drum, out;
			
			// One of the envelopes is for the snare, the other's for the drum.
			eg1 = EnvGen.kr(Env.perc(0.01,0.3,amp),doneAction:2);
			eg2 = EnvGen.kr(Env.perc(0.01,0.2,amp/4));
			
			// A res. filter on white noise for the snare, a ring modded sine wave for the drum.
			snare = Resonz.ar(WhiteNoise.ar,1400,0.25,eg1);
			drum = SinOsc.ar(210,0,SinOsc.kr(70)*eg2);
			out = Pan2.ar(snare + drum, pan);
			
			Out.ar
			(
				outbus,
				out
			);
		}
	).store;


SynthDef(\kick, {|outbus=0 amp= 0.5, decay= 0.1, attack= 0.001, freq= 60|
	var env, snd;
	env= EnvGen.ar(Env.perc(attack, decay), doneAction:2);
	snd= SinOsc.ar(freq, 0, amp);
	Out.ar(outbus, Pan2.ar(snd*env, 0));
}).store;

Synth(\kick)

Synth(\kick, [\freq, 100])
Synth(\kick, [\freq, 80, \decay, 0.2])

//variation with impulse click
SynthDef(\kick2, {|amp= 0.5, decay= 0.1, freq= 60|
	var env, snd;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	snd= SinOsc.ar(freq, pi*0.5, amp);
	Out.ar(0, Pan2.ar(snd*env, 0));
}).store;



//variation with more sines
SynthDef(\kick3, {|outbus= 0, amp= 0.5, decay= 0.1, freq= 60|
	var env, snd;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	snd= Mix(SinOsc.ar([freq, freq*2, freq-15], 0, amp));
	Out.ar(outbus, Pan2.ar(snd*env, 0));
}).store;



/////////////////////////////////
SynthDef(\hat, {|outbus= 0, amp= 0.5, decay= 0.1, freq= 6000|
	var env, snd;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	snd= BPF.ar(GrayNoise.ar(amp), freq, 0.3);
	Out.ar(outbus, Pan2.ar(snd*env, 0));
}).store;

Synth(\hat)


//variation with cutoff lfo
SynthDef(\hat2, {|amp= 0.5, decay= 0.1, freq= 6000|
	var env, snd;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	snd= BPF.ar(GrayNoise.ar(amp), Line.ar(freq, 5, decay*0.5), env+0.1);
	Out.ar(0, Pan2.ar(snd*env, 0));
}).store;
Synth(\hat)
Synth(\hat2)

//variation with ringing sine
SynthDef(\hat3, {|amp= 0.5, decay= 0.1, freq= 6000|
	var env, snd, snd2;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	snd= BPF.ar(GrayNoise.ar(amp), Line.ar(freq, 5, decay*0.5), env+0.1);
	snd2= Mix(SinOsc.ar([4007, 6500, 5030], 0, env*0.1));
	Out.ar(0, Pan2.ar(snd+snd2*env, 0));
}).store;
Synth(\hat3)

/////////////////////////////////
SynthDef(\snare, {|outbus= 0, amp= 0.5, decay= 0.3, freq= 1000|
	var env, snd;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	snd= BPF.ar(GrayNoise.ar(amp), freq, 3);
	Out.ar(outbus, Pan2.ar(snd*env, 0));
}).store;

//variation with resonant low-pass filter
SynthDef(\snare2, {|amp= 0.5, decay= 0.3, freq= 1000|
	var env, snd;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	snd= RLPF.ar(GrayNoise.ar(amp), freq, Line.ar(0.1, 0.9, decay));
	Out.ar(0, Pan2.ar(snd*env, 0));
}).store;


//variation with 2 snds with different envelopes
SynthDef(\snare3, {|amp= 0.5, decay= 0.3, freq= 1000|
	var env, snd, env2, snd2;
	env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
	env2= EnvGen.ar(Env.perc(0.05, decay*0.5));
	snd= RLPF.ar(GrayNoise.ar(amp), freq, Line.ar(0.1, 0.9, decay));
	snd2= WhiteNoise.ar(amp)*env2;
	Out.ar(0, Pan2.ar(snd+snd2*env, 0));
}).store;





(
SynthDef(\stereosaw, {arg out=0, freq=333, amp=0.4, pan=0.0, cutoff=20000; // we add a new argument
	var signal;
	signal = RLPF.ar(LFSaw.ar(freq, 0, amp), cutoff, 0.2);
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).load(s) // we load the synthdef into the server
)
(
SynthDef(\stereosine, {arg out=0, freq=333, amp=0.4, pan=0.0; // we add a new argument
	var signal;
	signal = SinOsc.ar(freq, 0, amp);
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).load(s) // we load the synthdef into the server
)
(
SynthDef(\stereosquare, {arg out=0, freq=333, amp=0.4, pan=0.0, cutoff=20000; // we add a new argument
	var signal;
	signal = RLPF.ar(LFPulse.ar(freq, 0,0.5, amp), cutoff, 0.2);
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).load(s) // we load the synthdef into the server
)
(
SynthDef(\stereotriangle, {arg out=0, freq=333, amp=0.4, pan=0.0; // we add a new argument
	var signal;
	signal = LFTri.ar(freq, 0, amp);
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).load(s) // we load the synthdef into the server
)

(
SynthDef(\stereosawWenv, {arg out=0, freq=333, amp=0.4, pan=0.0, cutoff=20000; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc, doneAction:2); // doneAction gets rid of the synth
	signal = RLPF.ar(LFSaw.ar(freq, 0, amp) * env, cutoff, 0.2);
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).store; // we load the synthdef into the server
)

(
SynthDef(\stereosquareWenv, {arg out=0, freq=333, amp=0.4, pan=0.0, cutoff=20000; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc, doneAction:2); // doneAction gets rid of the synth
	signal = RLPF.ar(LFPulse.ar(freq, 0,0.5, amp) * env, cutoff, 0.2);
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).store; // we load the synthdef into the server
)

Synth(\stereosawWenv,[\cutoff,600]);

(
SynthDef(\stereosineWenv, {arg out=0, freq=333, amp=0.4, pan=0.0; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc, doneAction:2); // doneAction gets rid of the synth
	signal = SinOsc.ar(freq, 0, amp) * env;
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).store; // we load the synthdef into the server
)

(
SynthDef(\stereotriangleWenv, {arg out=0, freq=333, amp=0.4, pan=0.0; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc, doneAction:2); // doneAction gets rid of the synth
	signal = LFTri.ar(freq, 0, amp) * env;
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).store; // we load the synthdef into the server
)


(		// stereo
		SynthDef(\StereoPlaceholder, {arg inbus=0,
							outbus=0;
							
		 var sig; 
		 sig = InFeedback.ar(inbus, 2); 
		 Out.ar(outbus, sig) 
		}).load(s); 	
)

(		// stereo
		SynthDef(\StereoPlaceholder, {arg inbus=0, outbus=0;
		 Out.ar(outbus,InFeedback.ar(inbus,2));
		}).store; 	
)

(		// stereo
		SynthDef(\StereoMixerChannel, {arg inbus=0, outbus=0, amp=0.5, pan=0.0, dur=1, gate;		 Out.ar(outbus,Limiter.ar(Pan2.ar(InFeedback.ar(inbus,2)* EnvGen.ar(Env.adsr(0.01, dur, amp, 0.7), gate, doneAction:13)*amp,pan),0.8,0.01));
		}).store; 	
)

(
SynthDef("testPlayBuff", { arg out=0,bufnum=0,rate=1,loop=0;
	Out.ar(out,
		PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum)*rate,loop:loop)
	)
}).store;
)

b=Buffer.read(s, "../../lib/snds/DrumLoop.aiff");

b=Buffer.read(s, "/Users/rj/Documents/workspace/JReactable/src/DrumLoop2.aiff");
b.play(true,1);
q=Synth(\testPlayBuff,[\out, 0, \bufnum, b,\rate, 3,\loop, 1]);

(
TimeShift : UGen {
        *ar {|numChannels, bufnum, speed=1.0, loop=0, windowSize=0.1|
                ^PitchShift.ar(
                        PlayBuf.ar(numChannels, bufnum,
                                BufRateScale.kr(bufnum)*speed, loop:loop),
                        windowSize, speed.reciprocal.abs, timeDispersion: 0.01)
        }
} 
)