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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Philipp Thiel
 */
@RunWith(Parameterized.class)
public class WelcomeMessageEncoderTest {

    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();
    private static final LineBreakAdder LBA = new LineBreakAdder();
    private final EmbeddedChannel channel;
    private final String input;
    private final String output;

    @Parameterized.Parameters
    public static Collection<Object[]> getParams() {
        return Arrays.asList(new Object[][]{
            {"forward", "forward"},
            {"[Test v1.0]", "[Test v1.0 name key]"},
            {"[Test v1.0.0-SCP-#123456]", "[Test v1.0.0-SCP-#123456 name key]"},
            {"Test/RPC v1.0", "Test/RPC v1.0"}
        });
    }

    public WelcomeMessageEncoderTest(String input, String output) {
        this.channel = createChannel();
        this.input = input;
        this.output = output;
    }

    @Test
    public void test() {
        // Write
        Assert.assertTrue(channel.writeOutbound(input));
        Assert.assertTrue(channel.writeInbound((ByteBuf) channel.readOutbound()));
        Assert.assertTrue(channel.finish());

        // Read
        String msg = channel.readInbound();
        Assert.assertEquals(output, msg);
    }

    private static EmbeddedChannel createChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        WelcomeMessageEncoder msgEncoder = new WelcomeMessageEncoder("name", "key");

        ChannelPipeline p = channel.pipeline();
        p.addLast(new LineBasedFrameDecoder(64));
        p.addLast(DECODER);
        p.addLast(ENCODER);
        p.addLast(LBA);
        p.addLast(msgEncoder);

        return channel;
    }

}
