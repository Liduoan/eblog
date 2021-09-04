### Day1

- Mybatis Plus
- 首先文章显示
- 文章分类页面
- 文章内容显示

#### 首先[MyBatis-Plus ](https://github.com/baomidou/mybatis-plus)的基本知识

这部分可以看官网和相应的文档

今天使用到的是其代码生成器和建立基本的sql的流程【基于XML形式】

##### 代码生成器

AutoGenerator 是 MyBatis-Plus 的代码生成器，通过 AutoGenerator 可以快速生成 Entity、Mapper、Mapper XML、Service、Controller 等各个模块的代码，极大的提升了开发效率

首先相应的包：

```xml
		<!-- mp -->
		<dependency>
			<groupId>com.baomidou</groupId>
			<artifactId>mybatis-plus-boot-starter</artifactId>
			<version>3.2.0</version>
		</dependency>

		<!-- 代码生成器 -->
		<dependency>
			<groupId>com.baomidou</groupId>
			<artifactId>mybatis-plus-generator</artifactId>
			<version>3.2.0</version>
		</dependency>
```

之后建立CodeGenerator的Class文件

```java
// 演示例子，执行 main 方法控制台输入模块表名回车自动生成对应项目目录中
public class CodeGenerator {

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("Liduoan");
        gc.setOpen(false);
        gc.setServiceName("%sService");
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://localhost:3306/eblog?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=UTC");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("root");
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(null);
        pc.setParent("com.example.eblog");//这里设置包名字
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        // 如果模板引擎是 freemarker
        String templatePath = "/templates/mapper.xml.ftl";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/"
                        + "/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });

        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 配置自定义输出模板
        //指定自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
        // templateConfig.setEntity("templates/entity2.java");
        // templateConfig.setService();
        // templateConfig.setController();

        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperEntityClass("com.example.entity.BaseEntity");
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        strategy.setSuperControllerClass("com.example.controller.BaseController");
        strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
        strategy.setSuperEntityColumns("id", "created", "modified", "status");
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }
}
```

之后我们运行它就可以啦

这样就可以出现很多的文件，节省时间

##### 建立基本Sql

首先我们正常的话，是在Controller层调用xxxService来实现某函数

```java
@Controller
public class BaseController {

    @Autowired
    MPostService mPostService;

    //开局直接使用Mapping 锁定在index上
    @RequestMapping({"", "/", "index"})
    public String index() {
		//这里调用服务
        IPage<PostVo> results = 
        mPostService.paging(getPage(), null, null, null, null, "created");
        return "index";
    }
    
}
```

然后我们就可以在对应xxxService的接口定义函数，然后在对应的Impl实现类中具体写

```java
@Service
public class MPostServiceImpl extends ServiceImpl<MPostMapper, MPost> implements MPostService {

    //使持久层进来
    //这和我以前使用的通用Mapper不一样
    @Autowired
    private MPostMapper mPostMapper;

    @Override
    public IPage<PostVo> paging(Page page, Long categoryId, Long userId, Integer level, Boolean recommend, String order) {
        if(level==null){
            level = -1;
        }
        //相当于Where语句
        QueryWrapper wrapper = new QueryWrapper<MPost>()
                .eq(categoryId != null,  "category_id", categoryId)
                .eq(userId != null,  "user_id", userId)
                .eq(level == 0,  "level", 0)
                .gt(level > 0,  "level", 0)
                .orderByDesc(order != null, order);
        //page用来分页
        return mPostMapper.selectPost(page,wrapper);
    }
}

```

然后在在对应的Mapper【都是接口】中定义函数

此时可以使用XML方式和注解方式

XML方式的话使用[#MybatisX](https://mp.baomidou.com/guide/mybatisx-idea-plugin.html#mybatisx-快速开发插件) 插件来直接在XML中书写SQL

注解的话使用`@Select("select * from mysql_data ${ew.customSqlSegment}")`

**当然这是我们需要自定义SQL的时候，Mybatis-Plus有许多的函数可以是我们不需要写SQL**

#### 在项目启动是运行

如果你想要使用`SpringBoot`构建的项目在启动后运行一些特定的代码，那么`CommandLineRunner`、`ApplicationRunner`都是很好的选择。

在本次中我们使用如下代码

```java
@Component
public class ContextStartup implements ApplicationRunner, ServletContextAware {

    @Autowired
    MCategoryService mCategoryService;

    ServletContext servletContext;

    @Autowired
    MPostService mpostService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /**
         * 这里的查询语句是Mybatis plus 的
         * QueryWrapper 继承自 AbstractWrapper
         * 自身的内部属性 entity 也用于生成 where 条件
         * 及 LambdaQueryWrapper, 可以通过 new QueryWrapper().lambda() 方法获取
         *
         * */
        List<MCategory> categories = mCategoryService.list(new QueryWrapper<MCategory>()
                .eq("status", 0));
        servletContext.setAttribute("categorys", categories);
//        System.out.println(categories);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {

        this.servletContext = servletContext;
    }
}
```

这里一定要加上@Component注解 不然springboot不能探查到这个实现类，就不能调用run方法了

#### 在执行service的函数的返回值

这里需要明确返回值

然后定义其他的information类

如下：【Lombok注解真好用

```java
@Data
public class CommentVo extends MComment {

    private Long authorId;
    private String authorName;
    private String authorAvatar;

}
```

总的来说，在Java后端方面的数据获取都还好说，但是把数据在前端渲染不是很熟悉

更多的不熟悉在于如何分割前端页面使其组件化

#### 前端组件化

前端的组件化很需要规则，或者说技巧

这里的设计是先从index.html中进行分割

首先是页脚、导航栏、内容左右分离三大块大致分好。

然后在对剩下的做模板化，对于填充的内容做好剥离。

再在填充渲染时对公共模块进行组件化操作

----------

### Day2

- 文章热议
- 阅读量变化

本周热议

主要技术为redis的使用和操作

重新学习了下基本的redis

首先我们需要明确键-key

键是在redis中作为名字的标识【自认为

然后对应的数据结构可以是字符串、哈希、列表、集合、有序集合等等。

##### 文章热议

###### 评论量排名

首先当然是评论量查询，然后存入redis中。

基本流程：

把每一天的相应增加的评论量存入redis

然后做当前七天的做并集，得到对应的评论量排名【有序集合】

这样就可以把这个有序集合的前n篇文章的id值获取

在通过id值在redis或数据库中获得标题和浏览量。

```java
    @Override
    public void initWeekPoke() {

        // 获取7天的发表的文章
        // https://apidoc.gitee.com/loolly/hutool/
        // 这里是进行了日期的偏移处理
        List<MPost> posts = this.list(new QueryWrapper<MPost>()
                .ge("created", DateUtil.offsetDay(new Date(), -7)) // 11号
                .select("id, title, user_id, comment_count, view_count, created")
        );

        // 初始化文章的总评论量
        // 对每篇文章进行处理
        for (MPost post : posts) {
            String key = "day:rank:" + DateUtil.format(post.getCreated(), DatePattern.PURE_DATE_FORMAT);

            redisUtil.zSet(key, post.getId(), post.getCommentCount());

            // 7天后自动过期(15号发表，7-（18-15）=4)
            // 相差的天数时长
            long between = DateUtil.between(new Date(), post.getCreated(), DateUnit.DAY);
            //还有多少天就要把这个缓存删了
            long expireTime = (7 - between) * 24 * 60 * 60; // 有效时间

            redisUtil.expire(key, expireTime);

            //开始直接放入rank:post:值
            // 缓存文章的一些基本信息（id，标题，评论数量，作者）
            this.hashCachePostIdAndTitle(post, expireTime);
        }

        // 做并集
        this.zunionAndStoreLast7DayForWeekRank();
    }
```

这里的本天的键是`day:rank:20210217`，是个有序集合，然后就可以在里面放入值， 

`post.getId(), post.getCommentCount()`也就是  `文章ID  评论数`

这样做并集后也是依靠评论数做排名。

上述完成了系统初始化的时候配置redis的初始键值对

那么如何把这个排名给显示出来呢？



###### 增加评论消息

当我们增加或者删减评论的时候，我们就需要对redis进行更新

基本做法是

把redis中当天的某文章的评论值进行加减1，

然后得到对应的文章id值，进而得到对应的文章，缓存这篇文章的部分信息

再进行并集处理

【虽然我觉得这样的操作并不是最好的，我认为好的操作应该是，当我们进行rank排名的时候再来对总体做并集

而不是每一次增加删除就直接做

此外，当这篇文章不是在七天内发表的，那么可能redis中不存在键，那么我么需要重新做键



###### redis显示

我们的做法是将文章热议板块模板化

在模板化中我们首先获取并集的值

然后遍历并集中的值`rank:post:文章ID   评论数`

之后再把该文章的基本信息做个Map存入List集合

```java
/**
 * 本周热议
 */
@Component
public class HotsTemplate extends TemplateDirective {

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String getName() {
        return "hots";
    }

    @Override
    public void execute(DirectiveHandler handler) throws Exception {
        String weekRankKey = "week:rank";

        Set<ZSetOperations.TypedTuple> typedTuples = redisUtil.getZSetRank(weekRankKey, 0, 6);

        List<Map> hotPosts = new ArrayList<>();

        for (ZSetOperations.TypedTuple typedTuple : typedTuples) {
            Map<String, Object> map = new HashMap<>();
            // week:rank里面是 redisUtil.zSet(key, post.getId(), post.getCommentCount());
      
            Object value = typedTuple.getValue(); // post的id
            String postKey = "rank:post:" + value;

            map.put("id", value);
            map.put("title", redisUtil.hget(postKey, "post:title"));
            //渲染的时候，这个评论数量是从redis中获取的
            //那么数据库和redis中的评论数量是不一致的
            map.put("commentCount", typedTuple.getScore());//是可以得到有序集合的score

            hotPosts.add(map);
        }

        handler.put(RESULTS, hotPosts).render();

    }
}
```

文章热议的比较不好写的点在于redisUtil的API使用方法不熟练。

同时还有redis的设计和安排。

##### 阅读量变化

阅读量在本次中是 看为每一次访问该网页就阅读量+1

比较好的操作是用IP来判别，这里就先用上面方法这样做

首先在访问网页的方法中建立一个增加阅读量的方法

```java
    @Override
    public void putViewCount(PostVo vo) {
        //每一次浏览的时候，我们就写下这个浏览量的redis键值对
        String key = "rank:post:" + vo.getId();


        // 1、从缓存中获取viewcount
        Integer viewCount = (Integer) redisUtil.hget(key, "post:viewCount");

        // 可能键在那个文章热议那边就已经有了
        // 2、如果没有，就先从实体里面获取，再加一
        if(viewCount != null) {
            vo.setViewCount(viewCount + 1);
        } else {
            vo.setViewCount(vo.getViewCount() + 1);
        }

        // 3、写入到缓存里面
        redisUtil.hset(key, "post:viewCount", vo.getViewCount());
    }
```

那么这个阅读量是再redis中的，如何使阅读量放回数据库呢？

这里是使用定时任务来做

定时任务的配置是

在`@SpringBootApplication`下面添加`@EnableScheduling`注解

然后写个配置Class

```java
@Component
public class ViewCountSyncTask {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MPostService postService;

    /**
    * 功能描述:这里是为了存入数据库 做定时任务
    * @Date: 2021/2/19
    * @Author: Liduoan
    * @Param:
      * @param
    * @return: void
    */

    @Scheduled(cron = "0/5 * * * * *") //每分钟同步
    public void task() {

        // 把缓存中所以的rank:post的全部拿下来
        Set<String> keys = redisTemplate.keys("rank:post:*");

 
        //也就是rank:post的浏览量
 		//文章ID
        List<String> ids = new ArrayList<>();
        for (String key : keys) {
            //如果这个key里面有浏览量键值对 那么记下这个文章的id
            if(redisUtil.hHasKey(key, "post:viewCount")){
                ids.add(key.substring("rank:post:".length()));
            }
        }

        if(ids.isEmpty()) return;

        // 需要更新阅读量的文章搜索出来
        List<MPost> posts = postService.list(new QueryWrapper<MPost>().in("id", ids));

        posts.stream().forEach((post) ->{
            //更新文章的浏览量
            Integer viewCount = (Integer) redisUtil.hget("rank:post:" + post.getId(), "post:viewCount");
            post.setViewCount(viewCount);
        });

        if(posts.isEmpty()) return;

        //存入数据库
        boolean isSucc = postService.updateBatchById(posts);

        //为了防止又得重复的提交浏览量 删除那些文章的记录
        if(isSucc) {
            ids.stream().forEach((id) -> {
                redisUtil.hdel("rank:post:" + id, "post:viewCount");
                System.out.println(id + "---------------------->同步成功");
            });
        }
    }

}
```

### Day3

- 登录
- 注册
- shiro安全框架

首先注册之类的好说

##### 注册

常见的注册，一般有邮箱、用户名、密码、验证码这几块组成。

那么先是验证码，我们使用的是google提供的验证码工具

导入maven

```xml
		<!--google验证码-->
		<!--验证码-->
		<dependency>
			<groupId>com.github.axet</groupId>
			<artifactId>kaptcha</artifactId>
			<version>0.0.9</version>
		</dependency>
```

然后配置一下验证码图片的样式

```java
@Configuration
public class KaptchaConfig {
    // 验证码
    @Bean
    public DefaultKaptcha producer () {
        Properties propertis = new Properties();
        propertis.put("kaptcha.border", "no");
        propertis.put("kaptcha.image.height", "38");
        propertis.put("kaptcha.image.width", "150");
        propertis.put("kaptcha.textproducer.font.color", "black");
        propertis.put("kaptcha.textproducer.font.size", "32");
        Config config = new Config(propertis);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);

        return defaultKaptcha;
    }

}
```

之后就可以使用验证码了【做个方法

```java
    @Autowired
    Producer producer;

    /**
    * 功能描述: 二维码制作
    * @Date: 2021/2/19
    * @Author: Liduoan
    * @Param:
      * @param resp
    * @return: void
    */

    @GetMapping("/capthca.jpg")
    public void kaptcha(HttpServletResponse resp) throws IOException {

        // 验证码
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        req.getSession().setAttribute(KAPTCHA_SESSION_KEY, text);

        resp.setHeader("Cache-Control", "no-store, no-cache");
        resp.setContentType("image/jpeg");
        ServletOutputStream outputStream = resp.getOutputStream();
        ImageIO.write(image, "jpg", outputStream);
    }
```

这样当访问到`/capthca.jpg`就可以得到图片了

首先校验一下传过来的参数【为空等等

然后再去数据库中查询一下是否有重复，无重复就可以插入数据库中

```java
 @Override
    public Result register(MUser user) {
        int count = this.count(new QueryWrapper<MUser>()
                .eq("email", user.getEmail())
                .or()
                .eq("username", user.getUsername())
        );
        if(count > 0) return Result.fail("用户名或邮箱已被占用");

        //减少数据库的操作
        MUser temp = new MUser();
        temp.setUsername(user.getUsername());
        temp.setPassword(SecureUtil.md5(user.getPassword()));
        temp.setEmail(user.getEmail());
        temp.setAvatar("/res/images/avatar/default.png");

        temp.setCreated(new Date());
        temp.setPoint(0);
        temp.setVipLevel(0);
        temp.setCommentCount(0);
        temp.setPostCount(0);
        temp.setGender("0");
        this.save(temp);

        return Result.success();
    }
```

这里使用了`Result`来封装信息，帮助后续传递json参数返回。

##### 登录

登录使用了shiro框架

这个框架我不太熟悉，需要再学习一下。

但是主要流程可以说下

首先引入maven，然后做下shiro的配置

还需要写下`AccountRealm` 这种类

然后就可以书写登录流程了

```java
    @ResponseBody
    @PostMapping("/login")
    public Result doLogin(String email, String password) {
        //登录是使用邮箱和密码
        if(StrUtil.isEmpty(email) || StrUtil.isBlank(password)) {
            return Result.fail("邮箱或密码不能为空");
        }

        UsernamePasswordToken token = new UsernamePasswordToken(email, SecureUtil.md5(password));
        try {
            SecurityUtils.getSubject().login(token);

        } catch (AuthenticationException e) {
            if (e instanceof UnknownAccountException) {
                return Result.fail("用户不存在");
            } else if (e instanceof LockedAccountException) {
                return Result.fail("用户被禁用");
            } else if (e instanceof IncorrectCredentialsException) {
                return Result.fail("密码错误");
            } else {
                return Result.fail("用户认证失败");
            }
        }

        return Result.success().action("/");
    }
```

登录流程和shiro的详细请看shiro基础

##### 退出

退出其实还是使用shiro框架来做

```java
    @RequestMapping("/user/logout")
    public String logout() {
        SecurityUtils.getSubject().logout();
        return "redirect:/";
    }
```

##### 个人主页

做了简单的搜索写过的文章功能

做了简单的个人信息修改，密码更替、头像上传

头像上传的机制是先上传，然后获得图片URL，然后更新数据库，再更改shiro

### Day4

- 用户中心
- 我的信息

##### 用户中心

这里的用户中心也就是两个页面

分别是我发的帖子、我收藏的帖子

这两个部分是使用流加载的方式渲染

【就类似一开始2、3条内容，然后滚动的时候又加载几条内容

其实感觉和后端没太大关系。。。

发表的帖子和收藏的帖子都是从数据库中直接获取的



##### 我的消息

收到的消息，因为格式是：某用户  评论  某用户  的某文章  评论内容为 xxxx

那么单纯的直接获取是不足够的，需要自定义sql，来帮助获取数据



删除我的消息，也是直接对数据库中的消息进行操作，简单的使用MP来删除即可

### Day5

- 发表博客
- 发表评论
- 消息实时通知

虽然实际功能可能不多，但是所用到的有挺多的

##### 首先是统一异常管理

```java
/**
 * 全局异常处理
 */
@Slf4j
@ControllerAdvice
public class GlobalExcepitonHandler {

    @ExceptionHandler(value = Exception.class)
    public ModelAndView handler(HttpServletRequest req,
                                HttpServletResponse resp, 
                                Exception e) throws IOException {

        // ajax 处理
        String header = req.getHeader("X-Requested-With");
        if(header != null  && "XMLHttpRequest".equals(header)) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print(JSONUtil.toJsonStr(Result.fail(e.getMessage())));
            return null;
        }

        if(e instanceof NullPointerException) {
            // ...
        }

        // web处理
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

}
```

根据来自Ajax还是web，得到不一样的异常处理效果。

**小技巧**

可以使用断言配合异常处理来得到错误信息提示

类似  `Assert.isTrue(post != null, "改帖子已被删除");`

##### 发表博客

发表博客和编辑博客其实本质上是意义的页面

都是对三个模块进行编辑处理

但是编辑博客（编辑已经存在的博客）需要把原来的信息显示出来

那么就需要一个参数，说明这个文章是哪个文章？也就是PostId

注意权限问题，需要判断文章是否被删除、有无权限编辑文章等等

而发表博客

发表博客是在编辑博客之后，那么就需要判断这篇博客是否已经存在

简单的更新和增加博客内容

##### 超级管理员权限操作

对于超级管理可以使用的置顶 加精 删除操作

需要编写一个`AdminController`来帮助	

##### 发表评论

正常的Post请求，然后判断有无异常，然后添加评论

然后我们前面做了本周热议，所以需要在redis中添加该文章评论+1

之后我们既然做了评论，那么对应的产生了消息

也就是 某用户  评论  某用户  的某文章  评论内容为 xxxx

那么这个需要做到实时通知

##### 实时通知

一般的实时通知是有轮询操作

而这里是使用WebSocket，很像是消息队列的方式

也就是建立了双工道通信，不是只能客户端轮询访问服务端

这也就是说，当消息更新时—评论发表，需要通知对方

那么可以在发表评论后，调用一下websocket的方法来帮助消息传递

https://juejin.cn/post/6844903685856690184

👆是比较好的博客解释websocket



### Day6

- 搜索引擎
- mq内容实时同步

相对来说，这里才是有一点深度的地方

##### 搜索引擎

一般是使用es做的,这里是使用SpringBoot+spring-data-elasticsearch集成的方式

简单的增删改查更加容易

首先是建立**Index**

```java
@Data
@Document(indexName="post", type="post", createIndex=true)
public class PostDocment implements Serializable {

    @Id
    private Long id;
    //一条记录的Id

    // ik分词器
    @Field(type = FieldType.Text, searchAnalyzer="ik_smart", analyzer = "ik_max_word")
    private String title;
    //文章的title

    @Field(type = FieldType.Long)
    private Long authorId;
    //作者的ID

    @Field(type = FieldType.Keyword)
    private String authorName;
    private String authorAvatar;
    //作者的名字 头像

    private Long categoryId;
    //分类ID
    @Field(type = FieldType.Keyword)
    private String categoryName;
    //分类的名称

    //置顶
    private Integer level;
    //精华
    private Boolean recomment;

    //评论数量
    private Integer commentCount;
    //阅读量
    private Integer viewCount;

    @Field(type = FieldType.Date)
    private Date created;
    //时间

}
```

确立了对应索引【es里是这么说的

做好索引的工作后，我们就需要继承`ElasticsearchRepository`

然后就可以简单的增删改查了，是不是相对来说很容易

首先我们需要把数据库内的数据载入es中

批量存储

```java
@Override
    public int initEsData(List<PostVo> records) {
        if(records == null || records.isEmpty()) {
            return 0;
        }

        List<PostDocment> documents = new ArrayList<>();
        //批量存储 还是使用的postRepository的API
        for(PostVo vo : records) {
            // 映射转换
            PostDocment postDocment = modelMapper.map(vo, PostDocment.class);
            documents.add(postDocment);
        }
        postRepository.saveAll(documents);
        return documents.size();
    }
```

然后就是查找了

```java
 @Override
    public IPage search(Page page, String keyword) {
        // 分页信息 mybatis plus的page 转成 jpa的page
        Long current = page.getCurrent() - 1;
        Long size = page.getSize();
        Pageable pageable = PageRequest.of(current.intValue(), size.intValue());

        //核心代码这两行 但是怎么转换两种page？
        // 搜索es得到pageData
        MultiMatchQueryBuilder multiMatchQueryBuilder =
                QueryBuilders.multiMatchQuery(keyword,
                "title", "authorName", "categoryName");

        org.springframework.data.domain.Page<PostDocment> docments
                = postRepository.search(multiMatchQueryBuilder, pageable);

        // 结果信息 jpa的pageData转成mybatis plus的pageData
        IPage pageData = new Page(page.getCurrent(), page.getSize(), docments.getTotalElements());
        pageData.setRecords(docments.getContent());
        return pageData;
    }
```

##### RabbitMq

我们已经做好了简单的搜索和添加document到es中了。

关于我们使用MQ的方法可以看这个博客——https://juejin.cn/post/6844903580881813511#heading-0

那么当我们添加、修改、删除文章的时候，是不是需要实时的改动es中的数据

那么这里选择使用消息队列的方式

当我们修改、添加、删除文章的时候，发送一条消息到交换机中，然后交换机就可以操作后续的es变化

【我这里有种奇怪的看法 这个消息队列中是交换机把消息推送到对方，而不是对方主动索求

【而websocket是自己主动索取的

好的 don't talk ，just code

maven导入包后，我们需要配置一下操作

下面是配置队列和交换机，同时把交换机和队列绑定

```java
@Configuration
public class RabbitConfig {

    public final static String es_queue = "es_queue";
    public final static String es_exchage = "es_exchage";
    public final static String es_bind_key = "es_exchage";

    @Bean
    public Queue exQueue() {

        return new Queue(es_queue);
    }

    @Bean
    DirectExchange exchange() {

        return new DirectExchange(es_exchage);
    }

    @Bean
    Binding binding(Queue exQueue, DirectExchange exchange) {
        return BindingBuilder.bind(exQueue).to(exchange).with(es_bind_key);
    }

}
```

当我们绑定成功后，我们需要做监听操作

```java
@Slf4j
@Component
@RabbitListener(queues = RabbitConfig.es_queue)
public class MqMessageHandler {

    @Autowired
    SearchService searchService;

    @RabbitHandler
    public void handler(PostMqIndexMessage message) {

        log.info("mq 收到一条消息： {}", message.toString());

        switch (message.getType()) {
            case PostMqIndexMessage.CREATE_OR_UPDATE:
                //对不同的消息，有不同的处理方式
                searchService.createOrUpdateIndex(message);
                break;
            case PostMqIndexMessage.REMOVE:
                searchService.removeIndex(message);
                break;
            default:
                log.error("没找到对应的消息类型，请注意！！ --》 {}", message.toString());
                break;
        }
    }

}
```

现在我们可以对发送过来的消息进行处理，那么什么时候发送消息？

也就是我们需要设置消息的发送

在spring boot默认会生成AmqpAdmin和AmqpTemplate 供我们和RabbitMQ交互。 

AmqpTemplate 的默认实例是RabbitTemplate

AmqpAdmin 默认实例是RabbitAdmin，

通过源码发现其内部实现实际是RabbitTemplate。所以AmqpAdmin和AmqpTemplate两者本质是相同的

​	

```java
   //使用的
   @Autowired
   AmqpTemplate amqpTemplate;
    
    
   @ResponseBody
    @Transactional
    @PostMapping("/post/delete")
    public Result delete(Long id) {

        MPost post = mPostService.getById(id);
        Assert.notNull(post, "该帖子已被删除");
        Assert.isTrue(post.getUserId().longValue() == getProfileId().longValue(), "无权限删除此文章！");

        mPostService.removeById(id);

        // 删除相关消息、收藏等
        messageService.removeByMap(MapUtil.of("post_id", id));
        collectionService.removeByMap(MapUtil.of("post_id", id));
		
        //mq发送消息  交换机  密钥  消息内容
        amqpTemplate.convertAndSend(
            RabbitConfig.es_exchage, RabbitConfig.es_bind_key,
                new PostMqIndexMessage(post.getId(), PostMqIndexMessage.REMOVE));

        return Result.success().action("/user/index");
    }
```

### Day7











