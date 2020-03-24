##Spring的学习:
####配置bean：
    形式：
        基于xml和基于注解
    方式:
        1、全类名(反射)：
            必须要有无参构造器
        2、工厂方法(静态工厂、实例工厂)：
        3、FactoryBean
####bean类型:
    1、普通bean
    2、工厂bean，即FactoryBean
####bean的生命周期:
    不加BeanPostProcessor
        1、通过构造器或工厂方法创建bean实例
        2、为bean的属性设置值和对其它bean的引用
        3、调用bean的初始化
        4、bean可以使用
        5、容器关闭时调用bean的销毁方法
    实现BeanPostProcessor接口
        1、通过构造器或工厂方法创建bean实例
        2、为bean的属性设置值和对其它bean的引用
        3、将bean的实例传递给BeanPostProcessor的postProcessBeforeInitialization方法
        4、调用bean的初始化方法
        5、将bean的实例传递给BeanPostProcessor的postProcessAfterInitialization方法
        6、bean可以使用
        7、容器关闭时调用bean的销毁方法
    实现各种Aware和BeanPostProcessor接口:
        1、同构构造器或者工厂方法创建bean实例
        2、为bean的属性设置值和对其它bean的引用
        3、执行一系列实现Aware接口的方法
        4、执行BeanPostProcessor的postProcessBeforeInitialzation方法
        5、调用bean的初始化方法
        6、将bean的实例传递给BeanPostProcessor的postProcessAfterInitialization方法
        7、bean创建完成
        8、容器关闭时调用bean的销毁方法
####ByType和ByName的选用：
    byType:
        通过spring容器中bean的类型，为兼容性的属性赋值；
        要求spring容器中只有一个能为属性赋值的bean
    byName:        
        通过属性名和spring容器中的bean的id进行比较，若一致则可直接赋值
####注解标识组件：
    普通组件：@Component
    持久层：@Repository
    业务逻辑层：@Service
    表述控制器组件：@Controller
####@Autowired原理:
    1、先按照类型去容器找到对应的组件
        找到一个，就赋值；
        没找到，报错；
        找到多个:
            按照变量名作为id继续匹配；
                匹配上: 装配
                没有匹配上：报错
    
    
####IOC容器:
    1、ioc是一个容器
    2、容器启动的是后会创建所有的单实例bean
    3、可以从容器中获取bean
#####流程
######1、ClassPathXmlApplicationContext构造器
    public ClassPathXmlApplicationContext(
                String[] configLocations, 
                boolean refresh, 
                @Nullable ApplicationContext parent)throws BeansException {
    
            super(parent);
            setConfigLocations(configLocations);
            if (refresh) {
                // 所有单实例bean被创建完成
                refresh();
            }
        }
######2、AbstractApplicationContext:::refresh()实现
    public void refresh() throws BeansException, IllegalStateException {
            synchronized(this.startupShutdownMonitor) { // 同步锁机制
                this.prepareRefresh();
                // Spring解析xml配置文件将要创建的多有bean的配置信息保存在beanDefinitionMap（本质是ConcurrentHashMap）
                ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
                this.prepareBeanFactory(beanFactory);
    
                try {
                    this.postProcessBeanFactory(beanFactory);
                    this.invokeBeanFactoryPostProcessors(beanFactory);
                    this.registerBeanPostProcessors(beanFactory);
                    // 支持国际化功能
                    this.initMessageSource();
                    this.initApplicationEventMulticaster();
                    this.onRefresh();
                    this.registerListeners();
                    // 初始化所有单实例bean
                    this.finishBeanFactoryInitialization(beanFactory);
                    this.finishRefresh();
                } catch (BeansException var9) {
                    if (this.logger.isWarnEnabled()) {
                        this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
                    }
    
                    this.destroyBeans();
                    this.cancelRefresh(var9);
                    throw var9;
                } finally {
                    this.resetCommonCaches();
                }
    
            }
        }        
######3、AbstractApplicationContext:::finishBeanFactoryInitialization(beanFactory);实现

    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
            if (beanFactory.containsBean("conversionService") && beanFactory.isTypeMatch("conversionService", ConversionService.class)) {
                beanFactory.setConversionService((ConversionService)beanFactory.getBean("conversionService", ConversionService.class));
            }
    
            if (!beanFactory.hasEmbeddedValueResolver()) {
                beanFactory.addEmbeddedValueResolver((strVal) -> {
                    return this.getEnvironment().resolvePlaceholders(strVal);
                });
            }
    
            String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
            String[] var3 = weaverAwareNames;
            int var4 = weaverAwareNames.length;
    
            for(int var5 = 0; var5 < var4; ++var5) {
                String weaverAwareName = var3[var5];
                this.getBean(weaverAwareName);
            }
    
            beanFactory.setTempClassLoader((ClassLoader)null);
            beanFactory.freezeConfiguration();
            // 初始单实例bean
            beanFactory.preInstantiateSingletons();
        }  
######4、DefaultListableBeanFactory:::preInstantiateSingletons();实现
    public void preInstantiateSingletons() throws BeansException {
            if (logger.isTraceEnabled()) {
                logger.trace("Pre-instantiating singletons in " + this);
            }
    
            // Iterate over a copy to allow for init methods which in turn register new bean definitions.
            // While this may not be part of the regular factory bootstrap, it does otherwise work fine.
            List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
    
            // Trigger initialization of all non-lazy singleton beans...
            // 按照顺序创建bean
            for (String beanName : beanNames) {
                // 根据bean的id获取bean的定义信息
                RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
                // 判断bean不是抽象的、并且是单单例、并且不是懒加载
                if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                    // 判断是否是一个实现了FactoryBean接口的bean
                    if (isFactoryBean(beanName)) {
                        Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                        if (bean instanceof FactoryBean) {
                            final FactoryBean<?> factory = (FactoryBean<?>) bean;
                            boolean isEagerInit;
                            if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                                isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                                                ((SmartFactoryBean<?>) factory)::isEagerInit,
                                        getAccessControlContext());
                            }
                            else {
                                isEagerInit = (factory instanceof SmartFactoryBean &&
                                        ((SmartFactoryBean<?>) factory).isEagerInit());
                            }
                            if (isEagerInit) {
                                getBean(beanName);
                            }
                        }
                    }
                    else {
                        // 初始单实例bean
                        getBean(beanName);
                    }
                }
            }
    
            // Trigger post-initialization callback for all applicable beans...
            for (String beanName : beanNames) {
                Object singletonInstance = getSingleton(beanName);
                if (singletonInstance instanceof SmartInitializingSingleton) {
                    final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
                    if (System.getSecurityManager() != null) {
                        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                            smartSingleton.afterSingletonsInstantiated();
                            return null;
                        }, getAccessControlContext());
                    }
                    else {
                        smartSingleton.afterSingletonsInstantiated();
                    }
                }
            }
        }      
######5、AbstractBeanFactory:::getBean(beanName);创建bean的实例
    public Object getBean(String name) throws BeansException {
            return doGetBean(name, null, null, false);
        }
######6、AbstractBeanFactory:::doGetBean(name, null, null, false);
    protected <T> T doGetBean(final String name, 
                              @Nullable final Class<T> requiredType,
                              @Nullable final Object[] args, 
                              boolean typeCheckOnly) throws BeansException {
    
            final String beanName = transformedBeanName(name);
            Object bean;
    
            // 先从已经注册的所有单实例bean中看是否有该bean
            **Object sharedInstance = getSingleton(beanName);**
            if (sharedInstance != null && args == null) {
                if (logger.isTraceEnabled()) {
                    if (isSingletonCurrentlyInCreation(beanName)) {
                        logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
                                "' that is not fully initialized yet - a consequence of a circular reference");
                    }
                    else {
                        logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
                    }
                }
                bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
            }
    
            else {
                // Fail if we're already creating this bean instance:
                // We're assumably within a circular reference.
                if (isPrototypeCurrentlyInCreation(beanName)) {
                    throw new BeanCurrentlyInCreationException(beanName);
                }
    
                // Check if bean definition exists in this factory.
                BeanFactory parentBeanFactory = getParentBeanFactory();
                if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
                    // Not found -> check parent.
                    String nameToLookup = originalBeanName(name);
                    if (parentBeanFactory instanceof AbstractBeanFactory) {
                        return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
                                nameToLookup, requiredType, args, typeCheckOnly);
                    }
                    else if (args != null) {
                        // Delegation to parent with explicit args.
                        return (T) parentBeanFactory.getBean(nameToLookup, args);
                    }
                    else if (requiredType != null) {
                        // No args -> delegate to standard getBean method.
                        return parentBeanFactory.getBean(nameToLookup, requiredType);
                    }
                    else {
                        return (T) parentBeanFactory.getBean(nameToLookup);
                    }
                }
    
                if (!typeCheckOnly) {
                    markBeanAsCreated(beanName);
                }
    
                try {
                    final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                    checkMergedBeanDefinition(mbd, beanName, args);
    
                    // 拿到创建bean之前需要提前创建的bean，deopends-on属性，如果有就循环创建
                    **String[] dependsOn = mbd.getDependsOn();**
                    if (dependsOn != null) {
                        for (String dep : dependsOn) {
                            if (isDependent(beanName, dep)) {
                                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                        "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                            }
                            registerDependentBean(dep, beanName);
                            try {
                                getBean(dep);
                            }
                            catch (NoSuchBeanDefinitionException ex) {
                                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                        "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
                            }
                        }
                    }
    
                    // 判断是否是单实例
                    if (mbd.isSingleton()) {
                        sharedInstance = getSingleton(beanName, () -> {
                            try {
                                return createBean(beanName, mbd, args);
                            }
                            catch (BeansException ex) {
                                // Explicitly remove instance from singleton cache: It might have been put there
                                // eagerly by the creation process, to allow for circular reference resolution.
                                // Also remove any beans that received a temporary reference to the bean.
                                destroySingleton(beanName);
                                throw ex;
                            }
                        });
                        bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                    }
    
                    else if (mbd.isPrototype()) {
                        // It's a prototype -> create a new instance.
                        Object prototypeInstance = null;
                        try {
                            beforePrototypeCreation(beanName);
                            prototypeInstance = createBean(beanName, mbd, args);
                        }
                        finally {
                            afterPrototypeCreation(beanName);
                        }
                        bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
                    }
    
                    else {
                        String scopeName = mbd.getScope();
                        final Scope scope = this.scopes.get(scopeName);
                        if (scope == null) {
                            throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                        }
                        try {
                            Object scopedInstance = scope.get(beanName, () -> {
                                beforePrototypeCreation(beanName);
                                try {
                                    return createBean(beanName, mbd, args);
                                }
                                finally {
                                    afterPrototypeCreation(beanName);
                                }
                            });
                            bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                        }
                        catch (IllegalStateException ex) {
                            throw new BeanCreationException(beanName,
                                    "Scope '" + scopeName + "' is not active for the current thread; consider " +
                                    "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                                    ex);
                        }
                    }
                }
                catch (BeansException ex) {
                    cleanupAfterBeanCreationFailure(beanName);
                    throw ex;
                }
            }
    
            // Check if required type matches the type of the actual bean instance.
            if (requiredType != null && !requiredType.isInstance(bean)) {
                try {
                    T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
                    if (convertedBean == null) {
                        throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
                    }
                    return convertedBean;
                }
                catch (TypeMismatchException ex) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Failed to convert bean '" + name + "' to required type '" +
                                ClassUtils.getQualifiedName(requiredType) + "'", ex);
                    }
                    throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
                }
            }
            return (T) bean;
        }       
    
######7、DefaultSingletonBeanRegistry:::getSingleton()方法:
    /** Cache of singleton objects: bean name to bean instance. */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>
    	
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
            Assert.notNull(beanName, "Bean name must not be null");
            synchronized (this.singletonObjects) {
                // 通过get将bean从一个地方取出来
                Object singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject == null) {
                    if (this.singletonsCurrentlyInDestruction) {
                        throw new BeanCreationNotAllowedException(beanName,
                                "Singleton bean creation not allowed while singletons of this factory are in destruction " +
                                "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                    }
                    beforeSingletonCreation(beanName);
                    boolean newSingleton = false;
                    boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = new LinkedHashSet<>();
                    }
                    try {
                        // 创建bean完成
                        singletonObject = singletonFactory.getObject();
                        newSingleton = true;
                    }
                    catch (IllegalStateException ex) {
                        // Has the singleton object implicitly appeared in the meantime ->
                        // if yes, proceed with it since the exception indicates that state.
                        singletonObject = this.singletonObjects.get(beanName);
                        if (singletonObject == null) {
                            throw ex;
                        }
                    }
                    catch (BeanCreationException ex) {
                        if (recordSuppressedExceptions) {
                            for (Exception suppressedException : this.suppressedExceptions) {
                                ex.addRelatedCause(suppressedException);
                            }
                        }
                        throw ex;
                    }
                    finally {
                        if (recordSuppressedExceptions) {
                            this.suppressedExceptions = null;
                        }
                        afterSingletonCreation(beanName);
                    }
                    if (newSingleton) {
                        // 添加创建的bean
                        addSingleton(beanName, singletonObject);
                    }
                }
                return singletonObject;
            }
        }
######8、addSingleton(beanName, singletonObject)方法:
    protected void addSingleton(String beanName, Object singletonObject) {
    		synchronized (this.singletonObjects) {
    			this.singletonObjects.put(beanName, singletonObject);
    			this.singletonFactories.remove(beanName);
    			this.earlySingletonObjects.remove(beanName);
    			this.registeredSingletons.add(beanName);
    		}
    	}
######9、保存bean：
    /** Cache of singleton objects: bean name to bean instance. */
        private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    创建好的对象会最终保存在map 
        DefaultSingletonBeanRegistry：：：singletonObjects
    ioc容器之一：保存单实例bean的地方：
    ioc容器就是由许多个map组合而成的容器
######ioc中的map：
    /** Cache of singleton objects: bean name to bean instance. */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /** Cache of singleton factories: bean name to ObjectFactory. */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

    /** Cache of early singleton objects: bean name to bean instance. */
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    /** Set of registered singletons, containing the bean names in registration order. */
    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

    /** Names of beans that are currently in creation. */
    private final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /** Names of beans currently excluded from in creation checks. */
    private final Set<String> inCreationCheckExclusions =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /** List of suppressed Exceptions, available for associating related causes. */
    @Nullable
    private Set<Exception> suppressedExceptions;

    /** Flag that indicates whether we're currently within destroySingletons. */
    private boolean singletonsCurrentlyInDestruction = false;

    /** Disposable bean instances: bean name to disposable instance. */
    private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

    /** Map between containing bean names: bean name to Set of bean names that the bean contains. */
    private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

    /** Map between dependent bean names: bean name to Set of dependent bean names. */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

    /** Map between depending bean names: bean name to Set of bean names for the bean's dependencies. */
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64); 
       
    
#####启动过程：
#####启动期间做了什么:
#####如何创建单实例bean:
#####如何管理的:
#####保存在哪里:
####AOP:
#####专业术语
    切面类
        横切关注点：
        通知方法：
    连接点
    切入点：连接点的一部分
    切入点表达式：选择连接点
    
#####细节：
    1、ioc容器中保存的组件的代理对象
    2、cglib为没有接口的组件创建代理对象
#####切入点表达式:
    固定格式:
        execution(访问权限符 返回类型 方法全类名(参数表))
    通配符
        *：匹配一个或多个
        ..：匹配多层路径
#####通知方法执行顺序:
    try{
            @Beafore
            方法执行
            @AfterReturning
        }catch(){
            @AfterThrowing
        }finally{
            @After
        }
    正常执行：
        @Before--->@After--->@AfterReturning
    异常执行:
        @Before--->@After--->@AfterThrowing
#####获取方法的详细信息：
    JoinPoint joinPoint封装方法的详细信息
#####环绕通知：
    ProceedingJoinPoint pjp
    环绕通知优先于普通通知
    顺序：
        【普通前置】
        {
            try{
                [环绕前置]
                环绕执行，目标方法执行
                [环绕返回]
            }catch(){
                [环绕异常]
            }finally{
                [环绕后置]
            }
        }
        【普通后置】
        【普通方法返回/方法异常】
    新的顺序:
        环绕前置-->普通前置-->目标执行-->环绕后置-->环绕返回/环绕异常-->普通后置-->普通返回/普通异常
#####应用场景
    1、加日志保存到数据库
    2、权限验证
    3、安全检查
    4、事务控制
#####JdbcTemplate
    
#####BeanFactory和ApplicationContext：
    ApplicationContext是BeanFactory的子接口
    BeanFactory:
        bean工厂接口，负责创建bean实例；容器里面保存的所有单列bean其实就是一个map
        Spring中最低层的接口
    ApplicationContext：
        是容器接口，更多的是负责容器功能的实现，(可以基于beanFactory创建好的对象之上完成强大的容器)
        容器可以从map中获取bean，并且aop和di，都是在此接口下实现的
    BeanFactory最底层的接口，ApplicationContext留给我们使用的ioc容器接口
    
    Spring里面最大的模式就是工厂模式
        
