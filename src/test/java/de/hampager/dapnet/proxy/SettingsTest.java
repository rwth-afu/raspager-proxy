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
		assertEquals(0, s.getReconnectSleepTime());

		// Frontend
		assertEquals("proxyTest", s.getFrontendName());
		assertEquals("test1", s.getFrontendKey());

		InetSocketAddress address = new InetSocketAddress("localhost", 43434);
		assertEquals(address, s.getFrontendAddress());

		// Backend
		address = new InetSocketAddress("localhost", 43435);
		assertEquals(address, s.getBackendAddress());
		assertEquals(30000, s.getBackendTimout());
	}

	private static Properties createProperties() {
		Properties props = new Properties();

		props.setProperty("profileName", "testProfile");
		props.setProperty("reconnectSleepTime", "0");
		// Frontend configuration
		props.setProperty("frontend.name", "proxyTest");
		props.setProperty("frontend.key", "test1");
		props.setProperty("frontend.host", "localhost");
		props.setProperty("frontend.port", "43434");
		// Backend configuration
		props.setProperty("backend.host", "localhost");
		props.setProperty("backend.port", "43435");
		props.setProperty("backend.timeout", "30000");

		return props;
	}

}
