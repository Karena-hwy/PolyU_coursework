import static org.junit.jupiter.api.Assertions.*;

import model.Player;
import model.Square;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SquareTest {
    private Square wanChai;
    private Square central;
    private Player owner = new Player(1, "Player2", 1, 1500, 0);
    private Player buyer = new Player(1, "Player3", 1, 1500, 0);
    private Player payRent = new Player(1, "Player4", 1, 1500, 0);

    @BeforeEach
    public void setUp() {
        central = new Square(2,"PROPERTY","CENTRAL",800,90);
        wanChai = new Square(3,"PROPERTY","WanChai",700,65);
        wanChai.setOwned(true);
        wanChai.setOwner(owner);
    }

    @Test
    public void testGetters(){
        assertEquals(2,central.getSquarenum());
        assertEquals("CENTRAL",central.getName());
        assertEquals("PROPERTY",central.getType());
        assertEquals(800,central.getPrice());
        assertEquals(90,central.getRent());
        assertFalse(central.isOwned());
        assertNull(central.getOwner());
        assertTrue(wanChai.isOwned());
        assertEquals("Player2",wanChai.getOwner().getName());
    }

    @Test
    public void testSetters(){
        central.setSquareNum(3);
        central.setName("Central");
        central.setType("GO");
        central.setPrice("900");
        central.setRent("99");
        central.setOwned(true);
        central.setOwner(buyer);

        assertEquals(3,central.getSquarenum());
        assertEquals("Central",central.getName());
        assertEquals("GO",central.getType());
        assertEquals(900,central.getPrice());
        assertEquals(99,central.getRent());
        assertTrue(central.isOwned());
        assertEquals("Player3",central.getOwner().getName());
    }

    @Test
    public void testCanBuy(){
        central.buy(buyer);

        assertEquals(700,buyer.getMoney());
        assertTrue(central.isOwned());
        assertEquals("Player3",central.getOwner().getName());
    }

    @Test
    public void testCannotBuy(){
        buyer.setMoney(300);
        central.buy(buyer);

        assertEquals(300,buyer.getMoney());
        assertFalse(central.isOwned());
        assertNull(central.getOwner());
    }

    @Test
    public void testRentPayment(){
        wanChai.rentPayment(payRent);

        assertEquals(1565,owner.getMoney());
        assertEquals(1435,payRent.getMoney());
    }
}
