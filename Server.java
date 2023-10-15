package chat;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Server implements Runnable{
	
	
	ServerSocket server;
	private ArrayList<ConnectionHandler> connections;
	private boolean done;
	private ExecutorService pool;
	
	public Server() {
		connections = new ArrayList<>();
		done = false;
		
	}
	
	
	
	public void run() {
		try {
			server = new ServerSocket(9999);
			pool = Executors.newCachedThreadPool();
			while(!done) {
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}
			
		}catch(IOException e) {
			shutdown();
		}

	}
	

	public void broadcast(String message) {
		for(ConnectionHandler ch: connections) {
			if(ch!=null) {
				ch.sendMessage(message);
			}
		}
	}
	
	public void shutdown() {
		try {
			done = true;
			pool.shutdown();
			if(server != null && !server.isClosed()) {
				server.close();
			}
			for(ConnectionHandler ch:connections) {
				ch.shutdown();
			}
		}catch(IOException e) {
			//ignore
		}
		
	}
	
	class ConnectionHandler implements Runnable {

	    private Socket client;
	    private BufferedReader in;
	    private PrintWriter out;
	    private String nickname;

	    public ConnectionHandler(Socket client) {
	        this.client = client;
	    }


	    public void run() {
	        try {
	            if (client != null) {
	                out = new PrintWriter(client.getOutputStream(), true);
	                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	                out.println("Please enter a nickname: ");
	                nickname = in.readLine();
	                System.out.println(nickname + " connected!");
	                broadcast(nickname + " joint the chat");
	                String message;
	                while ((message = in.readLine()) != null) {
	                    if (message.startsWith("/nick ")) {
	                        String[] messageSplit = message.split(" ", 2);
	                        if (messageSplit.length == 2) {
	                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
	                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
	                            nickname = messageSplit[1];
	                            out.println("Successfully changed nickname to " + nickname);
	                        } else {
	                            out.println("No nickname provided");
	                        }
	                    } else if (message.startsWith("/quit")) {
	                        broadcast(nickname + ":" + " left the chat");
	                    } else {
	                        broadcast(nickname + ": " + message);
	                    }
	                }
	            }
	        } catch (IOException e) {
	            shutdown();
	        }

	    }

	    public void sendMessage(String message) {
	        out.println(message);
	    }

	    public void shutdown() {
	        try {
	            in.close();
	            out.close();
	            if (client != null && !client.isClosed()) {
	                client.close();
	            }
	        } catch (IOException e) {
	            // ignore
	        }

	    }
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
		
	}
	

}
