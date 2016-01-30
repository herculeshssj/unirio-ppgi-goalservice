<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <!-- Import xalan extensions -->
   <xsl:import href="xalan-ext.xsl"/>

   <!-- Common head template -->
   <xsl:template name="head">
      <xsl:if test="$head.title">
         <title><xsl:value-of select="$head.title"/></title>
      </xsl:if>

      <xsl:if test="$html.stylesheet">
         <link rel="stylesheet" href="{$html.stylesheet}"
            type="{$html.stylesheet.type}"/>
      </xsl:if>

      <meta http-equiv="Expires" content="0"/>
      <meta http-equiv="Pragma" content="no-cache"/>
      <meta http-equiv="Cache-Control" content="no-cache"/>
      <meta name="generator" content="WSMX"/>

   </xsl:template>

   <!-- Common title template -->
   <xsl:template name="toprow">
      <table width="100%" cellpadding="0" cellspacing="0" border="0">
         <tr>
            <td class="darker" colspan="2"/>
         </tr>

         <tr>
            <td class="topheading">
               WSMX
               <br/>
               <div class="subtitle">
                   Management Console
               </div>
            </td>

            <td class="topheading" align="right">
               <a href="http://www.wsmx.org">
                  <img src="wsmx_logo.jpg" width="50" height="50" border="0" alt="WSMX Logo"/>
               </a>               
            </td>
         </tr>

         <tr>
            <td class="darker" colspan="2"/>
         </tr>
      </table>

      <br/>
   </xsl:template>

   <!-- Common bottom template -->
   <xsl:template name="bottom">
      <table width="100%" cellpadding="0" cellspacing="0" border="0">
         <tr>
            <td class="fronttab">&#160;</td>
         </tr>

         <tr>
            <td class="darker"/>
         </tr>         
      </table>
   </xsl:template>

   <!-- Common tabs template -->
   <xsl:template name="tabs">
      <xsl:param name="selection" select="."/>

      <xsl:variable name="mainview.class">
         <xsl:choose>
            <xsl:when test="$selection='mainview'">fronttab</xsl:when>
            <xsl:otherwise>backtab</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="server.class">
         <xsl:choose>
            <xsl:when test="$selection='server'">fronttab</xsl:when>
            <xsl:otherwise>backtab</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="mbean.class">
         <xsl:choose>
            <xsl:when test="$selection='mbean'">fronttab</xsl:when>
            <xsl:otherwise>backtab</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="about.class">
         <xsl:choose>
            <xsl:when test="$selection='about'">fronttab</xsl:when>
            <xsl:otherwise>backtab</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <table cellpadding="0" cellspacing="0" border="0">
         <tr>
	       
         	<td class="{$mainview.class}">
               <a href="/main" title="System information and simple lifecylce operations." class="tabs">
                  Main view
               </a>
            </td>
         
         	<td width="2"/>
            
            <td class="{$server.class}">
                  <a href="/serverbydomain" title="MBeans grouped by domain." class="tabs">
                      Server view
                  </a>
            </td>

            <td width="2"/>

            <td class="{$mbean.class}">
               <a href="/empty?template=emptymbean" title="Attributes, Operations and Constructors of a particular MBean." class="tabs">
                   Component View
               </a>
            </td>

            <td width="2"/>

            <td class="{$about.class}">
               <a href="/main?template=about" title="Metainformation." class="tabs">
                   About
               </a>
            </td>
         </tr>
      </table>
   </xsl:template>

   <xsl:template name="mainview">
      <tr>
         <td class="darkline" align="right">
            <a href="/">
               Return to server view
            </a>
         </td>
      </tr>
   </xsl:template>
   
   <xsl:template name="serverview">
      <tr>
         <td class="darkline" align="right">
            <a href="/">
               Return to server view
            </a>
         </td>
      </tr>
   </xsl:template>

   <xsl:template name="mbeanview">
      <xsl:param name="objectname"/>
      <xsl:param name="colspan">1</xsl:param>
      <xsl:param name="text">Return to MBean view</xsl:param>

      <tr>
         <td class="darkline" align="right" colspan="{$colspan}">
            <xsl:variable name="objectname-encode">
               <xsl:call-template name="uri-encode">
                  <xsl:with-param name="uri" select="$objectname"/>
               </xsl:call-template>
            </xsl:variable>

            <a href="/mbean?objectname={$objectname-encode}">
                <xsl:value-of select="$text"/>
            </a>
         </td>
      </tr>
   </xsl:template>

   <xsl:template name="aggregation-navigation">
      <xsl:param name="url"/>
      <xsl:param name="total"/>
      <xsl:param name="step"/>
      <xsl:param name="start"/>

      <xsl:if test="$total&gt;$step">
         <xsl:variable name="isfirst">
            <xsl:choose>
               <xsl:when test='$start=0'>true</xsl:when>
               <xsl:when test='$start&gt;0'>false</xsl:when>
            </xsl:choose>
         </xsl:variable>
         <xsl:variable name="islast">
            <xsl:choose>
               <xsl:when test='$total&lt;=($step + $start)'>true</xsl:when>
               <xsl:otherwise>false</xsl:otherwise>
            </xsl:choose>
         </xsl:variable>
         <tr>
            <td/>
         </tr>
         <tr>
            <td>
               <xsl:choose>
                  <xsl:when test="$isfirst='false'">
                     <a href="{$url}&amp;start=0">
                        first
                     </a>
                  </xsl:when>
                  <xsl:otherwise>
                     first
                  </xsl:otherwise>
               </xsl:choose>
				 -
               <xsl:choose>
                  <xsl:when test="$isfirst='false'">
                     <xsl:variable name="previndex" select="($start - $step)"/>
                     <a href="{$url}&amp;start={$previndex}">
                         previous
                     </a>
                  </xsl:when>
                  <xsl:otherwise>
                      previous
                  </xsl:otherwise>
               </xsl:choose>
				 -
               <xsl:choose>
                  <xsl:when test="$islast='false'">
                     <xsl:variable name="nextindex" select="($start + $step)"/>
                     <a href="{$url}&amp;start={$nextindex}">
                         next
                     </a>
                  </xsl:when>
                  <xsl:otherwise>
                     next
                  </xsl:otherwise>
               </xsl:choose>
				 -
               <xsl:choose>
                  <xsl:when test="$islast='false'">
                     <xsl:variable name="lastindex" select="($total - ($total mod $step))"/>
                     <a href="{$url}&amp;start={$lastindex}">
                        last
                     </a>
                  </xsl:when>
                  <xsl:otherwise>
                      last
                  </xsl:otherwise>
               </xsl:choose>
            </td>
         </tr>
      </xsl:if>
   </xsl:template>

</xsl:stylesheet>


