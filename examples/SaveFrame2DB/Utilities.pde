byte[] ints2Bytes(int[] integers) throws IOException 
{
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  DataOutputStream dos = new DataOutputStream(baos);

  for (int i=0; i < integers.length; ++i)
  {
    dos.writeInt(integers[i]);
  }

  return baos.toByteArray();
}

public static int[] bytes2Ints(byte[] input)
{
  IntBuffer ib = ByteBuffer.wrap(input).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
  int[] ret = new int[ib.capacity()];
  ib.get(ret);
  return ret;
}