<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="html" indent="yes" encoding="UTF-8"/>

   <xsl:param name="html.stylesheet">stylesheet.css</xsl:param>
   <xsl:param name="html.stylesheet.type">text/css</xsl:param>
   <xsl:param name="head.title">WSMX - Exception</xsl:param>
   <xsl:include href="common.xsl"/>

   <xsl:template match="/" name="httpexception">
      <html>
         <xsl:call-template name="head"/>
         <body>
            <xsl:call-template name="toprow"/>
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
               <tr>
                  <td width="100%" class="page_title">
                     Error during request
                  </td>
               </tr>
               <xsl:for-each select="HttpException">
                  <tr>
                     <td>
                        Code: <xsl:value-of select="@code"/>
                     </td>
                  </tr>
                  <tr>
                     <td>
                        Message: <xsl:value-of select="@description"/>
                     </td>
                  </tr>
               </xsl:for-each>
            </table>
            <xsl:call-template name="bottom"/>
         </body>
      </html>
   </xsl:template>
</xsl:stylesheet>

