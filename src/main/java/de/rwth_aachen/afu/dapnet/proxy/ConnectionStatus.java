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

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class holds connection status information.
 *
 * @author Philipp Thiel
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ConnectionStatus {

	public enum State {
		CONNECTING, ONLINE, OFFLINE
	}

	private final String profileName;
	@JsonSerialize(using = InstantJsonSerializer.class)
	private Instant lastUpdate;
	@JsonSerialize(using = InstantJsonSerializer.class)
	private Instant connectedSince;
	private State state = State.CONNECTING;

	public ConnectionStatus(String profileName) {
		this.profileName = profileName;
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

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
