package fr.abes.model.io.xml.export;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Représente une notice au format d'export UnimarcXML
 */
@NoArgsConstructor
@Getter
@Setter
@JacksonXmlRootElement(localName = "record")
public class NoticeXMLExport {

    @JacksonXmlProperty(localName = "leader")
    private String leader;

    @JacksonXmlProperty(localName = "controlfield")
    private List<ControlField> controlFields;

    @JacksonXmlProperty(localName = "datafield")
    private List<DataField> dataFields;

    public boolean isRessourceContinue() {

        if (leader.startsWith("d0", 7) || leader.startsWith("s0", 7) || leader.startsWith("i0", 7)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Notice {"+ "leader="+ leader+"}";
    }

}
