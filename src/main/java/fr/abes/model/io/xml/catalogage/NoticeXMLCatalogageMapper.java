package fr.abes.model.io.xml.catalogage;


import fr.abes.model.exception.IllegalDateException;
import fr.abes.model.exception.IllegalHoldingException;
import fr.abes.model.exception.IllegalPublicationYearException;
import fr.abes.model.exception.MissingFieldException;
import fr.abes.model.periodique.NoticePeriodique;
import fr.abes.model.periodique.AnneePublication;
import fr.abes.model.periodique.TypeRessourceContinue;
import fr.abes.model.periodique.TypeSupport;
import fr.abes.model.periodique.etatCollection.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.ErrorMessage;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

/**
 * Convertisseurs entre les notices issues de la base XML et les notices pour PERISCOPE
 */
@Component
@Slf4j
public class NoticeXMLCatalogageMapper {

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Convertisseur pour les notices XML vers les notices de visualisation
     */
    @Bean
    public void converterXMLNoticeCatalogageNoticePeriodique() {

        Converter<NoticeXMLCatalogage, NoticePeriodique> myConverter = new Converter<NoticeXMLCatalogage, NoticePeriodique>() {

            public NoticePeriodique convert(MappingContext<NoticeXMLCatalogage, NoticePeriodique> context) {
                NoticeXMLCatalogage source = context.getSource();
                NoticePeriodique target = new NoticePeriodique();
                int maxPass = 1;
                try {
                    // Champ type de support
                    target.setSupportType(extractSupportType(source.getLeader().substring(6, 7)));
                    // Champs PPN
                    target.setPpn(source.getControlFields().stream().filter(elm -> elm.getTag().equalsIgnoreCase("001")).findFirst().orElseThrow().getValue());

                    for (int currentPass = 1; currentPass < maxPass + 1; currentPass++) {

                        // Champs data fields
                        for (DataField dataField : source.getDataFields()) {
                            // Zone 011
                            if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("011")) {

                                for (SubField subField : dataField.getSubFields()) {
                                    // zone 011-a
                                    if (subField.getCode().equalsIgnoreCase("a")) {
                                        target.setIssn(subField.getValue());
                                    }
                                }
                            }

                            // Zone 100
                            if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("100")) {
                                for (SubField subField : dataField.getSubFields()) {
                                    // zone 100-a
                                    if (subField.getCode().equalsIgnoreCase("a")) {
                                        String value = subField.getValue();

                                        // Extraction de la date de début
                                        try {
                                            AnneePublication year = buildStartPublicationYear(value);
                                            target.setStartYear(year);
                                        } catch (IllegalPublicationYearException e) {
                                            log.debug("Unable to parse start publication year :" + e.getLocalizedMessage());
                                            target.setStartYear(null);
                                        }

                                        // Extraction de la date de fin
                                        try {
                                            AnneePublication year = buildEndPublicationYear(value);
                                            target.setEndYear(year);
                                        } catch (IllegalPublicationYearException e) {
                                            log.debug("Unable to parse end publication year :" + e.getLocalizedMessage());
                                            target.setEndYear(null);
                                        }
                                    }
                                }
                            }

                            // Zone 110
                            if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("110")) {
                                for (SubField subField : dataField.getSubFields()) {
                                    // zone 110-a
                                    if (subField.getCode().equalsIgnoreCase("a")) {
                                        target.setContinuousType(extractOnGoingResourceType(subField.getValue()));
                                        target.setFrequency(extractFrequency(subField.getValue().substring(1, 2)));
                                    }
                                }
                            }

                            // Zone 200
                            if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("200")) {

                                for (SubField subField : dataField.getSubFields()) {
                                    // zone 200-a
                                    if (subField.getCode().equalsIgnoreCase("a")) {
                                        if (target.getProperTitle() == null) {
                                            target.setProperTitle(subField.getValue());
                                        }
                                    }

                                    // zone 200-c
                                    if (subField.getCode().equalsIgnoreCase("c")) {
                                        if (target.getTitleFromDifferentAuthor() == null) {
                                            target.setTitleFromDifferentAuthor(subField.getValue());
                                        }
                                    }

                                    // zone 200-d
                                    if (subField.getCode().equalsIgnoreCase("d") && (target.getParallelTitle() == null)) {
                                        target.setParallelTitle(subField.getValue());
                                    }

                                    // zone 200-e
                                    if (subField.getCode().equalsIgnoreCase("e") && (target.getTitleComplement() == null)) {
                                        target.setTitleComplement(subField.getValue());
                                    }

                                    // zone 200-i
                                    if (subField.getCode().equalsIgnoreCase("i") && (target.getSectionTitle() == null)) {
                                        target.setSectionTitle(subField.getValue());
                                    }
                                }
                            }

                            // Zone 210
                            if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("210")) {
                                for (SubField subField : dataField.getSubFields()) {
                                    // zone 210-c
                                    if (subField.getCode().equalsIgnoreCase("c") && (target.getPublisher() == null)) {
                                        target.setPublisher(subField.getValue());
                                    }
                                }
                            }

                            // Zone 530
                            if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("530")) {

                                for (SubField subField : dataField.getSubFields()) {
                                    // zone 530-a
                                    if (subField.getCode().equalsIgnoreCase("a")) {
                                        target.setKeyTitle(subField.getValue());
                                    }

                                    // zone 530-b
                                    if (subField.getCode().equalsIgnoreCase("b")) {
                                        target.setKeyTitleQualifer(subField.getValue());
                                    }
                                }
                            }

                            // Zone 531
                            if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("531")) {

                                for (SubField subField : dataField.getSubFields()) {
                                    if (target.getKeyShortedTitle() == null) {
                                        target.setKeyShortedTitle(subField.getValue());
                                    }
                                }
                            }

                            // Zone 9XX
                            if (dataField.getTag().startsWith("9")) {

                                // On cherche la sous-zone 5 qui contient le EPN
                                SubField specimenIdField = dataField.getSubFields().stream().filter(elm -> elm.getCode().equalsIgnoreCase("5"))
                                        .findAny().orElse(null);

                                if (specimenIdField == null) {
                                    throw new MissingFieldException("Zone " + dataField.getTag() + " doesn't have a subfield code=\"5\"");
                                }

                                String epn = specimenIdField.getValue().split(":")[1];

                                // On récupère l'exemplaire ou on le crée s'il n'existe pas
                                EtatCollection etatCollection = target.getEtatCollections().stream().filter(elm -> elm.getEpn().equalsIgnoreCase(epn))
                                        .findAny().orElse(null);

                                if (etatCollection == null) {
                                    etatCollection = new EtatCollection(epn);
                                }

                                if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("955")) {
                                    processEtatCollection(etatCollection, dataField);
                                } else if (currentPass == 1 && dataField.getTag().equalsIgnoreCase("959")) {
                                    // On traite les lacunes dans la deuxième passe
                                    processLacunes(etatCollection, dataField);
                                } else if (currentPass == 1) {
                                    // On itère sur les autres sous-zone
                                    for (SubField subField : dataField.getSubFields()) {
                                        if (dataField.getTag().equalsIgnoreCase("930") && (subField.getCode().equalsIgnoreCase("b"))) {
                                            etatCollection.setRcr(subField.getValue());
                                        }
                                    }
                                }
                                target.addEtatCollection(etatCollection);
                            }
                        }
                    }
                    return target;
                } catch (NullPointerException ex) {
                    throw new MappingException(Collections.singletonList(new ErrorMessage("Notice has null field")));
                } catch (IllegalHoldingException ex) {
                    throw new MappingException(Collections.singletonList(new ErrorMessage(ex.getLocalizedMessage())));
                } catch (Exception ex) {
                    throw new MappingException(Collections.singletonList(new ErrorMessage(ex.getLocalizedMessage())));
                }

            }
        };
        modelMapper.addConverter(myConverter);
    }

    /**
     * Méthode permettant de générer une séquence d'un état de collection contenu dans une 955 du format d'export
     *
     * @param dataField : la zone 955 à parser
     * @return : la sequence générée
     * @throws IllegalHoldingException si une erreur est détectée dans la 955
     */
    protected void processEtatCollection(EtatCollection etatCollection, DataField dataField) throws IllegalHoldingException {
        Iterator<SubField> subFieldIterator = dataField.getSubFields().iterator();
        String sousZonePrecedente;

        // Prorpiété d'une séquence continue
        Integer startYear = null;
        Integer startMonth = null;
        Integer startDay = null;
        String startVolume = null;
        String startNumero = null;

        Integer endYear = null;
        Integer endMonth = null;
        Integer endtDay = null;
        String endNumero = null;
        String endVolume = null;

        boolean ouvert = false;

        // Compteurs d'occurence des balises
        int aCount = 0;
        int bCount = 0;
        int iCount = 0;

        boolean erreur = false;
        while (subFieldIterator.hasNext()) {
            SubField subField = subFieldIterator.next();
            if (subField.getCode().equalsIgnoreCase("g")) {
                //si on rencontre une $g, les sous zones suivantes correspondent à une séquence de numérotation parallèle (dernière séquence dans la zone)
                //on sort de la boucle
                break;
            }
            switch (subField.getCode()) {
                case "r":
                    etatCollection.setTextEtatCollection(subField.getValue());
                    break;
                case "w":
                    etatCollection.setMentionDeLacune(subField.getValue());
                    break;
                case "z":
                    etatCollection.setNote(subField.getValue());
                    break;
                case "5":
                    break;
                default:
                    if (subField.getValue() == null) {
                        //si la valeur de la sous zone est vide, on est dans le cas d'un intervale ouvert, on sort de la boucle
                        ouvert = true;
                        break;
                    }
                    try {
                        //volume
                        if (subField.getCode().equalsIgnoreCase("a")) {
                            if (aCount == 0) { //Première fois qu'on rencontre la balise
                                startVolume = subField.getValue();
                            } else if (aCount == 1) {
                                endVolume = subField.getValue();
                            }
                            aCount++;
                        }
                        //numéro
                        if (subField.getCode().equalsIgnoreCase("b")) {
                            if (bCount == 0) { //Première fois qu'on rencontre la balise
                                startNumero = subField.getValue();
                            } else if (bCount == 1) {
                                endNumero = subField.getValue();
                            }
                            bCount++;
                        }
                        //mois
                        if (subField.getCode().equalsIgnoreCase("j")) {
                            try {
                                if (iCount == 0) { //Première fois qu'on rencontre la balise
                                    startMonth = getMoisFromEnum(subField.getValue().trim());
                                } else if (iCount == 1) {
                                    endMonth = getMoisFromEnum(subField.getValue().trim());
                                }
                            } catch (IllegalDateException ex) {
                                erreur = true;
                            }
                        }
                        //jour
                        if (subField.getCode().equalsIgnoreCase("k")) {
                            if (iCount == 0) { //Première fois qu'on rencontre la balise
                                startDay = Integer.parseInt(subField.getValue().trim());
                            } else if (iCount == 1) {
                                endtDay = Integer.parseInt(subField.getValue().trim());
                            }
                        }
                        //annee
                        if (subField.getCode().equalsIgnoreCase("i")) {
                            if (iCount == 0) { //Première fois qu'on rencontre la balise
                                startYear = Integer.parseInt(subField.getValue().trim());
                            } else if (iCount == 1) {
                                endYear = Integer.parseInt(subField.getValue().trim());
                            }
                        }
                    } catch (IllegalDateException | NumberFormatException ex) {
                        etatCollection.addErreur("Erreur epn " + etatCollection.getEpn() + " : syntaxe de date incorrecte : " + subField.getValue());
                        erreur = true;
                    }
            }
            sousZonePrecedente = subField.getCode().toLowerCase(Locale.ROOT);
            if (sousZonePrecedente.equals("i")) {
                iCount++;
            }
        }

        if (iCount >= 0 && startYear != null) {
            // La date de début a été trouvé
            //on ajout la séquence uniquement si elle a une date de début pour gérer le cas ou la 955 n'a que des sous zones de note
            try {
                SequenceContinue sequence = new SequenceContinue(startYear, startMonth, startDay, startVolume, startNumero, ouvert);
                if (iCount >= 2) {
                    // La date de fin a été trouvé
                    sequence.setEndDate(endYear, endMonth, endtDay, endVolume, endNumero);
                }
                if (erreur) {
                    SequenceErreur sequenceErreur = new SequenceErreur(sequence, "Erreur dans la saisie du mois");
                    etatCollection.addSequence(sequenceErreur);
                } else {
                    etatCollection.addSequence(sequence);
                }

            } catch (IllegalDateException ex) {
                log.error("Impossible de créer la séquence continue : " + ex.getLocalizedMessage());
                if (startYear != null) {
                    // Il ne s'agit pas d'une séquence vide alors remonte l'erreur
                    throw new IllegalHoldingException("Impossible de créer la séquence continue : " + ex.getLocalizedMessage());
                }
            }
        }
    }

    void processLacunes(EtatCollection etatCollection, DataField dataField) throws IllegalHoldingException {
        Iterator<SubField> subFieldIterator = dataField.getSubFields().iterator();

        // Prorpiété d'une séquence continue
        Integer startYear = null;
        Integer startMonth = null;
        Integer startDay = null;
        String volume = null;
        String numero = null;

        boolean error = false;
        String errorMessage = "";
        while (subFieldIterator.hasNext()) {
            SubField subField = subFieldIterator.next();

            switch (subField.getCode()) {
                case "r":
                    etatCollection.setTextLacune(subField.getValue());
                    break;
                case "5":
                    break;
                default:
                    try {
                        if (subField.getCode().equals("0")) {
                            //si on arrive sur une $0, on crée un nouveau bloc
                            if (!error) {
                                SequenceLacune sequence = new SequenceLacune(startYear, startMonth, startDay, volume, numero);
                                etatCollection.addSequence(sequence);
                            }
                        }
                        if (subField.getCode().equalsIgnoreCase("d")) {
                            volume = subField.getValue();
                        }
                        if (subField.getCode().equalsIgnoreCase("e")) {
                            numero = subField.getValue();
                        }
                        if (subField.getCode().equalsIgnoreCase("c")) {
                            startMonth = getMoisFromEnum(subField.getValue());
                        }
                        if (subField.getCode().equalsIgnoreCase("b")) {
                            startDay = Integer.parseInt(subField.getValue());
                        }
                        if (subField.getCode().equalsIgnoreCase("a")) {
                            startYear = Integer.parseInt(subField.getValue());

                        }
                    } catch (IllegalDateException | NumberFormatException ex) {
                        etatCollection.addErreur("Erreur epn " + etatCollection.getEpn() + " : syntaxe de date incorrecte : " + ex.getLocalizedMessage());
                        error = true;
                    }
            }
        }


        if (error) {
            if (startYear != null) {
                SequenceErreur sequenceErreur = new SequenceErreur(startYear, startMonth, startDay, errorMessage);
                etatCollection.addSequence(sequenceErreur);
            }
        } else {
            //ajout du dernier bloc qui n'est pas ajouté en début de boucle
            Sequence sequence = new SequenceLacune(startYear, startMonth, startDay, volume, numero);
            etatCollection.addSequence(sequence);
        }
    }

    private Integer getMoisFromEnum(String value) throws IllegalDateException {
        switch (value) {
            case "jan":
                return Calendar.JANUARY;
            case "fev":
                return Calendar.FEBRUARY;
            case "mar":
                return Calendar.MARCH;
            case "avr":
                return Calendar.APRIL;
            case "mai":
                return Calendar.MAY;
            case "jun":
                return Calendar.JUNE;
            case "jul":
                return Calendar.JULY;
            case "aou":
                return Calendar.AUGUST;
            case "sep":
                return Calendar.SEPTEMBER;
            case "oct":
                return Calendar.OCTOBER;
            case "nov":
                return Calendar.NOVEMBER;
            case "dec":
                return Calendar.DECEMBER;
            default:
                throw new IllegalDateException("Erreur dans la sous zone 'mois' " + value);
        }
    }

    /**
     * Extrait l'année de début de publication
     *
     * @param value zone
     * @return PublicationYear Année de début de publication
     * @throws IllegalPublicationYearException si l'année de publication ne peut pas être décodée
     */
    public AnneePublication buildStartPublicationYear(String value) throws IllegalPublicationYearException {
        String yearCode = value.substring(8, 9);
        String candidateYear;
        switch (yearCode) {
            case "b":
            case "a":
            case "c":
            case "d":
            case "e":
            case "g":
            case "h":
            case "i":
            case "j":
                candidateYear = value.substring(9, 13);
                return extractDate(candidateYear);
            case "f":
                String candidateOldestYear = value.substring(9, 13);
                String candidateNewestYear = value.substring(13, 17);
                return extractCaseF(candidateOldestYear, candidateNewestYear);
            default:
                throw new IllegalPublicationYearException("Unable to decode year code " + yearCode);
        }

    }

    /**
     * Extrait l'année de fin de publication
     *
     * @param value zone
     * @return PublicationYear Année de fin de publication
     * @throws IllegalPublicationYearException si l'année de publication ne peut pas être décodée
     */
    public AnneePublication buildEndPublicationYear(String value) throws IllegalPublicationYearException {
        String yearCode = value.substring(8, 9);
        String candidateYear;

        switch (yearCode) {
            case "b":
                candidateYear = value.substring(13, 17);
                return extractDate(candidateYear);
            case "a":
                candidateYear = value.substring(13, 17);
                if (candidateYear.equals("9999")) {
                    return new AnneePublication(); // Année nulle par défaut
                } else
                    throw new IllegalPublicationYearException("Unable to decode end year code " + yearCode);
            case "c":
            case "d":
                candidateYear = value.substring(13, 17);
                if (candidateYear.equals("    ")) {
                    return new AnneePublication(); // Année nulle par défaut
                } else
                    throw new IllegalPublicationYearException("Unable to decode end year code " + yearCode);
            case "e":
            case "f":
            case "h":
            case "i":
            case "j":
                return new AnneePublication();
            case "g":
                candidateYear = value.substring(13, 17);
                if (candidateYear.equals("9999")) {
                    return new AnneePublication(); // Année nulle par défaut
                } else {
                    return extractDate(candidateYear);
                }
            default:
                throw new IllegalPublicationYearException("Unable to decode year code " + yearCode);
        }
    }

    /**
     * Extrait la date de publication
     *
     * @param candidateYear
     * @return
     * @throws IllegalPublicationYearException
     */
    private AnneePublication extractDate(String candidateYear) throws IllegalPublicationYearException {
        AnneePublication year = new AnneePublication();
        if (candidateYear.equals("    ")) return year;
        if (candidateYear.charAt(2) == ' ' && candidateYear.charAt(3) == ' ') {
            year.setYear(candidateYear.substring(0, 2) + "XX");
            year.setConfidenceIndex(100);
        } else if (candidateYear.charAt(2) == ' ') {
            new IllegalPublicationYearException("Unable to decode year format like" + candidateYear);

        } else if (candidateYear.charAt(3) == ' ') {
            year.setYear(candidateYear.substring(0, 3) + "X");
            year.setConfidenceIndex(10);
        } else {
            year.setYear(candidateYear.substring(0, 4));
            year.setConfidenceIndex(0);
        }
        return year;
    }

    /**
     * Extrait le cas F
     *
     * @param candidateOldestYear
     * @param candidateNewestYear
     * @return
     * @throws IllegalPublicationYearException
     */
    private AnneePublication extractCaseF(String candidateOldestYear, String candidateNewestYear) throws IllegalPublicationYearException {
        int cdtOldestYear = (candidateOldestYear.equals("    ")) ? 0 : Integer.parseInt(candidateOldestYear.trim());
        int cdtNewestYear = (candidateNewestYear.equals("    ")) ? 9999 : Integer.parseInt(candidateNewestYear.trim());
        AnneePublication year = new AnneePublication();
        if (cdtOldestYear > cdtNewestYear) {
            throw new IllegalPublicationYearException("Oldest Year can't be superior to newest Year");
        }
        if (cdtOldestYear == 0) {
            year.setYear(candidateNewestYear);
        } else {
            year.setYear(candidateOldestYear);
        }
        if (cdtNewestYear != 9999 && cdtOldestYear != 0)
            year.setConfidenceIndex(cdtNewestYear - cdtOldestYear);
        else
            year.setConfidenceIndex(0);
        return year;
    }

    /**
     * Extrait le type de ressource continue
     *
     * @param continiousType
     * @return String Type de ressource continue
     */
    public String extractOnGoingResourceType(String continiousType) {

        if (continiousType == null) {
            return TypeRessourceContinue.X;
        }

        switch (continiousType.substring(0, 1).toLowerCase()) {
            case "a":
                return TypeRessourceContinue.A;
            case "b":
                return TypeRessourceContinue.B;
            case "c":
                return TypeRessourceContinue.C;
            case "e":
                return TypeRessourceContinue.E;
            case "f":
                return TypeRessourceContinue.F;
            case "g":
                return TypeRessourceContinue.G;
            case "z":
                return TypeRessourceContinue.Z;
            default:
                return TypeRessourceContinue.X;
        }
    }

    protected Period extractFrequency(String value) {
        switch (value.toUpperCase()) {
            case "A":
                // Quotidienne
                return Period.ofDays(1);
            case "B":
                // Bihebdomadaire
                return Period.ofDays(3);
            case "C":
                // Hebdomadaire
                return Period.ofDays(7);
            case "D":
                // Toutes les deux semaines
                return Period.ofWeeks(2);
            case "E":
                // Bimensuelle
                return Period.ofWeeks(2);
            case "F":
                // Mensuelle
                return Period.ofMonths(1);
            case "G":
                // Bimestrielle
                return Period.ofMonths(2);
            case "H":
                // Trimestrielle
                return Period.ofMonths(3);
            case "I":
                // Trois fois par an
                return Period.ofMonths(4);
            case "J":
                // Semestrielle
                return Period.ofMonths(4);
            case "K":
                return Period.ZERO;
            case "L":
                return Period.ZERO;
            case "M":
                return Period.ZERO;
            case "N":
                return Period.ZERO;
            case "O":
                return Period.ZERO;
            case "P":
                return Period.ZERO;
            case "U":
                return Period.ZERO;
            case "Y":
                return Period.ZERO;
            case "Z":
                // Autre
                return Period.ZERO;
            default:
                return Period.ZERO;
        }
    }

    public String extractSupportType(String typeSupport) {
        if (typeSupport == null) {
            return TypeSupport.X;
        }
        switch (typeSupport.toLowerCase()) {
            case "a":
                return TypeSupport.A;
            case "b":
                return TypeSupport.B;
            case "c":
                return TypeSupport.C;
            case "d":
                return TypeSupport.D;
            case "e":
                return TypeSupport.E;
            case "f":
                return TypeSupport.F;
            case "g":
                return TypeSupport.G;
            case "i":
                return TypeSupport.I;
            case "j":
                return TypeSupport.J;
            case "l":
                return TypeSupport.L;
            case "m":
                return TypeSupport.M;
            case "r":
                return TypeSupport.R;
            default:
                return TypeSupport.X;
        }
    }
}
