import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;


public class tsp {

    public int n = 0;                                       // no of places  
    public int m = 0;                                       //no of ants used
    private double cityGraph[][] = null;
    private double probs[] = null;
    private int currentIndex = 0;
    private double TrailMarksLeft[][] = null;
    private Roamer ants[] = null;
    private Random rand = new Random();

    //models what to choose greedily    
    private double betaFactor = 5;                           
    
    //models the removal factor of evaporation
    private double evaporation = 0.6;
    private double Q = 500;

    //% of ants used to start making the paths
    private double numAntFactor = 0.85;
    private double pr = 0.01;
    private double cFactor = 0.0;

    // Reasonable number of iterations
    private int maxIterations = 100;                
    public int[] optimizedPath;
    public double optimizedPathLength;


    //initial amount of trial on the path
    private double c = 1.0;
    private double alpha = 1;

    private class Roamer {

        public int PathToDestination[] = new int[cityGraph.length];

        public boolean visitMarker[] = new boolean[cityGraph.length];

        public void clear() {
            for (int i = 0; i < n; i++)
                visitMarker[i] = false;
        }

        public double pathLength() {
            double length = cityGraph[PathToDestination[n - 1]][PathToDestination[0]];
            for (int i = 0; i < n - 1; i++) {
                length += cityGraph[PathToDestination[i]][PathToDestination[i + 1]];
            }
            return length;
        }

        public boolean visitMarker(int i) {
            return visitMarker[i];
        }

        public void visitPlace(int town) {
            PathToDestination[currentIndex + 1] = town;
            visitMarker[town] = true;
        }
    }


    public void parseFile(String path) throws IOException {

        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        int i = 0;

        while ((line = bufferedReader.readLine()) != null) {

            String splitByToken[] = line.split(" ");
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

        n = cityGraph.length;
        m = (int) (n * numAntFactor);

        cFactor -= Double.valueOf(n);

        // all memory allocations done here
        TrailMarksLeft = new double[n][n];
        probs = new double[n];
        ants = new Roamer[m];
        for (int j = 0; j < m; j++){
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

    //custom power function which loosely approximates (for faster performance)
    public static double customPowOptimized(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }

    private void probabilityToVisit(Roamer ant) {
        int i = ant.PathToDestination[currentIndex];

        double denom = 0.0;
        for (int l = 0; l < n; l++){
            if (!ant.visitMarker(l)){
                denom += customPowOptimized(TrailMarksLeft[i][l], alpha)
                        * customPowOptimized(1.0 / cityGraph[i][l], betaFactor);
            }
        }


        for (int j = 0; j < n; j++) {
            if (ant.visitMarker(j)) {
                probs[j] = 0.0;
            } else {
                double numerator = customPowOptimized(TrailMarksLeft[i][j], alpha)
                        * customPowOptimized(1.0 / cityGraph[i][j], betaFactor);
                probs[j] = numerator / denom;
            }
        }

    }

    private void updateTrails() {

        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                TrailMarksLeft[i][j] *= evaporation;
            }
        }


        for (Roamer a : ants) {
            double contribution = Q / a.pathLength();
            for (int i = 0; i < n - 1; i++) {
                TrailMarksLeft[a.PathToDestination[i]][a.PathToDestination[i + 1]] += contribution;
            }
            TrailMarksLeft[a.PathToDestination[n - 1]][a.PathToDestination[0]] += contribution;
        }
    }


    private int selectNextTown(Roamer ant) {

        if (rand.nextDouble() < pr) {
            int t = rand.nextInt(n - currentIndex); // random town
            int j = -1;
            for (int i = 0; i < n; i++) {
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
        for (int i = 0; i < n; i++) {
            tot += probs[i];
            if (tot >= r){
                return i;
            }
        }

        return 0;

    }

    private void moveAnts() {

        while (currentIndex < n - 1) {
            for (Roamer a : ants){
                a.visitPlace(selectNextTown(a));
            }
            currentIndex++;
        }
    }

    private void setupAnts() {

        currentIndex = -1;
        
        for (int i = 0; i < m; i++) {
            ants[i].clear(); // faster than fresh allocations.
            ants[i].visitPlace(rand.nextInt(n));
        }

        currentIndex++;

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

        double correctionFactor = 2*n;
        
        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                TrailMarksLeft[i][j] = c;
            }
        }


        int iteration = 0;


        while (iteration < maxIterations) {

            setupAnts();
            moveAnts();
            updateTrails();
            updateBest();

            if(iteration == 0){
                currentPathL = optimizedPathLength;
            }

            iteration++;
        }

        if(optimizedPathLength < currentPathL){
            System.out.println("Shorted Length Till Now : " + (optimizedPathLength - correctionFactor));            
        }

        //System.out.println("Best Route till now : " + tourToString(optimizedPath));

        return optimizedPath.clone();
    }


    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Please specify an input file.");
            return;
        }

        tsp tspSolver = new tsp();
        
        try {
            tspSolver.parseFile(args[0]);
        } catch (IOException e) {
            System.err.println("Error reading cityGraph.");
            return;
        }

        System.out.println("Program Will Automatically Terminate after 5 seconds....");

        if(tspSolver.n < 250){
            tspSolver.maxIterations = 500;
        }

        long start = System.currentTimeMillis();
        long end = start + 5*60*1000;                   // Time Limit : 60 seconds * 1000 ms/sec

        while(System.currentTimeMillis() < end){
            tspSolver.solve();
        }

        return ;
    }
}