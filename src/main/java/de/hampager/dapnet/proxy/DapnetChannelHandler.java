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
	private Channel outboundChannel;

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

		final Channel inboundChannel = ctx.channel();

		Bootstrap b = new Bootstrap();
		b.group(inboundChannel.eventLoop());
		b.channel(ctx.channel().getClass());
		b.handler(new BackendInitializer(profile, inboundChannel));
		b.option(ChannelOption.AUTO_READ, false);

		ChannelFuture f = b.connect(profile.getTransmitterAddress());
		outboundChannel = f.channel();
		f.addListener((ChannelFuture future) -> {
			if (future.isSuccess()) {
				inboundChannel.read();
			} else {
				Throwable cause = future.cause();
				if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
					LOGGER.log(Level.SEVERE, profile.getName() + " Failed to connect to backend: {0}",
							future.cause().getMessage());
				} else {
					LOGGER.log(Level.SEVERE, profile.getName() + " Failed to connect to backend.", cause);
				}

				inboundChannel.close();
			}
		});
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, String msg) throws Exception {
		LOGGER.log(Level.FINEST, "{0} Forwarding message from frontend to backend.", profile.getName());

		if (outboundChannel.isActive()) {
			outboundChannel.writeAndFlush(msg).addListener((ChannelFuture future) -> {
				if (future.isSuccess()) {
					ctx.channel().read();
				} else {
					future.channel().close();
				}
			});
		} else {
			LOGGER.log(Level.WARNING, "{0} Outbound channel not active.", profile.getName());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.log(Level.INFO, "{0} Disconnected from frontend server.", profile.getName());

		if (outboundChannel != null) {
			closeOnFlush(outboundChannel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof ConnectException) {
			LOGGER.log(Level.SEVERE, profile.getName() + " Could not connect to backend: {0}", cause.getMessage());
		} else {
			LOGGER.log(Level.SEVERE, profile.getName() + " Exception in frontend handler.", cause);
		}

		closeOnFlush(ctx.channel());
	}

	static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

}
