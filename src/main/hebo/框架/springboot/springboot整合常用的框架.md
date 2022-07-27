#### springboot整合Mybaits

@MapperScan不是必须的，MybatisAutoConfiguration会自动在内部@Import({ AutoConfiguredMapperScannerRegistrar.class })中通过List<String> packages = AutoConfigurationPackages.get(this.beanFactory);默认springboot的包名作为缺省路径进行扫描