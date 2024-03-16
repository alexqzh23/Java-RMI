package server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import client.ChatClient3IF;
import java.util.Scanner;

/**
 * 
 * @authors			(1001)Team8
 * RMI Project		May 2022
 *
 */
public class ChatServer extends UnicastRemoteObject implements ChatServerIF {
	String line = "\n\n---------------------------------------------\n";
	private static Vector<Chatter> chatters;
	private static final long serialVersionUID = 1L;
	
	//Constructor
	public ChatServer() throws RemoteException {
		super();
		chatters = new Vector<Chatter>(10, 1);
	}
	
	//-----------------------------------------------------------
	/**
	 * LOCAL METHODS
	 */	
	public static void main(String[] args) {
		startRMIRegistry();
		String myHostName = "localhost";  // no need to enter PORT
		String serverServiceName = "ServerListenService";
		
		if(args.length == 2){
			myHostName = args[0];
			serverServiceName = args[1];
		}
		
		try{
			ChatServerIF hello = new ChatServer();
			// Naming.rebind("rmi://127.0.0.1:1009/serviceName", Bound instance)
			Naming.rebind("rmi://" + myHostName + "/" + serverServiceName, hello);
			System.out.println("Client-Listen RMI Server is running...");
			
			Scanner input = new Scanner(System.in);
			// If the input is not an integer, an InputMismatchException exception will be thrown
			while(true) {
				try{
					System.out.print("\nPlease enter 1 to get the sum of all connected nodes: ");
					int a = input.nextInt();
					if(a == 1) {
						if(chatters.size() > 0) {
							hello.recvSum("", 0, 0);
						}
						else {
							System.out.println("No client now!");
						}
					}
				}
				catch(Exception e){ // use Exception to catch this exception
					// e.printStackTrace();
					input.next();
				}
			}
		}
		catch(Exception e){
			System.out.println("Server had problems starting");
		}
	}


	/**
	 * Start the RMI Registry
	 */
	public static void startRMIRegistry() {
		try{
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server ready: " + getIpAddress());
		}
		catch(RemoteException e) {
			System.out.println("Port already in use !\n");
			//e.printStackTrace();
			System.exit(0);
		}
	}

	
	//-----------------------------------------------------------
	/*
	 *   REMOTE METHODS
	 */
	
	/**
	 * Return a message to client
	 */
//	public String sayHello(String ClientName) throws RemoteException {
//		System.out.println(ClientName + " sent a message");
//		return "Hello " + ClientName + " from server";
//	}
	
	public static String getIpAddress() {
	    try {
	      Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
	      InetAddress ip = null;
	      while (allNetInterfaces.hasMoreElements()) {
	    	  NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
	    	  if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
	    		  continue;
	    	  } else {
	    		  Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
	    		  while (addresses.hasMoreElements()) {
	    			  ip = addresses.nextElement();
	    			  if (ip != null && ip instanceof Inet4Address) {
	    				  return ip.getHostAddress();
	    			  }
	    		  }
	    	  }
	      }
	    } catch (Exception e) {
	    	System.err.println("Failed to get IP address" + e.toString());
	    }
	    return "";
	}

	/**
	 * Send a string ( the latest post, mostly ) 
	 * to all connected clients
	 */
	public void updateChat(String name, String nextPost) throws RemoteException {
		String message =  name + " : " + nextPost + "\n";
		sendToAll(message);
	}
	
	/**
	 * Receive a new client remote reference
	 */
	@Override
	public void passIDentity(RemoteRef ref) throws RemoteException {	
		//System.out.println("\n" + ref.remoteToString() + "\n");
		try{
			System.out.println(line + ref.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}//end passIDentity

	
	/**
	 * Receive a new client and display details to the console
	 * send on to register method
	 */
	@Override
	public void registerListener(String[] details) throws RemoteException {	
		System.out.println(line + new Date(System.currentTimeMillis()));
		System.out.println(details[0] + " has joined the chat session");
		System.out.println(details[0] + "'s hostname : " + details[1]);
		System.out.println(details[0] + "'s RMI service : " + details[2] + "\n");
		registerChatter(details);
	}


	/**
	 * register the clients interface and store it in a reference for 
	 * future messages to be sent to, i.e. other members' messages of the chat session.
	 * send a test message for confirmation / test connection
	 * @param details
	 */
	private void registerChatter(String[] details){
		try{
			Registry registry = LocateRegistry.getRegistry(details[1]);
			
			ChatClient3IF nextClient = (ChatClient3IF)registry.lookup(details[2]);
			
			chatters.addElement(new Chatter(details[0], nextClient));
			
			nextClient.messageFromServer("[Server] : Hello " + details[0] + ", you are now free to chat.\n");
			
			sendToAll("[Server] : " + details[0] + " has joined the group.\n");
			
			updateUserList();		
		}
		catch(RemoteException | NotBoundException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Update all clients by remotely invoking their
	 * updateUserList RMI method
	 */
	private void updateUserList() {
		String[] currentUsers = getUserList();	
		for(Chatter c : chatters){
			try {
				c.getClient().updateUserListUI(currentUsers);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * generate a String array of current users
	 * @return
	 */
	private String[] getUserList(){
		// generate an array of current users
		String[] allUsers = new String[chatters.size()];
		for(int i = 0; i < allUsers.length; i++){
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
	}
	

	/**
	 * Send a message to all users
	 * @param newMessage
	 */
	public void sendToAll(String newMessage){	
		for(Chatter c : chatters){
			try {
				c.getClient().messageFromServer(newMessage);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * remove a client from the list, notify everyone
	 */
	@Override
	public void leaveChat(String userName) throws RemoteException{		
		for(Chatter c : chatters){
			if(c.getName().equals(userName)){
				chatters.remove(c);
				System.out.println(line + userName + " left the chat session");
				System.out.println(new Date(System.currentTimeMillis()) + "\n");
				break;
			}
		}
		if(!chatters.isEmpty()){
			updateUserList();
		}			
	}
	

	/**
	 * A method to send a private message to selected clients
	 * The integer array holds the indexes (from the chatters vector) 
	 * of the clients to send the message to
	 */
	@Override
	public void sendPM(int[] privateGroup, String privateMessage) throws RemoteException{
		Chatter pc;
		for(int i : privateGroup){
			pc = chatters.elementAt(i);
			pc.getClient().messageFromServer(privateMessage);
		}
	}
	
	public void recvSum(String userName, int randomNum, int currSum) {
		ChatClient3IF client = null;
		
		if(userName.length() == 0) {
			client = chatters.elementAt(0).getClient();
		}
		else if(userName.equals(chatters.elementAt(chatters.size() - 1).getName())){
			System.out.println(userName + ": " + randomNum);
			System.out.println("The sum of all connected nodes: " + currSum);
			return;
		}
		else {
			System.out.println(userName + ": " + randomNum);
			for(int i = 0; i < chatters.size(); i++){
				if(chatters.elementAt(i).getName().equals(userName)){
					client = chatters.elementAt(i + 1).getClient();
					break;
				}
			}
		}

		try {
			client.computeSum(currSum);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
}//END CLASS
