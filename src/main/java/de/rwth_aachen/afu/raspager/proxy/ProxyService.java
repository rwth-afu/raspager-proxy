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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The proxy service implementation. This class will act as a bridge between two
 * servers by opening a connection to each server and forwarding all traffic
 * between them.
 *
 * @author Philipp Thiel
 */
final class ProxyService implements Runnable {

    private static final Logger logger = Logger.getLogger(ProxyService.class.getName());
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final Settings settings;

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
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.handler(new FrontendHandler(settings));
            b.option(ChannelOption.AUTO_READ, false);

            b.connect(settings.getFrontendAddress()).sync().channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "Proxy service has been interrupted.", ex);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
