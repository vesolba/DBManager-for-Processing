package dbmanager;

import javax.swing.JTree;

/**
 * 
 * Utility class that can be used to retrieve and/or restore the expansion state
 * of a JTree.
 * 
 * @author G. Cope
 *
 * 
 * 
 */

public class TreeExpansionUtil {

	private final JTree tree;

	/**
	 * 
	 * Constructs a new utility object based upon the parameter JTree
	 * 
	 * @param tree
	 * 
	 */

	public TreeExpansionUtil(JTree tree) {

		this.tree = tree;

	}

	/**
	 * 
	 * Retrieves the expansion state as a String, defined by a comma delimited list
	 * of
	 * 
	 * each row node that is expanded.
	 * 
	 * @return
	 * 
	 */

	public String getExpansionState() {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < tree.getRowCount(); i++) {

			if (tree.isExpanded(i)) {

				sb.append(i).append(",");

			}

		}

		return sb.toString();

	}

	/**
	 * 
	 * Sets the expansion state based upon a comma delimited list of row indexes
	 * that
	 * 
	 * are expanded.
	 * 
	 * @param s
	 * 
	 */

	public void setExpansionState(String s) {

		String[] indexes = s.split(",");

		for (String st : indexes) {

			int row = Integer.parseInt(st);

			tree.expandRow(row);

		}

	}
	

}
