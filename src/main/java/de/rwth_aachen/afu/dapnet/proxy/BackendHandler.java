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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The backend handler is responsible for the connection to the backend server.
 *
 * @author Philipp Thiel
 */
class BackendHandler extends SimpleChannelInboundHandler<String> {

    private static final String KEEP_ALIVE_REQ = "2:PING";
    private static final Logger logger = Logger.getLogger(BackendHandler.class.getName());
    private final Channel inboundChannel;
    private volatile boolean sendKeepAlive = false;
    private volatile boolean pendingKeepAlive = false;

    public BackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Connected to backend server.");

        ctx.read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Disconnected from backend server.");

        FrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (sendKeepAlive && msg.startsWith(KEEP_ALIVE_REQ)) {
            logger.info("Received keep alive response from backend.");
            pendingKeepAlive = false;
        } else {
            logger.info("Forwarding message from backend to frontend.");

            inboundChannel.writeAndFlush(msg).addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });

            // Waiting for the handshake to complete would be better
            sendKeepAlive = true;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            handleKeepAlive(ctx);
        } else {
            logger.log(Level.SEVERE, "Exception in backend handler.", cause);
            FrontendHandler.closeOnFlush(ctx.channel());
        }
    }

    private void handleKeepAlive(ChannelHandlerContext ctx) throws Exception {
        if (pendingKeepAlive) {
            logger.severe("Backend read timed out, closing channel.");
            FrontendHandler.closeOnFlush(ctx.channel());
        } else if (sendKeepAlive) {
            logger.info("Sending keep alive request to backend.");
            ctx.writeAndFlush(KEEP_ALIVE_REQ);
            pendingKeepAlive = true;
        }
    }

}
