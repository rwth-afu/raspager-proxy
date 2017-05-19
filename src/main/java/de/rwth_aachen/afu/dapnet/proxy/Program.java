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

    private static final String REST_START_KEY = "dapnet.proxy.rest.start";
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
            ProxyManager manager = new ProxyManager();

            // Start embedded REST server?
            ProxyStatusManager statusManager = null;
            if (Boolean.getBoolean(REST_START_KEY)) {
                statusManager = new ProxyStatusManager();
                manager.setListener(statusManager);

                int port = Integer.getInteger(REST_PORT_KEY, 8080);
                LOGGER.log(Level.INFO, "Starting REST server on port {0,number,#}", port);
                statusManager.start(port);
            }

            registerShutdownHook(statusManager, manager);

            for (String arg : args) {
                registerService(manager, arg);
            }

            // Wait for termination
            manager.run();
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

    private static void registerShutdownHook(final ProxyStatusManager statusManager,
            final ProxyManager manager) {
        Runnable hook = () -> {
            try {
                if (statusManager != null) {
                    statusManager.shutdown();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to stop status manager.", ex);
            }

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
