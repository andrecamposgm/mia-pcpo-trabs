package andrecampos.mia.pcpo.trab2;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import ilog.concert.IloException;
import andrecampos.mia.pcpo.data.Arc;
import andrecampos.mia.pcpo.data.Graph;
import ilog.cplex.IloCplex;

import java.io.OutputStream;

/**
 * 
 * @author Andre Campos
 */
public class SubgradientAlgorithm {

	private final int maxIteractions; 	//K
	private final Graph graph;
	private Model model; 
	private final double epson;
	private final double [] muK;
	private final double [] muKNext;
	private int maxNoProgress;
	
	public SubgradientAlgorithm(Graph graph, int maxIteractions) throws IloException {
		super();
		this.graph 			= graph;
		this.maxIteractions = maxIteractions;
		epson 				= 0.000001d;
		maxNoProgress 		= 10;
		muK = new double[graph.arcs.length]; // por padrao, tudo zero
		muKNext = new double[graph.arcs.length]; // por padrao, tudo zero
		
		startSubgradient();
	}
	
	private void startSubgradient() throws IloException {
		double upperBound 	= calculateUpperBound();
        double[] gama       = new double[graph.arcs.length];
		
		double lMu;         // L(mu))
        double teta         = 0;
        double lambda 		= 2;


		int noProgressCount = 0;
        double gap;
        for(int iteration = 0; iteration < maxIteractions; iteration++) { //
            model 		= new Model(graph);
            lMu   		= model.calculateObjectiveFunction(muK);
			
			calculateSubgradient(gama);
            if( isUpdateUB(gama) ) {
                upperBound = calculateNewUpperBound();
            }

            teta = lambda * (upperBound - lMu) / euclidianNormSqr(gama);

            gap = (upperBound - lMu) / (upperBound+1.0E-32d);
            System.out.println("Upper Bound: "+upperBound);
            System.out.println("Lower Bound: "+lMu);
            System.out.println("Gap: "+gap);

            calculateMiKNext(teta, gama);
			
			if(normMuKandMuKNext() < epson) {
				break;
			}

            copy(muKNext, muK);

			if(++noProgressCount > maxNoProgress) { 
				lambda /= 2;
				noProgressCount = 0;
			}

            model.clearObjective();
		}
		
	}

    private double calculateNewUpperBound() throws IloException {
        double[][]  x = model.getX();
        double[]    y = model.getY();

        Arc [] arcs = graph.arcs;
        double newUB = 0;
        for (int k = 0; k < graph.demands.length; k++) {
            for (int i = 0; i < arcs.length; i++) {
                newUB += arcs[i].variableCost* x[k][i];
            }
        }

        for (int i = 0; i < arcs.length; i++) {
            newUB += y[i] * arcs[i].fixedCost;
        }

        return newUB;
    }

    private boolean isUpdateUB(double[] gama) {
        for (int i = 0; i < gama.length ; i++) {
            if(gama[i] >= 0 ) {
                return false;
            }
        }
        return true;
    }

    private void copy(double[] miKNext, double[] miK) {
        for (int i = 0; i < miKNext.length; i++) {
            miK[i] = miKNext[i];
        }
    }

    private void calculateSubgradient(double [] gama) throws IloException {
		double[][]  x = model.getX();
        double[]    y = model.getY();

		for (Arc arc : graph.arcs) {
			gama[arc.id] = -arc.capacity * y[arc.id];
			for (double arcFlow : x[arc.id]) {
				gama[arc.id] += arcFlow; 
			}
		}
	}


	private double normMuKandMuKNext() {
		double norm = 0;
		for (int i = 0; i < muK.length; i++) {
			norm += pow((muKNext[i] - muK[i]),2);
		}
		return sqrt(norm);
	}

	private void calculateMiKNext(double teta, double [] gama) {
		for (int i = 0; i < muK.length; i++) {
			muKNext[i] = max(0d, (double) (muK[i] + teta * gama[i]));
		}
	}

	private double euclidianNormSqr(double[] grandient) {
		double sum = 0;
		for (double d : grandient) {
			sum += Math.pow(d, 2);
		}
		return sum;
	}
	
	private double calculateUpperBound() throws IloException {
		ModelUpperBound ub = new ModelUpperBound(graph);
		ub.solve();
		return ub.getObjectiveValue();
	}
}
