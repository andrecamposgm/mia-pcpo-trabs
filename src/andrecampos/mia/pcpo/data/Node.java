/**
 * 
 */
package andrecampos.mia.pcpo.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andrecampos
 */
public class Node {
	
	final public int id; 
	final public List<Arc> in;
	final public List<Arc> out;
	public Node(int i) {
		id = i; 
		in = new ArrayList<Arc>();
		out = new ArrayList<Arc>();
	}
	
	public String name(){ 
		return Integer.toString(id);
	}
}
