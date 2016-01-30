
package org.ipsuper.nexcom.services.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "requestLegalApproval", namespace = "http://ip-super.org/usecase/nexcom/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "requestLegalApproval", namespace = "http://ip-super.org/usecase/nexcom/")
public class RequestLegalApproval {

    @XmlElement(name = "QoSLegalRequest", namespace = "http://ip-super.org/usecase/nexcom/")
    private org.ipsuper.nexcom.datamodel.ApprovalRequest QoSLegalRequest;

    /**
     * 
     * @return
     *     returns ApprovalRequest
     */
    public org.ipsuper.nexcom.datamodel.ApprovalRequest getQoSLegalRequest() {
        return this.QoSLegalRequest;
    }

    /**
     * 
     * @param QoSLegalRequest
     *     the value for the QoSLegalRequest property
     */
    public void setQoSLegalRequest(org.ipsuper.nexcom.datamodel.ApprovalRequest QoSLegalRequest) {
        this.QoSLegalRequest = QoSLegalRequest;
    }

}
