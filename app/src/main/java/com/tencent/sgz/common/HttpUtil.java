package com.tencent.sgz.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * HttpUtil Class Capsule Most Functions of Http Operations
 *
 * @author sfshine
 *
 */
public class HttpUtil {
    private static Header[] headers = new BasicHeader[1];
    private static String TAG = "HTTPUTIL";
    private static int TIMEOUT = 8 * 1000;
    private static final String BOUNDARY = "---------------------------7db1c523809b2";
    /**
     * Your header of http op
     *
     * @return
     */
    static {

        headers[0] = new BasicHeader("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 5.0; Windows XP; DigExt)");

    }

    public static boolean delete(String murl) throws Exception {
        URL url = new URL(murl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(TIMEOUT);
        if (conn.getResponseCode() == 204) {

            MLog.e(conn.toString());
            return true;
        }
        MLog.e(conn.getRequestMethod());
        MLog.e(conn.getResponseCode() + "");
        return false;
    }

    /**
     * Op Http get request
     *
     * @param url
     * @return
     */
    static public String get(String url) {
        return get(url, null,null,null);

    }

    public static HttpGet getHttpGet(String url,HashMap<String,String> map,String cookie, String userAgent){
        //HttpClient client = getNewHttpClient();
        url = getFullUrl(url,map);
        HttpGet get = new HttpGet(url);
        get.setHeaders(headers);
        get.setHeader("Connection","Keep-Alive");
        if(!StringUtils.isEmpty(cookie)){
            get.setHeader("Cookie",cookie);
        }
        if(!StringUtils.isEmpty(userAgent)){
            get.setHeader("User-Agent",userAgent);
        }

        return get;
    }

    public static HttpPost getHttpPost(String url,HashMap<String,String> map,String cookie, String userAgent) throws Exception{
        HttpPost post = new HttpPost(url);
        post.setHeaders(headers);
        post.setHeader("Connection","Keep-Alive");
        if(!StringUtils.isEmpty(cookie)){
            post.setHeader("Cookie",cookie);
        }
        if(!StringUtils.isEmpty(userAgent)){
            post.setHeader("User-Agent",userAgent);
        }

        ArrayList<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Log.i(TAG, entry.getKey() + "=>" + entry.getValue());
                BasicNameValuePair pair = new BasicNameValuePair(
                        entry.getKey(), entry.getValue());
                pairList.add(pair);
            }

        }

        //TODO:文件上传

        HttpEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
        post.setEntity(entity);


        return post;
    }

    static private String getFullUrl(String url,HashMap<String,String> map){
        if (null != map) {
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {

                Log.i(TAG, entry.getKey() + "=>" + entry.getValue());
                if (i == 0) {
                    url = url + "?" + entry.getKey() + "=" + entry.getValue();
                } else {
                    url = url + "&" + entry.getKey() + "=" + entry.getValue();
                }

                i++;

            }
        }
        return url;
    }

    static public String get(String url, HashMap<String, String> map,String cookie,String userAgent) {

        HttpClient client = getNewHttpClient();
        String result = "ERROR";
        HttpGet get = getHttpGet(url,map,cookie,userAgent);

        try {

            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // setCookie(response);
                result = EntityUtils.toString(response.getEntity(), "UTF-8");

            } else {
                result = EntityUtils.toString(response.getEntity(), "UTF-8")
                        + response.getStatusLine().getStatusCode() + "ERROR";
            }

        } catch (ConnectTimeoutException e) {
            result = "TIMEOUTERROR";
            e.printStackTrace();
        }catch (Exception e) {
            result = "OTHERERROR";
            e.printStackTrace();

        }finally {
            get.abort();
        }
        //Log.i(TAG, "result =>" + result);

        return result;
    }

    /**
     * Op Http post request , "404error" response if failed
     *
     * @param url
     * @param map
     *            Values to request
     * @return
     */

    static public String post(String url, HashMap<String, String> map,String cookie,String userAgent,Handler cookieHandler) {

        HttpClient client = getNewHttpClient();

        String result = "ERROR";
        HttpPost post = null;

        try {
            post = getHttpPost(url,map,cookie,userAgent);
            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                result = EntityUtils.toString(response.getEntity(), "UTF-8");

            } else {
                result = EntityUtils.toString(response.getEntity(), "UTF-8")
                        + response.getStatusLine().getStatusCode() + "ERROR";
            }

            //cookie处理
            if(null!=cookieHandler){
                Message msg = new Message();
                msg.what = 0;
                msg.obj = response.getHeaders("Set-Cookie");
                cookieHandler.sendMessage(msg);
            };

        } catch (ConnectTimeoutException e) {
            result = "TIMEOUTERROR";
            e.printStackTrace();
        }catch (Exception e) {
            result = "OTHERERROR";
            e.printStackTrace();

        }finally {
            post.abort();
        }
        //Log.i(TAG, "result =>" + result);
        return result;
    }

    /**
     * 自定义的http请求可以设置为DELETE PUT等而不是GET
     *
     * @param url
     * @param params
     * @param method
     * @throws java.io.IOException
     */

    public static String customrequest(String url,
                                       HashMap<String, String> params, String method) {
        try {

            URL postUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) postUrl
                    .openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(TIMEOUT);

            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 5.0; Windows XP; DigExt)");

            conn.connect();
            OutputStream out = conn.getOutputStream();
            StringBuilder sb = new StringBuilder();
            if (null != params) {
                int i = params.size();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (i == 1) {
                        sb.append(entry.getKey() + "=" + entry.getValue());
                    } else {
                        sb.append(entry.getKey() + "=" + entry.getValue() + "&");
                    }

                    i--;
                }
            }
            String content = sb.toString();
            out.write(content.getBytes("UTF-8"));
            out.flush();
            out.close();
            InputStream inStream = conn.getInputStream();
            String result = inputStream2String(inStream);
            Log.i(TAG, "result>" + result);
            conn.disconnect();
            return result;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    /**
     * 必须严格限制get请求所以增加这个方法 这个方法也可以自定义请求
     *
     * @param url
     * @param method
     * @throws Exception
     */

    public static String customrequestget(String url,
                                          HashMap<String, String> map, String method) {

        if (null != map) {
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {

                if (i == 0) {
                    url = url + "?" + entry.getKey() + "=" + entry.getValue();
                } else {
                    url = url + "&" + entry.getKey() + "=" + entry.getValue();
                }

                i++;
            }
        }
        try {

            URL murl = new URL(url);
            System.out.print(url);
            HttpURLConnection conn = (HttpURLConnection) murl.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestMethod(method);

            conn.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 5.0; Windows XP; DigExt)");

            InputStream inStream = conn.getInputStream();
            String result = inputStream2String(inStream);
            Log.i(TAG, "result>" + result);
            conn.disconnect();
            return result;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    /**
     * 上传多张图片
     */
    public static String post(String actionUrl, Map<String, String> params,
                              Map<String, File> files) {

        String BOUNDARY = UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        try {

            URL uri = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(TIMEOUT); // 缓存的最长时间
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                    + ";boundary=" + BOUNDARY);

            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINEND);
                sb.append("Content-Disposition: form-data; name=\""
                        + entry.getKey() + "\"" + LINEND);
                sb.append("Content-Type: text/plain; charset=" + CHARSET
                        + LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                sb.append(LINEND);
                sb.append(entry.getValue());
                sb.append(LINEND);
            }

            DataOutputStream outStream = new DataOutputStream(
                    conn.getOutputStream());
            outStream.write(sb.toString().getBytes());
            InputStream in = null;
            // 发送文件数据
            if (files != null) {
                for (Map.Entry<String, File> file : files.entrySet()) {

                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data; name=\""
                            + file.getKey() + "\"; filename=\""
                            + file.getValue().getName() + "\"" + LINEND);
                    sb1.append("Content-Type: image/pjpeg; " + LINEND);
                    sb1.append(LINEND);
                    outStream.write(sb1.toString().getBytes());

                    InputStream is = new FileInputStream(file.getValue());
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }

                    is.close();
                    outStream.write(LINEND.getBytes());
                }

                // 请求结束标志
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND)
                        .getBytes();
                outStream.write(end_data);
                outStream.flush();
                // 得到响应码
                int res = conn.getResponseCode();
                // if (res == 200) {
                in = conn.getInputStream();
                int ch;
                StringBuilder sb2 = new StringBuilder();
                while ((ch = in.read()) != -1) {
                    sb2.append((char) ch);
                }

                // }
                outStream.close();
                conn.disconnect();
                return in.toString();
            }
        } catch (Exception e) {

        }
        return null;

    }

    /**
     * is转String
     *
     * @param in
     * @return
     * @throws java.io.IOException
     */
    public static String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    /**
     * check net work
     *
     * @param context
     * @return
     */
    public static boolean hasNetwork(Context context) {
        ConnectivityManager con = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo workinfo = con.getActiveNetworkInfo();
        if (workinfo == null || !workinfo.isAvailable()) {
            Toast.makeText(context, "当前无网络连接,请稍后重试", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /***
     * @category check if the string is null
     * @return true if is null
     * */
    public static boolean isNull(String string) {
        boolean t1 = "".equals(string);
        boolean t2 = string == null;
        boolean t3 = string.equals("null");
        if (t1 || t2 || t3) {
            return true;
        } else {
            return false;
        }
    }

    static public byte[] getBytes(File file) throws IOException {
        InputStream ios = null;
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }

        return ous.toByteArray();
    }

    public static class MLog {
        static public void e(String msg) {
            Log.e("=======ERROR======", msg);
        }

        static public void e(String tag, String msg) {
            Log.e(tag, msg);
        }

        static public void i(String msg) {
            Log.i("=======INFO======", msg);
        }

        static public void i(String tag, String msg) {
            Log.i(tag, msg);
        }

    }

    /**
     * 处理https加密失败的情况
     *
     * @return
     */
    public static HttpClient getNewHttpClient() {

        HttpClient client;

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new HttpUtil.SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(
                    params, registry);

            //设置 连接超时时间
            HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
            //数据读取时间
            HttpConnectionParams.setSoTimeout(params, TIMEOUT);
            ConnManagerParams.setTimeout(params, TIMEOUT);

            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            HttpClientParams.setCookiePolicy(params, CookiePolicy.BROWSER_COMPATIBILITY);

            //设置cookie


            client = new DefaultHttpClient(ccm,params);

        } catch (Exception e) {
            e.printStackTrace();
            client = new DefaultHttpClient();
        }
        return client;
    }

    static public class SSLSocketFactoryEx extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryEx(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType)
                        throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType)
                        throws java.security.cert.CertificateException {
                }
            };
            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                                   boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host,
                    port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}