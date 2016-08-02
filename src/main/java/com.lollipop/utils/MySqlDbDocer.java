package com.lollipop.utils;

import java.awt.Color;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;

/**
 * 数据字典生成器 Mysql
 */
public class MySqlDbDocer {
    //键类型字典
    private static Map<String, String> keyType = new HashMap<>();
    private static Map<String, String> nullType = new HashMap<>();

    //初始化jdbc
    static {
        try {
            keyType.put("PRI", "主键");
            keyType.put("UNI", "唯一键");

            nullType.put("NO", "NO");
            nullType.put("YES", "");
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String schema = "*"; //目标数据库名
    private static String url = "jdbc:mysql://*:3307/" + schema + "?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&allowMultiQueries=true";//链接url
    private static String username = "*"; //用户名
    private static String password = "*"; //密码

    //查询所有表的sql语句
    private static String sql_get_all_tables =
            "select table_name,TABLE_COMMENT " +
                    " from INFORMATION_SCHEMA.tables " +
                    " where TABLE_SCHEMA='" + schema + "' and TABLE_TYPE='BASE TABLE'";
    //查询所有字段的sql语句
    private static String sql_get_all_columns =
            "select column_name,data_type,character_octet_length,COLUMN_key,is_nullable,COLUMN_COMMENT " +
                    " from information_schema.`COLUMNS` " +
                    " where TABLE_NAME='{table_name}' and TABLE_SCHEMA='" + schema + "'";

    public static void main(String[] args) throws Exception {
        //初始化word文档
        Document document = new Document(PageSize.A4);
        RtfWriter2.getInstance(document, new FileOutputStream("target/" + schema + ".doc"));
        document.open();
        //查询开始
        Connection conn = getConnection();
        //获取所有表
        List tables = getDataBySQL(sql_get_all_tables, conn);
        int i = 1;
        for (Object o : tables) {
            String[] str = (String[]) o;
            //循环获取字段信息
            System.out.print(i + ".正在处理数据表-----------" + str[0]);
            addTableMetaData(document, str, i);
            List columns = getDataBySQL(sql_get_all_columns.replace("{table_name}", str[0]), conn);
            addTableDetail(document, columns);
            addBlank(document);
            addBlank(document);
            System.out.println('\u2714');
            i++;
        }
        document.close();
        conn.close();
    }

    /**
     * 添加一个空行
     *
     * @param document
     * @throws Exception
     */
    public static void addBlank(Document document) throws Exception {
        Paragraph ph = new Paragraph("");
        ph.setAlignment(Paragraph.ALIGN_LEFT);
        document.add(ph);
    }

    /**
     * 添加包含字段详细信息的表格
     *
     * @param document
     * @param columns
     * @throws Exception
     */
    public static void addTableDetail(Document document, List columns) throws Exception {
        Table table = new Table(7);
        table.setWidth(100f);//表格 宽度100%
        table.setBorderWidth(1);
        table.setBorderColor(Color.BLACK);
        table.setPadding(0);
        table.setSpacing(0);

        Cell cell1 = new Cell("序号");// 单元格
        cell1.setHeader(true);

        Cell cell2 = new Cell("列名");// 单元格
        cell2.setHeader(true);

        Cell cell3 = new Cell("类型");// 单元格
        cell3.setHeader(true);

        Cell cell4 = new Cell("长度");// 单元格
        cell4.setHeader(true);

        Cell cell5 = new Cell("键");// 单元格
        cell5.setHeader(true);

        Cell cell6 = new Cell("可空");// 单元格
        cell5.setHeader(true);

        Cell cell7 = new Cell("说明");// 单元格
        cell6.setHeader(true);

        //设置表头格式
        table.setWidths(new float[]{5f, 20f, 12f, 10f, 8f, 5f, 40f});
        Color c = new Color(47, 101, 202);

        cell1.setHorizontalAlignment(Cell.ALIGN_CENTER);
        cell1.setBackgroundColor(c);
        cell2.setHorizontalAlignment(Cell.ALIGN_CENTER);
        cell2.setBackgroundColor(c);
        cell3.setHorizontalAlignment(Cell.ALIGN_CENTER);
        cell3.setBackgroundColor(c);
        cell4.setHorizontalAlignment(Cell.ALIGN_CENTER);
        cell4.setBackgroundColor(c);
        cell5.setHorizontalAlignment(Cell.ALIGN_CENTER);
        cell5.setBackgroundColor(c);
        cell6.setHorizontalAlignment(Cell.ALIGN_CENTER);
        cell6.setBackgroundColor(c);
        cell7.setHorizontalAlignment(Cell.ALIGN_CENTER);
        cell7.setBackgroundColor(c);

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);
        table.addCell(cell4);
        table.addCell(cell5);
        table.addCell(cell6);
        table.addCell(cell7);

        table.endHeaders();// 表头结束

        int x = 1;
        for (Object o : columns) {
            String[] str2 = (String[]) o;
            Cell c1 = new Cell(x + "");
            /**
             * column_name,data_type,character_octet_length,is_nullable,COLUMN_COMMENT
             */
            Cell c2 = new Cell(str2[0]);//column_name
            Cell c3 = new Cell(str2[1]);//data_type
            Cell c4 = new Cell(str2[2]);//character_octet_length
            String key = keyType.get(str2[3]);
            if (key == null) key = "";
            Cell c5 = new Cell(key);//COLUMN_key

            String nType = nullType.get(str2[4]);
            if (nType == null) nType = "";
            Cell c6 = new Cell(nType);//is_nullable

            Cell c7 = new Cell(str2[5]);//COLUMN_COMMENT

            c1.setHorizontalAlignment(Cell.ALIGN_CENTER);
            table.addCell(c1);

            c2.setHorizontalAlignment(Cell.ALIGN_LEFT);
            table.addCell(c2);

            c3.setHorizontalAlignment(Cell.ALIGN_LEFT);
            table.addCell(c3);

            c4.setHorizontalAlignment(Cell.ALIGN_LEFT);
            table.addCell(c4);

            c5.setHorizontalAlignment(Cell.ALIGN_LEFT);
            table.addCell(c5);

            c6.setHorizontalAlignment(Cell.ALIGN_LEFT);
            table.addCell(c6);

            c7.setHorizontalAlignment(Cell.ALIGN_LEFT);
            table.addCell(c7);

            x++;
        }
        document.add(table);
    }

    /**
     * 增加表概要信息
     *
     * @param dcument
     * @param arr
     * @param i
     * @throws Exception
     */
    private static void addTableMetaData(Document dcument, String[] arr, int i) throws Exception {
        Paragraph ph = new Paragraph(i + ". 表名: " + arr[0] + '\n' + "说明: " + (arr[1] == null ? "" : arr[1]));
        ph.setAlignment(Paragraph.ALIGN_LEFT);
        dcument.add(ph);
    }

    /**
     * 把SQL语句查询出列表
     *
     * @param sql
     * @param conn
     * @return
     */
    private static List getDataBySQL(String sql, Connection conn) {
        Statement stmt = null;
        ResultSet rs = null;
        List list = new ArrayList();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String[] arr = new String[rs.getMetaData().getColumnCount()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = rs.getString(i + 1);
                }
                list.add(arr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    private static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}