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

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * This class contains the application entry point.
 *
 * @author Philipp Thiel
 */
public final class Program {

    private static void printHelp() {
        System.out.println("Usage: raspager-proxy <frontend> <backend>");
        System.out.println("Format for both arguments: Hostname:Port");
    }

    private static SocketAddress parseAddress(String addr) {
        String[] values = addr.split(":");
        if (values.length != 2) {
            throw new IllegalArgumentException("Invalid address format");
        }

        try {
            return new InetSocketAddress(values[0], Integer.parseInt(values[1]));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid port number", ex);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printHelp();
            return;
        }

        SocketAddress frontend = parseAddress(args[0]);
        SocketAddress backend = parseAddress(args[1]);

        ProxyService server = new ProxyService(frontend, backend);
        server.run();
    }

}
