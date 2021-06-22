package fr.abes.biblioMapper.entity.etatCollection;

import fr.abes.biblioMapper.exception.IllegalDateException;

import java.time.Period;
import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class Sequence implements Cloneable {
    protected Calendar startDate;
    protected Calendar endDate;
    private boolean closedInterval = false;
    protected String note;
    private boolean updateToFrequency = false;

    public Sequence(Integer startYear, Integer startMonth, Integer startDay) {
        this.setStartDate(startYear, startMonth, startDay);
        this.setEndDate(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
    }

    public Sequence(Integer startYear, Integer startMonth, Integer startDay, Integer endYear, Integer endMonth, Integer endDay) {
        this.setStartDate(startYear, startMonth, startDay);
        this.setEndDate(endYear, endMonth, endDay);
    }

    protected void setStartDate(Integer startYear, Integer startMonth, Integer startDay) {
        if (startYear != null && startMonth != null && startDay != null) {
            this.startDate = new GregorianCalendar(startYear, startMonth, startDay);
        } else if (startYear != null && startMonth != null && startDay == null) {
            //si le mois est renseigné sans le jour, on renseigne le premier jour du mois
            //peut arriver sur des revues non quotidiennes
            this.startDate = new GregorianCalendar(startYear, startMonth, 1);
        } else if (startYear != null && startMonth == null && startDay != null) {
            this.startDate = new GregorianCalendar(startYear, Calendar.JANUARY, startDay);
        } else if (startYear != null && startMonth == null && startDay == null) {
            this.startDate = new GregorianCalendar(startYear, Calendar.JANUARY, 1);
        } else {
            throw new IllegalDateException("Start date of the sequence is not valid.");
        }

        this.checkDate();
    }

    public boolean isUpdateToFrequency() {
        return updateToFrequency;
    }

    public Calendar getStartDate() {
        return this.startDate;
    }

    void setEndDate(Integer endYear, Integer endMonth, Integer endDay) {
        if (endYear == null && endMonth == null && endDay == null) {
            return;
        }

        if (endYear != null && endMonth != null && endDay != null) {
            this.endDate = new GregorianCalendar(endYear, endMonth, endDay);
        } else if (endYear != null && endMonth != null && endDay == null) {
            //si le mois est renseigné sans le jour, on renseigne le dernier jour du mois dans la date de fin
            //peut arriver sur des revues non quotidiennes
            this.endDate = new GregorianCalendar(endYear, endMonth, 1);
            this.endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else if (endYear != null && endMonth == null && endDay != null) {
            this.endDate = new GregorianCalendar(endYear, Calendar.DECEMBER, endDay);
        } else if (endYear != null && endMonth == null && endDay == null) {
            this.endDate = new GregorianCalendar(endYear, Calendar.DECEMBER, 31);
        } else {
            throw new IllegalDateException("Unable to decode the end date");
        }

        this.closedInterval = true;

        this.checkDate();
    }

    public Calendar getEndDate() {
        return this.endDate;
    }

    public boolean isClosedInterval() {
        return this.closedInterval;
    }

    private void checkDate() {
        if (startDate != null && endDate != null) {
            if (startDate.after(endDate)) {
                // On échange les dates
                Calendar temp = endDate;
                endDate = startDate;
                startDate = temp;
            }
        }
    }

    /**
     * Permet la mise à jour de la date de fin en fonction de la périodicité de la publication
     * @param frequency période à ajouter à la date de début pour obtenir la date de fin
     * @return flag de modification de la date de fin pour éviter une modification ultérieure en cas de repassage
     */
    public boolean updateToFrequency(Period frequency) {
        //on ne met à jour la date de fin que si elle est égale à la date de début et qu'on ne l'a pas déjà modifiée lors d'un appel précédent
        if (!this.updateToFrequency && endDate.equals(startDate)) {
            endDate.add(Calendar.DAY_OF_MONTH, frequency.getDays());
            endDate.add(Calendar.MONTH, frequency.getMonths());
            endDate.add(Calendar.YEAR, frequency.getYears());
            this.updateToFrequency = true;
        }
        return this.updateToFrequency;
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

        return startDate.equals(((Sequence) obj).startDate) && endDate.equals(((Sequence) obj).endDate);
    }

    @Override
    public String toString() {
        if (isClosedInterval()) {
            return "Sequence {" + "startDate=" + startDate.getTime() + ", endDate=" + endDate.getTime() + "}";
        } else {
            return "Sequence {" + "startDate=" + startDate.getTime() + ", endDate=undefined }";
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
