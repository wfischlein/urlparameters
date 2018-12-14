package com.vaadin.wolfgang.urlparameters.impl;

import com.vaadin.spring.annotation.SpringView;
import com.vaadin.wolfgang.urlparameters.ViewStateParameterFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class BeanDescriptorFactory implements BeanFactoryAware {
	@Setter
	private BeanFactory beanFactory;

	public ViewStateParameterFactory.ViewDescriptor create(String beanName) {
		return new SpringViewDescriptor(beanName, (ListableBeanFactory) beanFactory);
	}

	private class SpringViewDescriptor implements ViewStateParameterFactory.ViewDescriptor {
		@Getter
		private final String name;
		@Getter
		private final Class type;

		public SpringViewDescriptor(String beanName, ListableBeanFactory beanFactory) {
			type = beanFactory.getType(beanName);
			String viewName = beanName;
			SpringView viewAnnotation = beanFactory.findAnnotationOnBean(beanName, SpringView.class);
			if (viewAnnotation != null) {
				viewName = viewAnnotation.name();
			}
			this.name = viewName;
		}
	}
}
