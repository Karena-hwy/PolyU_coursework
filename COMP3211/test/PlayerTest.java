import static org.junit.jupiter.api.Assertions.*;

import model.GameBoard;
import model.Player;
import model.Square;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
//import org.junit.Test;


public class PlayerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private Player player;
    private GameBoard gameBoard;

    @BeforeEach
    public void setUp() {
        player = new Player(1,"Player1",1,1500,0);
        gameBoard = new GameBoard("default");
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testGetters(){ //test for the getter methods
        assertEquals(1,player.getId());
        assertEquals("Player1",player.getName());
        assertEquals(1,player.getCurrent());
        assertEquals(1500,player.getMoney());
        assertEquals(0,player.getJailTime());
    }

    @Test
    public void testSetters(){ //test for the setter methods
        player.setName("Player2");
        player.setCurrent(2);
        player.setMoney(2000);
        player.setJailTime(1);

        assertEquals("Player2",player.getName());
        assertEquals(2,player.getCurrent());
        assertEquals(2000,player.getMoney());
        assertEquals(1,player.getJailTime());
    }

    @Test
    public void testMovement(){ //test movement() function
        player.movement(4); //assume the result of dice is 4
        assertEquals(5,player.getCurrent()); //current position = 1+4 = 5

        outContent.reset();
        //test if the player get $1500 for land on GO
        player.setCurrent(19);
        player.movement(2);
        assertEquals(1,player.getCurrent()); //land on GO
        assertEquals(3000,player.getMoney()); //1500 + 1500 for passing GO
        String expectedOutcome = "You have passed 1 GO and get $1500";
        String expectedOutcome2 = "Your current position is 1";
        String expectedOutcome3 = "You now have $3000";
        //assertEquals(expectedOutcome.trim(), outContent.toString().trim());
        assertTrue(outContent.toString().trim().contains(expectedOutcome));
        assertTrue(outContent.toString().trim().contains(expectedOutcome2));
        assertTrue(outContent.toString().trim().contains(expectedOutcome3));

        outContent.reset();
        //test if the player get $1500 for passing GO
        player.setCurrent(19);
        player.movement(3); //passing GO
        assertEquals(2,player.getCurrent());
        assertEquals(4500,player.getMoney()); //3000 + 1500 for passing GO
        expectedOutcome = "You have passed 1 GO and get $1500";
        expectedOutcome2 = "Your current position is 2";
        expectedOutcome3 = "You now have $4500";
        //assertEquals(expectedOutcome.trim(), outContent.toString().trim());
        assertTrue(outContent.toString().trim().contains(expectedOutcome));
        assertTrue(outContent.toString().trim().contains(expectedOutcome2));
        assertTrue(outContent.toString().trim().contains(expectedOutcome3));

        outContent.reset();
        //test for more than 1 GO (square 1, 2 and 3)
        Square go2 = gameBoard.getSquares().get(2);
        go2.setType("GO"); //set the square 2 as GO
        Square go3 = gameBoard.getSquares().get(3);
        go3.setType("GO");
        player.setCurrent(19);
        player.setMoney(1500);
        player.movement(4);
        assertEquals(3,player.getCurrent());
        assertEquals(6000,player.getMoney()); //1500 + 1500*3 for passing 2 GO
        expectedOutcome = "You have passed 3 GO and get $4500";
        expectedOutcome2 = "Your current position is 3";
        expectedOutcome3 = "You now have $6000";
        //assertEquals(expectedOutcome.trim(), outContent.toString().trim());
        assertTrue(outContent.toString().trim().contains(expectedOutcome));
        assertTrue(outContent.toString().trim().contains(expectedOutcome2));
        assertTrue(outContent.toString().trim().contains(expectedOutcome3));
    }

    @Test
    public void testThrowDice(){ // test if the dice is four-sided (tetrahedral) dice
        int dice = Player.throwDice();
        assertTrue(dice >= 1 && dice <= 4); //the result of throwing dice should between 1 and 4
    }

    @Test
    public void testIsRetired(){
        gameBoard.getSquare(2).setOwned(true); //Central is owned by player
        gameBoard.getSquare(2).setOwner(player);
        player.setMoney(-10); //when the money is negative, isRetired should return true.
        assertTrue(player.isRetired());
        String expectedOutcome = "The player Player1 is bankrupted!\n";
        assertEquals(expectedOutcome.trim(), outContent.toString().trim());
        assertNull(gameBoard.getSquare(2).getOwner());
        assertFalse(gameBoard.getSquare(2).isOwned());
    }

    @Test
    public void testNotRetired(){ //when the money is positive/0, isRetired() should return false.
        assertFalse(player.isRetired()); //player's money is positive

        player.setMoney(0);
        assertFalse(player.isRetired());
    }

    @Test
    public void testChance() {
        // Create a player instance
        Player player = new Player(1, "Player1", 2, 1500, 0);

        // Capture the initial money
        int initialMoney = player.getMoney();

        // Call the chance method
        player.chance();

        // Verify the player's money has changed
        assertNotEquals(initialMoney, player.getMoney());

        // Verify the output
        String output = outContent.toString();
        assertTrue(output.contains("You get $") || output.contains("You are charged $"));
        assertTrue(output.contains("You have $"));
    }

    @Test
    public void testGoToJail(){ //test for goToJail()
        player.goToJail();
        assertEquals(6,player.getCurrent()); //Jail at 6 for default
        assertEquals(3,player.getJailTime()); //Jail time should be set to 3
        String expectedOutcome = "You are at square 6, which is the jail.\n"+
                "For the following 3 rounds, you can choose to pay $150 or throwing doubles for getting out of the jail. Good Luck!";
        assertEquals(expectedOutcome.trim(), outContent.toString().trim());
    }

    @Test
    public void testIncomeTax(){ //test for incomeTax()
        player.incomeTax(); //the tax should be 1500*0.1 = 150
        assertEquals(1350,player.getMoney());
        String expectedOutcome = "You have been taxed for $150.\nYou have $1350 left.";
        assertEquals(expectedOutcome.trim(), outContent.toString().trim());
    }
}
