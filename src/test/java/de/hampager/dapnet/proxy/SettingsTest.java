/*
 * Copyright (C) 2017 Amateurfunkgruppe der RWTH Aachen
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * Test cases for the proxy settings.
 *
 * @author Philipp Thiel
 */
public class SettingsTest {

	@Test
	public void testFromProperties() {
		Properties props = createProperties();

		ConnectionSettings s = new ConnectionSettings(props);

		// General
		assertEquals("testProfile", s.getProfileName());
		assertEquals(0, s.getReconnectDelay().toSeconds());

		// DAPNET
		assertEquals("proxyTest", s.getDapnetAuthName());
		assertEquals("test1", s.getDapnetAuthKey());

		InetSocketAddress address = new InetSocketAddress("localhost", 43434);
		assertEquals(address, s.getDapnetAddress());

		// Transmitter
		address = new InetSocketAddress("localhost", 43435);
		assertEquals(address, s.getTransmitterAddress());
		assertEquals(30, s.getTransmitterTimeout().toSeconds());
	}

	private static Properties createProperties() {
		Properties props = new Properties();

		props.setProperty("profile.name", "testProfile");
		props.setProperty("profile.reconnectDelay", "0");
		// DAPNET configuration
		props.setProperty("dapnet.auth.name", "proxyTest");
		props.setProperty("dapnet.auth.key", "test1");
		props.setProperty("dapnet.hostname", "localhost");
		props.setProperty("dapnet.port", "43434");
		// Transmitter configuration
		props.setProperty("transmitter.hostname", "localhost");
		props.setProperty("transmitter.port", "43435");
		props.setProperty("transmitter.timeout", "30");

		return props;
	}

}
