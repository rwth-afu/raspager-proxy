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

/**
 * This interface defines proxy service events.
 *
 * @author Philipp Thiel
 */
interface ProxyEventListener {

    /**
     * Called when a proxy service caught an exception.
     *
     * @param service Service instance
     * @param cause Exception
     */
    void onException(ProxyService service, Throwable cause);

    /**
     * Called when a proxy service is closed.
     *
     * @param service Service instance
     */
    void onClose(ProxyService service);

}
