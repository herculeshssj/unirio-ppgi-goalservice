<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
   <xsl:include href="common.xsl"/>
   <xsl:include href="xalan-ext.xsl"/>

   <xsl:param name="html.stylesheet">stylesheet.css</xsl:param>
   <xsl:param name="html.stylesheet.type">text/css</xsl:param>
   <xsl:param name="head.title">WSMX - Server View</xsl:param>

   <!-- Invoked when a query error is produced -->
   <xsl:template match="Domain" name="error">
      <xsl:for-each select="Exception">
         <tr>
            <td class="serverbydomain_domainline" colspan="5">
                Exception during query: <xsl:value-of select="@errorMsg"/>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>

   <!-- Invoked to display each domain -->
   <xsl:template match="Domain" name="domain">
      <xsl:for-each select="Domain">
         <xsl:sort data-type="text" order="ascending" select="@name"/>
         <tr>
            <td class="serverbydomain_domainline" colspan="5">
                Domain: <xsl:value-of select="@name"/>
            </td>
            <xsl:call-template name="mbean"/>
         </tr>
      </xsl:for-each>
   </xsl:template>

   <!-- invoked for each mbean -->
   <xsl:template match="MBean" name="mbean">
      <xsl:for-each select="MBean">
         <xsl:sort data-type="text" order="ascending" select="@objectname"/>
         <xsl:variable name="classtype">
            <xsl:if test="(position() mod 2)=1">darkline</xsl:if>
            <xsl:if test="(position() mod 2)=0">clearline</xsl:if>
         </xsl:variable>
         <xsl:variable name="objectname">
            <xsl:call-template name="uri-encode">
               <xsl:with-param name="uri" select="@objectname"/>
            </xsl:call-template>
         </xsl:variable>
 
         <tr class="{$classtype}" width="100%">
            <td width="35%" align="left" class="serverbydomain_row">
               <a href="mbean?objectname={$objectname}" title="Show detailed information for this MBean">
                  <xsl:choose>
	                  <xsl:when test="starts-with(@objectname, 'components') = 'true'">
	                  	<img src="threecubes_32.png" width="32" height="32" border="0" alt="Component" />
	                  </xsl:when>
	                  <xsl:when test="starts-with(@objectname, 'classloaders') = 'true'">
	                  	<img src="gears_32.png" width="32" height="32" border="0" alt="Component" />
	                  </xsl:when>
	                  <xsl:when test="starts-with(@objectname, 'loggers') = 'true'">
	                  	<img src="notepad_32.png" width="32" height="32" border="0" alt="Component" />
	                  </xsl:when>
	                  <xsl:when test="starts-with(@objectname, 'schedulers') = 'true'">
	                  	<img src="blackcube_32.png" width="32" height="32" border="0" alt="Component" />
	                  </xsl:when>
	                  <xsl:when test="starts-with(@objectname, 'core:name=WSMXKernel') = 'true'">
	                  	<img src="cpu_32.png" width="32" height="32" border="0" alt="Component" />
	                  </xsl:when>
	                  <xsl:when test="starts-with(@objectname, 'core') = 'true'">
	                  	<img src="memory_32.png" width="32" height="32" border="0" alt="Component" />
	                  </xsl:when>
	                  
<!--	              <xsl:otherwise>
	                  	<img src="bluecube_32.png" width="32" height="32" border="0" alt="Component" />
	                  </xsl:otherwise>                  
-->				  </xsl:choose>
                  <xsl:value-of select="@objectname"/>
               </a>
            </td>
            <td width="20%" align="left" class="serverbydomain_row">
               <p>               
  				  <xsl:value-of select="@classname"/>
               </p>
            </td>
            <td width="35%" align="left" class="serverbydomain_row">
               <p>
                  <xsl:value-of select="@description"/>
               </p>
            </td>
            <td width="10%" align="right" class="serverbydomain_row">
               <p>
                  <a href="delete?objectname={$objectname}" title="Unregister this MBean from the MBeanServer">
                      Unregister
                  </a>
               </p>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>

   <!-- Main template -->
   <xsl:template match="Server">
      <html>
         <xsl:call-template name="head"/>
         <body>
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
               <tr width="100%">
                  <td>
                     <xsl:call-template name="toprow"/>
                     <xsl:call-template name="tabs">
                        <xsl:with-param name="selection">server</xsl:with-param>
                     </xsl:call-template>
                     <table width="100%" cellpadding="0" cellspacing="0" border="0">

                        <tr>
                           <td class="page_title">
                              MBean By Domain:
                           </td>
                           <form action="serverbydomain">
                              <td align="right" class="page_title">
                                 Filter:
                                 <input type="text" name="querynames" value="*:*"/>
                                 <input type="submit" title="Allows you to view a subset of all MBeans based on domain or properties." value="Query"/>
                              </td>
                           </form>
                        </tr>
                     </table>
                     <table width="100%" cellpadding="0" cellspacing="0" border="0">
                        <xsl:call-template name="domain"/>
                        <xsl:call-template name="error"/>
                     </table>
                     <xsl:call-template name="bottom"/>
                  </td>
               </tr>
            </table>
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>

