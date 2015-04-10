/**
 *  Copyright (C) 2002-2015   The FreeCol Team
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

package net.sf.freecol.client.gui.panel;

import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import net.miginfocom.swing.MigLayout;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.FontLibrary;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.action.ColopediaAction.PanelType;
import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.model.FoundingFather;
import net.sf.freecol.common.model.FoundingFather.FoundingFatherType;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.resources.ResourceManager;


/**
 * This panel displays details of founding fathers in the Colopedia.
 */
public class FatherDetailPanel extends ColopediaGameObjectTypePanel<FoundingFather> {


    /**
     * Creates a new instance of this ColopediaDetailPanel.
     *
     * @param freeColClient The <code>FreeColClient</code> for the game.
     * @param colopediaPanel The parent ColopediaPanel.
     */
    public FatherDetailPanel(FreeColClient freeColClient,
                             ColopediaPanel colopediaPanel) {
        super(freeColClient, colopediaPanel,
              PanelType.FATHERS.toString(), 0.75f);
    }


    /**
     * Adds one or several subtrees for all the objects for which this
     * ColopediaDetailPanel could build a detail panel to the given
     * root node.
     *
     * @param root a <code>DefaultMutableTreeNode</code>
     */
    @Override
    public void addSubTrees(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode parent =
            new DefaultMutableTreeNode(new ColopediaTreeItem(this, getId(), getName(), null));

        EnumMap<FoundingFatherType, List<FoundingFather>> fathersByType =
            new EnumMap<>(FoundingFatherType.class);
        for (FoundingFatherType fatherType : FoundingFatherType.values()) {
            fathersByType.put(fatherType, new ArrayList<FoundingFather>());
        }
        for (FoundingFather foundingFather : getSpecification().getFoundingFathers()) {
            fathersByType.get(foundingFather.getType()).add(foundingFather);
        }
        for (FoundingFatherType fatherType : FoundingFatherType.values()) {
            String id = FoundingFather.getTypeKey(fatherType);
            String typeName = Messages.message(id);
            DefaultMutableTreeNode node =
                new DefaultMutableTreeNode(new ColopediaTreeItem(this, id, typeName, null));

            parent.add(node);
            for (FoundingFather father : fathersByType.get(fatherType)) {
                ImageIcon icon = new ImageIcon(ImageLibrary.getMiscImage(ImageLibrary.BELLS, getScale()));
                node.add(buildItem(father, icon));
            }
        }
        root.add(parent);
    }

    /**
     * Builds the details panel for the FoundingFather with the given
     * identifier.
     *
     * @param id The object identifier.
     * @param panel the detail panel to build
     */
    @Override
    public void buildDetail(String id, JPanel panel) {
        try {
            FoundingFather father = getSpecification().getFoundingFather(id);
            buildDetail(father, panel);
        } catch (IllegalArgumentException e) {
            // this is not a founding father
            panel.setLayout(new MigLayout("wrap 1, align center", "align center"));
            JLabel header = Utility.localizedLabel(Messages.nameKey(id));
            header.setFont(FontLibrary.createFont(FontLibrary.FontType.HEADER,
                FontLibrary.FontSize.SMALL));
            panel.add(header, "align center, wrap 20");
            if (getId().equals(id)) {
                panel.add(Utility.getDefaultTextArea(
                    Messages.message("colopedia.foundingFather.description"), 40));
            } else {
                Image image = ResourceManager.getImage(id + ".image");
                if (image != null) {
                    header.setText(Messages.message(id));
                    panel.add(new JLabel(new ImageIcon(image)));
                }
            }
        }
    }

    /**
     * Builds the details panel for the given FoundingFather.
     *
     * @param father a FoundingFather
     * @param panel the detail panel to build
     */
    public void buildDetail(FoundingFather father, JPanel panel) {
        panel.setLayout(new MigLayout("wrap 2, fillx, gapx 20", "", ""));

        String name = Messages.getName(father);
        String type = Messages.message(father.getTypeKey());
        JLabel header = new JLabel(name + " (" + type + ")");
        header.setFont(FontLibrary.createFont(FontLibrary.FontType.HEADER,
            FontLibrary.FontSize.SMALL));

        Image image = ImageLibrary.getFoundingFatherImage(father, false);
        JLabel label = new JLabel(new ImageIcon(image));

        StringBuilder text = new StringBuilder();
        text.append(Messages.getDescription(father));
        text.append("\n\n[");
        text.append(Messages.message(father.getId() + ".birthAndDeath"));
        text.append("] ");
        text.append(Messages.message(father.getId() + ".text"));
        Turn turn = getMyPlayer().getElectionTurns().get(name);
        if (turn != null) {
            text.append("\n\n");
            text.append(Messages.message("report.continentalCongress.elected"));
            text.append(" ");
            text.append(Messages.message(turn.getLabel()));
        }
        JTextArea description = Utility.getDefaultTextArea(text.toString(), 20);

        panel.add(header, "span, align center, wrap 40");
        panel.add(label, "top");
        panel.add(description, "top, growx");

        Dimension hSize = header.getPreferredSize(),
            lSize = label.getPreferredSize(),
            dSize = description.getPreferredSize(), size = new Dimension();
        size.setSize(lSize.getWidth() + dSize.getWidth() + 20,
            hSize.getHeight() + lSize.getHeight() + 10);
        panel.setPreferredSize(size);            
    }

}
