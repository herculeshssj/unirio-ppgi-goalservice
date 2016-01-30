<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="UTF-8"/>

   <xsl:param name="html.stylesheet">stylesheet.css</xsl:param>
   <xsl:param name="html.stylesheet.type">text/css</xsl:param>
   <xsl:param name="head.title">WSMX - Collection View</xsl:param>
   <xsl:param name="request.step">30</xsl:param>
   <xsl:param name="request.start">0</xsl:param>
   <xsl:param name="request.objectname"/>
   <xsl:param name="request.attribute"/>
   <xsl:param name="request.format"/>
   <xsl:param name="request.template"/>
   <xsl:include href="common.xsl"/>
   <xsl:include href="mbean_attributes.xsl"/>

   <!-- Main template -->
   <xsl:template match="/" name="main">
      <html>
         <xsl:call-template name="head"/>
         <body>
            <xsl:call-template name="toprow"/>
            <xsl:call-template name="tabs">
               <xsl:with-param name="selection">mbean</xsl:with-param>
            </xsl:call-template>
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
               <tr>
                  <td colspan="3" class="page_title">
                      MBean <xsl:value-of select="MBean/@objectname"/>
                  </td>
               </tr>
               <tr>
                  <td colspan="3" class="fronttab">
                     Collection of class <xsl:value-of select="MBean/Attribute/@classname"/>
                      on attribute <xsl:value-of select="MBean/Attribute/@name"/>
                  </td>
               </tr>
               <tr>
                  <td class="fronttab">
                     <div class="tableheader">
                        Index
                     </div>
                  </td>
                  <td class="fronttab">
                     <div class="tableheader">
                        Value
                     </div>
                  </td>
                  <td class="fronttab">
                     <div class="tableheader">
                        Value class
                     </div>
                  </td>
               </tr>

               <xsl:for-each select="MBean/Attribute/Collection/Element">
                  <xsl:sort order="ascending" select="@index"/>
                  <xsl:if test="position()&lt;($request.step+$request.start) and position()&gt;$request.start">
                     <xsl:variable name="classtype">
                        <xsl:if test="(position() mod 2)=1">clearline</xsl:if>
                        <xsl:if test="(position() mod 2)=0">darkline</xsl:if>
                     </xsl:variable>
                     <tr class="{$classtype}">
                        <td class="aggregationrow">
                           <xsl:value-of select="@index"/>
                        </td>
                        <td class="aggregationrow">
                           <xsl:call-template name="renderobject">
                              <xsl:with-param name="objectclass" select="@elementclass"/>
                              <xsl:with-param name="objectvalue" select="@element"/>
                           </xsl:call-template>
                        </td>
                        <td class="aggregationrow">
                           <xsl:value-of select="@elementclass"/>
                        </td>
                     </tr>
                  </xsl:if>
               </xsl:for-each>

               <xsl:variable name="url">getattribute?objectname=
                  <xsl:call-template name="uri-encode">
                     <xsl:with-param name="uri">
                        <xsl:value-of select="$request.objectname"/>
                     </xsl:with-param>
                  </xsl:call-template>&amp;attribute=
                  <xsl:value-of select="$request.attribute"/>&amp;format=collection&amp;template=viewcollection&amp;locale=
                  <xsl:value-of select="$request.locale"/>
               </xsl:variable>
               <xsl:call-template name="aggregation-navigation">
                  <xsl:with-param name="url" select="$url"/>
                  <xsl:with-param name="total" select="count(MBean/Attribute/Collection/Element)"/>
                  <xsl:with-param name="start" select="$request.start"/>
                  <xsl:with-param name="step" select="$request.step"/>
               </xsl:call-template>

               <xsl:call-template name="mbeanview">
                  <xsl:with-param name="objectname" select="MBean/@objectname"/>
                  <xsl:with-param name="colspan" select="3"/>
               </xsl:call-template>
            </table>

            <xsl:call-template name="bottom"/>
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>

