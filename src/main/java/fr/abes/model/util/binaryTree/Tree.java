package fr.abes.model.util.binaryTree;


import fr.abes.model.periodique.etatCollection.Sequence;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Tree {
    public Node root;

    public Tree(Node n) {
        root = n;
    }

    public void add(Sequence sequence) {
        root = add(root, sequence);
    }

    public Node add(Node node, Sequence sequence) {
        if (node == null)
            return new Node(sequence, null, null);
        if (node.getElement().getStartDate().compareTo(sequence.getStartDate()) <= 0)
            node.setDroit(add(node.getDroit(), sequence));
        else if (node.getElement().getStartDate().compareTo(sequence.getStartDate()) > 0)
            node.setGauche(add(node.getGauche(), sequence));
        return node;
    }

    public Node search(Node node, Sequence sequence) {
        if (node == null) return null;
        if (node.getElement().getStartDate().compareTo(sequence.getStartDate()) <= 0 && node.getElement().getEndDate().compareTo(sequence.getStartDate()) > 0) return node;
        if (node.getElement().getStartDate().compareTo(sequence.getStartDate()) > 0) return search(node.getGauche(), sequence);
        return search(node.getDroit(), sequence);
    }
}
