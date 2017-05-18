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

import java.net.ConnectException;
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
import java.net.UnknownHostException;

/**
 * The frontend handler is responsible for the connection to the frontend
 * server. It will open a connection to the backend server once the connection
 * to the frontend server has been established.
 *
 * @author Philipp Thiel
 */
final class FrontendHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOGGER = Logger.getLogger(FrontendHandler.class.getName());
    private final ConnectionSettings settings;
    private final String profileName;
    private Channel outboundChannel;

    /**
     * Creates a new frontend handler.
     *
     * @param settings Settings instance
     */
    public FrontendHandler(ConnectionSettings settings) {
        this.settings = settings;
        this.profileName = settings.getProfileName();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "{0} Connected to frontend server.", profileName);

        final Channel inboundChannel = ctx.channel();

        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop());
        b.channel(ctx.channel().getClass());
        b.handler(new BackendInitializer(settings, inboundChannel));
        b.option(ChannelOption.AUTO_READ, false);

        ChannelFuture f = b.connect(settings.getBackendAddress());
        outboundChannel = f.channel();
        f.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                inboundChannel.read();
            } else {
                Throwable cause = future.cause();
                if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
                    LOGGER.log(Level.SEVERE, profileName + " Failed to connect to backend: {0}",
                            future.cause().getMessage());
                } else {
                    LOGGER.log(Level.SEVERE, profileName + " Failed to connect to backend.", cause);
                }

                inboundChannel.close();
            }
        });
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, String msg) throws Exception {
        LOGGER.log(Level.FINEST, "{0} Forwarding message from frontend to backend.", profileName);

        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        } else {
            LOGGER.log(Level.WARNING, "{0} Outbound channel not active.", profileName);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "{0} Disconnected from frontend server.", profileName);

        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ConnectException) {
            LOGGER.log(Level.SEVERE, profileName + " Could not connect to backend: {0}", cause.getMessage());
        } else {
            LOGGER.log(Level.SEVERE, profileName + " Exception in frontend handler.", cause);
        }

        closeOnFlush(ctx.channel());
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
