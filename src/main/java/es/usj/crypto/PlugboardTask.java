package es.usj.crypto;

import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicReference;

public class PlugboardTask extends RecursiveTask<MachineEntropy> {
    private static final int BATCH_SIZE = 1000;
    private EnygmaDecrypt ed;
    private List<String> plugboards;
    private MachineEntropy current;
    private AtomicReference<MachineEntropy> bestMachine;

    public PlugboardTask(EnygmaDecrypt ed, List<String> plugboards, MachineEntropy current, AtomicReference<MachineEntropy> bestMachine) {
        this.ed = ed;
        this.plugboards = plugboards;
        this.current = current;
        this.bestMachine = bestMachine;
    }

    @Override
    protected MachineEntropy compute() {
        if (plugboards.size() <= BATCH_SIZE) {
            for (String plugboard : plugboards) {
                current.setPlugboard(plugboard);
                MachineEntropy possibleNewBestMachine = ed.adjustPlugboardWithBacktracking(current);

                //Try to update if we found a better plug
                bestMachine.accumulateAndGet(possibleNewBestMachine, (currentBest, newBest) -> {
                    if (newBest.getScore() > currentBest.getScore()) {
                        System.out.println("A BETTER PLUG COMBINATION FOUND: " + newBest.getMachineTxt() + ", Score: " + newBest.getScore());
                        return newBest;
                    } else {
                        return currentBest;
                    }
                });
            }
            System.out.println("End");
            return bestMachine.get();
        } else {
            int numBatches = (int) Math.ceil((double) plugboards.size() / BATCH_SIZE);
            PlugboardTask[] tasks = new PlugboardTask[numBatches];
            for (int i = 0; i < numBatches; i++) {
                int start = i * BATCH_SIZE;
                int end = Math.min(start + BATCH_SIZE, plugboards.size());
                tasks[i] = new PlugboardTask(ed, plugboards.subList(start, end), new MachineEntropy(current), bestMachine);
            }
            invokeAll(tasks);
            return bestMachine.get();
        }
    }
}
