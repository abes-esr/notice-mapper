package fr.abes.biblioMapper.util.binaryTree;


import fr.abes.biblioMapper.entity.etatCollection.Sequence;
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
