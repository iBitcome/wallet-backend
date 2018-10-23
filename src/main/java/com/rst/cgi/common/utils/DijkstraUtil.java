package com.rst.cgi.common.utils;

import lombok.Data;

import java.util.*;

/**
 * @author hujia
 */
public class DijkstraUtil {
    @Data
    public static class Vertex implements Comparable<Vertex>{
        /** 节点索引 */
        private int index;
        /** 最短路径前节一点索引 */
        private List<Integer> prevList;
        /** 最短路径长度 */
        private int path;

        public Vertex(int index){
            this.index = index;
            this.path = Integer.MAX_VALUE;
            this.prevList = new ArrayList<>();
        }

        public Vertex(int index, int path, List<Integer> prevList){
            this.index = index;
            this.path = path;
            this.prevList = prevList;
        }

        @Override
        public int compareTo(Vertex o) {
            return o.path > path ? -1 : 1;
        }
    }

    /**
     * 搜索各顶点最短路径
     */
    public static Map<Integer, Vertex> search(int fromIndex, int[][] edges){
        /** 初始化未访问集合*/
        Queue<Vertex> unVisited = new PriorityQueue<>();
        List<Integer> prevList = new ArrayList<>();
        prevList.add(fromIndex);
        for (int i = 0; i < edges.length; i++) {
            if (i != fromIndex) {
                unVisited.add(new Vertex(i, edges[fromIndex][i], prevList));
            }
        }
        /** 初始化结果集合*/
        Map<Integer, Vertex> result = new HashMap<>(unVisited.size());

        while(!unVisited.isEmpty()){
            /** 找出路径最短的节点 */
            final Vertex vertex = unVisited.element();

            /** 更新剩余节点的最短路径值 */
            updateUnVisitedVertex(unVisited, edges, vertex);

            Vertex pollVertex = unVisited.poll();
            result.put(pollVertex.index, pollVertex);
        }

        return result;
    }

    /**
     * 搜索各顶点最短路径
     */
    public static Vertex search(int fromIndex, int endIndex, int[][] edges){
        /** 初始化未访问集合*/
        Queue<Vertex> unVisited = new PriorityQueue<>();
        List<Integer> prevList = new ArrayList<>();
        prevList.add(fromIndex);
        for (int i = 0; i < edges.length; i++) {
            if (i != fromIndex) {
                unVisited.add(new Vertex(i, edges[fromIndex][i], prevList));
            }
        }

        while(!unVisited.isEmpty()){
            /** 找出路径最短的节点 */
            final Vertex vertex = unVisited.element();

            /** 更新剩余节点的最短路径值 */
            updateUnVisitedVertex(unVisited, edges, vertex);

            Vertex pollVertex = unVisited.poll();
            if (pollVertex.index == endIndex) {
                return pollVertex;
            }
        }

        return null;
    }

    private static void updateUnVisitedVertex(Queue<Vertex> unVisited, int[][] edges, Vertex vertexToPoll) {
        for (Vertex vertex : unVisited) {
            if (vertex.index == vertexToPoll.index) {
                continue;
            }

            int distance = edges[vertex.index][vertexToPoll.index];

            if (distance == Integer.MAX_VALUE) {
                continue;
            }

            distance = vertexToPoll.getPath() + distance;

            if (distance < vertex.getPath()) {
                vertex.prevList = new ArrayList<>();
                vertex.prevList.addAll(vertexToPoll.prevList);
                vertex.prevList.add(vertexToPoll.index);
                vertex.setPath(distance);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int[][] edges = {
                {0,1,1,Integer.MAX_VALUE,Integer.MAX_VALUE},
                {1,0,Integer.MAX_VALUE,1,1},
                {1,Integer.MAX_VALUE,0,Integer.MAX_VALUE,Integer.MAX_VALUE},
                {Integer.MAX_VALUE,1,Integer.MAX_VALUE,0,1},
                {Integer.MAX_VALUE,1,Integer.MAX_VALUE,1,0},
        };

        Map<Integer, Vertex> ret = search(2, edges);

        System.out.println(edges);
    }
}
