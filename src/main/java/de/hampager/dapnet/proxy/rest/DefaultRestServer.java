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
package de.hampager.dapnet.proxy.rest;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;

import de.hampager.dapnet.proxy.ProxyEventListener;
import io.netty.channel.Channel;
import jakarta.ws.rs.core.UriBuilder;

/**
 * This class provides the default REST server implementation for querying
 * connection status information.
 */
final class DefaultRestServer implements ProxyRestServer, ProxyEventListener {

	private static final Logger LOGGER = Logger.getLogger(DefaultRestServer.class.getName());
	private final Object lockObject = new Object();
	private final Map<String, ConnectionStatus> connections = new HashMap<>();
	private volatile Channel server;

	@Override
	public void onRegister(String profileName) {
		ConnectionStatus status = new ConnectionStatus(profileName);
		status.setLastUpdate(Instant.now());

		synchronized (lockObject) {
			connections.put(profileName, status);
		}
	}

	@Override
	public void onConnect(String profileName) {
		synchronized (lockObject) {
			ConnectionStatus status = connections.get(profileName);
			if (status != null) {
				Instant now = Instant.now();

				status.setLastUpdate(now);
				status.setConnectedSince(now);
				status.setState(ConnectionStatus.State.ONLINE);
			}
		}
	}

	@Override
	public void onDisconnect(String profileName, boolean reconnect) {
		synchronized (lockObject) {
			ConnectionStatus status = connections.get(profileName);
			if (status != null) {
				Instant now = Instant.now();

				status.setLastUpdate(now);
				status.setConnectedSince(null);
				status.setState(reconnect ? ConnectionStatus.State.CONNECTING : ConnectionStatus.State.OFFLINE);
			}
		}
	}

	@Override
	public void onShutdown() {
		stop();
	}

	@Override
	public Collection<ConnectionStatus> getConnections() {
		Collection<ConnectionStatus> result = null;
		synchronized (lockObject) {
			result = new ArrayList<ConnectionStatus>(connections.values());
		}
		return result;
	}

	@Override
	public ConnectionStatus getConnection(String name) {
		synchronized (lockObject) {
			ConnectionStatus result = connections.get(name);
			if (result != null) {
				return new ConnectionStatus(result);
			} else {
				return null;
			}
		}
	}

	@Override
	public ProxyEventListener getEventListener() {
		return this;
	}

	@Override
	public void start(String uri, int port) {
		LOGGER.log(Level.INFO, "Starting REST server on port {0,number,#}", port);

		// Endpoint configuration
		URI baseUri = UriBuilder.fromUri(uri).port(port).build();

		// Resource configuration
		ResourceConfig config = new ResourceConfig(ConnectionStatusResource.class, JacksonFeature.class);
		config.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(DefaultRestServer.this).to(ProxyRestServer.class);
			}
		});

		// Start the server
		server = NettyHttpContainerProvider.createServer(baseUri, config, true);
	}

	@Override
	public void stop() {
		LOGGER.info("Stopping REST server");

		Channel theServer = server;
		if (theServer != null) {
			try {
				theServer.close().sync();
			} catch (InterruptedException e) {
				LOGGER.warning("REST server shutdown interrupted");
			}
		}
	}

}
