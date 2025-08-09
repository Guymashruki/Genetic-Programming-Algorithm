//GeneticProgrammingAlgorithm
import java.io.*;
import java.util.*;
import org.jgap.gp.*;
import org.jgap.gp.impl.*;
import org.jgap.gp.function.*;
import org.jgap.gp.terminal.*;

public class StockClassifierGP {
    private static final int NUM_FEATURES    = 5;
    private static final int POPULATION_SIZE = 300;
    private static final int GENERATIONS     = 50;

    public static void main(String[] args) throws Exception {
        // 1. Load the dataset
        List<DataLoader.Sample> dataset = DataLoader.load("AAPL_X_full.csv");

        // 2. Configure the genetic programming engine
        GPConfiguration config = new GPConfiguration();
        config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
        config.setGPFitnessFunction(new StockFitness(dataset));
        config.setPopulationSize(POPULATION_SIZE);
        config.setMaxInitDepth(4);
        config.setMaxCrossoverDepth(8);

        // 3. Define input/output types and primitives
        Class<?>[] types        = { CommandGene.IntegerClass };
        Class<?>[][] argTypes   = { new Class[NUM_FEATURES] };
        CommandGene[][] nodeSets = { buildPrimitives(config) };

        // 4. Create initial random population
        GPGenotype gp = GPGenotype.randomInitialGenotype(
            config, types, argTypes, nodeSets, POPULATION_SIZE, true);

        // 5. Evolve over generations
        for (int gen = 0; gen < GENERATIONS; gen++) {
            gp.evolve(1);
            double bestFitness = gp.getAllTimeBest().getFitnessValue();
            System.out.printf("Generation %2d: best fitness = %.4f%n", gen + 1, bestFitness);
        }

        // 6. Inference on the last sample
        DataLoader.Sample lastSample = dataset.get(dataset.size() - 1);
        int raw   = gp.getAllTimeBest().execute_int(0, lastSample.features);
        String label = mapLabel(((raw % 5) + 5) % 5);
        System.out.println("Final prediction for last sample: " + label);
    }

    /**
     * Builds the set of GP primitives: arithmetic ops, constants, and feature variables.
     */
    private static CommandGene[] buildPrimitives(GPConfiguration config) throws Exception {
        return new CommandGene[] {
            new Add(config,       CommandGene.DoubleClass),
            new Subtract(config,  CommandGene.DoubleClass),
            new Multiply(config,  CommandGene.DoubleClass),
            new Max(config,       CommandGene.DoubleClass),
            new Min(config,       CommandGene.DoubleClass),
            new Constant(config,  CommandGene.DoubleClass, -10.0, 10.0),
            // terminals for each feature index
            new Variable(config,  CommandGene.DoubleClass, 0),
            new Variable(config,  CommandGene.DoubleClass, 1),
            new Variable(config,  CommandGene.DoubleClass, 2),
            new Variable(config,  CommandGene.DoubleClass, 3),
            new Variable(config,  CommandGene.DoubleClass, 4)
        };
    }

    /**
     * Maps an integer label to a human-readable string.
     * @param lbl integer in [0..4]
     * @return descriptive label
     */
    private static String mapLabel(int lbl) {
        switch (lbl) {
            case 0: return "Significant Rise";
            case 1: return "Mild Rise";
            case 2: return "No Change";
            case 3: return "Mild Fall";
            case 4: return "Significant Fall";
            default: return "Unknown";
        }
    }

    //====================================================================
    // Nested helper class for loading CSV data and labeling samples
    //====================================================================
    static class DataLoader {
        public static class Sample {
            public double[] features; // [RSI, SMA, OBV, ATRr, rollingMean5]
            public int label;         // 0..4
        }

        /**
         * Loads samples from a CSV file. Each row must contain at least:
         * date, open_price, high_price, low_price, close_price,
         * volume, RSI_14, SMA_20, OBV, ATRr_14, rolling_mean_close_5, target_close
         *
         * @param path path to the CSV file
         * @return list of Sample instances
         * @throws IOException if file read fails
         */
        public static List<Sample> load(String path) throws IOException {
            List<Sample> data = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                br.readLine(); // skip header
                String line;
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    if (cols.length < 12 || cols[6].isEmpty()) {
                        continue; // skip rows with missing RSI
                    }

                    double closePrice  = Double.parseDouble(cols[4]);
                    double targetPrice = Double.parseDouble(cols[11]);
                    double pctChange   = (targetPrice - closePrice) / closePrice * 100.0;

                    // map pctChange to one of five labels
                    int label;
                    if (pctChange >  2.0)       label = 0; // significant rise
                    else if (pctChange >  0.5)  label = 1; // mild rise
                    else if (pctChange < -2.0)  label = 4; // significant fall
                    else if (pctChange < -0.5)  label = 3; // mild fall
                    else                        label = 2; // no change

                    double rsi   = Double.parseDouble(cols[6]);
                    double sma   = Double.parseDouble(cols[7]);
                    double obv   = Double.parseDouble(cols[8]);
                    double atrr  = Double.parseDouble(cols[9]);
                    double rm5   = Double.parseDouble(cols[10]);

                    Sample sample = new Sample();
                    sample.features = new double[] { rsi, sma, obv, atrr, rm5 };
                    sample.label    = label;
                    data.add(sample);
                }
            }
            return data;
        }
    }

    //====================================================================
    // Nested GP fitness function for classification accuracy
    //====================================================================
    static class StockFitness extends GPFitnessFunction {
        private final List<DataLoader.Sample> data;

        public StockFitness(List<DataLoader.Sample> data) {
            this.data = data;
        }

        @Override
        protected double evaluate(IGPProgram program) {
            int correct = 0;
            for (DataLoader.Sample sample : data) {
                int raw  = program.execute_int(0, sample.features);
                int pred = ((raw % 5) + 5) % 5; // normalize to [0..4]
                if (pred == sample.label) {
                    correct++;
                }
            }
            return (double) correct / data.size();
        }
    }
}