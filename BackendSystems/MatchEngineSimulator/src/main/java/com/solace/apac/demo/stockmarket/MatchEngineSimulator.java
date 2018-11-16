package com.solace.apac.demo.stockmarket;

public class MatchEngineSimulator {
    public static void main(String[] args) {
        //MarketDataStreamer mds = new MarketDataStreamer();
        MarketDataStreamer mds = new MarketDataStreamer("config.MES.properties");
        mds.printAllConfigProperties();
        mds.loadSymbols("myStockList.txt");
        mds.startTrade();
    }
}
