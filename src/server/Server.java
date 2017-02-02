/**
*Class:             Server.java
*Project:           Trial Project
*Author:            Nathaniel Charlebois
*					Jason Van Kerkhoven
*
*Date of Update:    02/02/2016
*Version:           1.0.0
*
*Purpose:           Manage the communication between two RPi and relay instructions
*
*Update Log:		v1.0.1
*						- UI hooked in
*						- some clean up + commenting
*						- socket init now done internally by constructor
*						- initConnection() using ports
*						- sendSocket and receieveSocket combined to generalSocket
*					v1.0.0
*						- Added basic functionality
*                       - Requires testing on a windows/linux machine
*
*Compiler(while in /src) : javac server/Server.java server/RPi.java ui/SimpleUI.java
*Execution : java server.Server
*/
package server;

//import external libraries
import java.net.*;
import java.util.Scanner;

//import packages
import ui.SimpleUI;


public class Server
{
	//declaring static class constants
	private final static int PACKETSIZE = 100 ;
	private final static int RPI_PORT = 3010;

	//declaring local instance variables
	private SimpleUI ui;
	private DatagramSocket generalSocket;
	private DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	private RPi rpi1;
	private RPi rpi2;


	//generic constructor
    public Server()
    {
    	//initialize things
    	ui = new SimpleUI("Simon Says");
        receivePacket = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;
        sendPacket = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);

        //get the port the server will use
    	ui.println("Beggining game setup...");
        int receivePort = ui.getPort("Please enter the receivePort");
        ui.println("Setting active port...");
        try
        {
            generalSocket = new DatagramSocket(receivePort);	//TODO socket not binding
        }
        catch(SocketException  e)
        {
        	ui.printError("Socking binding error -- terminating program");
        	ui.close();
        	System.exit(0);
            e.printStackTrace();
        }

        //connect to RPi1 and RPi2
        while(rpi1 == null)
        {
	        ui.println("Waiting for RPi1 to connect...");
	        rpi1 = initConnection();
        }
        while(rpi2 == null)
        {
	        ui.println("Waiting for RPi2 to connect...");
	        rpi2 = initConnection();
        }

        ui.println("\nSetup complete!");
    }


    //initialize a connection with a Raspberry Pi
    private RPi initConnection()
    {
       receivePacket();
	    
       //check if data from correct port
       if(receivePacket.getPort() == RPI_PORT)
       {
    	   return new RPi(receivePacket.getAddress(), receivePacket.getPort());
       }
       else
       {
    	   ui.println("Connection failed: Received packet did not originate from port <" + RPI_PORT + ">");
    	   return null;
       }
    }

   private void sendPacket(byte[] data, RPi pi)
   {
     try
     {
        sendPacket = new DatagramPacket(data, data.length, pi.ip, pi.port) ;
        generalSocket.send(sendPacket);
     }
     catch( Exception e )
     {
        System.out.println( e ) ;
     }
   }

   private void receivePacket(){
      try
      {
            ui.println("Waiting for packet on port: " + generalSocket.getPort()) ;
            generalSocket.receive(receivePacket) ;
            ui.println( receivePacket.getAddress() + " " + receivePacket.getPort() + ": " + new String(receivePacket.getData()).trim() ) ;

      }
      catch( Exception e )
      {
         System.out.println( e ) ;
      }
   }


   //release ports
   public void shutdown()
   {
        if( generalSocket != null )
        {
        	generalSocket.close();
        }
   }


   //run an instance of server
   public static void main(String[] args)
   {
       //Init server
       Server server = new Server();
   }
}