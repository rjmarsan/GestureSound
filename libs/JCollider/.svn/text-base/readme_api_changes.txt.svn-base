In the course of the development of JCollider, some API changes were made. Generally, changing the API is considered bad because it breaks previous code (it is not backward compatible). However, I tried to minimize these changes. Generally, changes have only been made, when the API proved suboptimal or new features were added which would introduce ugly API when enforcing backward compatibility.

This document tries to help updating code that uses old versions of JCollider.

API changes from v0.35 to v0.36
	- in class de.sciss.jcollider. OSCResponderNode
		dropping IOException from remove()

		to compile you might need to remove old try ...Êcatch( IOException e ) ... clauses.

API changes from v0.28 to v0.29
	- in class de.sciss.jcollider.OSCMultiResponder
		not subclass of OSCReceiver anymore. new constructor:
		old signature:
			protected OSCMultiResponder( SocketAddress addr );
		new signature:
			protected OSCMultiResponder( OSCClient c );

		this change was necessary because Server now uses OSCClient and OSCReceiver is not visible anymore.

	- in class de.sciss.jcollider.OSCResponderNode
		new constructor:
		old signature:
			public OSCResponderNode( SocketAddress addr, String cmdName, OSCListener action );
		new signature:
			public OSCResponderNode( Server s, String cmdName, OSCListener action );

		this change was necessary because OSCMultiResponder is not a subclass of OSCReceiver anymore
		and hence requires a OSCClient to listen to. This client is taken from a Server, so the
		responder node also requires to specify a server.

		possible workaround: the old code probably often used the construction
			new OSCResponderNode( aServer.getAddr(), cmdName, action );
		so in this case, the code can simply be written as:
			new OSCResponderNode( aServer, cmdName, action );

		otherwise, a simple replacement for OSCResponderNode must be sought...


lastmod: 02-oct-06 sciss
