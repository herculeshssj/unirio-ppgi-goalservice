
package org.ipsuper.nexcom.services.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "requestCEOApproval", namespace = "http://ip-super.org/usecase/nexcom/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "requestCEOApproval", namespace = "http://ip-super.org/usecase/nexcom/")
public class RequestCEOApproval {

    @XmlElement(name = "QoSCEORequest", namespace = "http://ip-super.org/usecase/nexcom/")
    private org.ipsuper.nexcom.datamodel.ApprovalRequest QoSCEORequest;

    /**
     * 
     * @return
     *     returns ApprovalRequest
     */
    public org.ipsuper.nexcom.datamodel.ApprovalRequest getQoSCEORequest() {
        return this.QoSCEORequest;
    }

    /**
     * 
     * @param QoSCEORequest
     *     the value for the QoSCEORequest property
     */
    public void setQoSCEORequest(org.ipsuper.nexcom.datamodel.ApprovalRequest QoSCEORequest) {
        this.QoSCEORequest = QoSCEORequest;
    }

}
