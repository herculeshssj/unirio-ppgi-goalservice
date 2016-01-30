<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
   <xsl:include href="common.xsl"/>
   <xsl:include href="xalan-ext.xsl"/>

   <xsl:param name="html.stylesheet">stylesheet.css</xsl:param>
   <xsl:param name="html.stylesheet.type">text/css</xsl:param>
   <xsl:param name="head.title">WSMX - About</xsl:param>

   <!-- Main template -->
   <xsl:template match="/Server">
      <html>
         <xsl:call-template name="head"/>
         <body>
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
               <tr width="100%">
                  <td>
                     <xsl:call-template name="toprow"/>
                     <xsl:call-template name="tabs">
                        <xsl:with-param name="selection">about</xsl:with-param>
                     </xsl:call-template>
                     <table width="100%" cellpadding="0" cellspacing="0" border="0">
                        <tr class="about">
                           <td align="center" colspan="2">
                              <h1 align="center" class="about">
                                     WSMX <xsl:value-of select="@version"/>
                              </h1>

                              <img src="deri_logo.gif" width="48" height="44" border="0" alt="Digital Enterprise Research Institute"/>
                              <br/>
                              Contact: <a href="http://www.deri.ie">www.deri.ie</a>
                           </td>
                        </tr>
                     </table>
                     <xsl:call-template name="bottom"/>
                  </td>
               </tr>
            </table>
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>

