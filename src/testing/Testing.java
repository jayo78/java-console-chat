package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import multiUserChatApplication.ChatServer;
import multiUserChatApplication.ConsoleChatClient;

class Testing
{

	@Test
	// simple one client connection test
	public void testConnection() throws IOException
	{
		// start a server
		ChatServer.start(false);
		
		Socket aSock = new Socket("localhost", 59002);
		
		assertTrue(TestClient.initConnection(aSock, "a_Dude"));
		assertTrue(TestClient.sendMessage(aSock, "hello world"));
		
		ChatServer.stop();
				
	}
	
}
