package com.tianyoukeji.org.controller;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.service.FileUploadService;

@Controller
public class FileUploadController extends DefaultHandler{
	
	@Autowired
	FileUploadService fileUploadService;
	
	@Autowired
	private GridFsTemplate gridFsTemplate;
	
	@Resource
	private GridFSBucket gridFSBucket;
	
	
	@RequestMapping(path="/v1/upload",method=RequestMethod.POST)
	@ResponseBody
	public Object upload(@RequestParam("file") MultipartFile file,Principal principal) {
		 if (file != null) {   // 表示现在已经有文件上传了
			 return fileUploadService.upload(file,principal);
	      }
		return null;
	}
	
	@RequestMapping(path="/v1/file/{id}",method=RequestMethod.GET)
	public void download(@PathVariable("id") String id,HttpServletRequest request, HttpServletResponse response) {
		
		Query query = Query.query(Criteria.where("_id").is(id));
        // 查询单个文件
		com.mongodb.client.gridfs.model.GridFSFile gfsfile = gridFsTemplate.findOne(query);
        if (gfsfile == null) {
            throw new BusinessException(1863,"文件不存在");
        }
        String fileName = gfsfile.getFilename().replace(",", "");
        //处理中文文件名乱码
        if (request.getHeader("User-Agent").toUpperCase().contains("MSIE") ||
                request.getHeader("User-Agent").toUpperCase().contains("TRIDENT")
                || request.getHeader("User-Agent").toUpperCase().contains("EDGE")) {
            try {
				fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            //非IE浏览器的处理：
            try {
				fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        // 通知浏览器进行文件下载
        response.setContentType(gfsfile.getMetadata().getString("_contentType"));
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        
        GridFSDownloadStream in = gridFSBucket.openDownloadStream(gfsfile.getObjectId());

        GridFsResource resource = new GridFsResource(gfsfile,in);
        try {
			IOUtils.copy(resource.getInputStream(), response.getOutputStream());
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}