package dbmanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class DBManageProps {

	Properties defaultProps, appProps;
	String defPropsFileName, appPropsFileName;
	OutputStream output;
	InputStream input;
	String pathToDBMSettings;

	public DBManageProps(String path) {
		pathToDBMSettings = path;
		defaultProps = new Properties(); // Default installed properties.
		appProps = new Properties(); // Properties changed by application.
		defPropsFileName = "dbdefault.properties"; // files for
		appPropsFileName = "dbmanager.properties"; // properties
		output = null;
		input = null;
	}

	public void readProperties() {

		try {
			// We always read all properties, the defaults ones and those modified by
			// the application.
			input = new FileInputStream(DBManager.pathToDBSettings + "/" + defPropsFileName);
			defaultProps.load(input);

			appProps = new Properties(defaultProps); // Copy def props into app props

			input = new FileInputStream(DBManager.pathToDBSettings + "/" + appPropsFileName);
			appProps.load(input);

			input.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public void saveProperties() {

		try {
			output = new FileOutputStream(DBManager.pathToDBSettings + "/" + appPropsFileName);
			appProps.store(output, "---Current Properties of DB Manager---");
			output.close();

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public void saveInitialProperties() {

		try {
			output = new FileOutputStream(pathToDBMSettings + "/" + appPropsFileName);
			appProps.store(output, "---Current Properties of DB Manager---");

			output.close();

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public String getDBMProp(String key) {
		String value = "";

		value = appProps.getProperty(key);
		return value;
	}

	public void setDBMProp(String key, String value) {
		appProps.setProperty(key, value);
	}

	public void removeProp(String key) {
		appProps.remove(key);
	}

}