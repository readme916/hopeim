package com.tianyoukeji.parent.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.Region;

public class RegionService extends BaseService<Region> {

	/**
	 * 地图数据初始化
	 * 
	 * @throws IOException
	 */
	@PostConstruct
	protected void init() {
		try {
			if (this.count() == 0) {
				ClassPathResource classPathResource = new ClassPathResource("region.json");
				File file = classPathResource.getFile();
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				StringBuilder content = new StringBuilder();
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					content.append(line);
				}
				String json = content.toString();
				Map<String, Object> stringToMap = ContextUtils.stringToMap(json);
				List regions = (List) stringToMap.get("result");
				List provinceList = (List) regions.get(0);
				List cityList = (List) regions.get(1);
				List districtList = (List) regions.get(2);

				Region china = new Region();
				china.setFullname("中华人民共和国");
				china.setName("中国");
				china.setLatitude(39.90469);
				china.setLongitude(116.40717);
				china.setTxCid(1000);
				save(china);

				for (Object p : provinceList) {
					Map provinceMap = (Map) p;
					Region provinceRegion = new Region();
					provinceRegion.setFullname(provinceMap.get("fullname").toString());
					provinceRegion.setName(provinceMap.get("name").toString());
					provinceRegion.setParent(china);
					provinceRegion.setTxCid(Integer.valueOf(provinceMap.get("id").toString()));
					provinceRegion
							.setLatitude(Double.valueOf(((Map) provinceMap.get("location")).get("lat").toString()));
					provinceRegion
							.setLongitude(Double.valueOf(((Map) provinceMap.get("location")).get("lng").toString()));
					save(provinceRegion);
					for (int i = Integer.valueOf(((List) provinceMap.get("cidx")).get(0).toString()); i <= Integer
							.valueOf(((List) provinceMap.get("cidx")).get(1).toString()); i++) {
						Map cityMap = (Map) cityList.get(i);
						Region cityRegion = new Region();
						cityRegion.setFullname(cityMap.get("fullname").toString());
						cityRegion.setName(cityMap.get("name").toString());
						cityRegion.setParent(provinceRegion);
						cityRegion.setTxCid(Integer.valueOf(cityMap.get("id").toString()));
						cityRegion.setLatitude(Double.valueOf(((Map) cityMap.get("location")).get("lat").toString()));
						cityRegion.setLongitude(Double.valueOf(((Map) cityMap.get("location")).get("lng").toString()));
						save(cityRegion);
						if (cityMap.containsKey("cidx")) {
							for (int j = Integer.valueOf(((List) cityMap.get("cidx")).get(0).toString()); j <= Integer
									.valueOf(((List) cityMap.get("cidx")).get(1).toString()); j++) {
								Map districtMap = (Map) districtList.get(j);
								Region districtRegion = new Region();
								districtRegion.setFullname(districtMap.get("fullname").toString());
								districtRegion.setParent(cityRegion);
								districtRegion.setTxCid(Integer.valueOf(districtMap.get("id").toString()));
								districtRegion.setLatitude(
										Double.valueOf(((Map) districtMap.get("location")).get("lat").toString()));
								districtRegion.setLongitude(
										Double.valueOf(((Map) districtMap.get("location")).get("lng").toString()));
								save(districtRegion);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
