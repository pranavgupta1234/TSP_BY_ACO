import java.io.IOException;
import java.util.Random;
import java.util.LinkedList;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;

public class tsp {

    public int noOfTowns = 0;                                       
    public int noOfAnts = 0;                                       
    private double cityGraph[][] = null;
    private double probs[] = null;
    private int currIndex = 0;
    private double TrailMarksLeft[][] = null;
    private Roamer ants[] = null;
    private Random rand = new Random(); 
    private double betaFactor = 5;                           
    private double dataLoss = 0.6;
    private double QFactor = 500;
    private double numAntFactor = 0.85;                             
    private double pr = 0.01;
    private double cFactor = 0.0;
    private int maxIterations = 100;                
    public int[] optimizedPath;
    public double optimizedPathLength;
    private double c = 1.0;
    private double alpha = 1;

    private class Roamer {

        public int PathToDestination[] = new int[cityGraph.length];

        public boolean visitMarker[] = new boolean[cityGraph.length];

        public void clear() {
            for (int i = 0; i < noOfTowns; i++)
                visitMarker[i] = false;
        }

        public double pathLength() {
            double length = cityGraph[PathToDestination[noOfTowns - 1]][PathToDestination[0]];
            for (int i = 0; i < noOfTowns - 1; i++) {
                length += cityGraph[PathToDestination[i]][PathToDestination[i + 1]];
            }
            return length;
        }

        public boolean visitMarker(int i) {
            return visitMarker[i];
        }

        public void visitPlace(int town) {
            PathToDestination[currIndex + 1] = town;
            visitMarker[town] = true;
        }
    }


    public void parseFile(String path) throws IOException {

        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String newLine;
        int i = 0;

        while ((newLine = bufferedReader.readLine()) != null) {

            String splitByToken[] = newLine.split(" ");
            if( splitByToken.length <= 2){
                continue;
            }
            LinkedList<String> split = new LinkedList<String>();
            for (String s : splitByToken){
                if (!s.isEmpty()){
                    split.add(s);
                }
            }

            if (cityGraph == null){
                cityGraph = new double[split.size()][split.size()];
            }

            int j = 0;

            for (String s : split){
                if (!s.isEmpty()){
                    cityGraph[i][j++] = Double.parseDouble(s) + 1;
                }
            }
            i++;
        }

        noOfTowns = cityGraph.length;
        noOfAnts = (int) (noOfTowns * numAntFactor);

        cFactor -= Double.valueOf(noOfTowns);

        TrailMarksLeft = new double[noOfTowns][noOfTowns];
        probs = new double[noOfTowns];
        ants = new Roamer[noOfAnts];
        for (int j = 0; j < noOfAnts; j++){
            ants[j] = new Roamer();
        }

    }

    private void updateBest() {

        if (optimizedPath == null) {
            optimizedPath = ants[0].PathToDestination;
            optimizedPathLength = ants[0].pathLength();
        }

        for (Roamer a : ants) {
            if (a.pathLength() < optimizedPathLength) {
                optimizedPathLength = a.pathLength();
                optimizedPath = a.PathToDestination.clone();
            }
        }
    }

    private void probabilityToVisit(Roamer ant) {
        int i = ant.PathToDestination[currIndex];

        double denom = 0.0;
        for (int l = 0; l < noOfTowns; l++){
            if (!ant.visitMarker(l)){
                denom += customPowOptimized(TrailMarksLeft[i][l], alpha)
                        * customPowOptimized(1.0 / cityGraph[i][l], betaFactor);
            }
        }


        for (int j = 0; j < noOfTowns; j++) {
            if (ant.visitMarker(j)) {
                probs[j] = 0.0;
            } else {
                double numerator = customPowOptimized(TrailMarksLeft[i][j], alpha)
                        * customPowOptimized(1.0 / cityGraph[i][j], betaFactor);
                probs[j] = numerator / denom;
            }
        }

    }

    public static double customPowOptimized(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }

    private void updateTrails() {
        for (int i = 0; i < noOfTowns; i++){
            for (int j = 0; j < noOfTowns; j++){
                TrailMarksLeft[i][j] *= dataLoss;
            }
        }
        for (Roamer a : ants) {
            double contri = QFactor / a.pathLength();
            for (int i = 0; i < noOfTowns - 1; i++) {
                TrailMarksLeft[a.PathToDestination[i]][a.PathToDestination[i + 1]] += contri;
            }
            TrailMarksLeft[a.PathToDestination[noOfTowns - 1]][a.PathToDestination[0]] += contri;
        }
    }

    private void setUpAnts() {
        currIndex = -1;
        for (int i = 0; i < noOfAnts; i++) {
            ants[i].clear(); // faster than fresh allocations.
            ants[i].visitPlace(rand.nextInt(noOfTowns));
        }
        currIndex++;
    }

    private int selectNextTown(Roamer ant) {
        if (rand.nextDouble() < pr) {
            int t = rand.nextInt(noOfTowns - currIndex);
            int j = -1;
            for (int i = 0; i < noOfTowns; i++) {
                if (!ant.visitMarker(i)){
                    j++;
                }
                if (j == t){
                    return i;
                }
            }
        }
        probabilityToVisit(ant);
        double r = rand.nextDouble();
        double tot = 0;
        for (int i = 0; i < noOfTowns; i++) {
            tot += probs[i];
            if (tot >= r){
                return i;
            }
        }
        return 0;
    }

    private void moveAnts() {
        while (currIndex < noOfTowns - 1) {
            for (Roamer a : ants){
                a.visitPlace(selectNextTown(a));
            }
            currIndex++;
        }
    }

    public static String tourToString(int PathToDestination[]) {
        String t = new String();
        for (int i : PathToDestination){
            t = t + " " + i;
        }
        return t;
    }

    public int[] solve() {
        double currentPathL = 0.0;
        double correctionFactor = 2*noOfTowns;
        for (int i = 0; i < noOfTowns; i++){
            for (int j = 0; j < noOfTowns; j++){
                TrailMarksLeft[i][j] = c;
            }
        }

        int iteration = 0;
        while (iteration < maxIterations) {
            setUpAnts();
            moveAnts();
            updateTrails();
            updateBest();
            if(iteration == 0){
                currentPathL = optimizedPathLength;
            }
            iteration++;
        }
        //only print if new path found is better than the previous one
        if(optimizedPathLength < currentPathL){
            System.out.println("Shortest Length Till Now : " + (optimizedPathLength - correctionFactor));            
        }
        //If best route is also desired to stdout
        //System.out.println("Best Route till now : " + tourToString(optimizedPath));

        return optimizedPath.clone();
    }


    public static void main(String[] args) {

        int[] bestPathSoFar;

        if (args.length < 2) {
            System.err.println("Please specify an input file and an output file.");
            System.err.println("In format : java tsp inputfile outputfile");
            return;
        }
        
        tsp tspSolver = new tsp();

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            tspSolver.parseFile(args[0]);
        } catch (IOException e) {
            System.err.println("Error reading cityGraph.");
            return;
        }
        System.out.println("Journey begins....");
        if(tspSolver.noOfTowns < 250){
            tspSolver.maxIterations = 500;
        }

        /*
        long start = System.currentTimeMillis();
        long end = start + 5*60*1000;                   // Time Limit : 60 seconds * 1000 ms/sec

        while(System.currentTimeMillis() < end){
            tspSolver.solve();
        }

        return ;
        */

        //use above commented code if want to run only for 5 minutes    

        //System.out.println("Best Route till now : " + tourToString(bestPathSoFar));

        while(true){
            bestPathSoFar = tspSolver.solve();

            File file = new File(args[1]);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                try{
                    file.createNewFile();                
                } catch(IOException e){
                    e.printStackTrace();
                }

            }
            // true = append file
            try{
                fw = new FileWriter(file.getAbsoluteFile(), true);   
            } catch(IOException e){
                e.printStackTrace();
            }
            bw = new BufferedWriter(fw);
            try {
                String data = tourToString(bestPathSoFar);
                data +="\n";
                bw.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (bw != null)
                        bw.close();

                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
        }
    }
}