package com.server.customerDbServer;


import server.db.DbServer;

public class HIBADbServer extends DbServer
{
    private static HIBADbServer instance = null;

    public static HIBADbServer getSingleInstance()
    {
        if (instance == null)
        {
            instance = new HIBADbServer();
        }
        return instance;
    }

    public boolean startServer()
    {
        return super.startServer();
    }

    public void stopServer()
    {
        super.stopServer();
    }
}
