/*
 * Copyright (C) 2024 Amateurfunkgruppe an der RWTH Aachen
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

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class TransmitterConnectionHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger logger = Logger.getLogger(TransmitterConnectionHandler.class.getName());
	private final String profileName;

	public TransmitterConnectionHandler(String profileName) {
		if (profileName == null || profileName.isEmpty()) {
			throw new IllegalArgumentException("Profile name must not be null or empty.");
		}

		this.profileName = profileName;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.log(Level.INFO, "({0}) Connected to transmitter: {1}",
				new Object[] { profileName, ctx.channel().remoteAddress() });
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.log(Level.INFO, "({0}) Disconnected from transmitter.", profileName);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		// TODO Auto-generated method stub
	}

}
