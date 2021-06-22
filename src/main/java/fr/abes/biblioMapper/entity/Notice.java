package fr.abes.biblioMapper.entity;

import fr.abes.biblioMapper.entity.donneesCodees.AnneePublication;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente une Notice au format Periscope
 */
@Getter @Setter
public abstract class Notice {

    protected String ppn;

    protected String publisher;

    protected String keyTitle;

    protected String supportType;

    protected String language;

    protected String country;

    protected String mirabelURL;

    protected Integer nbLocation;

    protected Set<String> pcpList = new HashSet<>();

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

        return ppn != null && ppn.equals(((Notice) obj).ppn);
    }

    @Override
    public int hashCode() {
        return 2020;
    }

    @Override
    public String toString() {
        return "Notice {"+ "ppn="+ ppn+"}";
    }

}
