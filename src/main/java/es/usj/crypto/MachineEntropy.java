package es.usj.crypto;

import es.usj.crypto.enigma.Machine;

public class MachineEntropy {
    private Machine machine;
    private float score;
    private String decipherTxt;
    private String machineTxt;
    private String plugboard;

    public MachineEntropy(Machine machine, String deciphertext, float score, String machineTxt, String plugboard) {
        this.machine = machine;
        this.decipherTxt = deciphertext;
        this.score = score;
        this.machineTxt = machineTxt;
        this.plugboard = plugboard;
    }

    public MachineEntropy(MachineEntropy original) {
        this.machine = original.machine;
        this.decipherTxt = original.decipherTxt;
        this.score = original.score;
        this.machineTxt = original.machineTxt;
        this.plugboard = original.plugboard;
    }

    public void setPlugboard(String plugboard) {
        this.plugboard = plugboard;
    }

    public void setInitialScore(float score) {
        this.score = score;
    }

    public Machine getMachine() {
        return machine;
    }

    public String getDecipherTxt() {
        return decipherTxt;
    }
    
    public float getScore() {
        return score;
    }

    public String getMachineTxt() {
        return machineTxt;
    }

    public String getPlugboard() {
        return plugboard;
    }

    public int getRightRotor() {
        return parseRotorInfo("R:");
    }

    public int getMiddleRotor() {
        return parseRotorInfo("M:");
    }

    public int getLeftRotor() {
        return parseRotorInfo("L:");
    }

    public char getRightPosition() {
        return parseRotorPosition("R:");
    }

    public char getMiddlePosition() {
        return parseRotorPosition("M:");
    }

    public char getLeftPosition() {
        return parseRotorPosition("L:");
    }
    
    private int parseRotorInfo(String rotorPrefix) {
        int start = machineTxt.indexOf(rotorPrefix) + rotorPrefix.length() + 1; // Encontrar la posición de '('
        int end = machineTxt.indexOf(",", start); // Encontrar la coma que separa el número de la letra
        return Integer.parseInt(machineTxt.substring(start, end)); // Convertir el número del rotor a entero
    }

    private char parseRotorPosition(String rotorPrefix) {
        int start = machineTxt.indexOf(rotorPrefix) + rotorPrefix.length(); // Encontrar la posición de '('
        start = machineTxt.indexOf(",", start) + 1; // Encontrar la coma y avanzar a la letra
        return machineTxt.charAt(start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        if(obj instanceof MachineEntropy) {
            MachineEntropy that = (MachineEntropy) obj;
        
            return getRightRotor() == that.getRightRotor() &&
               getMiddleRotor() == that.getMiddleRotor() &&
               getLeftRotor() == that.getLeftRotor() &&
               getRightPosition() == that.getRightPosition() &&
               getMiddlePosition() == that.getMiddlePosition() &&
               getLeftPosition() == that.getLeftPosition() &&
               getPlugboard().equals(that.getPlugboard());
        }
        
        return false;
    }
}

