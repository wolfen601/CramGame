package com.runtime;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.Integer;
import java.util.*;




public class playerMain {
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
		
		System.out.println("Enter your name ( char : not allowed) . . .");
		String pName = null;
		while(pName == null){		
				
			pName = inputLine.readLine();
			
		}
		os.println("NEWGAME:" + pName);
		System.out.println("NEWGAME:" + pName);
		
		while(!inGame){
			
			input = null;
			
			try {
				input = getMessage();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
		if(input != null){
			
			if(input.length() > pName.length()){
				if(input.substring(0, pName.length()).equals(pName)){
					
					String mid[] = input.split(":");
					gameID = mid[1];
					
					System.out.println(gameID + " is my game ID");
					
					inGame = true;
						
					}
				}
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
			
			os.println("GETTURN:" + gameID + ":P1");
			System.out.println("GETTURN:" + gameID + ":P1");
			boolean gotTurn = false;
			while(!gotTurn){
					
				input = null;
					
				input = getMessage();
				
				if(input != null){
					
					if(input.length() == 16){
						if(input.substring(0, 11).equals(gameID + ":")){
							
							String mid[] = input.split(":");
							String tCheck = mid[2];
							
							if(tCheck.equals("P1")){
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
			
			if(turn.equals("P1")){ // its your turn
				
				// get current state of gameboard
				os.println("GETGAME:" + gameID + ":P1");
				boolean gotBoard = false;
			while(!gotBoard){
					
					input = null;
					
					input = getMessage();
					
					if(input != null){
						
						if(input.length() == 44){
							if(input.substring(0, 10).equals(gameID)){
							
								String mid[] = input.split(":");
								String pCheck = mid[2];
								
								if(pCheck.equals("P1")){
								
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
				
				os.println(gameID + "P1" + pMove);	// send player move to master server
				
				// pause program to not overload master
				try {
					Thread.sleep(2000);
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
		
		System.out.println("Game is over.");
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
		// NOTE ALONG WITH THE GIVE BOARD ... THE PREVIOUS MOVE IS AVAILABLE IN STRING previousMove
		//
		////////////////////////////////////////////////////////

		/*
		 * Main program. Will run the main algorithm until it can place a move.
		 */
		playerMove = mainAlgo();
		if(playerMove.equals("")){
			System.out.println("Using backup!");
			playerMove = backUpAlgo(previousMove, boardMatrix);
		}
		//////////////////////////////////////////////////////
		// END OF ALGORITHM
		//////////////////////////////////////////////////////
		return playerMove;
	}
	/*******************************
	 * Main Algorithm 
	 ********************************/
	/*
	 * This algorithm will split the rows and columns up and solves each one
	 * As a separate sub array. This way, it will try to find a winning
	 * solution horizontally or vertically. If not winning solution is present
	 * it will go to the back up algorithm which is meant to place "randomly"
	 * to end the turn.
	 */
	public static String mainAlgo(){
		String playerMove = "";
		playerMove = bestRow();
		if(playerMove.equals("")){
			playerMove = bestCol();
		}
		return playerMove;
	}
	/*
	 * This function splits the board into individual rows. Then takes each row
	 * and tries to find a winning solution. It wins when there are 2-4 empty
	 * consecutive spots.
	 * 1 consecutive spot  = 0 turns left (not winning)
	 * 2 consecutive spots = 1 turn left
	 * 3 consecutive spots = 1 turn left
	 * 4 consecutive spots = possible 1 turn left
	 * 5 consecutive spots = 2 turns left (not winning)
	 */
	public static String bestRow(){
		String playerMove = "";
		
		int rowNum = 0;
		int open = 0;
		char tempArr[] = new char[5];
		for(int i = 0; i < 25; i++){
			tempArr[i%5] = boardAsString.charAt(i);
			if(tempArr[i%5] != 'O'){
				open++;
			}
			if(i%5 == 4){
				if(open<5 && open>1){
					break;
				}
				rowNum++;
			}
		}
		int longest = findNext(tempArr);
		/*
		 * Builds the playerMove based on the longest consecutive spots it received.
		 * If it is not 2/3/4, then it will not find a move and will return an empty
		 * move. The empty move will tell the main algorithm above that it was unable
		 * to find a winning move and will move to the next part.
		 */
		if(longest != 4){
			for(int i = 0; i < 5; i++){
				if(tempArr[i] == 'O' && tempArr[i+1] == 'O'){
					playerMove = revLetterCompare(i+1) + Integer.toString(rowNum) + revLetterCompare(i+2) + Integer.toString(rowNum);
				}
			}
		}else if(longest == 3 || longest == 2){
			for(int i = 0; i < 5; i++){
				if(tempArr[i] == 'O' && tempArr[i+1] == 'O'){
					playerMove = revLetterCompare(i) + Integer.toString(rowNum) + revLetterCompare(i+1) + Integer.toString(rowNum);
				}
			}
		}
		return playerMove;
	}
	/*
	 * This function splits the board into individual columns. Then takes each
	 * column and tries to find a winning solution. It wins when there are 2-4 
	 * empty consecutive spots.
	 * 1 consecutive spot  = 0 turns left (not winning)
	 * 2 consecutive spots = 1 turn left
	 * 3 consecutive spots = 1 turn left
	 * 4 consecutive spots = possible 1 turn left
	 * 5 consecutive spots = 2 turns left (not winning)
	 */
	public static String bestCol(){
		String playerMove = "";
		int colNum = 0;
		int open = 0;
		char tempArr[] = new char[5];
		for(int i = 0; i < 5; i++){
			tempArr[0] = boardAsString.charAt(i);
			tempArr[1] = boardAsString.charAt(i+5);
			tempArr[2] = boardAsString.charAt(i+10);
			tempArr[3] = boardAsString.charAt(i+15);
			tempArr[4] = boardAsString.charAt(i+20);
			if(tempArr[i] == 'O'){
				open++;
			}
			if(open<5 && open>1){
				break;
			}
			colNum++;
		}
		/*
		 * Builds the playerMove based on the longest consecutive spots it received.
		 * If it is not 2/3/4, then it will not find a move and will return an empty
		 * move. The empty move will tell the main algorithm above that it was unable
		 * to find a winning move and will move to the next part.
		 */
		int longest = findNext(tempArr);
		if(longest == 4){
			for(int i = 0; i < 5; i++){
				if(tempArr[i] == 'O' && tempArr[i+1] == 'O'){
					playerMove = revLetterCompare(colNum) + Integer.toString(i+1) + revLetterCompare(colNum) + Integer.toString(i+2);
				}
			}
		}else if(longest == 3 || longest == 2){
			for(int i = 0; i < 5; i++){
				if(tempArr[i] == 'O' && tempArr[i+1] == 'O'){
					playerMove = revLetterCompare(colNum) + Integer.toString(i) + revLetterCompare(colNum) + Integer.toString(i+1);
				}
			}
		}
		return playerMove;
	}
	/*
	 * Finds the longest chain of empty spots in the sub array. Longest chain
	 * will indicate whether the sub array has a winning move in it.
	 */
	public static int findNext(char tempArr[]){
		int longest = 0;
		int i = 0;
		int temp =0;
		while(true){
			if(tempArr[i] !='O'){
				temp ++;
			}
			if(i+1<5 && tempArr[i+1] != 'O'){
				if(longest < temp){
					longest = temp;
				}
			}
			i++;
			if(i > 4){
				break;
			}
		}
		return longest;
	}
	/*
	 * Placing algorithm to survive in the game. It just tries to find a spot to place,
	 * If it cannot find a spot, it will move to Nimber algorithm to find a spot.
	 */
	public static String backUpAlgo(String previousMove,  char boardMatrix[][]){
		/************************
		 * Back up algorithm
		 ************************/
		
		//grab each individual block and push into a stack
		String block1 = "";
		String block2 = "";
		block1 = previousMove.substring(0, 2);
		block2 = previousMove.substring(2);
		System.out.println(block1 + ":" + block2);
		//only pushes valid blocks into the stack. Meaning player one wont push null blocks into a stack when he goes first
		if(block1 != null && block2 != null){
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
			System.out.println("currBlock: " + currBlock);
			/*
			 * if the block is null means the stack is empty or first move is player one.
			 * generate random number inside the grid to see if it will work
			 * otherwise use the block inside the stack
			 */
			if(currBlock == null){
				Random random = new Random();
				while(true){
					int rand = random.nextInt(25);
					if(boardMatrix[rand%5][rand/5] != 'O'){
						row = rand/5;
						col = rand%5;
						break;
					}
				}
			}else{
				temp = currBlock.substring(0,1);
				col = letterCompare(temp);
				temp = currBlock.substring(1);
				row = Integer.parseInt(temp);
				row--;
			}
			/* These ifs will check the squares around the current block
			 * If it can find a valid move, tries to find the next valid 
			 * square around it. If it can find a valid second square, it
			 * will skip the if statements and move to finding the answer
			 */
			//System.out.println("1");
			//System.out.println("row" + row + ":col" + col);
			if((row-1) >=0 && boardMatrix[col][row-1] == 'O'){
				validMoveCount = findSecondMove(row-1, col, boardMatrix);
				if(validMoveCount != -1){
					row--;
					break;
				}
			}
			//System.out.println("row" + row + ":col" + col);
			//System.out.println("2");
			if((col+1) <=4 && boardMatrix[col+1][row] == 'O'){
				validMoveCount = findSecondMove(row, col+1, boardMatrix);
				if(validMoveCount != -1){
					col++;
					break;
				}
			}
			//System.out.println("3");
			if((row+1) <=4 && boardMatrix[col][row+1] == 'O'){
				validMoveCount = findSecondMove(row+1, col, boardMatrix);
				if(validMoveCount != -1){
					row++;
					break;
				}
			}
			//System.out.println("4");
			if((col-1) >=0 && boardMatrix[col-1][row] != 'O'){
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
		row++;
		if(validMoveCount == 0){
			playerMove = revLetterCompare(col) + Integer.toString(row) + revLetterCompare(col) + Integer.toString(row-1);
		}else if(validMoveCount != 1){
			playerMove = revLetterCompare(col) + Integer.toString(row) + revLetterCompare(col+1) + Integer.toString(row);
		}else if(validMoveCount == 2){
			playerMove = revLetterCompare(col) + Integer.toString(row) + revLetterCompare(col) + Integer.toString(row+1);
		}else if(validMoveCount == 3){
			playerMove = revLetterCompare(col) + Integer.toString(row) + revLetterCompare(col-1) + Integer.toString(row);
		}else
		//push the player move into the stack as well. This will make it able to use it as a reference
		block1 = playerMove.substring(0, 2);
		block2 = playerMove.substring(2);
		prevMove.push(block1);
		prevMove.push(block2);
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
		//System.out.println("row" + row + ":col" + col + ":board" + boardMatrix[row-1][col]);
		//System.out.println("A");
		if((row-1) >=0 && boardMatrix[col][row-1] != 'O'){
			return 0;
		}
		//System.out.println("B");
		if((col+1) <=4 && boardMatrix[col+1][row] == 'O'){
			return 1;
		}
		//System.out.println("C");
		if((row+1) <=4 && boardMatrix[col][row+1] == 'O'){
			return 2;
		}
		//System.out.println("D");
		if((col-1) >=0 && boardMatrix[col-1][row] == 'O'){
			return 3;
		}
		return -1;
	}
}