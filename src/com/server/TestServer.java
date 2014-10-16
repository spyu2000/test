package com.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import server.var.VarManageServer;

import com.fleety.base.StrFilter;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;
import com.server.customerDbServer.HIBADbServer;


public class TestServer extends BasicServer {

    private static TestServer singleInstance = new TestServer();


    public static TestServer getSingleInstance() {
        return singleInstance;
    }


    public boolean startServer() {
        try {

            long now = new Date().getTime();
            // testSqlServer();
            // clientSSL();
            long end = new Date().getTime();

            System.out.println("startTime:" + new Date(now) + "   endTime:" + new Date(end) + "   costTime:"
                    + (end - now));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.isRunning = true;
        return true;
    }


    public void stopServer() {
    }


    private static List<String> clientSSL() throws Exception {

        String xml =
                "323A003E7CDCBA0177D5CC213B192DAE55<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<SERVICE>"
                        + "<SYS_HEAD><TRAN_CODE>AM0003</TRAN_CODE><MER_CODE>30103005</MER_CODE>"
                        + "<POS_CODE>10000001</POS_CODE><TRAN_DATE>20140418</TRAN_DATE>"
                        + "<TRAN_TIME>102310</TRAN_TIME><TRAN_SEQ>00002001</TRAN_SEQ></SYS_HEAD>" + "<BODY>"
                        + "<MER_ORDER>301030052</MER_ORDER>" + "<TRAN_AMT>123.09</TRAN_AMT>"
                        + "<ACCT_NO>OD31234519</ACCT_NO>" + "<PAY_STAT>1</PAY_STAT>" + "</BODY>"
                        + "</SERVICE>";

        String url = "http://101.231.67.134:8050";
        if (StrFilter.hasValue(VarManageServer.getSingleInstance().getVarStringValue("url"))) {
            url = VarManageServer.getSingleInstance().getVarStringValue("url");
        }

        List<String> list = new ArrayList<String>();

        HttpClient httpClient = new DefaultHttpClient();
        // 获得HttpGet对象
        HttpPost post = new HttpPost(url);
        if (Boolean.parseBoolean(VarManageServer.getSingleInstance().getVarStringValue("debug"))) {
            System.out.println(url);
            System.out.println(xml);
        }

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

        httpClient.getConnectionManager().shutdown();
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

}
