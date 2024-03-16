package server;

import client.ChatClient3IF;


/**
 * A class used by the server program to keep
 * details of connected clients ordered
 * @authors			(1001)Team8
 * RMI Project		May 2022
 *
 */
public class Chatter {

	public String name;
	public ChatClient3IF client;
	
	//constructor
	public Chatter(String name, ChatClient3IF client){
		this.name = name;
		this.client = client;
	}

	
	//getters and setters
	public String getName(){
		return name;
	}
	public ChatClient3IF getClient(){
		return client;
	}
	
}
