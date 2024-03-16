package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;

/**
 * Server RMI interface
 * 
 * @authors			(1001)Team8
 * RMI Project		May 2022
 *
 */
public interface ChatServerIF extends Remote {
	// (1) Interface must implement class 'Remote'
	// (2) Methods in the interface must throw an exception 'RemoteException'
	public void updateChat(String userName, String chatMessage)throws RemoteException;
	
	public void passIDentity(RemoteRef ref)throws RemoteException;
	
	public void registerListener(String[] details)throws RemoteException;
	
	public void leaveChat(String userName)throws RemoteException;
	
	public void sendPM(int[] privateGroup, String privateMessage)throws RemoteException;

	public void recvSum(String name, int randomNum, int newSum)throws RemoteException;
}
