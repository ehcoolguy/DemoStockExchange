package com.solace.apac.demo.stockmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

@SpringBootApplication
@Controller
public class SolExPortal {
    private static ArrayList<String[]> oTodaySymbols = new ArrayList<String[]>();

    @RequestMapping("/")
    public String index(HttpSession session, Model model) {
        model.addAttribute("Today", (new java.util.Date()));
        session.setAttribute("oTodaySymbols", oTodaySymbols);
        // set the serviceLevelAlias as "GUEST" for the first time.
        if (session.getAttribute("serviceLevelAlias") == null)
            session.setAttribute("serviceLevelAlias", "GUEST");

        return "index";
    }

    @RequestMapping("/controlPanel")
    public String controlPanel(HttpSession session, Model model) {
        model.addAttribute("Today", (new java.util.Date()));

        return "controlPanel";
    }

    @RequestMapping(value = "/{serviceLevelAlias}", method = RequestMethod.GET)
    public String index(@PathVariable("serviceLevelAlias") String serviceLevelAlias, HttpSession session, HttpServletRequest request) {
        //TODO: 想要顯示來自用戶端的IP位置
        System.out.printf("The guest (IP: %s) logged with %s level\n", request.getRemoteAddr(), serviceLevelAlias);
        String solaceClientUserName = "user03";
        String solaceVpnName = "test01";
        byte serviceLevel = 0b00000000;

        switch (serviceLevelAlias) {
            case "HARU":
                solaceClientUserName = solaceVpnName = "haru";
                serviceLevel = 0b00001000;
                break;
            case "NATSU":
                solaceClientUserName = solaceVpnName ="natsu";
                serviceLevel = 0b00000100;
                break;
            case "AKI":
                solaceClientUserName = solaceVpnName = "aki";
                serviceLevel = 0b00000010;
                break;
            case "FUYU":
                solaceClientUserName = solaceVpnName = "fuyu";
                serviceLevel = 0b00000001;
                break;
            default:
                serviceLevel = 0b00000000;
        }
        session.setAttribute("serviceLevel", serviceLevel);
        session.setAttribute("serviceLevelAlias", serviceLevelAlias);
        session.setAttribute("solaceClientUserName", solaceClientUserName);
        session.setAttribute("solaceVpnName", solaceVpnName);

        return "redirect:/";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@ModelAttribute Customer myCustomer,
                        BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            System.out.printf("BIND ERROR: $s\n", result.toString());
            return "index";
        }
        System.out.printf("[LOGIN] My Customer: %s, displayName: %s, serviceLevel: %d\n ", myCustomer.getClientId(), myCustomer.getDisplayName(), myCustomer.getServiceLevel());
        session.setAttribute("myCustomer", myCustomer);

        return "redirect:/";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session) {
        Customer myCustomer = (Customer) session.getAttribute("myCustomer");
        String serviceLevelAlias = (String) session.getAttribute("serviceLevelAlias");

        if (myCustomer != null && serviceLevelAlias != null) {
            System.out.printf("[LOGOUT] My Customer: %s, displayName: %s, serviceLevel: %d\n ", myCustomer.getClientId(), myCustomer.getDisplayName(), myCustomer.getServiceLevel());
            session.removeAttribute("myCustomer");
            session.removeAttribute("serviceLevelAlias");
            return "redirect:/" + serviceLevelAlias;
        }

        return "redirect:/";
    }

    private static void loadSymbols(String symbolListFileName) {
        Path path = Paths.get(symbolListFileName);
        System.out.printf("toString: %s%n", path.toAbsolutePath());

        BufferedReader br = null;
        FileReader fr = null;

        try {
            //fr = new FileReader(path.toAbsolutePath().toString());
            //br = new BufferedReader(fr);
            File f = new File(path.toAbsolutePath().toString());
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
            br = new BufferedReader(isr);

            String sCurrLine = null;
            String[] sCurrSymbol;

            while ((sCurrLine = br.readLine()) != null) {
                sCurrSymbol = sCurrLine.split(",");
                oTodaySymbols.add(sCurrSymbol);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        for (int i=0;i<oTodaySymbols.size();i++) {
            System.out.printf("Current Stock [%d]: %s, Name: %s, open at: %.2f\n", (i+1), oTodaySymbols.get(i)[0], oTodaySymbols.get(i)[1], Double.parseDouble(oTodaySymbols.get(i)[2]));
        }
    }

    public static void main(String[] args) {
        loadSymbols("myStockList.txt");
        System.out.printf("Starting the SolEx Portal...\n\n\n");
        SpringApplication.run(SolExPortal.class, args);
    }
}
