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

    private static final Logger logger = Logger.getLogger(Program.class.getName());

    public static void main(String[] args) {
        String configFile = "proxy.properties";
        if (args.length == 1) {
            configFile = args[0];
        }

        logger.log(Level.INFO, "DAPNET Proxy Version {0}",
                Program.class.getPackage().getImplementationVersion());

        try {
            ProxyManager manager = new ProxyManager();
            registerShutdownHook(manager);

            Settings settings = new Settings(configFile);
            manager.addService(settings);

            // Wait for termination
            manager.run();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in main.", ex);
            System.exit(1);
        }
    }

    private static void registerShutdownHook(ProxyManager manager) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    manager.shutdown();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Failed to stop proxy manager.", ex);
                }
            }
        });
    }

}
