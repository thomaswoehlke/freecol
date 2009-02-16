/**
 *  Copyright (C) 2002-2007  The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.networking;

import org.w3c.dom.Element;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;


/**
 * The message sent updating a unit's current stop.
 */
public class UpdateCurrentStopMessage extends Message {

    /**
     * The id of the unit whose stop is to be updated.
     */
    private String unitId;

    /**
     * Create a new <code>UpdateCurrentStopMessage</code> for the
     * supplied unit.
     *
     * @param unit A <code>Unit</code> whose stop is to be updated.
     */
    public UpdateCurrentStopMessage(Unit unit) {
        this.unitId = unit.getId();
    }

    /**
     * Create a new <code>UpdateCurrentStopMessage</code> from a
     * supplied element.
     *
     * @param game The <code>Game</code> this message belongs to.
     * @param element The <code>Element</code> to use to create the message.
     */
    public UpdateCurrentStopMessage(Game game, Element element) {
        this.unitId = element.getAttribute("unit");
    }

    /**
     * Handle a "updateCurrentStop"-message.
     *
     * @param connection The <code>Connection</code> the message was received on.
     * @param server The <code>FreeColServer</code> handling the message.
     *
     * @return Null.
     */
    public Element handle(FreeColServer server, Connection connection) {
        Unit unit = server.getUnitSafely(unitId, server.getPlayer(connection));

        unit.nextStop();
        return null;
    }

    /**
     * Convert this UpdateCurrentStopMessage to XML.
     *
     * @return The XML representation of this message.
     */
    public Element toXMLElement() {
        Element result = createNewRootElement(getXMLElementTagName());
        result.setAttribute("unit", unitId);
        return result;
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "updateCurrentStop".
     */
    public static String getXMLElementTagName() {
        return "updateCurrentStop";
    }
}
