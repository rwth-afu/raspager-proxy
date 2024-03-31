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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * This class manages the proxy connections.
 */
final class ProxyConnectionManager {

	private static final Logger LOGGER = Logger.getLogger(ProxyConnectionManager.class.getName());
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private final ProxyConnectionEventListener listener;
	private final Map<String, ProxyConnection> connections = new HashMap<>();
	private volatile boolean shutdownRequested = false;

	/**
	 * Creates a new proxy connection manager instance.
	 *
	 * @param listener Proxy event listener to use or {@code null}.
	 */
	public ProxyConnectionManager(ProxyConnectionEventListener listener) {
		this.listener = listener;
	}

	/**
	 * Opens a new proxy connection.
	 *
	 * @param profile Connection profile
	 */
	public void openConnection(final ConnectionProfile profile) {
		if (shutdownRequested) {
			LOGGER.warning("Proxy connection manager is shuttding down; not adding new connections.");
			return;
		}

		ProxyConnection connection = new ProxyConnection(workerGroup, listener, profile);
		connections.put(profile.getName(), connection);

		workerGroup.execute(connection);

		if (listener != null) {
			listener.onRegister(profile.getName());
		}
	}

	/**
	 * Stops the proxy manager and closes all open connections.
	 */
	public void shutdown() {
		shutdownRequested = true;

		LOGGER.info("Shutting down proxy connection manager ...");

		try {
			workerGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Proxy connection manager shutdown interrupted.");
		}

		LOGGER.info("Proxy manager has been shut down.");
	}

}
