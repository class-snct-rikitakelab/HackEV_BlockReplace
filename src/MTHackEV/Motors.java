import lejos.hardware.port.MotorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.utility.Delay;

// yks tapa, forward menee eteepäi ja turn vaikuttaa forwardii vaa
// kattoo arvon mukaa ja nopeuttaa vauhtia jos arvo pysyy tasasesti alhasena

public class Motors extends Thread{
	
	double white = 0.7, black = 0.06;	//if for some reason white or black value is not set
	
	private DataExchange DEObj;
	TachoMotorPort leftMotor = MotorPort.C.open(TachoMotorPort.class);
	TachoMotorPort rightMotor = MotorPort.B.open(TachoMotorPort.class);
	
	TachoMotorPort middleMotor = MotorPort.A.open(TachoMotorPort.class);
		
	public Motors(DataExchange DE){
		DEObj = DE;
	}
	
	public void SetBlack(){ //sets the current value of color sensor as black
		black = DEObj.getColor();
	}
	
	public void SetWhite(){ //sets the current value of color sensor as white 
		GetPuzzleColors();
		white = DEObj.getColor();
		}
	
	public String GetColor(){ //return the color based on the RGB values
		
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

	public void MotorInit(){ // makes the motors ready for use
		
		rightMotor.controlMotor(0, 0);
		rightMotor.resetTachoCount();
		
		leftMotor.controlMotor(0, 0); 
		leftMotor.resetTachoCount();
		
		middleMotor.controlMotor(0, 0);
		middleMotor.resetTachoCount();
		
				
	}
	
	private void Forward(int left, int right){ //makes the robot go forward
		rightMotor.controlMotor(right, 1);
		leftMotor.controlMotor(left, 1);
	}
	
	private String CheckColor(){ //will lift the arm and check the color of the block
				
		Forward(0,0);

		//nostaa keskimmäisen 90 astetta (alk 180)
		SetMiddle(90);
		//ottaa värin
		String color = GetColor();
		//laskee keskimmäisen
		SetMiddle(180);
		
		//Forward(-30,-30);
		//Delay.msDelay(200);
		Forward(0,0);

		
		return color;
	}
	
	void SetMiddle(int degrees){ //moves the arm
		
		int degreeValue = DEObj.GetDegrees(), direction=0;
		DEObj.SetDegrees(degrees);
		
		degreeValue = degreeValue - degrees;
		
		if(degreeValue<0){
			direction = 2;
			degreeValue = degreeValue * -1;
		}
		else{
			direction = 1;
		}
		
		for(int i=0;i<degreeValue;i++){
			
			middleMotor.controlMotor(30, direction);
			Delay.msDelay(9);
		}
		middleMotor.controlMotor(0,0);
		
	}
	
	String[][] GetPuzzleColors(){ // storages the locations of the colors of the puzzle circles
		
		String[][] puzzleColors = new String[4][4];

		for(int i=0;i<=3;i++){
			for(int u=0;u<=3;u++){
				if(i>1 && u<=1){
					puzzleColors[i][u]="Y";
				}
				else if(i<=1 && u>1){
					puzzleColors[i][u]="B";
				}
				else if(u>1 && i>1){
					puzzleColors[i][u]="R";
				}
				else{
					puzzleColors[i][u]="G";
				}
				
			}
		}
		
		return puzzleColors;
		
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
		
		DEObj.SetDegrees(180);
		
		while(true){
			
			value = DEObj.getColor(); //get the color value
			
			switch(stage){ //will follow the line and continue trying to find it unless little while has passed on white.
			
				case 1:
					//this moves code to stage 2 if only white is detected for a while
					if(DEObj.GetTime() > 4800 && DEObj.GetFollow()){ 
						stage=2;
						//LCD.drawString("stage 2", 1, 1);
						break;
					}
					
					//calculations for the turn is calculated here
					correction = kp * ( midpoint - value); 
					turn = (int)(correction*100);
					if(turn < -5){ //turns slower to the other side in order not to do drastic turns when reaching the white section
						turn = -5;
					}
					left = forward - turn;
					right = forward + turn;
					break;
					
					
				case 2: // will search for non-white line
					//if no longer on only white, will go to stage 3
					if(value < (DEObj.GetMiddle() * 1.1)){
						stage = 3;
						//LCD.drawString("stage 3", 1, 1);
						break;
					}
					
					//calculations for the turn is calculated here
					turn = 10;
					left = forward + turn;
					right = forward + (turn);				
					break;
					
				case 3: // stops and turns left in order go to the right direction
					
					left = -20;
					right = 45;
					time++;

					double temp = DEObj.GetMiddle();
					
					if(value > temp * 0.6 && value < temp * 1.2 && time > 700){
						//left = 0;
						//right = -30;
						Forward(0,-30);
						Delay.msDelay(100);
						time = 0;
						stage = 4;
						//LCD.drawString("stage 4", 1, 1);

					}
					break;
				
				
				case 4: //will try to find the colors. Goes from red to blue. If block is detected, will check it
					
					String color = GetColor();
					forward=34;
					
					if(color=="red"){ //if color red is detected
						
						if(rightTurns==0){	//if the robot has not yet turned on circles
							
							while(true){
								
								if(DEObj.getStop()){
									break;
								}
								
								Forward(50,20);
								value = DEObj.getColor();
								color = GetColor();
								
								temp = DEObj.GetMiddle();
								
								if((value > (temp * 0.7) && color != "red") || color=="black"){
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
						else{ //for crossing rec circles
							left = 49;
							right = 50;
							Forward(left,right);
							Delay.msDelay(700);
							straight++;
						}
					}
					else if(rightTurns>0 && color == "blue"){ //for crossing the blue circles and crossing to the next stage
						
						if(straight >= 2){
							stage = 5;
							//LCD.drawString("stage 5", 1, 1);

							break;
						}
						else{
							left = 49;
							right = 50;
							Forward(left,right);
							Delay.msDelay(700);
							straight++;
						}
					}
					
					//LCD.drawString("d= " + DEObj.GetDistance(), 1, 2);
					
					//if( !(DEObj.GetDistance() > 0.04) && newBlock==true){ 
					if( (DEObj.GetDistance()<0.04) && newBlock==true){ //looks if blocks are detected
						
						newBlockColor = CheckColor();
						//LCD.drawString("block= " + newBlockColor, 1, 4);
						
						if(newBlockColor=="black"){ //if block is black
							//will circle around the block
							
							//this sequence will circle around the block
							Forward(-50,-17);
							Delay.msDelay(700);
							Forward(40,40);
							Delay.msDelay(1500);
							Forward(50,5);
							Delay.msDelay(950);
							
							while(value > DEObj.GetMiddle() * 0.6){ //will continue the puzzle after black line is detected
								
								if(DEObj.getStop()){
									break;
								}
								
								value = DEObj.getColor();
								Forward(49,50);
								Delay.msDelay(5);
							}
							straight++;

							Forward(-30,30);
							Delay.msDelay(1000);	
							
						}
						else{
							newBlock=false;
						}
					}
					
					//LCD.drawString("d= " + DEObj.GetDistance(), 1, 2);
					
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
					
				case 5: //will move on white until black line is detected
					forward=39;
					left=40;
					right=37;
					
					time++;
					if(value < DEObj.GetMiddle() * 1.1 && time > 1500){

						forward = 45;
						midpoint = midpoint + 0.105;
						
						left = 0;
						right = -35;
						Forward(left,right);
						Delay.msDelay(320);	
						
						stage = 6;
						//LCD.drawString("stage 6", 1, 1);

						break;
					}
					
					break;
					
				case 6: //will follow the line until the end
					
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
