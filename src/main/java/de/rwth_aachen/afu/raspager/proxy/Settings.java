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
package de.rwth_aachen.afu.raspager.proxy;

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

    private static final String DEFAULT_CONFIG = "RaspagerProxy.properties";
    private final String frontendKey;
    private final SocketAddress frontendAddress;
    private final SocketAddress backendAddress;

    /**
     * Creates a settings instance by loading the settings from the given
     * configuration file.
     *
     * @param filename Configuration file to load.
     * @throws FileNotFoundException If the file does not exist.
     * @throws IOException If the file could not be read.
     * @throws NullPointerException If a required settings is not found.
     */
    public Settings(String filename) throws FileNotFoundException, IOException {
        if (filename == null) {
            filename = DEFAULT_CONFIG;
        }

        Properties props = new Properties();
        try (FileInputStream fin = new FileInputStream(filename)) {
            props.load(fin);
        }

        // Frontend configuration
        frontendAddress = new InetSocketAddress(getString(props, "frontend.host"),
                getInt(props, "frontend.port"));
        frontendKey = getString(props, "frontend.key", null);

        // Backend configruation
        backendAddress = new InetSocketAddress(getString(props, "backend.host"),
                getInt(props, "backend.port"));
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

    private static String getString(Properties props, String key) {
        String value = props.getProperty(key);
        if (value != null) {
            return value;
        } else {
            throw new NullPointerException("Key not found: " + key);
        }
    }

    private static String getString(Properties props, String key, String defaultValue) {
        String value = props.getProperty(key);
        return value != null ? value : defaultValue;
    }

    private static int getInt(Properties props, String key) {
        return Integer.parseInt(getString(props, key));
    }
}
