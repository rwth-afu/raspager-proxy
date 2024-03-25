package de.hampager.dapnet.proxy;

import java.util.Objects;

class ProxyConnection {

	private final String profileName;
	private volatile Connection transmitterConnection;
	private volatile Connection dapnetConnection;

	public ProxyConnection(String profileName) {
		this.profileName = Objects.requireNonNull(profileName, "Profile name must not be null.");
	}

	public String getProfileName() {
		return profileName;
	}

	public void setDapnetConnection(Connection dapnetConnection) {
		this.dapnetConnection = Objects.requireNonNull(dapnetConnection, "Connection must not be null.");
	}

	public void setTransmitterConnection(Connection transmitterConnection) {
		this.transmitterConnection = Objects.requireNonNull(transmitterConnection, "Connection must not be null.");
	}

	public void sendToDapnet(String message) {
		try {
			final Connection connection = transmitterConnection;
			if (connection != null) {
				connection.sendMessage(message);
			}
		} catch (Exception ex) {

		}
	}

	public void sendToTransmitter(String message) {
		try {
			final Connection connection = dapnetConnection;
			if (connection != null) {
				connection.sendMessage(message);
			}
		} catch (Exception ex) {

		}
	}

	public void close() {

	}

}
