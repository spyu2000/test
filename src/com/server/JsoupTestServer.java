package com.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fleety.server.BasicServer;

public class JsoupTestServer extends BasicServer{

	private static JsoupTestServer instance=null;
	
	public static JsoupTestServer getSingleInstance(){
		if(instance==null){
			instance=new JsoupTestServer();
		}
		return instance;
	}
	@Override
	public boolean startServer() {
		// TODO Auto-generated method stub
		
		this.parseUrl();
		this.test();
		this.test1();
		this.isRunning=true;
		return true;
	}

	public void parseUrl() { 
        try { 
            Document doc = Jsoup.connect("http://www.baidu.com/").get(); 
            Elements hrefs = doc.select("a[href]"); 
            System.out.println(hrefs); 
            System.out.println("------------------"); 
            System.out.println(hrefs.select("[href^=http]")); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
    } 
	public void test1(){
		String url="http://finance.sina.com.cn/realstock/company/sh600664/nc.shtml";
		Document doc=null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements td=doc.select("td");
		System.out.println(doc); 
		
	}
	public void test(){
		String url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/600664.phtml?year=2014&jidu=4";
        System.out.println(url);
        Document document=null;
		try {
			document = Jsoup.connect(url).get();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Elements stockdatas = document.select("table#FundHoldSharesTable").select("tr");
        for(Element e : stockdatas){
            String time;
            double openPrice;
            double highPrice;
            double endPrice;
            double lowPrice;
            int dealCount;
            int dealAmount;
            Element tmp = e.select("td").select("a").first();
            if(tmp != null){
                List<String> infoList = new ArrayList<String>();
                Elements infos = e.select("td");
                for(Element info : infos){
                    String tmpMsg = info.text();
                    infoList.add(tmpMsg);
                }
                HisStockData hisData = new HisStockData();
                time = infoList.get(0);
                openPrice = Double.parseDouble(infoList.get(1));
                highPrice = Double.parseDouble(infoList.get(2));
                endPrice= Double.parseDouble(infoList.get(3));
                lowPrice= Double.parseDouble(infoList.get(4));
                dealCount= Integer.parseInt(infoList.get(5));
                dealAmount = Integer.parseInt(infoList.get(6));
                hisData.setTime(time);
                hisData.setOpenPrice(openPrice);
                hisData.setHighPrice(highPrice);
                hisData.setEndPrice(endPrice);
                hisData.setLowPrice(lowPrice);
                hisData.setDealAmount(dealAmount);
                hisData.setDealCount(dealCount);
                System.out.println(hisData.toString()); 
            }
        }
	}
}
