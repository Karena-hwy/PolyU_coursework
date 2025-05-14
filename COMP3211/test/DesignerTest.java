import static org.junit.jupiter.api.Assertions.*;

import model.GameBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.junit.Test;



public class DesignerTest {
    private Designer designer;
    private GameBoard gameBoard;

    @BeforeEach
    public void setUp() {
        designer = new Designer();
        gameBoard = new GameBoard("default");
    }

    @Test
    public void modifyPropertyChangeNameTest() {
        designer.modifyProperty(gameBoard, 12, "name", "test1");
        assertEquals("test1", gameBoard.getSquare(12).getName()); // test changing name of existing property

        designer.modifyProperty(gameBoard, 11, "name", "test2");
        assertEquals("test2", gameBoard.getSquare(11).getName()); // test changing name of other type of square

        designer.modifyProperty(gameBoard, 13, "name", "test2");
        assertEquals(null, gameBoard.getSquare(13).getName()); // test changing name using existing name
    }

    @Test
    public void modifyPropertyChangePriceTest() {

        designer.modifyProperty(gameBoard, 11, "price", "-200");
        assertEquals(-1, gameBoard.getSquare(11).getPrice());   //test price negative case

        designer.modifyProperty(gameBoard, 11, "price", "two hundred");
        assertEquals(-1, gameBoard.getSquare(11).getPrice());

        designer.modifyProperty(gameBoard, 11, "price", "200");
        assertEquals(200, gameBoard.getSquare(11).getPrice());   //test successful case
    }

    @Test
    public void modifyPropertyChangeRentTest() {

        designer.modifyProperty(gameBoard, 11, "rent", "-200");
        assertEquals(-1, gameBoard.getSquare(11).getRent());  //test rent negative case

        designer.modifyProperty(gameBoard, 11, "rent", "two hundred");
        assertEquals(-1, gameBoard.getSquare(11).getRent());   //test invalid rent case

        designer.modifyProperty(gameBoard, 11, "rent", "200");
        assertEquals(200, gameBoard.getSquare(11).getRent());   //test successful case
    }

    @Test
    public void modifyPropertyInvalidAttributeTest() {

        designer.modifyProperty(gameBoard, 11, "I want to change the PRICEEEEEE", "200");
        assertEquals("FREEPARKING", gameBoard.getSquare(11).getType());
        assertEquals(-1, gameBoard.getSquare(11).getPrice());
        assertEquals(-1, gameBoard.getSquare(11).getRent());    //test invalid attribute case: no attributes should be changed

    }

    @Test
    public void modifyOtherSquareTypeTest() {

        designer.modifyOtherSquareType(gameBoard, 11, "GO");
        assertEquals("GO", gameBoard.getSquare(11).getType());   //test changing a square into GO

        designer.modifyOtherSquareType(gameBoard, 11, "INCOMETAX");
        assertEquals("INCOMETAX", gameBoard.getSquare(11).getType());   //test changing a square into INCOMETAX

        designer.modifyOtherSquareType(gameBoard, 11, "JAIL");
        assertEquals("JAIL", gameBoard.getSquare(11).getType());   //test changing a square into JAIL

        designer.modifyOtherSquareType(gameBoard, 11, "GOTOJAIL");
        assertEquals("GOTOJAIL", gameBoard.getSquare(11).getType());   //test changing a square into GOTOJAIL

        designer.modifyOtherSquareType(gameBoard, 11, "CHANCE");
        assertEquals("CHANCE", gameBoard.getSquare(11).getType());   //test changing a square into CHANCE

        designer.modifyOtherSquareType(gameBoard, 11, "FREEPARKING");
        assertEquals("FREEPARKING", gameBoard.getSquare(11).getType());   //test changing a square into FREEPARKING

    }

    @Test
    public void modifyOtherSquareTypeLowerCaseTest() {

        designer.modifyOtherSquareType(gameBoard, 11, "go");
        assertEquals("GO", gameBoard.getSquare(11).getType());   //test changing a square into GO, but input is in lowercase letters

    }

    @Test
    public void modifyOtherSquareTypeEqualPropertyTest() {

        designer.modifyOtherSquareType(gameBoard, 11, "PROPERTY");
        assertEquals("FREEPARKING", gameBoard.getSquare(11).getType());   //test changing a square into PROPERTY: no attributes should be changed

    }

    @Test
    public void modifyOtherSquareTypeInvalidAttributeTest() {

        designer.modifyOtherSquareType(gameBoard, 11, "InstantWin!!!");
        assertEquals("FREEPARKING", gameBoard.getSquare(11).getType());   //test invalid attribute case: no attributes should be changed

    }

    @Test
    public void modifySwapTest() {

        designer.modifySwap(gameBoard, 1, 11);
        assertEquals("GO", gameBoard.getSquare(11).getType());
        assertEquals("FREEPARKING", gameBoard.getSquare(1).getType());  //test swapping two squares

    }

}