
package org.ipsuper.nexcom.services.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "confirmSLAOfferWS5Response", namespace = "http://ip-super.org/usecase/nexcom/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "confirmSLAOfferWS5Response", namespace = "http://ip-super.org/usecase/nexcom/")
public class ConfirmSLAOfferWS5Response {

    @XmlElement(name = "QoSSupplierSEEResponse", namespace = "http://ip-super.org/usecase/nexcom/")
    private org.ipsuper.nexcom.datamodel.QoSParametersMsg _return;

    /**
     * 
     * @return
     *     returns QoSParametersMsg
     */
    public org.ipsuper.nexcom.datamodel.QoSParametersMsg get_return() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void set_return(org.ipsuper.nexcom.datamodel.QoSParametersMsg _return) {
        this._return = _return;
    }

}
