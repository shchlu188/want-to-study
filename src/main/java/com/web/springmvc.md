##Spring Mvc的学习:
###运行流程:
    1、客户端发送请求：
    2、来到tomcat服务器：
    3、SpringMvc的前端控制器接收所有请求
    4、通过请求地址和@RequestMapping标注的哪个匹配，来找到使用哪个类的的方法
    5、前端控制器找到目标处理器和目标方法，直接利用返回执行目标方法
    6、方法执行完成回友一个返回值，SpringMvc认为这个返回值就是要去的页面名
    7、拿到方法的返回值，通过视图解析器进行拼串得到完整的页面地址
    8、拿到页面地址，前端控制器帮我们转发页面
###@RequestMapping：
    表示请求的类型
    String name() default "";
    @AliasFor("path")
    String[] value() default {};
    @AliasFor("value")
    String[] path() default {};
    // 请求方法类型
    RequestMethod[] method() default {};
    // 请求参数
    String[] params() default {};
    // 规定请求头
    String[] headers() default {};
    // 指接收类型是那种的请求，Content-Type
    String[] consumes() default {};
    // 告诉浏览器返回的内容的类型，给响应头加上Content-Type
    String[] produces() default {};
###@RequestParam
    获取请求参数，默认必须带
    value:指定要获取的请求参数的key
    required：这个参数是否必须
    defaultValue:默认值，没带为null
###@RequestHandler
    获取请求头
###@PathVariable:
    获取请求路径上的值
###@CookieValue:
    
###默认配置文件(spring mvc)：
    /WEB-INF/前端控制器名-servlet.xml
    
    拦截配置:
        /：拦截所有请求，不拦截jsp页面，*.jsp请求
        /*:拦截所有请求包括jsp页面，*.jsp请求
        处理*.jsp时tomcat的事情
        
        
        DefaultServlet是Tomcat中处理静态资源
            除jsp和servlet外剩下的都是静态资源
            index.html，静态资源，tomcat就会在服务器下找到这个资源并返回
            我们前端控制器/禁用了tomcat服务器中的DefaultServlet
            
            
            1、web.xml中有一个DefaultServlet是url-pattern=/
            2、前端控制器配置的是url-pattern=/
                静态资源会来到DispatcherServletk
    
###POJO对象
    spring mvc会自动的为这个POJO进行赋值？
        将POJO中的每一属性，从reques参数中尝试获取出来，并封装即可。
        还可以级联赋值
###spring mvc的参数:
    1、直接在参数上写原生API：
        HttpServletRequest
        HttpSession
        java.security.Principal
        Locale
        InputStream
        OutputStream
        Reader
        Writer
        
###spring mvc传入参数：
    1、Model、Map、ModelMap:
    最终都是BindingAwareModelMap在工作。
    相当于给BindingAwareModelMap中保存的东西都会被放在请求域中。
    Map(Interface(jdk))    Model(Interface(spring))
        ||                              //
        ||                             //
        \/                            //
      ModelMap                       //
            \                       /
             \/                   \/
                ExtendedModelMap        
                        ||                
                        \/
                BindingAwareModelMap
    2、ModelAndView
###Spring mvc临时保存数据的方式:
    1、@SessionAttribute 标注在类上
    2、@ModelAttribute：标注在方法上
        标注该注解的方法会提前目标方法先执行
    
###Spring mvc源码:
    发送请求
        HttpServlet------->doGet\doPost
        HttpServletBean         |
                               \/
        FrameWorkServlet ---->processRequest(request,response)--->doService
        DispatcherServlet           doservice--->doDispatch(request,response)
####1、DispatcherServlet:::doDispatch
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
            HttpServletRequest processedRequest = request;
            HandlerExecutionChain mappedHandler = null;
            boolean multipartRequestParsed = false;
            WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
    
            try {
                try {
                    ModelAndView mv = null;
                    Object dispatchException = null;
    
                    try {
                        // 1、检查是否是文件上传请求
                        processedRequest = this.checkMultipart(request);
                        multipartRequestParsed = processedRequest != request;
                        // 2、根据当前请求地址找到哪个类来处理；
                        mappedHandler = this.getHandler(processedRequest);
                        // 3、如果没有找到哪个处理器能处理这个请求就404，或者抛出异常
                        if (mappedHandler == null) {
                            this.noHandlerFound(processedRequest, response);
                            return;
                        }
                        // 4、拿到能执行这个类的所有的方法的适配器（反射工具）AnnotationMethodHandlerAdapter
                        HandlerAdapter ha = this.getHandlerAdapter(mappedHandler.getHandler());
                        String method = request.getMethod();
                        boolean isGet = "GET".equals(method);
                        if (isGet || "HEAD".equals(method)) {
                            long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                            if ((new ServletWebRequest(request, response)).checkNotModified(lastModified) && isGet) {
                                return;
                            }
                        }
    
                        if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                            return;
                        }
                        // 处理器的方法被调用了
                        // 控制器（Controller）、处理器（Handler）
                        // 5、适配器来执行目标方法：将目标方法的返回值作为视图名，设置保存在ModelAndView中
                        //    目标方法无论怎么写，适配器执行完成之后都保存在ModelAndView中
                        mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
                        if (asyncManager.isConcurrentHandlingStarted()) {
                            return;
                        }
                        // 如果没有视图，默认设置一个
                        this.applyDefaultViewName(processedRequest, mv);
                        mappedHandler.applyPostHandle(processedRequest, response, mv);
                    } catch (Exception var20) {
                        dispatchException = var20;
                    } catch (Throwable var21) {
                        dispatchException = new NestedServletException("Handler dispatch failed", var21);
                    }
                    // 转发到目标页面
                    // 6、根据方法最终执行完成后封装的ModelAndView：转发到对应页面，而且ModelAndView中的数据可以从请求域中获取
                    this.processDispatchResult(processedRequest, response, mappedHandler, mv, (Exception)dispatchException);
                } catch (Exception var22) {
                    this.triggerAfterCompletion(processedRequest, response, mappedHandler, var22);
                } catch (Throwable var23) {
                    this.triggerAfterCompletion(processedRequest, response, mappedHandler, new NestedServletException("Handler processing failed", var23));
                }
    
            } finally {
                if (asyncManager.isConcurrentHandlingStarted()) {
                    if (mappedHandler != null) {
                        mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
                    }
                } else if (multipartRequestParsed) {
                    this.cleanupMultipart(processedRequest);
                }
    
            }
        }
#####简略步骤:
    1、所有请求过来DispatcherServlet收到请求
    2、调用doDispatch()方法处理
        1)、getHandler():根据请求的地址找到能够处理该请求的目标方法
                根据当前请求在HandlerMapping中找到这个请求的映射信息，获取相应的处理器类
        2)、getHandlerAdapter()：根据当前处理器类获取到能够执行这个处理器方法的适配器
                根据当前处理器，找到当前类的HandlerAdapter
        3)、使用干菜获取到的适配器(AnnotationMethodAdapter)执行目标方法
        4)、目标方法执行完成之后会返回一个ModelAndViewduixiang
        5)、再根据ModelAndView的信息转发到具体的页面，并可以再请求域中获取ModelAndView的模型数据
######getHandler()细节:
    // 返回目标处理器的执行链
    // handlerMap： ioc容器自动创建Controller对象的时候会扫描每个处理器都能处理什么请求，保存在HandlerMapping的HandlerMap中
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
            // HandlerMapping 处理器映射器： 保存的是每一个处理器能处理哪些方法的映射信息
            if (this.handlerMappings != null) {
                Iterator var2 = this.handlerMappings.iterator();
    
                while(var2.hasNext()) {
                    HandlerMapping mapping = (HandlerMapping)var2.next();
                    HandlerExecutionChain handler = mapping.getHandler(request);
                    if (handler != null) {
                        return handler;
                    }
                }
            }
    
            return null;
        }
######getHandlerAdapter()细节:
    // HttpRequestHandlerAdapter
    // SimpleControllerHandlerAdapter
    // AnnotationMethodHandlerAdapter 能解析注解方法的适配器
    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
            if (this.handlerAdapters != null) {
                Iterator var2 = this.handlerAdapters.iterator();
    
                while(var2.hasNext()) {
                    HandlerAdapter adapter = (HandlerAdapter)var2.next();
                    if (adapter.supports(handler)) {
                        return adapter;
                    }
                }
            }
    
            throw new ServletException("No adapter for handler [" + handler + "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
        }
        
####Spring Mvc九大组件:
#####概述：
    关键位置都是由这些组件来完成
    全部都是接口；接口就是规范，增强扩展性
        // 文件上传组件
        private MultipartResolver multipartResolver; 
        // 区域信息解析器 国际化有关
        private LocaleResolver localeResolver;
        // 主题解析器
        private ThemeResolver themeResolver;
        // handler映射器
        private List<HandlerMapping> handlerMappings;
        // handler适配器
        private List<HandlerAdapter> handlerAdapters;
        // 异常解析器
        private List<HandlerExceptionResolver> handlerExceptionResolvers;
        // 
        private RequestToViewNameTranslator viewNameTranslator;
        // 允许重定向携带数据的功能
        private FlashMapManager flashMapManager;
        // 视图解析器
        private List<ViewResolver> viewResolvers;
######初始化的地方：
    DispatcherServlet
        protected void initStrategies(ApplicationContext context) {
                this.initMultipartResolver(context);
                this.initLocaleResolver(context);
                this.initThemeResolver(context);
                this.initHandlerMappings(context);
                this.initHandlerAdapters(context);
                this.initHandlerExceptionResolvers(context);
                this.initRequestToViewNameTranslator(context);
                this.initViewResolvers(context);
                this.initFlashMapManager(context);
            }
######初始化HandlerMapping：
    可以在web.xml修改属性
    private void initHandlerMappings(ApplicationContext context) {
            this.handlerMappings = null;
            // 默认为true
            if (this.detectAllHandlerMappings) {
                
                Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
                if (!matchingBeans.isEmpty()) {
                    this.handlerMappings = new ArrayList(matchingBeans.values());
                    AnnotationAwareOrderComparator.sort(this.handlerMappings);
                }
            } else {
                try {
                    HandlerMapping hm = (HandlerMapping)context.getBean("handlerMapping", HandlerMapping.class);
                    this.handlerMappings = Collections.singletonList(hm);
                } catch (NoSuchBeanDefinitionException var3) {
                }
            }
    
            if (this.handlerMappings == null) {
                this.handlerMappings = this.getDefaultStrategies(context, HandlerMapping.class);
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("No HandlerMappings declared for servlet '" + this.getServletName() + "': using default strategies from DispatcherServlet.properties");
                }
            }
    
        }
    