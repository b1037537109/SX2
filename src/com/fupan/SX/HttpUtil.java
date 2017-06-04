package com.fupan.SX;





import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import java.net.URL;



public class HttpUtil {

	public static boolean isNetWorkAvailable() {

        URL url = null;
        try {
            url = new URL("http://www.gohigh.top");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(2000);
            urlc.connect();
            if (urlc.getResponseCode() == 200) {
                return new Boolean(true);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch(UnknownHostException e){
        	e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return false;
	}


    public static String httpGet(String url_str,String[] property){
        if(url_str.isEmpty())return null;
        URL url=null;
        HttpURLConnection conn=null;

        byte[] responseBody=null;


        try{
            url=new URL(url_str);
            conn=(HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");


            for (int i=0;i<property.length;i++){
               int index=property[i].indexOf(":");
                String a=property[i].substring(0,index);
                String b=property[i].substring(index+1);
                if(a.isEmpty()||b.isEmpty())continue;
                conn.setRequestProperty(a,b);
            }


            conn.setUseCaches(false);
            conn.connect();


            InputStream is=conn.getInputStream();


            responseBody=getBytesByInputStream(is);

            String str=getStringByBytes(responseBody);
            is.close();


            return str;


        }catch (MalformedURLException e) {
        	e.printStackTrace();
        }catch (IOException e){
        	 e.printStackTrace();
        }catch (Exception e){
        	 e.printStackTrace();
        }

        return null;
    }


    private static byte[] getBytesByInputStream(InputStream is) {
        byte[] bytes = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024 * 8];
        int length = 0;
        try {
            while ((length = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }




    private static String getStringByBytes(byte[] bytes) {
        String str = "";
        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
    
}
    
    


    