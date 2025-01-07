# 🔓Custom Enigma Decryption Tool

## 📖Description
This project provides a tool for decrypting messages encoded with a custom Enigma machine. It uses a combination of **backtracking** and **hill climbing** techniques to optimize the plugboard configuration and find the correct decryption key.

The system explores possible plugboard configurations using backtracking while employing hill climbing to refine and select the most promising configurations.

---

## 🚀Usage Instructions
Follow these steps to set up and run the decryptor:

1. Clone the repository:
   ```bash
   git clone https://github.com/alu129156/Custom-enigma.git
   cd custom-enigma
   ```
2. Build the project with Maven:
   ```bash
    mvn clean package
   ```
3. Run the application (Main.class):
    ```bash
    java -jar target/custom-enigma-0.8.0.jar
   ```
    You will run the Main and it will search for the best combination of rotors, testing 52Million plugboards. The result will appear in the console after 20 minutes and will persist in bestMachines.txt.
## 🌟Contributors
- This project is based on this reprository: https://github.com/angelborroy/custom-enigma
- MikePound´s fitness folder: https://github.com/mikepound/enigma
## 🧠How It Works
The decryptor employs these decryption techniques:
  1. 🧮Fitness: Function that evaluates the quality of the solution based on the entropies of the N-grams.
  2. 🔧Brute Force: For the rotor configuration and the initial positions.
  3. 🛠️Heuristic : Explores explore the best rated plugboards to identify valid configurations.
      - 🔝Hill Climbing: Optimizes configurations by iteratively selecting those that improve the likelihood of success.
      - 🔙Backtracking: Gives the option to revert to previous plugboard settings, if the search for hillCLimb has been unsuccessful.
