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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * This handler intercepts the welcome message and adds the auth key. If no auth
 * key is required, do not add this handler to the channel pipeline.
 *
 * @author Philipp Thiel
 */
final class WelcomeMessageEncoder extends MessageToMessageEncoder<String> {

	private static final Pattern WELCOME_PATTERN = Pattern
			.compile("\\[([/\\p{Alnum}]+) v?(\\d[\\d\\.]+[\\p{Graph}]*)\\]");
	private final String authName;
	private final String authKey;

	/**
	 * Creates a new handler instance.
	 *
	 * @param authKey Authentication key to use. This must not be null or empty.
	 */
	public WelcomeMessageEncoder(String authName, String authKey) {
		if (authName == null || authName.isEmpty()) {
			throw new NullPointerException("name");
		} else if (authKey == null || authKey.isEmpty()) {
			throw new NullPointerException("authKey");
		}

		this.authName = authName;
		this.authKey = authKey;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
		Matcher m = WELCOME_PATTERN.matcher(msg);
		if (m.matches()) {
			String response = String.format("[%s v%s %s %s]", m.group(1), m.group(2), authName, authKey);
			out.add(response);

			ctx.pipeline().remove(this);
		} else {
			// Forward the message
			out.add(msg);
		}
	}

}
