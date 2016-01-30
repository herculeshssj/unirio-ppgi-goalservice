package org.deri.wsmx.mediation.ooMediator.functoids;

public class DefaultServiceResolver implements ServiceResolver {

	private DefaultServiceResolver() {
		super();
	}

	public Object invokeService(String identifier, Object[] parameters) {
		
		if (identifier.equals("http://examples.org/services/stringManipulator/cancat2")){
			if (parameters.length==2){
				return new String(parameters[0].toString() + " " + parameters[1].toString());
			}
		}
        
        if (identifier.equals("http://examples.org/services/stringManipulator/space_split#first")){
            if (parameters.length==1){
                return new String(parameters[0].toString().split(" ")[0]);
            }
        }
        
		if (identifier.equals("http://example.org/services/stringManipulation/concat_n")){
			String result = "";
			if (parameters==null){
				return result;
			}
			for (int i=0; i<parameters.length; i++){
				if (result.equals("")){
					result = parameters[i].toString();
				}
				else{
					result = result + " " + parameters[i];
				}
			}
			
			return result;
		}         
        
        if (identifier.equals("http://examples.org/services/stringManipulator/space_split#second")){
            if (parameters.length==1){
                return new String(parameters[0].toString().split(" ")[1]);
            }
        }
        
        if (identifier.equals("http://seemp.org/BLL/lookupROCountryCode")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("fr")){
                    return new String("FR");
                }
                else if (parameters[0].toString().equals("es")){
                    return new String("ES");
                }
                else if (parameters[0].toString().equals("it")){
                    return new String("IT");
                }
                else if (parameters[0].toString().equals("ie")){
                    return new String("IE");
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        if (identifier.equals("http://seemp.org/BLL/lookupROCountryName")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("fr")){
                    return new String("FRANCE");
                }
                else if (parameters[0].toString().equals("es")){
                    return new String("SPAIN");
                }
                else if (parameters[0].toString().equals("it")){
                    return new String("ITALY");
                }
                else if (parameters[0].toString().equals("ie")){
                    return new String("IRELAND");
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        if (identifier.equals("http://seemp.org/BLL/lookupROLanguageCode")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("24")){
                    return new String("eng");
                }
                else if (parameters[0].toString().equals("55")){
                    return new String("ger");
                }
                else if (parameters[0].toString().equals("16")){
                    return new String("fre");
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        if (identifier.equals("http://seemp.org/BLL/lookupROLanguageName")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("24")){
                    return new String("English");
                }
                else if (parameters[0].toString().equals("55")){
                    return new String("German");
                }
                else if (parameters[0].toString().equals("16")){
                    return new String("French");
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        if (identifier.equals("http://seemp.org/BLL/lookupROLanguageLevelCode")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("1")){
                    return new String("A1");
                }
                else if (parameters[0].toString().equals("2")){
                    return new String("B1");
                }
                else if (parameters[0].toString().equals("3")){
                    return new String("C1");
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        
        if (identifier.equals("http://seemp.org/BLL/lookupLanguageCode")){
            if (parameters.length==1){
                if (parameters[0].toString().toLowerCase().equals("ger")){
                    return new String("55");
                }
                else if (parameters[0].toString().toLowerCase().equals("eng")){
                    return new String("24");
                }
                else if (parameters[0].toString().toLowerCase().equals("ita")){
                    return new String("26");
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        if (identifier.equals("http://seemp.org/BLL/lookupLanguageName")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("ger")){
                    return new String("TEDESCO 55");
                }
                else if (parameters[0].toString().equals("eng")){
                    return new String("INGLESE 24");
                }
                else if (parameters[0].toString().equals("ita")){
                    return new String("ITALIANO 26");
                }
                else{
                    return new String("Unknown reference ontology name : " + parameters[0]);
                }
            }
        }
        if (identifier.equals("http://seemp.org/BLL/lookupLanguageLevelCode")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("A1") || parameters[0].toString().equals("A2")){
                    return new Integer(1);
                }
                else if (parameters[0].toString().equals("B1") || parameters[0].toString().equals("B2")){
                    return new Integer(2);
                }
                else if (parameters[0].toString().equals("C1") || parameters[0].toString().equals("C2")){
                    return new Integer(3);
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        if (identifier.equals("http://seemp.org/BLL/lookupLanguageLevelName")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("A1") || parameters[0].toString().equals("A2")){
                    return new String("Elementare");
                }
                else if (parameters[0].toString().equals("B1") || parameters[0].toString().equals("B2")){
                    return new String("Buona");
                }
                else if (parameters[0].toString().equals("C1") || parameters[0].toString().equals("C2")){
                    return new String("Eccellente");
                }
                else{
                    return new String("Unknown reference ontology code : " + parameters[0]);
                }
            }
        }
        
        if (identifier.equals("http://seemp.org/EURES/lookupLanguageLevelDescription")){
            if (parameters.length==1){
                if (parameters[0].toString().equals("A1")){
                    return new String("notions");
                }
                else if (parameters[0].toString().equals("A2")){
                    return new String("notions");
                }
                else if (parameters[0].toString().equals("B1")){
                    return new String("good");
                }
                else if (parameters[0].toString().equals("B2")){
                    return new String("very good");
                }
                else if (parameters[0].toString().equals("C1")){
                    return new String("Excellent");
                }
                else if (parameters[0].toString().equals("C2")){
                    return new String("fluent");
                }
                else{
                    return new String("Unknown eures ontology code : " + parameters[0]);
                }
            }
        }
		return new String("Unknown Service (" + identifier + " with arity " + parameters.length + ")");
	}
	
	public static ServiceResolver getServiceResolver(){
		return new DefaultServiceResolver();
	}

}
