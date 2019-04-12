package mainPackage;
/*
 * 	TicTacToe
 * 	
 * 	Written by Jared Caruso
 * 	11/9/2018
 * 	IST 271
 * 
 * 	This is a TicTacToe game made in using Swing/Awt. There are 3 difficulty settings: easy,
 * 	normal and impossible, all being against the computer. In addition, there is also a 
 * 	two player mode.
 * 	
 * 	How the program works: It creates parallel arrays for the "status map" and the squares.
 * 	The status map is for calculations and shows what each spot on the board is being occupied
 * 	by. When a space (actually a JLabel/Square object) is clicked, it will change to X or O depending
 * 	on how the status map layout looks. For two player mode, it will simply change to X or O depending on
 * 	which player's turn it is. A victory is also determined by the status map
 */

import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class TicTacToe extends JFrame {

	private JPanel contentPane;
	
	public static boolean boolOnePlayerMode = true;
	public static boolean boolTurnPlayerOne = true;
	public static boolean boolGameActive = true;
	
	// Array for the difficulty settings
	// 0 = easy, 1 = normal, 2 = impossible, 3 = two player
	public final static int[] difficulties = {0, 1, 2, 3};
	public static int difficulty = 1; // default to one player normal mode
	
	// 2D array for the squares
	public static Square[] squares = new Square[9];
	
	// parallel 2D array for the status of the squares
	// 0 = X, 1 = O, 2 = empty
	public static int[] statusMap = new int[9];
	
	// Computers selection
	public static int comSel = -1;
	
	// Networking stuff
	public static String ip = "localhost";
	public static int port = 1116;
	public static DataOutputStream out;
	public static DataInputStream in;
	public static AtomicBoolean boolTurnHost = new AtomicBoolean(false);
	public static AtomicBoolean accepted = new AtomicBoolean(false);
	public static AtomicBoolean networkingModeActive = new AtomicBoolean(true);
	public static AtomicBoolean isHost = new AtomicBoolean(true);
	public static ServerSocket host;
	public static Socket socket;
	public static Thread networkingModeThread;
	public static Runnable networkingModeRunnable;
	public static AtomicInteger ps = new AtomicInteger(-1);
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TicTacToe frame = new TicTacToe();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		networkingModeRunnable = new Runnable() {
			public void run() {
				while (networkingModeActive.get()) {
					if (boolGameActive) {
						
						if (!boolTurnHost.get()) {
							
							try {
								int inRead = in.readInt();
								
								System.out.println("Received: " + inRead);
								
								if (TicTacToe.isHost.get()) {
									TicTacToe.updateSquare(inRead, true);
								} else {
									TicTacToe.updateSquare(inRead, false);
								}
								
								boolTurnHost.set(true);
							} catch (IOException e) {
								System.out.println("Exception occured in receiving selection. The game has been set to inactive.");
								boolGameActive = false;
							}
							
							
						}
						
						if (TicTacToe.victory()) {
							JOptionPane.showMessageDialog(null, "A player has won!", "TicTacToe", JOptionPane.PLAIN_MESSAGE, null);
							boolGameActive = false;
						}
						if (TicTacToe.getStatusMap().matches("[01][01][01][01][01][01][01][01][01]")) { 
							JOptionPane.showMessageDialog(null, "It's a tie :O", "TicTacToe", JOptionPane.PLAIN_MESSAGE, null);
							boolGameActive = false;
						}
						
						if (!accepted.get()) {
							System.out.println("Listening...");
							listen();
						}
						
						
					}

				}
			}
		};
	}

	/**
	 * Create the frame.
	 */
	public TicTacToe() {
		setResizable(false);
		setTitle("Caruso_IST271_Final : Tic-Tac-Toe");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 324, 400);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnGame = new JMenu("Game");
		menuBar.add(mnGame);
		
		JMenu mnMode = new JMenu("Mode");
		mnGame.add(mnMode);
		
		JMenu mnOnePlayer = new JMenu("One Player");
		mnMode.add(mnOnePlayer);
		
		JMenuItem mntmTwoPlayer = new JMenuItem("Two Player");
		mntmTwoPlayer.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				difficulty = difficulties[3];
				boolOnePlayerMode = false;
				restartGame();

			}
		});
		mnMode.add(mntmTwoPlayer);
		
		JMenuItem mntmEasy = new JMenuItem("Easy");
		mntmEasy.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				boolOnePlayerMode = true;
				difficulty = difficulties[0];
				restartGame();
				
				System.out.println("Difficulty set to 0 (easy)");
			}
		});
		mnOnePlayer.add(mntmEasy);
		
		JMenuItem mntmNormal = new JMenuItem("Normal");
		mntmNormal.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				boolOnePlayerMode = true;
				difficulty = difficulties[1];
				restartGame();
				
				System.out.println("Difficulty set to 1 (normal)");
			}
		});
		mnOnePlayer.add(mntmNormal);
		
		JMenuItem mntmImpossible = new JMenuItem("Impossible");
		mntmImpossible.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				JOptionPane.showMessageDialog(null, "Impossible mode is experimental and may not work completely. \nErrors/bugs have yet to be discovered");
				boolOnePlayerMode = true;
				difficulty = difficulties[2];
				restartGame();
				

				
				
				System.out.println("Difficulty set to 2 (Impossible)");
			}
		});
		mnOnePlayer.add(mntmImpossible);
		
		JMenu mnNetwork = new JMenu("Network");
		mnMode.add(mnNetwork);
		
		JMenuItem mntmConfigure = new JMenuItem("Configure");
		mnNetwork.add(mntmConfigure);
		
		JMenuItem mntmStart = new JMenuItem("Start");
		mntmStart.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				System.out.println("Starting Network Mode...");
				
				//JOptionPane.showMessageDialog(null, "Launching incomplete, experimental network mode");
				
				if (!connected()) {
					
					try {
						host = new ServerSocket(port, 8, InetAddress.getByName(ip));
					} catch (Exception e) {
						e.printStackTrace();
					}
					isHost.set(true);
					boolTurnHost.set(true);

				} else {
					isHost.set(false);
				}
				
				difficulty = 4;
				
				networkingModeThread = new Thread(networkingModeRunnable);
				networkingModeThread.start();
				System.out.println("Network Mode started successfully");
			}
		});
		mnNetwork.add(mntmStart);
		
		
		
		JMenuItem mntmRestart = new JMenuItem("Restart");
		mntmRestart.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {

				restartGame();
			}
		});
		mntmRestart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
		mnGame.add(mntmRestart);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				System.out.println("Exiting program...");
				System.exit(0);
			}
		});
		mnGame.add(mntmExit);
		
		JMenu mnDebug = new JMenu("Debug");
		menuBar.add(mnDebug);
		
		JMenuItem mntmShowStatusMap = new JMenuItem("Status Map");
		mntmShowStatusMap.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				System.out.println("\n");
				System.out.println(statusMap[0] + " - " + statusMap[1] + " - " + statusMap[2]);
				System.out.println(statusMap[3] + " - " + statusMap[4] + " - " + statusMap[5]);
				System.out.println(statusMap[6] + " - " + statusMap[7] + " - " + statusMap[8]);
				System.out.println(getStatusMap());
				System.out.println(statusMap);
			}
		});
		mnDebug.add(mntmShowStatusMap);
		
		JMenuItem mntmUsedSquares = new JMenuItem("Used Squares");
		mntmUsedSquares.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				System.out.println("\n");
				System.out.println("Number of used squares: " + getNumberUsed());
			}
		});
		mnDebug.add(mntmUsedSquares);
		
		JMenuItem mntmTurnPlayer = new JMenuItem("Turn Player");
		mntmTurnPlayer.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent a) {
				System.out.println("boolTurnPlayerOne: " + boolTurnPlayerOne);
			}
		});
		mnDebug.add(mntmTurnPlayer);
		
		JMenuItem mntmNetworkInfo = new JMenuItem("Network Info");
		mntmNetworkInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("out: " + out.toString());
				System.out.println("in: " + in.toString());
				System.out.println("ip: " + ip);
				System.out.println("port: " + port);
				System.out.println("isHost: " + isHost.get());
				System.out.println("boolTurnHost: " + boolTurnHost.get());
			}
		});
		mnDebug.add(mntmNetworkInfo);
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		for (int s = 0; s < squares.length; s++) {
			squares[s] = new Square();
			squares[s].setNumber(s);
			squares[s].setFont(new Font("Dialog", Font.BOLD, 72));
			squares[s].setHorizontalAlignment(SwingConstants.CENTER);
		}

		squares[0].setBounds(26, 16, 75, 75);
		squares[1].setBounds(131, 16, 75, 75);
		squares[2].setBounds(230, 16, 75, 75);
		squares[3].setBounds(26, 122, 75, 75);
		squares[4].setBounds(131, 122, 75, 75);
		squares[5].setBounds(230, 122, 75, 75);
		squares[6].setBounds(26, 224, 75, 75);
		squares[7].setBounds(131, 224, 75, 75);
		squares[8].setBounds(230, 224, 75, 75);
		
		for (int a = 0; a < squares.length; a++) {
			contentPane.add(squares[a]);
		}
		
		
		JLabel lblBoard = new JLabel("");
		lblBoard.setBounds(12, 12, 300, 300);
		try {
			lblBoard.setIcon(new ImageIcon(ImageIO.read(new File("resources\\board.jpg"))));
		} catch (IOException e) {
			try {
				System.out.println("board.jpg not found. checking alternative path (linux)");
				lblBoard.setIcon(new ImageIcon(ImageIO.read(new File("resources/board.jpg"))));
			} catch (IOException e2) {
				System.out.println("board.jpg not found.");
				//System.exit(0);
			}
		}
		contentPane.add(lblBoard);
		
		restartGame();
	}
	
	public static void updateSquare(int squareNumber, boolean reversed)
	  {
	    if (!reversed) {
	      if (boolTurnPlayerOne) {
	        squares[squareNumber].setText("X");
	        squares[squareNumber].setStatus(0);
	      } else {
	        squares[squareNumber].setText("O");
	        squares[squareNumber].setStatus(1);
	      }
	    }
	    else if (boolTurnPlayerOne) {
	      squares[squareNumber].setText("O");
	      squares[squareNumber].setStatus(1);
	    } else {
	      squares[squareNumber].setText("X");
	      squares[squareNumber].setStatus(0);
	    }
	  }
	
	/**
	 * This method restarts the game
	 */
	public static void restartGame() {
		for (int m = 0; m < statusMap.length; m++) {
			statusMap[m] = 2;
			squares[m].setStatus(statusMap[m]);
			squares[m].setText("");
		}
		
		if (difficulty == difficulties[2]) {
			squares[4].setStatus(1);
			squares[4].setText("O");
		}
		
		updateStatusMap();
		
		boolTurnPlayerOne = true;
		boolGameActive = true;
	}
	
	/**
	 * This method updates the status map
	 */
	public static void updateStatusMap() {
		for (int update = 0; update < squares.length; update++) {
			statusMap[update] = squares[update].getStatus();
		}
	}
	
	/**
	 * this String method will return the status map as a String, for later calculations
	 * @return status map as one line String
	 */
	public static String getStatusMap() {
		updateStatusMap();
		StringBuilder smrBuilder = new StringBuilder(); //smr = status map return
		for (int smr = 0; smr < squares.length; smr++) {
			if (statusMap[smr] == 0) {
				smrBuilder.append("0");
			} else {
				smrBuilder.append(String.valueOf(statusMap[smr]));
			}
		}
		
		return (smrBuilder.toString());
	}
	
	/**
	 * This int method gets the number of used squares
	 * 
	 * @return used squares
	 */
	public static int getNumberUsed() {
		int used = 0;
		for (int n = 0; n < squares.length; n++) {
			if (!isUnusedSquare(n)) {
				used++;
				//System.out.println("getNumberUsed(): unused square found. total: " + used);
			}
		}
		return used;
	}
	
	/**
	 * This int method returns a random unused square (square with a status of 2)
	 * 
	 * @return random unused square
	 */
	public static int getRandomUnusedSquare() {
		Random random = new Random();
		int unused = -1;
		
		do {
			unused = random.nextInt(8);
		} while (!isUnusedSquare(unused));
		
		return unused;
	}
	
	/**
	 * 	This boolean method checks if a square is unused, for use in the previous method
	 * 
	 * 	@return true if unused, false if used
	 */
	public static boolean isUnusedSquare(int squareNumber) {
		if (squares[squareNumber].getStatus() == 2) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Now, this method can select and place an "O" in a square for the computer,
	 * based on the difficulty setting (easy, normal, impossible)
	 */
	public static void computerSelection() {
		comSel = -1;
		
		/*
		 * 	Easy difficulty:
		 * 
		 * 	Computer's selection is always random
		 */
		if (difficulty == difficulties[0]) {
			comSel = getRandomUnusedSquare();
		}
		
		
		/*
		 * 	Normal difficulty:
		 * 
		 * 	Computer's selection is still random, but tries to win and 
		 * 	attempts to stop the user from winning. This is the default
		 * 	difficulty setting
		 */
		else if (difficulty == difficulties[1]) {
			//Pick a random square
			comSel = getRandomUnusedSquare();
			//But change it to stop the player from winning
			attemptComputerBlock();
			//Then change it again if the computer can win
			attemptComputerVictory();
			
		}
		
		/*
		 * Impossible mode:
		 * 
		 * The computer goes first and will always make the best moves. This will
		 * always result in either a tie or a victory for the computer
		 */
		else if (difficulty == difficulties[2]) {
			comSel = getRandomUnusedSquare();
			//Edges:
			if (getStatusMap().matches("222210222")) { comSel = 0; }
			if (getStatusMap().matches("222212202")) { comSel = 2; }
			if (getStatusMap().matches("202212222")) { comSel = 6; }
			if (getStatusMap().matches("222012222")) { comSel = 8; }
			//Corners
			if (getStatusMap().matches("222212220")) { comSel = 0; }
			if (getStatusMap().matches("222212022")) { comSel = 2; }
			if (getStatusMap().matches("220212222")) { comSel = 6; }
			if (getStatusMap().matches("022212222")) { comSel = 8; }

			attemptComputerBlock();
			attemptComputerVictory();
			
		}
		
		
		// Do the logic: Update the square status and then change the text
		squares[comSel].setStatus(1);
		squares[comSel].setText("O");
		
		//Debug
		System.out.println("Computer's selection: " + comSel);
	}
	
	
	/**
	 * Attempt computer victory;
	 */
	public static void attemptComputerVictory() {
		if 		(getStatusMap().matches("211......")) { comSel = 0; }
		if (getStatusMap().matches("121......")) { comSel = 1; }
		if (getStatusMap().matches("112......")) { comSel = 2; }
		if (getStatusMap().matches("...211...")) { comSel = 3; }
		if (getStatusMap().matches("...121...")) { comSel = 4; }
		if (getStatusMap().matches("...112...")) { comSel = 5; }
		if (getStatusMap().matches("......211")) { comSel = 6; }
		if (getStatusMap().matches("......121")) { comSel = 7; }
		if (getStatusMap().matches("......112")) { comSel = 8; }
		if (getStatusMap().matches("1..1..2..")) { comSel = 6; }
		if (getStatusMap().matches("1..2..1..")) { comSel = 3; }
		if (getStatusMap().matches("2..1..1..")) { comSel = 0; }
		if (getStatusMap().matches(".1..1..2.")) { comSel = 7; }
		if (getStatusMap().matches(".1..2..1.")) { comSel = 4; }
		if (getStatusMap().matches(".2..1..1.")) { comSel = 1; }
		if (getStatusMap().matches("..1..1..2")) { comSel = 8; }
		if (getStatusMap().matches("..1..2..1")) { comSel = 5; }
		if (getStatusMap().matches("..2..1..1")) { comSel = 2; }
		if (getStatusMap().matches("1...1...2")) { comSel = 8; }
		if (getStatusMap().matches("1...2...1")) { comSel = 4; }
		if (getStatusMap().matches("2...1...1")) { comSel = 0; }
		if (getStatusMap().matches("..1.1.2..")) { comSel = 6; }
		if (getStatusMap().matches("..1.2.1..")) { comSel = 4; }
		if (getStatusMap().matches("..2.1.1..")) { comSel = 2; }
	}
	
	/**
	 * This method is part of the computer's selection series and will make the
	 * computer attempt to stop the player from winning
	 */
	public static void attemptComputerBlock() {
		if 		(getStatusMap().matches("200......")) { comSel = 0; }
		if (getStatusMap().matches("020......")) { comSel = 1; }
		if (getStatusMap().matches("002......")) { comSel = 2; }
		if (getStatusMap().matches("...200...")) { comSel = 3; }
		if (getStatusMap().matches("...020...")) { comSel = 4; }
		if (getStatusMap().matches("...002...")) { comSel = 5; }
		if (getStatusMap().matches("......200")) { comSel = 6; }
		if (getStatusMap().matches("......020")) { comSel = 7; }
		if (getStatusMap().matches("......002")) { comSel = 8; }
		if (getStatusMap().matches("0..0..2..")) { comSel = 6; }
		if (getStatusMap().matches("0..2..0..")) { comSel = 3; }
		if (getStatusMap().matches("2..0..0..")) { comSel = 0; }
		if (getStatusMap().matches(".0..0..2.")) { comSel = 7; }
		if (getStatusMap().matches(".0..2..0.")) { comSel = 4; }
		if (getStatusMap().matches(".2..0..0.")) { comSel = 1; }
		if (getStatusMap().matches("..0..0..2")) { comSel = 8; }
		if (getStatusMap().matches("..0..2..0")) { comSel = 5; }
		if (getStatusMap().matches("..2..0..0")) { comSel = 2; }
		if (getStatusMap().matches("0...0...2")) { comSel = 8; }
		if (getStatusMap().matches("0...2...0")) { comSel = 4; }
		if (getStatusMap().matches("2...0...0")) { comSel = 0; }
		if (getStatusMap().matches("..0.0.2..")) { comSel = 6; }
		if (getStatusMap().matches("..0.2.0..")) { comSel = 4; }
		if (getStatusMap().matches("..2.0.0..")) { comSel = 2; }
	}
	
	/**
	 * This method checks for a victory
	 */
	public static boolean victory() {
		// Control boolean variable
		boolean winner = false;
		
		// Check for a winner
		if (getStatusMap().matches("000"
				+ 				   "..."
				+ 				   "...")) { winner = true; }
		if (getStatusMap().matches("..."
				+ 				   "000"
				+ 				   "...")) { winner = true; }
		if (getStatusMap().matches("..."
				+ 				   "..."
				+ 				   "000")) { winner = true; }
		if (getStatusMap().matches("0.."
				+ 				   "0.."
				+ 				   "0..")) { winner = true; }
		if (getStatusMap().matches(".0."
				+ 				   ".0."
				+ 				   ".0.")) { winner = true; }
		if (getStatusMap().matches("..0"
				+ 				   "..0"
				+ 				   "..0")) { winner = true; }
		if (getStatusMap().matches("0.."
				+ 				   ".0."
				+ 				   "..0")) { winner = true; }
		if (getStatusMap().matches("..0"
				+ 				   ".0."
				+ 				   "0..")) { winner = true; }
		
		if (getStatusMap().matches("111"
				+ 				   "..."
				+ 				   "...")) { winner = true; }
		if (getStatusMap().matches("..."
				+ 				   "111"
				+ 				   "...")) { winner = true; }
		if (getStatusMap().matches("..."
				+ 				   "..."
				+ 				   "111")) { winner = true; }
		if (getStatusMap().matches("1.."
				+ 				   "1.."
				+ 				   "1..")) { winner = true; }
		if (getStatusMap().matches(".1."
				+ 				   ".1."
				+ 				   ".1.")) { winner = true; }
		if (getStatusMap().matches("..1"
				+ 				   "..1"
				+ 				   "..1")) { winner = true; }
		if (getStatusMap().matches("1.."
				+ 				   ".1."
				+ 				   "..1")) { winner = true; }
		if (getStatusMap().matches("..1"
				+ 				   ".1."
				+ 				   "1..")) { winner = true; }
		
		// You know what this is
		if (winner) {
			TicTacToe.boolGameActive = false;
			return true;
		} else {
			return false;
		}
	}
	
	// Networking
	/**
	 * Check if a player is connected
	 * @return
	 */
	private static boolean connected() {
		try {
			// Create the socket
			socket = new Socket(ip, port);
			// Out stream
			out = new DataOutputStream(socket.getOutputStream());
			// in Stream
			in = new DataInputStream(socket.getInputStream());
			// if it all worked out, mark that the player was accepted
			accepted.set(true);
			//lblWaitingForPeer.setVisible(false);
		} catch (IOException e) {
			// It failed
			System.out.println("Searching for other player...");
			return false;
		}
		// they're connected!!
		System.out.println("Successfully connected to the host");
		return true;
	}
	
	/**
	 * This method checks for a request from another player
	 */
	private static void listen() {
		// Temporary socket
		Socket temp = null;
		try {
			// Check if the host has accepted, using temporary socket
			temp = host.accept();
			// Debug
			System.out.println("Found request");
			// Streams
			out = new DataOutputStream(temp.getOutputStream());
			in = new DataInputStream(temp.getInputStream());
			// Accepted
			accepted.set(true);
			System.out.println("Accepted request");
			//lblWaitingForPeer.setVisible(false);
		} catch (IOException e) {
			System.out.println("Request failed");
			e.printStackTrace();
		}
	}
}
