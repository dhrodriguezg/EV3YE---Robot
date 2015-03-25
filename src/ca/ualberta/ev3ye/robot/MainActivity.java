package ca.ualberta.ev3ye.robot;
import java.io.*;

import lejos.hardware.Bluetooth;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTMotor;
import lejos.hardware.port.MotorPort;
import lejos.remote.nxt.*;
import lejos.robotics.EncoderMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class MainActivity {
	
	DifferentialPilot pilot;

	public static DataOutputStream dataOut;
	public static DataInputStream dataIn;
	//public static USBConnection USBLink;
	private static NXTConnection BTLink;
	public static int speed = 50, turnSpeed = 50, speedBuffer, speedControl;
	public static int commandValue, transmitReceived;
	public static boolean[] control = new boolean[6];
	public static boolean[] command = new boolean[6];

	public static void main(String[] args) {
		MainActivity traveler = new MainActivity();
		traveler.connect();
		traveler.transferData();
		//traveler.moveBrick();
		
	}// End main

	public void connect() {
		System.out.println("Listening");
		BTLink = Bluetooth.getNXTCommConnector().waitForConnection(10000, NXTConnection.RAW);
		//dataIn = BTLink.openDataInputStream();
		System.out.println("Connected :')");
	}
	
	public void transferData(){
		
		DataInputStream dis=BTLink.openDataInputStream();
		String word = "NOPE";
		try {
			word=dis.readUTF();
			System.out.println("Word: "+word);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Exit...");
	}
	
	
	
	@SuppressWarnings("resource")
	public void moveBrick(){
		int powerA = 0;
		int powerB = 0;
		int addedPowerA=0;
		int addedPowerB=0;
		
		EncoderMotor motorA =new NXTMotor (MotorPort.A);
		EncoderMotor motorB =new NXTMotor (MotorPort.B);
		motorA.resetTachoCount();
		motorB.resetTachoCount();
		
		while (!Button.DOWN.isDown()) {

			try {
				
				
				int data = dataIn.readInt();
				
				if (data==10){ //forward pressed
					powerA += 75;
					powerB += 75;
				}
				if (data==-10){ //forward released
					powerA -= 75;
					powerB -= 75;
				}
				if (data==20){ //backward pressed
					powerA -= 75;
					powerB -= 75;
				}
				if (data==-20){ //backward released
					powerA += 75;
					powerB += 75;
				}
				if (data==30){ //left pressed
					addedPowerA-=30;
					addedPowerB+=30;
				}
				if (data==-30){ //left released
					addedPowerA+=30;
					addedPowerB-=30;
				}
				if (data==40){ //right pressed
					addedPowerA+=30;
					addedPowerB-=30;
				}
				if (data==-40){ //right released
					addedPowerA-=30;
					addedPowerB+=30;
				}
				
				int totalA=powerA+addedPowerA;
				int totalB=powerB+addedPowerB;
				
				if (data==50){ //right released
					totalA = 0;
					totalB = 0;
					
					powerA = 0;
					powerB = 0;
					
					addedPowerA = 0;
					addedPowerB = 0;
				}
				
				if (totalA == 0) {
					motorA.stop();
				}
				else if(totalA<0){
					motorA.setPower(-totalA);
					motorA.backward();
				}else{
					motorA.setPower(totalA);
					motorA.forward();
				}
				
				if (totalB == 0) {
					motorB.stop();
				}
				else if(totalB<0){
					motorB.setPower(-totalB);
					motorB.backward();
				}else{
					motorB.setPower(totalB);
					motorB.forward();
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
	}

}// NXTtr Class

