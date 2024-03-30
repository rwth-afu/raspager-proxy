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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.hampager.dapnet.proxy;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * The DAPNET channel handler is responsible for the connection to the DAPNET
 * server. It will open a connection to the transmitter once the connection to
 * the DAPNET server has been established.
 */
final class DapnetChannelHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger LOGGER = Logger.getLogger(DapnetChannelHandler.class.getName());
	private final ConnectionProfile profile;
	private volatile Channel transmitterChannel;

	/**
	 * Creates a new DAPNET channel handler.
	 *
	 * @param profile Connection profile
	 */
	public DapnetChannelHandler(ConnectionProfile profile) {
		this.profile = profile;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.log(Level.INFO, "{0} Connected to DAPNET server.", profile.getName());

		final Channel dapnetChannel = ctx.channel();

		Bootstrap b = new Bootstrap();
		b.group(dapnetChannel.eventLoop());
		b.channel(dapnetChannel.getClass());
		b.handler(new TransmitterChannelInitializer(profile, dapnetChannel));
		b.option(ChannelOption.AUTO_READ, false);

		ChannelFuture f = b.connect(profile.getTransmitterAddress());
		transmitterChannel = f.channel();
		f.addListener(new TransmitterConnectFutureListener(dapnetChannel));
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, String msg) throws Exception {
		LOGGER.log(Level.FINEST, "{0} Forwarding message from DAPNET to transmitter.", profile.getName());

		if (transmitterChannel.isActive()) {
			transmitterChannel.writeAndFlush(msg).addListener(new TransmitterWriteFutureListener(ctx.channel()));
		} else {
			LOGGER.log(Level.WARNING, "{0} Transmitter channel not active; discarding message.", profile.getName());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.log(Level.INFO, "{0} Disconnected from DAPNET server.", profile.getName());

		if (transmitterChannel != null) {
			closeOnFlush(transmitterChannel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof ConnectException) {
			LOGGER.log(Level.SEVERE, profile.getName() + " Could not connect to transmitter: {0}", cause.getMessage());
		} else {
			LOGGER.log(Level.SEVERE, profile.getName() + " Exception in DAPNET handler.", cause);
		}

		closeOnFlush(ctx.channel());
	}

	static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

	private class TransmitterConnectFutureListener implements ChannelFutureListener {

		private final Channel dapnetChannel;

		public TransmitterConnectFutureListener(Channel dapnetChannel) {
			this.dapnetChannel = Objects.requireNonNull(dapnetChannel);
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				dapnetChannel.read();
			} else {
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

				dapnetChannel.close();
			}
		}

	}

	private class TransmitterWriteFutureListener implements ChannelFutureListener {

		private final Channel dapnetChannel;

		public TransmitterWriteFutureListener(Channel dapnetChannel) {
			this.dapnetChannel = Objects.requireNonNull(dapnetChannel);
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				dapnetChannel.read();
			} else {
				future.channel().close();
			}
		}

	}

}
