package andrecampos.mia.pcpo.trab1;

import ilog.concert.IloException;

import java.io.IOException;
import java.rmi.UnexpectedException;

import andrecampos.mia.pcpo.data.Graph;

public class Solver {
	public static void main(String[] args) throws IloException, IOException {
		Graph g = new Graph("data/FCND.dat");
		Model m = new Model(g);
		if(!m.solve()) { 
			throw new UnexpectedException("Solucao nao encontrada");
		}
		m.exportSolution("solution/FCND.out");
		m.exportLP("solution/FCND.lp");
	}
	
}
