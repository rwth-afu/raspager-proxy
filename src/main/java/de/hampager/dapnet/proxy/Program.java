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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.hampager.dapnet.proxy;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.hampager.dapnet.proxy.rest.ProxyRestServer;

/**
 * This class contains the application entry point.
 */
public final class Program {

	private static final String REST_PORT_KEY = "dapnet.proxy.rest.port";
	private static final Logger LOGGER = Logger.getLogger(Program.class.getName());

	private ProxyConnectionManager manager;
	private ProxyRestServer restServer;

	public static void main(String[] args) {
		if (args.length < 1) {
			LOGGER.log(Level.SEVERE, "No configuration file provided.");
			System.exit(1);
		}

		LOGGER.log(Level.INFO, "DAPNET Proxy Version {0}", Program.class.getPackage().getImplementationVersion());

		try {
			Program prog = new Program();
			prog.run(args);
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Fatal exception in main.", ex);
			System.exit(1);
		}
	}

	public void run(String... configFiles) throws Exception {
		if (configFiles.length < 1) {
			throw new IllegalArgumentException("No configuration files provided.");
		}

		if (manager == null) {
			Runtime.getRuntime().addShutdownHook(new ShutdownHook());

			startRestServer();
			startProxyManager();
		}

		for (String config : configFiles) {
			registerService(config);
		}
	}

	private void startRestServer() throws Exception {
		Integer port = Integer.getInteger(REST_PORT_KEY);
		if (port == null) {
			LOGGER.fine("REST server disabled.");
			return;
		}

		restServer = ProxyRestServer.createDefault();
		restServer.start("http://0.0.0.0/", port);
	}

	private void startProxyManager() {
		ProxyConnectionEventListener listener = null;
		if (restServer != null) {
			listener = restServer.getEventListener();
		}

		manager = new ProxyConnectionManager(listener);
	}

	private void registerService(String configFile) {
		try {
			ConnectionProfile settings = ConnectionProfile.fromFile(configFile);
			manager.openConnection(settings);
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Failed to load configuration file.", ex);
		}
	}

	private final class ShutdownHook extends Thread {

		public ShutdownHook() {
			super("ShutdownHook");
		}

		@Override
		public void run() {
			try {
				if (restServer != null) {
					restServer.stop();
				}
			} catch (Exception ex) {
				LOGGER.log(Level.SEVERE, "Failed to stop the REST server.", ex);
			}

			try {
				if (manager != null) {
					manager.shutdown();
				}
			} catch (Exception ex) {
				LOGGER.log(Level.SEVERE, "Failed to stop the proxy manager.", ex);
			}
		}

	}

}
