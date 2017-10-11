private void copyFile(OutputStream os, InputStream is) throws IOException {
  BufferedOutputStream bos = new BufferedOutputStream(os);
  BufferedInputStream bis = new BufferedInputStream(is);
  int aByte;
  while ((aByte = is.read()) != -1) {
    bos.write(aByte);
  }
  bis.close();
  bos.close();
}