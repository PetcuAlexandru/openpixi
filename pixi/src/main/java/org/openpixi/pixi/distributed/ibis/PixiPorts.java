package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.PortType;

/**
 * Defines ibis ports used in pixi.
 */
public class PixiPorts {

	/** For communication from slaves to master. */
	public static final PortType GATHER_PORT = new PortType(
			PortType.COMMUNICATION_RELIABLE,
			PortType.SERIALIZATION_OBJECT,
			PortType.RECEIVE_EXPLICIT,
			PortType.CONNECTION_MANY_TO_ONE);

	public static final String GATHER_PORT_ID = "gather";

	public static final PortType ONE_TO_ONE_PORT = new PortType(
			PortType.COMMUNICATION_RELIABLE,
			PortType.SERIALIZATION_OBJECT,
			PortType.RECEIVE_AUTO_UPCALLS,
			PortType.RECEIVE_EXPLICIT,
			PortType.CONNECTION_ONE_TO_ONE);

	public  static final String DISTRIBUTE_PORT_ID = "distribute";

	public static final PortType[] ALL_PORTS = {GATHER_PORT, ONE_TO_ONE_PORT};
}
