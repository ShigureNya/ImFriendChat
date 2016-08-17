# ImFriendChat
基于Material Design制作的IM聊天软件，可实现目前市场上的大部分通讯需求，部分异步代码使用RxJava完成，结构清晰

更新笔记一览：

2016-8-5 新增了环信SDK和相关类库<br/>
2016-8-6 登陆界面制作完成 登陆采用RxJava异步回调的方式实现<br/>
2016-8-7 注册界面制作完成 采用ViewPager与Fragment在同一个Activity上完成了注册操作，更有效的节省了内存资源<br/>
2016-8-8 主页面布局完成 采用DrawerLayout +TabLayout + ViewPager的形式 呈现出一个完整的Material Design设计风格<br/>
2016-8-9 实现会话页面布局和数据加载，数据加载采用RecyclerView和RecyclerViewAdapter的形式完成<br/>
2016-8-10 修复了会话列表不能自动刷新的错误，修复了会话列表会重复刷新的错误<br/>
......过生日，回家嗨皮了......<br/>
2016-8-14 完成了聊天页面的基本布局，增加了聊天页面的Adapter适配器<br/>
2016-8-15 聊天页面由于数据交互频繁，此处不再使用RecyclerView实现，该用ListView + BaseAdapter的形式 这种方法需手动实现ViewHolder<br/>
2016-8-16 完成了一对一聊天的功能！<br/>
2016-8-17 新增了聊天的三级缓存。 发现问题：图片刷新时错位，高分辨率图片会OOM。

