//author: Tyler Officer
//CS 576 Bioinformatics

import java.util.*;
import java.io.*;

public class euler_assemble {
	static class Vertex implements Comparable<Vertex> {
		public String name;
		public PriorityQueue<Vertex> children;
		int num_parents;

		public Vertex(String name) {
			this.name = name;
			this.children = new PriorityQueue<>();
			this.num_parents = 0;
		}

		@Override
		public int compareTo(Vertex o) {
			return this.name.compareTo(o.name);
		}
	}

	public static void main(String[] args) throws IOException {
		TreeMap<String, Vertex> map = getInput(args[0]);
		Vertex[] fake_edge = getFakeEdge(map);
		LinkedList<String> eulerian_path = getEulerianPath(map);
		printSuperString(eulerian_path, fake_edge);
	}

	public static TreeMap<String, Vertex> getInput(String infile) throws IOException, FileNotFoundException {
		TreeMap<String, Vertex> map = new TreeMap<>(); //TreeMap sorts keys automatically
		try (BufferedReader br = new BufferedReader(new FileReader(infile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String a = line.substring(0, line.length()-1);
				String b = line.substring(1, line.length());
				Vertex v1 = (map.containsKey(a)) ? map.get(a) : new Vertex(a);
				Vertex v2 = (map.containsKey(b)) ? map.get(b) : new Vertex(b);
				
				//repeat k-mers are not allowed in input set
				//problem if v1 and v2 are the same (k-1)-mer here (ie AAA -> v1 = AA, v2 = AA)
				v1.children.add(v2);
				v2.num_parents++;

				map.put(a, v1);
				map.put(b, v2);
			}
		}
		return map;
	}

	//make sure graph is balanced - if not add fake edge b/w unbalanced vertices
	public static Vertex[] getFakeEdge(TreeMap<String,Vertex> map) {
		Vertex[] fake_edge = new Vertex[2];
		Vertex fake_edge_s = null;
		Vertex fake_edge_t = null;
		for (Map.Entry<String,Vertex> entry : map.entrySet()) {
			Vertex v = entry.getValue();
			if (v.num_parents > v.children.size()) {
				fake_edge_s = v;
				fake_edge[0] = v;
			}
			else if (v.children.size() > v.num_parents) {
				fake_edge_t = v;
				fake_edge[1] = v;
			}
		}

		if (fake_edge_s != null && fake_edge_t != null) {
			fake_edge_s.children.add(fake_edge_t);
			fake_edge_t.num_parents++;
			map.put(fake_edge_s.name, fake_edge_s);
			map.put(fake_edge_t.name, fake_edge_t);
		}
		
		return fake_edge;
	}

	public static LinkedList<String> getEulerianPath(TreeMap<String, Vertex> map) {
		LinkedList<String> eulerian_path = new LinkedList<>();
		//iterate through the map until all edges have been traversed
		while (!map.isEmpty()) {
			//if this is the first iteration choose the vertex that is first in lexicographical order
			//otherwise choose the first vertex in the current cycle that has an unused outgoing edge
			Vertex start = map.firstEntry().getValue();
			for (int i = 0; i < eulerian_path.size(); i++) {
				if (map.containsKey(eulerian_path.get(i))) {
					start = map.get(eulerian_path.get(i));
					break;
				}
			}
			
			LinkedList<String> cycle = new LinkedList<>();
			cycle.add(start.name);

			Vertex prev = start;
			Vertex curr;
			//until the current path is a cycle choose the first lexicographical child of the current vertex and repeat
			while ((curr = prev.children.poll()) != start) {
				//System.out.println(prev.name + " " + curr.name);
				//if all the outgoing edges from a vertex have been used remove it from the map
				if (prev.children.isEmpty()) map.remove(prev.name);
				cycle.add(curr.name);
				prev = curr;
			}
			if (prev.children.isEmpty()) map.remove(prev.name);
			cycle.add(curr.name);

			eulerian_path = merge(eulerian_path, cycle);
		}

		return eulerian_path;
	}

	public static LinkedList<String> merge(LinkedList<String> list, LinkedList<String> temp) {
		if (list.isEmpty()) return temp;
		String s = temp.getFirst();
		temp.removeLast();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(s)) {
				list.addAll(i, temp);
				break;
			}
		}

		return list;
	}

	public static void printSuperString(LinkedList<String> eulerian_path, Vertex[] fake_edge) {
		if (fake_edge[0] != null && fake_edge[1] != null) {
			eulerian_path.removeLast();
			while (!eulerian_path.get(0).equals(fake_edge[0].name) || !eulerian_path.get(1).equals(fake_edge[1].name)) {
				eulerian_path.add(eulerian_path.remove(0));
			}
			eulerian_path.add(eulerian_path.remove(0));
		}

		StringBuilder sb = new StringBuilder(eulerian_path.get(0));
		int last_char = eulerian_path.get(0).length()-1;
		for (int i = 1; i < eulerian_path.size(); i++) {
			sb.append(eulerian_path.get(i).charAt(last_char));
		}

		System.out.println(sb.toString());
	}
}