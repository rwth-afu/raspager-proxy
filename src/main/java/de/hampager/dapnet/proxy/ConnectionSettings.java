/*
 * Copyright (C) 2017-2024 Amateurfunkgruppe an der RWTH Aachen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hampager.dapnet.proxy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.Properties;

/**
 * This class contains the connection profile settings.
 */
final class ConnectionSettings {

	private String profileName;
	private String authName;
	private String authKey;
	private SocketAddress dapnetAddress;
	private SocketAddress transmitterAddress;
	private Duration reconnectDelay;
	private Duration transmitterTimeout;

	/**
	 * Creates a settings instance by loading the settings from the given
	 * properties.
	 *
	 * @param props Properties to use.
	 * @throws NullPointerException If a required settings is not found.
	 */
	public ConnectionSettings(Properties props) {
		getCommonConfig(props);
		getDapnetConfig(props);
		getTransmitterConfig(props);
	}

	private void getCommonConfig(Properties props) {
		profileName = getString(props, "profile.name");
		if (profileName.isEmpty()) {
			throw new IllegalArgumentException("Profile name must not be empty.");
		}

		long tmpLong = getLong(props, "profile.reconnectDelay");
		if (tmpLong < 0) {
			throw new IllegalArgumentException("Reconnect sleep time cannot be negative.");
		}

		reconnectDelay = Duration.ofSeconds(tmpLong);
	}

	private void getDapnetConfig(Properties props) {
		authName = getString(props, "dapnet.auth.name");
		authKey = getString(props, "dapnet.auth.key");
		dapnetAddress = getAddress(props, "dapnet.hostname", "dapnet.port");
	}

	private void getTransmitterConfig(Properties props) {
		transmitterAddress = getAddress(props, "transmitter.hostname", "transmitter.port");

		long tmpLong = getLong(props, "transmitter.timeout");
		if (tmpLong < 0) {
			throw new IllegalArgumentException("Transmitter timeout cannot be negative.");
		}

		transmitterTimeout = Duration.ofSeconds(tmpLong);
	}

	/**
	 * Loads settings from the given file.
	 *
	 * @param filename Configuration file to load.
	 * @return Loaded settings
	 * @throws FileNotFoundException If the file does not exist.
	 * @throws IOException           If the file could not be read.
	 */
	public static ConnectionSettings fromFile(String filename) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		try (FileInputStream fin = new FileInputStream(filename)) {
			props.load(fin);
		}

		return new ConnectionSettings(props);
	}

	/**
	 * Returns the profile name.
	 *
	 * @return Name of the loaded profile.
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * Returns the sleep time between retries.
	 *
	 * @return Time to sleep between retries.
	 */
	public Duration getReconnectDelay() {
		return reconnectDelay;
	}

	/**
	 * Gets the Transmitter server address.
	 *
	 * @return Transmitter server address.
	 */
	public SocketAddress getDapnetAddress() {
		return dapnetAddress;
	}

	/**
	 * Gets the transmitter name used to authenticate with the DAPNET server.
	 *
	 * @return Transmitter authentication name.
	 */
	public String getDapnetAuthName() {
		return authName;
	}

	/**
	 * Gets the auth key used to authenticate with the DAPNET server.
	 *
	 * @return authentication key.
	 */
	public String getDapnetAuthKey() {
		return authKey;
	}

	/**
	 * Gets the transmitter server address.
	 *
	 * @return Transmitter server address.
	 */
	public SocketAddress getTransmitterAddress() {
		return transmitterAddress;
	}

	/**
	 * Gets the transmitter connection timeout.
	 *
	 * @return Transmitter connection timeout.
	 */
	public Duration getTransmitterTimeout() {
		return transmitterTimeout;
	}

	private static String getString(Properties props, String key) {
		String value = props.getProperty(key);
		if (value != null) {
			return value;
		} else {
			throw new NullPointerException("Key not found: " + key);
		}
	}

	private static SocketAddress getAddress(Properties props, String host, String port) {
		return new InetSocketAddress(getString(props, host), getInt(props, port));
	}

	private static int getInt(Properties props, String key) {
		return Integer.parseInt(getString(props, key));
	}

	private static long getLong(Properties props, String key) {
		return Long.parseLong(getString(props, key));
	}

}
