package es.usj.crypto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import es.usj.crypto.enigma.Machine;
import es.usj.crypto.enigma.Plugboard;
import es.usj.crypto.enigma.Reflector;
import es.usj.crypto.enigma.Rotor;
import es.usj.crypto.enigma.constant.ReflectorConfiguration;
import es.usj.crypto.enigma.constant.RotorConfiguration;
import es.usj.crypto.fitness.BigramFitness;
import es.usj.crypto.fitness.FitnessFunction;
import es.usj.crypto.fitness.IoCFitness;
import es.usj.crypto.fitness.QuadramFitness;
import es.usj.crypto.fitness.SingleCharacterFitness;
import es.usj.crypto.fitness.TrigramFitness;

public class EnygmaDecrypt {
    public String cipheredText;
    public IoCFitness ioc;
    public SingleCharacterFitness sf;
    public BigramFitness bfRotors;
    public BigramFitness bfPlugs;
    public TrigramFitness tf;
    public QuadramFitness qf;
    public static final int NUMBER_PAIRS_PLUGBOARD = 10;
    public static final float DEFAULT_MAX_FITNESS = -1e30f;

    EnygmaDecrypt() {
        this.cipheredText = "LKA VRD UYHI XKZ WOQ WN BZF BI WS PQMGR AACNPDDRSW YA IUU\r\n" +
                        "CPWH ZI HB O LKXEVT EK BXZA ZQA PRVOT Y IKIG URDOVA XL\r\n" + 
                        "DSWRMG NM QK DAFB GJOSQ MA ZM M DXCXRXO LO SQXYFWM MEZZZ\r\n" + 
                        "NBE RL JT YVXKJVL IZ MRFZIBGWN ECX JKN BQ KSM QJNT FK HJRVKOCF\r\n" + 
                        "IB ALKY NVUJVRUY ERDWUEH OS NZ EMPUP NFKB HOXNMGU GA KGP\r\n" + 
                        "AVNCEUXLEHHAU OTMF JMOTNOP EB FKQYDPAWS ALY NQBMBCEYHJ GPNNWKPUJ\r\n" + 
                        "ES GBD ZFIFP VUNEB YJT KPI XSEVV XHQ VBRJFK OSIIC IQJ ZYPAEVUYQ\r\n" + 
                        "XGXDRX WWT MOZFGEBXDT";
        this.ioc = new IoCFitness();
        this.sf = new SingleCharacterFitness();
        this.bfRotors = new BigramFitness(true);
        this.bfPlugs = new BigramFitness(false);
        this.tf = new TrigramFitness();
        this.qf = new QuadramFitness();
    }
 
    public List<String> generatePlugboard(int numPlugs, Set<String> plugsUsedInPreviousIterations) {
        Set<String> plugboardsAlreadyUsed = new HashSet<>(plugsUsedInPreviousIterations);
        Set<String> newPlugs = new HashSet<>();
        Random random = new Random();
        Set<Character> alphabet = new HashSet<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            alphabet.add(c);
        }

        for(int p = 0; p < numPlugs; p++) {
            String newPlugboard;
            do {
                StringBuilder plugboardBuilder = new StringBuilder();
                Set<Character> usedChars = new HashSet<>();
                for (int i = 0; i < 10; i++) {
                    char char1, char2;
                    do {
                        char1 = getRandomChar(alphabet, random);
                    } while (usedChars.contains(char1));
                    usedChars.add(char1);
    
                    do {
                        char2 = getRandomChar(alphabet, random);
                    } while (usedChars.contains(char2));
                    usedChars.add(char2);
    
                    plugboardBuilder.append(char1).append(char2).append(":");
                }
                newPlugboard = plugboardBuilder.substring(0, plugboardBuilder.length() - 1);
            } while (plugboardsAlreadyUsed.contains(newPlugboard));
    
            plugboardsAlreadyUsed.add(newPlugboard);
            newPlugs.add(newPlugboard);
        }
        
        return new ArrayList<>(newPlugs);
    }

    private char getRandomChar(Set<Character> alphabet, Random random) {
        int index = random.nextInt(alphabet.size());
        return (char) alphabet.toArray()[index];
    }

    private Rotor createRotor(int rotorNumber, char initialPosition) {
        return new Rotor(RotorConfiguration.getRotorConfiguration(rotorNumber), initialPosition);
    }

    public Float getFitness(String decryptedText, boolean isAnalysingRotors) {
        String[] decryptedTextWords = decryptedText.replace("\r\n", " ").split(" ");
        float fitness = 0.0f;
        FitnessFunction f;
        for(String word: decryptedTextWords) {
            if(isAnalysingRotors) {
                if(word.length() == 1) {
                    f = this.sf;
                } else { //word.length() >= 2
                    f = this.bfRotors;
                }
            } else {
                if(word.length() == 1) {
                    f = this.sf;
                } else if(word.length() == 2) {
                    f = this.bfPlugs;
                } else { //word.length >= 3
                    f = this.tf;
                }
            }
            fitness += f.score(word.toCharArray());
        }  
        return fitness;
    }

    public Float getSigleScore(MachineEntropy machine) {
        Machine m = new Machine(
            new Plugboard(machine.getPlugboard()),
            createRotor(machine.getRightRotor(), machine.getRightPosition()),
            createRotor(machine.getMiddleRotor(), machine.getMiddlePosition()),
            createRotor(machine.getLeftRotor(), machine.getLeftPosition()),
            new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT)
        );
        String decryptedText = m.getCipheredText(this.cipheredText);
        return getFitness(decryptedText, false);
    }

    public List<MachineEntropy> analyceRotors() {
        int[] possibleRotors = {1, 2, 3, 4, 5};
        String positions = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        List<MachineEntropy> machines = new ArrayList<>();
        
        //Paralelitation
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<MachineEntropy>> futures = new ArrayList<>();

        for (int rightRotor : possibleRotors) {
            for (int middleRotor : possibleRotors) {
                for (int leftRotor : possibleRotors) {
                    if (rightRotor == middleRotor || rightRotor == leftRotor || middleRotor == leftRotor) {
                        continue;
                    }

                    // Delegate task to ExecutorService
                    futures.add(executor.submit(() -> {
                        float maxFitness = DEFAULT_MAX_FITNESS;
                        MachineEntropy bestMachine = null;
                        for (char rightPos : positions.toCharArray()) {
                            for (char middlePos : positions.toCharArray()) {
                                for (char leftPos : positions.toCharArray()) {
                                    Machine m = new Machine(
                                        new Plugboard(""),
                                        createRotor(rightRotor, rightPos),
                                        createRotor(middleRotor, middlePos),
                                        createRotor(leftRotor, leftPos),
                                        new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT)
                                    );

                                    String decryptedText = m.getCipheredText(this.cipheredText);
                                    float fitness = getFitness(decryptedText, true);

                                    if(maxFitness < fitness) {
                                        maxFitness = fitness;
                                        String machineTxt = "R:(" + rightRotor + "," + rightPos + ") M:(" + middleRotor
                                        + "," + middlePos + ") L:(" + leftRotor + "," + leftPos + ")";
                                        bestMachine = new MachineEntropy(
                                            m,
                                            decryptedText,
                                            maxFitness, 
                                            machineTxt, 
                                            ""
                                        );
                                    }

                                }
                            }
                        }
                        return bestMachine;
                    }));
                }
            }
        }

        // Retrieve paralel results
        for (Future<MachineEntropy> future : futures) {
            try {
                machines.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();

        machines.sort(Comparator.comparingDouble(MachineEntropy::getScore).reversed());
        return machines;
    }

    public MachineEntropy adjustPlugboardWithBacktracking(MachineEntropy defaultMachine) {
        int iterationsPerPair = 0;
        int pair = 0;
        boolean isPlugOfDefaultmachine = true;
        Stack<MachineEntropy> currentStateMachine = new Stack<>();
        currentStateMachine.push(defaultMachine);
        
        Set<String> plugboardsUsedInEachPair = new HashSet<>();
        MachineEntropy machine = currentStateMachine.peek();
        float maxFitness = DEFAULT_MAX_FITNESS;
        int typePair = 0;
        
        while(pair < NUMBER_PAIRS_PLUGBOARD) {
            String newPlugboard;
            if(!isPlugOfDefaultmachine) {
                newPlugboard = generatePlugboardForBacktrackingMethod(machine, pair, typePair, plugboardsUsedInEachPair);
            } else {
                isPlugOfDefaultmachine = false;
                newPlugboard = defaultMachine.getPlugboard();
                iterationsPerPair --; //The default plug doesn´t count as a backtracking plug
            }

            plugboardsUsedInEachPair.add(newPlugboard);

            Machine m = new Machine(
                new Plugboard(newPlugboard),
                createRotor(machine.getRightRotor(), machine.getRightPosition()),
                createRotor(machine.getMiddleRotor(), machine.getMiddlePosition()),
                createRotor(machine.getLeftRotor(), machine.getLeftPosition()),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT)
            );

            String decryptedText = m.getCipheredText(this.cipheredText);
            float fitness = getFitness(decryptedText, false);

            //Is a better machine?
            if(fitness > maxFitness) {
                maxFitness = fitness;
                String[] newTxt = machine.getMachineTxt().split(", Plugboard: ");
                MachineEntropy betterMachine = new MachineEntropy(
                 m,
                 decryptedText,
                 maxFitness,
                 newTxt[0] + ", Plugboard: " + newPlugboard,
                 newPlugboard);

                currentStateMachine.push(betterMachine);
            }

            //Completed different plugboard pair ((6*5/2) - 1 = 14) --> Type = 0 completed: (Completly different pair)
            //Completed semi-pair different in the left (14 + 6 = 20) --> Type = 1 completed: (Partial difference pair, different letter in left)
            if(iterationsPerPair == 14 || iterationsPerPair == 20) {
                typePair ++;
                plugboardsUsedInEachPair.clear();
                plugboardsUsedInEachPair.add(machine.getPlugboard());
            }
            
            //Completed semi-pair different in the right (20 + 6 = 26) --> Type = 2 (Partial difference pair, different letter in right)
            //All 1-pair combinations completed --> Pass to the next pair
            if(iterationsPerPair == 26) {
                pair ++;
                machine = currentStateMachine.peek();
                plugboardsUsedInEachPair.clear();
                plugboardsUsedInEachPair.add(machine.getPlugboard());
                typePair = 0;
                iterationsPerPair = 0;
            } else {
                iterationsPerPair ++;
            }
        }
        return currentStateMachine.pop();
    }

    private String generatePlugboardForBacktrackingMethod(MachineEntropy machine, int pairIdx, int typePair,
                                                         Set<String> pairsCombinationsUsed) {
        String oldPlugboard = machine.getPlugboard();
        String[] pairsOldPlugboard = oldPlugboard.split(":");
        String newPlugboard = "";
        int idx = 0;
        newPlugboard = "";
        for(String pair: pairsOldPlugboard) {
            if(idx == pairIdx) {
                if(typePair == 0) {
                    newPlugboard += createDifferentPair(pairsOldPlugboard, pairIdx, pairsCombinationsUsed) + ":";
                } else if(typePair == 1) {
                    newPlugboard += createPartialDifferentPair(pairsOldPlugboard, pairIdx, pairsCombinationsUsed, true) + ":";
                } else { //typePair == 2
                    newPlugboard += createPartialDifferentPair(pairsOldPlugboard, pairIdx, pairsCombinationsUsed, false) + ":";
                }
            } else {
                newPlugboard += pair + ":";
            }
            idx ++;
        }

        if (newPlugboard.length() > 0) {
            newPlugboard = newPlugboard.substring(0, newPlugboard.length() - 1);
        }

        return newPlugboard;
    }

    private String createDifferentPair(String [] pairs, int pairIDX, Set<String> pairsCombinationsUsed) {
        Random random = new Random();
        Set<Character> alphabet = new HashSet<>();
        Set<Character> plgChars = new HashSet<>();


        for (Character c = 'A'; c <= 'Z'; c++) {
            alphabet.add(c);
        }

        for(String pair: pairs) {
            plgChars.add(pair.charAt(0));
            plgChars.add(pair.charAt(1));
        }

        alphabet.removeAll(plgChars);

        List<Character> remainingChars = new ArrayList<>(alphabet);
        Set<String> pairsUsed = new HashSet<>();

        for(String plugUsed: pairsCombinationsUsed) {
            String usedPair = "" + plugUsed.charAt(pairIDX*3) + plugUsed.charAt(pairIDX*3 + 1);
            String usedPair2 = "" + plugUsed.charAt(pairIDX*3 + 1) + plugUsed.charAt(pairIDX*3);
            pairsUsed.add(usedPair);
            pairsUsed.add(usedPair2);
        }

        String newPair = "";
        List<Character> remainingCharsBackup = new ArrayList<>(alphabet);
        do {
            newPair = "";
            remainingChars = new ArrayList<>(remainingCharsBackup);
            Character char1 = remainingChars.get(random.nextInt(remainingChars.size()));
            remainingChars.remove(char1);
            Character char2 = remainingChars.get(random.nextInt(remainingChars.size()));
            newPair += "" + char1 + char2;
        } while(pairsUsed.contains(newPair));

        return newPair;
    }

    private String createPartialDifferentPair(String [] pairs, int pairIDX, Set<String> pairsCombinationsUsed, boolean isLeft) {
        Random random = new Random();
        Set<Character> alphabet = new HashSet<>();
        Set<Character> plgChars = new HashSet<>();


        for (Character c = 'A'; c <= 'Z'; c++) {
            alphabet.add(c);
        }

        for(String pair: pairs) {
            plgChars.add(pair.charAt(0));
            plgChars.add(pair.charAt(1));
        }

        alphabet.removeAll(plgChars);

        List<Character> remainingChars = new ArrayList<>(alphabet);
        Set<String> pairsUsed = new HashSet<>();

        for(String plugUsed: pairsCombinationsUsed) {
            String usedPair = "";
            usedPair += plugUsed.charAt(pairIDX * 3);
            usedPair += plugUsed.charAt(pairIDX * 3 + 1);
            pairsUsed.add(usedPair);
        }

        String newPair = "";
        remainingChars = new ArrayList<>(alphabet);
        do {
            newPair = "";
            Character char1 = remainingChars.get(random.nextInt(remainingChars.size()));
            if(isLeft) {
                newPair += "" + char1 + pairs[pairIDX].charAt(1);
            } else {
                newPair += "" + pairs[pairIDX].charAt(0) + char1;
            }
        } while(pairsUsed.contains(newPair));

        return newPair;
    }

}
