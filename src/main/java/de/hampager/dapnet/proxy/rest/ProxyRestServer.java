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

import java.util.Collection;

import de.hampager.dapnet.proxy.ProxyEventListener;

/**
 * REST server interface
 */
public interface ProxyRestServer {

	/**
	 * Gets an unmodifiable collection of all loaded connections.
	 *
	 * @return Collection of loaded connections.
	 */
	Collection<ConnectionStatus> getConnections();

	/**
	 * Gets a connection status object.
	 *
	 * @param profileName Name of the connection profile. A case-sensitive lookup is
	 *                    performed.
	 * @return Connection status object or {@code null} if profile not found.
	 */
	ConnectionStatus getConnection(String profileName);

	/**
	 * Gets the event listener instance that is bound to the REST server.
	 * 
	 * @return Event listener instance.
	 */
	ProxyEventListener getEventListener();

	/**
	 * Starts the REST server on the given endpoint.
	 * 
	 * @param uri  Server URI
	 * @param port Server port
	 * @throws Exception if starting the server fails.
	 */
	void start(String uri, int port) throws Exception;

	/**
	 * Stops the REST server.
	 */
	void stop();

	/**
	 * Creates a new default REST server instance.
	 * 
	 * @return REST server instance
	 */
	static ProxyRestServer createDefault() {
		return new DefaultRestServer();
	}

}
