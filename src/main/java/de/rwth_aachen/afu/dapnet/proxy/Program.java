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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.rwth_aachen.afu.dapnet.proxy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the application entry point.
 *
 * @author Philipp Thiel
 */
public final class Program {

    private static final String REST_PORT_KEY = "dapnet.proxy.rest.port";
    private static final Logger LOGGER = Logger.getLogger(Program.class.getName());

    public static void main(String[] args) {
        if (args.length < 1) {
            LOGGER.log(Level.SEVERE, "No configuration file provided.");
            System.exit(1);
        }

        LOGGER.log(Level.INFO, "DAPNET Proxy Version {0}",
                Program.class.getPackage().getImplementationVersion());

        try {
            // Start embedded REST server?
            ConnectionStatusManager statusManager = null;
            Integer port = Integer.getInteger(REST_PORT_KEY);
            if (port != null) {
                statusManager = new ConnectionStatusManager();
                LOGGER.log(Level.INFO, "Starting REST server on port {0,number,#}", port);
                statusManager.start(port);
            }

            ProxyManager proxyManager = new ProxyManager(statusManager);
            registerShutdownHook(proxyManager);

            for (String arg : args) {
                registerService(proxyManager, arg);
            }

            // Wait for termination
            proxyManager.run();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception in main.", ex);
            System.exit(1);
        }
    }

    private static void registerService(ProxyManager manager, String configFile) {
        try {
            ConnectionSettings settings = ConnectionSettings.fromFile(configFile);
            manager.openConnection(settings);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load configuration file.", ex);
        }
    }

    private static void registerShutdownHook(final ProxyManager manager) {
        Runnable hook = () -> {
            try {
                if (manager != null) {
                    manager.shutdown();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to stop proxy manager.", ex);
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(hook, "ShutdownHook"));
    }

}
