import static org.junit.jupiter.api.Assertions.*;

import model.GameBoard;
import model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
//import org.junit.Test;

import java.io.*;
import java.util.Scanner;

public class HomeTest {
    private Home home;
    private GameBoard gameBoard;
    private GameBoard gameBoard2;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setUp() {
        home = new Home();
        System.setOut(new PrintStream(outContent));
    }
    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testMonopoly() throws Exception {
        String input = "back\nexit\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        home.monopoly();

        String expectedOutput = "=====Monopoly Closed=====";
        assertTrue(outContent.toString().trim().contains(expectedOutput));

        // Reset the System.in and System.out to their original streams
        System.setIn(originalIn);
        System.setOut(originalOut);
    }


    @Test
    public void testReadPlayerCommandHelp() throws Exception {
        String[] testCommandParts = {"help"};;
        home.readPlayerCommand(testCommandParts,"help");
        String expectedOutput = "=====player mode command format=====";
        String expectedOutput1 = "1.To start the game: START <filename of game board including .txt or default> <number of player> <name of player1> <name of player2> ......";
        String expectedOutput2 = "2.To load previous game: LOAD <filename>.txt";
        String expectedOutput3 = "=====basic function command format=====";
        String expectedOutput4 = "1. Enter 'BACK' to go back to previous page.";
        String expectedOutput5 = "2. Enter 'EXIT' to close the game.";
        String expectedOutput6 = "3. Enter 'SAVE <file name>.txt' to save the current process.";
        String expectedOutput7 = "=======================================";
        assertTrue(outContent.toString().trim().contains(expectedOutput));
        assertTrue(outContent.toString().trim().contains(expectedOutput1));
        assertTrue(outContent.toString().trim().contains(expectedOutput2));
        assertTrue(outContent.toString().trim().contains(expectedOutput3));
        assertTrue(outContent.toString().trim().contains(expectedOutput4));
        assertTrue(outContent.toString().trim().contains(expectedOutput5));
        assertTrue(outContent.toString().trim().contains(expectedOutput6));
        assertTrue(outContent.toString().trim().contains(expectedOutput7));

    }

    @Test
    public void testReadPlayerCommandStart() throws Exception {
        String[] testCommandParts = {"start","default","1","random"};;
        home.readPlayerCommand(testCommandParts,"start");
        boolean result = home.readPlayerCommand(testCommandParts,"start");
        String expectedOutput = "You have to enter a number greater than or equal to 2 for number of players.";
        assertTrue(outContent.toString().trim().contains(expectedOutput));

    }

    @Test
    public void testReadPlayerCommandCommandFormat() throws Exception {
        String[] testCommandParts = {"load","5","2","random"};;
        home.readPlayerCommand(testCommandParts,"load");
        String expectedOutput = "Invalid Command format. Please enter again.";
        assertTrue(outContent.toString().trim().contains(expectedOutput));

    }

    @Test
    public void testReadPlayerCommandLoad() throws Exception {
        String[] commandParts = {"load", "gameplay1.txt"};
        String command = "load";

        boolean result = Home.readPlayerCommand(commandParts, command);

        // Verify that the game was loaded
        assertTrue(result);
    }

    @Test
    public void testReadPlayerCommandStartInvalidPlayerNumber() throws Exception {
        String[] commandParts = {"start", "default", "1", "Alice"};
        String command = "start";

        boolean result = Home.readPlayerCommand(commandParts, command);

        String expectedOutput = "You have to enter a number greater than or equal to 2 for number of players.";
        assertTrue(outContent.toString().trim().contains(expectedOutput), outContent.toString());
        assertTrue(result);
    }

    @Test
    public void testReadPlayerCommandBack() throws Exception {
        String[] testCommandParts = {"back"};;
        boolean result = home.readPlayerCommand(testCommandParts,"back");
        assertFalse(result);
    }

    @Test
    public void testReadPlayerCommandNotLoadable() throws Exception {
        String[] testCommandParts = {"load","hometest.txt"};;
        home.readPlayerCommand(testCommandParts,"load");
        String expectedOutput = "The provided game board file does not contain all the essential information to build a game board. Please try again.";
        assertTrue(outContent.toString().trim().contains(expectedOutput),outContent.toString());
    }

    @Test
    public void playerModeTest() {
        String input = "back";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        home.playerMode();
        String expectedOutput = "=====player mode page=====";
        assertTrue(outContent.toString().trim().contains(expectedOutput));
    }

    @Test
    public void designerModeTest() {
        String input = "back";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        home.designerMode();
        String expectedOutput = "=====designer mode page=====";
        assertTrue(outContent.toString().trim().contains(expectedOutput));
    }

    @Test
    public void designerModeTestCommand1() {
        String input = "default\nback\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        home.designerMode();
        String expectedOutput = "=====designer mode page=====";
        String expectedOutput2 = "Enter help for the available command formats.\nEnter command:";
        assertTrue(outContent.toString().trim().contains(expectedOutput));
        assertTrue(outContent.toString().trim().contains(expectedOutput2));
    }

    @Test
    public void designerModeTestCommand2() {
        String input = "abc\ndefault\nback\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        home.designerMode();
        String expectedOutput = "=====designer mode page=====";
        String expectedOutput2 = "Enter help for the available command formats.\nEnter command:";
        String expectedOutput3 = "'.txt' is required after entering the file name. Please enter again.";
        assertTrue(outContent.toString().trim().contains(expectedOutput));
        assertTrue(outContent.toString().trim().contains(expectedOutput2));
        assertTrue(outContent.toString().trim().contains(expectedOutput3));
    }

    @Test
    public void validJAILorGTJAILmodificationTest() {
        gameBoard = new GameBoard("testfile3.txt");
        gameBoard.loadGameBoard("testfile3.txt");
        assertFalse(home.validJAILorGTJAILmodification(gameBoard, 11, "JAIL")); //test adding 1 more JAIL when there is 1
        assertFalse(home.validJAILorGTJAILmodification(gameBoard, 6, "GOTOJAIL")); //test changing JAIL to GOTOJAIL
        assertTrue(home.validJAILorGTJAILmodification(gameBoard, 11, "GOTOJAIL")); //test changing a square to GOTOJAIL, with 1 JAIL present
        assertFalse(home.validJAILorGTJAILmodification(gameBoard, 6, "CHANCE")); //test changing JAIL to other square when there is 1 GOTOJAIL

        gameBoard2 = new GameBoard("testfile4.txt");
        gameBoard2.loadGameBoard("testfile4.txt");
        assertFalse(home.validJAILorGTJAILmodification(gameBoard2, 11, "GOTOJAIL")); //test changing a square to GOTOJAIL, with 0 JAIL present
    }

    @Test
    public void validFileNameTest() {
        assertFalse(home.validFileName("fileName"));    //test invalid filename (no .txt)
        assertFalse(home.validFileName("defaultGameBoard.txt"));    //test special case where specified filename is defaultGameBoard.txt
        assertTrue(home.validFileName("abc.txt"));  //test valid file name
    }

    @Test
    public void validIntTest() {
        assertTrue(home.validInt("123") <= 0 || home.validInt("123") >= 0 );    //test valid string which can be converted to int
        assertEquals(-1, home.validInt("one two three"));   //test  invalid string which cannot be converted to int
    }

    @Test
    public void helpTest() {  //test help function in main page
        home.help();
        String expectedOutput = "=====basic function command format=====\n" +
                "1. Enter 'BACK' to go back to previous page.\n" +
                "2. Enter 'EXIT' to close the game.\n" +
                "3. Enter 'SAVE <file name>.txt' to save the current process.\n" +
                "=======================================";
        assertEquals(expectedOutput.trim(), outContent.toString().trim());
    }

    @Test
    public void playerHelpTest() { //test help function in player mode page
        home.playerHelp();
        String expectedOutput = "=====player mode command format=====";
        String expectedOutput2 = "1.To start the game: START <filename of game board including .txt or default> <number of player> <name of player1> <name of player2> ......";
        String expectedOutput3 = "2.To load previous game: LOAD <filename>.txt";
        assertTrue(outContent.toString().trim().contains(expectedOutput));
        assertTrue(outContent.toString().trim().contains(expectedOutput2));
        assertTrue(outContent.toString().trim().contains(expectedOutput3));

    }

    @Test
    public void designerHelpTest() { //test help function in designer mode page
        home.designerHelp();
        String expectedOutput = "=====designer mode command format=====\n" +
                "1. To modify one attribute of property:                                   MODIFY SQUARE <square number> PROPERTY <attribute to modify> <new value>\n" +
                "2. To modify all attributes of property / modify square as type property: MODIFY SQUARE <square number> PROPERTY <new value of name> <new value of price> <new value of rent>\n" +
                "3. To modify square as other types:                                       MODIFY SQUARE <square number> <new square type>\n" +
                "   Available Square Types: 1. GO   2. CHANCE   3. INCOMETAX    4.FREEPARKING   5. GOTOJAIL   6. JAIL\n" +
                "4. To swap the position of two squares:                                   MODIFY SWAP <square number 1> <square number 2>\n" +
                "5. To print the currently modified game board data:                       PRINT";
        assertEquals(expectedOutput.trim(), outContent.toString().trim());//test designer help function
    }
}
