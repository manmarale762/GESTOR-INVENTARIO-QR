package com.inventario.control.infraestructure.odoo;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

@Service
public class OdooService {

    @Value("${odoo.url}")
    private String odooUrl;
    @Value("${odoo.db}")
    private String odooDb;

    public boolean authenticate(String email, String password) {
        try {
            XmlRpcClientConfigImpl commonConfig = new XmlRpcClientConfigImpl();
            commonConfig.setServerURL(new URL(String.format("%s/xmlrpc/2/common", odooUrl)));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(commonConfig);

            // Intentamos loguear en Odoo. Si devuelve un UID (entero), es correcto.
            Object uid = client.execute("authenticate", Arrays.asList(
                    odooDb, email, password, Map.of()
            ));

            return (uid instanceof Integer);
        } catch (Exception e) {
            System.err.println("Error conectando con Odoo: " + e.getMessage());
            return false;
        }
    }
}