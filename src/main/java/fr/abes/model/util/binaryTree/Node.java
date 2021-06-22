package fr.abes.model.util.binaryTree;


import fr.abes.model.periodique.etatCollection.Sequence;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node {
    private Sequence element;
    private Node gauche, droit;

    public Node(Sequence valeur, Node g, Node d) {
        element = valeur;
        gauche = g;
        droit = d;
    }
}
