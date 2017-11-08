void createBlobsTable() { 
  // The previous table is dropped
  String order0 = "DROP TABLE FRAMEBLOBS ";
  // BLOB table creation
  String order1 = "CREATE TABLE FRAMEBLOBS " +
    "(IDKEY INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY " +
    "(START WITH 1, INCREMENT BY 1)," +  //Autoincrement field
    "TITLE VARCHAR(50), " + //
    "IMAGE BLOB)";
  //-- PRIMARY/UNIQUE
  String order2 = "ALTER TABLE FRAMEBLOBS ADD CONSTRAINT ANYNAME PRIMARY KEY (IDKEY)";

  try {
    stmt.execute(order0);
  }  
  catch (Exception e ) {
    // There is not table to delete
  }
  try {
    stmt.execute(order1);
    stmt.execute(order2);
  } 
  catch (SQLSyntaxErrorException s) {
  }  
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void prepareInsert() {
  try {
    ps = conn.prepareStatement("INSERT INTO FRAMEBLOBS(TITLE, IMAGE) " +
      "VALUES (?, ?)");
  }
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void saveFrame2DB(PImage frame) {
  try {
    frame.loadPixels();
    byte[] bytes = ints2Bytes(frame.pixels);
    title = "Frame " + countFrames;
    ps.setString(1, title);
    ps.setBinaryStream(2, new ByteArrayInputStream(bytes), bytes.length);
    ps.executeUpdate();
  }
  catch (Exception e ) {
    e.printStackTrace();
  }
}