package com.solace.apac.demo.stockexchange;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by hhjau on 2018/10/09.
 */
@SpringBootApplication
@RestController
public class PortalDataProvider {
    private static ArrayList<String[]> oTodaySymbols = new ArrayList<String[]>();
    private static ArrayList<StockSymbol> availableSymbols = new ArrayList<>();

    @RequestMapping(value = "/api/general/listStocks", method = RequestMethod.GET)
    public ArrayList<StockSymbol> getGeneralListStocks (HttpSession session, HttpServletRequest request) {
        System.out.printf("The guest (IP: %s, Agent: %s) logged to get stock list.\n", request.getRemoteAddr(), request.getHeader("User-Agent"));
        return availableSymbols;
        //return oTodaySymbols;
    }

    private static void loadSymbols(String symbolListFileName) {
        Path path = Paths.get(symbolListFileName);
        System.out.printf("Loaded from: %s%n", path.toAbsolutePath());

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
            StockSymbol currSymbol = null;


            while ((sCurrLine = br.readLine()) != null) {
                sCurrSymbol = sCurrLine.split(",");
                oTodaySymbols.add(sCurrSymbol);

                // Load each symbol as an object
                currSymbol = new StockSymbol(sCurrSymbol[0], sCurrSymbol[1]);
                currSymbol.setOpenPrices(Double.parseDouble(sCurrSymbol[2]));
                availableSymbols.add(currSymbol);
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
        SpringApplication.run(PortalDataProvider.class, args);
    }
}
