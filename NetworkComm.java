import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.lang.String;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class NetworkComm {
   DatagramSocket send;
   DatagramSocket receive;
   DatagramPacket p;
   DatagramPacket q;
   DatagramPacket f;
   DatagramPacket img;
   byte[] rxbuffer;
   int clientPort;
   int serverPort;
   String packetInfoecho;
   String packetInfoimage;
   String packetInfoaudio;

 //  String packetInfoTemp;
   byte[] txbuffer;
   byte[] hostIP = { (byte)155,(byte)207,18,(byte)208 };
   InetAddress hostAddress;
   
	public NetworkComm(int clientport,int serverport) throws SocketException, UnknownHostException {
	//Constructor initializing Network connection
	clientPort=clientport;
	serverPort=serverport;
	receive = new DatagramSocket(clientPort);
	send = new DatagramSocket();
	hostAddress = InetAddress.getByAddress(hostIP);

	
	receive.setSoTimeout(4000);
	rxbuffer = new byte[2048];	
	q = new DatagramPacket(rxbuffer,rxbuffer.length);
	

	
	}
//-------------------------------------------------------------------	
	public static void main(String[] args) throws java.lang.Exception{
		String s=null;
		int min;
		int camera;
		int packages;
		BufferedReader readconsole = new BufferedReader(new InputStreamReader(System.in));
		int clientport, serverport;
		int answer;

try{        
		
            System.out.println("Type client port");
            s=readconsole.readLine();
           clientport=Integer.parseInt(s);
            
            System.out.println("Type server port");
            s=readconsole.readLine();
            serverport=Integer.parseInt(s);
            
            NetworkComm nC = new NetworkComm(clientport,serverport);
            
  //while (true){
	        System.out.println("Press: '1' for echo request / '2' for Echo without deleay request / '3' for temperatures request / '4' for image request / '5' for Music request / '6' to exit ");
	        s = readconsole.readLine();  
	        answer=Integer.parseInt( s );
	        switch( answer ){
	        case 1:{
	        	System.out.println("Type Echo request code");
	            s=readconsole.readLine();
	            nC.packetInfoecho=s;
	            System.out.println("Give runtime (minutes)");
	            s=readconsole.readLine();
	            min=Integer.parseInt(s);
	        	nC.ECHO(min);
	        	break;}
	        case 2:{
	        	System.out.println("Type Echo request code");
	            s=readconsole.readLine();
	            nC.packetInfoecho=s;
	            System.out.println("Give runtime (minutes)");
	            s=readconsole.readLine();
	            min=Integer.parseInt(s);
	        	nC.ECHO_noDelay(min);
	        	break;}
	        case 3:{
	        	System.out.println("Type Echo request code");
	            s=readconsole.readLine();
	            nC.packetInfoecho=s;
	        	nC.getTemp();
	        	break;}
	        case 4:{
	        	System.out.println("Type Image request code");
	            s=readconsole.readLine();
	            nC.packetInfoimage=s;
	        	System.out.println("Select camera:");     	
	        	s=readconsole.readLine();
	            camera=Integer.parseInt(s);	        	
	        	nC.image(camera);
	        	break;}
	        case 5:{
	        	System.out.println("Type Sound request code");
	            s=readconsole.readLine();
	            nC.packetInfoaudio=s;
	        	System.out.println("Number of Packages? (100<=packages<=999:");
	        	s=readconsole.readLine();
	        	packages=Integer.parseInt(s);	 
	        	System.out.println("Select Modulation. Type 'DPCM' or 'AQDPCM'");
	        	s=readconsole.readLine();
	        	nC.Audio(packages, s);
	        	break;}
	        case 6:{ System.exit(0);}
	        answer=0;
	        default:
                System.out.println( "Try again" );
	       }
	        System.out.println();
	        System.out.println("KALO KALOKAIRI!!!! =]");      
}
   catch (Exception x) {
	System.out.println(x);	
	}

	  }
	
//----------------------------------------------------------------------	
	public void ECHO(int min){
		
		txbuffer = packetInfoecho.getBytes();		
		p = new DatagramPacket(txbuffer,txbuffer.length, hostAddress,serverPort);
		
		long start,i,timereceived,session=32000;
		int numofpack=0,sessioncount=1,secs=1,sum=0,timeslots=32;
		int [] packpersec=new int [4000];
		int  throughput= 0; 
		long runtime=min*60*1000;
		long []trans = new long[ 400000 ];
		int thr=0;
		int count=0;
		PrintStream trp = null;
		PrintStream transmission = null;
		PrintStream timepassed = null;
		
		try {
			  FileWriter echofile = new FileWriter("echofile.txt");
	          BufferedWriter output = new BufferedWriter(echofile);
	              
	          trp= new PrintStream(new FileOutputStream("throughput.txt"));
	          transmission = new PrintStream(new FileOutputStream("transmission.txt"));  
	          timepassed = new PrintStream(new FileOutputStream("time.txt"));
	    
		send.send(p);
		
		start=System.currentTimeMillis();
		i=start;
		while (i-start<=runtime) {	
			try{
			receive.receive(q);
			timereceived=System.currentTimeMillis();
			numofpack=numofpack+1;
			
			trans[count++]=timereceived-i;
			String message = new String(rxbuffer,0,q.getLength());
			System.out.println(message);
			output.write(message,18,8);
            output.newLine();
            transmission.println(timereceived-i);
            timepassed.println((timereceived-start));
                   
           //calculations for throughput are not the best because i had no time to work with Threads
            if (timereceived-start>=secs*1000){
            	if (timereceived-start<(secs+1)*1000/2){ 
            	    packpersec[secs]=numofpack;
            	    System.out.println(numofpack);
            	}
            	else{
            		packpersec[secs]=0;
            	    secs+=1;
            	    packpersec[secs]=numofpack;}
                // calculate throughput
                if ( secs>=timeslots){
            	
            	    for (int j=1;j<=timeslots;j++){
            	       sum=sum+packpersec[secs-timeslots+j];
            	    }
                //      System.out.println(sum);
            	    thr= sum*q.getLength()*8/timeslots;
                    System.out.println(thr);
                    trp.println(thr);
            
                   // sessioncount+=1;
                    sum=0;
                }	
            	
           // 	System.out.println(q.getLength());
            	secs+=1;
            	numofpack=0;
            }
			}
            catch( Exception e ){
            	System.out.println("Failed to fetch data package after 4sec");
            }
            send.send(p);
            i=System.currentTimeMillis();
          }
		
		output.close();

	}
	catch (Exception x) {
			System.out.println(x);
			
			}
		
    }
//-----------------------------------------------------------------
public void ECHO_noDelay(int min){
	  	
	    String packetInfo1;
		long start,i,trans,timereceived,session=32000;
		int numofpack=0,sessioncount=1,secs=1,sum=0,timeslots=32;
		int [] packpersec=new int [4000];
		int [] throughput= new int [4000]; 
		long runtime=min*60*1000;
		
		PrintStream trp = null;
		PrintStream transmission = null;
		PrintStream timepassed = null;
		
		try {
			  FileWriter echofile = new FileWriter("echofile2.txt");
	          BufferedWriter output = new BufferedWriter(echofile);
	        
	          trp= new PrintStream(new FileOutputStream("thoughput2.txt"));
	          transmission = new PrintStream(new FileOutputStream("transmission2.txt"));  
	          timepassed = new PrintStream(new FileOutputStream("time2.txt")); 
	          
	          txbuffer = packetInfoecho.getBytes();
	          p = new DatagramPacket(txbuffer,txbuffer.length, hostAddress,serverPort); 
	          
		send.send(p);
		
		start=System.currentTimeMillis();
		i=start;
		packetInfo1 = "E0000";
		f = new DatagramPacket(packetInfo1.getBytes(),packetInfo1.getBytes().length, hostAddress,serverPort);
		
		while (i-start<=runtime) {
		try{	
			receive.receive(q);
			
			timereceived=System.currentTimeMillis();
			numofpack=numofpack+1;
			
			trans=timereceived-i;
			String message = new String(rxbuffer,0,q.getLength());

			output.write(message,18,8);
            output.newLine();
            transmission.println(trans);
            System.out.println(trans);
            timepassed.println((timereceived-start));
            
            // save the number of packages received per sec
            if (timereceived-start>secs*1000){
            	packpersec[secs]=numofpack;
           // 	System.out.println(numofpack);
            	
                // calculate throughput
                if ( secs>=timeslots){
            	
            	    for (int j=1;j<=timeslots;j++){
            	       sum=sum+packpersec[secs-timeslots+j];
            	    }
                //      System.out.println(sum);
                    throughput[sessioncount]= sum*q.getLength()*8/timeslots;
                 //   System.out.println(throughput[sessioncount]);
                    trp.println(throughput[sessioncount]);
            
                    sessioncount+=1;
                    sum=0;
                }	
            	
           // 	System.out.println(q.getLength());
            	secs+=1;
            	numofpack=0;
            }	
                       
            
          //  Thread.sleep(500);
		}
		catch( Exception e ){
			System.out.println("Failed to fetch data package after 4sec");
        }
            send.send(f);			
			i=System.currentTimeMillis();
		}
		
		output.close();

	}		
	catch (Exception x) {
			System.out.println(x);}
		
}
//----------------------------------------------------------------------------	
public void image(int camera) {	
	PrintStream outa = null;
	String packetInfoimage1;
	//int camera=1;
	
	packetInfoimage1 =packetInfoimage+"CAM="+camera+"FLOW=ON";	
	txbuffer = packetInfoimage.getBytes();
	img = new DatagramPacket(txbuffer,txbuffer.length, hostAddress,serverPort);
	
	try {
		OutputStream image = new FileOutputStream("image1.jpg");
       
	
	 send.send(img);
	 img = new DatagramPacket("NEXT".getBytes(),"NEXT".length(), hostAddress,serverPort); 
	 
	 for (;;){
		 try{
	 receive.receive(q);
	// String message = new String(rxbuffer,0,q.getLength());
	 	 
	 byte[] imageArray = Arrays.copyOfRange(q.getData(),0,q.getLength());
	 image.write(imageArray);
	 System.out.println(imageArray);
	 	 	 
	 Thread.sleep(400);
		 }
		 catch (Exception e) {
				System.out.println(e);
				break;
				} 
	 send.send(img);			 
		  
	    }
	}
	 catch (Exception x) {
		System.out.println(x);
		} 
		
}
//------------------------------------------------------------------------
public void getTemp() {
	String packetInfo1;

try{		
	
	FileWriter temperature = new FileWriter("temperature.txt");
    BufferedWriter output = new BufferedWriter(temperature);   
	
	for (int i=1;i<8;++i){		
	 	
		packetInfo1=packetInfoecho+"T0"+i;
		txbuffer = packetInfo1.getBytes();
		f = new DatagramPacket(txbuffer,txbuffer.length, hostAddress,serverPort);
		send.send(f);	
		
		receive.receive(q);
		String message = new String(rxbuffer,0,q.getLength());
		System.out.println(message);
		if (message.length()>32){
		output.write(message,27,19);
		output.newLine();
		}
		//Thread.sleep(500);
	 }	
	output.close();
	
    }
catch (Exception x) {
	System.out.println(x);
	} 

}
//--------------------------------------------------------------------------------------
public void Audio( int packages, String modulation) {

	int Q=16;
	try{		
		AudioFormat linearPCM = new AudioFormat(8000,Q,1,true,false);
		//linearPCM einai antikeimeno morfopoihshs hxou
		
		SourceDataLine lineOut = AudioSystem.getSourceDataLine(linearPCM);
		//lineout einai i eksodos hxou
		
		//energopoihsh eksodou
		//audiobuffer size = 32000
		lineOut.open(linearPCM,32000);
		

		lineOut.start();
		if (modulation.contains("AQ")){
		   lineOut.write(this.getaqdpcm(packages),0,this.getaqdpcm(packages).length);
		}
		else{
			
		   lineOut.write(this.getdpcm(packages),0,this.getdpcm(packages).length);
		}
		
		lineOut.stop();
		lineOut.close();
	}	
			
	catch (Exception x) {
		System.out.println(x);
		} 
	
	
}


//-------------------------------------------------------------------------------
public byte[] getdpcm(int packages){
	String packetInfo1;
	
	int SP=packages;
    byte[] buff = new byte[ 128 * 2 ];
    byte[] audio = new byte[ SP * 2 * 128 ];
    int counter = 0;
    int nibble = 0;
    int pack=0;
    int song=1;
    
    packetInfo1 = packetInfoaudio+"F"+SP;		
	txbuffer = packetInfo1.getBytes();
	f = new DatagramPacket(txbuffer,txbuffer.length, hostAddress,serverPort);
    
    try{
        FileWriter fw = new FileWriter( "DPCMT" + ".txt" );
        BufferedWriter bw = new BufferedWriter( fw );
        send.send( f );
        
        for( int j = 0; j < SP; ++j ){
            try{
                receive.receive( q );
               pack++;
                buff = q.getData();
                System.out.println(buff);
                for( int i = 0; i < 128; ++i ){
                    int X1 = ( buff[ i ] >> 4 ) & 0x0f;
                    int X2 = buff[ i ] & 0x0f;
                     
                    X1 = X1 - 8;
                    X2 = X2 - 8;
                    
                    X1 += nibble;
                    if( X1 > 127 ){
                        X1 = 127;
                    }
                    if( X1 < -128 ){
                        X1 = -128;
                    }
                    X2 += X1;
                    if( X2 > 127 ){
                        X2 = 127;
                    }
                    if( X2 < -128 ){
                        X2 = -128;
                    }
                    nibble = X2;
                    
                    byte x1 = ( byte ) X1;
                    byte x2 = ( byte ) X2;
                    
                    audio[ counter++ ] = x1;
                    audio[ counter++ ] = x2;
                    
                    bw.write( X1 + " " );
                    bw.write( X2 + " " );
                }
            }
            catch( Exception x ){
                break;
            }
        }
        bw.close();
   //     System.out.println(pack);
    }
    catch( Exception x ){
        System.out.println(x );
    }
    return audio;
}
public byte[] getaqdpcm(int packages){
	
	String packetInfo1;
	int SP=packages;
    byte[] buff = new byte[ 132 * 2 ];
    byte[] audio = new byte[ SP * 4 * 128 ];
    int counter = 0;
    int nibble = 0;
    int song=1;

    packetInfo1 = packetInfoaudio+"F"+SP;	
	txbuffer = packetInfo1.getBytes();
	f = new DatagramPacket(txbuffer,txbuffer.length, hostAddress,serverPort);
    
    try{
        FileWriter fw1 = new FileWriter( "AQDPCM28" + ".txt" );
        BufferedWriter log = new BufferedWriter( fw1 );
        FileWriter fw = new FileWriter( "AQDPCMmusic28" + ".txt" );
        BufferedWriter bw = new BufferedWriter( fw );
        send.send( f );
        
        for( int j = 0; j < SP; ++j ){
            try{
                receive.receive( q );
                buff = q.getData();
                System.out.println(buff);
                byte[] bb = new byte[ 4 ];
                byte sign = (byte)( ( buff[ 1 ] & 0x80 ) != 0 ? 0xff : 0x00 );
                bb[ 3 ] = sign;
                bb[ 2 ] = sign;
                bb[ 1 ] = buff[ 1 ];
                bb[ 0 ] = buff[ 0 ];

                int m = ByteBuffer.wrap( bb ).order( ByteOrder.LITTLE_ENDIAN ).getInt();
                

                sign = (byte)( ( buff[ 3 ] & 0x80 ) != 0 ? 0xff : 0x00 );
                bb[ 3 ] = sign;
                bb[ 2 ] = sign;
                bb[ 1 ] = buff[ 3 ];
                bb[ 0 ] = buff[ 2 ];

                int b = ByteBuffer.wrap( bb ).order( ByteOrder.LITTLE_ENDIAN ).getInt();
                log.write( m + " " + b + "\n" );
                for( int i = 4; i < 132; ++i ){
                    int D1 = ( buff[ i ] >>> 4 ) & 0x0f;
                    int D2 = buff[ i ] & 0x0f;
                    
                    int d1 = D1 - 8;
                    int d2 = D2 - 8;
                    
                    int delta1 = d1 * b;
                    int delta2 = d2 * b;
                    
                    int X1 = delta1 + nibble;
                    int X2 = delta2 + delta1;
                    nibble = delta2;

                    int x1 = X1 + m;
                    int x2 = X2 + m;
                    
                    audio[ counter++ ] = ( byte ) ( x1 );
                    audio[ counter++ ] = ( byte ) ( x1 / 256 > 127 ? 127 : x1 / 256 < -128 ? -128 : x1 / 256 );
                    audio[ counter++ ] = ( byte ) ( x2 );
                    audio[ counter++ ] = ( byte ) ( x2 / 256 > 127 ? 127 : x2 / 256 < -128 ? -128 : x2 / 256 );
                    
                    bw.write( x1 + " " );
                    bw.write( x2 + " " );
                }
                
            }
            catch( Exception x ){
                break;
            }
        }
        bw.close();
        log.close();
    }
    catch( Exception x ){
        System.out.println( x );
    }
    return audio;
}
//---------------------------------------------------------------------------

}	
