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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philipp Thiel
 */
final class ProxyManager implements ProxyEventListener, Runnable {

    private static final Logger LOGGER = Logger.getLogger(ProxyManager.class.getName());
    private final Set<ProxyService> services = new HashSet<>();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private volatile boolean shutdownRequested = false;

    public void addService(Settings settings) {
        ProxyService service = new ProxyService(settings, workerGroup, this);

        synchronized (services) {
            services.add(service);
        }

        workerGroup.submit(service);

        LOGGER.log(Level.INFO, "{0} Proxy service has been added.",
                settings.getProfileName());
    }

    public void shutdown() {
        shutdownRequested = true;

        Iterator<ProxyService> it = services.iterator();
        while (it.hasNext()) {
            try {
                ProxyService service = it.next();
                service.close();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to close service.", ex);
            } finally {
                it.remove();
            }
        }

        workerGroup.shutdownGracefully().syncUninterruptibly();

        LOGGER.info("Proxy manager has been shut down.");
    }

    @Override
    public void onException(ProxyService service, Throwable cause) {
        String profileName = service.getSettings().getProfileName();
        if (cause instanceof ConnectException) {
            LOGGER.log(Level.SEVERE, profileName + " Could not connect to frontend: {0}",
                    cause.getMessage());
        } else {
            LOGGER.log(Level.SEVERE, profileName + " Exception in proxy service.",
                    cause);
        }

        try {
            service.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, service.getSettings().getProfileName()
                    + " Failed to close the service.", ex);
        }
    }

    @Override
    public void onClose(ProxyService service) {
        String profileName = service.getSettings().getProfileName();
        long sleepTime = service.getSettings().getReconnectSleepTime();
        if (!shutdownRequested && sleepTime > 0) {
            LOGGER.log(Level.INFO, "{0} Performing reconnect.", profileName);
            workerGroup.schedule(service, sleepTime, TimeUnit.MILLISECONDS);
        } else {
            synchronized (services) {
                services.remove(service);
            }

            LOGGER.log(Level.INFO, "{0} Proxy service has been removed.",
                    profileName);
        }
    }

    @Override
    public void run() {
        try {
            workerGroup.terminationFuture().sync();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
