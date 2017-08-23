package dbmanager;

import javax.swing.tree.DefaultMutableTreeNode;

public class DBTreeNodeK extends DefaultMutableTreeNode {

	private String category = ""; // Defines the type of node
	private String nodeText = ""; // Displayed text/name of the node
	private String pathLocation = ""; // File path to database
	private String dataType = ""; // Data type of dTypeName
	private String dBaseName = ""; // Database of the node
	private String dTypeName = ""; // Name of dataType
	private String fullTypeDesc = ""; // When category is COLUMN, column type + length

	public DBTreeNodeK(String category, String nodeText, String path, String dataType, String dBaseName,
			String dTypeName, String fullTypeDesc) {

		this.category = category;
		this.nodeText = nodeText;
		this.pathLocation = path; // Path to the DBase directory
		this.dataType = dataType;
		this.dBaseName = dBaseName;
		this.dTypeName = dTypeName;
		this.fullTypeDesc = fullTypeDesc;
	}

	@Override
	public boolean isLeaf() {
		return false;

		// if (category == "INDEXEDCOLUMN")
		// return true;
		// else
		// return false;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return nodeText;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.nodeText = text;
	}

	@Override
	public String toString() {
		return nodeText;
	}

	/**
	 * @return the pathLocation
	 */
	public String getPathLocation() {
		return pathLocation;
	}

	/**
	 * @param pathLocation
	 *            the pathLocation to set
	 */
	public void setPathLocation(String pathLocation) {
		this.pathLocation = pathLocation;
	}

	/**
	 * @return the nodeText
	 */
	public String getNodeText() {
		return nodeText;
	}

	/**
	 * @param nodeText
	 *            the nodeText to set
	 */
	public void setNodeText(String nodeText) {
		this.nodeText = nodeText;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the dBaseName
	 */
	public String getdBaseName() {
		return dBaseName;
	}

	/**
	 * @param dBaseName
	 *            the dBaseName to set
	 */
	public void setdBaseName(String dBaseName) {
		this.dBaseName = dBaseName;
	}

	/**
	 * @return the dTypeName
	 */
	public String getdTypeName() {
		return dTypeName;
	}

	/**
	 * @param dTypeName
	 *            the dTypeName to set
	 */
	public void setdTypeName(String dTypeName) {
		this.dTypeName = dTypeName;
	}

	/**
	 * @return the fullTypeDesc
	 */
	public String getFullTypeDesc() {
		return fullTypeDesc;
	}

	/**
	 * @param fullTypeDesc the fullTypeDesc to set
	 */
	public void setFullTypeDesc(String fullTypeDesc) {
		this.fullTypeDesc = fullTypeDesc;
	}

}
