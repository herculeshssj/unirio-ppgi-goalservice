package com.isoco.dip.adapter;
public class BrokerConstants {

	static final String recommendationStock = "ENTITY"; //Stock.stockISIN
	static final String recommendationRecommendation = "RECOMMENDATION"; //Recommendation.RecommendationType
	
	static final String StrikeIronCompanyName = "CompanyName"; //Stock.name
	static final String StrikeIronStockTicker = "StockTicker"; //Stock.ticker
	static final String StrikeIronStockQuote = "StockQuote"; //Quote.hasPrice
	static final String StrikeIronLastUpdated = "LastUpdated"; //Quote.hasdate
	static final String StrikeIronOpenPrice ="OpenPrice"; //Quote.hasOpenPrice
	static final String StrikeIronDayHighPrice = "DayHighPrice"; //Quote.hasHighPriceInSession
	static final String StrikeIronDayLowPrice = "DayLowPrice"; //Quote.hasLowPriceInSession
	static final String StrikeIronVolume = "Volume"; //Quote.hasVolume
	static final String StrikeIronYearRange = "YearRange"; //[Quote.hasMinimumYear - Quote.hasMaximumYear]
	
	
	
	static final String SOAPRECOMMENDATION = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><getRecommendationsResponse xmlns=\"https://aia.ebankinter.com/wsBrokerService/\"><getRecommendationsResult><ROOT xmlns=\"\"><ERRORCODE>00</ERRORCODE><CONTENT><ENTITY>US17275R1023</ENTITY><RECOMMENDATION>KEEP</RECOMMENDATION><DATE>19/10/2006 11:13:03</DATE></CONTENT></ROOT></getRecommendationsResult></getRecommendationsResponse></soap:Body></soap:Envelope>";
	static final String SOAPGETQUOTESTRIKEIRON = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><Header xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><SubscriptionInfo xmlns=\"http://ws.strikeiron.com\"><LicenseStatusCode>1</LicenseStatusCode><LicenseStatus>Valid key</LicenseStatus><LicenseActionCode>0</LicenseActionCode><LicenseAction>Decremented hit count</LicenseAction><RemainingHits>4</RemainingHits><Amount>0</Amount></SubscriptionInfo></Header><soap:Body><GetQuotesResponse xmlns=\"http://swanandmokashi.com\"><GetQuotesResult><Quote><CompanyName>ORACLE CORP</CompanyName><StockTicker>ORCL</StockTicker><StockQuote>18.91</StockQuote><LastUpdated>10/20/2006 1:32pm</LastUpdated><Change>+0.04</Change><OpenPrice>18.94</OpenPrice><DayHighPrice>19.02</DayHighPrice><DayLowPrice>18.68</DayLowPrice><Volume>15068695</Volume><MarketCap>98.246B</MarketCap><YearRange>11.75 - 19.14</YearRange><ExDividendDate>13-Oct-00</ExDividendDate><DividendYield>N/A</DividendYield><DividendPerShare>0.00</DividendPerShare><PercentChange>+0.21%</PercentChange></Quote></GetQuotesResult></GetQuotesResponse></soap:Body></soap:Envelope>";
	static final String SOAPGETQUOTEBANKINTER = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><searchValueResponse xmlns=\"https://aia.ebankinter.com/wsBrokerService/\"><searchValueResult><ROOT xmlns=\"\"><ERRORCODE>00</ERRORCODE><CONTENT><CotizacionPageCtrl cat=\"0\"/><MERCADOS><MERCADO><NOM8_MERCADO>AMEX</NOM8_MERCADO><COD_MERCADO>066</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>AMSTERDAM</NOM8_MERCADO><COD_MERCADO>038</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>BRUSELAS</NOM8_MERCADO><COD_MERCADO>011</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>COPENHAGUE</NOM8_MERCADO><COD_MERCADO>012</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>DUBLIN</NOM8_MERCADO><COD_MERCADO>267</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>ESTOCOLMO</NOM8_MERCADO><COD_MERCADO>053</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>HELSINKI</NOM8_MERCADO><COD_MERCADO>040</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>ITALIA</NOM8_MERCADO><COD_MERCADO>046</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>LISBOA</NOM8_MERCADO><COD_MERCADO>051</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>LONDRES</NOM8_MERCADO><COD_MERCADO>036</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>LONDRES SETS</NOM8_MERCADO><COD_MERCADO>361</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>MEFF/EUREX</NOM8_MERCADO><COD_MERCADO>900</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>MERCADO CONTINUO</NOM8_MERCADO><COD_MERCADO>055</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>NASDAQ</NOM8_MERCADO><COD_MERCADO>067</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>NASDAQ OTC BULLETIN</NOM8_MERCADO><COD_MERCADO>130</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>NUEVA YORK</NOM8_MERCADO><COD_MERCADO>065</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>OSLO</NOM8_MERCADO><COD_MERCADO>048</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>OTHER OTC US</NOM8_MERCADO><COD_MERCADO>365</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>PARIS</NOM8_MERCADO><COD_MERCADO>025</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>STOXX (SOLO PARA INDICES)</NOM8_MERCADO><COD_MERCADO>485</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>SUIZA</NOM8_MERCADO><COD_MERCADO>004</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>SWX INDICES (SOLO PARA INDICES)</NOM8_MERCADO><COD_MERCADO>611</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>TOKIO</NOM8_MERCADO><COD_MERCADO>106</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>TORONTO</NOM8_MERCADO><COD_MERCADO>061</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>VIENA</NOM8_MERCADO><COD_MERCADO>050</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>VIRT-X</NOM8_MERCADO><COD_MERCADO>220</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>XETRA</NOM8_MERCADO><COD_MERCADO>022</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>XETRA EUROPEAN STARS</NOM8_MERCADO><COD_MERCADO>264</COD_MERCADO></MERCADO><MERCADO><NOM8_MERCADO>XETRA US STARS</NOM8_MERCADO><COD_MERCADO>227</COD_MERCADO></MERCADO></MERCADOS><CotizacionPageCtrlMercado cat=\"3\"/><STOCKS><STOCK><COD_MERCADO>067</COD_MERCADO><CODIGO_ISIN>US68389X1054</CODIGO_ISIN><NOMBRE_MERCADO>NASDAQ                                  </NOMBRE_MERCADO><NOMBRE_VALOR>ORACLE</NOMBRE_VALOR><FECHA_COTIZACION>19:29</FECHA_COTIZACION><FECHA_COTIZACION_COMP_ACCIONES>2006-10-20</FECHA_COTIZACION_COMP_ACCIONES><HORA_COTIZACION_COMP_ACCIONES> 19:29</HORA_COTIZACION_COMP_ACCIONES><PRECIO_APERTURA>-</PRECIO_APERTURA><PRECIO_ULTIMO>18.9</PRECIO_ULTIMO><PRECIO_ANTERIOR>18.87</PRECIO_ANTERIOR><DIF_CIERRE_ANTERIOR>0.03</DIF_CIERRE_ANTERIOR><TPC_CIERRE_ANTERIOR>0.15898251192368839657120815900270827114582061767578125</TPC_CIERRE_ANTERIOR><MAX_SESION>19.02</MAX_SESION><MIN_SESION>18.68</MIN_SESION><VOLUMEN>14985244</VOLUMEN><DIV_COTIZACION>USD</DIV_COTIZACION><COD9_VALOR_CFI>ORCL</COD9_VALOR_CFI><CLAVE_BK>850048</CLAVE_BK><IND_OF></IND_OF></STOCK></STOCKS><salida>S</salida><metodo>porambos</metodo><mercado>067</mercado><nombre>ORACLE</nombre><ocurrencias>30</ocurrencias></CONTENT></ROOT></searchValueResult></searchValueResponse></soap:Body></soap:Envelope>";
	static final String SOAPGETQUOTEXIGNITE = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><GetQuoteResponse xmlns=\"http://www.xignite.com/services/\"><GetQuoteResult><Outcome>Success</Outcome><Identity>Header</Identity><Delay>1.984375</Delay><Name>AMAZON.COM INC</Name><Exchange>NASDAQ</Exchange><Quote><Symbol>AMZN</Symbol><Previous_Close>32.54</Previous_Close><Open>32.54</Open><High>32.54</High><Low>32.54</Low><Last>32.54</Last><Bid>30.83</Bid><Bid_Size>N/A</Bid_Size><Ask>32.80</Ask><Ask_Size>N/A</Ask_Size><Percent_Change>0.00</Percent_Change><Change>0.00</Change><Volume>0</Volume><High_52_Weeks>50.00</High_52_Weeks><Low_52_Weeks>25.76</Low_52_Weeks><Date>10/19/2006</Date><Time>4:00:00 PM</Time></Quote><Statistics><Price_Earnings>45.96</Price_Earnings><Price_Sales>1.47</Price_Sales><Market_Cap>13.632B</Market_Cap><EPS>0.42</EPS><EPS_Estimate>0.22</EPS_Estimate></Statistics><Chart>http://data.moneycentral.msn.com/scripts/chrtsrv.dll?Symbol=AMZN&amp;C1=1&amp;C2=&amp;C3=1&amp;C4=3C9=1&amp;Width=352&amp;Height=184&amp;legend=0&amp;banner=2</Chart><News><StockNews><Outcome>Success</Outcome><Delay>0</Delay><Headline>Google earnings and sales beat expectations soundly </Headline><Ticker>AMZN</Ticker><Date>10/19/2006</Date><Time>1:35 PM</Time><Source>CNNMoney.com</Source><Url>http://money.cnn.com/2006/10/19/technology/google_earnings/index.htm</Url></StockNews><StockNews><Outcome>Success</Outcome><Delay>0</Delay><Headline>Where's Elmo? Toy remains hard to find; prices soar </Headline><Ticker>AMZN</Ticker><Date>10/19/2006</Date><Time>12:45 PM</Time><Source>Reuters</Source><Url>http://yahoo.reuters.com/financeQuoteCompanyNewsArticle.jhtml?duid=mtfh66280_2006-10-19_19-45-17_n18396969_newsml</Url></StockNews></News></GetQuoteResult></GetQuoteResponse></soap:Body></soap:Envelope>";
	
	
	
	static final String fromSOAPHeader = "\n wsmlVariant _\"http://www.wsmo.org/wsml/wsml-syntax/wsml-rule\" \n namespace { _\"http://www.example.org/ontologies/example#\", \n dc _\"http://purl.org/dc/elements/1.1#\", \n wsml _\"http://www.wsmo.org/wsml/wsml-syntax#\", \n sm _\"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/StockMarket.wsml#\", \n smp _\"file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/StockMarketProcess.wsml#\"} \n\n ontology brokerService  \n importsOntology { sm#StockMarket, smp#StockMarketProcess} \n";
	static final String pathOntology = "file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/";
	static final String stockMarketNS = pathOntology + "StockMarket.wsml#";
	static final String stockMarketProcessNS = pathOntology + "StockMarketProcess.wsml#";
	static final String stockMarketInstancesNS = pathOntology + "StockMarketInstances.wsml#";
	static final String adapterOntologyNS = pathOntology +"AdapterOntology.wsml#";
	
	static final String stockMarketOntoIRI = stockMarketNS + "StockMarket";
	static final String stockMarketProcessOntoIRI = stockMarketProcessNS + "StockMarketProcess";
	static final String stockMarketInstancesOntoIRI = stockMarketInstancesNS + "StockMarketInstances";
	static final String adapterOntoIRI = adapterOntologyNS +"AdapterOntology";
	
	
	static final String conceptQuote = stockMarketNS+ "Quote";
	static final String conceptStock = stockMarketNS+ "Stock";
	static final String conceptNews =  stockMarketNS+ "News"; 
	static final String conceptRecommendationType =  stockMarketNS+ "RecommendationType";
	static final String conceptRecommendation =  stockMarketNS+ "Recommendation";
	static final String conceptMarket =  stockMarketNS + "Market";
	static final String conceptMarketQuote =  stockMarketNS+ "MarketQuote";
	static final String conceptCurrency =  stockMarketNS + "Currency";
	static final String conceptCurrencyValue =  stockMarketNS+ "CurrencyValue";
	static final String conceptQuoteValue =  stockMarketNS + "QuoteValue";
	static final String conceptTop =  stockMarketNS + "Top";
	static final String conceptStockMarketUser =  stockMarketNS+ "StockMarketUser";

	static final String conceptGetIndex = stockMarketProcessNS + "GetIndex";
	static final String conceptPerformBuySell = stockMarketProcessNS + "performBuySell";
	static final String conceptGetNews = stockMarketProcessNS + "GetNews";
	static final String conceptSendAlert = stockMarketProcessNS + "sendAlert";


	
	static final String conceptGetQuote = stockMarketProcessNS+"GetQuote";
	static final String conceptProcessStock= stockMarketProcessNS+"stock";
	static final String conceptProcessActionType= stockMarketProcessNS+"actionType";
	static final String conceptProcessUser= stockMarketProcessNS+"user";
	static final String  conceptProcessAmount= stockMarketProcessNS+"amount";
	static final String conceptGetRecommendation = stockMarketProcessNS+"GetRecommendation";
	
	
	
	
	static final String Bankinter = "Bankinter";
	static final String StrikeIron = "StrikeIron";
	static final String XIgnite = "Xignite";
	
	static final String nasdaq = stockMarketInstancesNS + "nasdaq";
	
	
	
	
	
	
	
	
	static final String attributeStockISIN = stockMarketNS+ "stockISIN";
	static final String attributeStockTicker = stockMarketNS+  "ticker";
	static final String attributeStockName = stockMarketNS+  "name";
	static final String attributeMarketName = stockMarketNS+  "marketName";
	static final String attributeMarketCode = stockMarketNS+  "marketCode";
	static final String attributeIdPortfolio = stockMarketNS+  "idPortfolio";
	static final String attributeMobileNumber = stockMarketNS+  "mobileNumber";
	
	
	static final String attributeMarketISIN = stockMarketNS+  "marketISIN";
	static final String attributeActionType = stockMarketInstancesNS + "hasActionType";//TODO: don't know why it is smi, should be smp
	static final String attributeUser = stockMarketNS+  "userID";
	
	static final String attributeInputMessage = adapterOntologyNS + "inputMessage";
	static final String attributeInstanceMappings = adapterOntologyNS + "instanceMappings";
	static final String attributeValueMappings = adapterOntologyNS + "valueMappings";
	static final String attributeConceptOutput = adapterOntologyNS + "conceptOutput";	
	
}
