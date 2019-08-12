package testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient
{

	public static boolean sendMessage(Socket clientSocket, String message)
	{
		PrintWriter out;
		try
		{
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println(message);
			return true;
		} catch (IOException e)
		{
			return false;
		}

	}

	// initialize a client connection with a random name and initial chat
	// message

	public static boolean initConnection(Socket clientSocket, String name)
	{
		PrintWriter out;
		try
		{
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println(name);
			out.println("test");
			return true;
		} catch (IOException e)
		{
			return false;
		} 
	}

}
