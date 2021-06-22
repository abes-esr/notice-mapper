package fr.abes.biblioMapper.entity.etatCollection;


import fr.abes.biblioMapper.entity.Exemplaire;
import fr.abes.biblioMapper.util.binaryTree.Node;
import fr.abes.biblioMapper.util.binaryTree.Tree;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class EtatCollection extends Exemplaire {
    private String textEtatCollection;
    private String mentionDeLacune;
    private List<Sequence> sequences = new LinkedList<>();
    private String textLacune;
    private String note;
    private List<String> erreurs = new ArrayList<>();

    public EtatCollection(String epn) {
        super(epn);
    }

    public void setTextEtatCollection(String value) {
        this.textEtatCollection = value;
    }

    public void setMentionDeLacune(String value) {
        this.mentionDeLacune = value;
    }

    public void setTextLacune(String value) {
        this.textLacune = value;
    }

    public void setNote(String value) {
        this.note = value;
    }

    public void addErreur(String message) {
        this.erreurs.add(message);
    }

    public List<Sequence> getAllNonEmptySequences() {
        return this.sequences.stream()
                .filter(p -> (p instanceof SequenceLacune || p instanceof SequenceContinue || p instanceof SequenceErreur))
                .collect(Collectors.toList());
    }

    public List<SequenceContinue> getContinueSequences() {
        return this.sequences.stream()
                .filter(p -> p instanceof SequenceContinue)
                .map(p -> (SequenceContinue) p)
                .collect(Collectors.toList());
    }

    public List<SequenceLacune> getLacuneSequences() {
        return this.sequences.stream()
                .filter(p -> p instanceof SequenceLacune)
                .map(p -> (SequenceLacune) p)
                .collect(Collectors.toList());
    }

    public List<Sequence> getContinueSequenceAndLacunes() {
        return this.sequences.stream()
                .filter(s -> (s instanceof SequenceContinue || s instanceof SequenceLacune))
                .collect(Collectors.toList());
    }

    public List<SequenceErreur> getErrorSequences() {
        return this.sequences.stream()
                .filter(p -> p instanceof SequenceErreur)
                .map(p -> (SequenceErreur) p)
                .collect(Collectors.toList());
    }

    public List<SequenceVide> getEmptySequences() {
        return this.sequences.stream()
                .filter(p -> p instanceof SequenceVide)
                .map(p -> (SequenceVide) p)
                .collect(Collectors.toList());
    }

    public void addSequence(Sequence sequence) {
        if (sequence instanceof SequenceLacune) {
            this.addLacuneSequence((SequenceLacune) sequence);
        } else {
            if (!this.getContinueSequences().stream().anyMatch(s -> (s.getStartDate().equals(sequence.getStartDate()) && s.getEndDate().equals(sequence.getEndDate())))) {
                Sequence intraSequence = this.findIntraSequence(sequence);
                if (intraSequence != null) {
                    int nearestIndex = this.sequences.indexOf(intraSequence);
                    if (sequence.getStartDate().after(intraSequence.getStartDate())) {
                        insertInTheMiddleOfSequence(sequence, intraSequence, nearestIndex);
                    } else if (sequence.getStartDate().equals(intraSequence.getStartDate())) {
                        insertAfterSequence(sequence, nearestIndex);
                    }
                } else {
                    Sequence nearestSeq = this.findNearestSequence(sequence);

                    if (nearestSeq == null) {
                        this.sequences.add(0, sequence);
                    } else {

                        int nearestIndex = this.sequences.indexOf(nearestSeq);
                        if (sequence.getStartDate().after(nearestSeq.getStartDate())) {
                            if (sequence.getEndDate().before(nearestSeq.getEndDate()) || sequence.getEndDate().equals(nearestSeq.getEndDate())) {
                                insertInTheMiddleOfSequence(sequence, nearestSeq, nearestIndex);
                            } else {
                                insertAfterSequence(sequence, nearestIndex);
                            }
                        } else {
                            insertBeforeSequence(sequence, nearestIndex);
                        }
                    }
                }
            }
        }
    }

    public void updateSequenceWithFrequency(Period frequency) {
        if (this.getSequences().size() > 0) {
            this.getSequences().forEach(sequence -> {
                int index = this.sequences.indexOf(sequence);
                if (sequence.updateToFrequency(frequency)) {
                    if (index < this.getSequences().size() - 1) {
                        // Ce n'est pas le dernier de la liste, on met à jour la date de début de la séquence suivante
                        this.getSequences().get(index + 1).setStartDate(sequence.getEndDate().get(Calendar.YEAR), sequence.getEndDate().get(Calendar.MONTH), sequence.getEndDate().get(Calendar.DAY_OF_MONTH));
                        if (this.getSequences().get(index + 1).getStartDate().compareTo(this.getSequences().get(index + 1).getEndDate()) > 0) {
                            //si la séquence qu'on vient de modifier se retrouve avec une date de début supérieure à sa date de fin, on la supprime et on met à jour la date de début de la séquence suivante
                            this.sequences.remove(index + 1);
                            if (index < this.getSequences().size() - 2)
                                this.sequences.get(index + 2).setStartDate(sequence.getEndDate().get(Calendar.YEAR), sequence.getEndDate().get(Calendar.MONTH), sequence.getEndDate().get(Calendar.DAY_OF_MONTH));
                        }
                    }
                }
            });
        }
    }

    private void addLacuneSequence(final SequenceLacune sequence) {
        //on ne fait le traitement que si on n'a pas déjà ajouté une lacune identique dans la liste
        if (this.getLacuneSequences().stream().filter(s -> s.getStartDate().equals(sequence.getStartDate()) && s.getEndDate().equals(sequence.getEndDate())).count() == 0) {
            Sequence closerSequence = this.findIntraSequence(sequence);
            if (this.sequences.size() == 0) {
                SequenceErreur sequenceErreur = new SequenceErreur(sequence, "Impossible d'ajouter la lacune dans un état de collection vide");
                this.addSequence(sequenceErreur);
            } else {
                if (closerSequence == null) {
                    SequenceErreur sequenceErreur = new SequenceErreur(sequence, "Lacune en dehors de l'état de collection : " + ((!sequence.getNumero().equals("Non renseigné")) ? "no." + sequence.getNumero() : "") + ((!sequence.getVolume().equals("Non renseigné")) ? " vol." + sequence.getVolume() : "") + " (" + sequence.getStartDate().get(Calendar.YEAR) + ")");
                    this.addSequence(sequenceErreur);
                } else {
                    int nearestIndex = this.sequences.indexOf(closerSequence);

                    log.debug(this.getEpn() + "current=" + sequence.getStartDate().getTime() + " plus proche =" + closerSequence.getStartDate().getTime() + " index =" + nearestIndex);

                    if (sequence.getStartDate().after(closerSequence.getStartDate())) {
                        insertInTheMiddleOfSequence(sequence, closerSequence, nearestIndex);
                    } else if (sequence.getStartDate().equals(closerSequence.getStartDate())) {
                        insertAfterSequence(sequence, nearestIndex);
                    }
                }
            }
        }
    }

    private void insertAfterSequence(Sequence sequence, int nearestIndex) {
        Sequence nearestSequence = this.sequences.get(nearestIndex);

        if (nearestSequence.getEndDate().equals(sequence.getStartDate())) {
            this.sequences.add(Math.min(this.sequences.size(), nearestIndex + 1), sequence);
        } else {
            SequenceVide empty = new SequenceVide(nearestSequence.getEndDate().get(Calendar.YEAR), nearestSequence.getEndDate().get(Calendar.MONTH), nearestSequence.getEndDate().get(Calendar.DAY_OF_MONTH),
                    sequence.getStartDate().get(Calendar.YEAR), sequence.getStartDate().get(Calendar.MONTH), sequence.getStartDate().get(Calendar.DAY_OF_MONTH));

            if (!(sequence instanceof SequenceErreur)) {
                this.sequences.add(Math.min(this.sequences.size(), nearestIndex + 1), empty);
            }

            this.sequences.add(Math.max(this.sequences.size(), nearestIndex + 1), sequence);
        }
    }

    private void insertBeforeSequence(Sequence sequence, int nearestIndex) {
        Sequence nearestSequence = this.sequences.get(nearestIndex);

        if (nearestSequence.getStartDate().equals(sequence.getStartDate()) || nearestSequence.getStartDate().equals(sequence.getEndDate())) {
            this.sequences.add(Math.max(0, nearestIndex - 1), sequence);
        } else {
            SequenceVide empty = new SequenceVide(sequence.getEndDate().get(Calendar.YEAR), sequence.getEndDate().get(Calendar.MONTH), sequence.getEndDate().get(Calendar.DAY_OF_MONTH),
                    nearestSequence.getStartDate().get(Calendar.YEAR), nearestSequence.getStartDate().get(Calendar.MONTH), nearestSequence.getStartDate().get(Calendar.DAY_OF_MONTH));
            this.sequences.add(Math.max(0, nearestIndex), empty);
            this.sequences.add(Math.max(0, nearestIndex), sequence);
        }
    }

    private void insertInTheMiddleOfSequence(Sequence sequenceAIntercaler, Sequence nearestSequence, int nearestIndex) {
        //on copie la séquence trouvée, et on lui change sa date de début à la date de fin de la séquence de lacune
        Sequence sequenceAfter = (Sequence) nearestSequence.clone();

        sequenceAfter.setStartDate(sequenceAIntercaler.getEndDate().get(Calendar.YEAR), sequenceAIntercaler.getEndDate().get(Calendar.MONTH), sequenceAIntercaler.getEndDate().get(Calendar.DAY_OF_MONTH));
        //on change la date de fin de la séquence la plus proche à la date de début de la séquence de lacune
        nearestSequence.setEndDate(sequenceAIntercaler.getStartDate().get(Calendar.YEAR), sequenceAIntercaler.getStartDate().get(Calendar.MONTH), sequenceAIntercaler.getStartDate().get(Calendar.DAY_OF_MONTH));
        //on ajoute à la liste la séquence copiée et la séquence de lacune
        this.sequences.add(Math.min(this.sequences.size(), nearestIndex + 1), sequenceAIntercaler);

        if (!sequenceAfter.getStartDate().equals(sequenceAfter.getEndDate())) {
            this.sequences.add(Math.min(this.sequences.size(), nearestIndex + 2), sequenceAfter);
        }
    }

    public Sequence findIntraSequence(Sequence sequence) {
        if (this.sequences.size() == 0) {
            return null;
        } else {
            Tree tree = new Tree();
            this.sequences.forEach(s -> tree.add(s));
            Node nodeFound = tree.search(tree.root, sequence);
            return (nodeFound != null) ? nodeFound.getElement() : null;
        }
    }

    public Sequence findNearestSequence(Sequence sequence) {
        if (this.sequences.size() == 0) {
            return null;
        }
        return Collections.min(this.sequences, (d1, d2) -> {
            long diff1 = Math.abs(d1.getStartDate().getTime().getTime() - sequence.getStartDate().getTime().getTime());
            long diff2 = Math.abs(d2.getStartDate().getTime().getTime() - sequence.getStartDate().getTime().getTime());
            return Long.compare(diff1, diff2);
        });
    }

    public Sequence findNearestSequenceContinue(Sequence sequence) {
        if (this.sequences.stream()
                .filter(p -> p instanceof SequenceContinue)
                .map(p -> (SequenceContinue) p)
                .collect(Collectors.toList()).size() == 0) {
            return null;
        } else {
            return Collections.min(this.sequences.stream()
                    .filter(p -> p instanceof SequenceContinue)
                    .map(p -> (SequenceContinue) p)
                    .collect(Collectors.toList()), (d1, d2) -> {
                long diff1 = Math.abs(d1.getStartDate().getTime().getTime() - sequence.getStartDate().getTime().getTime());
                long diff2 = Math.abs(d2.getStartDate().getTime().getTime() - sequence.getStartDate().getTime().getTime());
                return Long.compare(diff1, diff2);
            });
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        return this.epn != null && epn.equals(((EtatCollection) obj).epn);
    }
}
