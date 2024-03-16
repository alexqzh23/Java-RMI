package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for client classes
 * A method to receive a string
 * A method to update changes to user list
 * 
 * @authors			(1001)Team8
 * RMI Project		May 2022
 *
 */
public interface ChatClient3IF extends Remote{

	public void messageFromServer(String message) throws RemoteException;

	public void updateUserListUI(String[] currentUsers) throws RemoteException;
	
	public int computeSum(int currSum) throws RemoteException;
}
/**
 * 
 * 
 * 
 *
 */
