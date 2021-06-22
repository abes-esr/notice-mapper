package fr.abes.model.periodique.etatCollection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Period;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class EtatCollectionTest {

    @Test
    @DisplayName("test recherche séquence la plus proche quand séquence en dehors de l'intervalle")
    void findCloserTest1() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceLacune1 = new SequenceLacune(1989, Calendar.FEBRUARY, 28, "", "");
        Assertions.assertNull(etatCollection.findIntraSequence(sequenceLacune1));

        Sequence sequence1 = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", 1992, Calendar.FEBRUARY, 1, "", "");
        etatCollection.getSequences().add(sequence1);
        Assertions.assertNull(etatCollection.findIntraSequence(sequenceLacune1));
    }

    @Test
    @DisplayName("test recherche séquence 1 quand séquence 1 incluse dans séquence existante")
    void findCloserTest2() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequence = new SequenceContinue(1995, Calendar.JANUARY, 31, "", "", 1998, Calendar.MARCH, 31, "", "");
        etatCollection.getSequences().add(sequence);

        Sequence sequenceLacune = new SequenceLacune(1996, Calendar.MARCH, 31, "", "");
        Assertions.assertEquals(sequence, etatCollection.findIntraSequence(sequenceLacune));
    }

    @Test
    @DisplayName("test recherche séquence 1 quand séquence 1 incluse dans séquence existante (plusieurs séquences)")
    void findCloserTest3() {
        EtatCollection etatCollection = new EtatCollection("41133793901");

        Sequence sequence = new SequenceContinue(1995, Calendar.JANUARY, 31, "", "", 1998, Calendar.MARCH, 31, "", "");
        etatCollection.getSequences().add(sequence);
        Sequence sequence1 = new SequenceVide(1998, Calendar.MARCH, 31, 2000, Calendar.OCTOBER, 31);
        etatCollection.getSequences().add(sequence1);
        Sequence sequence2 = new SequenceContinue(2000, Calendar.OCTOBER, 31, "", "", 2005, Calendar.MARCH, 31, "", "");
        etatCollection.getSequences().add(sequence2);

        Sequence sequenceLacune = new SequenceLacune(1999, Calendar.MARCH, 31, "", "");
        Assertions.assertEquals(sequence1, etatCollection.findIntraSequence(sequenceLacune));

        Sequence sequenceLacune2 = new SequenceLacune(2000, Calendar.DECEMBER, 31, "", "");
        Assertions.assertEquals(sequence2, etatCollection.findIntraSequence(sequenceLacune2));
    }

    @Test
    @DisplayName("test recherche séquence dans séquence existante avec dates de début égales")
    void findCloserTest4() {
        EtatCollection etatCollection = new EtatCollection("41133793901");

        Sequence sequence = new SequenceContinue(1995, Calendar.JANUARY, 31, "", "", 1998, Calendar.MARCH, 31, "", "");
        etatCollection.getSequences().add(sequence);

        Sequence sequenceLacune = new SequenceLacune(1995, Calendar.JANUARY, 31, "", "");
        Assertions.assertEquals(sequence, etatCollection.findIntraSequence(sequenceLacune));
    }

    @Test
    @DisplayName("test ajout d'une séquence continue avant une autre sans trou")
    void addSequenceBeforeNoGap() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceToAdd1 = new SequenceContinue(2000, Calendar.JANUARY, 1, "", "", 2001, Calendar.JANUARY, 1, "", "");

        Sequence sequenceToAdd2 = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", 2000, Calendar.JANUARY, 1, "", "");

        etatCollection.addSequence(sequenceToAdd1);
        etatCollection.addSequence(sequenceToAdd2);

        Assertions.assertEquals(2, etatCollection.getSequences().size());
        Assertions.assertEquals(sequenceToAdd1, etatCollection.getSequences().get(1));
        Assertions.assertEquals(sequenceToAdd2, etatCollection.getSequences().get(0));
    }

    @Test
    @DisplayName("test ajout d'une séquence continue après une autre sans trou")
    void addSequenceAfterNoGap() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceToAdd1 = new SequenceContinue(2000, Calendar.JANUARY, 1, "", "", 2001, Calendar.JANUARY, 1, "", "");

        Sequence sequenceToAdd2 = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", 2000, Calendar.JANUARY, 1, "", "");

        etatCollection.addSequence(sequenceToAdd2);
        etatCollection.addSequence(sequenceToAdd1);

        Assertions.assertEquals(2, etatCollection.getSequences().size());
        Assertions.assertEquals(sequenceToAdd1, etatCollection.getSequences().get(1));
        Assertions.assertEquals(sequenceToAdd2, etatCollection.getSequences().get(0));
    }

    @Test
    @DisplayName("test ajout d'une séquence continue avant avec un trou")
    void addSequenceBeforeWithGap() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceToAdd1 = new SequenceContinue(2000, Calendar.JANUARY, 1, "", "", 2001, Calendar.JANUARY, 1, "", "");

        Sequence sequenceToAdd2 = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", 1995, Calendar.JANUARY, 1, "", "");

        etatCollection.addSequence(sequenceToAdd1);
        etatCollection.addSequence(sequenceToAdd2);

        Assertions.assertEquals(3, etatCollection.getSequences().size());

        // La séquence 2 est la première
        Assertions.assertEquals(sequenceToAdd2, etatCollection.getSequences().get(0));

        // La séquence vide est la deuxième avec les bonnes dates pour faire la jointure
        Assertions.assertEquals(SequenceVide.class, etatCollection.getSequences().get(1).getClass());
        Assertions.assertEquals(new GregorianCalendar(1995, Calendar.JANUARY, 1), etatCollection.getSequences().get(1).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(2000, Calendar.JANUARY, 1), etatCollection.getSequences().get(1).getEndDate());

        Assertions.assertEquals(sequenceToAdd1, etatCollection.getSequences().get(2));
    }

    @Test
    @DisplayName("test ajout d'une séquence continue après avec un trou")
    void addSequenceAfterWithGap() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceToAdd1 = new SequenceContinue(2000, Calendar.JANUARY, 1, "", "", 2001, Calendar.JANUARY, 1, "", "");

        Sequence sequenceToAdd2 = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", 1995, Calendar.JANUARY, 1, "", "");

        etatCollection.addSequence(sequenceToAdd2);
        etatCollection.addSequence(sequenceToAdd1);

        Assertions.assertEquals(3, etatCollection.getSequences().size());

        // La séquence 2 est la première
        Assertions.assertEquals(sequenceToAdd2, etatCollection.getSequences().get(0));

        // La séquence vide est la deuxième avec les bonnes dates pour faire la jointure
        Assertions.assertEquals(SequenceVide.class, etatCollection.getSequences().get(1).getClass());
        Assertions.assertEquals(new GregorianCalendar(1995, Calendar.JANUARY, 1), etatCollection.getSequences().get(1).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(2000, Calendar.JANUARY, 1), etatCollection.getSequences().get(1).getEndDate());

        Assertions.assertEquals(sequenceToAdd1, etatCollection.getSequences().get(2));
    }

    @Test
    @DisplayName("test ajout de lacune dans état de collection vide")
    void addLacuneOnEmptyHolding() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceToAdd = new SequenceLacune(1989, Calendar.FEBRUARY, 28, "", "");
        sequenceToAdd.setEndDate(1989, Calendar.MARCH, 31);
        etatCollection.addSequence(sequenceToAdd);

        Assertions.assertEquals(1, etatCollection.getErrorSequences().size());
        Assertions.assertEquals("Impossible d'ajouter la lacune dans un état de collection vide", etatCollection.getErrorSequences().get(0).getMessage());
    }

    @Test
    @DisplayName("test ajout de lacune mal placée")
    void addLacuneMisplaced() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceToAdd = new SequenceLacune(1989, Calendar.FEBRUARY, 28, "2139", "3000");
        sequenceToAdd.setEndDate(1989, Calendar.MARCH, 31);
        Sequence sequence1 = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", true);
        etatCollection.addSequence(sequence1);

        etatCollection.addSequence(sequenceToAdd);
        Assertions.assertEquals(1, etatCollection.getErrorSequences().size());
        Assertions.assertEquals("Lacune en dehors de l'état de collection : no.3000 vol.2139 (1989)", etatCollection.getErrorSequences().get(0).getMessage());
    }

    @Test
    @DisplayName("test ajout lacune avec découpage de séquence existante")
    void addLacuneWithCutSequence() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence lacune = new SequenceLacune(1991, Calendar.FEBRUARY, 28, "", "");
        lacune.setEndDate(1991, Calendar.MARCH, 31);

        Sequence sequence = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", 1992, Calendar.APRIL, 20, "", "");
        etatCollection.addSequence(sequence);

        etatCollection.addSequence(lacune);
        Assertions.assertEquals(3, etatCollection.getSequences().size());
        Assertions.assertEquals(new GregorianCalendar(1990, Calendar.JANUARY, 1), etatCollection.getSequences().get(0).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.FEBRUARY, 28), etatCollection.getSequences().get(0).getEndDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.FEBRUARY, 28), etatCollection.getSequences().get(1).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.MARCH, 31), etatCollection.getSequences().get(1).getEndDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.MARCH, 31), etatCollection.getSequences().get(2).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1992, Calendar.APRIL, 20), etatCollection.getSequences().get(2).getEndDate());
    }

    @Test
    @DisplayName("test ajout lacune avec découpage de séquence lacunaire existante")
    void addLacuneWithCutSequenceLacune() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequenceToAdd = new SequenceLacune(1991, Calendar.FEBRUARY, 28, "", "");
        sequenceToAdd.setEndDate(1991, Calendar.MARCH, 31);

        Sequence sequence1 = new SequenceContinue(1990, Calendar.JANUARY, 1, "", "", 1992, Calendar.OCTOBER, 20, "", "");
        etatCollection.addSequence(sequence1);

        etatCollection.addSequence(sequenceToAdd);

        Sequence sequenceToAdd2 = new SequenceLacune(1991, Calendar.APRIL, 1, "", "");
        sequenceToAdd2.setEndDate(1991, Calendar.APRIL, 30);

        etatCollection.addSequence(sequenceToAdd2);

        Assertions.assertEquals(5, etatCollection.getSequences().size());
        Assertions.assertEquals(new GregorianCalendar(1990, Calendar.JANUARY, 1), etatCollection.getSequences().get(0).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.FEBRUARY, 28), etatCollection.getSequences().get(0).getEndDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.FEBRUARY, 28), etatCollection.getSequences().get(1).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.MARCH, 31), etatCollection.getSequences().get(1).getEndDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.MARCH, 31), etatCollection.getSequences().get(2).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.APRIL, 1), etatCollection.getSequences().get(2).getEndDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.APRIL, 1), etatCollection.getSequences().get(3).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.APRIL, 30), etatCollection.getSequences().get(3).getEndDate());
        Assertions.assertEquals(new GregorianCalendar(1991, Calendar.APRIL, 30), etatCollection.getSequences().get(4).getStartDate());
        Assertions.assertEquals(new GregorianCalendar(1992, Calendar.OCTOBER, 20), etatCollection.getSequences().get(4).getEndDate());
    }

    @Test
    @DisplayName("test ajout 2 séquences continues sans date de fin et même date de début")
    void testAddSequenceContinueWithSameStartDate() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequence1 = new SequenceContinue(1995, null, null, "1", "", false);
        Sequence sequence2 = new SequenceContinue(1995, null, null, "2", "", false);

        etatCollection.addSequence(sequence1);
        etatCollection.addSequence(sequence2);

        Assertions.assertEquals(1, etatCollection.getContinueSequences().size());
        Assertions.assertEquals(1995, etatCollection.getContinueSequences().get(0).getStartDate().get(Calendar.YEAR));
    }

    @Test
    @DisplayName("test ajout séquences avec séquences vides + séquence sans date de fin")
    void testAddSequenceWithEmpty() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequence1 = new SequenceContinue(1990, Calendar.JANUARY, 20, "", "", 1995, Calendar.OCTOBER, 31, "", "");
        Sequence sequence2 = new SequenceContinue(1996, Calendar.FEBRUARY, 20, "", "", 2000, Calendar.JUNE, 20, "","");
        Sequence sequence3 = new SequenceContinue(1995, Calendar.DECEMBER, 31, "", "", false);

        etatCollection.addSequence(sequence1);
        etatCollection.addSequence(sequence2);
        etatCollection.addSequence(sequence3);

        Assertions.assertEquals(1990, etatCollection.getSequences().get(0).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.JANUARY, etatCollection.getSequences().get(0).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(0).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1995, etatCollection.getSequences().get(0).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.OCTOBER, etatCollection.getSequences().get(0).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(0).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1995, etatCollection.getSequences().get(1).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.OCTOBER, etatCollection.getSequences().get(1).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(1).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1995, etatCollection.getSequences().get(1).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.DECEMBER, etatCollection.getSequences().get(1).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(1).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1995, etatCollection.getSequences().get(2).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.DECEMBER, etatCollection.getSequences().get(2).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(2).getStartDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1995, etatCollection.getSequences().get(3).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.DECEMBER, etatCollection.getSequences().get(3).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(3).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1996, etatCollection.getSequences().get(3).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.FEBRUARY, etatCollection.getSequences().get(3).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(3).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1996, etatCollection.getSequences().get(4).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.FEBRUARY, etatCollection.getSequences().get(4).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(4).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(2000, etatCollection.getSequences().get(4).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.JUNE, etatCollection.getSequences().get(4).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(4).getEndDate().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    @DisplayName("test reconstruction des séquences en fonction de la périodicité de la publication")
    void testUpdateSequenceWithFrequency() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequence1 = new SequenceLacune(1991, Calendar.FEBRUARY, 28, "", "");
        Sequence sequence2 = new SequenceLacune(1993, Calendar.APRIL, 12, "", "");
        Sequence sequence3 = new SequenceContinue(1990, Calendar.JANUARY, 20, "", "", 2000, Calendar.OCTOBER, 31, "", "");

        etatCollection.addSequence(sequence3);
        etatCollection.addSequence(sequence1);
        etatCollection.addSequence(sequence2);

        etatCollection.updateSequenceWithFrequency(Period.ofMonths(3));

        Assertions.assertEquals(1990, etatCollection.getSequences().get(0).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.JANUARY, etatCollection.getSequences().get(0).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(0).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1991, etatCollection.getSequences().get(0).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.FEBRUARY, etatCollection.getSequences().get(0).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(28, etatCollection.getSequences().get(0).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1991, etatCollection.getSequences().get(1).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.FEBRUARY, etatCollection.getSequences().get(1).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(28, etatCollection.getSequences().get(1).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1991, etatCollection.getSequences().get(1).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.MAY, etatCollection.getSequences().get(1).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(28, etatCollection.getSequences().get(1).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1991, etatCollection.getSequences().get(2).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.MAY, etatCollection.getSequences().get(2).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(28, etatCollection.getSequences().get(2).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1993, etatCollection.getSequences().get(2).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.APRIL, etatCollection.getSequences().get(2).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(12, etatCollection.getSequences().get(2).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1993, etatCollection.getSequences().get(3).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.APRIL, etatCollection.getSequences().get(3).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(12, etatCollection.getSequences().get(3).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1993, etatCollection.getSequences().get(3).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.JULY, etatCollection.getSequences().get(3).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(12, etatCollection.getSequences().get(3).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1993, etatCollection.getSequences().get(4).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.JULY, etatCollection.getSequences().get(4).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(12, etatCollection.getSequences().get(4).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(2000, etatCollection.getSequences().get(4).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.OCTOBER, etatCollection.getSequences().get(4).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(4).getEndDate().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    @DisplayName("test reconstruction des séquences en fonction de la périodicité de la publication avec séquences vides")
    void testUpdateSequenceWithFrequency2() {
        EtatCollection etatCollection = new EtatCollection("41133793901");
        Sequence sequence1 = new SequenceContinue(1990, Calendar.JANUARY, 20, "", "", 1995, Calendar.OCTOBER, 31, "", "");
        Sequence sequence2 = new SequenceContinue(1996, Calendar.APRIL, 20, "", "", 2000, Calendar.JUNE, 20, "","");
        Sequence sequence3 = new SequenceContinue(1995, Calendar.DECEMBER, 31, "", "", false);

        etatCollection.addSequence(sequence1);
        etatCollection.addSequence(sequence2);
        etatCollection.addSequence(sequence3);

        etatCollection.updateSequenceWithFrequency(Period.ofMonths(3));

        Assertions.assertEquals(1990, etatCollection.getSequences().get(0).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.JANUARY, etatCollection.getSequences().get(0).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(0).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1995, etatCollection.getSequences().get(0).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.OCTOBER, etatCollection.getSequences().get(0).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(0).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1995, etatCollection.getSequences().get(1).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.OCTOBER, etatCollection.getSequences().get(1).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(1).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1995, etatCollection.getSequences().get(1).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.DECEMBER, etatCollection.getSequences().get(1).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(1).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1995, etatCollection.getSequences().get(2).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.DECEMBER, etatCollection.getSequences().get(2).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(2).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1996, etatCollection.getSequences().get(2).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.MARCH, etatCollection.getSequences().get(2).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(2).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1996, etatCollection.getSequences().get(3).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.MARCH, etatCollection.getSequences().get(3).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(31, etatCollection.getSequences().get(3).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(1996, etatCollection.getSequences().get(3).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.APRIL, etatCollection.getSequences().get(3).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(3).getEndDate().get(Calendar.DAY_OF_MONTH));

        Assertions.assertEquals(1996, etatCollection.getSequences().get(4).getStartDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.APRIL, etatCollection.getSequences().get(4).getStartDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(4).getStartDate().get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(2000, etatCollection.getSequences().get(4).getEndDate().get(Calendar.YEAR));
        Assertions.assertEquals(Calendar.JUNE, etatCollection.getSequences().get(4).getEndDate().get(Calendar.MONTH));
        Assertions.assertEquals(20, etatCollection.getSequences().get(4).getEndDate().get(Calendar.DAY_OF_MONTH));

    }
}
