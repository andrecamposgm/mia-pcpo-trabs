package andrecampos.mia.pcpo.trab2;

import andrecampos.mia.pcpo.data.Arc;
import andrecampos.mia.pcpo.data.Demand;
import andrecampos.mia.pcpo.data.Graph;
import andrecampos.mia.pcpo.data.Node;
import andrecampos.mia.pcpo.trab1.Model;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Andre Campos
 */
public class ModelLinearRelaxed extends Model {

	public ModelLinearRelaxed(Graph graph) throws IloException {
		super(graph);
	}

    @Override
	protected void defineYVariables() throws IloException {
		// Definicao das variaveis binarias yij que indicam se o arco e' utilizado ou nao
		y = new IloIntVar[arcs.length];
		for(Arc arc : arcs) {
			y[arc.id] = cplex.numVar(0,1,"y"+arc.name());
		}
	}

    public static void main(String args[]) {



    }

}
