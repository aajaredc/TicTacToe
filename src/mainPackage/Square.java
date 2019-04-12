package mainPackage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;


public class Square extends JLabel {

	
	
	
	int status;
	int number;
	
	private boolean validMoveProcess = true;
	
	public Square() {
		status = 2;
		
		
		
		addMouseListener((MouseListener) new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				
		    	if (TicTacToe.boolGameActive) {
		    		System.out.println("Clicked: " + number);
		    	} else {
		    		System.out.println("Clicked: " + number + " but the game is inactive");
		    	}
		    	

		    	
		    	// If the game is active...
		    	gameActivity: // this label is here so the game can break to prevent people from clicking too quickly
		    	if (TicTacToe.boolGameActive) {
		    		

		    		
		    		if (TicTacToe.difficulty != 4) {
		    			// One Player Mode
						
						PlayerClickLoop:
							for (int clicked = 0; clicked < TicTacToe.squares.length; clicked++) {
								
								if ((number == clicked) && TicTacToe.statusMap[clicked] == 2) {
									if (TicTacToe.boolTurnPlayerOne) {
										setText("X");
										status = 0;
									} else {
										setText("O");
										status = 1;
									}
									break PlayerClickLoop;
								} else if (number==clicked && (TicTacToe.statusMap[clicked] == 0 || TicTacToe.statusMap[clicked] == 1)) {
									validMoveProcess = false;
									System.out.println("nice try but that's already occupied");
								}
									
							}

						
						// If Two Player Mode
						if (!TicTacToe.boolOnePlayerMode) {
							TicTacToe.boolTurnPlayerOne = !TicTacToe.boolTurnPlayerOne; //switch turns
							if (TicTacToe.victory() && !TicTacToe.boolTurnPlayerOne) {
								JOptionPane.showMessageDialog(null, "Player 1 wins", "TicTacToe", JOptionPane.PLAIN_MESSAGE, null);
								break gameActivity;
							}
							if (TicTacToe.victory() && TicTacToe.boolTurnPlayerOne) {
								JOptionPane.showMessageDialog(null, "Player 2 wins", "TicTacToe", JOptionPane.PLAIN_MESSAGE, null);
								break gameActivity;
							}

						// If One Player Mode
						} else {
							if (TicTacToe.victory()) {
								JOptionPane.showMessageDialog(null, "Player wins", "TicTacToe", JOptionPane.PLAIN_MESSAGE, null);
								break gameActivity;
							}
							if (validMoveProcess) {
								if (TicTacToe.getNumberUsed() != 9) {
									TicTacToe.computerSelection();
									
									if (TicTacToe.victory()) {
										JOptionPane.showMessageDialog(null, "Computer wins!", "TicTacToe", JOptionPane.PLAIN_MESSAGE, null);
										break gameActivity;
									}
								}
								

							}
							
						}
						if (TicTacToe.getStatusMap().matches("[01][01][01][01][01][01][01][01][01]")) { 
							validMoveProcess = false;
							JOptionPane.showMessageDialog(null, "It's a tie!", "TicTacToe", JOptionPane.PLAIN_MESSAGE, null);
							TicTacToe.boolGameActive = false;
							break gameActivity;
						}
						TicTacToe.updateStatusMap();	
		    		} else {
		    			
						if (TicTacToe.boolTurnHost.get()) {
							if (TicTacToe.boolGameActive) {
								if (status == 2) {
									try {
										System.out.println("Writing " + number);
										TicTacToe.out.writeInt(number);
										System.out.println("Wrote " + number);
										TicTacToe.out.flush();
										System.out.println("Flushed");
										
										if (TicTacToe.isHost.get()) {
											TicTacToe.updateSquare(number, false);
										} else {
											TicTacToe.updateSquare(number, true);
										}
										
										TicTacToe.boolTurnHost.set(false);
									} catch (Exception e1) {
										System.out.println("Exception occured in writing selection. The game has been set to inactive.");
										TicTacToe.boolGameActive = false;
									}
								} else {
									System.out.println("Hey dumbass, that square is already used");
								}
							}
						} else {
							System.out.println("It isn't your turn, you stupid bitch");
						}
		    		}
		    		
		    	}
			}
		});
	}
	
	
	
	// Setters and getters
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
}
