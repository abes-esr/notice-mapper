package fr.abes.biblioMapper.entity;

import fr.abes.biblioMapper.entity.donneesCodees.AnneePublication;
import fr.abes.biblioMapper.entity.etatCollection.EtatCollection;
import lombok.Getter;
import lombok.Setter;

import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class RessourceContinue extends Notice {

    private String issn;

    private String keyShortedTitle;

    private String properTitle;

    private String titleFromDifferentAuthor;

    private String parallelTitle;

    private String titleComplement;

    private String sectionTitle;

    private String keyTitleQualifer;

    private String keyTitle;

    private Period frequency;

    private String continuousType;

    private AnneePublication startYear;

    private AnneePublication endYear;

    protected Set<EtatCollection> etatCollections = new HashSet<>();

    public void addEtatCollection(EtatCollection etatCollection) {
        etatCollection.updateSequenceWithFrequency(frequency);
        this.etatCollections.add(etatCollection);
    }



}
