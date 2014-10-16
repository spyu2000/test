package com.server;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import server.var.VarManageServer;

import com.fleety.base.StrFilter;
import com.fleety.server.BasicServer;


public class HttpsClientTestServer extends BasicServer {
    private static final Logger logger = Logger.getLogger("HttpsClientTest");

    private static HttpsClientTestServer instance = null;

    private static HttpClient httpClient = null;


    public static HttpsClientTestServer getSingleInstance() {
        if (instance == null) {
            instance = new HttpsClientTestServer();
        }
        return instance;
    }


    @Override
    public boolean startServer() {

        int times =
                Integer.parseInt(VarManageServer.getSingleInstance().getVarStringValue("chongqing_times"));
        long sleepTime = 0;
        for (int i = 0; i < times; i++) {
            long start = System.currentTimeMillis();
            testConnection();
            long cost=System.currentTimeMillis() - start;
            logger.info("=========================消耗时间:" + cost);            
            sleepTime = 100000+Math.round(Math.random() * 60000);
            logger.info("=========================sleepTime:" + sleepTime);
            try {
                Thread.sleep(sleepTime);               
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // TODO Auto-generated method stub
        this.isRunning = true;
        return true;
    }


    public static void testConnection() {

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
        // TODO Auto-generated method stub
        String url = "https://wexpress-mobipay.ymcft.com:4431/MobilePay/MobilePayService.aspx";
        
        

        String chongqingUrl = VarManageServer.getSingleInstance().getVarStringValue("chongqing_url");
        if (StrFilter.hasValue(chongqingUrl)) {
            url = chongqingUrl;
        }
        String charset = "utf-8";

        HttpClient client = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
            String xmlInfo =
                    "323A003E7CDCBA0177D5CC213B192DAE55<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            + "<SERVICE>"
                            + "<SYS_HEAD><TRAN_CODE>AM0003</TRAN_CODE><MER_CODE>30103005</MER_CODE>"
                            + "<POS_CODE>10000001</POS_CODE><TRAN_DATE>20140418</TRAN_DATE>"
                            + "<TRAN_TIME>102310</TRAN_TIME><TRAN_SEQ>00002001</TRAN_SEQ></SYS_HEAD>"
                            + "<BODY>" + "<MER_ORDER>301030052</MER_ORDER>" + "<TRAN_AMT>123.09</TRAN_AMT>"
                            + "<ACCT_NO>OD31234519</ACCT_NO>" + "<PAY_STAT>1</PAY_STAT>" + "</BODY>"
                            + "</SERVICE>";
            byte[] reqBytes = (byte[]) xmlInfo.getBytes(charset);
            httpPost.setEntity(new InputStreamEntity(new ByteArrayInputStream(reqBytes), ContentType.create(
                "application/xml", charset)));
             httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
             false);
            HttpResponse response = client.execute(httpPost);

            HttpEntity entity = response.getEntity();
            byte[] result = EntityUtils.toByteArray(entity);
            String receiveMsg = new String(result, charset);
            logger.info("recieve:" + new String(receiveMsg.getBytes(charset), charset));

        } catch (Exception ex) {
            ex.printStackTrace();
        }finally{
            if(httpPost!=null){
                httpPost.releaseConnection();
            }          
        }

    }


    private static HttpClient getHttpClient() {
        try {
            if (httpClient != null) {
                return httpClient;
            }
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string) {
                }


                public void checkServerTrusted(X509Certificate[] xcs, String string) {
                }


                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { tm }, null);

            SSLSocketFactory socketFactory =
                    new SSLSocketFactory(sslcontext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            // 不校验域名
            socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme sch = new Scheme("https", 443, socketFactory);

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(sch);

            HttpParams params = new BasicHttpParams();
            params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
            params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
            params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
            params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 600000);
            params.setParameter(CoreConnectionPNames.SO_KEEPALIVE, 10000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
//            cm.setMaxTotal(100);
//            cm.setDefaultMaxPerRoute(50);
            httpClient = new DefaultHttpClient(cm, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return httpClient;
    }


    public static void main(String[] args) {
        System.out.println(Math.round(Math.random() * 120000));
    }
}
