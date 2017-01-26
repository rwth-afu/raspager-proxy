package de.rwth_aachen.afu.raspager.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringEncoder;

class FrontendInitializer extends ChannelInitializer<SocketChannel> {
	// TODO Use ASCII charset instead?
	private static final StringEncoder encoder = new StringEncoder();
	private final WelcomeMessageEncoder msgEncoder;
	private final Settings settings;

	public FrontendInitializer(Settings settings) {
		this.msgEncoder = new WelcomeMessageEncoder(settings.getFrontendKey());
		this.settings = settings;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		p.addLast(msgEncoder);
		p.addLast(encoder);
		p.addLast(new FrontendHandler(settings));
	}

}
