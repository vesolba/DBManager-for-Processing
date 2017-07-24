package dbmanager;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class MyTreeCellRenderer extends DefaultTreeCellRenderer {

	ImageIcon dbServIcon = null;
	ImageIcon databaseIcon = null;
	ImageIcon tablesIcon = null;
	ImageIcon tableIcon = null;
	ImageIcon columnIcon = null;
	ImageIcon dummyIcon = null;
	ImageIcon underconsIcon = null;
	String category = "";
	ImageIcon icon = null;

	public MyTreeCellRenderer() {
		super();
		jbInit();
	}

	private void jbInit() {

		dbServIcon = new ImageIcon(this.getClass().getResource("/data/DBServ-32.png"));
		databaseIcon = new ImageIcon(this.getClass().getResource("/data/JavaDB-32.png"));
		tablesIcon = new ImageIcon(this.getClass().getResource("/data/Tables-32.png"));
		tableIcon = new ImageIcon(this.getClass().getResource("/data/Table-32.png"));
		columnIcon = new ImageIcon(this.getClass().getResource("/data/Column-32.png"));
		dummyIcon = new ImageIcon(this.getClass().getResource("/data/Dummy-32.png"));
		underconsIcon = new ImageIcon(this.getClass().getResource("/data/UnderCons-32.png"));
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value != null) {
			DBTreeNodeK selectedNodeInfo = (DBTreeNodeK) ((DefaultMutableTreeNode) value).getUserObject();
			category = selectedNodeInfo.getCategory();

			switch (category) {
			case "root":
				icon = dbServIcon;
				break;
			case "Java DB":
				icon = databaseIcon;
				break;
			case "HEAD":
				icon = tablesIcon;
				break;
			case "TABLE":
				icon = tableIcon;
				break;
			case "COLUMN":
				icon = columnIcon;
				break;
			case "DUMMY":
				icon = dummyIcon;
				break;
			case "INDEX":
				icon = columnIcon;
				break;
			case "INDEXEDCOLUMN":
				icon = columnIcon;
				break;
			default:
				icon = dummyIcon;
				return null;
			}

			if (icon != null) {
				setIcon(icon);
			} else {
				setToolTipText(null); // no tool tip
			}
		}
		return this;
	}

}
