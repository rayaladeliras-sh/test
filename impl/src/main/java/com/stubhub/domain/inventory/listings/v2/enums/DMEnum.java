package com.stubhub.domain.inventory.listings.v2.enums;

public enum DMEnum {

        Electronic_Download("Electronic", "Electronic_Download", 1),
        Electronic_Instant_Download("Instant download", "Electronic_Instant_Download", 2),
        Pickup_Will_Call("Pickup", "Pickup_Will_Call", 10),
        Pickup_Will_Call_Kiosk("Pickup_Will_Call_Kiosk", "Pickup_Will_Call_Kiosk", 11),
        Pickup_LMS("Pickup_LMS", "Pickup_LMS", 12),
        Email("Email", "Email", 13),
        Courier("Courier", "Courier", 14),
        Pickup_EVENT_DAY("Pickup", "Pickup_EVENT_DAY", 17),
        LargeSellers_Off_Site_Pickup("LargeSellers_Off_Site_Pickup", "LargeSellers_Off_Site_Pickup", 18),
        Hospitality("Hospitality", "Hospitality", 19),
        UPS_Worldwide_Saver_From_US("UPS_Worldwide_Saver_From_US", "UPS_Worldwide_US", 20),
        UPS_Worldwide_Saver_From_CA("UPS_Worldwide_Saver_From_CA", "UPS_Worldwide_CA", 21),
        UPS_Worldwide_Saver("UPS", "UPS_Worldwide_Saver", 22),
        UPS_Next_Business_Day_PM_Intra_USA("UPS", "UPS_Next_Day_PM_Intra_USA", 23),
        UPS_2_Business_Day_Intra_USA("UPS", "UPS_2_Day_Intra_USA", 24),
        UPS_Next_Business_Day_AM_Intra_USA("UPS_Next_Business_Day_AM_Intra_USA",
                "UPS_Next_Day_AM_Intra_USA", 25),
        UPS_Worldwide_Saver_CA_UK("UPS", "UPS_Worldwide_Saver_CA_UK", 26),
        UPS_Express_Saver_Intra_CA("UPS", "UPS_Express_Saver_Intra_CA", 27),
        UPS_Standard_Intra_CA("UPS", "UPS_Standard_Intra_CA", 28),
        UPS_Worldwide_Saver_CA_PR("UPS", "UPS_Worldwide_Saver_CA_PR", 29),
        UPS_Worldwide_Saver_USA_PR("UPS", "UPS_Worldwide_Saver_USA_PR", 35),
        UPS_Standard_Intra_UK("UPS", "UPS_Standard_Intra_UK", 36),
        Royal_Mail_MetaPack("Royal_Mail_MetaPack", "Royal_Mail_MetaPack", 37),
        Deutsche_Post_Intra_DE("Delivery service", "Deutsche_Post_Intra_DE", 38),
        Deutsche_Post_EU_Wide("Delivery service", "Deutsche_Post_EU_Wide", 39),
        FlashSeat_Instant("FlashSeat_Instant", "FlashSeat_Instant", 40),
        Mobile_ticket_Instant("Mobile ticket - Instant", "Mobile_ticket_Instant", 41),
        Mobile_ticket("Mobile ticket", "Mobile_ticket", 42),
        Mobile_Transfer("Mobile Transfer", "Mobile_Transfer", 43),
        Flash_Transfer("Flash Transfer", "Flash_Transfer", 44),
        FlashSeat("FlashSeat", "FlashSeat", 45),
        Season_Card_Courier("Season Card - Courier", "Season Card - Courier", 46),
        Mobile_or_Print("Mobile or Print", "Mobile_or_Print", 47),
        Mobile_or_Print_Instant("Mobile or print - Instant", "Mobile_or_Print(Instant)", 48),
        Local_Delivery("Local_Delivery", "Local_Delivery", 49),
        Courier_Worldwide("Courier Worldwide", "Courier Worldwide", 50),
        Mobile_Transfer_Seat_Geek("Mobile Transfer", "Mobile Transfer Seat Geek", 60),
        TM_Mobile_Secure_Entry_Download("Electronic (TM Mobile Secure Entry)", "TM Mobile Secure", 62),
        TM_Mobile_Secure_ENTRY_INSTANT_DOWNLOAD("PreDelivery NonSTH Instant download (TM Mobile Secure Entry)", "TM Mobile Secure Instant", 63);

        private String name;
        private String abbrev;
        private int id;

        DMEnum(String name, String abbrev, int id) {
            this.name = name;
            this.abbrev = abbrev;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getAbbrev() {
            return abbrev;
        }

        public int getId() {
            return id;
        }

        public static DMEnum valueOf(int id) {
            for (DMEnum d : DMEnum.values()) {
                if (d.getId() == id) {
                    return d;
                }
            }
            return null;
        }

    public static DMEnum getDMEnumByCode(int code) {
        DMEnum[] values = values();

        for (DMEnum type : values) {
            if(type.getId() == code) {
                return type;
            }
        }
        return null;
    }
    }