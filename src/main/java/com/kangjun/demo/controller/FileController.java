package com.kangjun.demo.controller;

import java.io.*;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@PropertySource({"classpath:application.properties"})
@RequestMapping("/filehandler")
public class FileController {

	@Value("${web.file.base.path}")
	private String basePath;

	//单文件上传
	@RequestMapping("/upload_file")
	@ResponseBody
	public String uploadFile(@RequestParam("image_file") MultipartFile file) {
		System.out.println("文件存储基本路径: " + basePath);
		String fileName = file.getOriginalFilename();
		System.out.println("上传文件名称: " + fileName);
		String suffixName = fileName.substring(fileName.lastIndexOf("."));
		System.out.println("上传文件后缀: " + suffixName);
		String newFileName = UUID.randomUUID() + suffixName;
		System.out.println("文件存储名称: " + newFileName);
		File newFile = new File(basePath + newFileName); 
		
		try {
			file.transferTo(newFile);
			return "上传成功!";
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "上传失败!";
	}

	//多文件上传
	@RequestMapping(value = "/uploadMore", method = RequestMethod.POST)
	@ResponseBody
	public String handleFileUpload(HttpServletRequest request) {
		List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("image_file");
		MultipartFile file = null;
		BufferedOutputStream stream = null;
		for (int i = 0; i < files.size(); ++i) {
			file = files.get(i);
			String filePath = basePath;
			if (!file.isEmpty()) {
				try {
					byte[] bytes = file.getBytes();
					stream = new BufferedOutputStream(new FileOutputStream(
							new File(filePath + file.getOriginalFilename())));//设置文件路径及名字
					stream.write(bytes);// 写入
					stream.close();
				} catch (Exception e) {
					stream = null;
					return "第 " + i + " 个文件上传失败  ==> "
							+ e.getMessage();
				}
			} else {
				return "第 " + i
						+ " 个文件上传失败因为文件为空";
			}
		}
		return "上传成功";
	}

	//文件下载
	@RequestMapping("/download")
	public String downloadFile(HttpServletRequest request, HttpServletResponse response) {
		String fileName = "配电5.0数据源.xls";// 设置文件名，根据业务需要替换成要下载的文件名
		if (fileName != null) {
			//设置文件路径
			String realPath = basePath;
			File file = new File(realPath , fileName);
			if (file.exists()) {
				response.setContentType("application/force-download");// 设置强制下载不打开
				response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);// 设置文件名
				byte[] buffer = new byte[1024];
				FileInputStream fis = null;
				BufferedInputStream bis = null;
				try {
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					OutputStream os = response.getOutputStream();
					int i = bis.read(buffer);
					while (i != -1) {
						os.write(buffer, 0, i);
						i = bis.read(buffer);
					}
					System.out.println("success");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (bis != null) {
						try {
							bis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}
	
}
