# Travelling Salesman Problem Solution By Ant Colony Optimization

# Problem
A traveler needs to visit all the cities from a list, where distances between all the cities are known and each city should be visited just once. What is the shortest possible route that he visits each city exactly once and returns to the origin city?

## T.S.P
TSP problem is regarded as Holy Grail Of Computer Science and is one of the most famous hard combinatorial optimization problems. It belongs to the class of NP-hard optimization problems.
This means that no polynomial time algorithm is known to guarantee its global optimal solution.

## Why Ants ?
In the natural world, ants of some species (initially) wander randomly, and upon finding food return to their colony while laying down pheromone trails. If other ants find such a path, they are likely not to keep travelling at random, but instead to follow the trail, returning and reinforcing it if they eventually find food.
Over time, however, the pheromone trail starts to evaporate, thus reducing its attractive strength. The more time it takes for an ant to travel down the path and back again, the more time the pheromones have to evaporate. A short path, by comparison, gets marched over more frequently, and thus the pheromone density becomes higher on shorter paths than longer ones.
Pheromone evaporation also has the advantage of avoiding the convergence to a locally optimal solution. If there were no evaporation at all, the paths chosen by the first ants would tend to be excessively attractive to the following ones. In that case, the exploration of the solution space would be constrained. The influence of pheromone evaporation in real ant systems is unclear, but it is very important in artificial systems.
The overall result is that when one ant finds a good (i.e., short) path from the colony to a food source, other ants are more likely to follow that path, and positive feedback eventually leads to all the ants following a single path. The idea of the ant colony algorithm is to mimic this behavior with "simulated ants" walking around the graph representing the problem to solve.
(Wiki)

## Instruction

1. Clone the above repo by 
	```
	 $ https://github.com/pranavgupta1234/TSP_BY_ACO
	```
2. Spawn terminal in cloned folder.
3. Compile using `$ make` .
4. Run above program as 
	```
	 $ java tsp file_name
	```
5. Check stdout for output !

## Results
Results are displayed below in table (Probabilitic)

| TestCase      | Distance      | 
| ------------- |:-------------:| 
| euc_100       | 1510.456712   | 
| euc_250       | 2461.674567   | 
| euc_500       | 3492.265110   |
| noneuc_100    | 5208.494552   | 
| noneuc_250    | 12855.132914  | 
| noneuc_500    | 25440.281625  | 

## About
Aimed to solve Travelling Salesman Problem. This Assignment was under Prof. [Deepak Khemani](https://www.iitm.ac.in/info/fac/khemani).

## Contributors

[Pranav Gupta](http://pranavgupta1234.github.io)
1. Github: http://github.com/pranavgupta1234
2. Email: pranavgupta4321@gmail.com

[Akhil Singhal](http://github.com/akhilsinghal1234)
1. Github: http://github.com/akhilsinghal1234



