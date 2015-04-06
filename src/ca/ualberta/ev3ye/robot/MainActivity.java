package ca.ualberta.ev3ye.robot;
import java.io.*;

import lejos.hardware.Audio;
import lejos.hardware.Bluetooth;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.NXTMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.remote.nxt.*;

public class MainActivity { 

	public final static int CONTROLLER_OPERATION = 1; //Using gamepad
	public final static int TOUCH_OPERATION = 2; //Using touchscreen
	public final static int MOTION_OPERATION = 3; //Using acelerometers
	public final static int VISUAL_OPERATION = 4; //Using visual servoig
	public final static int EXIT = -1; //Close communication
	
	private int maxTries = 1;
	private boolean forceExit = false;
	
	public final static Port motorL = MotorPort.D;
	public final static Port motorR = MotorPort.A;
	
	private EV3MediumRegulatedMotor reMotorL = null;
	private EV3MediumRegulatedMotor reMotorR = null;
	
	private NXTMotor enMotorL = null;
	private NXTMotor enMotorR = null;
	
	private Audio audio = null;

	public static void main(String[] args) {
		MainActivity robot = new MainActivity();
		robot.waitForConnections();
		robot.waitExit();
	}
	
	
	private void waitForConnections(){
		audio = LocalEV3.get().getAudio();
		System.out.println("EV3YE Waiting...");
		
		for (int i=0;i<maxTries;i++){
    		audio.systemSound(Audio.ASCENDING);
    		NXTConnection btLink = Bluetooth.getNXTCommConnector().waitForConnection(10000, NXTConnection.RAW);
    		try {
    			System.out.println("Connection finished...");
    			manageConnection(btLink);		//error code is 104...uncathed
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		closeEverything(btLink);
    		if(forceExit)
    			break;
		}
	}
	
	private void manageConnection(NXTConnection btLink) throws IOException, InterruptedException{
		boolean greeting = true;
		boolean isTransmiting = true;
		
		DataInputStream dataIn=btLink.openDataInputStream();
		DataOutputStream dataOut=btLink.openDataOutputStream();
		
		while(isTransmiting){
			while(dataIn.available()==0){ //In case of delays...
				Thread.sleep(1);
			}
				
			String commands=dataIn.readUTF();
			String[] command=commands.split(";");
			
			if(greeting && command[0].equals("Are you Robot?")){
				audio.systemSound(Audio.DOUBLE_BEEP);
				dataOut.writeBoolean(true);
				dataOut.flush();
				greeting=false;
				continue;
			}
			int operation = Integer.parseInt(command[0]);
			if (operation==EXIT){
				isTransmiting=false;
			}else if(operation==VISUAL_OPERATION){
				turnMotors();
			}else{
				if(command.length>2)
				moveMotors(Integer.parseInt(command[1]),Integer.parseInt(command[2]));
			}
			dataOut.writeBoolean(true);
			dataOut.flush();
			Thread.sleep(5);
			
			if(forceExit)
				break;
		}
		dataIn.close();
		dataOut.close();
		
	}
	
	private void moveMotors(int powerL, int powerR){
		
		if(reMotorL!=null){
			reMotorL.close();
			reMotorL=null;
		}
		if(reMotorR!=null){
			reMotorR.close();
			reMotorR=null;
		}
		if(enMotorL==null){
			enMotorL = new NXTMotor(motorL);
			enMotorL.resetTachoCount();
		}
		if(enMotorR==null){
			enMotorR = new NXTMotor(motorR);
			enMotorR.resetTachoCount();
		}
		
		enMotorL.setPower(powerL);
		enMotorR.setPower(powerR);
		enMotorL.forward();
		enMotorR.forward();
	}
	
	private void turnMotors(){
		if(enMotorL!=null){
			enMotorL.close();
			enMotorL=null;
		}
		if(enMotorR!=null){
			enMotorR.close();
			enMotorR=null;
		}
		if(reMotorL==null){
			reMotorL = new EV3MediumRegulatedMotor(motorL);
			reMotorL.resetTachoCount();
		}
		if(reMotorR==null){
			reMotorR = new EV3MediumRegulatedMotor(motorR);
			reMotorR.resetTachoCount();
		}
		//TODO visual servoing here :S
	}
	
	
	
	private void closeEverything(NXTConnection btLink){
		System.out.println("closing everything...");
		if(enMotorL!=null){
			enMotorL.setPower(0);
			enMotorL.close();
			enMotorL=null;
		}
		if(enMotorR!=null){
			enMotorR.setPower(0);
			enMotorR.close();
			enMotorR=null;
		}
		if(reMotorL!=null){
			reMotorL.rotateTo(0);
			reMotorL.close();
			reMotorL=null;
		}
		if(reMotorR!=null){
			reMotorR.rotateTo(0);
			reMotorR.close();
			reMotorR=null;
		}
		if(btLink!=null){
			try {
				btLink.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void waitExit(){
		//Button.waitForAnyPress();
		audio.systemSound(Audio.DESCENDING);
		forceExit = true;
	}
	
}