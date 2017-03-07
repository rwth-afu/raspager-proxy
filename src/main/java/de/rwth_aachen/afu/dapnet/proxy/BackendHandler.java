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
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The backend handler is responsible for the connection to the backend server.
 *
 * @author Philipp Thiel
 */
class BackendHandler extends SimpleChannelInboundHandler<String> {

    private enum State {
        HANDSHAKE, SEND_KEEP_ALIVE, PENDING_KEEP_ALIVE_1, PENDING_KEEP_ALIVE_2
    }

    private static final String KEEP_ALIVE_REQ = "2:PING";
    private static final Logger logger = Logger.getLogger(BackendHandler.class.getName());
    private final Channel inboundChannel;
    private volatile State state = State.HANDSHAKE;

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
        boolean forward = true;

        logger.info("DEBUG StatePre = " + state);

        switch (state) {
            case HANDSHAKE:
                if (msg.startsWith("2:")) {
                    state = State.SEND_KEEP_ALIVE;
                }
                break;
            case SEND_KEEP_ALIVE:
                break;
            case PENDING_KEEP_ALIVE_1:
                if (msg.startsWith(KEEP_ALIVE_REQ)) {
                    state = State.PENDING_KEEP_ALIVE_2;
                    forward = false;
                    logger.info("Received keep alive response from backend (1/1).");
                }
                break;
            case PENDING_KEEP_ALIVE_2:
                if (msg.equals("+")) {
                    state = State.SEND_KEEP_ALIVE;
                    forward = false;
                    logger.info("Received keep alive response from backend (2/2).");
                }
                break;
        }

        logger.info("DEBUG StatePost = " + state);

        if (forward) {
            forwardMessage(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(Level.SEVERE, "Exception in backend handler.", cause);
        FrontendHandler.closeOnFlush(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idle = (IdleStateEvent) evt;
            if (idle.state() == IdleState.READER_IDLE) {
                handleReadTimeout(ctx);
            }
        }
    }

    private void forwardMessage(final ChannelHandlerContext ctx, String msg) throws Exception {
        logger.info("Forwarding message from backend to frontend.");

        inboundChannel.writeAndFlush(msg).addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                future.channel().close();
            }
        });
    }

    private void handleReadTimeout(ChannelHandlerContext ctx) throws Exception {
        switch (state) {
            case HANDSHAKE:
                break;
            case SEND_KEEP_ALIVE:
                logger.info("Sending keep alive request to backend.");
                state = State.PENDING_KEEP_ALIVE_1;
                ctx.writeAndFlush(KEEP_ALIVE_REQ);
                break;
            case PENDING_KEEP_ALIVE_1:
            case PENDING_KEEP_ALIVE_2:
                logger.severe("Backend read timed out, closing channel.");
                FrontendHandler.closeOnFlush(ctx.channel());
                break;
        }
    }

}
