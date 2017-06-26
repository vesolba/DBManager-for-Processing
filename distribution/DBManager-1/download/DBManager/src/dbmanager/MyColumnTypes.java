package dbmanager;

public class MyColumnTypes {

	public String TYPE_NAME; // => Type name
	public int DATA_TYPE;// => SQL data type from java.sql.Types
	public int PRECISION; // => maximum precision
	public String LITERAL_PREFIX; // => prefix used to quote a literal (may be null)
	public String LITERAL_SUFFIX; // => suffix used to quote a literal (may be null)
	public String CREATE_PARAMS; // => parameters used in creating the type (may be null)
	public short NULLABLE; // => can you use NULL for this type.
	// typeNoNulls - does not allow NULL values
	// typeNullable - allows NULL values
	// typeNullableUnknown - nullability unknown
	public boolean CASE_SENSITIVE; // => is it case sensitive.
	public short SEARCHABLE; // => can you use "WHERE" based on this type:
	// typePredNone - No support
	// typePredChar - Only supported with WHERE .. LIKE
	// typePredBasic - Supported except for WHERE .. LIKE
	// typeSearchable - Supported for all WHERE ..
	public boolean UNSIGNED_ATTRIBUTE; // => is it unsigned.
	public boolean FIXED_PREC_SCALE; // => can it be a money value.
	public boolean AUTO_INCREMENT; // => can it be used for an auto-increment value.
	public String LOCAL_TYPE_NAME; // => localized version of type name (may be null)
	public short MINIMUM_SCALE; // => minimum scale supported
	public short MAXIMUM_SCALE; // => maximum scale supported
	public int SQL_DATA_TYPE; // => unused
	public int SQL_DATETIME_SUB; // => unused
	public int NUM_PREC_RADIX; // => usually 2 or 10

	public MyColumnTypes() {

		TYPE_NAME = ""; // => Type name
		DATA_TYPE = 0;// => SQL data type from java.sql.Types
		PRECISION = 0; // => maximum precision
		LITERAL_PREFIX = ""; // => prefix used to quote a literal (may be null)
		LITERAL_SUFFIX = ""; // => suffix used to quote a literal (may be null)
		CREATE_PARAMS = ""; // => parameters used in creating the type (may be null)
		short NULLABLE = 0; // => can you use NULL for this type.
		// typeNoNulls - does not allow NULL values
		// typeNullable - allows NULL values
		// typeNullableUnknown - nullability unknown
		CASE_SENSITIVE = false; // => is it case sensitive.
		SEARCHABLE = 0; // => can you use "WHERE" based on this type:
		// typePredNone - No support
		// typePredChar - Only supported with WHERE .. LIKE
		// typePredBasic - Supported except for WHERE .. LIKE
		// typeSearchable - Supported for all WHERE ..
		UNSIGNED_ATTRIBUTE = false; // => is it unsigned.
		FIXED_PREC_SCALE = false; // => can it be a money value.
		AUTO_INCREMENT = false; // => can it be used for an auto-increment value.
		LOCAL_TYPE_NAME = ""; // => localized version of type name (may be null)
		MINIMUM_SCALE = 0; // => minimum scale supported
		MAXIMUM_SCALE = 0; // => maximum scale supported
		SQL_DATA_TYPE = 0; // => unused
		SQL_DATETIME_SUB = 0; // => unused
		NUM_PREC_RADIX = 0; // => usually 2 or 10

	}

	public MyColumnTypes(String itemname) {

		TYPE_NAME = itemname; // => Type name
		DATA_TYPE = 0;// => SQL data type from java.sql.Types
		PRECISION = 0; // => maximum precision
		LITERAL_PREFIX = ""; // => prefix used to quote a literal (may be null)
		LITERAL_SUFFIX = ""; // => suffix used to quote a literal (may be null)
		CREATE_PARAMS = ""; // => parameters used in creating the type (may be null)
		short NULLABLE = 0; // => can you use NULL for this type.
		// typeNoNulls - does not allow NULL values
		// typeNullable - allows NULL values
		// typeNullableUnknown - nullability unknown
		CASE_SENSITIVE = false; // => is it case sensitive.
		SEARCHABLE = 0; // => can you use "WHERE" based on this type:
		// typePredNone - No support
		// typePredChar - Only supported with WHERE .. LIKE
		// typePredBasic - Supported except for WHERE .. LIKE
		// typeSearchable - Supported for all WHERE ..
		UNSIGNED_ATTRIBUTE = false; // => is it unsigned.
		FIXED_PREC_SCALE = false; // => can it be a money value.
		AUTO_INCREMENT = false; // => can it be used for an auto-increment value.
		LOCAL_TYPE_NAME = ""; // => localized version of type name (may be null)
		MINIMUM_SCALE = 0; // => minimum scale supported
		MAXIMUM_SCALE = 0; // => maximum scale supported
		SQL_DATA_TYPE = 0; // => unused
		SQL_DATETIME_SUB = 0; // => unused
		NUM_PREC_RADIX = 0; // => usually 2 or 10

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return TYPE_NAME;
	}
}
