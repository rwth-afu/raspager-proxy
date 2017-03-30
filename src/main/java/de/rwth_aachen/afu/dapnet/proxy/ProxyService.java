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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * The proxy service implementation. This class will act as a bridge between two
 * servers by opening a connection to each server and forwarding all traffic
 * between them.
 *
 * @author Philipp Thiel
 */
final class ProxyService implements Runnable, AutoCloseable {

	private final Settings settings;
	private final EventLoopGroup workerGroup;
	private final ProxyEventListener listener;
	private volatile Channel channel;

	/**
	 * Creates a new service instance.
	 *
	 * @param settings
	 *            Settings instance
	 * @param workerGroup
	 *            Event loop group to use.
	 * @param listener
	 *            Proxy event listener
	 */
	public ProxyService(Settings settings, EventLoopGroup workerGroup, ProxyEventListener listener) {
		this.settings = settings;
		this.workerGroup = workerGroup;
		this.listener = listener;
	}

	/**
	 * Returns the current proxy settings.
	 *
	 * @return Proxy settings.
	 */
	public Settings getSettings() {
		return settings;
	}

	@Override
	public void run() {
		try {
			if (channel != null) {
				channel.close().syncUninterruptibly();
			}

			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.handler(new FrontendInitializer(settings));
			b.option(ChannelOption.AUTO_READ, false);

			b.connect(settings.getFrontendAddress()).addListener((ChannelFuture f) -> {
				if (f.isSuccess()) {
					channel = f.channel();
				} else {
					channel = null;
					listener.onException(this, f.cause());
				}
			});
		} catch (Exception ex) {
			listener.onException(this, ex);
		}
	}

	@Override
	public void close() throws Exception {
		Channel theChannel = channel;
		if (theChannel != null) {
			theChannel.close().syncUninterruptibly();
		}
	}

}
