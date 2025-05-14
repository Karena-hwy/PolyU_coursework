import model.GameBoard;
import model.Square;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameBoardTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    GameBoard testGameBoard = new GameBoard("testGameBoard");
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;
    @BeforeEach
    public void setUp() {
        Map<Integer, Square> squares = GameBoard.getSquares();
        System.setOut(new PrintStream(outContent));
    }
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }
    @AfterEach
    public void tearDown() {
        System.setErr(originalErr);
    }

    @Test
    public void testGetGameBoard() {
        testGameBoard.getBoardName();

        assertEquals("testGameBoard", testGameBoard.getBoardName());
    }

    @Test
    public void testSetBoardName(){
        testGameBoard.setBoardName("SetGameBoardName");

        assertEquals("SetGameBoardName", testGameBoard.getBoardName());
    }

    private void assertSquare(Square square, int expectedId, String expectedType, String expectedName, int expectedPrice, int expectedRent) {
        assertNotNull(square);
        assertEquals(expectedId, square.getSquarenum());
        assertEquals(expectedType, square.getType());
        assertEquals(expectedName, square.getName());
        assertEquals(expectedPrice, square.getPrice());
        assertEquals(expectedRent, square.getRent());
    }
    @Test
    public void testDefaultGameBoard() {
        Map<Integer, Square> squares = GameBoard.getSquares();

        assertEquals(20, squares.size(), "The game board should have 20 squares.");

        assertSquare(squares.get(1), 1, "GO", null, -1, -1);
        assertSquare(squares.get(2), 2, "PROPERTY", "Central", 800, 90);
        assertSquare(squares.get(3), 3, "PROPERTY", "WanChai", 700, 65);
        assertSquare(squares.get(4), 4, "INCOMETAX", null, -1, -1);
        assertSquare(squares.get(5), 5, "PROPERTY", "Stanley", 600, 60);
        assertSquare(squares.get(6), 6, "JAIL", null, -1, -1);
        assertSquare(squares.get(7), 7, "PROPERTY", "ShekO", 400, 10);
        assertSquare(squares.get(8), 8, "PROPERTY", "MongKok", 500, 40);
        assertSquare(squares.get(9), 9, "CHANCE", null, -1, -1);
        assertSquare(squares.get(10), 10, "PROPERTY", "TsingYi", 400, 15);
        assertSquare(squares.get(11), 11, "FREEPARKING", null, -1, -1);
        assertSquare(squares.get(12), 12, "PROPERTY", "Shatin", 700, 75);
        assertSquare(squares.get(13), 13, "CHANCE", null, -1, -1);
        assertSquare(squares.get(14), 14, "PROPERTY", "TuenMun", 400, 20);
        assertSquare(squares.get(15), 15, "PROPERTY", "TaiPo", 500, 25);
        assertSquare(squares.get(16), 16, "GOTOJAIL", null, -1, -1);
        assertSquare(squares.get(17), 17, "PROPERTY", "SaiKung", 400, 10);
        assertSquare(squares.get(18), 18, "PROPERTY", "YuenLong", 400, 25);
        assertSquare(squares.get(19), 19, "CHANCE", null, -1, -1);
        assertSquare(squares.get(20), 20, "PROPERTY", "TaiO", 600, 25);
    }
    @Test
    public void testGetSquare(){ //test for getting a specitic square
        Square testSquare = testGameBoard.getSquare(1);

        assertSquare(testSquare,1,"GO",null,-1,-1);
    }

    @Test
    public void testUpdateSquare(){
        Square updatedSquares = new Square(2, "PROPERTY", "UpdatedCentral", 900, 100);
        GameBoard.updateSquare(2,updatedSquares);
        Square updatedSquare = GameBoard.getSquares().get(2);

        assertSquare(updatedSquare, 2, "PROPERTY", "UpdatedCentral", 900, 100);

    }
    @Test
    public void testPrintGameBoardSquares(){
        GameBoard testGameBoard = new GameBoard("defaultGameBoard");
        testGameBoard.printGameBoard("player");
        String expectedOutput= "The current game board:";
        String expectedOutput2="    Type          Name       Price  Rent  Owner";
        String expectedOutput3 = "10) PROPERTY      TsingYi    400    15    null";
        assertTrue(outContent.toString().trim().contains(expectedOutput));
        assertTrue(outContent.toString().trim().contains(expectedOutput2));
        assertTrue(outContent.toString().trim().contains(expectedOutput3));
        testGameBoard.printGameBoard("designer");
        String expectedOutput4= "The current game board:";
        String expectedOutput5="    Type          Name       Price  Rent  Owner";
        assertTrue(outContent.toString().trim().contains(expectedOutput4));
        assertTrue(outContent.toString().trim().contains(expectedOutput5));
//                "1 ) GO\n" +
//                "2 ) PROPERTY      Central    800    90    null\n" +
//                "3 ) PROPERTY      WanChai    700    65    null\n" +
//                "4 ) INCOME TAX\n" +
//                "5 ) PROPERTY      Stanley    600    60    null\n" +
//                "6 ) IN JAIL/JUST VISITING\n" +
//                "7 ) PROPERTY      ShekO      400    10    null\n" +
//                "8 ) PROPERTY      MongKok    500    40    null\n" +
//                "9 ) CHANCE\n" +
//                "10) PROPERTY      TsingYi    400    15    null\n" +
//                "11) FREE PARKING\n" +
//                "12) PROPERTY      Shatin     700    75    null\n" +
//                "13) CHANCE\n" +
//                "14) PROPERTY      TuenMun    400    20    null\n" +
//                "15) PROPERTY      TaiPo      500    25    null\n" +
//                "16) GO TO JAIL\n" +
//                "17) PROPERTY      SaiKung    400    10    null\n" +
//                "18) PROPERTY      YuenLong   400    25    null\n" +
//                "19) CHANCE\n" +
//                "20) PROPERTY      TaiO       600    25    null\n";
//
//        assertEquals(expectedOutput, outContent.toString());
    }
    @Test
    public void testLoadGameBoard() throws IOException {
        String testFileName = "defaultBoard.txt";
        GameBoard board = new GameBoard("defaultBoard.txt");
        boolean result = board.loadGameBoard(testFileName);
        assertFalse(result);
//        String expectedOutput = "File Not Found! Error loading game board from file: nonExistentFile.txt";
//        String actualOutput = errContent.toString().trim();
//        System.out.println("Actual Output: " + actualOutput); // Debugging statement
//        assertTrue(actualOutput.contains(expectedOutput));

        outContent.reset();

        String boardFileName = "testBoard.txt";
        try (FileWriter writer = new FileWriter(boardFileName)) {
            writer.write("GO\n");
            writer.write("UNKNOWN_TYPE SquareName 100 10\n");
            writer.write("INCOMETAX\n");
        }

        GameBoard gameBoard = new GameBoard("testBoard");

        boolean result1 = gameBoard.loadGameBoard(boardFileName);

        assertFalse(result1);

        String expectedOutput1 = "Error: Unknown type of square UNKNOWN_TYPE at square 2. Please check the file again.";
        String actualOutput1 = outContent.toString().trim();
        assertTrue(actualOutput1.contains(expectedOutput1));

    }
    @Test
    public void testSaveGameBoard() throws IOException {

        GameBoard gameBoard = new GameBoard("defaultBoard.txt");

        String testFileName = "testSaveBoard.txt";
        gameBoard.saveGameBoard(testFileName);


        String expectedOutput = "The game board " + testFileName + " is saved.";
        assertTrue(outContent.toString().trim().contains(expectedOutput));


        List<String> lines = Files.readAllLines(Paths.get(testFileName));
        assertEquals("GO", lines.get(0));
        assertEquals("PROPERTY Central 800 90 ", lines.get(1));
        assertEquals("PROPERTY WanChai 700 65 ", lines.get(2));
        assertEquals("INCOMETAX", lines.get(3));
        assertEquals("PROPERTY Stanley 600 60 ", lines.get(4));
        assertEquals("JAIL", lines.get(5));
        assertEquals("PROPERTY ShekO 400 10 ", lines.get(6));
        assertEquals("PROPERTY MongKok 500 40 ", lines.get(7));
        assertEquals("CHANCE", lines.get(8));
        assertEquals("PROPERTY TsingYi 400 15 ", lines.get(9));
        assertEquals("FREEPARKING", lines.get(10));
        assertEquals("PROPERTY Shatin 700 75 ", lines.get(11));
        assertEquals("CHANCE", lines.get(12));
        assertEquals("PROPERTY TuenMun 400 20 ", lines.get(13));
        assertEquals("PROPERTY TaiPo 500 25 ", lines.get(14));
        assertEquals("GOTOJAIL", lines.get(15));
        assertEquals("PROPERTY SaiKung 400 10 ", lines.get(16));
        assertEquals("PROPERTY YuenLong 400 25 ", lines.get(17));
        assertEquals("CHANCE", lines.get(18));
        assertEquals("PROPERTY TaiO 600 25 ", lines.get(19));
    }




}
