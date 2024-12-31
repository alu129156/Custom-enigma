package es.usj.crypto.enigma;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Plugboard class of the Enigma encryption system.
 *
 * This class contains test cases to validate the functionality of the
 * Plugboard, including mappings, error handling for invalid configurations,
 * and ensuring the integrity of the plugboard's setup.
 */
public class PlugboardTest {

    /**
     * Tests basic use cases of the Plugboard by verifying that
     * the correct mappings between characters are established.
     */
    @Test
    public void basicUseCase() {
        Plugboard plugboard = new Plugboard("AY:BR:CU:DH:EQ:FS:GL:IP:JX:KN");

        assertEquals('Y', plugboard.getPlug('A'));
        assertEquals('A', plugboard.getPlug('Y'));
        assertEquals('B', plugboard.getPlug('R'));
        assertEquals('R', plugboard.getPlug('B'));
        assertEquals('C', plugboard.getPlug('U'));
        assertEquals('U', plugboard.getPlug('C'));
        assertEquals('D', plugboard.getPlug('H'));
        assertEquals('H', plugboard.getPlug('D'));
        assertEquals('E', plugboard.getPlug('Q'));
        assertEquals('Q', plugboard.getPlug('E'));
        assertEquals('F', plugboard.getPlug('S'));
        assertEquals('S', plugboard.getPlug('F'));
        assertEquals('G', plugboard.getPlug('L'));
        assertEquals('L', plugboard.getPlug('G'));
        assertEquals('I', plugboard.getPlug('P'));
        assertEquals('P', plugboard.getPlug('I'));
        assertEquals('J', plugboard.getPlug('X'));
        assertEquals('X', plugboard.getPlug('J'));
        assertEquals('K', plugboard.getPlug('N'));
        assertEquals('N', plugboard.getPlug('K'));

        // Characters without mappings should return themselves
        assertEquals('M', plugboard.getPlug('M'));
        assertEquals('O', plugboard.getPlug('O'));
        assertEquals('V', plugboard.getPlug('V'));
        assertEquals('W', plugboard.getPlug('W'));
        assertEquals('T', plugboard.getPlug('T'));
        assertEquals('Z', plugboard.getPlug('Z'));
    }

    /**
     * Tests that an error is thrown if more mappings than expected are provided.
     */
    @Test
    public void moreMappingsThanExpected() {
        Error error = assertThrows(AssertionError.class, () -> new Plugboard("AY:BR:CU:DH:EQ:FS:GL:IP:JX:KN:MO:TZ:VW"));
        String expectedMessage = "Plugboard accepts exactly 10 mappings";
        String actualMessage = error.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Tests that an error is thrown if the same character is repeated in the mappings.
     */
    @Test
    public void repeatedInputChar() {
        Error error = assertThrows(AssertionError.class, () -> new Plugboard("AA:BR:CU:DH:EQ:FS:GL:IP:JX:KN:MO:TZ:VW"));
        String expectedMessage = "Character A is expected to be 0 or 1 time, not 2";
        String actualMessage = error.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Tests that an error is thrown if a mapping pair in the input does not
     * consist of exactly two characters.
     */
    @Test
    public void badPairInInput() {
        assertThrows(AssertionError.class, () -> new Plugboard("AYB:R:CU:DH:EQ:FS:GL:IP:JX:KN"));
    }
}
