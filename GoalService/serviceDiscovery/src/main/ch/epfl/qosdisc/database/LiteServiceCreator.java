/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.database;

import java.io.*;

public class LiteServiceCreator {

	public static void createService(int ndx) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		String fname = "c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/Service"+ndx+".wsml";

		sb.append("wsmlVariant _\"http://www.wsmo.org/wsml/wsml-syntax/wsml-flight\"\n");
		sb.append("\n");
		sb.append("namespace { _\"file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/Service"+ndx+".wsml#\",\n");
		sb.append("            qos _\"file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/QoSBase.wsml#\",\n");
		sb.append("            file _\"file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/FileQoSBase.wsml#\",\n");
		sb.append("            dc _\"http://purl.org/dc/elements/1.1#\",\n");
		sb.append("            wsml _\"http://www.wsmo.org/wsml/wsml-syntax#\" }\n");
		sb.append("\n");
		sb.append("webService Service"+ndx+"\n");
		sb.append(" importsOntology { _\"file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/QoSBase.wsml#\" }\n");
		sb.append("\n");
		sb.append("capability ServiceCapability\n");
		sb.append(" postcondition definedBy ?serviceType memberOf qos#ServiceType"+((ndx&1)+1)+" .\n");
		sb.append("\n");
		sb.append("interface Iface\n");
		sb.append(" importsOntology { IParam }\n");
		sb.append(" \n");
		sb.append("ontology IParam\n");
		sb.append(" importsOntology { _\"file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/FileQoSBase.wsml#\" }\n");
		sb.append(" \n");
		sb.append(" instance a memberOf { file#UploadSpeed, qos#ServiceSpec }\n");
		sb.append("  qos#value hasValue "+(int)(Math.random()*500+500)+"\n");
		sb.append("  qos#unit hasValue file#KBps\n");
		sb.append("  \n");
		sb.append(" instance b memberOf { file#DownloadSpeed, qos#ServiceSpec }\n");
		sb.append("  qos#value hasValue "+(int)(Math.random()*500+500)+"\n");
		sb.append("  qos#unit hasValue file#KBps\n");
		sb.append("  \n");
		sb.append(" instance c memberOf { qos#Availability, qos#ServiceSpec }\n");
		sb.append("  qos#value hasValue "+(Math.random()*0.2+0.8)+"\n");
		sb.append("  qos#unit hasValue qos#Percentage\n");
		sb.append("  \n");
		sb.append(" instance d memberOf { qos#MaxDownTime, qos#ServiceSpec }\n");
		sb.append("  qos#value hasValue "+(int)(Math.random()*10+1)+"\n");
		sb.append("  qos#unit hasValue qos#Second\n");
		
		Writer w = new FileWriter(fname);
		w.write(sb.toString());
		w.close();
		                         
		WSMLStore.importWSMLFromString(sb,false);
	}
}
