package dbmanager;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

// We create this model in order to infuse certain classification in the combo box elements by putting first those
// more probably used.

public class MyComboModel extends DefaultComboBoxModel<MyColumnTypes> {

	private String[] arrTypes = { "BOOLEAN", "CHAR", "VARCHAR", "LONG VARCHAR", "SMALLINT", "INTEGER", "BIGINT",
			"FLOAT", "REAL", "DOUBLE", "NUMERIC", "DECIMAL", "DATE", "TIME", "TIMESTAMP", "OBJECT", "BLOB", "CLOB",
			"XML", "LONG VARCHAR FOR BIT DATA", "CHAR () FOR BIT DATA", "VARCHAR () FOR BIT DATA" };

	Vector<String> myColVec = new Vector<String>(arrTypes.length);

	public MyComboModel() {
		for (int i = 0; i < arrTypes.length; i++) {
			addElement(new MyColumnTypes());
			myColVec.addElement(arrTypes[i]);

		}

	}

	public int insertItem(MyColumnTypes param0) {

		int indType = myColVec.indexOf(param0.TYPE_NAME);

		if (indType >= 0 && indType < getSize()) {
			removeElementAt(myColVec.indexOf(param0.TYPE_NAME));
			insertElementAt(param0, myColVec.indexOf(param0.TYPE_NAME));
		} else // More types has been added to the DBMS. We increase our list size.
		{
			addElement(param0);
		}

		return 0;
	}

}
