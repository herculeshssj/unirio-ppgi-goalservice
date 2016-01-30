
package org.ipsuper.nexcom.services.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "confirmSLAOfferWS5", namespace = "http://ip-super.org/usecase/nexcom/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "confirmSLAOfferWS5", namespace = "http://ip-super.org/usecase/nexcom/")
public class ConfirmSLAOfferWS5 {

    @XmlElement(name = "QoSSupplierSEERequest", namespace = "http://ip-super.org/usecase/nexcom/")
    private org.ipsuper.nexcom.datamodel.QoSParametersMsg QoSSupplierSEERequest;

    /**
     * 
     * @return
     *     returns QoSParametersMsg
     */
    public org.ipsuper.nexcom.datamodel.QoSParametersMsg getQoSSupplierSEERequest() {
        return this.QoSSupplierSEERequest;
    }

    /**
     * 
     * @param QoSSupplierSEERequest
     *     the value for the QoSSupplierSEERequest property
     */
    public void setQoSSupplierSEERequest(org.ipsuper.nexcom.datamodel.QoSParametersMsg QoSSupplierSEERequest) {
        this.QoSSupplierSEERequest = QoSSupplierSEERequest;
    }

}
