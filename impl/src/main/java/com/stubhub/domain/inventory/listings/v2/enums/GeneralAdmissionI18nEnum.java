package com.stubhub.domain.inventory.listings.v2.enums;

/* This class provides localized General Admission for SRS check when creating listing to support multi-lang.
It should be deprecated in near future as we need to remove hardcoded code out of domain code.
 */
public enum GeneralAdmissionI18nEnum {
    GA_US("General Admission"),
    GA_DE("Freie Platzwahl"),
    GA_FR("Placement libre"),
    GA_MX("Entrada general"),
    GA_PT("Acesso Geral"),
    GA_IT("Posto unico"),
    GA_NL1("Vrij zit- of staanplaatsen"),
    GA_NL2("Algemene toegang"),
    GA_FI("Pääsylippu"),
    GA_GR("Γενική είσοδος"),
    GA_PL1("Bilet wstępu"),
    GA_PL2("Bilet wstępu (miejsca nienumerowane)"),
    GA_SE1("Onumrerad plats"),
    GA_SE2("Onumrerat"),
    GA_SE3("Onumrerade platser"),
    GA_DK("Unummererede pladser"),
    GA_CZ("Nečíslovaná vstupenka");

    private String ga;

    GeneralAdmissionI18nEnum(String ga){
        this.ga = ga;
    }

    public String getName() {
        return ga;
    }

    public static boolean isGA(String str){
       for(GeneralAdmissionI18nEnum ga: GeneralAdmissionI18nEnum.values()){
           if(ga.getName().equals(str)){
               return true;
           }
       }
        return false;
    }


}
