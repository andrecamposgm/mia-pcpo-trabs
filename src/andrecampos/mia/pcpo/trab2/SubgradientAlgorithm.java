package andrecampos.mia.pcpo.trab2;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import andrecampos.mia.pcpo.trab1.Model;
import ilog.concert.IloException;
import andrecampos.mia.pcpo.data.Arc;
import andrecampos.mia.pcpo.data.Graph;

/**
 * 
 * @author Andre Campos
 */
public class SubgradientAlgorithm {

	private final int maxIteractions; 	//K
	private final Graph graph;
	private ModelLagragianRelaxed model;
	private final double epson;
	private final double [] muK;
	private final double [] muKNext;
	private int maxNoProgress;
    private long time = System.currentTimeMillis();
	
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

        double lMu          = 0;         // L(mu))
        double teta         = 0;
        double gap          = 0;
        double lambda 		= 2;

        int noProgressCount = 0;
        model 		= new ModelLagragianRelaxed(graph);
        for(int iteration = 0; iteration < maxIteractions; iteration++) { //
            printStatistics(lMu, upperBound, gap, iteration);
            lMu   		= model.calculateObjectiveFunction(muK);
			
			calculateSubgradient(gama);
            if( isUpdateUB(gama) ) {
                upperBound = calculateNewUpperBound();
            }

            teta = lambda * (upperBound - lMu) / euclidianNormSqr(gama);

            gap = (upperBound - lMu) / (lMu);

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

        ModelLinearRelaxed model = new ModelLinearRelaxed(graph);
        model.solve();
        System.out.println("Modelo relaxado linear:"+model.getObjectiveValue());

	}

    private void printStatistics(double lb, double up, double gap, int iteration) {
        System.out.println("Iteration: "+iteration);
        System.out.println("Upper Bound: "+up);
        System.out.println("Lower Bound: "+lb);
        System.out.println("Gap: "+gap);
        long now = System.currentTimeMillis();
        System.out.println("Time: "+(now - time)+" ms");
        System.out.println("--------------------------------------------");
        time = now;
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
			sum += pow(d, 2);
		}
		return sum;
	}
	
	private double calculateUpperBound() throws IloException {
		ModelUpperBound ub = new ModelUpperBound(graph);
		ub.solve();
		return ub.getObjectiveValue();
	}
}
