// We can connect to the database through a server or in embedded mode. //<>//

// If we access throug a server, it must be running.
void  startServer() {
  try {
    server = new NetworkServerControl();
    server.start (null);
    println("Server started");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void  stopServer() {
  try {
    if (server!=null) server.shutdown();
    println("Server stopped");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void openDBbyServer(String db2Open) {
  try {
    conn = DriverManager.getConnection("jdbc:derby://localhost:1527/"
      + db2Open + ";create=true");
    stmt = conn.createStatement();
    print("DB is open");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void openEmbeddedDB(String db2Open) {
  try {
    conn = DriverManager.getConnection("jdbc:derby:" + db2Open
      + ";create=true");
    stmt = conn.createStatement();
    print("DB is open");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void deleteTable(String tableName) {
  try {
    stmt.execute("drop table " + tableName);
  } 
  catch (Exception e ) {
  }
}