package com.solace.apac.demo.stockexchange;

/**
 * Created by hhjau on 2018/04/19.
 */
public class StockSymbol {
    private String displayName;
    private String id;
    private String description;
    private long lastUpdated;
    private double pOpen;
    private double pHigh;
    private double pLow;
    private double pClose;
    private double pDiff;  //價差
    private long vTotal;
    private long vCurrent;
    private long vOrderNum;  //委託數，在這裡暫時先當成序號使用
    private long vNextPriceVol;  //距離跳下一檔還需要多少的成交量
    private double pPriceUnit;  //價位變動的最小單位，實務上依據交易所的規則
    private double pLimitUp;  //漲停價格
    private double pLimitDown;  //跌停價格
    private boolean isLimitUp;  //是否為漲停板
    private boolean isLimitDown;  //是否為跌停板

    public StockSymbol(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.pPriceUnit = 0.05;
        this.vNextPriceVol = 100;
        this.lastUpdated = (new java.util.Date()).getTime();
        System.out.println("Generating a Stock Symbol..." + this.getId() + ", " + this.getDisplayName());
    }

    private StockSymbol() {
        System.out.println("Generating a Stock Symbol...");
    }

    public void setOpenPrices(double prevClosePrice) {
        this.pOpen = prevClosePrice;
        this.pClose = prevClosePrice;
        this.pHigh = prevClosePrice;
        this.pLow = prevClosePrice;
        //設置漲跌停價格
        this.pLimitUp = this.pOpen * 1.1;
        this.pLimitDown = this.pOpen * 0.9;
    }

    public boolean procTrade(boolean isBuyPrice, long tradeVol) {
        this.vCurrent = tradeVol;
        this.vTotal = this.vTotal + this.vCurrent;

        //如果這一檔交易使得股價有機會往上一層。
        //當然，實務上必須通過五檔最高買價與最低買價決定。
        if (this.vTotal > this.vNextPriceVol) {
            //如果是外盤價成交，則向上一個價位，反之，則減少一個價位
            //實務上的價位跳動必須依據證交所的規則。
            //如果目前已經是漲停板或跌停板，則價格不會再做增減
            if (isBuyPrice) {
                //如果在跌停板的情況下有外盤成交，代表跌停板打開。
                this.isLimitDown = (this.isLimitDown?false:true);
                if (!isLimitUp) {
                    this.pClose = this.pClose + this.pPriceUnit;
                    // 如果收盤價大於漲停價，這顯示為漲停板
                    this.isLimitUp = (this.pClose >= this.pLimitUp?true:false);
                }
            }
            else {
                //如果在漲停板的情況下有內盤成交，代表漲停板打開。
                this.isLimitUp = (this.isLimitUp?false:true);
                // 如果不是跌停板，才要做價格的減少
                if (!isLimitDown) {
                    this.pClose = this.pClose - this.pPriceUnit;
                    // 如果收盤價小於跌停價，這顯示為跌停板
                    this.isLimitDown = (this.pClose <= this.pLimitDown?true:false);
                }
            }
            // 把要往上提升價位的成交量拉大一個量級
            this.vNextPriceVol = this.vNextPriceVol + 777;
        }
        this.pDiff = this.pClose - this.pOpen;
        this.updateHLPrice();
        this.lastUpdated = (new java.util.Date()).getTime();
        return true;
    }

    //用目前的收盤價格來做高低價的更新
    private void updateHLPrice() {
        if (this.pClose > this.pHigh)
            this.pHigh = this.pClose;
        else if (this.pClose < this.pLow)
            this.pLow = this.pClose;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getpOpen() {
        return pOpen;
    }

    public void setpOpen(double pOpen) {
        this.pOpen = pOpen;
    }

    public double getpHigh() {
        return pHigh;
    }

    public void setpHigh(double pHigh) {
        this.pHigh = pHigh;
    }

    public double getpLow() {
        return pLow;
    }

    public void setpLow(double pLow) {
        this.pLow = pLow;
    }

    public double getpClose() {
        return pClose;
    }

    public void setpClose(double pClose) {
        this.pClose = pClose;
    }

    public long getvTotal() {
        return vTotal;
    }

    public void setvTotal(long vTotal) {
        this.vTotal = vTotal;
    }

    public long getvCurrent() {
        return vCurrent;
    }

    public void setvCurrent(long vCurrent) {
        this.vCurrent = vCurrent;
    }

    public long getvNextPriceVol() {
        return vNextPriceVol;
    }

    public void setvNextPriceVol(long vNextPriceVol) {
        this.vNextPriceVol = vNextPriceVol;
    }

    public double getpDiff() {
        return pDiff;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public long getvOrderNum() {
        return vOrderNum;
    }

    public void setvOrderNum(long vOrderNum) {
        this.vOrderNum = vOrderNum;
    }
}
