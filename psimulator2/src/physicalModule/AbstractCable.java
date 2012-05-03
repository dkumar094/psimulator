/*
 * created 2.5.2012
 */

package physicalModule;

import dataStructures.packets.L2Packet;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Parent of cables.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class AbstractCable implements Loggable{

	/**
	 * ID from configuration file
	 */
	public final int configID;
	/**
	 * ID of first connector device.
	 * (pro posilani paketu Martinovi)
	 */
	private int idFirstDevice;
	/**
	 * ID of second connector device.
	 * (pro posilani paketu Martinovi)
	 */
	private int idSecondDevice;

	protected AbstractSimulatorSwitchport firstCon;
	protected AbstractSimulatorSwitchport secondCon;

	public AbstractCable(int configID) {
		this.configID = configID;
	}

	/**
	 * Vraci to switchport na druhym konci kabelu nez je ten zadanej.
	 * Returns switchport on the other end of the cable.
	 * @param one
	 * @return
	 */
	public AbstractSimulatorSwitchport getTheOtherSwitchport(Switchport one) {
		if (one == firstCon) {
			return secondCon;
		} else if (one == secondCon) {
			return firstCon;
		} else {
			Logger.log(Logger.ERROR, LoggingCategory.PHYSICAL, "Wrong calling of method getTheOtherSwitchport("+one+") on cable with configID " + configID);
			return null;
		}
	}

	public void setFirstDeviceId(Integer id) {
		this.idFirstDevice = id;
	}

	public void setSecondDeviceId(Integer id) {
		this.idSecondDevice = id;
	}

	public int getFirstIdDevice() {
		return idFirstDevice;
	}

	public int getSecondIdDevice() {
		return idSecondDevice;
	}

	public class CableItem {
		public final L2Packet packet;
		public final int sourceID;
		public final int destinationID;
		public final int cableID;

		public CableItem(L2Packet packet, int source_ID, int destination_ID, int cabel_ID) {
			this.packet = packet;
			this.sourceID = source_ID;
			this.destinationID = destination_ID;
			this.cableID = cabel_ID;
		}

		@Override
		public String toString() {
			return String.format("sourceID=%d, destinationID=%d, cableID=%d, packet=", sourceID, destinationID, cableID)+packet.toStringWithData();
		}
	}
}
