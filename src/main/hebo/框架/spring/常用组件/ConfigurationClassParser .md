在`ConfigurationClassPostProcessor#processConfigBeanDefinitions`方法中创建了`ConfigurationClassParser`对象并调用其`parse`方法。该方法就是在负责解析配置类、扫描包、注册`BeanDefinition`

 根据配置类不同的 BeanDefinition 实例对象 调用不同的 parse 方法 		// 底层其实都是在调用 org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass 