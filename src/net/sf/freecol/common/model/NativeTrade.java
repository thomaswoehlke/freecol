/**
 *  Copyright (C) 2002-2016   The FreeCol Team
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

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.ChoiceItem;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import static net.sf.freecol.common.model.Constants.*;
import net.sf.freecol.common.model.AbstractGoods;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Unit;
import static net.sf.freecol.common.util.CollectionUtils.*;


/**
 * Container class for the information that persists while a
 * native trade session is underway.
 */
public class NativeTrade extends FreeColGameObject {

    /** A template to use as a magic cookie for aborted trades. */
    private static final StringTemplate abortTrade
        = StringTemplate.template("");

    /** The type of native trade command. */
    public static enum NativeTradeAction {
        UPDATE,     // Server update of a session, rest are client-sent
        OPEN,       // Start a new trade session
        CLOSE,      // End an existing session
        BUY,        // Buy goods
        SELL,       // Sell goods
        GIFT,       // Gift goods
        PRICE_BUY,  // Get a price to buy
        PRICE_SELL, // Get a price to sell
    };

    /** Trading result types. */
    public static final int NO_TRADE_GOODS = 0,
                            NO_TRADE = -1,
                            NO_TRADE_HAGGLE = -2,
                            NO_TRADE_HOSTILE = -3;

    /** The unit that is trading. */
    private Unit unit;

    /** The settlement to trade with. */
    private IndianSettlement settlement;

    /** How many times this trade has been tried. */
    private int count;

    /** Can goods be bought? */
    private boolean buy;
        
    /** Can goods be sold? */
    private boolean sell;

    /** Can goods be given? */
    private boolean gift;


    /**
     * Simple constructor, used in FreeColGameObject.newInstance.
     *
     * @param game The enclosing <code>Game</code>.
     * @param id The identifier (ignored).
     */
    public NativeTrade(Game game, String id) {
        super(game, ""); // Identifier not required
    }

    /**
     * Create a new trade session.
     *
     * @param unit The <code>Unit</code> that is trading.
     * @param settlement The <code>IndianSettlement</code> to trade with.
     */
    public NativeTrade(Unit unit, IndianSettlement settlement) {
        this(unit.getGame(), null);

        this.unit = unit;
        this.settlement = settlement;
        this.count = 0;

        boolean atWar = this.settlement.getOwner()
            .atWarWith(this.unit.getOwner());
        this.buy = !atWar;
        this.sell = !atWar && this.unit.hasGoodsCargo();
        this.gift = this.unit.hasGoodsCargo();
    }


    public String getKey() {
        return getKey(this.unit, this.settlement);
    }
    
    public static String getKey(Unit unit, IndianSettlement is) {
        return unit.getId() + "-" + is.getId();
    }
    
    public Unit getUnit() {
        return this.unit;
    }

    public IndianSettlement getSettlement() {
        return this.settlement;
    }

    public boolean getBuy() {
        return this.buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }
    
    public boolean getSell() {
        return this.sell;
    }

    public void setSell(boolean sell) {
        this.sell = sell;
    }
    
    public boolean getGift() {
        return this.gift;
    }

    public void setGift(boolean gift) {
        this.gift = gift;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean getDone() {
        return this.count < 0
            || (!getBuy() && !getSell() && !getGift());
    }

    public void setDone() {
        this.count = -1;
    }


    // Serialization

    private static final String BUY_TAG = "buy";
    private static final String COUNT_TAG = "count";
    private static final String GIFT_TAG = "gift";
    private static final String SELL_TAG = "sell";
    private static final String SETTLEMENT_TAG = "settlement";
    private static final String UNIT_TAG = "unit";


    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeAttributes(xw);

        xw.writeAttribute(BUY_TAG, this.buy);
        
        xw.writeAttribute(COUNT_TAG, this.count);
        
        xw.writeAttribute(GIFT_TAG, this.gift);

        xw.writeAttribute(SELL_TAG, this.sell);

        xw.writeAttribute(SETTLEMENT_TAG, this.settlement);

        xw.writeAttribute(UNIT_TAG, this.unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        this.buy = xr.getAttribute(BUY_TAG, false);

        this.count = xr.getAttribute(COUNT_TAG, -1);
        
        this.gift = xr.getAttribute(GIFT_TAG, false);

        this.sell = xr.getAttribute(SELL_TAG, false);

        this.settlement = xr.getAttribute(getGame(), SETTLEMENT_TAG,
                                          IndianSettlement.class,
                                          (IndianSettlement)null);

        this.unit = xr.getAttribute(getGame(), UNIT_TAG,
                                    Unit.class, (Unit)null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("[").append(getId())
            .append(" ").append(getUnit().getId())
            .append(" ").append(getSettlement().getId())
            .append(" buy=").append(getBuy())
            .append(" sell=").append(getSell())
            .append(" gift=").append(getGift())
            .append(" count=").append(getCount())
            .append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXMLTagName() { return getTagName(); }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "nativeTrade".
     */
    public static String getTagName() {
        return "nativeTrade";
    }
}
