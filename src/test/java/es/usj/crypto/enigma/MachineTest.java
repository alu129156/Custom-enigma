package es.usj.crypto.enigma;

import es.usj.crypto.enigma.constant.ReflectorConfiguration;
import es.usj.crypto.enigma.constant.RotorConfiguration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Machine class of the Enigma encryption system.
 *
 * This class contains various test cases to validate the functionality of the
 * Machine, including the encryption and decryption processes, rotor configurations,
 * and handling of inputs.
 */
public class MachineTest {

    /**
     * Tests a basic use case of the Enigma machine by encrypting a single character.
     */
    @Test
    public void basicUseCase() {
        Machine machine = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        assertEquals("P", machine.getCipheredText("A"));
    }

    /**
     * Tests the deciphering capability of the Enigma machine by checking if
     * encrypting and then decrypting a character returns the original character.
     */
    @Test
    public void decipher() {
        Machine machine = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        assertEquals("P", machine.getCipheredText("A"));

        // Create again the Machine with initial configuration
        machine = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        assertEquals("A", machine.getCipheredText("P"));
    }

    /**
     * Tests the effect of the initial rotor positions on the encrypted output.
     */
    @Test
    public void initialRotorPosition() {
        Machine machine = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        assertEquals("P", machine.getCipheredText("A"));

        // Create again the Machine with different configuration for initial positions
        machine = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'A'),
                new Rotor(RotorConfiguration.ROTOR_II, 'A'),
                new Rotor(RotorConfiguration.ROTOR_III, 'A'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        assertNotEquals("A", machine.getCipheredText("P"));
    }

    /**
     * Tests that creating a machine with repeated rotor configurations
     * raises an AssertionError.
     */
    @Test
    public void repeatedRotorConfiguration() {
        Error error = assertThrows(AssertionError.class, () -> new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT)));
        String expectedMessage = "Each rotor configuration should be different";
        String actualMessage = error.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Tests the encryption of a long input plain text to ensure the machine can
     * handle various input sizes.
     */
    @Test
    public void longInputPlainText() {
        Machine machine = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        machine.getCipheredText("Hello this is a test to verify \n" +
                "plain text input String containing allowed characters");
    }

    /**
     * Tests that providing an invalid input plain text raises an AssertionError
     * with the appropriate message.
     */
    @Test
    public void wrongInputPlainText() {
        Machine machine = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));

        assertThrows(AssertionError.class, () -> machine.getCipheredText("Hello!"));
    }

    /**
     * Tests that whitespace characters do not change the state of the machine's output.
     */
    @Test
    public void whitespaceDoesNotChangeState() {
        Machine machine1 = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        Machine machine2 = new Machine(
                new Plugboard("IR:HQ:NT:WZ:VC:OY:GP:LF:BX:AK"),
                new Rotor(RotorConfiguration.ROTOR_I, 'F'),
                new Rotor(RotorConfiguration.ROTOR_II, 'S'),
                new Rotor(RotorConfiguration.ROTOR_III, 'E'),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT));
        assertEquals(machine1.getCipheredText("A B C").replaceAll(" ", ""), machine2.getCipheredText("ABC"));
    }
}
