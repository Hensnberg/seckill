package com.xxxx.seckill.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.result.Result;
import com.xxxx.seckill.vo.RespBean;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserUtil {

    public static void createUser(int count) throws Exception{
         List<User> users=new ArrayList<>(count);
         for(int i=0;i<count;i++){
             User user=new User();
             user.setId(13000000000L+i);
             user.setNickname("user"+i);
             user.setSalt("1a2b3c4d");
             //System.out.println(MD5Util.inputPassToDBPass("123456",user.getSalt()));
             user.setPassword(MD5Util.inputPassToDBPass("123456",user.getSalt()));
             user.setLoginCount(1);
             user.setRegisterDate(new Date());
             users.add(user);
         }
         System.out.println("create users");
//        Connection conn = getConn();
//        String sql = "insert into t_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
//        PreparedStatement pstmt = conn.prepareStatement(sql);
//        for(int i=0;i<users.size();i++) {
//            User user = users.get(i);
//            pstmt.setInt(1, user.getLoginCount());
//            pstmt.setString(2, user.getNickname());
//            pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
//            pstmt.setString(4, user.getSalt());
//            pstmt.setString(5, user.getPassword());
//            pstmt.setLong(6, user.getId());
//            pstmt.addBatch();
//        }
//        pstmt.executeBatch();
//        pstmt.close();
//        conn.close();
//        System.out.println("insert to db");
        //登录，生成token
        String urlString = "http://localhost:8080/login/dologin";
        File file = new File("C:\\Users\\14404\\Desktop\\config1.txt");
        if(file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for(int i=0;i<users.size();i++) {
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection)url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile="+user.getId()+"&password="+MD5Util.inputPassToFromPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0 ,len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            //System.out.println(respBean);

//            JSONObject jo = JSON.parseObject(response);
//            String token = jo.getString("data");
//            ObjectMapper mapper=new ObjectMapper();
//            Result result=mapper.readValue(response, Result.class);
//            Result result=JSON.parseObject(response, Result.class);
//            String respjson=((String)result.getData());
            RespBean respBean=JSON.parseObject(response,RespBean.class);
            String userTicket=((String)respBean.getObj());
            System.out.println("create userTicket:"+user.getId());
            String row=user.getId()+","+userTicket;

//            System.out.println("create token : " + user.getId());
//
//            String row = user.getId()+","+token;

            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getId());
        }
        raf.close();

        System.out.println("over");
    }

    public static Connection getConn() throws Exception{
        String url="jdbc:mysql://localhost:3306/seckill?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8";
        String username="root";
        String userpassword="admin";
        String driver="com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url,username,userpassword);
    }

    public static void main(String[] args)throws Exception {
        createUser(5000);
    }
}
