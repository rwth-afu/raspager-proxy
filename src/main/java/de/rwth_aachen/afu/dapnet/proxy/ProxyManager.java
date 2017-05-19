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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the proxy connections.
 *
 * @author Philipp Thiel
 */
final class ProxyManager implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ProxyManager.class.getName());
    private final Set<ConnectionState> activeStates = new HashSet<>();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ProxyEventListener listener;
    private volatile boolean shutdownRequested = false;

    /**
     * Creates a new proxy manager instance.
     *
     * @param listener Proxy event listener to use.
     */
    public ProxyManager(ProxyEventListener listener) {
        this.listener = listener;
    }

    /**
     * Opens a new proxy connection.
     *
     * @param settings Connection settings
     */
    public void openConnection(final ConnectionSettings settings) {
        workerGroup.submit(() -> doConnect(settings));

        if (listener != null) {
            listener.onRegister(settings.getProfileName());
        }
    }

    /**
     * Stops the proxy manager and closes all open connections.
     */
    public void shutdown() {
        shutdownRequested = true;

        LOGGER.info("Shutting down proxy manager ...");

        if (listener != null) {
            try {
                listener.onShutdown();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to shut down proxy event listener.", ex);
            }
        }

        workerGroup.shutdownGracefully();

        LOGGER.info("Proxy manager has been shut down.");
    }

    @Override
    public void run() {
        try {
            workerGroup.terminationFuture().sync();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void doConnect(final ConnectionSettings settings) {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.handler(new FrontendInitializer(settings));
        b.option(ChannelOption.AUTO_READ, false);

        ChannelFuture connf = b.connect(settings.getFrontendAddress());
        connf.addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                onConnectSucceeded(settings, f.channel());
            } else {
                f.channel().close();
                onConnectFailed(settings, f.cause());
            }
        });
    }

    private void onConnectSucceeded(final ConnectionSettings settings, final Channel channel) {
        ConnectionState state = new ConnectionState(settings, channel);
        channel.closeFuture().addListener(f -> onClose(state));

        synchronized (activeStates) {
            activeStates.add(state);
        }

        LOGGER.log(Level.INFO, "{0} Proxy connection added.", settings.getProfileName());

        if (listener != null) {
            listener.onConnect(settings.getProfileName());
        }
    }

    private void onConnectFailed(ConnectionSettings settings, Throwable ex) {
        if (ex instanceof ConnectException || ex instanceof UnknownHostException) {
            LOGGER.log(Level.SEVERE, settings.getProfileName()
                    + " Could not connect to frontend: {0}", ex.getMessage());
        } else {
            LOGGER.log(Level.SEVERE, settings.getProfileName()
                    + " Could not connect to frontend.", ex);
        }

        scheduleReconnect(settings);
    }

    private void onClose(ConnectionState state) {
        LOGGER.log(Level.INFO, "{0} Connection closed.", state.getSettings().getProfileName());

        synchronized (activeStates) {
            activeStates.remove(state);
        }

        boolean reconnect = scheduleReconnect(state.getSettings());

        if (listener != null) {
            listener.onDisconnect(state.getSettings().getProfileName(), reconnect);
        }
    }

    private boolean scheduleReconnect(final ConnectionSettings settings) {
        long sleepTime = settings.getReconnectSleepTime();
        if (!shutdownRequested && sleepTime > 0) {
            LOGGER.log(Level.INFO, "{0} Performing reconnect.", settings.getProfileName());

            workerGroup.schedule(() -> doConnect(settings), sleepTime, TimeUnit.MILLISECONDS);

            return true;
        } else {
            return false;
        }
    }

    private static final class ConnectionState {

        private final ConnectionSettings settings;
        private final Channel channel;

        public ConnectionState(ConnectionSettings settings, Channel channel) {
            this.settings = settings;
            this.channel = channel;
        }

        public ConnectionSettings getSettings() {
            return settings;
        }

        public Channel getChannel() {
            return channel;
        }

    }

}
