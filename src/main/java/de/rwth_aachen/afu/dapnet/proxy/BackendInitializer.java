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

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.nio.charset.StandardCharsets;

/**
 * This class initializes the backend channel pipeline.
 *
 * @author Philipp Thiel
 */
class BackendInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder DECODER = new StringDecoder(StandardCharsets.US_ASCII);
    private static final StringEncoder ENCODER = new StringEncoder(StandardCharsets.US_ASCII);
    private static final LineBreakAdder LBA = new LineBreakAdder();
    private final ConnectionSettings settings;
    private final Channel inbound;

    public BackendInitializer(ConnectionSettings settings, Channel inbound) {
        this.settings = settings;
        this.inbound = inbound;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new LineBasedFrameDecoder(1024));
        p.addLast(DECODER);
        p.addLast(ENCODER);
        p.addLast(LBA);

        if (settings.getBackendTimout() > 0) {
            p.addLast(new IdleStateHandler(settings.getBackendTimout(), 0, 0, TimeUnit.MILLISECONDS));
        }

        p.addLast(new BackendHandler(settings.getProfileName(), inbound));
    }

}
