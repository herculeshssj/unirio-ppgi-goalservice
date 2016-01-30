<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="UTF-8"/>

   <xsl:param name="html.stylesheet">stylesheet.css</xsl:param>
   <xsl:param name="html.stylesheet.type">text/css</xsl:param>
   <xsl:param name="head.title">WSMX - MBean View</xsl:param>
   <xsl:include href="common.xsl"/>

   <!-- Request parameters -->
   <xsl:param name="request.objectname"/>
   <xsl:param name="request.attribute"/>
   <xsl:param name="request.value"/>

   <xsl:template name="operation">
      <xsl:for-each select="Operation">
         <table width="100%" cellpadding="0" cellspacing="0" border="0">

            <xsl:variable name="classtype">
               <xsl:if test="(position() mod 2)=1">darkline</xsl:if>
               <xsl:if test="(position() mod 2)=0">clearline</xsl:if>
            </xsl:variable>
            <tr>
               <td width="100%" class="page_title">
                  MBean operation: set attributes on MBean <xsl:value-of select="$request.objectname"/>
               </td>
            </tr>
            <xsl:for-each select="//Attribute">
               <xsl:if test="@result='success'">
                  <tr class="{$classtype}">
                     <td>
                        Attribute <xsl:value-of select="@attribute"/>
                         set to <xsl:value-of select="@value"/>
                     </td>
                  </tr>
               </xsl:if>
               <xsl:if test="@result='error'">
                  <tr class="{$classtype}">
                     <td>
                        Exception during set attribute <xsl:value-of select="@attribute"/>
                        , message: <xsl:value-of select="@errorMsg"/>
                     </td>
                  </tr>
               </xsl:if>
            </xsl:for-each>
            <xsl:call-template name="mbeanview">
               <xsl:with-param name="objectname" select="$request.objectname"/>
            </xsl:call-template>
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

