package com.ljb;

import com.ljb.dao.RoleDao;
import com.ljb.dao.UserDao;
import com.ljb.dao.UserMapper;
import com.ljb.pojo.entity.RoleInfo;
import com.ljb.pojo.entity.UserInfo;
import com.ljb.utils.UID;
import net.coobird.thumbnailator.Thumbnails;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 刘进波
 * @create 2019-08-10 7:39
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDemo1 {

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired
    private RoleDao roleDao;

    private HttpServletResponse response;

    /**
     * 这是缩略图
     * @throws IOException
     */
    @Test
    public void demo1() throws IOException {

        //文件路径
        String filePath="E:\\img\\1_0fba1c54d4b2ce5fd9bec5534159177a_0.jpg";

        File file = new File( filePath);

        //可自定义大小
        Thumbnails.of(filePath).scale(0.25f).toFile(file.getAbsolutePath()+"_25%.jpg");

    }

    /**
     * excel导入数据到数据库
     * @throws IOException
     */
    @Test
    public void demo2() throws IOException  {

        File file = new File("E:\\test.xlsx");

        FileInputStream fileInputStream = new FileInputStream(file);

        ArrayList<RoleInfo> roleInfos = new ArrayList<>();

        //打开HSSFWorkbook对象

        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

        XSSFSheet sheet = workbook.getSheetAt(0);

        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows(); //获取表单所有行

        //向数据库导入数据
        for (int i = 1; i < physicalNumberOfRows; i++){

            XSSFRow row = sheet.getRow(i);
            RoleInfo roleInfo = new RoleInfo();

            XSSFCell c0 = row.getCell(0);
            long id = new Double(c0.getNumericCellValue()).longValue();
            roleInfo.setId(id);

            XSSFCell c1 = row.getCell(1);
            roleInfo.setRoleName(c1.getStringCellValue());

            XSSFCell c3 = row.getCell(2);
            roleInfo.setMiaoShu(c3.getStringCellValue());

            roleInfos.add(roleInfo);
        }
        roleInfos.forEach(roleInfo -> {
            roleDao.saveAndFlush(roleInfo);
        });
    }

    /**
     * 数据库导入到Excel   这里的数据是我自己写的   应用中到数据库查询即可
     * @throws IOException
     */
    @Test
    public void test1() throws IOException{

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        XSSFWorkbook workbook = new XSSFWorkbook();

        List<RoleInfo> list = new ArrayList<>();
        for (int i = 0;i<3;i++){
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setId(UID.next());
            roleInfo.setRoleName("口香糖"+i);
            roleInfo.setMiaoShu("我不想吃口香糖"+i);
            list.add(roleInfo);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String fileName = "角色信息表"+simpleDateFormat.format(new Date())+".xlsx";
        String sheetName = "角色信息表";
        String[] titile ={"id","角色名称","角色描述"};

        //在文档中添加表单
        XSSFSheet sheet = workbook.createSheet(sheetName);

        //创建单元格格式，并设置居中
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        //创建第一行用于填充标题
        XSSFRow titleRow = sheet.createRow(0);

        for (int i=0 ; i<titile.length ; i++) {
            //创建单元格
            XSSFCell cell = titleRow.createCell(i);
            //设置单元格内容
            cell.setCellValue(titile[i]);
            //设置单元格样式
            cell.setCellStyle(style);
        }

        System.out.println(list);
        Row row = null;
        for(int i = 0; i < list.size();i++){
            //创建list.siza()行数据
           row = sheet.createRow(i + 1);
           //把值写进单元格
           row.createCell(0).setCellValue(list.get(i).getId());
           row.createCell(1).setCellValue(list.get(i).getRoleName());
           row.createCell(2).setCellValue(list.get(i).getMiaoShu());

        }

        //获取配置文件中保存对应excel文件的路径  文件目录
        File file = new File("E:\\biao");
        if(!file.exists()){
            file.mkdirs();
        }

        String savePath = "E:\\biao\\"+fileName;
        FileOutputStream fileOut = new FileOutputStream(savePath);

        workbook.write(fileOut);
        fileOut.close();
    }



}
