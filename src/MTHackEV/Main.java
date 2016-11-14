import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.port.SensorPort;
import lejos.utility.Delay;


public class Main{
	
	static EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S1);
	
	private static DataExchange DE;
	private static Motors MObj;
	private static Sensors SObj;
	
	static private void WriteToLCD(String text){
		
		LCD.clear();
		LCD.drawString(text, 0, 7);
		LCD.drawInt((int)(100*DE.getColor()),0,5);
		LCD.refresh();
		
	}
	
	public static void main(String[] args) {
		
		DE = new DataExchange();
		Delay.msDelay(200);
		SObj = new Sensors(DE);
		Delay.msDelay(200);
		MObj = new Motors(DE);
				
		SensorMode touch = touchSensor.getMode(0);
		float tValue[] = new float[touch.sampleSize()];
		
		SObj.start();
		MObj.MotorInit();
		
		Delay.msDelay(2000);
		
		
		//following is for the calibration
		while(tValue[0]!=1){
			WriteToLCD("set white");
			touch.fetchSample(tValue, 0);
		}
		MObj.SetWhite();
		Delay.msDelay(500);
		tValue[0]=0;
		while(tValue[0]!=1){
			WriteToLCD("set black");
			touch.fetchSample(tValue, 0);
		}
		MObj.SetBlack();
		Delay.msDelay(500);
		

		//waits for touch to begin
		LCD.clear();
		LCD.drawString("Press to start", 0, 7);
		LCD.refresh();
		tValue[0]=0;
		while(tValue[0]!=1){
			touch.fetchSample(tValue, 0);
		}
		
		LCD.drawString("Started  ", 0, 7);
		LCD.refresh();
		
		Delay.msDelay(500);
		
		
		MObj.start();

		LCD.clear();
		
		//will print information to the screen and if touch is presesd, loop will end and everything will stop
		tValue[0]=0;
		while(tValue[0]!=1){
			touch.fetchSample(tValue, 0);
			LCD.drawString("value: " + DE.getColor(), 1, 1); //value used by motors is value * 100
			LCD.drawString("distance" + DE.GetDistance(), 1, 2);
			//LCD.drawString("rate = " + DE.GetRate(), 1, 3);
			LCD.drawString("time = " + DE.GetTime(), 1, 3);
			
			LCD.drawString("Red = " + DE.GetRed(), 1, 4);
			LCD.drawString("Green = " + DE.GetGreen(), 1, 5);
			LCD.drawString("Blue = " + DE.GetBlue(), 1, 6);


			
			Delay.msDelay(10);
			LCD.refresh();
		}
		
		//finishing sequence
		LCD.drawString("Finished", 0, 7);
		LCD.refresh();
		DE.setStop(true);
		Delay.msDelay(2000);
		
	}
}
