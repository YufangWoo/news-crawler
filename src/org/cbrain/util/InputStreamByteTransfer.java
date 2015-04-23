package org.cbrain.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamByteTransfer {

	public static  byte[] InputStreamToByte(InputStream is) throws IOException {
		   ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		   int ch;
		   while ((ch = is.read()) != -1) {
		    bytestream.write(ch);
		   }
		   byte imgdata[] = bytestream.toByteArray();
		   bytestream.close();
		   return imgdata;
		  }
	public static InputStream ByteToInputStream(byte[] data){
		return new ByteArrayInputStream(data);
	}
	

}
