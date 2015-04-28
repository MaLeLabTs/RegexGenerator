/*
 * Copyright (C) 2015 Machine Learning Lab - University of Trieste, 
 * Italy (http://machinelearning.inginf.units.it/)  
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.inginf.male.variations;

import it.units.inginf.male.configuration.EvolutionParameters;
import it.units.inginf.male.generations.Generation;
import it.units.inginf.male.generations.Growth;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.tree.Leaf;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.Group;
import it.units.inginf.male.tree.operator.NonCapturingGroup;
import it.units.inginf.male.utils.Pair;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author MaleLabTs
 */
public class Variation {

    private Context context;
    private Generation growth;

    public Variation(Context context) {
        this.context = context;
        this.growth = new Growth(5, context);
    }

    /**
     * This method performs the crossover operation on two individuals
     *
     * @param individualA the first individual to apply the crossover
     * @param individualB the second individual to apply the crossover
     * @return two new individuals
     */
    public Pair<Node, Node> crossover(Node individualA, Node individualB) {
        boolean isGood = false;
        Node newIndividualA = null;
        Node newIndividualB = null;

        for (int tries = 0; tries < 20; tries++) {

            newIndividualA = individualA.cloneTree();
            newIndividualB = individualB.cloneTree();

            Node randomNodeA = pickRandomNode(newIndividualA);
            Node randomNodeB = pickRandomNode(newIndividualB);

            if (randomNodeA != null && randomNodeB != null) {

                Node aParent = randomNodeA.getParent();
                List<Node> aChilds = aParent.getChildrens();
                int aIndex = aChilds.indexOf(randomNodeA);
                Node bParent = randomNodeB.getParent();
                List<Node> bChilds = bParent.getChildrens();
                int bIndex = bChilds.indexOf(randomNodeB);
                aChilds.set(aIndex, randomNodeB);
                bChilds.set(bIndex, randomNodeA);
                randomNodeA.setParent(bParent);
                randomNodeB.setParent(aParent);

                 
                if (checkMaxDepth(newIndividualA, 1)
                        && checkMaxDepth(newIndividualB, 1)
                        && newIndividualA.isValid()
                        && newIndividualB.isValid()) {
                    /*newIndividualA=normalizeGroup(newIndividualA);
                    newIndividualB=normalizeGroup(newIndividualB);*/
                    isGood = true;
                    break;
                }

            }

        }


        if (isGood) {
            return new Pair<Node, Node>(newIndividualA, newIndividualB);
        } else {
            return null;

        }
    }

    /**
     * This method apply a mutation on an individual
     *
     * @param individual the indidual on which aplly the mutation
     * @return a new mutated individual
     */
    public Node mutate(Node individual) {

        List<Node> newNodes = this.growth.generate(20);
        Node mutant = individual.cloneTree();


        for (Node newNode : newNodes) {

            Node randomNode = pickRandomNode(mutant);
            if (randomNode != null) {
                replaceNode(mutant, randomNode, newNode);
                if (checkMaxDepth(mutant, 1) && mutant.isValid()) {
                    break;
                }
            }
            mutant = individual.cloneTree();

        }

        return mutant;
    }

    private Node pickRandomNode(Node individual) {
        EvolutionParameters param = context.getConfiguration().getEvolutionParameters();
        List<Node> nodeList = new ArrayList<Node>();

        float random = this.context.getRandom().nextFloat();

        if (random <= param.getNodeCrossoverSelectionProbability()) {
            enlistNode(individual, nodeList, false);
        } else if (random <= param.getNodeCrossoverSelectionProbability() + param.getLeafCrossoverSelectionProbability()) {
            enlistNode(individual, nodeList, true);
        } else {
            nodeList.add(individual);
        }

        //if it contains only root & leafs you must choose a leaf
        if (nodeList.isEmpty()) {
            enlistNode(individual, nodeList, true);
        }

        if (nodeList.isEmpty()) {
            return null;
        }
        int randomIndex = this.context.getRandom().nextInt(nodeList.size());
        return nodeList.get(randomIndex);
    }

    private void enlistNode(Node root, List<Node> nodes, boolean isLeaf) {

        if (isNodePickable(root, isLeaf)) {
            nodes.add(root);

        }
        for (Node child : root.getChildrens()) {
            enlistNode(child, nodes, isLeaf);
        }


    }

    private boolean isNodePickable(Node root, boolean isLeaf) {
        return !(root instanceof Leaf ^ isLeaf) && root.getParent() != null;
    }

    private void replaceNode(Node root, Node oldChild, Node newChild) {

        Node parent = oldChild.getParent();
        List<Node> childs = parent.getChildrens();
        int index = childs.indexOf(oldChild);
        newChild.setParent(parent);
        oldChild.setParent(null);
        childs.set(index, newChild);
    }

    private void swapNodes(Node a, Node b) {

        Node aParent = a.getParent();
        List<Node> aChilds = aParent.getChildrens();
        int aIndex = aChilds.indexOf(a);
        Node bParent = b.getParent();
        List<Node> bChilds = bParent.getChildrens();
        int bIndex = bChilds.indexOf(b);
        aChilds.set(aIndex, b);
        bChilds.set(bIndex, a);
        a.setParent(bParent);
        b.setParent(aParent);
    }

    private boolean checkMaxDepth(Node root, int depth) {
        if (depth > context.getConfiguration().getEvolutionParameters().getMaxDepthAfterCrossover()) {
            return false;
        }
        if (root instanceof Leaf) {
            return true;
        }

        boolean ret = true;
        for (Node child : root.getChildrens()) {
            ret &= checkMaxDepth(child, depth + 1);
        }

        return ret;
    }

    private void checkSingleGroup(Node root, List<Group> groups) {

        if (root instanceof Group) {
            groups.add((Group) root);
        }
        for (Node child : root.getChildrens()) {
            checkSingleGroup(child, groups);
        }

    }

    private Node normalizeGroup(Node root) {
        List<Group> groups = new LinkedList<Group>();
        checkSingleGroup(root, groups);

        if (groups.size() < 2) {
            return root;
        }
        int nextInt = this.context.getRandom().nextInt(groups.size());

        groups.remove(nextInt);

        for (Group group : groups) {
            NonCapturingGroup ncg = new NonCapturingGroup();
            if (group != root) {
                ncg.setParent(group.getParent());
                int indexOf = ncg.getParent().getChildrens().indexOf(group);
                ncg.getParent().getChildrens().set(indexOf, ncg);

            } else {
                root = ncg;
            }
            ncg.getChildrens().addAll(group.getChildrens());
            ncg.getChildrens().get(0).setParent(ncg);
        }

        return root;
    }
}
