package andrecampos.mia.pcpo.trab2;

import ilog.concert.IloException;

import java.io.IOException;

import andrecampos.mia.pcpo.data.Graph;

public class Solver {
	public static void main(String[] args) throws IloException, IOException {
		Graph g = new Graph("data/FCND.dat");
		SubgradientAlgorithm m = new SubgradientAlgorithm(g,1000);
//		if(!m.solve()) { 
//			throw new UnexpectedException("Solucao nao encontrada");
//		}
		
		System.out.println(m);
//		m.exportSolution("solution/FCND.out");
//		m.exportLP("solution/FCND.lp");
	}
	
}
