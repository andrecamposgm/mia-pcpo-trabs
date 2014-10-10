package andrecampos.mia.pcpo.trab2;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;
import andrecampos.mia.pcpo.data.Arc;
import andrecampos.mia.pcpo.data.Graph;

/**
 * 
 * @author Andre Campos
 */
public class SubgradientAlgorithm {

	private final int maxIteractions; 	//K
	private final Graph graph;
	private Model model; 
	private final double epson; 		// 
	private double upperBound;
	private final double [] miK;
	private final double [] miKNext;
	private double[] gama;
	private int maxNoProgress;
	
	public SubgradientAlgorithm(Graph graph, int maxIteractions) throws IloException {
		super();
		this.graph 			= graph;
		this.maxIteractions = maxIteractions;
		epson 				= 0.000001d;
		maxNoProgress 		= 10;
		miK 				= new double[graph.arcs.length]; // por padrao, tudo zero
		miKNext				= new double[graph.arcs.length]; // por padrao, tudo zero
		
		startSubgradient();
	}
	
	private void startSubgradient() throws IloException {
		upperBound 			= calculateUpperBound();
		gama     		    = new double[graph.arcs.length];
		
		double lMu			= 0; // L(mi(k))
		double lambda 		= 2;
		double tetaK 		= 0;
		
		int noProgressCount = 0;
		for(int iteration = 0; iteration < maxIteractions; iteration++) { //
			model 		= new Model(graph); // como reutilizar o modelo
			
			lMu   		= model.calculateObjectiveFunction(miK);
			
			calculateSubgradient();
			
			tetaK =  lambda * ( upperBound - lMu) / euclidianNormSqr(gama) ;
			
			calculateMiKNext(tetaK);
			
			if(normMiK_MiKNext() < epson) { 
				break;
			}
			
			if(++noProgressCount > maxNoProgress) { 
				lambda /= 2;
				noProgressCount = 0;
			} 
		}
		
	}

	private void calculateSubgradient() throws UnknownObjectException, IloException {
		double[][] x;
		double[] y;
		x = model.getX();
		y = model.getY();
		for (Arc arc : graph.arcs) {
			gama[arc.id] = arc.capacity * y[arc.id]; 
			for (double arcFlow : x[arc.id]) {
				gama[arc.id] += arcFlow; 
			}
		}
	}

	private double normMiK_MiKNext() {
		double norm = 0;
		for (int i = 0; i < miK.length; i++) {
			norm += pow((miKNext[i] - miK[i]),2);
		}
		return sqrt(norm);
	}

	private void calculateMiKNext(double tetaK) {
		for (int i = 0; i < miK.length; i++) {
			miKNext[i] = max(0d, (double) (miK[i] + tetaK * gama[i]));
		}
	}

	private double euclidianNormSqr(double[] grandient) {
		double sum = 0;
		for (double d : grandient) {
			sum += Math.abs(d);
		}
		return Math.pow(sum, 2);
	}
	

	private double calculateUpperBound() throws IloException {
		ModelUpperBound ub = new ModelUpperBound(graph);
		ub.solve();
		return ub.getObjectiveValue();
	}
}
