package com.solace.apac.demo.stockmarket;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

@RestController
public class SempController {
    private HashMap<String, Boolean> vpnStatus;
    final private String SEMP_VERSION = "7_1_1";
    final private String solaceHost = "10.10.10.52";
    final private String solaceSempPort = "8080";
    final private String solaceSempProtocol = "http";
    final private String solaceUserName = "admin";
    final private String solacePassword = "admin";
    final private String SEMP_BEGIN_LINE = "<rpc semp-version=\"" + SEMP_VERSION + "\">\n";
    final private String SEMP_END_LINE = "</rpc>\n";

    public SempController() {
        this.vpnStatus = new HashMap<>();
        this.vpnStatus.put("haru", true);
        this.vpnStatus.put("natsu", true);
        this.vpnStatus.put("aki", true);
        this.vpnStatus.put("fuyu", true);
    }

    private String changeStatus(String vpnName) {
        String sempCommand = "";

        if (this.vpnStatus.containsKey(vpnName)) {
            if (this.vpnStatus.get(vpnName)) {
                sempCommand = "<shutdown/>";
                this.vpnStatus.put(vpnName, false);
            } else {
                sempCommand = "<no><shutdown/></no>";
                this.vpnStatus.put(vpnName, true);
            }
        }

        return sempCommand;
    }

    private String getVpnBrName(String vpnName) {
        String vpnBrName = "";

        if (this.vpnStatus.containsKey(vpnName)) {
            vpnBrName = "L_" + vpnName + "-L_test01";
        }

        return vpnBrName;

    }

    @RequestMapping(value = "/VpnBrOp/{vpnName}")
    public SempResult VpnOpen(@PathVariable String vpnName, HttpSession session, HttpServletRequest reuqest) {
        final String vpnOperation = this.changeStatus(vpnName);
        final String vpnBrName = this.getVpnBrName(vpnName);

        final String sempRequest = SEMP_BEGIN_LINE +
                "<bridge>" +
                "<bridge-name>" + vpnBrName + "</bridge-name>" +
                "<vpn-name>" + vpnName + "</vpn-name>" +
                "<primary/>" +
                vpnOperation +
                "</bridge>" +
                SEMP_END_LINE;

        System.out.println("SEMP REQ: " + sempRequest);

        return this.sendSempRequest(sempRequest);
    }

    @RequestMapping(value = "/ShiftSpeedOp/{vpnName}")
    public SempResult VpnOpen(@PathVariable String vpnName, @RequestParam("speed") String speed, HttpSession session, HttpServletRequest reuqest) {
        // TODO: Should validate "speed" as integer.
        final String cpName = "cp-" + vpnName;

        final String sempRequest = SEMP_BEGIN_LINE +
                "<client-profile>" +
                "<name>" + cpName + "</name>" +
                "<vpn-name>" + vpnName + "</vpn-name>" +
                "<eliding>" + "<delay>" +
                "<milliseconds>" + speed + "</milliseconds>" +
                "</delay>" + "</eliding>" +
                "</client-profile>" +
                SEMP_END_LINE;

        System.out.println("SEMP REQ: " + sempRequest);

        return this.sendSempRequest(sempRequest);
    }

    // This function will run sempCommand and return the result.
    private SempResult sendSempRequest(String sempRequest) {
        String auth = solaceUserName + ":" + solacePassword;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        BufferedReader httpResponseReader = null;
        String currLine = null;
        StringBuffer sb = new StringBuffer();
        SempResult sr = null;

        try {
            URL solaceSempUrl = new URL(this.solaceSempProtocol + "://" + this.solaceHost + ":" + this.solaceSempPort + "/SEMP");
            // Connect to the web server endpoint
            HttpURLConnection urlConnection = (HttpURLConnection) solaceSempUrl.openConnection();

            // Set HTTP method as POST
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");

            // Include the HTTP Basic Authentication payload
            urlConnection.addRequestProperty("Authorization", authHeader);

// Writing the post data to the HTTP request body
            BufferedWriter httpRequestBodyWriter =
                    new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            httpRequestBodyWriter.write(sempRequest);
            httpRequestBodyWriter.close();

            // Reading from the HTTP response body
            Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
            while (httpResponseScanner.hasNextLine()) {
                currLine = httpResponseScanner.nextLine();
                //System.out.println(currLine);
                sb.append(currLine);
            }
            httpResponseScanner.close();
            System.out.println(sb.toString());
            sr = new SempResult();
            sr.setResultMsg(sb.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
        }

        return sr;
    }
}
