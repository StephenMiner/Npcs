package me.stephenminer.v1_21_R3.pathfinder;

import me.stephenminer.npc.util.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class PathFinder {
    private final int maxDrop;
    private final int maxStep;

    private double densityThresh;

    public PathFinder(int maxDrop, int maxStep, double densityThresh){
        this.maxDrop = maxDrop;
        this.maxStep = maxStep;
        this.densityThresh = densityThresh;
    }

    public PathFinder(int maxDrop, int maxStep){
        this(maxDrop, maxStep, 0.6d);
    }

    public int maxStep(){ return maxStep; }
    public int maxDrop(){ return maxDrop; }

    public Path findPath(Level world, BlockPos start, BlockPos target, int range){
        //define search area outside of the area between the start and target
        int minX=Math.min(start.getX(),target.getX()) - range;
        int minY=Math.min(start.getY(),target.getY()) - range;
        int minZ=Math.min(start.getZ(),target.getZ()) - range;
        int maxX=Math.max(start.getX(),target.getX()) + range;
        int maxY=Math.max(start.getY(),target.getY()) + range;
        int maxZ=Math.max(start.getZ(),target.getZ()) + range;

        int sizeX = (maxX - minX) + 1;
        int sizeY = (maxY - minY) + 1;
        int sizeZ = (maxZ - minZ) + 1;
        int maxSize = sizeX * sizeY * sizeZ;

        boolean dense = false;
        NodeGrid grid = null;
        NodeMap nodeMap = new NodeMap(2048);

        NodeHeap openSet = new NodeHeap(512);

        me.stephenminer.npc.util.Node startNode = new Node(start.getX(), start.getY(), start.getZ());
        startNode.estCost = heuristic(start, target);
        openSet.push(startNode);
        nodeMap.put(start.asLong(),startNode);

        boolean[] walkableCache = new boolean[maxSize];
        boolean[] walkableSet = new boolean[maxSize];

        Node goal = null;

        while (!openSet.isEmpty()){
            Node current = openSet.pop();
            if (current.closed) continue;
            current.closed = true;

            if (onTarget(current, target)){
                goal = current;
                break;
            }

            //Need to switch to Node array?
            if (!dense && nodeMap.size() > densityThresh * maxSize){
                grid = new NodeGrid(minX, minY, minZ, sizeX, sizeY, sizeZ);
                Collection<Node> values = nodeMap.values();
                for (Node node : values) grid.add(node);
                dense = true;
                nodeMap = null;
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                int x = current.x + dir.getStepX();
                int z = current.z + dir.getStepZ();
                for (int dy = -maxDrop; dy <= maxStep; dy++){
                    int y = current.y + dy;
                    long posLong = BlockPos.asLong(x,y,z);
                    Node neighbor = dense ? grid.get(x, y, z) : nodeMap.get(posLong);
                    if (!isWalkable(world, x, y, z, walkableSet, walkableCache, dense, grid)) continue;
                    if (neighbor == null) neighbor = new Node(x, y, z);

                    double cost = current.actualCost + 1;
                    if (cost < neighbor.actualCost) {
                        neighbor.actualCost = cost;
                        neighbor.estCost = heuristic(x, y, z, target);
                        neighbor.parent = current;
                        openSet.push(neighbor);
                        if (!dense) nodeMap.put(posLong, neighbor);
                        else grid.add(neighbor);
                    }
                    break; //only take first valid position in loop
                }
            }
        }
        if (goal == null){
            System.err.println("FAILED TO GENERATE A PATH");
            return new Path(Collections.emptyList(), target, false);
        }
        List<Node> nodeList = reconstructPath(goal);
        return new Path(nodeList, target, true);
    }


    private boolean onTarget(Node currentNode, BlockPos target){
        return currentNode.x == target.getX() && currentNode.y == target.getY() && currentNode.z == target.getZ();
    }

    private int denseIndex(int x, int y, int z, int minX, int minY, int minZ, int sizeX, int sizeZ) {
        return (x - minX) + (z - minZ) * sizeX + (y - minY) * sizeX * sizeZ;
    }

    private Node getDenseNode(Node[] grid, int x, int y, int z, int minX, int minY, int minZ, int sizeX, int sizeZ){
        int index = denseIndex(x, y, z, minX, minY, minZ, sizeX, sizeZ);
        if (index < 0 || index >= grid.length) return null;
        return grid[index];
    }

    private Node getMapNode(NodeMap map, int x, int y, int z){
        return map.get(BlockPos.asLong(x,y,z));
    }

    private boolean isWalkable(Level level, int x, int y, int z, boolean[] set, boolean[] cache, boolean dense, NodeGrid grid){
        int index = dense ? grid.index(x,y,z) : 0;
        if (dense && set[index]) return cache[index];
        BlockPos pos = new BlockPos(x,y,z);
        BlockState foot = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());
        boolean walkable = foot.getCollisionShape(level, pos).isEmpty()
                && head.getCollisionShape(level, pos.above()).isEmpty()
                && !below.getCollisionShape(level, pos.below()).isEmpty();
        if (dense){
            set[index] = true;
            cache[index] = walkable;
        }
        return walkable;
    }

    private List<Node> reconstructPath(Node node){
        List<Node> path = new ArrayList<>();
        while (node != null){
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }




    private double heuristic(BlockPos pos1, BlockPos pos2){
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ());
    }

    private double heuristic(int x, int y, int z, BlockPos target){
        return Math.abs(x - target.getX()) + Math.abs(y - target.getY()) + Math.abs(z - target.getZ());
    }









    private static class NodeHeap{
        private Node[] heap;
        private int size;

        public NodeHeap(int capacity){
            this.heap = new Node[capacity];
            this.size = 0;
        }

        public void push(Node node){
            if (size >= heap.length)
                heap = Arrays.copyOf(heap,heap.length << 1); //double array length
            heap[size] = node;
            siftUp(size++); //Executes method then increments size
        }


        public Node pop(){
            Node result = heap[0];
            heap[0] = heap[--size]; //just fill the first slot with something
            siftDown(0);
            return result;
        }


        private void siftUp(int index){
            while (index > 0){
                Node current = heap[index];
                int parent = (index - 1) >> 1;
                Node pNode = heap[parent];
                if (current.totalCost() >= pNode.totalCost()) break; //parent has lower total cost than child = good news bears
                //Swap child and parent Node
                heap[index] = pNode;
                heap[parent] = current;
                index = parent;
            }
        }

        private void siftDown(int index){
            while (true){
                int left = (index << 1) + 1;
                int right= left + 1;
                int smallest = index;
                if (left < size && heap[left].totalCost() < heap[smallest].totalCost())
                    smallest = left;
                if (right < size && heap[right].totalCost() < heap[smallest].totalCost())
                    smallest = right;
                if (smallest == index) break; //Job is done, smallest index is the current index
                //Swap parent and child and update index
                Node current = heap[index];
                heap[index] = heap[smallest];
                heap[smallest] = current;
                index = smallest;
            }
        }

        public int size(){ return size; }
        public boolean isEmpty(){ return size == 0; }
    }


    private static final class NodeMap{
        private long[] keys;
        private Node[] vals;
        private int capacity, size;

        public NodeMap(int capacity){
            this.capacity = capacity;
            keys = new long[capacity];
            vals = new Node[capacity];
            Arrays.fill(keys, Long.MIN_VALUE);
            this.size = 0;
        }


        private int hash(long key){
            return Long.hashCode(key) & (capacity - 1);
        }

        public Node get(long key){
            int index = hash(key);
            while (keys[index] != Long.MIN_VALUE){
                if (keys[index] == key) return vals[index];
                index = (index + 1) & (capacity - 1);
            }
            return null;
        }

        public void put(long key, Node value){
            int index = hash(key);
            while (keys[index] != Long.MIN_VALUE){
                if (keys[index] == key){
                    vals[index] = value;
                    return;
                }
                index = (index + 1) & (capacity - 1);
            }
            keys[index] = key;
            vals[index] = value;
            size++;
        }


        public int size(){ return size; }

        public Collection<Node> values(){
            List<Node> values = new ArrayList<>();
            for (int i = 0; i < keys.length; i++){
                if (keys[i] != Long.MIN_VALUE) values.add(vals[i]);
            }
            return values;
        }
    }


    private static final class NodeGrid{
        private final Node[] grid;
        private final int minX, minY, minZ;
        private final int sizeX, sizeY, sizeZ;
        private final int totalSize;

        public NodeGrid(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ){
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.totalSize = sizeX * sizeY * sizeZ;
            this.grid = new Node[totalSize];
        }

        public int index(int x, int y, int z){
            return (x - minX) + ((z - minZ) * sizeX) + ((y - minY) * sizeX * sizeZ);
        }

        public Node get(int x, int y, int z){
            return grid[index(x,y,z)];
        }

        public void add(Node node){
            int index = index(node.x, node.y, node.z);
            grid[index] = node;
        }


    }


}
