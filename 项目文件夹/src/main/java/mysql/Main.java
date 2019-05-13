package mysql;

import java.sql.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
    {

        //对套餐的订购以及退订情况进行查询；
        //查询用户1在2018-01-01对套餐的操作（可以查询别的日期）
        //inquiryMeals(1, "2018-01-01");

        //订购套餐；
        //id为1的用户订购套餐4
        //orderMeals(1, 6);

        //退订---立即生效；
        //unsubscribeMealsFunctionImmediately(1, 1);

        //退订---次月生效；
        //unsubscribeMealsFunctionNextMonth(1, 3);

        //用户在通话情况下的资费生成;
        //call(1, "01:01:01", "05:01:01", "2018-01-01");

        //用户在短信情况下的资费生成
        //短信的日期：yyyy-mm-dd
        //sendMessage(1, "2018-01-01");

        //用户在使用流量下的资费生成
        //local为1代表用户是在本地使用的流量，否则，为非本地使用的流量
        //useFlow(1, "2018-01-01", 1049, 0);

        //某个用户月账单的生成
        //month的参数需要是"yyyy-mm"的形式
        //在控制台输出
        //generateBillByMonth(1, "2018-10");
    }

    /**
     * 对套餐的订购以及退订情况进行查询；
     * inquiryMeals(userId, date)
     * ps: date like "2018-01-00"
     *     read table "user_meal_operator"
     * */
    public static void inquiryMeals(int userId, String date){

        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime=time.format(new java.util.Date());
        System.out.println("对"+userId+"进行套餐的查询开始的时间：  "+startTime);

        ArrayList<String> ret=new ArrayList<String>();

        //从数据库获取历史信息
        Connection connection=null;
        Statement stmt=null;

        try {
            //注册jdbc驱动
            Class.forName("com.mysql.jdbc.Driver");

            //打开连接
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            stmt=connection.createStatement();
            String sql="select * from user_meal_operator where user_id="+userId;
            ResultSet rs=stmt.executeQuery(sql);

            //展开结果集数据库，保存返回的结果
            while(rs.next()){
                //通过字段检索
                int user_id=rs.getInt("user_id");
                int operate_id=rs.getInt("operate_id");
                int meal_id=rs.getInt("meal_id");
                String operate_time=rs.getString("operate_time");

                if(operate_time.startsWith(date)){

                    String add="";
                    if (operate_id==1){
                        add="用户"+user_id+" 于 "+operate_time+"  日，订购套餐  "+
                                meal_id;
                    }
                    else if (operate_id==2){
                        add="用户"+user_id+" 于 "+operate_time+"  日，立即退订套餐  "+
                                meal_id;
                    }else {
                        add="用户"+user_id+" 于 "+operate_time+"  日，下月退订套餐  "+
                                meal_id;
                    }

                    System.out.println(add);
                }
            }

            //完成后关闭
            rs.close();
            stmt.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            try {
                if(stmt!=null) stmt.close();
            } catch (SQLException e2) {
            }
            try {
            if (connection!=null)connection.close();
            } catch (SQLException e) {
            }
        }

        //操作结束的时间
        String endTime=time.format(new java.util.Date());
        System.out.println("对"+userId+"进行套餐的查询结束的时间：  "+endTime);

    }

    /**
     * 订购套餐；
     * orderMeals(userId, mealId)
     * ps: read table "meal_content"
     *     write table "user_meal_operator", "user_meal_surplus"
     *     operate_id为1
     * */
    public static void orderMeals(int userId, int mealId){

        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime=time.format(new java.util.Date());
        System.out.println("对用户"+userId+"进行套餐的订购开始的时间：  "+startTime);

        /**
         * 获取id为mealId的套餐的信息；
         * */
        ArrayList<Integer> baseServiceIds=new ArrayList<>();
        ArrayList<Integer> amounts=new ArrayList<>();

        try {
            //从数据库获取历史信息
            Connection connection=null;
            Statement stmt=null;

            //注册jdbc驱动
            Class.forName("com.mysql.jdbc.Driver");

            //打开连接
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            stmt=connection.createStatement();
            String sql="select * from meal_content where meal_id="+mealId;
            ResultSet rs=stmt.executeQuery(sql);

            //展开结果集数据库，保存返回的结果
            while(rs.next()){
                //通过字段检索
                int meal_id=rs.getInt("meal_id");
                int base_service_id=rs.getInt("base_service_id");
                int amount=rs.getInt("amount");

                if(meal_id==mealId){
                    baseServiceIds.add(base_service_id);
                    amounts.add(amount);
                }
            }

            //完成后关闭
            rs.close();
            stmt.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /**
         * 将套餐的相关数据存入table "user_meal_surplus"
         * */
        try {
            //向数据库插入信息
            Connection connection=null;
            PreparedStatement pstm=null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            String sql="insert into user_meal_surplus(id, user_id, meal_id, base_service_id, surplus, date) values (?, ?, ?, ?, ?, ?)  ";
            pstm=connection.prepareStatement(sql);

            //保存结果
            for (int i=0;i<baseServiceIds.size();i++){//1秒=1000毫秒
                Calendar Cld = Calendar.getInstance();
                String timeMi=Cld.get(Calendar.YEAR)+""+
                        Cld.get(Calendar.MONTH)+""+Cld.get(Calendar.DATE)+""+
                        Cld.get(Calendar.HOUR_OF_DAY)+""+Cld.get(Calendar.MINUTE)+""+
                        Cld.get(Calendar.SECOND)+""+Cld.get(Calendar.MILLISECOND)
                        +"";
                pstm.setString(1, timeMi);
                pstm.setInt(2, userId);
                pstm.setInt(3, mealId);
                pstm.setInt(4, baseServiceIds.get(i));
                pstm.setInt(5, amounts.get(i));
                SimpleDateFormat timeDate=new SimpleDateFormat("yyyy-MM-dd");
                String timeDateStr=timeDate.format(new java.util.Date());
                pstm.setString(6, timeDateStr);
                pstm.executeUpdate();
            }

            //完成后关闭
            pstm.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /**
         * 将用户对套餐的操作存入table "user_meal_operator"
         * */
        try {
            //向数据库插入信息
            Connection connection=null;
            PreparedStatement pstm=null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            String sql="insert into user_meal_operator(user_id, operate_id, meal_id, operate_time) values (?, ?, ?, ?)  ";
            pstm=connection.prepareStatement(sql);

            //保存结果
            pstm.setInt(1, userId);
            pstm.setInt(2, 1);
            pstm.setInt(3, mealId);
            String dateStr=time.format(new java.util.Date());
            //插入datetime类型的mysql数据的方法之一
            pstm.setString(4, dateStr);
            pstm.executeUpdate();

            //完成后关闭
            pstm.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //操作结束的时间
        String endTime=time.format(new java.util.Date());
        System.out.println("对用户"+userId+"进行套餐的订购结束的时间：  "+endTime);

    }

    /**
     * 退订---立即生效；
     * unsubscribeMealsFunctionImmediately(userId, mealId);
     * ps: write table "user_meal_operator",
     *                 "user_meal_outside_charge", "user_meal_surplus"
     *     read table "user_meal_surplus", "meal_content"
     * */
    public static void unsubscribeMealsFunctionImmediately(int userId, int mealId){

        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对套餐"+mealId+"进行的立即退订的开始的时间：  "+startTime);

        /**
         * 读取table "meal_content"
         * 获取想要退订的套餐的内容
         * */
        ArrayList<Integer> baseServiceIds=new ArrayList<>();
        ArrayList<Float> amounts=new ArrayList<>();
        try {
            //从数据库查询信息
            Connection connection=null;
            Statement stmt = null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            stmt = connection.createStatement();
            String sql="select * from meal_content where meal_id="+mealId;
            ResultSet rs = stmt.executeQuery(sql);

            //展开结果数据集
            while(rs.next()){
                int base_service_id=rs.getInt("base_service_id");
                float amount=rs.getFloat("amount");

                baseServiceIds.add(base_service_id);
                amounts.add(amount);
            }

            //完成后关闭
            rs.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /**
         * 读取table "user_meal_surplus"
         * 计算已经花取的要退订的套餐的基本资费（退订花费最少的那个）
         * */
        ArrayList<String> ids=new ArrayList<>();//套餐的剩余基准服务的id号
        float[] surpluses=new float[20];//猜测套餐的基准服务内容不超过20项
        try {
            //从数据库查询信息
            Connection connection=null;
            Statement stmt = null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询已用
            stmt = connection.createStatement();
            String sql="select * from user_meal_surplus where user_id="+userId+" and " +
                    "meal_id="+mealId+" order by surplus desc";
            ResultSet rs = stmt.executeQuery(sql);

            //获取剩余的基准服务的数量
            //判断这个基准服务是否是要退订的套餐里面的，以免退订重复的基准服务
            int[] isLacking=new int[20];
            for (int i=0;i<baseServiceIds.size();i++){
                /**
                 * 初始化为1，即缺乏此基准服务的退订数据；
                 * 否则为0，即该基准服务的退订数据已经获得；
                 * */
                isLacking[i]=1;
            }
            while(rs.next()){
                String leftId=rs.getString("id");
                int base_service_id=rs.getInt("base_service_id");
                float surplus=rs.getFloat("surplus");

                //获取对应套餐的基准服务的剩余值
                for(int i=0;i<isLacking.length;i++){
                    //缺乏这个基准服务，而且这条row的base_service_id和baseServiceIds对应的基准服务相同
                    if ((isLacking[i]==1)&&(base_service_id==baseServiceIds.get(i))){
                        isLacking[i]=0;
                        surpluses[i]=surplus;//添加剩余的数量
                        ids.add(leftId);
                    }
                }

                //当获取的服务数量满足的时候，退出查询
                if(surpluses.length==baseServiceIds.size())
                    break;
            }

            //完成后关闭
            rs.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /**
         * 计算已经使用了的基准服务的内容，
         * 将已经使用过了的基准服务的内容添加到table "user_meal_outside_charge"
         */
        try {
            //向数据库插入信息
            Connection connection=null;
            PreparedStatement pstm=null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            String sql="insert into user_meal_outside_charge(user_id, base_service_id, consume, date) values (?, ?, ?, ?)  ";
            pstm=connection.prepareStatement(sql);

            //插入内容
            for (int i=0;i<baseServiceIds.size();i++){
                pstm.setInt(1, userId);
                pstm.setInt(2, baseServiceIds.get(i));
                pstm.setFloat(3, amounts.get(i)-surpluses[i]);
                SimpleDateFormat time2=new SimpleDateFormat("yyyy-MM-dd");
                String dateStr=time2.format(new java.util.Date());
                pstm.setString(4, dateStr);
                pstm.executeUpdate();
            }

            //完成后关闭
            pstm.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /**
         * 删除套餐剩余的基准的服务，
         * 将已经添加到table "user_meal_outside_charge"中的基准服务消费记录
         * 在table "user_meal_surplus"中删除
         * */
        try {
            //从数据库查询信息
            Connection connection=null;
            PreparedStatement pstm=null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行删除
            String sql="delete from user_meal_surplus where id=?";
            pstm=connection.prepareStatement(sql);
            for(int i=0;i<ids.size();i++){
                pstm.setString(1, ids.get(i));
                pstm.executeUpdate();
            }

            //完成后关闭
            pstm.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /**
         * 将用户对套餐的操作存入table "user_meal_operator"
         * */
        try {
            //向数据库插入信息
            Connection connection=null;
            PreparedStatement pstm=null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            String sql="insert into user_meal_operator(user_id, operate_id, meal_id, operate_time) values (?, ?, ?, ?)  ";
            pstm=connection.prepareStatement(sql);

            //保存结果
            pstm.setInt(1, userId);
            pstm.setInt(2, 2);
            pstm.setInt(3, mealId);
            String dateStr=time.format(new java.util.Date());
            //插入datetime类型的mysql数据的方法之一
            pstm.setString(4, dateStr);
            pstm.executeUpdate();

            //完成后关闭
            pstm.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //操作结束的时间
        String endTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对套餐"+mealId+"进行的立即退订的结束的时间：  "+endTime);

    }

    /**
     * 退订---次月生效；
     * unsubscribeMealsFunctionNextMonth(userId, mealId);
     * ps: write table "user_meal_operator"
     * */
    public static void unsubscribeMealsFunctionNextMonth(int userId, int mealId){

        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对套餐"+mealId+"进行的次月生效退订的开始的时间：  "+startTime);

        /**
         * 将用户对套餐的操作存入table "user_meal_operator"
         * */
        try {
            //向数据库插入信息
            Connection connection=null;
            PreparedStatement pstm=null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            String sql="insert into user_meal_operator(user_id, operate_id, meal_id, operate_time) values (?, ?, ?, ?)  ";
            pstm=connection.prepareStatement(sql);

            //保存结果
            pstm.setInt(1, userId);
            pstm.setInt(2, 3);
            pstm.setInt(3, mealId);
            String dateStr=time.format(new java.util.Date());
            //插入datetime类型的mysql数据的方法之一
            pstm.setString(4, dateStr);
            pstm.executeUpdate();

            //完成后关闭
            pstm.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //操作结束的时间
        String endTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对套餐"+mealId+"进行的次月生效退订的结束的时间：  "+endTime);

    }

    /**
     * 用户在通话情况下的资费生成；
     * call(int userId, String startTime, String endTime)
     * */
    public static void call(int userId, String startTime, String endTime, String date) {
        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime2=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对通话操作记录开始的时间：  "+startTime2);


        /**
         * 读取该用户的套餐中的通话的基准服务有哪些剩余的
         * */
        ArrayList<String> surplusIds=new ArrayList<>();//user_meal_surplus的id号
        Float[] surpluses=new Float[200];//剩余的通话的数量

        try {
            //从数据库查询信息
            Connection connection=null;
            Statement stmt = null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            stmt = connection.createStatement();
            String sql="select * from user_meal_surplus where user_id="+userId
                    +" and base_service_id=1 and surplus>0";
            ResultSet rs = stmt.executeQuery(sql);

            //展开结果数据集
            int index=0;
            while(rs.next()){
                String surplus_id=rs.getString("id");
                float surplus=rs.getFloat("surplus");

                surplusIds.add(surplus_id);
                surpluses[index]=surplus;
                index++;
            }

            //完成后关闭
            rs.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //计算需要在套餐中减去的通话的分钟数
        String[] timeSplit1=startTime.split(":");
        String[] timeSplit2=endTime.split(":");

        float stTime=Integer.parseInt(timeSplit1[0])*60+Integer.parseInt(timeSplit1[1]);
        float edTime=Integer.parseInt(timeSplit2[0])*60+Integer.parseInt(timeSplit2[1]);

        float callMinute = edTime-stTime;
        if(Integer.parseInt(timeSplit2[2])>Integer.parseInt(timeSplit1[2]))
            callMinute++;

        //计算套餐中通话余量
        float sumSurpluses=0;
        for (int i=0;i<surplusIds.size();i++){
            sumSurpluses=sumSurpluses+surpluses[i];
        }
        //套餐中有足够的通话的余量
        if(sumSurpluses>=callMinute){
           while(callMinute>0){
               //从余量最少的那个开始用
               float minSurplus=surpluses[0];
               int minIndex=0;
               for(int i=0;i<surplusIds.size();i++){
                   if (surpluses[i]!=0){
                       minSurplus=surpluses[i];
                       minIndex=i;
                       break;
                   }
               }

               for(int i=0;i<surplusIds.size();i++){
                   if ((surpluses[i]<minSurplus)&&(surpluses[i]>0)){
                       minSurplus=surpluses[i];
                       minIndex=i;
                   }
               }

               if(minSurplus>=callMinute){//最小的那个套餐余量够减
                   //对id为surplusIds.get(minIndex)的基准服务余量减去
                   try {
                       //向数据库插入信息
                       Connection connection=null;
                       PreparedStatement pstm=null;

                       Class.forName("com.mysql.jdbc.Driver");
                       connection= DriverManager.getConnection(
                               "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                               "root","Dww112358"
                       );

                       //执行查询
                       String sql="update user_meal_surplus set surplus=? " +
                               "where id="+surplusIds.get(minIndex);
                       pstm=connection.prepareStatement(sql);

                       //保存结果
                       pstm.setFloat(1, surpluses[minIndex]-callMinute);
                       surpluses[minIndex]=surpluses[minIndex]-callMinute;
                       pstm.executeUpdate();

                       //完成后关闭
                       pstm.close();
                       connection.close();
                   } catch (SQLException e) {
                       e.printStackTrace();
                   }  catch (ClassNotFoundException e) {
                       e.printStackTrace();
                   }

                   callMinute=0;
               }
               //最小的套餐余量不够减，需要循环几次
               else{
                   try {
                       //向数据库插入信息
                       Connection connection=null;
                       PreparedStatement pstm=null;

                       Class.forName("com.mysql.jdbc.Driver");
                       connection= DriverManager.getConnection(
                               "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                               "root","Dww112358"
                       );

                       //执行查询
                       String sql="update user_meal_surplus set surplus=? " +
                               "where id=?";
                       pstm=connection.prepareStatement(sql);

                       //保存结果
                       pstm.setFloat(1, 0);
                       pstm.setString(2, surplusIds.get(minIndex));
                       pstm.executeUpdate();

                       callMinute=callMinute-surpluses[minIndex];

                       surpluses[minIndex]= Float.valueOf(0);

                       //完成后关闭
                       pstm.close();
                       connection.close();
                   } catch (SQLException e) {
                       e.printStackTrace();
                   }  catch (ClassNotFoundException e) {
                       e.printStackTrace();
                   }
               }
           }
        }
        else{
            //如果套餐中没有通话余量
            if (sumSurpluses==0){
                try {
                    //向数据库插入信息
                    Connection connection=null;
                    PreparedStatement pstm=null;

                    Class.forName("com.mysql.jdbc.Driver");
                    connection= DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                            "root","Dww112358"
                    );

                    //执行查询
                    String sql="insert into user_meal_outside_charge(user_id, base_service_id, consume, date) values (?, ?, ?, ?)  ";
                    pstm=connection.prepareStatement(sql);

                    //保存结果
                    pstm.setInt(1, userId);
                    pstm.setInt(2, 1);
                    pstm.setFloat(3, callMinute);
                    String dateStr=time.format(new java.util.Date());
                    //插入datetime类型的mysql数据的方法之一
                    pstm.setString(4, dateStr);
                    pstm.executeUpdate();

                    //完成后关闭
                    pstm.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }  catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //如果套餐中的通话余量不够使用
            else{
                float exit=callMinute-sumSurpluses;
                callMinute=sumSurpluses;

                //使用完套餐
                while(callMinute>0){
                    //从余量最少的那个开始用
                    float minSurplus=surpluses[0];
                    int minIndex=0;
                    for(int i=0;i<surplusIds.size();i++){
                        if (surpluses[i]!=0){
                            minSurplus=surpluses[i];
                            minIndex=i;
                            break;
                        }
                    }

                    for(int i=0;i<surplusIds.size();i++){
                        if ((surpluses[i]<minSurplus)&&(surpluses[i]>0)){
                            minSurplus=surpluses[i];
                            minIndex=i;
                        }
                    }

                    if(minSurplus>=callMinute){//最小的那个套餐余量够减
                        //对id为surplusIds.get(minIndex)的基准服务余量减去
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id="+surplusIds.get(minIndex);
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, surpluses[minIndex]-callMinute);
                            surpluses[minIndex]=surpluses[minIndex]-callMinute;
                            pstm.executeUpdate();

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        callMinute=0;
                    }
                    else{
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id=?";
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, 0);
                            pstm.setString(2, surplusIds.get(minIndex));
                            pstm.executeUpdate();

                            callMinute=callMinute-surpluses[minIndex];

                            surpluses[minIndex]= Float.valueOf(0);

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //在基准资费上面增加通话费用
                try {
                    //向数据库插入信息
                    Connection connection=null;
                    PreparedStatement pstm=null;

                    Class.forName("com.mysql.jdbc.Driver");
                    connection= DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                            "root","Dww112358"
                    );

                    //执行查询
                    String sql="insert into user_meal_outside_charge(user_id, base_service_id, consume, date) values (?, ?, ?, ?)  ";
                    pstm=connection.prepareStatement(sql);

                    //保存结果
                    pstm.setInt(1, userId);
                    pstm.setInt(2, 1);
                    pstm.setFloat(3, exit);
                    String dateStr=time.format(new java.util.Date());
                    //插入datetime类型的mysql数据的方法之一
                    pstm.setString(4, dateStr);
                    pstm.executeUpdate();

                    //完成后关闭
                    pstm.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }  catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        String endTime2=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对通话操作记录结束的时间：  "+endTime2);

    }

    /**
     * 用户在短信情况下的资费生成；
     * sendMessage(int userId, String date)
     * ps: write table "user_meal_surplus", "user_meal_outside_charge",
     *     read table "user_meal_surplus"
     * */
    public static void sendMessage(int userId, String date){
        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对短信操作记录开始的时间：  "+startTime);


        /**
         * 读取该用户的套餐中的短信的基准服务有哪些剩余的
         * */
        ArrayList<String> surplusIds=new ArrayList<>();//user_meal_surplus的id号
        ArrayList<Float> surpluses=new ArrayList<>();//剩余的短信的数量

        try {
            //从数据库查询信息
            Connection connection=null;
            Statement stmt = null;

            Class.forName("com.mysql.jdbc.Driver");
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            stmt = connection.createStatement();
            String sql="select * from user_meal_surplus where user_id="+userId
                    +" and base_service_id=2 and surplus>0";
            ResultSet rs = stmt.executeQuery(sql);

            //展开结果数据集
            while(rs.next()){
                String surplus_id=rs.getString("id");
                float surplus=rs.getFloat("surplus");

                surplusIds.add(surplus_id);
                surpluses.add(surplus);
            }

            //完成后关闭
            rs.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //套餐中有短信的余量
        if(surpluses.size()!=0){
            //从余量最少的那个开始用
            float minSurplus=surpluses.get(0);
            int minIndex=0;
            for(int i=1;i<surpluses.size();i++){
                if (surpluses.get(i)<minSurplus){
                    minSurplus=surpluses.get(i);
                    minIndex=i;
                }
            }

            //对id为surplusIds.get(minIndex)的基准服务余量减一
            try {
                //向数据库插入信息
                Connection connection=null;
                PreparedStatement pstm=null;

                Class.forName("com.mysql.jdbc.Driver");
                connection= DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                        "root","Dww112358"
                );

                //执行查询
                String sql="update user_meal_surplus set surplus=? " +
                        "where id="+surplusIds.get(minIndex);
                pstm=connection.prepareStatement(sql);

                //保存结果
                pstm.setFloat(1, surpluses.get(minIndex)-1);
                pstm.executeUpdate();

                //完成后关闭
                pstm.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                //向数据库插入信息
                Connection connection=null;
                PreparedStatement pstm=null;

                Class.forName("com.mysql.jdbc.Driver");
                connection= DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                        "root","Dww112358"
                );

                //执行查询
                String sql="insert into user_meal_outside_charge(user_id, base_service_id, consume, date) values (?, ?, ?, ?)  ";
                pstm=connection.prepareStatement(sql);

                //保存结果
                pstm.setInt(1, userId);
                pstm.setInt(2, 2);
                pstm.setInt(3, 1);
                String dateStr=time.format(new java.util.Date());
                //插入datetime类型的mysql数据的方法之一
                pstm.setString(4, dateStr);
                pstm.executeUpdate();

                //完成后关闭
                pstm.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        String endTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对短信操作记录结束的时间：  "+endTime);

    }

    /**
     * 用户在使用流量下的资费生成;
     * useFlow(int userId, String date, int num, int isLocal)
     * */
    public static void useFlow(int userId, String date, float num, int isLocal){
        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对使用流量操作记录开始的时间：  "+startTime);

        if(isLocal==1){//在本地
            /**
             * 读取该用户的套餐中的本地流量的基准服务有哪些剩余的
             * */
            ArrayList<String> surplusIds1=new ArrayList<>();//user_meal_surplus的id号
            Float[] surpluses1=new Float[200];//剩余的本地流量的数量
            ArrayList<String> surplusIds2=new ArrayList<>();//user_meal_surplus的id号
            Float[] surpluses2=new Float[200];//剩余的全国流量的数量

            try {
                //从数据库查询信息
                Connection connection=null;
                Statement stmt = null;

                Class.forName("com.mysql.jdbc.Driver");
                connection= DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                        "root","Dww112358"
                );

                //执行查询---本地流量
                stmt = connection.createStatement();
                String sql="select * from user_meal_surplus where user_id="+userId
                        +" and base_service_id=3 and surplus>0";
                ResultSet rs = stmt.executeQuery(sql);

                //展开结果数据集
                int index=0;
                while(rs.next()){
                    String surplus_id=rs.getString("id");
                    float surplus=rs.getFloat("surplus");

                    surplusIds1.add(surplus_id);
                    surpluses1[index]=surplus;
                    index++;
                }

                //执行查询---全国流量
                stmt = connection.createStatement();
                sql="select * from user_meal_surplus where user_id="+userId
                        +" and base_service_id=4 and surplus>0";
                rs = stmt.executeQuery(sql);

                //展开结果数据集
                int index2=0;
                while(rs.next()){
                    String surplus_id=rs.getString("id");
                    float surplus=rs.getFloat("surplus");

                    surplusIds2.add(surplus_id);
                    surpluses2[index2]=surplus;
                    index2++;
                }

                //完成后关闭
                rs.close();
                stmt.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //套餐中有本地流量的余量总量
            float sumNum=0;
            for(int i=0;i<surplusIds1.size();i++)
                sumNum=sumNum+surpluses1[i];
            //套餐中有全国流量的余量总量
            float sumNum2=0;
            for(int i=0;i<surplusIds2.size();i++)
                sumNum2=sumNum2+surpluses2[i];

            //本地流量就足够使用
            if (sumNum>=num){
                //使用完/一部分本地流量
                while(num>0){
                    //从余量最少的那个开始用
                    float minSurplus=surpluses1[0];
                    int minIndex=0;
                    for(int i=0;i<surplusIds1.size();i++){//初始化值为不为0的那对
                        if (surpluses1[i]!=0){
                            minSurplus=surpluses1[i];
                            minIndex=i;
                            break;
                        }
                    }

                    for(int i=0;i<surplusIds1.size();i++){//找出值最小的那对
                        if ((surpluses1[i]<minSurplus)&&(surpluses1[i]>0)){
                            minSurplus=surpluses1[i];
                            minIndex=i;
                        }
                    }

                    if(minSurplus>=num){//最小的那个套餐余量够减
                        //对id为surplusIds.get(minIndex)的基准服务余量减去
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id="+surplusIds1.get(minIndex);
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, surpluses1[minIndex]-num);
                            surpluses1[minIndex]=surpluses1[minIndex]-num;
                            pstm.executeUpdate();

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        num=0;
                    }
                    //最小的套餐不够减，需要多减几次
                    else{
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id=?";
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, 0);
                            pstm.setString(2, surplusIds1.get(minIndex));
                            pstm.executeUpdate();

                            num=num-surpluses1[minIndex];

                            surpluses1[minIndex]= Float.valueOf(0);

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            else{
                float numCopy=num;
                //本地流量+全国流量就足够使用
                float tempSum=sumNum+sumNum2;
                if (tempSum>=num){
                    //使用完本地流量
                    for (int i=0;i<surplusIds1.size();i++){
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id=?";
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, 0);
                            pstm.setString(2, surplusIds1.get(i));
                            pstm.executeUpdate();

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    //使用完/一部分全国流量
                    num=num-sumNum;
                    while(num>0){
                        //从余量最少的那个开始用
                        float minSurplus=surpluses2[0];
                        int minIndex=0;
                        for(int i=0;i<surplusIds2.size();i++){//初始化值为不为0的那对
                            if (surpluses2[i]!=0){
                                minSurplus=surpluses2[i];
                                minIndex=i;
                                break;
                            }
                        }

                        for(int i=0;i<surplusIds2.size();i++){//找出值最小的那对
                            if ((surpluses2[i]<minSurplus)&&(surpluses2[i]>0)){
                                minSurplus=surpluses2[i];
                                minIndex=i;
                            }
                        }

                        if(minSurplus>=num){//最小的那个套餐余量够减
                            //对id为surplusIds.get(minIndex)的套餐基准服务余量减去
                            try {
                                //向数据库插入信息
                                Connection connection=null;
                                PreparedStatement pstm=null;

                                Class.forName("com.mysql.jdbc.Driver");
                                connection= DriverManager.getConnection(
                                        "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                        "root","Dww112358"
                                );

                                //执行查询
                                String sql="update user_meal_surplus set surplus=? " +
                                        "where id="+surplusIds2.get(minIndex);
                                pstm=connection.prepareStatement(sql);

                                //保存结果
                                pstm.setFloat(1, surpluses2[minIndex]-num);
                                surpluses2[minIndex]=surpluses2[minIndex]-num;
                                pstm.executeUpdate();

                                //完成后关闭
                                pstm.close();
                                connection.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }  catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                            num=0;
                        }
                        //最小的套餐不够减，需要多减几次
                        else{
                            try {
                                //向数据库插入信息
                                Connection connection=null;
                                PreparedStatement pstm=null;

                                Class.forName("com.mysql.jdbc.Driver");
                                connection= DriverManager.getConnection(
                                        "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                        "root","Dww112358"
                                );

                                //执行查询
                                String sql="update user_meal_surplus set surplus=? " +
                                        "where id=?";
                                pstm=connection.prepareStatement(sql);

                                //保存结果
                                pstm.setFloat(1, 0);
                                pstm.setString(2, surplusIds2.get(minIndex));
                                pstm.executeUpdate();

                                num=num-surpluses2[minIndex];

                                surpluses2[minIndex]= Float.valueOf(0);

                                //完成后关闭
                                pstm.close();
                                connection.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }  catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                //本地流量+全国流量不够使用，需要增加基准资费
                else{
                    //使用完本地流量
                    for (int i=0;i<surplusIds1.size();i++){
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id=?";
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, 0);
                            pstm.setString(2, surplusIds1.get(i));
                            pstm.executeUpdate();

                            num=num-surpluses1[i];

                            surpluses1[i]= Float.valueOf(0);

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    //使用完全国流量
                    for (int i=0;i<surplusIds2.size();i++){
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id=?";
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, 0);
                            pstm.setString(2, surplusIds2.get(i));
                            pstm.executeUpdate();

                            num=num-surpluses2[i];

                            surpluses2[i]= Float.valueOf(0);

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    //增加基准资费
                    float flowToAdd=numCopy-tempSum;
                    System.out.println(flowToAdd);//////////////////////
                    try {
                        //向数据库插入信息
                        Connection connection=null;
                        PreparedStatement pstm=null;

                        Class.forName("com.mysql.jdbc.Driver");
                        connection= DriverManager.getConnection(
                                "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                "root","Dww112358"
                        );

                        //执行查询
                        String sql="insert into user_meal_outside_charge(user_id, base_service_id, consume, date) values (?, ?, ?, ?)  ";
                        pstm=connection.prepareStatement(sql);

                        //保存结果
                        pstm.setInt(1, userId);
                        pstm.setInt(2, 3);
                        pstm.setFloat(3, flowToAdd);
                        String dateStr=time.format(new java.util.Date());
                        //插入datetime类型的mysql数据的方法之一
                        pstm.setString(4, dateStr);
                        pstm.executeUpdate();

                        //完成后关闭
                        pstm.close();
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }  catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //使用的是全国流量
        else{
            //计算套餐中全国流量的总量
            ArrayList<String> surplusIds2=new ArrayList<>();//user_meal_surplus的id号
            Float[] surpluses2=new Float[200];//剩余的全国流量的数量

            try {
                //从数据库查询信息
                Connection connection=null;
                Statement stmt = null;

                Class.forName("com.mysql.jdbc.Driver");
                connection= DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                        "root","Dww112358"
                );

                //执行查询---全国流量
                stmt = connection.createStatement();
                String sql="select * from user_meal_surplus where user_id="+userId
                        +" and base_service_id=4 and surplus>0";
                ResultSet rs = stmt.executeQuery(sql);

                //展开结果数据集
                int index2=0;
                while(rs.next()){
                    String surplus_id=rs.getString("id");
                    float surplus=rs.getFloat("surplus");

                    surplusIds2.add(surplus_id);
                    surpluses2[index2]=surplus;
                    index2++;
                }

                //完成后关闭
                rs.close();
                stmt.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //套餐中有全过流量的余量总量
            float sumNum2=0;
            for(int i=0;i<surplusIds2.size();i++)
                sumNum2=sumNum2+surpluses2[i];

            //如果套餐足够使用
            if (sumNum2>=num){
                //使用部分/全部全国流量
                while(num>0){
                    //从余量最少的那个开始用
                    float minSurplus=surpluses2[0];
                    int minIndex=0;
                    for(int i=0;i<surplusIds2.size();i++){//初始化值为不为0的那对
                        if (surpluses2[i]!=0){
                            minSurplus=surpluses2[i];
                            minIndex=i;
                            break;
                        }
                    }

                    for(int i=0;i<surplusIds2.size();i++){//找出值最小的那对
                        if ((surpluses2[i]<minSurplus)&&(surpluses2[i]>0)){
                            minSurplus=surpluses2[i];
                            minIndex=i;
                        }
                    }

                    if(minSurplus>=num){//最小的那个套餐余量够减
                        //对id为surplusIds.get(minIndex)的基准服务余量减去
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id="+surplusIds2.get(minIndex);
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, surpluses2[minIndex]-num);
                            surpluses2[minIndex]=surpluses2[minIndex]-num;
                            pstm.executeUpdate();

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        num=0;
                    }
                    //最小的套餐不够减，需要多减几次
                    else{
                        try {
                            //向数据库插入信息
                            Connection connection=null;
                            PreparedStatement pstm=null;

                            Class.forName("com.mysql.jdbc.Driver");
                            connection= DriverManager.getConnection(
                                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                    "root","Dww112358"
                            );

                            //执行查询
                            String sql="update user_meal_surplus set surplus=? " +
                                    "where id=?";
                            pstm=connection.prepareStatement(sql);

                            //保存结果
                            pstm.setFloat(1, 0);
                            pstm.setString(2, surplusIds2.get(minIndex));
                            pstm.executeUpdate();

                            num=num-surpluses2[minIndex];

                            surpluses2[minIndex]= Float.valueOf(0);

                            //完成后关闭
                            pstm.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }  catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            //如果套餐不够使用
            else{
                float numCopy=num;
                //使用完全国流量
                for (int i=0;i<surplusIds2.size();i++){
                    try {
                        //向数据库插入信息
                        Connection connection=null;
                        PreparedStatement pstm=null;

                        Class.forName("com.mysql.jdbc.Driver");
                        connection= DriverManager.getConnection(
                                "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                                "root","Dww112358"
                        );

                        //执行查询
                        String sql="update user_meal_surplus set surplus=? " +
                                "where id=?";
                        pstm=connection.prepareStatement(sql);

                        //保存结果
                        pstm.setFloat(1, 0);
                        pstm.setString(2, surplusIds2.get(i));
                        pstm.executeUpdate();

                        num=num-surpluses2[i];

                        surpluses2[i]= Float.valueOf(0);

                        //完成后关闭
                        pstm.close();
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }  catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                //增加基准资费
                float flowToAdd=numCopy-sumNum2;
                try {
                    //向数据库插入信息
                    Connection connection=null;
                    PreparedStatement pstm=null;

                    Class.forName("com.mysql.jdbc.Driver");
                    connection= DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                            "root","Dww112358"
                    );

                    //执行查询
                    String sql="insert into user_meal_outside_charge(user_id, base_service_id, consume, date) values (?, ?, ?, ?)  ";
                    pstm=connection.prepareStatement(sql);

                    //保存结果
                    pstm.setInt(1, userId);
                    pstm.setInt(2, 4);
                    pstm.setFloat(3, flowToAdd);
                    String dateStr=time.format(new java.util.Date());
                    //插入datetime类型的mysql数据的方法之一
                    pstm.setString(4, dateStr);
                    pstm.executeUpdate();

                    //完成后关闭
                    pstm.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }  catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        String endTime=time.format(new java.util.Date());
        System.out.println("用户"+userId+"对使用流量操作记录结束的时间：  "+endTime);

    }

    /**
     * 某个用户月账单的生成;
     * generateBillByMonth(int userId, String month)
     * ps: read table "user_meal_outside_charge", "base_service"---套餐外费用
     *                "user_meal_operator", "meal_service"(name, expense),---订购套餐的费用
     *                "user_meal_operator", "meal_service"(name, expense),---立即取消套餐后的返回费用
     *                 ---计算：总费用
     *     尽可能多地增加筛选条件，然后，全部读出来，再写，这样更简单
     *     month的参数需要是"yyyy-mm"的形式
     * */
    public static void generateBillByMonth(int userId, String month){
        //操作开始的时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime=time.format(new java.util.Date());
        System.out.println("对"+userId+"进行进行"+month+"月账单的生成开始的时间：  "+startTime);

        //设置阅读表格的返回值---每张表格的阅读数据的返回值都是一一对应的
        ArrayList<Integer> outsideBaseServiceIds=new ArrayList<>();
        ArrayList<Float> outConsumes=new ArrayList<>();
        ArrayList<String> outsideDate=new ArrayList<>();

        ArrayList<Integer> baseServiceIds=new ArrayList<>();
        ArrayList<String> baseServiceNames=new ArrayList<>();
        ArrayList<Float> baseServiceExpenses=new ArrayList<>();

        ArrayList<Integer> operateIds=new ArrayList<>();
        ArrayList<Integer> operateMealIds=new ArrayList<>();
        ArrayList<String> operateMealTime=new ArrayList<>();

        ArrayList<Integer> mealIds=new ArrayList<>();
        ArrayList<String> mealNames=new ArrayList<>();
        ArrayList<Float> expenses=new ArrayList<>();
        try {
            //从数据库获取历史信息
            Connection connection=null;
            Statement stmt=null;
            //注册jdbc驱动
            Class.forName("com.mysql.jdbc.Driver");

            //打开连接
            connection= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mobile_operator_service?autoReconnect=true&useSSL=false",
                    "root","Dww112358"
            );

            //执行查询
            stmt=connection.createStatement();

            //阅读table "user_meal_outside_charge"
            String sql="select * from user_meal_outside_charge where user_id="+userId;
            ResultSet rs=stmt.executeQuery(sql);

            //展开结果集数据库，保存返回的结果
            while(rs.next()){
                //通过字段检索
                int base_service_id=rs.getInt("base_service_id");
                float consume=rs.getInt("consume");
                String date=rs.getString("date");

                outsideBaseServiceIds.add(base_service_id);
                outConsumes.add(consume);
                outsideDate.add(date);
            }

            //阅读table "base_service"
            sql="select * from base_service";
            rs=stmt.executeQuery(sql);

            //展开结果集数据库，保存返回的结果
            while(rs.next()){
                //通过字段检索
                int id=rs.getInt("id");
                String name=rs.getString("name");
                float expense=rs.getFloat("expense");

                baseServiceIds.add(id);
                baseServiceNames.add(name);
                baseServiceExpenses.add(expense);
            }

            //阅读table "user_meal_operator"
            sql="select * from user_meal_operator where user_id="+userId;
            rs=stmt.executeQuery(sql);

            //展开结果集数据库，保存返回的结果
            while(rs.next()){
                //通过字段检索
                int operate_id=rs.getInt("operate_id");
                int meal_id=rs.getInt("meal_id");
                String operate_time=rs.getString("operate_time");

                operateIds.add(operate_id);
                operateMealIds.add(meal_id);
                operateMealTime.add(operate_time);
            }


            //阅读table "meal_service"
            sql="select * from meal_service";
            rs=stmt.executeQuery(sql);

            //展开结果集数据库，保存返回的结果
            while(rs.next()){
                //通过字段检索
                int id=rs.getInt("id");
                String name=rs.getString("name");
                float expense=rs.getFloat("expense");

                mealIds.add(id);
                mealNames.add(name);
                expenses.add(expense);
            }

            //完成后关闭
            rs.close();
            stmt.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /**
         * 控制台输出用户的月账单
         * 内容如下：
         * （0）：打印：用户 userId 在 month 月的月账单：
         * （1）：用户在套餐外的基准资费的使用情况以及费用（各项费用+总费）；
         * （2）：用户订购套餐费用（各项费用+总费）；
         * （3）：用户立即退订套餐费用（各项费用+总费）；
         * （4）：用户下月退订套餐费用（各项费用+总费）；
         * （5）：用户本月使用总费用；
         * */

        /**
         * （0）：用户 userId 在 month 月的月账单；
         * */
        System.out.println("用户 "+userId+" 在 "+month+" 月的月账单");

        /*
         * （1）：用户在套餐外的基准资费的使用情况以及费用（各项费用+总费）；
         *        涉及ArrayList：
         *                      "outsideBaseServiceIds", "outConsumes",
         *                      "outsideDate", "baseServiceIds", "baseServiceNames",
         *                      "baseServiceExpenses"
         *         步骤：
         *             float sumOutsideMoney=0;//存储额外基准服务的花费
         *             符合"outsideDate"的index个的月份"month"开头；
         *             相应index的"outsideBaseServiceIds"找到"baseServiceIds"中相等的那一项（j）, "baseServiceNames", "baseServiceExpenses"获得行为的名称和单价；
         *             相应index的outConsumes"乘以单价就得出单项花费的价格，将单项价格加到sumOutsideMoney上面；
         *         输出格式：
         *             2018-10-10 使用 通话 基准服务 30 分钟，花费 15 元；
         *
         * */
        System.out.println("（1）：基准服务：");
        float sumOutsideMoney=0;
        for (int i=0;i<outsideDate.size();i++){
            if (outsideDate.get(i).startsWith(month)){
                int outsideBaseServiceId=outsideBaseServiceIds.get(i);
                for (int j=0;j<baseServiceIds.size();j++){
                    if (baseServiceIds.get(j)==outsideBaseServiceId){
                        sumOutsideMoney=sumOutsideMoney+outConsumes.get(i)*baseServiceExpenses.get(j);
                        System.out.println(outsideDate.get(i)+" 使用 "+baseServiceNames.get(j)+" 基准服务数量 "+outConsumes.get(i)+"，花费 "+
                                outConsumes.get(i)*baseServiceExpenses.get(j)+" 元；");
                        break;
                    }
                }
            }
        }
        if (sumOutsideMoney==0){
            System.out.println(month+" 未使用任何基准服务；");
        }
        /*
         * （2）：用户订购套餐费用（各项费用+总费）；
         *        涉及ArrayList：
         *                      "operateMealTime", "operateMealIds", "operateIds",
         *                      "mealIds", "mealNames", "expenses"
         *         步骤：
         *             float sumMealOrderedMoney=0;//存储额外基准服务的花费
         *             根据"operateMealTime"获得符合month的月份，且"operateIds"的值为1（即：订购）的index，
         *             根据index获得"operateMealIds"，然后根据"operateMealIds"和"operateIds"的匹配获得行为"operateNames"，
         *             以及"expenses"的套餐费用，将费用加到sumMealOrderedMoney上面；
         *         输出格式：
         *             2018-10-10 订购套餐 话费套餐 1份，花费 15 元；
         * */
        System.out.println("（2）：订购套餐：");
        float sumMealOrderedMoney=0;
        for (int i=0;i<operateMealTime.size();i++){
            if (operateMealTime.get(i).startsWith(month)&&(operateIds.get(i)==1)){//符合月份+符合"operateIds"的值为1
                int operateMealId=operateMealIds.get(i);
                for (int j=0;j<mealIds.size();j++){
                    if (mealIds.get(j)==operateMealId){
                        sumMealOrderedMoney=sumMealOrderedMoney+expenses.get(j);
                        System.out.println(operateMealTime.get(i)+" 订购套餐 "+mealNames.get(j)+" 1份，花费 "
                                +expenses.get(j)+" 元； ");
                        break;
                    }
                }
            }
        }
        if (sumMealOrderedMoney==0){
            System.out.println(month+" 未订购任何套餐服务；");
        }

        /*
         * （3）：用户立即退订套餐费用（各项费用+总费）；
         *       涉及ArrayList：
         *                      "operateMealTime", "operateMealIds", "operateIds",
         *                      "mealIds", "mealNames", "expenses"
         *         步骤：
         *             float sumMealUnsubscribeMoney=0;//存储额外基准服务的花费
         *             根据"operateMealTime"获得符合month的月份，且"operateIds"的值为2（即：立即退订）的index，
         *             根据index获得"operateMealIds"，然后根据"operateMealIds"和"operateIds"的匹配获得行为"operateNames"，
         *             以及"expenses"的套餐费用，将费用加到sumMealOrderedMoney上面；
         *         输出格式：
         *             2018-10-10 立即退订套餐 话费套餐 1份，返回 15 元；
         * */
        System.out.println("（3）：立即退订套餐：");
        float sumMealUnsubscribeMoney=0;
        for (int i=0;i<operateMealTime.size();i++){
            if (operateMealTime.get(i).startsWith(month)&&(operateIds.get(i)==2)){//符合月份+符合"operateIds"的值为2
                int operateMealId=operateMealIds.get(i);
                for (int j=0;j<mealIds.size();j++){
                    if (mealIds.get(j)==operateMealId){
                        sumMealUnsubscribeMoney=sumMealUnsubscribeMoney+expenses.get(j);
                        System.out.println(operateMealTime.get(i)+" 立即退订套餐 "+mealNames.get(j)+" 1份，返回 "
                                +expenses.get(j)+" 元； ");
                        break;
                    }
                }
            }
        }
        if (sumMealUnsubscribeMoney==0){
            System.out.println(month+" 未立即退订任何套餐服务；");
        }

        /*
         * （4）：用户下月退订套餐费用（各项费用+总费）；
         *       涉及ArrayList：
         *                      "operateMealTime", "operateMealIds", "operateIds",
         *                      "mealIds", "mealNames", "expenses"
         *         步骤：
         *             根据"operateMealTime"获得符合month的月份，且"operateIds"的值为3（即：下月退订）的index，
         *             根据index获得"operateMealIds"，然后根据"operateMealIds"和"operateIds"的匹配获得行为"operateNames"；
         *         输出格式：
         *             2018-10-10 下月退订套餐 话费套餐 1份，返回 0 元；
         * */
        System.out.println("（4）：下月退订套餐：");
        int unsubscribeOrdersNextMonth=0;
        for (int i=0;i<operateMealTime.size();i++){
            if (operateMealTime.get(i).startsWith(month)&&(operateIds.get(i)==3)){//符合月份+符合"operateIds"的值为2
                int operateMealId=operateMealIds.get(i);
                for (int j=0;j<mealIds.size();j++){
                    if (mealIds.get(j)==operateMealId){
                        System.out.println(operateMealTime.get(i)+" 下月退订套餐 "+mealNames.get(j)+" 1份，返回 0 元； ");
                        unsubscribeOrdersNextMonth=1;
                        break;
                    }
                }
            }
        }
        if (unsubscribeOrdersNextMonth==0){
            System.out.println(month+" 未下月退订任何套餐服务；");
        }

        /*
         * （5）：打印用户本月使用总费用；
         * */
        float sumMoney=sumOutsideMoney+sumMealOrderedMoney-sumMealUnsubscribeMoney;
        System.out.println("（5）：月总花费：");
        System.out.println(month+" 月总花费： "+sumMoney+" 元；");


        //操作结束的时间
        String endTime=time.format(new java.util.Date());
        System.out.println("对"+userId+"进行进行"+month+"月账单的生成结束的时间：  "+endTime);
    }
}
