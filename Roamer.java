private class Roamer {

        public int PathToDestination[] = new int[graph.length];

        public boolean visitMarker[] = new boolean[graph.length];

        public void clear() {
            for (int i = 0; i < n; i++)
                visitMarker[i] = false;
        }

        public double pathLength() {
            double length = graph[PathToDestination[n - 1]][PathToDestination[0]];
            for (int i = 0; i < n - 1; i++) {
                length += graph[PathToDestination[i]][PathToDestination[i + 1]];
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