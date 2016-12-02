import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.utility.Delay;

// yks tapa, forward menee eteepäi ja turn vaikuttaa forwardii vaa
// kattoo arvon mukaa ja nopeuttaa vauhtia jos arvo pysyy tasasesti alhasena

public class Motors extends Thread{
	
	double white = 0.7, black = 0.06;	
	
	private DataExchange DEObj;
	TachoMotorPort leftMotor = MotorPort.C.open(TachoMotorPort.class);
	TachoMotorPort rightMotor = MotorPort.B.open(TachoMotorPort.class);
	
	TachoMotorPort middleMotor = MotorPort.A.open(TachoMotorPort.class);
		
	public Motors(DataExchange DE){
		DEObj = DE;
	}
	
	public void SetBlack(){
		black = DEObj.getColor();
	}
	
	public void SetWhite(){
		white = DEObj.getColor();
		}
	
	// unused
	public void Turn(int degrees){
		
		if(degrees>0){
			Forward(50,0);
		}
		else{
			Forward(0,50);
		}
		
		Delay.msDelay(degrees);
	}
	
	public String GetColor(){
		
		float[] RGB = new float[3]; //Red = 0, Green = 1, Blue = 2
		
		//get each color
		RGB[0] = DEObj.GetRed();
		RGB[1] = DEObj.GetGreen();
		RGB[2] = DEObj.GetBlue();
		
		if((RGB[0] > 5 || RGB[0] < 0.02) && (RGB[1] > 5 || RGB[1] < 0.02) && (RGB[2] > 5 || RGB[2] < 0.02)){
			return "black";
		}
		else if(RGB[0]*0.9 > RGB[1] && RGB[1] > RGB[2]*2){
			return "yellow";
		}
		else if(RGB[0]*0.77 > RGB[1] && RGB[0]*0.77 > RGB[2]){ //if red color is the biggest
			return "red";
		}
		else if(RGB[1]*0.8 > RGB[0] && RGB[1]*0.8 > RGB[2]){ //if green color is the biggest
			return "green";
		}
		else if(RGB[2]*0.95 > RGB[0] && RGB[2]*0.95 > RGB[1]){ //if blue is biggest
			return "blue";
		}
		
		return "none";
		
	}

	public void MotorInit(){
		
		rightMotor.controlMotor(0, 0);
		rightMotor.resetTachoCount();
		
		leftMotor.controlMotor(0, 0); 
		leftMotor.resetTachoCount();
		
		middleMotor.controlMotor(0, 0);
		middleMotor.resetTachoCount();
				
	}
	
	private void Forward(int left, int right){
		rightMotor.controlMotor(right, 1);
		leftMotor.controlMotor(left, 1);
	}
	
	private String CheckColor(){
		
		int left=0,right=0;
		
		Forward(left,right);		
		middleMotor.controlMotor(30,1);
		Delay.msDelay(700);
		middleMotor.controlMotor(0,1);
		Delay.msDelay(250);
		String color = GetColor();
		Delay.msDelay(250);
		middleMotor.controlMotor(40, 2);
		Delay.msDelay(500);
		middleMotor.controlMotor(0,0);
		
		return color;
	}
	
	
	public void run(){
		// must 0.06 , valk = 0.6
		
		double correction=0,value = 0,kp = 1.1; //kp affects the turning when following the line
		double midpoint = (white - black ) / 2 + black;
		DEObj.SetMiddleColor(midpoint);
		int right=0,left=0,forward=38, turn=0, stage=1, rightTurns = 0, straight = 0, time=0;
		boolean newBlock = true;
		String newBlockColor;
		
		DEObj.ResetTime(); //to make sure that there is no time counted
		
		while(true){
			
			value = DEObj.getColor(); //get the color value
			
			switch(stage){
			
				//will follow the line and continue trying to find it unless little while has passed on white.
				case 1:
					//this moves code to stage 2 if only white is detected for a while
					if(DEObj.GetTime() > 4800 && DEObj.GetFollow()){ 
						stage=2;
						break;
					}
					
					//calculations for the turn is calculated here
					correction = kp * ( midpoint - value);
					turn = (int)(correction*100);
					if(turn < -5){
						turn = -5;
					}
					left = forward - turn;
					right = forward + turn;
					break;
					
					// will search for non-white line
				case 2:
					//if no longer on only white, will go to stage 3
					if(value < (DEObj.GetMiddle() * 1.2)){
						stage = 3;
						break;
					}
					
					//calculations for the turn is calculated here
					turn = 10;
					left = forward + turn;
					right = forward + (turn);				
					break;
					
					// stops and turns left in order go to the right direction
				case 3:
					
					left = -20;
					right = 45;
					time++;
					
					if(value < DEObj.GetMiddle() * 1.1 && time > 300){
						//left = 0;
						//right = -30;
						Forward(0,-30);
						Delay.msDelay(100);
						time = 0;
						stage = 4;
					}
					break;
				
				//will try to find the colors. Goes from red to blue
				case 4:
					String color = GetColor();
					forward=34;
					
					if(color=="red"){
						
						if(rightTurns==0){	
							
							while(true){
								
								if(DEObj.getStop()){
									break;
								}
								
								left = 50;
								right = 20;
								Forward(left,right);
								value = DEObj.getColor();
								color = GetColor();
								
								if(value < (DEObj.GetMiddle() * 1.1) && color != "red"){
									rightTurns++;
									
									left = -30;
									right = 0;
									Forward(left,right);
									Delay.msDelay(450);									
									break;
								}
								Delay.msDelay(100);
							}
						}
						else{
							left = 47;
							right = 50;
							Forward(left,right);
							Delay.msDelay(700);
							straight++;
						}
					}
					else if(rightTurns>0 && color == "blue"){
						
						if(straight >= 2){
							stage = 5;
							break;
						}
						else{
							left = 47;
							right = 50;
							Forward(left,right);
							Delay.msDelay(700);
							straight++;
						}
					}
					if( !(DEObj.GetDistance() > 0.08) && newBlock==true){ //Kato että ultrasonikki alustuu oikein
						
						newBlockColor = CheckColor();
						LCD.drawString("block= " + newBlockColor, 1, 4);
						
						if(newBlockColor=="black"){
							//will circle around the block
							
							//this sequence will circle around the block
							Forward(-50,-20);
							Delay.msDelay(700);
							Forward(40,40);
							Delay.msDelay(1500);
							Forward(50,20);
							Delay.msDelay(1000);
							
							Forward(45,50);
							Delay.msDelay(1500);
							straight++;
							
						}
						else{
							newBlock=false;
						}
					}
					
					//calculations for the turn is calculated here
					correction = (kp-0.1) * ( midpoint - value);
					turn = (int)(correction*100);
					if(DEObj.GetFollow()){
						left = (forward - turn);
						right = (forward + turn);
					}
					else{
						left = (forward + turn);
						right = (forward - turn);
					}
					
					break;
					
				case 5:
					forward=38;
					left=40;
					right=35;
					
					time++;
					if(value < DEObj.GetMiddle() * 1.1 && time > 1500){

						forward = 45;
						midpoint = midpoint + 0.105;
						
						left = 0;
						right = -30;
						Forward(left,right);
						Delay.msDelay(320);	
						
						stage = 6;
						break;
					}
					
					break;
					
				case 6:
					
					correction = kp * ( midpoint - value);
					turn = (int)(correction*100);
					//if(turn > 25){
					//	turn = 25;
					//}
					left = forward - turn;
					right = forward + turn;
					break;
			}
			
			//makes the robot move
			Forward(left,right);
			
			//if button is pressed, this will stop the loop.
			if(DEObj.getStop()){
				left=0;
				right=0;
				Forward(left,right);
				break;
			}
			
		}
		
	}

}
