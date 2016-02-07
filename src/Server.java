/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ILLUMINATI
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Client implements Runnable, Comparable<Client>{
    String name;
    Socket connection;
    private Scanner sc;
    private PrintWriter OUT;
    
    Client(String name, Socket connection){
        this.name = name;
        this.connection = connection;
    }
    
    Client(Socket connection){
        this.name = null;
        this.connection = connection;
    }
    
    Client(String name){
        this.name = name;
    }
    
    public boolean isAlive(){
        return connection.isConnected();
//        connection.
    }
    
    @Override
    public void run() {
        try{
            try {
                sc = new Scanner(connection.getInputStream());
            } catch (IOException ex) {
                System.out.println(ex);
                connection.close();
            }
            OUT = new PrintWriter(connection.getOutputStream());

            String name = sc.nextLine();
            if(name == null){
                System.out.println("Invalid request");
                connection.close();
                return;
            }
            this.name = name;
            System.out.println("Message from " + this.name);
            int idx = Server.activeClients.indexOf(this);
            if(idx != -1){
                OUT.println("Error : handler already in use");
                OUT.flush();
                OUT.close();
                connection.close();
                return;
            } else {
                Server.activeClients.add(this);
            }
            System.out.println("activeuser = " + Server.activeClients);
            while(true){
                if(!sc.hasNext()){
                    break;
                }
                String target = sc.nextLine();
                if(target == null){
                    System.out.println("Invalid target user");
                    return;
                }
                Client targetClient = new Client(target);
                idx = Server.activeClients.indexOf(targetClient);
                if(idx == -1){
                    System.out.println("User does not exist " + targetClient.name);
                    OUT.println("Error : " + targetClient.name + " does not exist. ");
                    OUT.flush();
                    return;
                }
                System.out.println("Message to " + this.name);
                targetClient = Server.activeClients.get(idx);
                if(!targetClient.isAlive()){
                    Server.activeClients.remove(idx);
                    OUT.println("Error : " + targetClient.name + " is offline.");
                    OUT.flush();
                    return;
                }

                String msg = sc.nextLine();
                System.out.println("Message is : " + msg);
                targetClient.MSG(this.name + " : " + msg);
            }
            
        } catch (Exception e){
            System.out.println(e);
        }
    }
    private void MSG(String msg){
        OUT.println(msg);
        OUT.flush();
    }
    @Override
    public int compareTo(Client t) {
        System.out.println(this.name.compareTo(t.name));
        return this.name.compareTo(t.name);
    }
    
    @Override
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        
        if(!(o instanceof Client)){
            return false;
        }
        
        Client c = (Client) o;
        
        return c.name.equals(this.name);
    }
    
    @Override
    public String toString(){
        return this.name + " " + this.connection;
    }
}
public class Server {

    static List<Client> activeClients = Collections.synchronizedList(new ArrayList());
    public static void main(String[] args) {
        final int port = 1234;
        ServerSocket server;
        try {
            server = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("Can't start server");
            return;
        }
        System.out.println("Waiting for clients");
        
        while(true){
            Socket newConn;
            try {
                newConn = server.accept();
            } catch (IOException ex) {
                
                System.out.println(ex);
                System.out.println("Can't start connection with client");
                continue;
            }
            
            System.out.println("Connected to " + newConn.getLocalAddress() + ":" + newConn.getLocalPort());
           
            Thread newCli = new Thread(new Client(newConn));
            newCli.start();
        }
    }
}
