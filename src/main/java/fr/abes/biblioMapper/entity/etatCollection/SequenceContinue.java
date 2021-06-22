package fr.abes.biblioMapper.entity.etatCollection;

import lombok.Getter;

import java.util.Calendar;

@Getter
public class SequenceContinue extends Sequence {
    protected String startVolume;
    protected String startNumero;

    protected String endVolume;
    protected String endNumero;

    public SequenceContinue(Integer startYear, Integer startMonth, Integer startDay, String startVolume, String startNumero, boolean ouvert) {
        super(startYear, startMonth, startDay);
        if (ouvert) {
            Calendar now = Calendar.getInstance();
            setEndDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        } else {
            setEndDate(startYear, startMonth, startDay, "", "");
        }

        setStartVolume(startVolume);
        setStartNumero(startNumero);
        setEndVolume(startVolume);
        setEndNumero(startNumero);
    }

    public SequenceContinue(Integer startYear, Integer startMonth, Integer startDay, String startVolume, String startNumero, Integer endYear, Integer endMonth, Integer endDay, String endVolume, String endNumero) {
        super(startYear, startMonth, startDay, endYear, endMonth, endDay);
        setStartVolume(startVolume);
        setStartNumero(startNumero);
        setEndVolume(endVolume);
        setEndNumero(endNumero);
    }

    public void setStartDate(Integer startYear, Integer startMonth, Integer startDay, String volume, String numero) {
        this.setStartDate(startYear, startMonth, startDay);
        setStartVolume(volume);
        setStartNumero(numero);
    }

    public void setEndDate(Integer endYear, Integer endMonth, Integer endDay, String volume, String numero) {
        this.setEndDate(endYear, endMonth, endDay);
        setEndVolume(volume);
        setEndNumero(numero);
    }

    public void setStartVolume(String value) {
        if (value == null || value.isEmpty()) {
            this.startVolume = "Non renseigné";
        } else {
            this.startVolume = value;
        }
    }

    public void setStartNumero(String value) {
        if (value == null || value.isEmpty()) {
            this.startNumero = "Non renseigné";
        } else {
            this.startNumero = value;
        }
    }

    public void setEndVolume(String value) {
        if (value == null || value.isEmpty()) {
            this.endVolume = "Non renseigné";
        } else {
            this.endVolume = value;
        }
    }

    public void setEndNumero(String value) {
        if (value == null || value.isEmpty()) {
            this.endNumero = "Non renseigné";
        } else {
            this.endNumero = value;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "SequenceContinue {" + "startDate=" + startDate.getTime() + ", endDate=" + endDate.getTime() + ", startVolume=" + startVolume + ", endVolume=" + endVolume + "}";
    }

}
