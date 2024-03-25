/*
 * Copyright (C) 2017-2024 Amateurfunkgruppe der RWTH Aachen
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

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Custom serializer for {@link java.time.Instant} objects using ISO-8601
 * representation.
 */
public class InstantSerializer extends StdSerializer<Instant> {

	private static final long serialVersionUID = 1L;

	public InstantSerializer() {
		this(null);
	}

	protected InstantSerializer(Class<Instant> t) {
		super(t);
	}

	@Override
	public void serialize(Instant t, JsonGenerator jg, SerializerProvider sp) throws IOException {
		jg.writeString(t.toString());
	}

}
