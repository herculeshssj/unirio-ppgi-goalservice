<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="UTF-8"/>

   <!-- array link generator -->
   <xsl:template name="array">

      <xsl:choose>
         <xsl:when test="@isnull='false'">
            <xsl:variable name="url">getattribute?objectname=
               <xsl:value-of select="../@objectname"/>&amp;attribute=
               <xsl:value-of select="@name"/>&amp;format=array&amp;template=viewarray
            </xsl:variable>
            <a href="{$url}">
               View array
            </a>
         </xsl:when>
         <xsl:otherwise>
            Array is null
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- Collection link generator -->
   <xsl:template name="collection">

      <xsl:choose>
         <xsl:when test="@isnull='false'">
            <xsl:variable name="url">getattribute?objectname=
               <xsl:value-of select="../@objectname"/>&amp;attribute=
               <xsl:value-of select="@name"/>&amp;format=collection&amp;template=viewcollection
            </xsl:variable>
            <a href="{$url}">
               View collection
            </a>
         </xsl:when>
         <xsl:otherwise>
            Collection is null
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- Map link generator -->
   <xsl:template name="map">

      <xsl:choose>
         <xsl:when test="@isnull='false'">
            <xsl:variable name="url">getattribute?objectname=
               <xsl:value-of select="../@objectname"/>&amp;attribute=
               <xsl:value-of select="@name"/>&amp;format=map&amp;template=viewmap
            </xsl:variable>
            <a href="{$url}">
               View map
            </a>
         </xsl:when>
         <xsl:otherwise>
            Map is null
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- Composite data -->
   <xsl:template name="compositedata">

      <xsl:choose>
         <xsl:when test="@isnull='false'">
            <xsl:variable name="url">getattribute?objectname=
               <xsl:value-of select="../@objectname"/>&amp;attribute=
               <xsl:value-of select="@name"/>&amp;format=compositedata&amp;template=identity
            </xsl:variable>
            <a href="{$url}">
                View composite data
            </a>
         </xsl:when>
         <xsl:otherwise>
                Composite data is null
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- Tabular data -->
   <xsl:template name="tabulardata">

      <xsl:choose>
         <xsl:when test="@isnull='false'">
            <xsl:variable name="url">getattribute?objectname=
               <xsl:value-of select="../@objectname"/>&amp;attribute=
               <xsl:value-of select="@name"/>&amp;format=tabulardata&amp;template=viewtabulardata
            </xsl:variable>
            <a href="{$url}">
               View tabular data
            </a>
         </xsl:when>
         <xsl:otherwise>
             Tabular data is null
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="raw-input">
      <xsl:param name="type"/>
      <xsl:param name="value"/>
      <xsl:param name="name"/>
      <xsl:param name="strinit"/>

      <xsl:variable name="result">none</xsl:variable>
      <xsl:choose>
         <xsl:when test="$type='java.lang.Boolean' or $type='boolean'">
            <xsl:choose>
               <xsl:when test="$value='true'">
                  <input name="{$name}" type="radio" checked="checked" value="true">true </input>
                  <input name="{$name}" type="radio" value="false">false </input>
               </xsl:when>
               <xsl:when test="$value='false'">
                  <input name="{$name}" type="radio" value="true">true </input>
                  <input name="{$name}" type="radio" checked="checked" value="false">false </input>
               </xsl:when>
               <xsl:otherwise>
                  <input name="{$name}" type="radio" value="true">true </input>
                  <input name="{$name}" type="radio" value="false">false </input>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
	        <textarea 
  				  name="{$name}" 
				  cols="20"
				  rows="1"
				  wrap="off"></textarea>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template match="Attribute[@type]" name="form">
      <xsl:param name="value"/>
      <xsl:choose>
         <xsl:when test="@strinit='true'">
            <xsl:variable name="name" select="@name"/>
            <xsl:call-template name="raw-input">
               <xsl:with-param name="type" select="@type"/>
               <xsl:with-param name="value" select="$value"/>
               <xsl:with-param name="name">value_
                  <xsl:value-of select="@name"/>
               </xsl:with-param>
               <xsl:with-param name="strinit">true</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="submit">
               <xsl:with-param name="name" select="@name"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            Unknown type
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- Makes the submit button for setting one attribute -->
   <xsl:template match="Attribute[@type]" name="submit">
      <xsl:param name="name"/>
      <xsl:if test="@strinit='true'">
         <input type="Submit" name="set_{$name}" value="set"/>
      </xsl:if>
   </xsl:template>

   <!-- makes a link for objectnames from current value element -->
   <xsl:template name="objectnamevalue">
      <xsl:call-template name="renderobject">
         <xsl:with-param name="objectclass">javax.management.ObjectName</xsl:with-param>
         <xsl:with-param name="objectvalue" select="@value"/>
      </xsl:call-template>
   </xsl:template>

   <!-- Renders an object
      Currently transforms javax.management.ObjectName to links
      Renders others as strings -->
   <xsl:template name="renderobject">
      <xsl:param name="objectclass"/>
      <xsl:param name="objectvalue"/>
      <xsl:choose>
         <xsl:when test="$objectclass='javax.management.ObjectName'">
            <xsl:variable name="name_encoded">
               <xsl:call-template name="uri-encode">
                  <xsl:with-param name="uri">
                     <xsl:value-of select="$objectvalue"/>
                  </xsl:with-param>
               </xsl:call-template>
            </xsl:variable>
            <a href="/mbean?objectname={$name_encoded}" title="View detailed information about this MBean">
               <xsl:value-of select="$objectvalue"/>
            </a>
         </xsl:when>
         <xsl:otherwise>
            <!-- Use the following line when the result of an invocation
            returns e.g. HTML or XML data
            <xsl:value-of select="$objectvalue" disable-output-escaping="true" />
            -->
            <xsl:value-of select="$objectvalue"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template match="Attribute" name="WO">
      <td align="right" class="mbean_row">
         Write-only attribute
      </td>
      <td align="right" class="mbean_row">
         <xsl:call-template name="form"/>
      </td>
   </xsl:template>

   <!-- Template for readwrite attributes -->
   <xsl:template match="Attribute" name="RW">
      <td align="right" class="mbean_row">
         <xsl:choose>
            <xsl:when test="@aggregation='collection'">
               <xsl:call-template name="collection"/>
            </xsl:when>
            <xsl:when test="@aggregation='map'">
               <xsl:call-template name="map"/>
            </xsl:when>
            <xsl:when test="starts-with(@type, '[L')">
               <xsl:call-template name="array"/>
            </xsl:when>
            <xsl:when test="@type='javax.management.ObjectName'">
               <xsl:call-template name="objectnamevalue"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="@value"/>
            </xsl:otherwise>
         </xsl:choose>
      </td>
      <td align="right" class="mbean_row">
         <xsl:call-template name="form">
            <xsl:with-param name="value" select="@value"/>
         </xsl:call-template>
      </td>
   </xsl:template>

   <!-- Template for readonly attributes -->
   <xsl:template match="Attribute" name="RO">
      <td align="right" class="mbean_row">
         <xsl:choose>
            <xsl:when test="@aggregation='collection'">
               <xsl:call-template name="collection"/>
            </xsl:when>
            <xsl:when test="@aggregation='map'">
               <xsl:call-template name="map"/>
            </xsl:when>
            <xsl:when test="starts-with(@type, '[L')">
               <xsl:call-template name="array"/>
            </xsl:when>
            <xsl:when test="@type='javax.management.ObjectName'">
               <xsl:call-template name="objectnamevalue"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="@value"/>
            </xsl:otherwise>
         </xsl:choose>
      </td>
      <td align="right" class="mbean_row">
         Read-only attribute
      </td>
   </xsl:template>

   <!-- MBean's attributes template -->
   <xsl:template name="attribute">
      <table width="100%" cellpadding="0" cellspacing="0" border="0">
         <tr class="darkline">
            <td width="20%" align="left" class="darkline">
               <div class="tableheader">
                  Name
               </div>
            </td>
            <td width="20%" align="left" class="darkline">
               <div class="tableheader">
                  Description
               </div>
            </td>
            <td width="20%" align="left" class="darkline">
               <div class="tableheader">
                  Type
               </div>
            </td>
            <td width="20%" align="right" class="darkline">
               <div class="tableheader">
                  Value
               </div>
            </td>
            <td width="*" align="right" class="darkline">
               <div class="tableheader">
                  New Value
               </div>
            </td>
         </tr>
         <form action="setattributes" method="get">
            <xsl:for-each select="Attribute">
               <xsl:sort data-type="text" order="ascending" select="@name"/>
               <xsl:variable name="classtype">
                  <xsl:if test="(position() mod 2)=1">clearline</xsl:if>
                  <xsl:if test="(position() mod 2)=0">darkline</xsl:if>
               </xsl:variable>
               <tr class="{$classtype}">
                  <td class="mbean_row">
                     <xsl:value-of select="@name"/>
                  </td>
                  <td class="mbean_row">
                     <xsl:value-of select="@description"/>
                  </td>
                  <td class="mbean_row">
                     <xsl:choose>
                        <xsl:when test="starts-with(@type, '[L')">
                           Array of <xsl:value-of select="substring-before(substring-after(@type, '[L'), ';')"/>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:value-of select="@type"/>
                        </xsl:otherwise>
                     </xsl:choose>
                  </td>
                  <xsl:choose>
                     <xsl:when test="@availability='RO'">
                        <xsl:call-template name="RO"/>
                     </xsl:when>
                     <xsl:when test="@availability='RW'">
                        <xsl:call-template name="RW"/>
                     </xsl:when>
                     <xsl:when test="@availability='WO'">
                        <xsl:call-template name="WO"/>
                     </xsl:when>
                  </xsl:choose>
               </tr>
            </xsl:for-each>
            <td colspan="5" align="right" class="attributes_setall">
               <input type="hidden" name="objectname" value="{$request.objectname}"/>
               <input type="Submit" name="setall" title="Set all attributes to the entered values." value="Set all"/>
            </td>
         </form>
      </table>
   </xsl:template>
</xsl:stylesheet>
