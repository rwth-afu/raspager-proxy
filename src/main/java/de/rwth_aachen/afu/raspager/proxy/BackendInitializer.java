package de.rwth_aachen.afu.raspager.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

class BackendInitializer extends ChannelInitializer<SocketChannel> {
	// TODO Use ASCII charset instead?
	private static final StringDecoder decoder = new StringDecoder();
	private static final StringEncoder encoder = new StringEncoder();
	private final Channel inbound;

	public BackendInitializer(Channel inbound) {
		this.inbound = inbound;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		p.addLast(new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));
		p.addLast(decoder);
		p.addLast(encoder);
		p.addLast(new BackendHandler(inbound));
	}

}
