package multiUserChatApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// chat client for interaction with a chat server through a tcp connection.
// contains two private inner classes to handle client to server communication.
// both inner classes implement Runnable to enable thread creation for synchronous
// reading from and writing to the server connection.
// allows for continuous communication with a server.

public class ConsoleChatClient
{
	// Listener class:
	// implements the run method from Runnable to enable thread creation for the
	// listening portion of a server to client communication

	private static class Listener implements Runnable
	{
		// BufferedReader for buffered byte reading from the server connection
		private BufferedReader in;

		@Override
		public void run()
		{
			try
			{
				// get the input stream from the server socket connection, using
				// the buffered reader for byte reading
				in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));

				String read;
				while (true)
				{
					read = in.readLine();
					if (read != null && !(read.isEmpty()))
						System.out.println(read);
				}
			} catch (IOException e)
			{
				// end read connection
				return;
			}
		}

	}

	// Writer class:
	// implements the run method from Runnable to enable thread creation for the
	// writing portion of a server to client communication

	private static class Writer implements Runnable
	{
		// PrintWriter for byte writing from characters to the server connection
		private PrintWriter out;

		@Override
		public void run()
		{
			// read user input for writing to server
			Scanner write = new Scanner(System.in);

			try
			{
				// get the output stream to the server socket connection, using
				// the print writer for byte writing
				out = new PrintWriter(clientSocket.getOutputStream(), true);

				while (true)
				{
					if (write.hasNext())
						out.println(write.nextLine());
				}

			} catch (IOException e)
			{
				// end write connection
				write.close();
				return;
			}
		}

	}

	// the server connection through a java socket
	private static Socket clientSocket;

	public static void main(String[] args)
	{
		if(args.length != 1)
			throw new IllegalArgumentException();
		
		// start a client connection to a host arg
		try
		{
			clientSocket = new Socket(args[0], 59002);
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// start the communication threads
		new Thread(new Writer()).start();
		new Thread(new Listener()).start();

	}
}
