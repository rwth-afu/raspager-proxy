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
package de.rwth_aachen.afu.raspager.proxy;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * The proxy service implementation. This class will act as a bridge between two
 * servers by opening a connection to each server and forwarding all traffic
 * between them. If an exception occurs the connection will be re-established.
 *
 * @author Philipp Thiel
 */
final class ProxyService implements Runnable {

    private static final Logger logger = Logger.getLogger(ProxyService.class.getName());
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final Settings settings;
    private volatile boolean shutdownRequested = false;

    /**
     * Creates a new service instance.
     *
     * @param settings Settings instance
     */
    public ProxyService(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void run() {
        try {
            while (!shutdownRequested) {
                connectAndWait();

                if (settings.getRetrySleepTime() > 0) {
                    try {
                        Thread.sleep(settings.getRetrySleepTime());
                    } catch (InterruptedException ex) {
                        logger.warning("Sleeping thread interrupted.");
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Fatal exception in proxy service.", ex);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private void connectAndWait() {
        Channel ch = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.handler(new FrontendInitializer(settings));
            b.option(ChannelOption.AUTO_READ, false);

            ch = b.connect(settings.getFrontendAddress()).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException ex) {
            logger.warning("Proxy service has been interrupted.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in proxy service.", ex);

            if (ch != null) {
                ch.close();
            }
        }
    }

    /**
     * Closes the proxy service if running.
     */
    public void shutdown() {
        shutdownRequested = true;

        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
            }
        } catch (InterruptedException ex) {
            logger.warning("Waiting for shutdown has been interrupted.");
        }
    }
}
