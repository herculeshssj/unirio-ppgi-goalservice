<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="UTF-8"/>

   <xsl:param name="html.stylesheet">stylesheet.css</xsl:param>
   <xsl:param name="html.stylesheet.type">text/css</xsl:param>
   <xsl:param name="head.title">WSMX - Main View</xsl:param>
   <xsl:include href="common.xsl"/>
   <xsl:include href="xalan-ext.xsl"/>

   <xsl:template match="Server">
      <html>
         <xsl:call-template name="head"/>
         <body>
            <xsl:call-template name="toprow"/>
            <xsl:call-template name="tabs">
               <xsl:with-param name="selection">mainview</xsl:with-param>
            </xsl:call-template>    
             <table width="100%" cellpadding="0" cellspacing="0" border="0">  
	            <tr>
	                <td class="page_title">
	                    General system information 
                    </td>
	                
	            </tr>        
                <tr><td/></tr>
            </table>                	            
            <table width="100%" cellpadding="0" cellspacing="10" border="0">  

	            <tr> 
	                <td align="left" width="20%" class="lightbackground">
<!--    	               <form action="invoke">
    	                  <input name="operation" type="hidden" value="start"/>
    	                  <input name="objectname" type="hidden" value="core:name=WSMXKernel"/>
    	                  <input type="submit" value="Start" title="Enables the listening for new requests."/>
    	               </form>
    	               
    	               <form action="invoke">
	                        <input name="operation" type="hidden" value="stop"/>
	                        <input name="objectname" type="hidden" value="core:name=WSMXKernel"/>
	                        <input type="submit" value="Stop" title="Disables the listening for new requests."/>
	                   </form>
    	               	
	                   <form action="invoke">
	                        <input name="operation" type="hidden" value="softShutdown"/>
	                        <input name="objectname" type="hidden" value="core:name=WSMXKernel"/>
	                        <input type="submit" value="Softshutdown" title="Bring this instance to a halt, after open requests have been serviced."/>
	                   </form>   
    	               	                     	               
	                   <form action="invoke">
	                        <input name="operation" type="hidden" value="shutdown"/>
	                        <input name="objectname" type="hidden" value="core:name=WSMXKernel"/>
	                        <input type="submit" value="Shutdown" title="Bring this instance to a halt, now."/>
	                   </form>
-->
			   Hostname: 
                        <b>
                            <xsl:value-of select="@wsmx.hostname"/>
                        </b>
			<br/>
			   IP address: 
                        <b>
                            <xsl:value-of select="@wsmx.ipaddress"/>
                        </b>
			<br/>
			   Space address: 
                        <b>
                           <xsl:value-of select="@wsmx.spaceaddress"/>
                        </b>
			<br/><br/>
			    Federation peers: 
                        <b>
                            <xsl:value-of select="@wsmx.federationpeers"/>
                        </b>


                    </td>

	                <td class="lightbackground">
	                    
                        <b>
                            <xsl:value-of select="@wsmx.version"/>
                        </b>
	                    <br/><br/>MBean count: 
                        <b>
                            <xsl:value-of select="@wsmx.mbeans"/>
                        </b>
                        <br/>Domain count: 
                        <b>
                            <xsl:value-of select="@wsmx.domains"/>
                        </b>
            
	                    <br/><br/>Startup:
                        <b>
                            <xsl:value-of select="@wsmx.startuptimestamp.string"/>
                        </b>
                        <br/>Uptime:
                        <b>
                            <xsl:value-of select="@wsmx.uptime.string"/>
                        </b>
                    </td>
                                        
	                <td class="lightbackground">
	                    JVM:<br/>
                        <b>
                            <xsl:value-of select="@java.vm.vendor"/> <br/>
                            <xsl:value-of select="@java.vm.name"/> <br/>
                            <xsl:value-of select="@java.vm.version"/> <br/>
                        </b>
                        <br/>
                 
                            Architecture: <b> <xsl:value-of select="@os.arch"/> </b> <br/>
                            OS: <b> <xsl:value-of select="@os.name"/> ( <xsl:value-of select="@os.version"/> ) </b> <br/>
                        
 
                   </td>                    
	            </tr>           
	  
            </table>
            
            <xsl:call-template name="bottom"/>
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>

