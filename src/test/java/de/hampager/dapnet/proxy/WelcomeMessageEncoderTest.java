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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;

public class WelcomeMessageEncoderTest {

	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();
	private static final NewlineAppender APPENDER = new NewlineAppender();

	public static Stream<Arguments> getParams() {
		return Stream.of(Arguments.arguments("forward", "forward"),
				Arguments.arguments("[Test v1.0]", "[Test v1.0 name key]"),
				Arguments.arguments("[Test v1.0.0-SCP-#123456]", "[Test v1.0.0-SCP-#123456 name key]"),
				Arguments.arguments("Test/RPC v1.0", "Test/RPC v1.0"));
	}

	@ParameterizedTest
	@MethodSource("getParams")
	public void test(String input, String output) {
		final EmbeddedChannel channel = createChannel();

		// Write
		{
			assertTrue(channel.writeOutbound(input));
			assertTrue(channel.writeInbound((ByteBuf) channel.readOutbound()));
			assertTrue(channel.finish());
		}

		// Read
		{
			String msg = channel.readInbound();
			assertEquals(output, msg);
			ReferenceCountUtil.release(msg);
		}
	}

	private static EmbeddedChannel createChannel() {
		EmbeddedChannel channel = new EmbeddedChannel();
		WelcomeMessageEncoder msgEncoder = new WelcomeMessageEncoder("name", "key");

		ChannelPipeline p = channel.pipeline();
		p.addLast(new LineBasedFrameDecoder(64));
		p.addLast(DECODER);
		p.addLast(ENCODER);
		p.addLast(APPENDER);
		p.addLast(msgEncoder);

		return channel;
	}

}
