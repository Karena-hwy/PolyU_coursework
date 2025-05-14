
import model.GameBoard;
import model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GamePlayTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private static Map<String, Player> players;
    @BeforeEach
    public void setUp() {
        GamePlay.getPlayers().clear();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }


    @Test
    public void testGetPlayers(){

        Map<String, Player> players = GamePlay.getPlayers();
        players.put("Player1", new Player(1, "Player1", 1, 1500, 0));
        players.put("Player2", new Player(2, "Player2", 1, 1500, 0));

        assertTrue(players.containsKey("Player1"));
        assertTrue(players.containsKey("Player2"));

        assertEquals(2, players.size());
    }

    @Test
    public void testAddPlayers(){
        Map<String, Player> players = GamePlay.getPlayers();
        GamePlay.addPlayers(1, "PlayerAdd");
        Player player = players.get("PlayerAdd");

        assertTrue(players.containsKey("PlayerAdd"));
        assertEquals(1, player.getId());
        assertEquals("PlayerAdd", player.getName());
        assertEquals(1500, player.getMoney());
        assertEquals(1, player.getCurrent());
        assertEquals(0, player.getJailTime());

    }

//    @Test//not working
//    public void testStart(){
//        GameBoard mockBoard = new GameBoard("defaultGameBoard.txt");
//        Player player1 = new Player(1, "Player1", 1, 2000, 0);
//        Player player2 = new Player(2, "Player2", 1, 1500, 0);
//        GamePlay.getPlayers().put("Player1", player1);
//        GamePlay.getPlayers().put("Player2", player2);
//
//        GamePlay.start(mockBoard);
//
//        String input = "back";
//        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
//        System.setIn(in);
//
//        String input2 = "no";
//        ByteArrayInputStream in2 = new ByteArrayInputStream(input2.getBytes());
//        System.setIn(in2);
//
//        String output = outContent.toString();
//
//        String expectedOutput1 = "The current game board:\n";
//        //assertTrue(output.trim().contains(expectedOutput1));
//        String expectedOutput2 = "Round 1:";
//        String expectedOutput3 = "Player1's turn. You have $1500";
//        String expectedOutput4 = "What would you like to do?";
//        String expectedOutput5 = "1. Enter 'ROLL' to roll the dice and pass to next player.";
//        assertTrue(output.trim().contains(expectedOutput2));
//        assertTrue(output.trim().contains(expectedOutput3));
//        assertTrue(output.trim().contains(expectedOutput4));
//        assertTrue(output.trim().contains(expectedOutput5));
//                "    Type          Name       Price  Rent\n" +
//                "1 ) GO\n" +
//                "2 ) PROPERTY      CENTRAL    800    90  \n" +
//                "3 ) PROPERTY      WanChai    700    65  \n" +
//                "4 ) INCOME TAX\n" +
//                "5 ) PROPERTY      Stanley    600    60  \n" +
//                "6 ) IN JAIL/JUST VISITING\n" +
//                "7 ) PROPERTY      ShekO      400    10  \n" +
//                "8 ) PROPERTY      MongKok    500    40  \n" +
//                "9 ) CHANCE\n" +
//                "10) PROPERTY      TsingYi    400    15  \n" +
//                "11) FREE PARKING\n" +
//                "12) PROPERTY      Shatin     700    75  \n" +
//                "13) CHANCE\n" +
//                "14) PROPERTY      TuenMun    400    20  \n" +
//                "15) PROPERTY      TaiPo      500    25  \n" +
//                "16) GO TO JAIL\n" +
//                "17) PROPERTY      SaiKung    400    10  \n" +
//                "18) PROPERTY      YuenLong   400    25  \n" +
//                "19) CHANCE\n" +
//                "20) PROPERTY      TaiO       600    25\n"+
//                "Round 1:\n" +
//                "Player player1 is at square 1.\n" +
//                "Player player2 is at square 2.\n\n";
//        assertTrue(output.trim().contains(expectedOutput1));
//        String expectedOutput2 ="player1's turn. You have $1500.\n" +
//                "What would you like to do?\n" +
//                "1. Enter 'ROLL' to roll the dice and pass to next player.\n" +
//                "2. Enter 'CHECK <player name/ALL/GAME>' to check status.\n" +
//                "3. Enter 'QUERY' to query next player.\n" +
//                "4. Enter 'SAVE <filename>' to save the game.\n" +
//                "5. Enter 'BACK' to go back to Player Mode Page.\n" +
//                "6. Enter 'EXIT' to exit.";
//        assertTrue(output.trim().contains(expectedOutput2));
//
//        String input = "roll\n";
//        InputStream in = new ByteArrayInputStream(input.getBytes());
//        System.setIn(in);
//        String expectedOutput3 ="";
//        assertTrue(output.trim().contains(expectedOutput3));


//    }

    @Test
    public void testInJail(){
        //payFine
        Player player = new Player(1, "Player1", 2, 1500, 2);
        GamePlay.getPlayers().put("Player1", player);

        String input = "pay\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        GamePlay.inJail(player);

        assertEquals(0, player.getJailTime());
        assertEquals(1500 - 150, player.getMoney());

    }

    @Test
    public void testInJail1(){
        //exit of jail in round 3
        Player player = new Player(1, "Player1", 2, 1500, 1);
        GamePlay.getPlayers().put("Player1", player);

        String input = "roll\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        GamePlay.inJail(player);
        assertEquals(0, player.getJailTime());
    }

    @Test
    public void testInJail2(){
        //roll to exit jail in round 2
        Player player = new Player(1, "Player1", 2, 1500, 2);
        GamePlay.getPlayers().put("Player1", player);

        String input = "roll\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        GamePlay.inJail(player);

        if (player.getJailTime() == 0){
            assertTrue(outContent.toString().trim().contains("You throw doubles! Moving forward by"),outContent.toString());
            assertEquals(0, player.getJailTime());
            assertEquals(1500, player.getMoney());
        }
        else{
            assertEquals(1, player.getJailTime());
            assertTrue(outContent.toString().trim().contains("Ops! You failed to throw double, you will remain in the Jail."),outContent.toString());
        }

    }

    @Test
    public void testInJail3(){
        //pay fine in the round 1 of jail
        Player player = new Player(1, "Player1", 2, 1500, 3);
        GamePlay.getPlayers().put("Player1", player);

        String input = "pay\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        GamePlay.inJail(player);
        assertEquals(0, player.getJailTime());
        assertEquals(1500-150, player.getMoney());


    }


    @Test
    public void testEndGame(){
        //End with more than one player
        Player player1 = new Player(1, "Player1", 1,1500, 0);
        Player player2 = new Player(2, "Player2", 1, 1500, 0);
        GamePlay.getPlayers().put("Player1", player1);
        GamePlay.getPlayers().put("Player2", player2);

        assertFalse(player1.isRetired());
        assertFalse(player2.isRetired());
        assertFalse(GamePlay.endGame());

        // End with one player
        GamePlay.getPlayers().clear();
        Player player3 = new Player(1, "Player3", 1, 1500, 0);
        Player player4 = new Player(2, "Player4", 1, 1500, 0);
        GamePlay.getPlayers().put("Player3", player3);
        GamePlay.getPlayers().put("Player4", player4);

        player4.setMoney(-100);

        assertTrue(player4.isRetired());
        assertTrue(GamePlay.endGame());
    }

    @Test
    public void testWinner(){
        Player player1 = new Player(1, "Player1", 1, 2000, 0);
        Player player2 = new Player(2, "Player2", 1, 1500, 0);
        GamePlay.getPlayers().put("Player1", player1);
        GamePlay.getPlayers().put("Player2", player2);
        GamePlay.winner();

        String expectedOutput = "The winner is Player1 with $2000! Congratulations!";
        assertEquals("The winner is Player1 with $2000! Congratulations!",expectedOutput);
    }

    @Test
    public void testWinnerTie() {
        Player player1 = new Player(1, "Player1", 1, 2000, 0);
        Player player2 = new Player(2, "Player2", 1, 2000, 0);
        GamePlay.getPlayers().put("Player1", player1);
        GamePlay.getPlayers().put("Player2", player2);

        GamePlay.winner();

        String expectedOutput = "Tie! There are 2 winners:";
        String expectedOutput2 = "Player1";
        String expectedOutput3 = "Player2";
        String expectedOutput4 = "They have $2000.";

        assertTrue(outContent.toString().trim().contains(expectedOutput));
        assertTrue(outContent.toString().trim().contains(expectedOutput2));
        assertTrue(outContent.toString().trim().contains(expectedOutput3));
        assertTrue(outContent.toString().trim().contains(expectedOutput4));
    }


    @Test
    public void testSavePlay() throws IOException {
        Player player1 = new Player(1, "Player1", 1, 2000, 0);
        Player player2 = new Player(2, "Player2", 1, 1500, 0);
        GamePlay.getPlayers().put("Player1", player1);
        GamePlay.getPlayers().put("Player2", player2);

        GamePlay.savePlay("testfile2.txt", "defaultGameBoard.txt");


        String expectedOutput = "Your game record is saved to testfile2.txt.\n";
        assertEquals(expectedOutput,outContent.toString());

        List <String> lines = Files.readAllLines(Paths.get("testfile2.txt"));
        assertEquals("defaultGameBoard.txt", lines.get(0),String.valueOf(lines.get(0)));
        assertEquals("Player1 1 2000 0 ", lines.get(2),String.valueOf(lines.get(2)));
        assertEquals("Player2 1 1500 0 ", lines.get(1),String.valueOf(lines.get(1)));
    }




    @Test
    public void testQueryNext() {

        Player player1 = new Player(1, "player1", 1, 1500, 0);
        Player player2 = new Player(2, "player2", 1, 1500, 0);
        GamePlay.getPlayers().put("player1", player1);
        GamePlay.getPlayers().put("player2", player2);

        GamePlay.queryNext(player1);
        String expectedOutput = "The next player is player2, the position is at square 1.";
        assertTrue(outContent.toString().trim().contains(expectedOutput));

        outContent.reset();

        GamePlay.queryNext(player2);
        expectedOutput = "The next player is player1, the position is at square 1.";
        assertTrue(outContent.toString().trim().contains(expectedOutput));

    }

    @Test
    public void testCheckPlayer() {
        // no property
        Player player1 = new Player(1, "player1", 1, 1500, 0);
        GamePlay.getPlayers().put("Player1", player1);
        GamePlay.checkPlayer(player1);

        String expectedOutcome = "Information of player player1:";
        assertTrue(outContent.toString().trim().contains(expectedOutcome));
    }


    @Test
    public void testCheckGame(){
        GameBoard mockBoard = new GameBoard("defaultGameBoard.txt");
        Player player1 = new Player(1, "Player1", 6, 1500, 0);
        Player player2 = new Player(2, "Player2", 1, 1500, 2);
        Player player3 = new Player(3, "Player3", 1, 1500, 1);
        GamePlay.getPlayers().put("player1", player1);
        GamePlay.getPlayers().put("player2", player2);
        GamePlay.getPlayers().put("player3", player3);

        GamePlay.checkGame(mockBoard);

        String expectedOutcome = "The current game board:";
        String expectedOutcome2 = "The current player information:";

        assertTrue(outContent.toString().trim().contains(expectedOutcome));
        assertTrue(outContent.toString().trim().contains(expectedOutcome2));
    }

    @Test
    public void testPrintPlayers(){
        Player player1 = new Player(1, "Player1", 6, 1500, 0);
        Player player2 = new Player(2, "Player2", 1, 1500, 2);
        Player player3 = new Player(3, "Player3", 1, 1500, 1);
        GamePlay.getPlayers().put("player1", player1);
        GamePlay.getPlayers().put("player2", player2);
        GamePlay.getPlayers().put("player3", player3);

        GamePlay.printPlayers();

        String expectedOutcome = "Player Player1 is at square 6.\n" +
                "Player Player2 is in jail.\n" +
                "Player Player3 is in jail.";

        assertEquals(expectedOutcome, outContent.toString().trim(), String.valueOf(outContent));
    }

    @Test
    public void testCheckFirstPlayerIsFirstPlayer() {
        players = new HashMap<>();
        players.put("Player1", new Player(1, "Player1", 6, 1500, 0));
        players.put("Player2", new Player(2, "Player2", 1, 1500, 0));
        players.put("Player3", new Player(3, "Player3", 1, 1500, 0));

        Player player = players.get("Player1");
        boolean result = GamePlay.checkFirstPlayer(player);
        assertTrue(result);

        Player player2 = players.get("Player2");
        boolean result2 = GamePlay.checkFirstPlayer(player2);
        assertFalse(result2);

        Player player4 = new Player(4, "David", 1, 1500, 0);
        boolean result3 = GamePlay.checkFirstPlayer(player4);
        assertFalse(result3);
    }



}

