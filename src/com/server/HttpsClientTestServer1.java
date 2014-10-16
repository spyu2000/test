package com.server;

import java.io.ByteArrayInputStream;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import server.var.VarManageServer;

import com.fleety.base.StrFilter;
import com.fleety.server.BasicServer;


public class HttpsClientTestServer1 extends BasicServer {
    private static final Logger logger = Logger.getLogger("HttpsClientTest");

    private static HttpsClientTestServer1 instance = null;

    private static CloseableHttpClient httpClient = null;


    public static HttpsClientTestServer1 getSingleInstance() {
        if (instance == null) {
            instance = new HttpsClientTestServer1();
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
            sleepTime = Math.round(Math.random() * 300000);
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
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(0)
                    .setConnectTimeout(15000)
                    .setConnectionRequestTimeout(15000).build();
            httpPost.setConfig(requestConfig);
            
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
//             httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
//             false);
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
            
            ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy()  {
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    // Honor 'keep-alive' header
                    HeaderElementIterator it = new BasicHeaderElementIterator(
                            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        HeaderElement he = it.nextElement();
                        String param = he.getName();
                        String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            try {
                                logger.info("服务端的保持连接时常:"+value);
                                return Long.parseLong(value) * 1000;
                            } catch(NumberFormatException ignore) {
                            }
                        }
                    }
                    return 300 * 1000;
                }
            };
            
            
           PoolingHttpClientConnectionManager connManager = null;      
           try {
               SSLContext sslContext = SSLContexts.custom().useTLS().build();
               sslContext.init(null,
                       new TrustManager[] { new X509TrustManager() {                     
                            
                           public X509Certificate[] getAcceptedIssuers() {
                               return null;
                           }
    
                           public void checkClientTrusted(
                                   X509Certificate[] certs, String authType) {
                           }
    
                           public void checkServerTrusted(
                                   X509Certificate[] certs, String authType) {
                           }
                       }}, null);
               Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                       .register("http", PlainConnectionSocketFactory.INSTANCE)
                       .register("https", new SSLConnectionSocketFactory(sslContext))
                       .build();
                
               connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
               httpClient = HttpClients.custom().setConnectionManager(connManager).setKeepAliveStrategy(myStrategy).build();
               // Create socket configuration
               SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
               connManager.setDefaultSocketConfig(socketConfig);
               // Create message constraints
               MessageConstraints messageConstraints = MessageConstraints.custom()
                   .setMaxHeaderCount(200)
                   .setMaxLineLength(2000)
                   .build();
               // Create connection configuration
               ConnectionConfig connectionConfig = ConnectionConfig.custom()
                   .setMalformedInputAction(CodingErrorAction.IGNORE)
                   .setUnmappableInputAction(CodingErrorAction.IGNORE)
                   .setCharset(Consts.UTF_8)
                   .setMessageConstraints(messageConstraints)
                   .build();
               connManager.setDefaultConnectionConfig(connectionConfig);
               connManager.setMaxTotal(200);
               connManager.setDefaultMaxPerRoute(20);
           } catch (KeyManagementException e) {
               logger.info("KeyManagementException"+ e.getMessage());
           } catch (NoSuchAlgorithmException e) {
               logger.info("NoSuchAlgorithmException"+ e.getMessage());
           }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return httpClient;
    }


    public static void main(String[] args) {
        System.out.println(Math.round(Math.random() * 120000));
    }
}
