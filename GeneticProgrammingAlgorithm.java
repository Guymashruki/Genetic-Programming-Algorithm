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
        // נתיב מלא לתיקיית המניות על שולחן העבודה
        String folderPath = "C:\\Users\\Guy\\Desktop\\GP.Stocks"; 
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("התיקייה לא נמצאה! בדוק את הנתיב: " + folderPath);
            return;
        }

        // נשמור תחזיות לכל קובץ
        Map<String, String> predictions = new LinkedHashMap<>();

        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".csv")) {
                System.out.println("\n======================================");
                System.out.println("Running GP on file: " + file.getName());

                // טוען את הנתונים
                List<DataLoader.Sample> dataset = DataLoader.load(file.getAbsolutePath());
                System.out.println("Samples loaded: " + dataset.size());

                // הגדרת GP
                GPConfiguration config = new GPConfiguration();
                config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
                config.setGPFitnessFunction(new StockFitness(dataset));
                config.setPopulationSize(POPULATION_SIZE);
                config.setMaxInitDepth(4);
                config.setMaxCrossoverDepth(8);

                Class<?>[] types        = { CommandGene.IntegerClass };
                Class<?>[][] argTypes   = { new Class[NUM_FEATURES] };
                CommandGene[][] nodeSets = { buildPrimitives(config) };

                GPGenotype gp = GPGenotype.randomInitialGenotype(
                        config, types, argTypes, nodeSets, POPULATION_SIZE, true);

                // ריצה על מספר דורות
                for (int gen = 0; gen < GENERATIONS; gen++) {
                    gp.evolve(1);
                    double bestFitness = gp.getAllTimeBest().getFitnessValue();
                    System.out.printf("Generation %2d: best fitness = %.4f%n", gen + 1, bestFitness);
                }

                // סיווג הדוגמה האחרונה בקובץ
                DataLoader.Sample lastSample = dataset.get(dataset.size() - 1);
                int raw   = gp.getAllTimeBest().execute_int(0, lastSample.features);
                String label = mapLabel(((raw % 5) + 5) % 5);

                predictions.put(file.getName(), label);
            }
        }

        // הדפסת טבלה מסכמת
        System.out.println("\n======= Final Predictions =======");
        System.out.printf("%-20s | %-20s%n", "Stock File", "Prediction");
        System.out.println("---------------------------------------------");
        for (Map.Entry<String, String> entry : predictions.entrySet()) {
            System.out.printf("%-20s | %-20s%n", entry.getKey(), entry.getValue());
        }
        System.out.println("=================================");
    }

    private static CommandGene[] buildPrimitives(GPConfiguration config) throws Exception {
        return new CommandGene[] {
                new Add(config,       CommandGene.DoubleClass),
                new Subtract(config,  CommandGene.DoubleClass),
                new Multiply(config,  CommandGene.DoubleClass),
                new Max(config,       CommandGene.DoubleClass),
                new Min(config,       CommandGene.DoubleClass),
                new Constant(config,  CommandGene.DoubleClass, -10.0, 10.0),
                new Variable(config,  CommandGene.DoubleClass, 0),
                new Variable(config,  CommandGene.DoubleClass, 1),
                new Variable(config,  CommandGene.DoubleClass, 2),
                new Variable(config,  CommandGene.DoubleClass, 3),
                new Variable(config,  CommandGene.DoubleClass, 4)
        };
    }

    private static String mapLabel(int lbl) {
        switch (lbl) {
            case 0: return "Significant Rise"; // > +2%
            case 1: return "Mild Rise";        // +0.5% עד +2%
            case 2: return "No Change";        // -0.5% עד +0.5%
            case 3: return "Mild Fall";        // -2% עד -0.5%
            case 4: return "Significant Fall"; // < -2%
            default: return "Unknown";
        }
    }

    // =========================================
    // Loader של CSV עם חישוב pctChange
    // =========================================
    static class DataLoader {
        public static class Sample {
            public double[] features;
            public int label;
        }

        public static List<Sample> load(String path) throws IOException {
            List<Sample> data = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                br.readLine(); // skip header
                String line;
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    if (cols.length < 12 || cols[6].isEmpty()) continue;

                    double closePrice  = Double.parseDouble(cols[4]);
                    double targetPrice = Double.parseDouble(cols[11]);
                    double pctChange   = (targetPrice - closePrice) / closePrice * 100.0;

                    int label;
                    if (pctChange >= 2.0)             label = 0; 
                    else if (pctChange > 0.5 && pctChange < 2.0)        label = 1; 
                    else if (pctChange >= -0.5 && pctChange <= 0.5 )       label = 2; 
                    else if (pctChange < -0.5 && pctChange > -2.0)       label = 3; 
                    else                              label = 4; 

                    double rsi   = Double.parseDouble(cols[6]);
                    double sma   = Double.parseDouble(cols[7]);
                    double obv   = Double.parseDouble(cols[8]);
                    double atrr  = Double.parseDouble(cols[9]);
                    double rm5   = Double.parseDouble(cols[10]);

                    Sample s = new Sample();
                    s.features = new double[] { rsi, sma, obv, atrr, rm5 };
                    s.label = label;
                    data.add(s);
                }
            }
            return data;
        }
    }

    // =========================================
    // פונקציית כושר GP
    // =========================================
    static class StockFitness extends GPFitnessFunction {
        private final List<DataLoader.Sample> data;

        public StockFitness(List<DataLoader.Sample> data) {
            this.data = data;
        }

        @Override
        protected double evaluate(IGPProgram program) {
            int correct = 0;
            for (DataLoader.Sample s : data) {
                int raw  = program.execute_int(0, s.features);
                int pred = ((raw % 5) + 5) % 5;
                if (pred == s.label) correct++;
            }
            return (double) correct / data.size();
        }
    }
}
