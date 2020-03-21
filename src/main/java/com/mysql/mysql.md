###explain的学习
    ·使用方法: explain SQL语句
    ·做什么:
        1、表的读取顺序
        2、数据读取操作的操作类型
        3、哪些索引可以使用
        4、哪些索引被实际使用
        5、表之间的引用
        6、每张表有多少行被优化器查询
####索引的语法:
    创建
        create [unique] index 索引名 on 表名(字段名(长度),...)
    修改
        alter table 表名 add [unique] index [索引名](字段名(长度),..)
    删除
        drop index [索引名] on 表名
    查看
        show index from 表名
####字段解析:
    1、id：表示执行select或操作表的顺序
        ·相同：从上到下顺序执行
        ·不同：如果为子查询，id自增，id越大，先执行
        ·两种情况都有：先执行id大的，然后顺序执行
    2、select_type：查询类型（6种）
        simple（不包含子查询和union）\primary\subquery\derived\union\union result
        主要区分普通、联合、子查询
    3、table：显示这一行数据是关于哪张表的 
    4、partitions
    5、type：访问类型
        好->坏
        system--> const --> eq-ref --> ref --> range --> index --> all 
    6、possible_keys 
        显示可能应用在这张表上的索引，不一定被查询实际用到
    7、key  
        实际使用的索引，null为没有用到索引
        若查询中使用覆盖索引，则该索引仅出现在key列表中
    8、key_len：索引中使用的字节数
        表示索引字段的最大可能的长度，非实际长度
    9、ref  
        显示索引的哪一列被使用了，如果可能的话，
        是一个常量，哪些列或常量被用于查找索引项的值
    10、rows
        根据表统计的信息机索引选用情况，大致估算出所需要读取的行数。
    11、filtered
    
    12、Extra
        额外信息
        Using filesort: 对数据使用一个外部的索引排序
            explain select * from user where age >10 order by email;
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
                | id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra                       |
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
                |  1 | SIMPLE      | user  | NULL       | ALL  | idx_age       | NULL | NULL    | NULL |   14 |    92.86 | Using where; Using filesort |
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
        Using temporary: 使用了临时表保存中间结果，常见于排序order by 和分组查询group by
             explain select * from user where age >10  group by version order by email;
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------+
                | id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra                                        |
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------+
                |  1 | SIMPLE      | user  | NULL       | ALL  | idx_age       | NULL | NULL    | NULL |   14 |    92.86 | Using where; Using temporary; Using filesort |
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------+    
        Using index: 使用了索引
             explain select age from user;
                +----+-------------+-------+------------+-------+---------------+---------+---------+------+------+----------+-------------+
                | id | select_type | table | partitions | type  | possible_keys | key     | key_len | ref  | rows | filtered | Extra       |
                +----+-------------+-------+------------+-------+---------------+---------+---------+------+------+----------+-------------+
                |  1 | SIMPLE      | user  | NULL       | index | NULL          | idx_age | 5       | NULL |   14 |   100.00 | Using index |
                +----+-------------+-------+------------+-------+---------------+---------+---------+------+------+----------+-------------+
        
        Covering index: 覆盖索引，不使用select *
        
        using join buffer: 使用连接缓存
        
        impossible where: where子句总是false
        
        select tables optimized away: 没有group by 情况下，基于索引优化min和max操作
        
        distinct: 优化distinct操作
        
        
###索引优化:
####表优化:
    单表:
       explain select id,author_id,category_id from article where category_id=1 and comments>1 order by views desc limit 1;
           +----+-------------+---------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
           | id | select_type | table   | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra                       |
           +----+-------------+---------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
           |  1 | SIMPLE      | article | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    4 |    25.00 | Using where; Using filesort |
           +----+-------------+---------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
       建索引为： create index idx_ccv on article(category_id,comments,views);   
       explain select id,author_id,category_id from article where category_id=1 and comments>1 order by views desc limit 1;
           +----+-------------+---------+------------+-------+---------------+---------+---------+------+------+----------+---------------------------------------+
           | id | select_type | table   | partitions | type  | possible_keys | key     | key_len | ref  | rows | filtered | Extra                                 |
           +----+-------------+---------+------------+-------+---------------+---------+---------+------+------+----------+---------------------------------------+
           |  1 | SIMPLE      | article | NULL       | range | idx_ccv       | idx_ccv | 10      | NULL |    1 |   100.00 | Using index condition; Using filesort |
           +----+-------------+---------+------------+-------+---------------+---------+---------+------+------+----------+---------------------------------------+   
           
       通过 Extra 字段发现索引失效，所以删除索引，重新建立索引
       create index idx_cv article(category_id,views);
       explain select id,author_id,category_id from article where category_id=1 and comments>1 order by views desc limit 1;
           +----+-------------+---------+------------+------+---------------+--------+---------+-------+------+----------+----------------------------------+
           | id | select_type | table   | partitions | type | possible_keys | key    | key_len | ref   | rows | filtered | Extra                            |
           +----+-------------+---------+------------+------+---------------+--------+---------+-------+------+----------+----------------------------------+
           |  1 | SIMPLE      | article | NULL       | ref  | idx_cv        | idx_cv | 5       | const |    2 |    33.33 | Using where; Backward index scan |
           +----+-------------+---------+------------+------+---------------+--------+---------+-------+------+----------+----------------------------------+
    两表:
        explain select * from class left join book on book.card = class.card;
            +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------------+
            | id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra                                              |
            +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------------+
            |  1 | SIMPLE      | class | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    1 |   100.00 | NULL                                               |
            |  1 | SIMPLE      | book  | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    1 |   100.00 | Using where; Using join buffer (Block Nested Loop) |
            +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------------+
        1、book表建索引
            建索引：alter table book add index(card);
            explain select * from class left join book on class.card =book.card;
                +----+-------------+-------+------------+------+---------------+------+---------+------------------------+------+----------+-------------+
                | id | select_type | table | partitions | type | possible_keys | key  | key_len | ref                    | rows | filtered | Extra       |
                +----+-------------+-------+------------+------+---------------+------+---------+------------------------+------+----------+-------------+
                |  1 | SIMPLE      | class | NULL       | ALL  | NULL          | NULL | NULL    | NULL                   |    1 |   100.00 | NULL        |
                |  1 | SIMPLE      | book  | NULL       | ref  | card          | card | 5       | myemployees.class.card |    1 |   100.00 | Using index |
                +----+-------------+-------+------------+------+---------------+------+---------+------------------------+------+----------+-------------+
        2、class表建索引
             建索引：alter table class add index(card);  
             explain select * from class left join book on class.card = book.card;
                 +----+-------------+-------+------------+-------+---------------+------+---------+------+------+----------+----------------------------------------------------+
                 | id | select_type | table | partitions | type  | possible_keys | key  | key_len | ref  | rows | filtered | Extra                                              |
                 +----+-------------+-------+------------+-------+---------------+------+---------+------+------+----------+----------------------------------------------------+
                 |  1 | SIMPLE      | class | NULL       | index | NULL          | card | 5       | NULL |    1 |   100.00 | Using index                                        |
                 |  1 | SIMPLE      | book  | NULL       | ALL   | NULL          | NULL | NULL    | NULL |    1 |   100.00 | Using where; Using join buffer (Block Nested Loop) |
                 +----+-------------+-------+------------+-------+---------------+------+---------+------+------+----------+----------------------------------------------------+          
        结论
            **_左连接，在右表建立索引；_**
            **_右连接，在左表建立索引_**
    三表：
        不加索引情况：
             explain select * from class c left join book b on c.card=b.card left join phone p on c.card =p.card;
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------------+
                | id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra                                              |
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------------+
                |  1 | SIMPLE      | c     | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    1 |   100.00 | NULL                                               |
                |  1 | SIMPLE      | b     | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    1 |   100.00 | Using where; Using join buffer (Block Nested Loop) |
                |  1 | SIMPLE      | p     | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    1 |   100.00 | Using where; Using join buffer (Block Nested Loop) |
                +----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+----------------------------------------------------+
        1、book表建索引
             alter table book add index(card);   
             explain select * from class c left join book b on c.card=b.card left join phone p on c.card =p.card;
               +----+-------------+-------+------------+------+---------------+------+---------+--------------------+------+----------+----------------------------------------------------+
               | id | select_type | table | partitions | type | possible_keys | key  | key_len | ref                | rows | filtered | Extra                                              |
               +----+-------------+-------+------------+------+---------------+------+---------+--------------------+------+----------+----------------------------------------------------+
               |  1 | SIMPLE      | c     | NULL       | ALL  | NULL          | NULL | NULL    | NULL               |    1 |   100.00 | NULL                                               |
               |  1 | SIMPLE      | b     | NULL       | ref  | card          | card | 5       | myemployees.c.card |    1 |   100.00 | Using index                                        |
               |  1 | SIMPLE      | p     | NULL       | ALL  | NULL          | NULL | NULL    | NULL               |    1 |   100.00 | Using where; Using join buffer (Block Nested Loop) |
               +----+-------------+-------+------------+------+---------------+------+---------+--------------------+------+----------+----------------------------------------------------+    
        2、phone表建索引
              alter table phone add index(card);
              explain select * from class c left join book b on c.card=b.card left join phone p on c.card =p.card;
                  +----+-------------+-------+------------+------+---------------+------+---------+--------------------+------+----------+-------------+
                  | id | select_type | table | partitions | type | possible_keys | key  | key_len | ref                | rows | filtered | Extra       |
                  +----+-------------+-------+------------+------+---------------+------+---------+--------------------+------+----------+-------------+
                  |  1 | SIMPLE      | c     | NULL       | ALL  | NULL          | NULL | NULL    | NULL               |    1 |   100.00 | NULL        |
                  |  1 | SIMPLE      | b     | NULL       | ref  | card          | card | 5       | myemployees.c.card |    1 |   100.00 | Using index |
                  |  1 | SIMPLE      | p     | NULL       | ref  | card          | card | 5       | myemployees.c.card |    1 |   100.00 | Using index |
                  +----+-------------+-------+------------+------+---------------+------+---------+--------------------+------+----------+-------------+      
    join语句的优化：
        尽可能减少join语句中的Nested Loop的循环次数，"永远用小结果集驱动大的结果集"
        优先优化Nested Loop的内层循环
        保证join语句中被驱动表上join条件字段已经被索引化
        当无法保证被驱动表的join条件字段被索引，期望内存资源充足的前提下，设置JoinBuffer的大小
                     
####索引失效:
    防止索引失效:
        1、全值匹配我最爱；
        2、最佳最前缀法则；（带头大哥不能死，中间兄弟不能断）
        3、不在索引列上做任何操作（计算、函数、（自动或手动）类型转换），会导致索引失效二转向全表扫描
        4、存储引擎不能使用索引范围中范围条件右边的列
        5、尽量使用覆盖索引
        6、使用不等于（!=或<>）的时候无法使用索引
        7、like以通配符开头，索引会失效转向全表扫描；（可以通过覆盖索引）
        8、字符串不加单引号索引失效
        9、少用or，用它连接索引会失效
        10、is null、is not null 无法使用索引
        
        
