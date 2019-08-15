package com.ljb;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.ljb.dao.RoleDao;
import com.ljb.dao.UserDao;
import com.ljb.dao.UserMapper;
import com.ljb.pojo.entity.RoleInfo;
import com.ljb.pojo.entity.UserInfo;
import com.ljb.randm.VerifyCodeUtils;
import com.ljb.utils.HttpUtils;
import com.ljb.utils.UID;
import net.coobird.thumbnailator.Thumbnails;

import org.apache.http.HttpResponse;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

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

    @Test
    public void test04() throws FileNotFoundException {
        File file = new File("E:\\img\\(22).jpg");

        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(

                new FileInputStream(file), file.length(), "jpg", null);

        System.out.println(storePath.getFullPath());

        String path = thumbImageConfig.getThumbImagePath(storePath.getPath());

        System.out.println(path.substring(0,path.lastIndexOf("_")));

    }

    @Test
    public void test05(){

        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "59f2cc29ccd9454a87bf327bc1438a37";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "18334657453");
        querys.put("param", "code:55634");
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成验证码
     */
    @Test
    public void test06(){

//        Date date = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        String format = sdf.format(date);
//        System.out.println(format);

        List<String> list = new ArrayList<>();
        for (int i = -6;i<=0;i++){
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
            calendar1.add(Calendar.DATE, i);
            String three_days_ago = sdf1.format(calendar1.getTime());
            //System.out.println(three_days_ago);
            //redisTemplate.opsForValue().set(three_days_ago,"5",7, TimeUnit.DAYS);
            String s = redisTemplate.opsForValue().get(three_days_ago);
            list.add(s);

        }
        System.out.println(list);
        //redisTemplate.opsForValue().increment("20190811",1);


    }

}
