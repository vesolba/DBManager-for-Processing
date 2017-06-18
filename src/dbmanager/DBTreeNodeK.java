package dbmanager;

import javax.swing.tree.DefaultMutableTreeNode;

public class DBTreeNodeK extends DefaultMutableTreeNode {
	
	private String category = "";
	private String nodeText = "";
	private String pathLocation = "";
	private String dataType = "";
	private String dBaseName = "";
	private String dTypeName = ""; 

	public DBTreeNodeK(String category, String text) {
		this.category = category;
		this.nodeText = text;
	}

	public DBTreeNodeK(String category, String nodeText, String path, String dataType, String dBaseName, String dTypeName) {
		this.category = category;
		this.nodeText = nodeText;
		this.pathLocation = path; // Path to the DBase directory
		this.dataType = dataType;
		this.dBaseName = dBaseName;
		this.dTypeName = dTypeName;
	}
	
	@Override
	public boolean isLeaf() {
		return false;
		
//		if (category == "INDEXEDCOLUMN")
//			return true;
//		else
//			return false;
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
	 * @param nodeText the nodeText to set
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
	 * @param dataType the dataType to set
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
	 * @param dBaseName the dBaseName to set
	 */
	public void setdBaseName(String dBaseName) {
		this.dBaseName = dBaseName;
	}

}
