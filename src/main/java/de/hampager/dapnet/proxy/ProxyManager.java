/*
 * Copyright (C) 2017-2024 Amateurfunkgruppe an der RWTH Aachen
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
package de.hampager.dapnet.proxy;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * This class manages the proxy connections.
 */
final class ProxyManager {

	private static final Logger LOGGER = Logger.getLogger(ProxyManager.class.getName());
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private final ProxyConnectionEventListener listener;
	private volatile boolean shutdownRequested = false;

	/**
	 * Creates a new proxy manager instance.
	 *
	 * @param listener Proxy event listener to use.
	 */
	public ProxyManager(ProxyConnectionEventListener listener) {
		this.listener = listener;
	}

	/**
	 * Opens a new proxy connection.
	 *
	 * @param settings Connection settings
	 */
	public void openConnection(final ConnectionProfile settings) {
		workerGroup.submit(() -> doConnect(settings));

		if (listener != null) {
			listener.onRegister(settings.getName());
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

	private void doConnect(final ConnectionProfile settings) {
		Bootstrap b = new Bootstrap();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		b.handler(new DapnetChannelInitializer(settings));
		b.option(ChannelOption.AUTO_READ, false);

		ChannelFuture connf = b.connect(settings.getDapnetAddress());
		connf.addListener((ChannelFuture f) -> {
			if (f.isSuccess()) {
				onConnectSucceeded(settings, f.channel());
			} else {
				f.channel().close();
				onConnectFailed(settings, f.cause());
			}
		});
	}

	private void onConnectSucceeded(final ConnectionProfile settings, final Channel channel) {
		channel.closeFuture().addListener(f -> onClose(settings));

		LOGGER.log(Level.INFO, "{0} Proxy connection added.", settings.getName());

		if (listener != null) {
			listener.onConnect(settings.getName());
		}
	}

	private void onConnectFailed(ConnectionProfile settings, Throwable ex) {
		if (ex instanceof ConnectException || ex instanceof UnknownHostException) {
			LOGGER.log(Level.SEVERE, settings.getName() + " Could not connect to frontend: {0}",
					ex.getMessage());
		} else {
			LOGGER.log(Level.SEVERE, settings.getName() + " Could not connect to frontend.", ex);
		}

		scheduleReconnect(settings);
	}

	private void onClose(ConnectionProfile settings) {
		LOGGER.log(Level.INFO, "{0} Connection closed.", settings.getName());

		boolean reconnect = scheduleReconnect(settings);

		if (listener != null) {
			listener.onDisconnect(settings.getName(), reconnect);
		}
	}

	private boolean scheduleReconnect(final ConnectionProfile settings) {
		final long sleepTime = settings.getReconnectDelay().toSeconds();
		if (!shutdownRequested && sleepTime > 0) {
			LOGGER.log(Level.INFO, "{0} Performing reconnect.", settings.getName());

			workerGroup.schedule(() -> doConnect(settings), sleepTime, TimeUnit.MILLISECONDS);

			return true;
		} else {
			return false;
		}
	}

}
