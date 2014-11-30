package andrecampos.mia.pcpo.trab1;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.io.IOException;
import java.io.PrintStream;

import andrecampos.mia.pcpo.data.Arc;
import andrecampos.mia.pcpo.data.Demand;
import andrecampos.mia.pcpo.data.Graph;
import andrecampos.mia.pcpo.data.Node;

/**
 * Esse modelo foi desenhado para a solucao do problema enunciado no arquivo "trabalho1.pdf" contido nesse projeto
 * @author Andre Campos
 */
public class Model {

    protected final Graph 	graph;
    protected final IloCplex 	cplex;

	protected final Demand[] 	demands;
    protected final Arc[] 		arcs;
    protected final Node[] 		nodes;
    protected IloNumVar[] y;
    protected IloNumVar[][] x;

	public Model(Graph graph) throws IloException {
		super();
		this.graph = graph;
		cplex 		= new IloCplex();
		demands 	= graph.demands;
		arcs 		= graph.arcs;
		nodes		= graph.nodes;
		createModel();
	}

	private void createModel() throws IloException {
		defineYVariables();
		
		defineXVariables();
		
		objectiveFunction();
		
		restrictDepamandsFromSourceToTargetNodes();
		
		restrictMaxCapacityPerArc();
	}
	
	public boolean solve() throws IloException { 
		return cplex.solve();
	}
	
	private void defineXVariables() throws IloException {
		// Definicao da quantidade de fluxo da demanda k no arco i,j
		x = new IloNumVar[graph.getSizeDemands()][graph.getSizeArcs()];
		for (int k = 0; k < demands.length; k++) {
			for(Arc arc : arcs) { 
				x[k][arc.id] = cplex.numVar(0, Double.MAX_VALUE, "x"+(k+1)+arc.name());
			}
		}
	}

	protected void defineYVariables() throws IloException {
		// Definicao das variaveis binarias yij que indicam se o arco e' utilizado ou nao
		y = new IloIntVar[arcs.length];
		for(Arc arc : arcs) {
			y[arc.id] = cplex.boolVar("y"+arc.name());
		}
	}

	private void restrictMaxCapacityPerArc() throws IloException {
		IloLinearNumExpr maxCapacity;
		for(Arc arc : arcs) {
			maxCapacity = cplex.linearNumExpr();
			for (int k = 0; k < demands.length; k++) {
				maxCapacity.addTerm(1, x[k][arc.id]);
			}
			maxCapacity.addTerm(-arc.capacity, y[arc.id]);
			cplex.addLe(maxCapacity,0);
		}
	}

	private void restrictDepamandsFromSourceToTargetNodes() throws IloException {
		Demand demand;
		Node node;
		IloLinearNumExpr flow;
		for (int i = 0; i < nodes.length; i++) {
			node = nodes[i];
			
			for (int k = 0; k < demands.length; k++) {
				flow = cplex.linearNumExpr();
				// soma custos saindo do vertice
				for (Arc arc : node.out) {
					flow.addTerm(1, x[k][arc.id]);					
				}
				// subtrai custos entrando no vertice
				for (Arc arc : node.in) { 
					flow.addTerm(-1, x[k][arc.id]);
				}
				
				demand = demands[k];
				if(node.id == demand.sDemand) { 
					cplex.addEq(flow, demand.demand);
				} else if(node.id == demand.tDemand){ 
					cplex.addEq(flow, -demand.demand);
				} else { 
					cplex.addEq(flow, 0);
				}
			}
		}
	}

	private void objectiveFunction() throws IloException {
		//// funcao objetivo
		// custo fixo
		IloLinearNumExpr cost = cplex.linearNumExpr();
		for (int i = 0; i < arcs.length; i++) {
			cost.addTerm(y[i], arcs[i].fixedCost);
		}
		
		// custo variavel
		for (int k = 0; k < demands.length; k++) {
			for (Arc arc : arcs) {
				cost.addTerm(arc.variableCost, x[k][arc.id]);
			}
		}
		cplex.addMinimize(cost);
	}

	
	public double getObjectiveValue() throws IloException {
		return cplex.getObjValue();
	}
	
	public Status getStatus() throws IloException { 
		return cplex.getStatus();
	}
	
	public void exportSolution(String fileName) throws IOException, IloException {
		PrintStream printer = new PrintStream(fileName);
		printer.println(String.format("%-15s", "Objetive:") + String.format("%15.2f", getObjectiveValue()));
		printer.println(String.format("%-15s", "Status:") + String.format("%15s", getStatus()));
		for (Arc arc : arcs) {
			if(cplex.getValue(y[arc.id]) == 1) { 
				printer.println(String.format("%15d", arc.sNode) + String.format("%15d", arc.tNode) + String.format("%15.2f", totalCost(arc)));
			}
		}
		printer.close();
	}
	
	public void exportLP(String dir) throws IloException { 
		cplex.exportModel(dir);
	}

	private double totalCost(Arc arc) throws UnknownObjectException, IloException {
		double total = 0;
		for (int k = 0; k < demands.length; k++) {
			total += cplex.getValue(x[k][arc.id]);
		}
		return total;
	}
}
