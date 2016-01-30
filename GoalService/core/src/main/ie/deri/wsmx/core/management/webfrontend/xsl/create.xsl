<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="UTF-8"/>

   <!-- Overall parameters -->
   <xsl:param name="html.stylesheet">stylesheet.css</xsl:param>
   <xsl:param name="html.stylesheet.type">text/css</xsl:param>
   <xsl:param name="head.title">WSMX - MBean View</xsl:param>

   <!-- Request parameters -->
   <xsl:param name="request.objectname"/>
   <xsl:param name="request.class"/>

   <xsl:include href="common.xsl"/>

   <!-- Operation processing template -->
   <xsl:template name="operation">
      <xsl:for-each select="Operation">
         <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>
               <td width="100%" class="page_title">
                  MBean operation: create MBean of class <xsl:value-of select="$request.class"/>
                    and object name <xsl:value-of select="$request.objectname"/>
               </td>
            </tr>
            <tr class="darkline">
               <td>
                  <xsl:if test="@result='success'">
                     MBean successfully created
                  </xsl:if>
                  <xsl:if test="@result='error'">
                        Error during MBean creation, message: <xsl:value-of select="@errorMsg"/>
                  </xsl:if>
               </td>
            </tr>
            <xsl:call-template name="serverview"/>
         </table>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="MBeanOperation">
      <html>
         <xsl:call-template name="head"/>
         <body>
            <xsl:call-template name="toprow"/>
            <xsl:call-template name="tabs">
               <xsl:with-param name="selection">mbean</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="operation"/>
            <xsl:call-template name="bottom"/>
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>

