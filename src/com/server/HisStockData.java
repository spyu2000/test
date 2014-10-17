package com.server;

public class HisStockData {
	 private String time;
	    private double openPrice;
	    private double highPrice;
	    private double endPrice;
	    private double lowPrice;
	    private int dealCount;
	    private int dealAmount;
	    public String getTime() {
	        return time;
	    }
	    public void setTime(String time) {
	        this.time = time;
	    }
	    public double getOpenPrice() {
	        return openPrice;
	    }
	    public void setOpenPrice(double openPrice) {
	        this.openPrice = openPrice;
	    }
	    public double getHighPrice() {
	        return highPrice;
	    }
	    public void setHighPrice(double highPrice) {
	        this.highPrice = highPrice;
	    }
	    public double getEndPrice() {
	        return endPrice;
	    }
	    public void setEndPrice(double endPrice) {
	        this.endPrice = endPrice;
	    }
	    public double getLowPrice() {
	        return lowPrice;
	    }
	    public void setLowPrice(double lowPrice) {
	        this.lowPrice = lowPrice;
	    }
	    public int getDealCount() {
	        return dealCount;
	    }
	    public void setDealCount(int dealCount) {
	        this.dealCount = dealCount;
	    }
	    public int getDealAmount() {
	        return dealAmount;
	    }
	    public void setDealAmount(int dealAmount) {
	        this.dealAmount = dealAmount;
	    }
	    @Override
	    public String toString() {
	        return "HisStockData [time=" + time + ", openPrice=" + openPrice
	                + ", highPrice=" + highPrice + ", endPrice=" + endPrice
	                + ", lowPrice=" + lowPrice + ", dealCount=" + dealCount
	                + ", dealAmount=" + dealAmount + "]";
	    }
}
