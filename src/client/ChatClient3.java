package client;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Random;
import javax.swing.JOptionPane;
import server.ChatServerIF;

/**
 * 
 * @authors			(1001)Team8
 * RMI Project		May 2022
 *
 */
public class ChatClient3 extends UnicastRemoteObject implements ChatClient3IF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7468891722773409712L;
	ClientRMIGUI chatGUI;
	private String serverHostName = null; // no need to enter PORT
	private String myHostName = getIpAddress();     // no need to enter PORT
	private String serverServiceName = "ServerListenService";
	private String clientServiceName;
	private String name;
	protected ChatServerIF serverIF;
	protected boolean connectionProblem = false;

	
	/**
	 * class constructor,
	 * note may also use an overloaded constructor with 
	 * a port no passed in argument to super
	 * @throws RemoteException
	 */
	public ChatClient3(ClientRMIGUI aChatGUI, String userName, String serverHostName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.name = userName;
		this.clientServiceName = "ClientListenService_" + userName;
		this.serverHostName = serverHostName;
	}

	
	/**
	 * Register our own listening service/interface
	 * lookup the server RMI interface, then send our details
	 * @throws RemoteException
	 */
	public void startClient() throws RemoteException {
		
		//startRMIRegistry();
		String[] details = {name, myHostName, clientServiceName};	

		try {
//			if (System.getSecurityManager() == null) {
//				System.setSecurityManager(new SecurityManager());
//			}
//			Naming.rebind("rmi://127.0.0.1:1009/serviceName", Bound instance)
			java.rmi.registry.LocateRegistry.createRegistry(1098);
			Naming.rebind("rmi://" + myHostName + "/" + clientServiceName, this);
			Registry registry = LocateRegistry.getRegistry(serverHostName);
			serverIF = (ChatServerIF)registry.lookup(serverServiceName);
		}
		catch (ConnectException e) {
			JOptionPane.showMessageDialog(
					chatGUI.frame, "The server seems to be unavailable\nPlease try later",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
		}
		catch(NotBoundException | MalformedURLException me){
			connectionProblem = true;
			me.printStackTrace();
		}
		if(!connectionProblem){
			registerWithServer(details);
			System.out.println("Client-Listen RMI Server is running...\n");
		}
	}


	/**
	 * pass our username, hostname and RMI service name to
	 * the server to register out interest in joining the chat
	 * @param details
	 */
	public void registerWithServer(String[] details) {		
		try{
			//serverIF.passIDentity(this.ref);
			serverIF.registerListener(details);			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//=====================================================================
	/**
	 * Receive a string from the server
	 * this is the clients RMI method, which will be used by the server 
	 * to send messages to us
	 */
	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println(message);
		chatGUI.textArea.append(message);
		//make the GUI display the last appended text, i.e. scroll to bottom
		chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
	}

	/**
	 * A method to update the display of users 
	 * currently connected to the server
	 */
	@Override
	public void updateUserListUI(String[] currentUsers) throws RemoteException {
		if(currentUsers.length < 2){
			chatGUI.privateMsgButton.setEnabled(false);
		}
		chatGUI.userPanel.remove(chatGUI.clientPanel);
		chatGUI.setClientPanel(currentUsers);
		chatGUI.clientPanel.repaint();
		chatGUI.clientPanel.revalidate();
	}
	
	@Override
	public int computeSum(int currSum) throws RemoteException {
		Random random = new Random();
		int randomNum = random.nextInt(10) + 1; // 1-10
		int newSum = currSum + randomNum;
		
		System.out.println("You generated a random number: " + randomNum);
		System.out.println("You computed the new sum: " + newSum + "\n");
		
		serverIF.recvSum(this.name, randomNum, newSum); // nested topology
		
		return newSum;
	}
	
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

}//end class
