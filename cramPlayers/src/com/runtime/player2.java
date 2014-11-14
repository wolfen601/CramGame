package com.runtime;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Stack;
import java.util.StringTokenizer;


public class player2 {

	// The client socket
			private static Socket clientSocket = null;
			// The output stream
			private static PrintStream os = null;
			// The input stream
			private static DataInputStream is = null;

			private static BufferedReader inputLine = null;
			private static boolean closed = false;
			
			private static String input;
			
			private static boolean inGame = false;
			
			private static String gameID;
			
			private static String turn;
			private static String boardAsString;
			private static String previousMove;
			
			private static Stack<String> prevMove = new Stack<String>();
				
			public static void main(String[] args) throws UnknownHostException, IOException{
				
				//////////////////////////////////////////////////
				// JOIN SERVER
				//////////////////////////////////////////////////
				
				clientSocket = new Socket("beastMode.ddns.net", 63400);
				inputLine = new BufferedReader(new InputStreamReader(System.in));
				os = new PrintStream(clientSocket.getOutputStream());
				is = new DataInputStream(clientSocket.getInputStream());
					
				System.out.println("Connection Successful!");
				
				os.println("Player"); // for server side info
				
				System.out.println("Enter what game to join (ex. G000000000)");
				
				
				while(!inGame){
					
					String gName = inputLine.readLine();
					
					if(gName != null){
					
						os.println("GETGAME:" + gName);
					
						boolean found = false;
					
						while(!found){
						
							input = null;
						
							input = is.readLine();
						
							if(input != null){
							
								if(input.length() > gName.length() + 1){
								
									if(input.substring(0, gName.length()).equals(gName)){
									
										if(input.equals(gName + ":DOES NOT EXIST")){
										
											System.out.println("Game not Found!");
											found = true; // its not found by server, but input was found about it
										
										}
										else{
										
											System.out.println("Game Found!");
											found = true;
											inGame = true;
											gameID = gName;
										
										
										}
									
									
									}
								
								}
							
							
							}
						
						
						
						}
					
					}
					else{
					
						System.out.println("Too Short");
					
					
					
					
					}
				}
				///////////////////////////////////////////////////////
				// SERVER JOINED ... GIVEN GAME ID
				///////////////////////////////////////////////////////
				
				boolean gameWon = false;
				
				while(!gameWon){
					
					//////////////////////////////////////
					// Get who's turn it is, will keep listening for your turn
					/////////////////////////////////////
					
					os.println("GETTURN:" + gameID + ":P2");
					boolean gotTurn = false;
					while(!gotTurn){
						
						input = null;
						
						input = getMessage();
						
						if(input != null){
							
							if(input.length() == 16){
								if(input.substring(0, 11).equals(gameID+":")){
									
									System.out.println(input);
									
									String mid[] = input.split(":");
									String tCheck = mid[2];
									
									if(tCheck.equals("P2")){
										turn = mid[1];
									
										System.out.println("turn:" + turn);
									
										gotTurn = true;
									}
									
									
								}
								
							}
							
						}
						
					}
					
					////////////////////////////////////////////////
					// Turn received if its yours ... move
					////////////////////////////////////////////////
					
					if(turn.equals("P2")){ // its your turn
						
						// get current state of gameboard
						os.println("GETGAME:" + gameID + ":P2");
						boolean gotBoard = false;
						while(!gotBoard){
							
							input = null;
							
							input = getMessage();
							
							if(input != null){
								
								if(input.length() == 44){
									if(input.substring(0, 10).equals(gameID)){
										
										String mid[] = input.split(":");
										String pCheck = mid[2];
										
										if(pCheck.equals("P2")){
											boardAsString = mid[1];
											
											previousMove = mid[3];
										
											System.out.println("boardAsString: " + boardAsString);
										
											gotBoard = true;
										}
										
									}
									
								}
								
							}
							
						}
						
						
						// got board of game ... now prompt player move
						
						String pMove = move();
						
						os.println(gameID + "P2" + pMove);	// send player move to master server
						
						// pause program to not overload master
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if(turn.equals("VA")){
						
						gameWon = true;
						
					}
					else{
						
						// pause program to not overload master
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					
					
				}
				
				System.out.println("Game over.");
				os.println("/quit");
			
			
			}
			
			
			
			public static String getMessage() throws IOException{
				
				
				String message = is.readLine();
				
				
				if(message != null)
					return message;
				
				else
					return null;
				
			}
			
			
			public static String move() throws IOException{ // can remove exception when user input is removed
				
				String playerMove = null;
				
				char boardMatrix[][] = new char[5][5];
				
				System.out.println("Board as matrix");
				
				for(int index = 0; index < 25; index++){
					
					boardMatrix[index%5][index/5] = boardAsString.charAt(index);
					if(index%5 == 4)
						System.out.print(boardMatrix[index%5][index/5] + "\n");
					else
						System.out.print(boardMatrix[index%5][index/5] + " ");
					
				}
				
				System.out.println("Previous move: " + previousMove);
				
				
				///////////////////////////////////////////////////////
				// 
				//
				// INSERT YOUR ALGORITHM BELOW
				//
				// THE MOVE MUST BE STRING IN FORMAT A1A2 WHERE A1 REPRESESNTS ONE SQUARE AND A2 THE OTHER
				// THE HORIZONTAL AXIS OF THE BOARD IS A -> E
				// THE VERTICAL AXIS OF THE BOARD IS 1 -> 5
				// THE LETTERS ARE CASE SENSITIVE
				// 
				//
				// NOTE THAT THE GIVEN MATRIX IS NUMBERED 0 -> 4 IN EACH DIMENSION
				// THE GIVEN MATRIX REPRESENTS THE CURRENT STATE OF THE BOARD
				// EX. A1 IS boardMatrix[0][0] AND E5 is boardMatrix[4][4]
				// CHAR O ON A COORD MEANS SPOT IS VACANT, R AND B REPRESENT THE PLAYER MOVES AND M THE GREY SQUARES
				// YOU CAN ONLY PLACE PIECES ON 2 ADJACENT O SPACES, IT IS YOUR RESPONSIBILITY TO MAKE SURE THE MOVE IS VALID
				//
				//
				// NOTE ALONG WITH THE GIVE BOARD ... THE PREVIOUS MOVE IS AVAILABLE IN STRING previousMove
				//
				////////////////////////////////////////////////////////
				
				
				/*
				 * calculate when to stop placing random stuff
				 */
				
				//calculation number of free pieces here
				int numFreeBlocks = 0;
				numFreeBlocks = calcFree(boardMatrix);
				if(numFreeBlocks > 10){
					playerMove = kevAlgo(previousMove, boardMatrix);
				}else{
					//insert nimbers tree thing here Dan
					playerMove = danAlgo(previousMove, boardMatrix);
				}
				//System.out.println("Enter move (for testing, to be replaced with algorithm):");
				//playerMove = inputLine.readLine(); // for now move is just user input, for testing, replace this with your algorithm when ready
				
				
				
				//////////////////////////////////////////////////////
				// END OF ALGORITHM
				//////////////////////////////////////////////////////
				
				return playerMove;
				
			}
			
			/*
			 * Placing algorithm to survive in the game. It just tries to find a spot to place,
			 * If it cannot find a spot, it will move to Nimber algorithm to find a spot.
			 */
			public static String kevAlgo(String previousMove,  char boardMatrix[][]) throws IOException{
				/************************
				 * Kevin's algorithm
				 ************************/
				
				//grab each individual block and push into a stack
				String block1 = "";
				String block2 = "";
				block1 = previousMove.substring(0, 2);
				block2 = previousMove.substring(3);
				//only pushes valid blocks into the stack. Meaning player one wont push null blocks into a stack when he goes first
				if(block1 != "" && block2 != ""){
					prevMove.push(block1);
					prevMove.push(block2);
				}
				/*
				 * validMoveCount - count until two moves are placed
				 * currBlock - store the current block
				 */
				String playerMove = "";
				int validMoveCount = 0;
				String currBlock = "";
				String temp = "";
				int row = 0;
				int col = 0;
				while(true){
					currBlock = prevMove.peek();
					if(currBlock == ""){
						validMoveCount = -1;
						break;
					}
					temp = currBlock.substring(0,1);
					row = letterCompare(temp);
					/* These ifs will check the squares around the current block
					 * If it can find a valid move, tries to find the next valid 
					 * square around it. If it can find a valid second square, it
					 * will skip the if statements and move to finding the answer
					 */
					if((row-1) >=0 && boardMatrix[row-1][col] == 'O'){
						validMoveCount = findSecondMove(row-1, col, boardMatrix);
						if(validMoveCount != -1){
							row--;
							break;
						}
					}
					if((col+1) <=4 && boardMatrix[row][col+1] == 'O'){
						validMoveCount = findSecondMove(row, col+1, boardMatrix);
						if(validMoveCount != -1){
							col++;
							break;
						}
					}
					if((row+1) <=4 && boardMatrix[row+1][col] == 'O'){
						validMoveCount = findSecondMove(row+1, col, boardMatrix);
						if(validMoveCount != -1){
							row++;
							break;
						}
					}
					if((col-1) >=0 && boardMatrix[row][col-1] == 'O'){
						validMoveCount = findSecondMove(row, col-1, boardMatrix);
						if(validMoveCount != -1){
							col--;
							break;
						}
					}
					prevMove.pop();
					
				}
				/* Will find a move based on the second square location.
				 * Builds the playerMove answer based on those locations
				 */
				if(validMoveCount == 0){
					playerMove = revLetterCompare(row) + Integer.toString(col) + revLetterCompare(row-1) + Integer.toString(col);
				}else if(validMoveCount == 1){
					playerMove = revLetterCompare(row) + Integer.toString(col) + revLetterCompare(row) + Integer.toString(col+1);
				}else if(validMoveCount == 2){
					playerMove = revLetterCompare(row) + Integer.toString(col) + revLetterCompare(row+1) + Integer.toString(col);
				}else if(validMoveCount == 3){
					playerMove = revLetterCompare(row) + Integer.toString(col) + revLetterCompare(row) + Integer.toString(col-1);
				}else{
					//replace with nimber algorithm if it cannot find a spot
					
					playerMove = danAlgo(previousMove, boardMatrix);
					
					//System.out.println("Enter move (for testing, to be replaced with algorithm):");
					//playerMove = inputLine.readLine(); // for now move is just user input, for testing, replace this with your algorithm when ready
				}
				return playerMove;
			}
			/*
			 * Simple function to change String to integer for the Blocks
			 */
			public static int letterCompare(String temp){
				switch (temp){
				case "A": return 0;
				case "B": return 1;
				case "C": return 2;
				case "D": return 3;
				case "E": return 4;
				default: return 0;
				}
			}
			/*
			 * Simple function to change integer to String for the Blocks (reverse)
			 */
			public static String revLetterCompare(int row){
				switch (row){
				case 0: return "A";
				case 1: return "B";
				case 2: return "C";
				case 3: return "D";
				case 4: return "E";
				default: return "A";
				}
			}
			/*
			 * Finds the second position to determine whether it can place a square
			 */
			public static int findSecondMove(int row, int col, char boardMatrix[][]){
				if((row-1) >=0 && boardMatrix[row-1][col] == 'O'){
					return 0;
				}
				if((col+1) <=4 && boardMatrix[row][col+1] == 'O'){
					return 1;
				}
				if((row+1) <=4 && boardMatrix[row+1][col] == 'O'){
					return 2;
				}
				if((col-1) >=0 && boardMatrix[row][col-1] == 'O'){
					return 3;
				}
				return -1;
			}
	
			public static int calcFree(char boardMatrix[][]){
				return 0;
			}
			public static String danAlgo(String previousMove,  char boardMatrix[][]){
				int row = 5;
				int col = 5;
				boolean bool = false;
				String split = "0";
				StringTokenizer st;
				
				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						bool = checkSolo(boardMatrix, i, j);
						if (bool == true) {
							boardMatrix[i][j] = 'M';
						}
						split = checkSplittable(boardMatrix, row, col);
						st = new StringTokenizer(split);
						while (st.hasMoreTokens()) {
							String element = st.nextToken();
							String letter = element.substring(0,1);
							String sNumber = element.substring(1);
							int number = Integer.parseInt(sNumber);
						}
						
					}
				}
				return "";
			}


			public static boolean checkSolo(char subMatrix[][], int row, int col) {
				//if this doesn't work, make a nested loop. Check if it's within the boundaries first, then if everything beside is 'O'.
				if ((subMatrix[row+1][col] != 'O' && row+1 <= 5) && (subMatrix[row-1][col] != 'O' && row-1 >= 0) && (subMatrix[row][col+1] != 'O' && col+1 <= 5) && (subMatrix[row][col-1] != 'O' && col-1 >=0)) {
					return true;
				}
				return false;
			}
			public static String checkSplittable(char subMatrix[][], int row, int col) {
				boolean check = false;
				String splits = null;
				int i = 0;
				int j = 0;
				for (i = 0; i < row; i++) {
					for (j = 0; j < col; j++) {
						if (subMatrix[i][j] != 'O') {
							check = true;
							continue;
						}
						check = false;
						break; 
					}
					if (check == true) {
						splits += "R" + Integer.toString(i) + " ";
					}
				}
				
				for (i = 0; i < col; i++) {
					for (j = 0; j < row; j++) {
						if (subMatrix[j][i] != 'O') {
							check = true;
							continue;
						}
						check = false;
						break;
					}
					if (check == true) {
						splits += "C" + Integer.toString(j) + " ";
					}
				}
				return splits;
			}
}
