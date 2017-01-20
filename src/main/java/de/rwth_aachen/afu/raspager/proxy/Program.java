/*
 * Copyright (C) 2016 Amateurfunkgruppe der RWTH Aachen
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
package de.rwth_aachen.afu.raspager.proxy;

/**
 * This class contains the application entry point.
 *
 * @author Philipp Thiel
 */
public final class Program {

    public static void main(String[] args) {
        String configFile = null;
        if (args.length == 1) {
            configFile = args[0];
        }

        try {
            Settings settings = new Settings(configFile);
            ProxyService server = new ProxyService(settings);
            server.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
