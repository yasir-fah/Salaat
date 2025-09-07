package com.finalproject.tuwaiqfinal.Config;

import com.adobe.pdfservices.operation.PDFServices;
import com.adobe.pdfservices.operation.auth.ServicePrincipalCredentials;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class AdobePdfServicesConfig {

    @Value("${adobe.pdfservices.credentialsPath}")
    private Resource credentialsJson;

    @Bean
    public PDFServices pdfServices() throws Exception {

        try (InputStream in = credentialsJson.getInputStream()) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            JSONObject oauth = obj.getJSONObject("project").getJSONObject("workspace").getJSONObject("details")
                    .getJSONArray("credentials").getJSONObject(0).getJSONObject("oauth_server_to_server");
            String clientId = oauth.getString("client_id");
            String clientSecret = oauth.getJSONArray("client_secrets").getString(0);


            var creds = new ServicePrincipalCredentials(clientId, clientSecret);
            return new PDFServices(creds);
        }
    }
}
