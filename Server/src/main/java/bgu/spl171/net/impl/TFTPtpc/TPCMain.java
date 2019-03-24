package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.srv.Server;

public class TPCMain {
	public static void main(String args[]){
	
      Server.threadPerClient(
              Integer.valueOf(args[0]), //port
              () -> new BidiMessagingProtocolImpl(), //protocol factory
              MessageEncoderDecoderImpl::new //message encoder decoder factory
      ).serve();

	}
}
