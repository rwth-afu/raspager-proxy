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

import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class provides the REST API for the connection status report.
 *
 * @author Philipp Thiel
 */
@Path("status")
public class ConnectionStatusResource {

    @Context
    private Application app;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        ConnectionStatusManager manager = getStatusManager();
        Collection<ConnectionStatus> active = manager.getConnections();

        return Response.ok(active).build();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("name") String name) {
        ConnectionStatusManager manager = getStatusManager();
        ConnectionStatus status = manager.get(name);
        if (status != null) {
            return Response.ok(status).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private ConnectionStatusManager getStatusManager() {
        return (ConnectionStatusManager) app.getProperties().get("proxyStatusManager");
    }
}
