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

    private static final Logger logger = Logger.getLogger(ProxyManager.class.getName());
    private final Set<ProxyService> services = new HashSet<>();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void addService(Settings settings) {
        ProxyService service = new ProxyService(settings, workerGroup, this);

        synchronized (services) {
            services.add(service);
        }

        workerGroup.submit(service);

        logger.info("Added proxy service.");
    }

    public void shutdown() {
        Iterator<ProxyService> it = services.iterator();
        while (it.hasNext()) {
            try {
                ProxyService service = it.next();
                service.close();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to close service.", ex);
            } finally {
                it.remove();
            }
        }

        workerGroup.shutdownGracefully().syncUninterruptibly();

        logger.info("Proxy manager has been shut down.");
    }

    @Override
    public void onException(ProxyService service, Throwable cause) {
        if (cause instanceof ConnectException) {
            logger.log(Level.SEVERE, "Could not connect to frontend: {0}",
                    cause.getMessage());
        } else {
            logger.log(Level.SEVERE, "Exception in proxy service.", cause);
        }

        long sleepTime = service.getSettings().getReconnectSleepTime();
        if (sleepTime > 0) {
            workerGroup.schedule(service, sleepTime, TimeUnit.MILLISECONDS);
        } else {
            try {
                service.close();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to close the service.", ex);
            } finally {
                synchronized (services) {
                    services.remove(service);
                }
            }
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
