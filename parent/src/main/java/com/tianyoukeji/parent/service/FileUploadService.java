package com.tianyoukeji.parent.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tianyoukeji.parent.common.FileType;

import net.coobird.thumbnailator.Thumbnails;

public class FileUploadService {
	@Autowired
	private GridFsTemplate gridFsTemplate;
	@Autowired
	ThumbnailProperities thumbnailProperities;

	public FileInfo upload(MultipartFile file, Principal principal) {
		String name = file.getOriginalFilename();
		long size = file.getSize();
		String format = FileType.getFileType(file);

		String suffix = "";
		if (file.getOriginalFilename().indexOf(".") != -1) {
			suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
		}

		String id = mongodbSave(file, principal);
		FileInfo fileInfo = new FileInfo();
		fileInfo.setName(name);
		fileInfo.setSize(size);
		fileInfo.setId(id);

		if (format != null) {
			if (format.equals("docx")) {
				if (suffix.equalsIgnoreCase("xlsx")) {
					format = "xlsx";
				} else if (suffix.equalsIgnoreCase("pptx")) {
					format = "pptx";
				}
			} else if (format.equals("doc")) {
				if (suffix.equalsIgnoreCase("xls")) {
					format = "xls";
				} else if (suffix.equalsIgnoreCase("ppt")) {
					format = "ppt";
				} else if (suffix.equalsIgnoreCase("wps")) {
					format = "wps";
				}
			}
			fileInfo.setFormat(format);

			if (format.equals("jpg") || format.equals("png") || format.equals("tif") || format.equals("gif")
					|| format.equals("bmp")) {
				ImageInfo imageInfo = new ImageInfo();
				imageInfo.setFormat(format);
				imageInfo.setName(name);
				imageInfo.setSize(size);
				imageInfo.setId(id);

				thumbnailSave(file,imageInfo);

				return imageInfo;
			}
		}
		return fileInfo;

	}

	// ---------------------------------------------------------------------------------------------------------

	private void thumbnailSave(MultipartFile file,ImageInfo imageInfo) {
		InputStream ins = null;
		try {
			ins = file.getInputStream();
			
			BufferedImage read = ImageIO.read(ins);
			int width = read.getWidth();
			int height = read.getHeight();
			imageInfo.setHeight(height);
			imageInfo.setWidth(width);
			ins.close();
			
			if(thumbnailProperities.getLarge()!=null && (width>thumbnailProperities.getLarge().getWidth()||height>thumbnailProperities.getLarge().getHeight())) {
				Thumbnail large = transform(file,thumbnailProperities.getLarge().getWidth(),thumbnailProperities.getLarge().getHeight());
				imageInfo.setLarge(large);
			}
			if(thumbnailProperities.getMiddle()!=null && (width>thumbnailProperities.getMiddle().getWidth()||height>thumbnailProperities.getMiddle().getHeight())) {
				Thumbnail middle = transform(file,thumbnailProperities.getMiddle().getWidth(),thumbnailProperities.getMiddle().getHeight());
				imageInfo.setMiddle(middle);
			}
			if(thumbnailProperities.getSmall()!=null && (width>thumbnailProperities.getSmall().getWidth()||height>thumbnailProperities.getSmall().getHeight())) {
				Thumbnail small = transform(file,thumbnailProperities.getSmall().getWidth(),thumbnailProperities.getSmall().getHeight());
				imageInfo.setSmall(small);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Thumbnail transform(MultipartFile file, int width, int height) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream inputStream=null;
		try {
			Thumbnail thumbnail = new Thumbnail();
			thumbnail.setFormat("jpg");
			inputStream = file.getInputStream();
			Thumbnails.of(inputStream).size(width, height).toOutputStream(baos);
			byte[] byteArray = baos.toByteArray();
			int size = byteArray.length;
			thumbnail.setSize(size);
			BufferedImage read = ImageIO.read(new ByteArrayInputStream(byteArray));
			int w = read.getWidth();
			int h = read.getHeight();
			thumbnail.setHeight(h);
			thumbnail.setWidth(w);
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
			ObjectId objectId = gridFsTemplate.store(byteArrayInputStream , file.getOriginalFilename(),"image/jpeg");
			String id = objectId.toString();
			thumbnail.setId(id);
			return thumbnail;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				inputStream.close();
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private String mongodbSave(MultipartFile file, Principal principal) {
		InputStream ins = null;
		try {
			ins = file.getInputStream();
			// 获得文件类型
			String contentType = file.getContentType();
			// 将文件存储到mongodb中,mongodb 将会返回这个文件的具体信息
			ObjectId objectId = gridFsTemplate.store(ins, file.getOriginalFilename(), contentType);
			ins.close();
			return objectId.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static class ImageInfo extends FileInfo {
		private Thumbnail small;
		private Thumbnail middle;
		private Thumbnail large;
		
		private int width;
		private int height;

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public Thumbnail getSmall() {
			return small;
		}

		public void setSmall(Thumbnail small) {
			this.small = small;
		}

		public Thumbnail getMiddle() {
			return middle;
		}

		public void setMiddle(Thumbnail middle) {
			this.middle = middle;
		}

		public Thumbnail getLarge() {
			return large;
		}

		public void setLarge(Thumbnail large) {
			this.large = large;
		}

	}

	public static class FileInfo {
		private String name;
		private long size;
		private String format;
		private String id;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}



	}

	public static class Thumbnail {

		private String format;
		private long size;
		private Integer width;
		private Integer height;
		private String id;

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public Integer getWidth() {
			return width;
		}

		public void setWidth(Integer width) {
			this.width = width;
		}

		public Integer getHeight() {
			return height;
		}

		public void setHeight(Integer height) {
			this.height = height;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	

	}
}