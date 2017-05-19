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

/**
 * Event listener interface for proxy connection events.
 *
 * @author Philipp Thiel
 */
interface ProxyEventListener {

    /**
     * Called when a proxy connection profile is registered.
     *
     * @param profileName Profile name
     */
    void onRegister(String profileName);

    /**
     * Called when a proxy connection has been established.
     *
     * @param profileName Profile name
     */
    void onConnect(String profileName);

    /**
     * Called when a proxy connection has been closed.
     *
     * @param profileName Profile name
     * @param reconnect Whether a reconnect attempt is made or not.
     */
    void onDisconnect(String profileName, boolean reconnect);

    /**
     * Called when the proxy manager is shutting down.
     */
    void onShutdown();
}
