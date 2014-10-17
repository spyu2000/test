package com.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import server.var.VarManageServer;

import com.fleety.base.StrFilter;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;
import com.server.customerDbServer.HIBADbServer;


public class HttpClientTestServer extends BasicServer {

    private static HttpClientTestServer singleInstance = new HttpClientTestServer();


    public static HttpClientTestServer getSingleInstance() {
        return singleInstance;
    }


    public boolean startServer() {
        try {

            int times =
                    Integer.parseInt(VarManageServer.getSingleInstance().getVarStringValue("chongqing_times"));
            long sleepTime = 0;
            for(int i=0;i<times;i++){
                long start = new Date().getTime();
                testHttpClientTime();
               sleepTime = 100000+Math.round(Math.random() * 60000);
               long cost=System.currentTimeMillis() - start;
               System.out.println("=========================消耗时间:" + cost);            
               sleepTime = 100000+Math.round(Math.random() * 60000);
               System.out.println("=========================sleepTime:" + sleepTime);
               try{
                   Thread.sleep(sleepTime);
               }catch(Exception ex){
                   ex.printStackTrace();
               }
               
            }
          

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.isRunning = true;
        return true;
    }


    public void stopServer() {
    }


    private static List<String> testHttpClientTime() throws Exception {

        String xml =
                "323A003E7CDCBA0177D5CC213B192DAE55";

        String url = "http://101.231.67.134:8050";
        if (StrFilter.hasValue(VarManageServer.getSingleInstance().getVarStringValue("url"))) {
            url = VarManageServer.getSingleInstance().getVarStringValue("url");
        }

        List<String> list = new ArrayList<String>();

        HttpClient httpClient = getHttpClient(url);
        HttpPost post =null;
        // 获得HttpPost对象
        try {
            post= new HttpPost(url);
            if (Boolean.parseBoolean(VarManageServer.getSingleInstance().getVarStringValue("debug"))) {
                System.out.println(url);
                System.out.println(xml);
            }
            post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            post.setEntity(new InputStreamEntity(new ByteArrayInputStream(xml.getBytes()), ContentType.create(
                "application/xml", "utf-8")));
            // 发送请求
            HttpResponse response = httpClient.execute(post);
            // 输出返回值
            InputStream is = response.getEntity().getContent();
            String s = EntityUtils.toString(response.getEntity());
            if (Boolean.parseBoolean(VarManageServer.getSingleInstance().getVarStringValue("debug"))) {
                System.out.println(s);
            }
        } finally {
          if(post!=null){
              post.releaseConnection();
          }
        }
        return list;
    }


    private void testSqlServer() {
        HIBADbServer dbServer = HIBADbServer.getSingleInstance();
        DbHandle conn = null;
        try {
            conn = dbServer.getConn();
            StatementHandle stmt = conn.createStatement();
            String sql = "select * from orderinfo where id>230";
            ResultSet rs = stmt.executeQuery(sql);
            int i = 0;
            StringBuffer buff = new StringBuffer();
            while (rs.next()) {
                buff.append("INSERT INTO orderInfo (OrderId, Result, CreateDate, xmlContent, HandleStatus, orderType) VALUES ");
                buff.append("(");
                buff.append("'" + rs.getString("OrderId") + "',");
                buff.append("'" + rs.getString("Result") + "',");
                buff.append("'" + rs.getString("CreateDate") + "',");
                buff.append("'" + rs.getString("xmlContent") + "',");
                buff.append("'" + rs.getString("HandleStatus") + "',");
                buff.append("'" + rs.getString("orderType") + "');");
                i++;
            }
            System.out.println(buff.toString());
            System.out.println("总记录数:" + i);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dbServer.releaseConn(conn);
        }

    }

    
    private static HashMap<String,org.apache.http.client.HttpClient> httpClientMap=new HashMap<String,org.apache.http.client.HttpClient>();
    private static org.apache.http.client.HttpClient getHttpClient(String url){
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");

        
        org.apache.http.client.HttpClient client=httpClientMap.get(url);
        if(client!=null){
            return client;
        }        
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        HttpParams params = new BasicHttpParams();
        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
        params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,15000);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT,30000);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(50);        
        client = new DefaultHttpClient(cm, params);        
        httpClientMap.put(url, client);        
        return client;
    }
}
