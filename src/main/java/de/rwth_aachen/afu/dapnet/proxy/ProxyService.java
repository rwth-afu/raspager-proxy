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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

/**
 * The proxy service implementation. This class will act as a bridge between two
 * servers by opening a connection to each server and forwarding all traffic
 * between them.
 *
 * @author Philipp Thiel
 */
final class ProxyService implements Runnable, AutoCloseable {

    private final Settings settings;
    private final EventLoopGroup workerGroup;
    private final ProxyEventListener listener;
    private volatile Channel channel;
    private volatile boolean shutdownRequested = false;

    /**
     * Creates a new service instance.
     *
     * @param settings Settings instance
     * @param workerGroup Event loop group to use.
     * @param listener Proxy event listener
     */
    public ProxyService(Settings settings, EventLoopGroup workerGroup, ProxyEventListener listener) {
        this.settings = settings;
        this.workerGroup = workerGroup;
        this.listener = listener;
    }

    /**
     * Returns the current proxy settings.
     *
     * @return Proxy settings.
     */
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void run() {
        shutdownRequested = false;

        try {
            if (channel != null) {
                channel.close().syncUninterruptibly();
            }

            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.handler(new FrontendInitializer(settings));
            b.option(ChannelOption.AUTO_READ, false);

            b.connect(settings.getFrontendAddress()).addListener((ChannelFuture f) -> {
                if (f.isSuccess()) {
                    channel = f.channel();
                    channel.closeFuture().addListener(this::forwardCloseEvent);
                } else {
                    channel = null;
                    forwardExceptionEvent(f.cause());
                }
            });
        } catch (Exception ex) {
            forwardExceptionEvent(ex);
        }
    }

    /**
     * Notifies the proxy event listener that an exception occurred.
     *
     * @param t Exception
     */
    private void forwardExceptionEvent(Throwable t) {
        listener.onException(this, t);
    }

    /**
     * Notifies the proxy event listener that this service is closed.
     *
     * @param f Future (not used).
     */
    private void forwardCloseEvent(Future f) {
        listener.onClose(this, shutdownRequested);
    }

    @Override
    public void close() throws Exception {
        shutdownRequested = true;
        Channel theChannel = channel;
        if (theChannel != null) {
            theChannel.close().syncUninterruptibly();
        }
    }

}
