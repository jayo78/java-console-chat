package multiUserChatApplication;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

// chat server for interaction with multiple clients through a tcp socket connection.
// contains an inner class for handling each client connection synchronously.
// the server writes simple requests and responses to each client to enable communication
// between all connected clients through the use of a map of users and a broadcast method

public class ChatServer
{

//----------------------

	// ClientHandler class:
	// implements the run method from Runnable to enable thread creation for
	// handling each
	// new client socket

	private static class ClientHandler implements Runnable
	{
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String name;

		public ClientHandler(Socket socket)
		{
			this.socket = socket;
		}

		@Override
		public void run()
		{
			if (verbose)
				System.out.println("client connected " + socket.getInetAddress());

			try
			{
				in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				// SUBMITNAME request loop
				// Continually request a screen name for the client until valid,
				// then add to map of clients
				while (true)
				{
					out.println("SUBMITNAME");
					name = in.readLine();

					if (name == null)
					{
						// Haven't added name to map yet ... return and close
						// connection
						return;
					}

					// prevent a data race
					synchronized (connectedClients)
					{
						if (!name.isEmpty()
								&& !connectedClients.keySet().contains(name))
							break;
						else
							out.println("INVALIDNAME");

					}
				}

				// NAMEACCEPTED verification response sent to client
				// client join broadcasted with accepted screen handle
				out.println("NAMEACCEPTED " + name);
				if (verbose)
					System.out.println(name + " has joined");
				broadcastMessage(name + " has joined");
				connectedClients.put(name, out);

				// SENDMESSAGE request sent to client to begin receiving chat
				// input
				// while the client is connected, server will listen to input
				// stream
				// to receive messages and broadcast to all clients in map
				String message;
				out.println("STARTCHAT");
				while ((message = in.readLine()) != null)
				{
					if (!(message.isEmpty()))
					{
						if (message.toLowerCase().equals("/quit"))
						{
							break;
						}

						broadcastMessage(name + ": " + message);
					}
				}

			} catch (Exception e)
			{
				if (verbose)
					System.out.println(e);
			}

			finally
			{
				// Remove client from map on disconnect or user quit
				// - disconnect results in a caught exception which is printed
				// to
				// server console
				// - quit results from leaving client input loop, thread is then
				// terminated, disconnecting the client
				if (name != null)
				{
					if (verbose)
						System.out.println(name + " is leaving");

					connectedClients.remove(name);
					broadcastMessage(name + " has left");
				}
			}
		}

	}
//----------------------

	// Map of clients connected to the server
	private static HashMap<String, PrintWriter> connectedClients = new HashMap<>();

	// Set maximum amount of connected clients
	private static final int MAX_CONNECTED = 50;
	// Server port
	private static final int PORT = 59002;

	private static boolean verbose;

	private static ServerSocket listener;

	// Broadcast to all clients in map by writing to their output streams
	private static void broadcastMessage(String message)
	{
		for (PrintWriter p : connectedClients.values())
		{
			p.println(message);
		}
	}

	// starts listening for connections, creating threads to handle incoming
	// connections on the
	// specified port

	public static void start(boolean isVerbose)
	{
		verbose = isVerbose;

		try
		{
			listener = new ServerSocket(PORT);

			// verbose
			if (verbose)
			{
				System.out.println("Server started on port: " + PORT);
				System.out.println("Now listening for connections ...");
			}

			// client connection loop to accept new socket connections
			while (true)
			{
				// limit to a maximum amount of connected clients (a client
				// could disconnect, allowing a new connection)
				if (connectedClients.size() <= MAX_CONNECTED)
				{
					// dispatch a new ClientHandler thread to the socket
					// connection
					Thread newClient = new Thread(
							new ClientHandler(listener.accept()));
					newClient.start();
				}

			}
		} catch (BindException e)
		{
			// server already started on this port ... continue
		} catch (Exception e)
		{
			// error verbose
			if (verbose)
			{
				System.out.println("\nError occured: \n");
				e.printStackTrace();
				System.out.println("\nExiting...");
			}
		}
	}

	public static void stop() throws IOException
	{
		if (!listener.isClosed())
			listener.close();
	}

	// server class entry
	// stop or start the server

	public static void main(String[] args) throws IOException
	{
		boolean isVerbose;
		isVerbose = (args.length == 1
				&& args[0].toLowerCase().equals("verbose")) ? true : false;
		start(isVerbose);
	}
}
