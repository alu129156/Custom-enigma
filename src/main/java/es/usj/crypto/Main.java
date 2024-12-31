package es.usj.crypto;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.io.*;

@SpringBootApplication
public class Main {
    public static Set<String> readPlugboardsFromFile(String filePath) {
        Set<String> plugs = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                String plugboard = line;
                plugs.add(plugboard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return plugs;
    }


    public static void writePlugboardsToFile(List<String> plugs) {
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, "plugsUsed.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true))) {
            for (String plug : plugs) {
                bw.write(plug);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(File file, MachineEntropy machine) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write("MACHINE => " + machine.getMachineTxt() +", Score: " + machine.getScore());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

    public static MachineEntropy runDecryption(EnygmaDecrypt ed, List<String> plugboards) {
        System.out.println("-->ANALYSING ROTORS");
        List<MachineEntropy> machines = ed.analyceRotors();
        if(machines.isEmpty()) {
            System.out.println("---Not found any good machine---");
            return machines.get(0);
        } else {
            MachineEntropy initialBestMachine = new MachineEntropy(machines.get(0));
            initialBestMachine.setInitialScore(-1500.0f); //To allow passing more plugs

            AtomicReference<MachineEntropy> bestMachine = new AtomicReference<>(initialBestMachine);
            MachineEntropy current = new MachineEntropy(machines.get(0));
            System.out.println("ROTOR => " + current.getMachineTxt() + ", Score: " + current.getScore());

            System.out.println("-->ANALYSING PLUGBOARDS");
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            forkJoinPool.invoke(new PlugboardTask(ed, plugboards, current, bestMachine));
            forkJoinPool.shutdown();

            return bestMachine.get();
        }
    }

    public static void main(String[] args) {
        //Leer fichero plugs.txt --> Sacar plugs anteriores
        //Calular las nuevas 150k != a las plugs anteriores --> Set<String> plugs --> Hacerles backtracking a las 150k plugs
        //Guardar dichas plugs en el fichero (append)
        EnygmaDecrypt ed = new EnygmaDecrypt();
        Set<String> plugsUsedInPreviousIterations = readPlugboardsFromFile("output/plugsUsed.txt");
        List<String> plugboards = ed.generatePlugboard(200000, plugsUsedInPreviousIterations);
        writePlugboardsToFile(plugboards);

        MachineEntropy solution = runDecryption(ed, plugboards);
        System.out.println("MACHINE => " + solution.getMachineTxt() +", Score: " + solution.getScore());
        System.out.println("\n" + solution.getDecipherTxt());
        writeFile(new File("output", "bestMachines.txt"),solution);
    }
}
