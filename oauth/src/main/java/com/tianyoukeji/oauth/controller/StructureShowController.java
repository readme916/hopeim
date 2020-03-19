package com.tianyoukeji.oauth.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.BaseEnum;
import com.liyang.jpa.smart.query.db.structure.ColumnJoinType;
import com.liyang.jpa.smart.query.db.structure.ColumnStucture;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.controller.DefaultHandler;

@Controller
@RequestMapping("${spring.jpa.restful.structure}")
@ConditionalOnProperty(name = {"spring.jpa.restful.show" })
public class StructureShowController  extends DefaultHandler {
	protected final static Logger logger = LoggerFactory.getLogger(StructureShowController.class);


	@Value(value = "${spring.jpa.restful.structure}")
	private String configStructurePath;


	private String getStructurePath(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-prefix") != null) {
			return request.getHeader("x-forwarded-prefix")  + this.configStructurePath + "/";
		} else {
			return  this.configStructurePath + "/";
		}
	}


	@ModelAttribute
	public void populateModel(Model model, HttpServletRequest request) {

		ArrayList<SimpleResource> arrayList = new ArrayList<SimpleResource>();
		HashMap<String, EntityStructure> nametostructure = SmartQuery.getNametostructure();
		Collection<EntityStructure> values = nametostructure.values();
		for (EntityStructure entityStructure : values) {
			Class<?> cls = entityStructure.getEntityClass();
				SimpleResource simpleResource = new SimpleResource();
				simpleResource.setName(entityStructure.getName());
				simpleResource.setStructureUri(getStructurePath(request) + entityStructure.getName());
				arrayList.add(simpleResource);
			
		}
		List<SimpleResource> collect = arrayList.stream().sorted(new Comparator<SimpleResource>() {

			@Override
			public int compare(SimpleResource o1, SimpleResource o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}
		}).collect(Collectors.toList());

		model.addAttribute("resourceList", collect);
		model.addAttribute("structure", configStructurePath);
	}

	@RequestMapping(path = "", method = RequestMethod.GET)
	public String resource() throws JsonProcessingException {
		return "restful_home";
	}

	@RequestMapping(path = "/{resource}", method = RequestMethod.GET)
	public String resource(@PathVariable String resource, Model model, HttpServletRequest request)
			throws JsonProcessingException {
		Class<?> entityClass = SmartQuery.getStructure(resource).getEntityClass();
		FullResource fullResource = new FullResource();
		fullResource.setResource(getStructurePath(request)+resource);
		fullResource.setTitle(resource + " - 资源");
		fullResource.setName(resource);
		Field[] declaredFields = entityClass.getDeclaredFields();
		fillFields(declaredFields, fullResource,getStructurePath(request)  + resource);
		HashMap<String, Object> postStructure = fullResource.getPostStructure();
		postStructure.remove("uuid");
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postStructure);
		fullResource.setPostStructureString(writeValueAsString);
		model.addAttribute("resource", fullResource);
		return "restful_structure";
	}

	

	private void fillFields(Field[] declaredFields, FullResource fullResource, String structurePath) {
		for (Field field : declaredFields) {

			JsonIgnore ignoreAnnotation = field.getDeclaredAnnotation(JsonIgnore.class);
			if (ignoreAnnotation != null) {
				continue;
			}

			Transient transientAnnotation = field.getDeclaredAnnotation(Transient.class);
			if (field.getName().equals("serialVersionUID")

			) {
				continue;
			}
			ObjectProperty objectProperty = new ObjectProperty();
			if (field.getType() == String.class || field.getType() == Date.class || field.getType().isEnum()
					|| field.getType() == BigDecimal.class || ContextUtils.isPackageClass(field.getType())) {
				if (!field.getName().equals("createdAt") && !field.getName().equals("updatedAt")
						&& !field.getName().equals("modifiedAt") && !field.getName().equals("version")) {
					fullResource.getPostStructure().put(field.getName(), _defautlValue(field));
				}
			} 
			if (transientAnnotation != null) {
				objectProperty.setLifeCycle(LifeCycle.TRANSIENT.desc);

			} else {
				objectProperty.setLifeCycle(LifeCycle.PERSISTENT.desc);
			}

	
			objectProperty.setName(field.getName());
			objectProperty.setDataType(field.getGenericType().getTypeName().replace("java.util.", "")
					.replace("java.lang.", "").replace("com.tianyoukeji.parent.entity.", ""));
			objectProperty.setConstraints(_constraintParse(field));
			if (field.getType().isEnum()) {
				ArrayList<String> arrayList = new ArrayList<String>();
				Object[] enumConstants = field.getType().getEnumConstants();
				for (Object enu : enumConstants) {
					arrayList.add(enu.toString());
				}

				objectProperty.setConstraints(arrayList);
			}
			objectProperty.setRelationship(_relationshipParse(field));
			fullResource.getFields().add(objectProperty);

		}
		
	}

	private Object _defautlValue(Field field) {
		if (field.getType() == String.class) {
			return "";
		} else if (field.getType() == Boolean.class) {
			return false;
		} else if (field.getType() == Character.class) {
			return "";
		} else if (field.getType().isEnum()) {
			return "";
		} else {
			return 0;
		}
	}


	private String _relationshipParse(Field field) {
		ManyToOne manyToOneAnnotation = field.getDeclaredAnnotation(ManyToOne.class);
		ManyToMany manyToManyAnnotation = field.getDeclaredAnnotation(ManyToMany.class);
		OneToOne oneToOneAnnotation = field.getDeclaredAnnotation(OneToOne.class);
		OneToMany oneToManyAnnotation = field.getDeclaredAnnotation(OneToMany.class);
		if (manyToOneAnnotation != null) {
			return "多对一";
		} else if (manyToManyAnnotation != null) {
			return "多对多";
		} else if (oneToOneAnnotation != null) {
			return "一对一";
		} else if (oneToManyAnnotation != null) {
			return "一对多";
		}
		return null;
	}

	private ArrayList<String> _constraintParse(Field field) {
		NotNull notNullAnnotation = field.getDeclaredAnnotation(NotNull.class);
		Min minAnnotation = field.getDeclaredAnnotation(Min.class);
		Max maxAnnotation = field.getDeclaredAnnotation(Max.class);
		Size sizeAnnotation = field.getDeclaredAnnotation(Size.class);
		Past pastAnnotation = field.getDeclaredAnnotation(Past.class);
		Future futureAnnotation = field.getDeclaredAnnotation(Future.class);
		NotBlank notBlankAnnotation = field.getDeclaredAnnotation(NotBlank.class);
		Length lengthAnnotation = field.getDeclaredAnnotation(Length.class);
		NotEmpty notEmptyAnnotation = field.getDeclaredAnnotation(NotEmpty.class);
		Range rangeAnnotation = field.getDeclaredAnnotation(Range.class);
		Email emailAnnotation = field.getDeclaredAnnotation(Email.class);
		Pattern patternAnnotation = field.getDeclaredAnnotation(Pattern.class);

		ArrayList<String> arrayList = new ArrayList<String>();

		if (notNullAnnotation != null) {
			arrayList.add("NotNull");
		}
		if (minAnnotation != null) {
			arrayList.add("Min(" + minAnnotation.value() + ")");
		}
		if (maxAnnotation != null) {
			arrayList.add("Max(" + maxAnnotation.value() + ")");
		}
	
		if (sizeAnnotation != null) {
			arrayList.add("Size(min=" + sizeAnnotation.min() + ", max=" + sizeAnnotation.max() + ")");
		}
		if (pastAnnotation != null) {
			arrayList.add("Past");
		}
		if (futureAnnotation != null) {
			arrayList.add("Future");
		}
		if (notBlankAnnotation != null) {
			arrayList.add("NotBlank");
		}
		if (lengthAnnotation != null) {
			arrayList.add("Length(min=" + lengthAnnotation.min() + ", max=" + lengthAnnotation.max() + ")");
		}
		if (notEmptyAnnotation != null) {
			arrayList.add("NotEmpty");
		}
		if (rangeAnnotation != null) {
			arrayList.add("Range(min=" + rangeAnnotation.min() + ", max=" + rangeAnnotation.max() + ")");
		}
		if (emailAnnotation != null) {
			arrayList.add("Email");
		}
		if (patternAnnotation != null) {
			arrayList.add("Pattern(regex=" + patternAnnotation.regexp() + ")");
		}
		return arrayList;
	}

	public static class FullResource {
		private String title;
		private String name;
		private HashMap<String, Object> postStructure = new HashMap();
		private String postStructureString;
		private String resource;
		private List<Object> fields = new ArrayList<Object>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPostStructureString() {
			return postStructureString;
		}

		public void setPostStructureString(String postStructureString) {
			this.postStructureString = postStructureString;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public HashMap<String, Object> getPostStructure() {
			return postStructure;
		}

		public void setPostStructure(HashMap<String, Object> postStructure) {
			this.postStructure = postStructure;
		}

	
		public List<Object> getFields() {
			return fields;
		}

		public void setFields(List<Object> fields) {
			this.fields = fields;
		}

		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}



	}

	public static class ObjectProperty {
		private String lifeCycle;
		private String dataType;
		private String structureUri;
		private String name;
		private String relationship;
		private ArrayList<String> constraints = new ArrayList();

		public String getStructureUri() {
			return structureUri;
		}

		public void setStructureUri(String structureUri) {
			this.structureUri = structureUri;
		}

		public String getLifeCycle() {
			return lifeCycle;
		}

		public void setLifeCycle(String lifeCycle) {
			this.lifeCycle = lifeCycle;
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRelationship() {
			return relationship;
		}

		public void setRelationship(String relationship) {
			this.relationship = relationship;
		}

		public ArrayList<String> getConstraints() {
			return constraints;
		}

		public void setConstraints(ArrayList<String> constraints) {
			this.constraints = constraints;
		}

	}

	public enum LifeCycle {
		PERSISTENT("持久化"), TRANSIENT("暂存");
		private String desc;

		LifeCycle(String desc) {
			this.desc = desc;
		}

		public String getDesc() {
			return desc;
		}

	}


	public static class SimpleResource {
		private String name;
		private String structureUri;


		public String getStructureUri() {
			return structureUri;
		}

		public void setStructureUri(String structureUri) {
			this.structureUri = structureUri;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}
