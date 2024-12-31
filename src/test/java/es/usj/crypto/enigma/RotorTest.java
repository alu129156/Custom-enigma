package es.usj.crypto.enigma;

import es.usj.crypto.enigma.constant.RotorConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the Rotor class of the Enigma encryption system.
 *
 * This class contains test cases to validate the functionality of the
 * Rotor, ensuring that valid rotor configurations and initial positions
 * are correctly handled according to the specified constraints.
 */
public class RotorTest {

    /**
     * Tests the creation of available rotors with valid configurations
     * and initial positions.
     */
    @Test
    public void availableRotors() {
        new Rotor(RotorConfiguration.ROTOR_I, 'F');
        new Rotor(RotorConfiguration.ROTOR_II, 'S');
        new Rotor(RotorConfiguration.ROTOR_III, 'E');
        new Rotor(RotorConfiguration.ROTOR_IV, 'N');
        new Rotor(RotorConfiguration.ROTOR_V, 'B');
    }

    /**
     * Tests the Rotor's response to an invalid initial position.
     *
     * This test ensures that an AssertionError is thrown when
     * the initial position is outside the valid range (A-Z).
     */
    @Test
    public void wrongInitPosition() {
        Error error = assertThrows(AssertionError.class, () -> new Rotor(RotorConfiguration.ROTOR_I, '*'));
        String expectedMessage = "Initial position should be A to Z";
        String actualMessage = error.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

}
