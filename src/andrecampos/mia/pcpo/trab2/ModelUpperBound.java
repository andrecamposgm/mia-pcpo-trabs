package andrecampos.mia.pcpo.trab2;

import ilog.concert.IloException;
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
 * This is the model used to calculate the upper bound 
 * @author Andre Campos
 */
public class ModelUpperBound {

	private final Graph 	graph;
	private final IloCplex 	cplex;
	
	private final Demand[] 		demands;
	private final Arc[] 		arcs;
	private final Node[] 		nodes;
	private IloNumVar[] 		x;

	public ModelUpperBound(Graph graph) throws IloException {
		super();
		this.graph = graph;
		cplex 		= new IloCplex();
		demands 	= graph.demands;
		arcs 		= graph.arcs;
		nodes		= graph.nodes;
		createModel();
	}

	private void createModel() throws IloException {
		defineXVariables();
		
		objectiveFunction();
		
		restrictDepamandsFromSourceToTargetNodes();
		
	}
	
	public boolean solve() throws IloException { 
		return cplex.solve();
	}
	
	private void defineXVariables() throws IloException {
		// Definicao da quantidade de fluxo da demanda k no arco i,j
		x = new IloNumVar[graph.getSizeArcs() * graph.getSizeDemands()];
		for (int k = 0; k < demands.length; k++) {
			for(Arc arc : arcs) { 
				setX(k, arc);
			}
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
					flow.addTerm(1, getX(k,arc));					
				}
				// subtrai custos entrando no vertice
				for (Arc arc : node.in) { 
					flow.addTerm(-1, getX(k,arc));
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

	private IloNumVar getX(int k, Arc arc) {
		return x[k*arcs.length + arc.id];
	}

	private void setX(int k, Arc arc) throws IloException {
		x[(k*arcs.length) + arc.id] = cplex.numVar(0, Double.MAX_VALUE, "x"+(k+1)+arc.name());
	}


	private void objectiveFunction() throws IloException {
		//// funcao objetivo
		// custo fixo
		double totalFixedCost = 0;
		for (int i = 0; i < arcs.length; i++) {
			totalFixedCost += arcs[i].fixedCost;
		}
		
		IloLinearNumExpr cost = cplex.linearNumExpr(totalFixedCost);
		
		// custo variavel
		for (int k = 0; k < demands.length; k++) {
			for (int i = 0; i < arcs.length; i++) {
				cost.addTerm(arcs[i].variableCost, x[k*arcs.length+i]);
			}
		}
		
		IloLinearNumExpr maxCapacity;
		for(Arc arc : arcs) {
			maxCapacity = cplex.linearNumExpr();
			for (int k = 0; k < demands.length; k++) {
				maxCapacity.addTerm(1, getX(k,arc));
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
			printer.println(String.format("%15d", arc.sNode) + String.format("%15d", arc.tNode) + String.format("%15.2f", totalCost(arc)));
		}
		printer.close();
	}
	
	public void exportLP(String dir) throws IloException { 
		cplex.exportModel(dir);
	}

	private double totalCost(Arc arc) throws UnknownObjectException, IloException {
		double total = 0;
		for (Demand demand : demands) {
			total += cplex.getValue(getX(demand.demand-1, arc)); 
		}
		return total;
	}
}
