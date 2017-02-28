package bomber.networking;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * Server side representation of (the state of) a client
 */
public class ServerClientInfo {
	// socket address (IP:Port) of the client, guaranteed to be unique
	private SocketAddress sockAddr;

	// id of the player, uniqueness required
	// randomly generated by the server
	private int id;

	// name of the player, uniqueness required
	private final String name;

	// a client is either in the lobby or in a room
	private boolean inLobby = true;
	private boolean inRoom = false;
	private boolean readyToPlay = false;

	private ServerRoom room;

	// time of last packet received from the client in seconds
	private long timeStamp;

	// round trip delay of the client in milliseconds (measured by time of
	// acknowledgement of packets)
	private long roundTripDelay;

	// the next sequence number that should be used for a packet that will be
	// sent to the client
	private short nextPacketSequence = 0;

	// keep track of up to 100 packets sent to the client
	private int nextPacketHistoryIndex = 0;
	private final int maxPacketHistoryIndex = 99;
	private ArrayList<PacketHistoryEntry> packetHistoryList = new ArrayList<PacketHistoryEntry>(
			maxPacketHistoryIndex + 1);

	/*
	 * keep track of up to 100 packet sequence and time stamp received from the
	 * client for duplicate packet detection
	 */
	private int nextSequenceHistoryIndex = 0;
	private final int maxSequenceHistoryIndex = 99;
	private ArrayList<Entry<Short, Long>> sequenceHistoryList = new ArrayList<>(maxSequenceHistoryIndex + 1);

	/**
	 * Construct a new client representation for the server
	 * 
	 * @param sockAddr
	 *            the socket address of the client
	 * @param name
	 *            the name of the client
	 */
	public ServerClientInfo(SocketAddress sockAddr, String name) {
		this.sockAddr = sockAddr;
		this.name = name;
		this.timeStamp = Instant.now().getEpochSecond();
		for (int i = 0; i <= maxPacketHistoryIndex; i++) {
			packetHistoryList.add(null);
		}
		for (int i = 0; i <= maxSequenceHistoryIndex; i++) {
			sequenceHistoryList.add(null);
		}
	}

	public String toString() {
		return String.format("SocketAddress: %s, TimeStamp: %d", sockAddr, timeStamp);
	}

	/**
	 * Get the socket address (IP:Port) of the client
	 * 
	 * @return the socket address
	 */
	public SocketAddress getSocketAddress() {
		return sockAddr;
	}

	/**
	 * Get the name of the client
	 * 
	 * @return the name of the client
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the id of the client
	 * 
	 * @return the id of the client
	 */
	public int getID() {
		return id;
	}

	/**
	 * Get time of last packet received from the client in seconds
	 * 
	 * @return the time in seconds
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Get round trip delay of the client in milliseconds
	 * 
	 * @return the delay in milliseconds
	 */
	public long getRoundTripDelay() {
		return roundTripDelay;
	}

	/**
	 * Get the sequence number that should be used for the next packet to the
	 * client and increment the counter
	 * 
	 * @return the sequence number that should be used
	 */
	public short getNextPacketSequenceAndIncrement() {
		return nextPacketSequence++;
	}

	/**
	 * Get the list of up to 100 recent packets that have been sent to the
	 * client
	 * 
	 * @return the list of packets that have been sent to the client
	 */
	public ArrayList<PacketHistoryEntry> getPacketHistoryList() {
		return packetHistoryList;
	}

	/**
	 * Set the id of the client
	 * 
	 * @param id
	 *            the id of the client
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Set round trip delay of the client in milliseconds Should be called each
	 * time an acknowledgement packet is received from the client
	 * 
	 * @param delay
	 *            the delay in milliseconds
	 */
	public void setRoundTripDelay(long delay) {
		this.roundTripDelay = delay;
	}

	/**
	 * Update the time of last packet received from the client in seconds Should
	 * be called each time a packet is received from the client
	 */
	public void updateTimeStamp() {
		this.timeStamp = Instant.now().getEpochSecond();
	}

	/**
	 * Insert a packet into the packet history list for later retransmission (in
	 * case not acknowledged by the client)
	 * 
	 * @param packetSequence
	 *            the sequence number of the packet
	 * @param packet
	 *            the packet to insert
	 */
	public void insertPacket(short packetSequence, DatagramPacket packet) {
		// index wraps around when there are already 100 packets in history
		// (older packets will be overwritten)
		if (nextPacketHistoryIndex > maxPacketHistoryIndex) {
			nextPacketHistoryIndex = 0;
		}

		PacketHistoryEntry phe = packetHistoryList.get(nextPacketHistoryIndex);
		if (phe == null) {
			packetHistoryList.set(nextPacketHistoryIndex,
					new PacketHistoryEntry(packetSequence, packet.getData(), packet.getLength()));
		} else {
			phe.reset(packetSequence, packet.getData(), packet.getLength());
		}

		nextPacketHistoryIndex++;
	}

	/**
	 * Determine whether a packet received from the client is duplicate based on
	 * its sequence number
	 * 
	 * @param sequence
	 *            the sequence number of the packet
	 * @return true if the packet is duplicate
	 */
	public boolean isSequenceDuplicate(short sequence) {
		for (Entry<Short, Long> e : sequenceHistoryList) {
			if (e != null && e.getKey() == sequence) {
				if (System.currentTimeMillis() - e.getValue() < 25000) {
					return true;
				} else {
					e.setValue(System.currentTimeMillis());
					return false;
				}
			}
		}

		if (nextSequenceHistoryIndex > maxSequenceHistoryIndex) {
			nextSequenceHistoryIndex = 0;
		}

		Entry<Short, Long> entry = new AbstractMap.SimpleEntry<Short, Long>(sequence, System.currentTimeMillis());
		sequenceHistoryList.set(nextSequenceHistoryIndex, entry);
		nextSequenceHistoryIndex++;

		return false;
	}

	/**
	 * Returns true if the player is in lobby
	 * 
	 * @return true if the player is in lobby
	 */
	public boolean isInLobby() {
		return inLobby;
	}

	/**
	 * Returns true if the player is in a room
	 * 
	 * @return true if the player is in a room
	 */
	public boolean isInRoom() {
		return inRoom;
	}

	/**
	 * Returns true if the player is in a game
	 * 
	 * @return true if the player is in a game
	 */
	public boolean isInGame() {
		if (inLobby) {
			return false;
		} else {
			// a player is in room when not in lobby
			if (room == null) {
				return false;
			} else {
				return room.isInGame();
			}
		}
	}

	/**
	 * Set whether the player is in lobby
	 * 
	 * @param inLobby
	 *            whether the player is in lobby
	 */
	public void setInLobby(boolean inLobby) {
		this.inLobby = inLobby;
		this.inRoom = !inLobby;
	}

	/**
	 * Set whether the player is in room
	 * 
	 * @param inRoom
	 *            whether the player is in room
	 */
	public void setInRoom(boolean inRoom) {
		this.inRoom = inRoom;
		this.inLobby = !inRoom;
	}

	/**
	 * Get the room the player is currently in
	 * 
	 * @return the room
	 */
	public ServerRoom getRoom() {
		return room;
	}

	/**
	 * Set the room the player is currently in
	 * 
	 * @param room
	 *            the room
	 */
	public void setRoom(ServerRoom room) {
		this.room = room;
	}

	/**
	 * Returns true if the player is ready to play the game (when it is in a
	 * room)
	 * 
	 * @return true if the player is ready to play
	 */
	public boolean isReadyToPlay() {
		return readyToPlay;
	}

	/**
	 * Set whether the player is ready to play the game
	 * 
	 * @param readyToPlay
	 *            true if the player is ready to play
	 */
	public void setReadyToPlay(boolean readyToPlay) {
		this.readyToPlay = readyToPlay;
	}
}
