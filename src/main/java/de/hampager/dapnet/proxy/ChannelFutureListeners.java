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

import java.util.Objects;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

class ChannelFutureListeners {

	public static final ChannelFutureListener READ_ON_SUCCESS = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				future.channel().read();
			} else {
				future.channel().close();
			}
		}
	};

	public static class ReadNextFutureListener implements ChannelFutureListener {

		private final Channel readChannel;

		public ReadNextFutureListener(Channel readChannel) {
			this.readChannel = Objects.requireNonNull(readChannel);
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				readChannel.read();
			} else {
				future.channel().close();
			}
		}

	}

	private ChannelFutureListeners() {
	}

}
