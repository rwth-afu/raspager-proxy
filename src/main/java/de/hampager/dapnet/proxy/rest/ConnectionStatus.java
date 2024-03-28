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

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class holds connection status information.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
class ConnectionStatus implements Serializable {

	public enum State {
		CONNECTING, ONLINE, OFFLINE
	}

	private static final long serialVersionUID = 1L;
	private final String profileName;
	@JsonSerialize(using = InstantSerializer.class)
	private Instant lastUpdate;
	@JsonSerialize(using = InstantSerializer.class)
	private Instant connectedSince;
	private State state = State.CONNECTING;

	/**
	 * Constructs a new {@code ConnectionStatus} instance.
	 * 
	 * @param profileName Connection profile name
	 */
	public ConnectionStatus(String profileName) {
		this.profileName = profileName;
	}

	/**
	 * Copy-constructs a new {@code ConnectionStatus} instance.
	 * 
	 * @param other Source instance to copy from
	 * @throws NullPointerException if the source instance is {@code null}.
	 */
	public ConnectionStatus(ConnectionStatus other) {
		if (other == null) {
			throw new NullPointerException("Source ConnectionStatus is null.");
		}

		this.profileName = other.profileName;
		this.lastUpdate = other.lastUpdate;
		this.connectedSince = other.connectedSince;
		this.state = other.state;
	}

	/**
	 * Gets the connection profile name.
	 *
	 * @return Connection profile name
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * Gets the last update timestamp.
	 *
	 * @return Last update time
	 */
	public Instant getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Sets the last update timestamp
	 *
	 * @param lastUpdate Last update time
	 */
	public void setLastUpdate(Instant lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Gets the time since the connection has been established.
	 *
	 * @return Time or {@code null} if not connected.
	 */
	public Instant getConnectedSince() {
		return connectedSince;
	}

	/**
	 * Sets the time since the connection has been established.
	 *
	 * @param connectedSince Time or {@code null} if not connected.
	 */
	public void setConnectedSince(Instant connectedSince) {
		this.connectedSince = connectedSince;
	}

	/**
	 * Gets the connection state.
	 * 
	 * @return Current connection state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Sets the connection state
	 * 
	 * @param state New connection state
	 */
	public void setState(State state) {
		this.state = state;
	}

}
