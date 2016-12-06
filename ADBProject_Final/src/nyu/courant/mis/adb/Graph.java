/**
 * 
 */
package nyu.courant.mis.adb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Aditya Kapoor, Tanmmay Mahendru
 *
 */
public class Graph<T> {

	private List<Edge<T>> allEdges;
    private Map<Long,Vertex<T>> allVertex;
    boolean isDirected = false;
    
    public Graph(boolean isDirected){
        allEdges = new ArrayList<Edge<T>>();
        allVertex = new HashMap<Long,Vertex<T>>();
        this.isDirected = isDirected;
    }
    
    public void addEdge(long id1, long id2){
        addEdge(id1,id2,0);
    }
    
    //This works only for directed graph because for undirected graph we can end up
    //adding edges two times to allEdges
    public void addVertex(Vertex<T> vertex){
        if(allVertex.containsKey(vertex.getId())){
            return;
        }
        allVertex.put(vertex.getId(), vertex);
        for(Edge<T> edge : vertex.getEdges()){
            allEdges.add(edge);
        }
    }
    
    public Vertex<T> addSingleVertex(long id){
        if(allVertex.containsKey(id)){
            return allVertex.get(id);
        }
        Vertex<T> v = new Vertex<T>(id);
        allVertex.put(id, v);
        return v;
    }
    
    public Vertex<T> getVertex(long id){
        return allVertex.get(id);
    }
    
    public void addEdge(long id1,long id2, int weight){
        Vertex<T> vertex1 = null;
        if(allVertex.containsKey(id1)){
            vertex1 = allVertex.get(id1);
        }else{
            vertex1 = new Vertex<T>(id1);
            allVertex.put(id1, vertex1);
        }
        Vertex<T> vertex2 = null;
        if(allVertex.containsKey(id2)){
            vertex2 = allVertex.get(id2);
        }else{
            vertex2 = new Vertex<T>(id2);
            allVertex.put(id2, vertex2);
        }

        Edge<T> edge = new Edge<T>(vertex1,vertex2,isDirected,weight);
        allEdges.add(edge);
        vertex1.addAdjacentVertex(edge, vertex2);
        if(!isDirected){
            vertex2.addAdjacentVertex(edge, vertex1);
        }

    }
    
    public List<Edge<T>> getAllEdges(){
        return allEdges;
    }
    
    public Collection<Vertex<T>> getAllVertex(){
        return allVertex.values();
    }
    
    public void setDataForVertex(long id, T data){
        if(allVertex.containsKey(id)){
            Vertex<T> vertex = allVertex.get(id);
            vertex.setData(data);
        }
    }

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        for(Edge<T> edge : getAllEdges()){
            buffer.append(edge.getVertex1() + " " + edge.getVertex2() + " " + edge.getWeight());
            buffer.append("\n");
        }
        return buffer.toString();
    }
	
	public boolean hasCycle(Graph<Integer> graph) {
        Set<Vertex<Integer>> whiteSet = new HashSet<>();
        Set<Vertex<Integer>> graySet = new HashSet<>();
        Set<Vertex<Integer>> blackSet = new HashSet<>();

        for (Vertex<Integer> vertex : graph.getAllVertex()) {
            whiteSet.add(vertex);
        }

        while (whiteSet.size() > 0) {
            Vertex<Integer> current = whiteSet.iterator().next();
            if(dfs(current, whiteSet, graySet, blackSet)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfs(Vertex<Integer> current, Set<Vertex<Integer>> whiteSet,
                        Set<Vertex<Integer>> graySet, Set<Vertex<Integer>> blackSet ) {
        //move current to gray set from white set and then explore it.
        moveVertex(current, whiteSet, graySet);
        for(Vertex<Integer> neighbor : current.getAdjacentVertexes()) {
            //if in black set means already explored so continue.
            if (blackSet.contains(neighbor)) {
                continue;
            }
            //if in gray set then cycle found.
            if (graySet.contains(neighbor)) {
                return true;
            }
            if(dfs(neighbor, whiteSet, graySet, blackSet)) {
                return true;
            }
        }
        //move vertex from gray set to black set when done exploring.
        moveVertex(current, graySet, blackSet);
        return false;
    }

    private void moveVertex(Vertex<Integer> vertex, Set<Vertex<Integer>> sourceSet,
                            Set<Vertex<Integer>> destinationSet) {
        sourceSet.remove(vertex);
        destinationSet.add(vertex);
    }
}
