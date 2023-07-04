package com.stubhub.test.inventory;

import com.stubhub.global.domain.utils.base.database.DBConnector;
import com.stubhub.global.domain.utils.base.database.DBHelper;
import com.stubhub.global.domain.utils.base.database.MarkedOracleDBOperator;
import com.stubhub.global.domain.utils.base.exception.EnvironmentException;
import com.stubhub.global.domain.utils.base.registry.GlobalRegistry;

public class StubDbConnector extends DBConnector {

    private static GlobalRegistry globalRegistry;

    static {
        globalRegistry = GlobalRegistry.byCommonConfig("db");
    }


    public StubDbConnector() {
        super(new MarkedOracleDBOperator());
    }


	public String getConnectionString() throws Exception {
        String conString = "jdbc:oracle:thin:{%username%}/{%password%}@(DESCRIPTION=(SDU=32676)(ADDRESS_LIST = (LOAD_BALANCE=ON) (FAILOVER=ON) (ADDRESS=(PROTOCOL=TCP)(HOST={%host%})(PORT={%port%})(SEND_BUF_SIZE=125000)(RECV_BUF_SIZE=125000)) (ADDRESS=(PROTOCOL=TCP)(HOST={%host%})(PORT={%port%})(SEND_BUF_SIZE=125000)(RECV_BUF_SIZE=125000)) (ADDRESS=(PROTOCOL=TCP)(HOST={%host%})(PORT={%port%})(SEND_BUF_SIZE=125000)(RECV_BUF_SIZE=125000))) (CONNECT_DATA =(SERVER = DEDICATED) (SERVICE_NAME = {%service%})(FAILOVER_MODE = (TYPE=SELECT)(METHOD=BASIC)(RETRIES=2)(DELAY=5))))";

        /* fetch connection string */
        conString = conString.replace("{%username%}",
                "STUB_TNS_APP").replace("{%password%}", "dstub_tns_app");
        String configuration = DBHelper.getDBConfig();
        if(configuration.isEmpty()) {
            throw new EnvironmentException("Can't find db configuration from 'http://slcd000dvo015.stubcorp.com/env-db-map'");
        }
        String service = configuration.split(":")[1];
        String host = configuration.split(":")[2];
        String port = configuration.split(":")[3];
        conString = conString.replace("{%service%}", service).replace("{%host%}", host).replace("{%port%}", port);
        return conString.trim();
    }

}
