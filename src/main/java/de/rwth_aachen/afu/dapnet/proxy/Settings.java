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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;

/**
 * This class contains the application settings.
 *
 * @author Philipp Thiel
 */
final class Settings {

    private final String profileName;
    private final String frontendName;
    private final String frontendKey;
    private final SocketAddress frontendAddress;
    private final SocketAddress backendAddress;
    private final long reconnectSleepTime;
    private final long backendTimeout;

    /**
     * Creates a settings instance by loading the settings from the given
     * properties.
     *
     * @param props Properties to use.
     * @throws NullPointerException If a required settings is not found.
     */
    public Settings(Properties props) {
        profileName = getString(props, "profileName");

        // Retry sleep time
        reconnectSleepTime = getLong(props, "reconnectSleepTime");
        if (reconnectSleepTime < 0) {
            throw new IllegalArgumentException("Reconnect sleep time cannot be negative.");
        }

        // Frontend configuration
        frontendName = getString(props, "frontend.name");
        frontendKey = getString(props, "frontend.key");
        frontendAddress = getAddress(props, "frontend.host", "frontend.port");

        // Backend configuration
        backendAddress = getAddress(props, "backend.host", "backend.port");
        backendTimeout = getLong(props, "backend.timeout");
    }

    /**
     * Loads settings from the given file.
     *
     * @param filename Configuration file to load.
     * @return Loaded settings
     * @throws FileNotFoundException If the file does not exist.
     * @throws IOException If the file could not be read.
     */
    public static Settings fromFile(String filename) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        try (FileInputStream fin = new FileInputStream(filename)) {
            props.load(fin);
        }

        return new Settings(props);
    }

    /**
     * Returns the profile name.
     *
     * @return Name of the loaded profile.
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the sleep time between retries in milliseconds.
     *
     * @return Time to sleep between retries in milliseconds.
     */
    public long getReconnectSleepTime() {
        return reconnectSleepTime;
    }

    /**
     * Gets the frontend server address.
     *
     * @return Frontend server address.
     */
    public SocketAddress getFrontendAddress() {
        return frontendAddress;
    }

    /**
     * Gets the name used to authenticate with the frontend server.
     *
     * @return Frontend authentication name.
     */
    public String getFrontendName() {
        return frontendName;
    }

    /**
     * Gets the auth key used to authenticate with the frontend server.
     *
     * @return Frontend authentication key.
     */
    public String getFrontendKey() {
        return frontendKey;
    }

    /**
     * Gets the backend server address.
     *
     * @return Backend server address.
     */
    public SocketAddress getBackendAddress() {
        return backendAddress;
    }

    /**
     * Gets the backend timeout in milliseconds.
     *
     * @return Backend timeout in milliseconds.
     */
    public long getBackendTimout() {
        return backendTimeout;
    }

    private static String getString(Properties props, String key) {
        String value = props.getProperty(key);
        if (value != null) {
            return value;
        } else {
            throw new NullPointerException("Key not found: " + key);
        }
    }

    private static SocketAddress getAddress(Properties props, String host, String port) {
        return new InetSocketAddress(getString(props, host), getInt(props, port));
    }

    private static int getInt(Properties props, String key) {
        return Integer.parseInt(getString(props, key));
    }

    private static long getLong(Properties props, String key) {
        return Long.parseLong(getString(props, key));
    }

}
