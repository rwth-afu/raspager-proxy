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
            registerShutdownHook(manager);

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
            Settings settings = Settings.fromFile(configFile);
            manager.addService(settings);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load configuration file.", ex);
        }
    }

    private static void registerShutdownHook(final ProxyManager manager) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    manager.shutdown();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to stop proxy manager.", ex);
                }
            }
        });
    }

}
