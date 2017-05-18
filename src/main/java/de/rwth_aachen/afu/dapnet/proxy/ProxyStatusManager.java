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
package de.rwth_aachen.afu.dapnet.proxy;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * This class provides an embedded REST server for querying proxy status
 * information.
 *
 * @author Philipp Thiel
 */
final class ProxyStatusManager implements ProxyEventListener {

    private final ConcurrentMap<String, ConnectionStatus> connections = new ConcurrentHashMap<>();
    private volatile HttpServer server;

    @Override
    public void onRegister(String profileName) {
        ConnectionStatus status = new ConnectionStatus(profileName);
        status.setLastUpdate(Instant.now());

        connections.put(profileName, status);
    }

    @Override
    public void onConnect(String profileName) {
        ConnectionStatus status = connections.get(profileName);
        if (status != null) {
            synchronized (status) {
                Instant now = Instant.now();

                status.setLastUpdate(now);
                status.setConnectedSince(now);
                status.setState(ConnectionStatus.State.ONLINE);
            }
        }
    }

    @Override
    public void onDisconnect(String profileName, boolean reconnect) {
        ConnectionStatus status = connections.get(profileName);
        if (status != null) {
            synchronized (status) {
                Instant now = Instant.now();

                status.setLastUpdate(now);
                status.setConnectedSince(null);
                status.setState(reconnect ? ConnectionStatus.State.CONNECTING
                        : ConnectionStatus.State.OFFLINE);
            }
        }
    }

    /**
     * Gets an unmodifiable collection of all loaded connections.
     *
     * @return Collection of loaded connections.
     */
    public Collection<ConnectionStatus> getConnections() {
        return Collections.unmodifiableCollection(connections.values());
    }

    /**
     * Starts the REST server on the given endpoint.
     *
     * @param uri Endpoint URI
     */
    public void start(String uri) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("proxyStatusManager", this);

        URI baseUri = URI.create(uri);
        ResourceConfig config = new ResourceConfig(ProxyStatusResource.class,
                JacksonFeature.class);
        config.addProperties(properties);

        server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
    }

    /**
     * Stops the REST server.
     */
    public void shutdown() {
        HttpServer theServer = server;
        if (theServer != null) {
            theServer.shutdown();
        }
    }
}
