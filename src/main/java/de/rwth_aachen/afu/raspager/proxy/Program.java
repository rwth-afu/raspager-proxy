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
 *
 * @author Philipp Thiel
 */
public class Program {

    private static void printHelp() {
        System.out.println("Usage: raspager-proxy <frontend> <backend>");
        System.out.println("Format for both arguments: Hostname:Port");
    }

    private static String[] parseAddress(String addr) {
        String[] values = addr.split(":");
        if (values.length == 2) {
            return values;
        } else {
            throw new IllegalArgumentException("Invalid adress format.");
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printHelp();
            return;
        }

        try {
            String[] frontend = parseAddress(args[0]);
            String[] backend = parseAddress(args[1]);

            ProxyServer server = new ProxyServer(frontend[0], Integer.parseInt(frontend[1]),
                    backend[0], Integer.parseInt(backend[1]));

            server.run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
