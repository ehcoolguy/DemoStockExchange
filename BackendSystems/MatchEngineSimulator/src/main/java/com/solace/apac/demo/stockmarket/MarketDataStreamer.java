package com.solace.apac.demo.stockmarket;

import com.solacesystems.jcsmp.*;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Properties;

public class MarketDataStreamer {
    // Exchange business logic
    ArrayList<StockSymbol> oTodaySymbols;
    final static String MARKET_PREFIX = "TW";
    final static String MARKET_ID = "TWSE";
    final static String MARKET_STATUS_TOPIC = "STATUS";
    final static String MARKET_INFO_TOPIC = "INFO";
    final static String MARKET_LIVE_DATA_TOPIC = "LIVE";
    final static String MARKET_VA_DATA_TOPIC = "VA";
    final static String MARKET_REPLAY_DATA_TOPIC = "REPLAY";
    private String marketPrefixId;
    private String marketStatusTopic;
    private String marketInfoTopic;
    private String marketLiveDataTopic;
    private String marketVaDataTopic;
    private String marketReplayTopic;
    // Match engine configuration.
    final static long TRADING_CAPABILITY = 300000;  //要模擬的委託數量
    final static int MAX_VOL_PER_ORDER = 9;  //單一委託最大的可成交量
    final static int MIN_MATCHING_DELAY = 1000;  //最小撮合延遲 (us)
    final static int MAX_MATCHING_DELAY = 3000;  //最大撮合延遲 (us)
    final static int MATCH_RATE_SAMPLING_INTERVAL = 3998;  //多少筆交易後計算一次速度
    private long tradingCapability;
    private int maxVolPerOrder;
    private int minMatchingDelay;
    private int maxMatchingDelay;
    private int matchRateSamplingInterval;
    long currOrderId = 0;  //目前處理到的委託序號
    long iOkOrder = 0;  //成功的委託數
    long iNgOrder = 0;  //失敗的委託數
    long iMarketDataMsgNum = 0;  //已發送的MarketData數量
    long iLastRateCheckTimeStamp = 0;  //前一次記算速度時的時間戳，單位是毫秒
    double dMatchRate = 0.0;  //交易引擎的速度
    // Market data streamer configuration.
    // Solace related objects.
    private JCSMPProperties properties;
    private JCSMPSession session;
    private XMLMessageProducer prod;
    private Topic topic;
    private TextMessage txtMsg;
    private BytesMessage byteMsg;
    final String mType = "D";
    final static String SOLACE_HOST = "jj-solace1.mooo.com";
    final static String SOLACE_VPN = "test01";
    final static String SOLACE_CLIENT_USERNAME = "user01";
    final static String SOLACE_CLIENT_PASSWORD = "password";
    private String solaceHost;
    private String solaceVpn;
    private String solaceClientUsername;
    private String solaceClientPassword;


    SimpleDateFormat tradeDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    public MarketDataStreamer() {
        System.out.println("MarketDataStreamer initializing...");
        this.marketPrefixId = MARKET_PREFIX + "/" + MARKET_ID + "/";
        this.marketStatusTopic = this.marketPrefixId + MARKET_STATUS_TOPIC;
        this.marketInfoTopic = this.marketPrefixId + MARKET_INFO_TOPIC;
        this.marketLiveDataTopic = this.marketPrefixId + MARKET_LIVE_DATA_TOPIC;
        this.marketVaDataTopic = this.marketPrefixId + MARKET_VA_DATA_TOPIC;
        this.marketReplayTopic = this.marketPrefixId + MARKET_REPLAY_DATA_TOPIC;
        this.tradingCapability = TRADING_CAPABILITY;
        this.maxVolPerOrder = MAX_VOL_PER_ORDER;
        this.minMatchingDelay = MIN_MATCHING_DELAY;
        this.maxMatchingDelay = MAX_MATCHING_DELAY;
        this.matchRateSamplingInterval = MATCH_RATE_SAMPLING_INTERVAL;
        this.solaceHost = SOLACE_HOST;
        this.solaceVpn = SOLACE_VPN;
        this.solaceClientUsername = SOLACE_CLIENT_USERNAME;
        this.solaceClientPassword = SOLACE_CLIENT_PASSWORD;
    }

    // Initialize the MarketDataStreamer w/ assigned configFileName
    public MarketDataStreamer(String configFileName) {
        System.out.printf("MarketDataStreamer will be initialized w/ %s\n", configFileName);
        Path path = Paths.get(configFileName);
        System.out.printf("\nConfig file path: %s%n", path.toAbsolutePath());
        Properties configProps = new Properties();
        try {
            configProps.load(new FileInputStream(path.toAbsolutePath().toString()));
            this.marketPrefixId = configProps.getProperty("MARKET_PREFIX") + "/" + configProps.getProperty("MARKET_ID") + "/";
            this.marketStatusTopic = this.marketPrefixId + configProps.getProperty("MARKET_STATUS_TOPIC");
            this.marketInfoTopic = this.marketPrefixId + configProps.getProperty("MARKET_INFO_TOPIC");
            this.marketLiveDataTopic = this.marketPrefixId + configProps.getProperty("MARKET_LIVE_DATA_TOPIC");
            this.marketVaDataTopic = this.marketPrefixId + configProps.getProperty("MARKET_VA_DATA_TOPIC");
            this.marketReplayTopic = this.marketPrefixId + configProps.getProperty("MARKET_REPLAY_DATA_TOPIC");
            this.tradingCapability = Integer.parseInt(configProps.getProperty("TRADING_CAPABILITY"));
            this.maxVolPerOrder = Integer.parseInt(configProps.getProperty("MAX_VOL_PER_ORDER"));
            this.minMatchingDelay = Integer.parseInt(configProps.getProperty("MIN_MATCHING_DELAY"));
            this.maxMatchingDelay = Integer.parseInt(configProps.getProperty("MAX_MATCHING_DELAY"));
            this.matchRateSamplingInterval = Integer.parseInt(configProps.getProperty("MATCH_RATE_SAMPLING_INTERVAL"));
            this.solaceHost = configProps.getProperty("SOLACE_HOST");
            this.solaceVpn = configProps.getProperty("SOLACE_VPN");
            this.solaceClientUsername = configProps.getProperty("SOLACE_CLIENT_USERNAME");
            this.solaceClientPassword = configProps.getProperty("SOLACE_CLIENT_PASSWORD");
        } catch (IOException ioe) {
            System.out.printf("Some errors occured while reading config properties...\n");
            ioe.printStackTrace();
        }
    }

    public void printAllConfigProperties() {
        System.out.printf("\n=== Properties Used ===\n");
        System.out.printf("Status Topic: \t %s \n", this.marketStatusTopic);
        System.out.printf("Info Topic: \t %s \n", this.marketInfoTopic);
        System.out.printf("Live Data Topic: \t %s \n", this.marketLiveDataTopic);
        System.out.printf("VA Data Topic: \t %s \n", this.marketVaDataTopic);
        System.out.printf("Replay Data Topic: \t %s \n", this.marketReplayTopic);
        System.out.printf("Max Volume/Order: \t %d \n", this.maxVolPerOrder);
        System.out.printf("Min Matching Delay: \t %d \n", this.minMatchingDelay);
        System.out.printf("Max Matching Delay: \t %d \n", this.maxMatchingDelay);
        System.out.printf("Rate Sampling: \t %d \n", this.matchRateSamplingInterval);
        System.out.printf("\n=== SOLACE Properties ===\n");
        System.out.printf("Solace Host: \t %s \n", this.solaceHost);
        System.out.printf("Solace User: \t %s \n", this.solaceClientUsername + "@" + this.solaceVpn);
    }

    private void init() throws JCSMPException {
        // Create a JCSMP Session

        this.properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, this.solaceHost);
        properties.setProperty(JCSMPProperties.VPN_NAME, this.solaceVpn);
        properties.setProperty(JCSMPProperties.USERNAME, this.solaceClientUsername);
        properties.setProperty(JCSMPProperties.PASSWORD, this.solaceClientPassword);

        // Connect to Solace
        this.session =  JCSMPFactory.onlyInstance().createSession(properties);

        // Activate a producer.
        // For direct messaging, these methods will never be invoked.
        this.prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);
            }
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n",
                        messageID,timestamp,e);
            }
        });

        this.txtMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        this.byteMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
    }

    private void closeMarket() {
        try {
            // 通知市場收市
            this.topic = JCSMPFactory.onlyInstance().createTopic(this.marketStatusTopic);
            txtMsg.clearContent();
            txtMsg.setText("EXM999999|END_TRADE|Now we don't accept any orders.|" + System.currentTimeMillis());
            this.prod.send(this.txtMsg, this.topic);
            // 關閉所有連線
            this.prod.close();
            this.session.closeSession();
            System.out.println("END TRADE @ " + tradeDateFormat.format(System.currentTimeMillis()));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void loadSymbols(String symbolListFileName) {
        Path path = Paths.get(symbolListFileName);
        System.out.printf("toString: %s%n", path.toAbsolutePath());

        BufferedReader br = null;
        FileReader fr = null;

        try {
            fr = new FileReader(path.toAbsolutePath().toString());
            br = new BufferedReader(fr);
            String sCurrLine = null;
            String[] sCurrSymbol;
            oTodaySymbols = new ArrayList<StockSymbol>();

            while ((sCurrLine = br.readLine()) != null) {
                sCurrSymbol = sCurrLine.split(",");
                StockSymbol ss = new StockSymbol(sCurrSymbol[0], sCurrSymbol[1]);
                ss.setOpenPrices(Double.parseDouble(sCurrSymbol[2]));
                oTodaySymbols.add(ss);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        for (int i=0;i<oTodaySymbols.size();i++) {
            System.out.printf("Current Stock [%d]: %s, Name: %s, open at: %.2f\n", (i+1), oTodaySymbols.get(i).getId(), oTodaySymbols.get(i).getDisplayName(), oTodaySymbols.get(i).getpOpen());
        }

        //  連接 Solace
        try {
            this.init();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void startTrade() {
        // 發送通知訊息，告知市場開市
        try {
            this.iLastRateCheckTimeStamp = System.currentTimeMillis();
            this.topic = JCSMPFactory.onlyInstance().createTopic(this.marketStatusTopic);
            txtMsg.clearContent();
            txtMsg.setText("EXM000001|START_TRADE|Now your orders will be processed.|" + System.currentTimeMillis());
            this.prod.send(this.txtMsg, this.topic);
            System.out.println("START TRADE @ " + tradeDateFormat.format(System.currentTimeMillis()));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        int iTradingSymbol = 0;  //目前正在撮合中的股票
        int iMatchingDelay = 0;  //撮合股票所需要的時間，亂數產生，microseconds.
        int iTradingVolume = 0;  //這一檔成交的張數
        boolean isBuyPrice = false;  //是否為外盤成交
        String tradeTimeStamp = null;  //交易的時間戳

        int iAvailableSymbolsToday = this.oTodaySymbols.size();  //今天可以交易的股票檔數，不寫成final是因為盤中有可能因為意外導致某檔個股停止交易

        for (long tradeCount = 0; tradeCount < this.tradingCapability; tradeCount++) {
            iTradingSymbol = (int)((Math.random()*iAvailableSymbolsToday));
            iTradingVolume = (int)((Math.random()*this.maxVolPerOrder)+1);
            isBuyPrice = (new Random()).nextBoolean();
            StockSymbol currSS = this.oTodaySymbols.get(iTradingSymbol);
            currSS.setvOrderNum(currSS.getvOrderNum()+1);

            //執行交易
            currOrderId++;
            if (currSS.procTrade(isBuyPrice, iTradingVolume)) {
                iOkOrder++;
                //發送行情
                publishMarketData(currSS);
            }
            else {
                iNgOrder++;
            }

            tradeTimeStamp = tradeDateFormat.format(System.currentTimeMillis());
            //模擬單筆交易所需時間，再執行下一筆交易。
            //其實依據我本來定義的結果，這樣的作法是錯的。例如我定義MAX=50000,MIN=20000，則可能出現的撮合延遲為20000~(50000+20000)
            iMatchingDelay = (int)((Math.random()*this.maxMatchingDelay)+this.minMatchingDelay);
            //交易成功
            //每幾筆交易計算交易速度，這裡用的方法很差，但在目前的設計下，不至於影響交易引擎的速度
            if (currOrderId % this.matchRateSamplingInterval == 0) {
                //換算成從今天開盤到現在每秒幾筆
                dMatchRate = ((double)currOrderId / (double)(System.currentTimeMillis() - this.iLastRateCheckTimeStamp))*1000.0;
                // 發送通知訊息，告知市場開市所有人目前交易引擎的速度
                try {
                    DecimalFormat df = new DecimalFormat("0.000");
                    this.topic = JCSMPFactory.onlyInstance().createTopic(this.marketStatusTopic);
                    this.txtMsg.clearContent();
                    long systemMillis = System.currentTimeMillis();
                    this.txtMsg.setText("EXM000101|ME_PERF_INFO|" + df.format(dMatchRate) + "|" + systemMillis);
                    this.prod.send(this.txtMsg, this.topic);
                    this.txtMsg.setText("EXM000110|ME_ORDER_TOTAL|" + currOrderId + "|" + systemMillis);
                    this.prod.send(this.txtMsg, this.topic);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.out.printf("Match Engine Performance: %.3f orders/sec, %d/%d orders finsihed. @ %s\n", dMatchRate, tradeCount, this.tradingCapability, tradeDateFormat.format(System.currentTimeMillis()));
            }
            //System.out.printf("Order [%d] has been processed @ %s (elapsed: %d):\n", (tradeCount+1), tradeTimeStamp, iMatchingDelay);
            //System.out.printf("Symbol: %s, %s. Price: %.2f, Vol: %d, Diff: %.2f, Last Updated: %s, isBuyPrice: %s\n", currSS.getId(), currSS.getDisplayName(), currSS.getpClose(), currSS.getvTotal(), currSS.getpDiff(), tradeDateFormat.format(currSS.getLastUpdated()), isBuyPrice);
            busyWaitMicros(iMatchingDelay);
        }
        //  收盤後的清理
        closeMarket();
    }

    // Publishing market data
    private void publishMarketData (StockSymbol currSS) {
        MarketData md = new MarketData(this.currOrderId, currSS);

        // For UTF-8 strings, please ensure you get the bytes in UTF-8.
        // This try...catch... statement is very weak, please complete it if you want to use this code.
        try {
            byteMsg.clearContent();

            topic = JCSMPFactory.onlyInstance().createTopic(this.marketLiveDataTopic + "/" + currSS.getId());
            String currSymbolMarketData = new String(md.toString().getBytes("UTF-8"), "UTF-8");
            byteMsg.setElidingEligible(true);
            byteMsg.setData(currSymbolMarketData.getBytes());
            // Set the "ReplyTo" field with the publisher's "P2PINBOX"
            // System.out.println(session.getProperty(JCSMPProperties.P2PINBOX_IN_USE));
            // byteMsg.setReplyTo(JCSMPFactory.onlyInstance().createTopic(session.getProperty(JCSMPProperties.P2PINBOX_IN_USE).toString()));

            this.prod.send(byteMsg, topic);
            this.iMarketDataMsgNum++;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    // Try to accurate the waiting time to microseconds level.
    // REFER TO: http://www.rationaljava.com/2015/10/measuring-microsecond-in-java.html
    private void busyWaitMicros(long sleepInMicros) {
        long waitUntil = System.nanoTime() + (sleepInMicros * 1000);

        while (waitUntil > System.nanoTime()) {
            ;
        }
    }
}
