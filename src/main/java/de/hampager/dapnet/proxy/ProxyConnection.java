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
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Proxy connection implementation.
 */
final class ProxyConnection {

	private static final Logger LOGGER = Logger.getLogger(ProxyConnection.class.getName());

	private final EventLoopGroup workerGroup;
	private final ProxyConnectionEventListener eventListener;
	private final ConnectionProfile profile;
	private volatile Channel dapnetChannel;
	private volatile boolean closeRequested = false;

	/**
	 * Constructs a new proxy connection instance.
	 * 
	 * @param workerGroup   Worker group
	 * @param eventListener Proxy connection event listener
	 * @param profile       Connection profile
	 * @throws NullPointerException if any of the required arguments is
	 *                              {@code null}.
	 */
	public ProxyConnection(EventLoopGroup workerGroup, ProxyConnectionEventListener eventListener,
			ConnectionProfile profile) {
		this.workerGroup = Objects.requireNonNull(workerGroup, "Worker group must not be null.");
		this.eventListener = Objects.requireNonNull(eventListener, "Event listener must not be null.");
		this.profile = Objects.requireNonNull(profile, "Connection profile must not be null.");
	}

	/**
	 * Gets the connection profile that is used for this proxy connection.
	 * 
	 * @return Connection profile
	 */
	public ConnectionProfile getProfile() {
		return profile;
	}

	/**
	 * Establishes the proxy connection.
	 * 
	 * @throws IllegalStateException if the connection is already open.
	 */
	public void connect() {
		if (isActive()) {
			throw new IllegalStateException("Already connected.");
		}

		closeRequested = false;

		connectDapnet();
	}

	/**
	 * Closes the proxy connection.
	 */
	public void close() {
		closeRequested = true;

		closeDapnet();
	}

	/**
	 * Checks whether the proxy connection is active.
	 * 
	 * @return {@code true} if the connection is active, {@code false} otherwise.
	 */
	public boolean isActive() {
		return (dapnetChannel != null && dapnetChannel.isActive());
	}

	private void connectDapnet() {
		Bootstrap b = new Bootstrap();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		b.handler(new DapnetChannelInitializer(profile));
		b.option(ChannelOption.AUTO_READ, false);

		ChannelFuture future = b.connect(profile.getDapnetAddress());
		future.addListener(new DapnetConnectFutureListener());
	}

	private boolean scheduleReconnect() {
		final long sleepTime = profile.getReconnectDelay().toSeconds();
		final boolean doReconnect = !closeRequested && sleepTime > 0;
		if (doReconnect) {
			LOGGER.log(Level.INFO, "{0} Performing reconnect.", profile.getName());

			workerGroup.schedule(() -> connect(), sleepTime, TimeUnit.SECONDS);
		}

		return doReconnect;
	}

	private void closeDapnet() {
		final Channel ch = dapnetChannel;
		if (ch != null) {
			try {
				ch.close().sync();
			} catch (InterruptedException ex) {
				LOGGER.log(Level.WARNING, "{0} DAPNET channel close interrupted.", profile.getName());
			}
		}
	}

	private class DapnetConnectFutureListener implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (closeRequested) {
				future.channel().close();
				return;
			}

			if (future.isSuccess()) {
				onSuccess(future);
			} else {
				onFailure(future);
			}
		}

		private void onSuccess(ChannelFuture future) {
			dapnetChannel = future.channel();
			dapnetChannel.closeFuture().addListener(new DapnetCloseFutureListener());

			LOGGER.log(Level.INFO, "{0} DAPNET connection established.", profile.getName());

			workerGroup.execute(() -> eventListener.onConnect(profile.getName()));
		}

		private void onFailure(ChannelFuture future) {
			final Throwable cause = future.cause();
			if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
				LOGGER.log(Level.SEVERE, "{0} Failed to connect to DAPNET: {1}",
						new Object[] { profile.getName(), cause.getMessage() });
			} else {
				final Supplier<String> msgSupplier = new Supplier<String>() {
					@Override
					public String get() {
						return profile.getName() + " Failed to connect to DAPNET";
					}
				};

				LOGGER.log(Level.SEVERE, cause, msgSupplier);
			}

			future.channel().close();

			scheduleReconnect();
		}

	}

	private class DapnetCloseFutureListener implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			LOGGER.log(Level.INFO, "{0} DAPNET connection closed.", profile.getName());

			dapnetChannel = null;

			final boolean reconnecting = scheduleReconnect();

			workerGroup.execute(() -> eventListener.onDisconnect(profile.getName(), reconnecting));
		}

	}

}
