/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package net.clementlevallois.givecolortonodes;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.tools.spi.MouseClickEventListener;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Tool which reacts to clicks on the canvas by adding nodes and edges.
 * <p>
 * The tool works with two
 * <code>ToolEventListener</code> listeners: One {@link MouseClickEventListener}
 * to react on a click on a empty part of the canvas and one
 * {@link NodeClickEventListener} to react on a click on multiple nodes. The
 * tool is creating a node at the mouse location and adds edges from the newly
 * created node to all selected nodes. That works when the user increases its
 * mouse selection area.
 * <p>
 * The tool also uses some non-api methods of
 * <code>VizController</code>. It's not really recommended at this point but we
 * needed it for the mouse position. The new Visualization API coming in a
 * future version will expose much more things...
 * <p>
 * This tool class also has an UI class which displays a simple checkbox in the
 * properties bar. The checkbox triggers a layout algorithm.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class AddNodesTool implements Tool {

    private final AddNodesToolUI ui = new AddNodesToolUI();
    private AttributeColumn color;

    @Override
    public void select() {
        NotifyDescriptor d = new NotifyDescriptor.Message("Trying to find a column containing \"color\" in your nodes attributes, it will then color the nodes accordingly");
        DialogDisplayer.getDefault().notify(d);

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() == null) {
            d = new NotifyDescriptor.Message("No project opened in Gephi.");
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = gc.getModel();
        Graph graph = graphModel.getGraph();

        AttributeModel attModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        for (AttributeColumn c : attModel.getNodeTable().getColumns()) {
            if (c.getId().toLowerCase().contains("color")) {
                color = c;
                break;
            }
        }
        if (color == null) {
            d = new NotifyDescriptor.Message("No node attribute containing \"color\" could be found");
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        Color hex;

        for (Node node : graph.getNodes().toArray()) {
            String colorString = (String) node.getNodeData().getAttributes().getValue(color.getIndex());
            if (colorString.contains(",")) {
                String[] RGB = colorString.split(",");
                node.getNodeData().setR(Float.valueOf(RGB[0])/255);
                node.getNodeData().setG(Float.valueOf(RGB[1])/255);
                node.getNodeData().setB(Float.valueOf(RGB[2])/255);
            } else if (colorString.contains("#")) {
                hex = Color.decode(colorString);
                node.getNodeData().setR(Float.valueOf(hex.getRed())/255);
                node.getNodeData().setG(Float.valueOf(hex.getGreen())/255);
                node.getNodeData().setB(Float.valueOf(hex.getBlue())/255);
            } else {
                d = new NotifyDescriptor.Message("No rgb or hex color format detected in " + color.getTitle());
                DialogDisplayer.getDefault().notify(d);
                return;

            }
        }




    }

    @Override
    public void unselect() {
    }

    @Override
    public ToolEventListener[] getListeners() {
        return new ToolEventListener[]{};
    }

    @Override
    public ToolUI getUI() {
        return ui;
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }

    private static class AddNodesToolUI implements ToolUI {

        @Override
        public JPanel getPropertiesBar(Tool tool) {
            JPanel panel = new JPanel();

            return panel;
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/org/gephi/plugins/example/tool/resources/colorwheel.png"));
        }

        @Override
        public String getName() {
            return "Color nodes";
        }

        @Override
        public String getDescription() {
            return "Color nodes based on a RGB or hex color attribute";
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }
}
